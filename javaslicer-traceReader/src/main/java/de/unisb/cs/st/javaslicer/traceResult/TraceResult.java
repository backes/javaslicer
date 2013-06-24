/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult
 *    Class:     TraceResult
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/TraceResult.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.javaslicer.traceResult;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.PushbackInputStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.GZIPInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.hammacher.util.MultiplexedFileReader;
import de.hammacher.util.MultiplexedFileReader.MultiplexInputStream;
import de.hammacher.util.StringCacheInput;
import de.unisb.cs.st.javaslicer.common.classRepresentation.AbstractInstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.AbstractInstructionInstanceFactory;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstanceFactory;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.AbstractInstruction;
import de.unisb.cs.st.javaslicer.common.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.common.progress.ConsoleProgressMonitor;
import de.unisb.cs.st.javaslicer.common.progress.ProgressMonitor;

public class TraceResult {

    private final static class ThreadIdList extends AbstractList<ThreadId> {

        private final List<ThreadTraceResult> wrappedThreadTraces;

        public ThreadIdList(List<ThreadTraceResult> threadTraces) {
            this.wrappedThreadTraces = threadTraces;
        }

        @Override
        public ThreadId get(final int index) {
            return this.wrappedThreadTraces.get(index).getId();
        }

        @Override
        public int size() {
            return this.wrappedThreadTraces.size();
        }
    }

    private final List<ReadClass> readClasses;
    private final List<ThreadTraceResult> threadTraces;

    private final Instruction[] instructions;

    public TraceResult(File filename) throws IOException {
        final MultiplexedFileReader file = new MultiplexedFileReader(filename);
        if (file.getStreamIds().size() < 2)
            throw new IOException("corrupted data");
        final MultiplexInputStream readClassesStream = file.getInputStream(0);
        if (readClassesStream == null)
            throw new IOException("corrupted data");
        PushbackInputStream pushBackInput =
            new PushbackInputStream(new BufferedInputStream(
                    new GZIPInputStream(readClassesStream, 512), 512), 1);
        final DataInputStream readClassesInputStream = new DataInputStream(
                pushBackInput);
        final ArrayList<ReadClass> readClasses0 = new ArrayList<ReadClass>();
        final StringCacheInput stringCache = new StringCacheInput();
        int testRead;
        while ((testRead = pushBackInput.read()) != -1) {
            pushBackInput.unread(testRead);
            readClasses0.add(ReadClass.readFrom(readClassesInputStream, stringCache));
        }
        readClasses0.trimToSize();
        Collections.sort(readClasses0);
        this.readClasses = readClasses0;
        this.instructions = getInstructionArray(readClasses0);

        final MultiplexInputStream threadTracersStream = file.getInputStream(1);
        if (threadTracersStream == null)
            throw new IOException("corrupted data");
        pushBackInput = new PushbackInputStream(new BufferedInputStream(
                new GZIPInputStream(threadTracersStream, 512), 512), 1);
        final DataInputStream threadTracersInputStream = new DataInputStream(
                pushBackInput);

        final ArrayList<ThreadTraceResult> threadTraces0 = new ArrayList<ThreadTraceResult>();
        while ((testRead = pushBackInput.read()) != -1) {
            pushBackInput.unread(testRead);
            threadTraces0.add(ThreadTraceResult.readFrom(threadTracersInputStream, this, file));
        }
        threadTraces0.trimToSize();
        Collections.sort(threadTraces0);
        this.threadTraces = threadTraces0;
    }

    private static Instruction[] getInstructionArray(final List<ReadClass> classes) throws IOException {
        int numInstructions = 0;
        for (final ReadClass c: classes)
            if (c.getInstructionNumberEnd() > numInstructions)
                numInstructions = c.getInstructionNumberEnd();
        final Instruction[] instructions = new Instruction[numInstructions];
        for (final ReadClass c: classes) {
            for (final ReadMethod m: c.getMethods()) {
                for (final AbstractInstruction instr: m.getInstructions()) {
                    if (instructions[instr.getIndex()] != null)
                        throw new IOException("Same instruction index given twice.");
                    instructions[instr.getIndex()] = instr;
                }
            }
        }

        return instructions;
    }

