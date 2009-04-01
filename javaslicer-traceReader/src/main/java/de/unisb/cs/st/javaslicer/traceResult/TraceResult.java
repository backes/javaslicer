package de.unisb.cs.st.javaslicer.traceResult;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.GZIPInputStream;

import de.hammacher.util.MultiplexedFileReader;
import de.hammacher.util.StringCacheInput;
import de.hammacher.util.MultiplexedFileReader.MultiplexInputStream;
import de.unisb.cs.st.javaslicer.common.classRepresentation.AbstractInstructionInstanceFactory;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstanceFactory;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.AbstractInstruction;
import de.unisb.cs.st.javaslicer.common.exceptions.TracerException;

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
        int written = 0;
        for (final ReadClass c: classes) {
            for (final ReadMethod m: c.getMethods()) {
                written += m.getInstructions().size();
                for (final AbstractInstruction instr: m.getInstructions()) {
                    if (instructions[instr.getIndex()] != null)
                        throw new IOException("Same instruction index given twice.");
                    instructions[instr.getIndex()] = instr;
                }
            }
        }

        if (written != numInstructions)
            throw new IOException("Omitted some instruction indexes.");

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
    public BackwardTraceIterator getBackwardIterator(final ThreadId threadId,
            final InstanceFilter filter, InstructionInstanceFactory instanceFactory) {
        final ThreadTraceResult res = findThreadTraceResult(threadId);
        return res == null ? null : res.getBackwardIterator(filter, instanceFactory);
    }

    /**
     * @see #getBackwardIterator(ThreadId, InstanceFilter)
     */
    public BackwardTraceIterator getBackwardIterator(final ThreadId threadId,
            final InstanceFilter filter) {
        return getBackwardIterator(threadId, filter, new AbstractInstructionInstanceFactory());
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
    public BackwardTraceIterator getBackwardIterator(final long javaThreadId,
            InstanceFilter filter) {
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
     * @param instanceFactory
     * @return an iterator that is able to iterate in any direction through the execution trace.
     *         the iterator extends {@link ListIterator} over {@link InstructionInstance}.
     */
    public ForwardTraceIterator getForwardIterator(final ThreadId threadId, InstructionInstanceFactory instanceFactory) {
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
     * @param instanceFactory
     * @return an iterator that is able to iterate in any direction through the execution trace.
     *         the iterator extends {@link ListIterator} over {@link InstructionInstance}.
     */
    public ForwardTraceIterator getIterator(final long javaThreadId, InstructionInstanceFactory instanceFactory) {
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
     * @param name the class name to search for
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

    public Instruction getInstruction(final int index) {
        if (index < 0 || index >= this.instructions.length)
            return null;
        return this.instructions[index];
    }

    public static void main(final String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: java " + TraceResult.class.getName() + " <file> [<threadId>]");
            System.exit(-1);
        }
        final File traceFile = new File(args[0]);
        Long threadToTrace = null;
        if (args.length > 1) {
            try {
                threadToTrace = Long.valueOf(args[1]);
            } catch (final NumberFormatException e) {
                System.err.println("Second parameter indicates the thread id to trace. Must be an integer.");
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
            System.out.println();
            System.out.println("The backward trace:");
            final Iterator<InstructionInstance> it = tr.getBackwardIterator(tracing,
                InstanceFilter.AdditionalLabelFilter.instance, new AbstractInstructionInstanceFactory());
            long nr = 0;
            final String format = "%8d: %-100s -> %3d %7d %s%n";
            System.out.format("%8s  %-100s    %3s %7s %s%n",
                    "Nr", "Location", "Dep", "OccNr", "Instruction");
            while (it.hasNext()) {
                /*
                if (++nr % 10000000 == 0) {
                    System.out.format("%10d: %s (%7.2f - %7.2f = %7.2f MB memory)%n",
                        nr, new Date(),
                        1e-6*Runtime.getRuntime().totalMemory(),
                        1e-6*Runtime.getRuntime().freeMemory(),
                        1e-6*(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())
                        );
                }
                it.next();
                */

                final InstructionInstance inst = it.next();
                final ReadMethod method = inst.getInstruction().getMethod();
                final ReadClass class0 = method.getReadClass();
                System.out.format(format, nr++, class0.getName()+"."
                        +method.getName()+":"+inst.getInstruction().getLineNumber(),
                        inst.getStackDepth(),
                        inst.getOccurrenceNumber(), inst.toString());
            }

            final BackwardTraceIterator it2 = (BackwardTraceIterator) it;

            System.out.format("%nNumber of instructions: %d  (+ %d additional = %d total instructions)%nReady%n",
                    it2.getNumInstructions(), it2.getNumFilteredInstructions(),
                    it2.getNumInstructions() + it2.getNumFilteredInstructions());

        } catch (final TracerException e) {
            System.err.print("Error while tracing: ");
            e.printStackTrace(System.err);
            System.exit(-1);
        }
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