    public static TraceResult readFrom(final File filename) throws IOException {
        return new TraceResult(filename);
    }

    /**
     * Returns an iterator that iterates backwards through the execution trace.
     *
     * This iteration is very cheap since no information has to be cached (in
     * contrast to the Iterator returned by
     * {@link #getForwardIterator(ThreadId, InstructionInstanceFactory)}.
     * The trace is generated while reading in the trace file.
     *
     * @param threadId the identifier of the thread whose execution trace
     *                 iterator is requested
     * @param filter   a filter to ignore certain instruction instances.
     *                 may be <code>null</code>.
     * @param instanceFactory a factory that creates the instruction instance objects
     * @return an iterator that iterates backwards through the execution trace.
     *         the iterator extends {@link Iterator} over {@link InstructionInstance}.
     */
    public <InstanceType extends InstructionInstance> BackwardTraceIterator<InstanceType> getBackwardIterator(final ThreadId threadId,
            final InstanceFilter<? super InstanceType> filter, InstructionInstanceFactory<? extends InstanceType> instanceFactory) {
        final ThreadTraceResult res = findThreadTraceResult(threadId);
        return res == null ? null : res.getBackwardIterator(filter, instanceFactory);
    }

    /**
     * @see #getBackwardIterator(ThreadId, InstanceFilter)
     */
    public BackwardTraceIterator<InstructionInstance> getBackwardIterator(
            final ThreadId threadId, final InstanceFilter<? super InstructionInstance> filter) {
        InstructionInstanceFactory<? extends InstructionInstance> factory = new AbstractInstructionInstanceFactory();
        return getBackwardIterator(threadId, filter, factory);
    }

    /**
     * Returns an iterator that iterates backwards through the execution trace.
     *
     * This iteration is very cheap since no information has to be cached (in
     * contrast to the Iterator returned by
     * {@link #getForwardIterator(ThreadId, InstructionInstanceFactory)}.
     * The trace is generated while reading in the trace file.
     *
     * @param javaThreadId the java thread id of the thread whose execution trace
     *                     iterator is requested
     * @param filter       a filter to ignore certain instruction instances.
     *                     may be <code>null</code>.
     * @return an iterator that iterates backwards through the execution trace.
     *         the iterator extends {@link Iterator} over {@link InstructionInstance}.
     */
    public BackwardTraceIterator<AbstractInstructionInstance> getBackwardIterator(
            final long javaThreadId, InstanceFilter<? super AbstractInstructionInstance> filter) {
        final ThreadId id = getThreadId(javaThreadId);
        return id == null ? null : getBackwardIterator(id, filter, new AbstractInstructionInstanceFactory());
    }

    /**
     * Returns an iterator that is able to iterate in any direction through the execution trace.
     *
     * This iteration is usually much more expensive (especially with respect to memory
     * consumption) than the Iterator returned by {@link #getBackwardIterator(long, InstanceFilter)}.
     * So whenever you just need to iterate backwards, you should use that backward iterator.
     *
     * @param threadId the identifier of the thread whose execution trace
     *                 iterator is requested
     * @param instanceFactory a factory which is used to create the instruction instance objects.
     *                        may be used to return special objects which can be annotated by the user
     *                        of this function.
     * @return an iterator that is able to iterate in any direction through the execution trace.
     *         the iterator extends {@link ListIterator} over {@link InstructionInstance}.
     */
    public <InstanceType extends InstructionInstance> ForwardTraceIterator<InstanceType> getForwardIterator(
            final ThreadId threadId, InstructionInstanceFactory<InstanceType> instanceFactory) {
        final ThreadTraceResult res = findThreadTraceResult(threadId);
        return res == null ? null : res.getForwardIterator(instanceFactory);
    }

    /**
     * Returns an iterator that is able to iterate in any direction through the execution trace.
     *
     * This iteration is usually much more expensive (especially with respect to memory
     * consumption) than the Iterator returned by {@link #getBackwardIterator(long, InstanceFilter)}.
     * So whenever you just need to iterate backwards, you should use that backward iterator.
     *
     * @param javaThreadId the java thread id of the thread whose execution trace
     *                     iterator is requested
     * @param instanceFactory a factory which is used to create the instruction instance objects.
     *                        may be used to return special objects which can be annotated by the user
     *                        of this function.
     * @return an iterator that is able to iterate in any direction through the execution trace.
     *         the iterator extends {@link ListIterator} over {@link InstructionInstance}.
     */
    public <InstanceType extends InstructionInstance> ForwardTraceIterator<InstanceType> getIterator(
            final long javaThreadId, InstructionInstanceFactory<InstanceType> instanceFactory) {
        final ThreadId id = getThreadId(javaThreadId);
        return id == null ? null : getForwardIterator(id, instanceFactory);
    }

    private ThreadTraceResult findThreadTraceResult(final ThreadId threadId) {
        // binary search
        int left = 0;
        int right = this.threadTraces.size();
        int mid;

        while ((mid = (left + right) / 2) != left) {
            final ThreadTraceResult midVal = this.threadTraces.get(mid);
            if (midVal.getId().compareTo(threadId) <= 0)
                left = mid;
            else
                right = mid;
        }

        final ThreadTraceResult found = this.threadTraces.get(mid);
        return found.getId().compareTo(threadId) == 0 ? found : null;
    }

    /**
     * Returns a sorted List of all threads that are represented
     * by traces in this TraceResult.
     *
     * @return the sorted list of {@link ThreadId}s.
     */
    public List<ThreadId> getThreads() {
        return new ThreadIdList(this.threadTraces);
    }

    /**
     * Returns a sorted List of all {@link ReadClass}es.
     *
     * @return a sorted List of all {@link ReadClass}es
     */
    public List<ReadClass> getReadClasses() {
        return Collections.unmodifiableList(this.readClasses);
    }

    /**
     * Search for the {@link ReadClass} with the given class name.
     *
     * @param name the java class name to search for (e.g. java.lang.String)
     * @return the {@link ReadClass} with the given class name, or
     *         <code>null</code> if this TraceResult does not contain
     *         a ReadClass with that name
     */
    public ReadClass findReadClass(String name) {
        // binary search
        int left = 0;
        int right = this.readClasses.size();
        int mid;

        while ((mid = (left + right) / 2) != left) {
            final ReadClass midVal = this.readClasses.get(mid);
            int cmp = midVal.getName().compareTo(name);
            if (cmp < 0)
                left = mid;
            else if (cmp == 0)
                return midVal;
            else
                right = mid;
        }

        final ReadClass found = this.readClasses.get(mid);
        return found.getName().equals(name) ? found : null;
    }

    /**
     * Search for the {@link ReadClass} with the given internal class name.
     *
     * @param internalName the internal class name to search for (e.g. java/lang/String)
     * @return the {@link ReadClass} with the given class name, or
     *         <code>null</code> if this TraceResult does not contain
     *         a ReadClass with that name
     */
    public ReadClass findReadClassByInternalName(String internalName) {
        // binary search
        int left = 0;
        int right = this.readClasses.size();
        int mid;

        while ((mid = (left + right) / 2) != left) {
            ReadClass midVal = this.readClasses.get(mid);
            int cmp = midVal.getInternalClassName().compareTo(internalName);
            if (cmp < 0)
                left = mid;
            else if (cmp == 0)
                return midVal;
            else
                right = mid;
        }

        ReadClass found = this.readClasses.get(mid);
        return found.getInternalClassName().equals(internalName) ? found : null;
    }

    public Instruction getInstruction(final int index) {
        if (index < 0 || index >= this.instructions.length)
            return null;
        return this.instructions[index];
    }

    public static void main(final String[] args) {
        Options options = createOptions();
        CommandLineParser parser = new GnuParser();
        CommandLine cmdLine;

        try {
            cmdLine = parser.parse(options, args, true);
        } catch (ParseException e) {
            System.err.println("Error parsing the command line arguments: " + e.getMessage());
            return;
        }

        if (cmdLine.hasOption('h')) {
            printHelp(options, System.out);
            System.exit(0);
        }

        InstanceFilter<InstructionInstance> filter;
        if (cmdLine.hasOption("filter")) {
        	if ("labels".equals(cmdLine.getOptionValue("filter"))) {
        		filter = InstanceFilter.LabelFilter.instance;
        	} else if ("additionals".equals(cmdLine.getOptionValue("filter"))) {
        		filter = InstanceFilter.AdditionalLabelFilter.instance;
        	} else if ("none".equals(cmdLine.getOptionValue("filter"))) {
        		filter = null;
        	} else {
                System.err.println("Illegal argument for filter: " + cmdLine.getOptionValue("filter"));
                return;
        	}
        } else {
        	// default:
        	filter = InstanceFilter.AdditionalLabelFilter.instance;
        }

        String[] additionalArgs = cmdLine.getArgs();
        if (additionalArgs.length != 1) {
            System.err.println("Error: No input file given.");
            printHelp(options, System.err);
            System.exit(-1);
        }

        final File traceFile = new File(additionalArgs[0]);
        Long threadToTrace = null;
        if (cmdLine.hasOption('t')) {
            try {
                threadToTrace = Long.parseLong(cmdLine.getOptionValue('t'));
            } catch (final NumberFormatException e) {
                System.err.println("Illegal thread id: " + cmdLine.getOptionValue('t'));
                System.exit(-1);
            }
        }

        System.out.println("Opening and reading trace file...");
        TraceResult tr = null;
        try {
            tr = readFrom(traceFile);
        } catch (final IOException e) {
            System.err.println("Error opening trace file: " + e);
            System.exit(-1);
            return;
        }

        final List<ThreadId> threads = tr.getThreads();
        if (threads.size() == 0) {
            System.err.println("The trace file contains no tracing information.");
            System.exit(-1);
        }

        System.out.println("The trace file contains traces for these threads:");
        ThreadId tracing = null;
        for (final ThreadId t: threads) {
            if (threadToTrace == null) {
                if ("main".equals(t.getThreadName()) && (tracing == null || t.getJavaThreadId() < tracing.getJavaThreadId()))
                    tracing = t;
            } else if (t.getJavaThreadId() == threadToTrace.longValue()) {
                tracing = t;
            }
            System.out.format("%15d: %s%n", t.getJavaThreadId(), t.getThreadName());
        }
        System.out.println();

        if (tracing == null) {
            System.out.println(threadToTrace == null ? "Couldn't find a main thread."
                    : "The thread you selected was not found.");
            System.exit(-1);
            return;
        }

        System.out.println(threadToTrace == null ? "Selected:" : "You selected:");
        System.out.format("%15d: %s%n", tracing.getJavaThreadId(), tracing.getThreadName());

        try {
			if (cmdLine.hasOption("length")) {
                final BackwardTraceIterator<AbstractInstructionInstance> it = tr.getBackwardIterator(tracing,
                    filter, new AbstractInstructionInstanceFactory());
                ProgressMonitor monitor = null;
                if (cmdLine.hasOption("--progress")) {
                    monitor = new ConsoleProgressMonitor(System.out, "Computing trace length", true, 100, true, true);
                    monitor.start(it);
                }
                try {
                    while (it.hasNext())
                        it.next();
                } finally {
                    if (monitor != null)
                        monitor.end();
                }

                System.out.format("%nNumber of instructions: %d  (+ %d additional = %d total instructions)%nReady%n",
                        it.getNumInstructions(), it.getNumFilteredInstructions(),
                        it.getNumInstructions() + it.getNumFilteredInstructions());
            } else {
                System.out.println();
                System.out.println("The backward trace:");
                BackwardTraceIterator<AbstractInstructionInstance> it = tr.getBackwardIterator(tracing,
                    filter, new AbstractInstructionInstanceFactory());
                long nr = 0;
                String format = "%8d (%8d)  %-100s -> %3d %7d %s%n";
                System.out.format("%19s  %-100s    %3s %7s %s%n",
                        "Nr (  intern)", "Location", "Dep", "OccNr", "Instruction");
                while (it.hasNext()) {
                    InstructionInstance inst = it.next();
                    ReadMethod method = inst.getInstruction().getMethod();
                    ReadClass class0 = method.getReadClass();
                    System.out.format(format, nr++, inst.getInstanceNr(), class0.getName()+"."
                            +method.getName()+":"+inst.getInstruction().getLineNumber(),
                            inst.getStackDepth(),
                            inst.getOccurrenceNumber(), inst.toString());
                }

                System.out.format("%nNumber of instructions: %d  (+ %d additional = %d total instructions)%nReady%n",
                        it.getNumInstructions(), it.getNumFilteredInstructions(),
                        it.getNumInstructions() + it.getNumFilteredInstructions());
            }
        } catch (final TracerException e) {
            System.err.print("Error while tracing: ");
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }

    @SuppressWarnings("static-access")
    private static Options createOptions() {
        Options options = new Options();
        options.addOption(OptionBuilder.isRequired(false).withArgName("threadid").hasArg(true).
            withDescription("thread id to select for trace output (default: main thread)").
            withLongOpt("threadid").create('t'));
        options.addOption(OptionBuilder.isRequired(false).hasArg(false).
            withDescription("do only output the trace length").withLongOpt("length").create('l'));
        options.addOption(OptionBuilder.isRequired(false).hasArg(false).
            withDescription("show progress while computing the trace length " +
                "(only effectfull together with --length)").
            withLongOpt("progress").create('p'));
        options.addOption(OptionBuilder.isRequired(false).hasArg(false).
            withDescription("print this help and exit").withLongOpt("help").create('h'));
        options.addOption(OptionBuilder.isRequired(false).withArgName("filter").hasArg(true).
            withDescription("(none/labels/additionals) which instructions to filter out " +
                "(default: additionals = labels added during instrumentation)").
            withLongOpt("filter").create('f'));
        return options;
    }

    private static void printHelp(Options options, PrintStream out) {
        out.println("Usage: " + TraceResult.class.getSimpleName() + " [<options>] <file>");
        out.println("where <file> is the input trace file, and <options> may be one or more of");
        HelpFormatter formatter = new HelpFormatter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printOptions(pw, 120, options, 5, 3);
        out.println();
        out.println("The output of the trace itself will have six fields:");
        out.println("   - Nr: just a continuously increasing counter, starting at 0");
        out.println("   - intern Nr: this is the instance number, which will always be identical when iterating the trace");
        out.println("   - Location: Class name, method name and line number of the instruction");
        out.println("   - Dep: Stack depth of the instruction (1 for outermost stack frames, like main)");
        out.println("   - OccNr: The occurence number of that single instruction (how often was it visited before)");
        out.println("   - Instruction: Textual representation of the bytecode instruction");
        out.println("Remember that the trace is output backwards. This means nr, intern nr and occNr are counting from the end of the trace.");
        out.println("The intern Nr may have gaps if specific instructions have been filtered out (see -f option).");
        out.println("By default, labels that have been inserted during instrumentation (so called additionals) are filtered out.");
    }

    public ThreadId getThreadId(final long javaThreadId) {
        // binary search
        int left = 0;
        int right = this.threadTraces.size();
        int mid;

        while ((mid = (left + right) / 2) != left) {
            final ThreadTraceResult midVal = this.threadTraces.get(mid);
            if (midVal.getJavaThreadId() <= javaThreadId)
                left = mid;
            else
                right = mid;
        }

        final ThreadId found = this.threadTraces.get(mid).getId();
        return found.getJavaThreadId() == javaThreadId ? found : null;
    }

}
