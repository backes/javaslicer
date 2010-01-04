package de.unisb.cs.st.javaslicer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.objectweb.asm.Opcodes;

import de.hammacher.util.maps.IntegerMap;
import de.unisb.cs.st.javaslicer.SlicingCriterion.SlicingCriterionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.AbstractInstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstanceFactory;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstanceInfo;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.LocalVariable;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.AbstractInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.VarInstruction;
import de.unisb.cs.st.javaslicer.common.progress.ConsoleProgressMonitor;
import de.unisb.cs.st.javaslicer.common.progress.ProgressMonitor;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.DataDependenceType;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.DependencesExtractor;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.DependencesVisitorAdapter;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.VisitorCapability;
import de.unisb.cs.st.javaslicer.traceResult.ThreadId;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.variables.Variable;

public class Slicer implements Opcodes {

    private static class SlicerInstance extends AbstractInstructionInstance {

        public boolean onDynamicSlice = false;

        // these variables are used to resolve which data dependences to follow:
        public boolean allDataInteresting = false;
        public boolean onlyIfAfterCriterion = false;
        public Variable interestingVariable = null;
        public Set<Variable> moreInterestingVariables = null;

        public SlicerInstance(AbstractInstruction instr, long occurenceNumber,
                int stackDepth, long instanceNr,
                InstructionInstanceInfo additionalInfo) {
            super(instr, occurenceNumber, stackDepth, instanceNr, additionalInfo);
        }

    }

    private static class SlicerInstanceFactory implements InstructionInstanceFactory<SlicerInstance> {

        public static final SlicerInstanceFactory instance = new SlicerInstanceFactory();

        public SlicerInstance createInstructionInstance(
                AbstractInstruction instruction, long occurenceNumber,
                int stackDepth, long instanceNr,
                InstructionInstanceInfo additionalInfo) {
            return new SlicerInstance(instruction, occurenceNumber, stackDepth, instanceNr, additionalInfo);
        }

    }

    private final TraceResult trace;
    private final List<ProgressMonitor> progressMonitors = new ArrayList<ProgressMonitor>(1);

    public Slicer(TraceResult trace) {
        this.trace = trace;
    }

    public static void main(String[] args) throws InterruptedException {
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

        String[] additionalArgs = cmdLine.getArgs();
        if (additionalArgs.length != 2) {
            printHelp(options, System.err);
            System.exit(-1);
        }
        File traceFile = new File(additionalArgs[0]);
        String slicingCriterionString = additionalArgs[1];

        Long threadId = null;
        if (cmdLine.hasOption('t')) {
            try {
                threadId = Long.parseLong(cmdLine.getOptionValue('t'));
            } catch (NumberFormatException e) {
                System.err.println("Illegal thread id: " + cmdLine.getOptionValue('t'));
                System.exit(-1);
            }
        }

        TraceResult trace;
        try {
            trace = TraceResult.readFrom(traceFile);
        } catch (IOException e) {
            System.err.format("Could not read the trace file \"%s\": %s%n", traceFile, e);
            System.exit(-1);
            return;
        }

        List<SlicingCriterion> sc = null;
        try {
            sc = readSlicingCriteria(slicingCriterionString, trace.getReadClasses());
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing slicing criterion: " + e.getMessage());
            System.exit(-1);
            return;
        }

        List<ThreadId> threads = trace.getThreads();
        if (threads.size() == 0) {
            System.err.println("The trace file contains no tracing information.");
            System.exit(-1);
        }

        ThreadId tracing = null;
        for (ThreadId t: threads) {
            if (threadId == null) {
                if ("main".equals(t.getThreadName()) && (tracing == null || t.getJavaThreadId() < tracing.getJavaThreadId()))
                    tracing = t;
            } else if (t.getJavaThreadId() == threadId.longValue()) {
                tracing = t;
            }
        }

        if (tracing == null) {
            System.err.println(threadId == null ? "Couldn't find the main thread."
                    : "The thread you specified was not found.");
            System.exit(-1);
            return;
        }

        long startTime = System.nanoTime();
        Slicer slicer = new Slicer(trace);
        if (cmdLine.hasOption("progress"))
            slicer.addProgressMonitor(new ConsoleProgressMonitor());
        boolean multithreaded;
        if (cmdLine.hasOption("multithreaded")) {
            String multithreadedStr = cmdLine.getOptionValue("multithreaded");
            multithreaded = ("1".equals(multithreadedStr) || "true".equals(multithreadedStr));
        } else {
            multithreaded = Runtime.getRuntime().availableProcessors() > 1;
        }

        Set<Instruction> slice = slicer.getDynamicSlice(tracing, sc, multithreaded);
        long endTime = System.nanoTime();

        List<Instruction> sliceList = new ArrayList<Instruction>(slice);
        Collections.sort(sliceList);

        System.out.println("The dynamic slice for criterion " + sc + ":");
        for (Instruction insn: sliceList) {
            System.out.format((Locale)null, "%s.%s:%d %s%n",
                    insn.getMethod().getReadClass().getName(),
                    insn.getMethod().getName(),
                    insn.getLineNumber(),
                    insn.toString());
        }
        System.out.format((Locale)null, "%nSlice consists of %d bytecode instructions.%n", sliceList.size());
        System.out.format((Locale)null, "Computation took %.2f seconds.%n", 1e-9*(endTime-startTime));
    }

    private void addProgressMonitor(ProgressMonitor progressMonitor) {
        this.progressMonitors .add(progressMonitor);
    }

    public static List<SlicingCriterion> readSlicingCriteria(String string,
            List<ReadClass> readClasses) throws IllegalArgumentException {
        List<SlicingCriterion> crit = new ArrayList<SlicingCriterion>(2);
        int oldPos = 0;
        while (true) {
            int bracketPos = string.indexOf('{', oldPos);
            int commaPos = string.indexOf(',', oldPos);
            while (bracketPos != -1 && bracketPos < commaPos) {
                int closeBracketPos = string.indexOf('}', bracketPos + 1);
                if (closeBracketPos == -1)
                    throw new IllegalArgumentException(
                            "Couldn't find matching '}'");
                bracketPos = string.indexOf('{', closeBracketPos + 1);
                commaPos = string.indexOf(',', closeBracketPos + 1);
            }

            SlicingCriterion newCrit = SimpleSlicingCriterion.parse(string.substring(oldPos,
                        commaPos == -1 ? string.length() : commaPos), readClasses);
            oldPos = commaPos + 1;

            crit.add(newCrit);

            if (commaPos == -1)
                return crit;
        }
    }


    public Set<Instruction> getDynamicSlice(ThreadId threadId, final List<SlicingCriterion> sc, boolean multithreaded) throws InterruptedException {
        DependencesExtractor<SlicerInstance> depExtractor = DependencesExtractor.forTrace(this.trace, SlicerInstanceFactory.instance);
        for (ProgressMonitor mon : this.progressMonitors)
            depExtractor.addProgressMonitor(mon);

        final Set<Instruction> dynamicSlice = new HashSet<Instruction>();

        depExtractor.registerVisitor(new DependencesVisitorAdapter<SlicerInstance>() {
            private final List<SlicingCriterionInstance> slicingCritInst = instantiateSlicingCriteria(sc);
            @SuppressWarnings("unchecked")
            private IntegerMap<Object>[] interestingLocalVariables = (IntegerMap<Object>[]) new IntegerMap<?>[0];
            private long[] critOccurenceNumbers = new long[2]; // 0 if not in a criterion

            private List<SlicingCriterionInstance> instantiateSlicingCriteria(
                    List<SlicingCriterion> criteria) {
                if (criteria.isEmpty())
                    return Collections.emptyList();
                else if (criteria.size() == 1)
                    return Collections.singletonList(criteria.get(0).getInstance());
                else {
                    List<SlicingCriterionInstance> instances = new ArrayList<SlicingCriterionInstance>(criteria.size());
                    for (SlicingCriterion crit : criteria)
                        instances.add(crit.getInstance());
                    return instances;
                }
            }

            @Override
            public void visitInstructionExecution(SlicerInstance instance) {
                int stackDepth = instance.getStackDepth();
                if (this.critOccurenceNumbers.length <= stackDepth) {
                    long[] newCritOccurenceNumbers = new long[2*Math.max(this.critOccurenceNumbers.length, stackDepth)];
                    System.arraycopy(this.critOccurenceNumbers, 0, newCritOccurenceNumbers, 0, this.critOccurenceNumbers.length);
                    this.critOccurenceNumbers = newCritOccurenceNumbers;
                }
                for (SlicingCriterionInstance crit : this.slicingCritInst) {
                    if (crit.matches(instance)) {
                        this.critOccurenceNumbers[stackDepth] = crit.getOccurenceNumber();
                        assert (this.critOccurenceNumbers[stackDepth] > 0);
                        // for each criterion, there are three cases:
                        //  - track all data read in this line
                        //  - track a given set of local variables
                        //  - track the control dependences of this instruction
                        // only in the last case, the instructions from that line are added to the dynamic slice
                        if (crit.matchAllData()) {
                            instance.allDataInteresting = true;
                            instance.onlyIfAfterCriterion = true;
                            instance.onDynamicSlice = true; // it's not really on the dynamic slice, but we have to set this
                        } else {
                            if (crit.hasLocalVariables()) {
                                if (this.interestingLocalVariables.length <= stackDepth) {
                                    @SuppressWarnings("unchecked")
                                    IntegerMap<Object>[] newInterestingLocalVariables =
                                            (IntegerMap<Object>[]) new IntegerMap<?>[Math.max(stackDepth+1, this.interestingLocalVariables.length*3/2)];
                                    System.arraycopy(this.interestingLocalVariables, 0, newInterestingLocalVariables, 0, this.interestingLocalVariables.length);
                                    this.interestingLocalVariables = newInterestingLocalVariables;
                                }
                                List<LocalVariable> localVariables = crit.getLocalVariables();
                                if (this.interestingLocalVariables[stackDepth] == null)
                                    this.interestingLocalVariables[stackDepth] = new IntegerMap<Object>(localVariables.size()*4/3+1);
                                for (LocalVariable i : localVariables)
                                    this.interestingLocalVariables[stackDepth].put(i.getIndex(), null);
                            } else {
                                Instruction insn = instance.getInstruction();
                                if (insn.getType() != InstructionType.LABEL)
                                    dynamicSlice.add(insn);
                                instance.onDynamicSlice = true;
                            }
                        }
                    } else if (this.critOccurenceNumbers[stackDepth] != 0) {
                        this.critOccurenceNumbers[stackDepth] = 0;
                    }
                }
                if (this.interestingLocalVariables.length > stackDepth &&
                        this.interestingLocalVariables[stackDepth] != null &&
                        instance.getInstruction().getType() == InstructionType.VAR) {
                    VarInstruction varInsn = (VarInstruction) instance.getInstruction();
                    if (this.interestingLocalVariables[stackDepth].containsKey(varInsn.getLocalVarIndex()) &&
                            (varInsn.getOpcode() == ISTORE ||
                             varInsn.getOpcode() == ASTORE ||
                             varInsn.getOpcode() == LSTORE ||
                             varInsn.getOpcode() == FSTORE ||
                             varInsn.getOpcode() == DSTORE)) {
                        this.interestingLocalVariables[stackDepth].remove(varInsn.getLocalVarIndex());
                        if (this.interestingLocalVariables[stackDepth].isEmpty())
                            this.interestingLocalVariables[stackDepth] = null;
                        dynamicSlice.add(instance.getInstruction());
                        instance.onDynamicSlice = true;
                        // and we want to know where the data comes from...
                        instance.allDataInteresting = true;
                    }
                }
            }

            @Override
            public void visitControlDependence(SlicerInstance from,
                    SlicerInstance to) {
                if (from.onDynamicSlice) {
                    Instruction insn = to.getInstruction();
                    if (insn.getType() != InstructionType.LABEL)
                        dynamicSlice.add(insn);
                    to.onDynamicSlice = true;
                    // since "to" controls the execution of "from", we want to track all data dependences of "to" to find out why it took this decision
                    to.allDataInteresting = true;
                }
            }

            @Override
            public void visitDataDependence(SlicerInstance from,
                    SlicerInstance to, Collection<Variable> fromVars,
                    Variable toVar, DataDependenceType type)
                    throws InterruptedException {
                assert type == DataDependenceType.READ_AFTER_WRITE;

                if (from.onDynamicSlice && // from must definitively be on the dynamic slice
                        ((from.allDataInteresting && (!from.onlyIfAfterCriterion || this.critOccurenceNumbers[to.getStackDepth()] == 0)) || // and either we want to track all data dependencies
                            (from.interestingVariable != null && ( // or (if the interestingVariable is set) ...
                                from.interestingVariable.equals(toVar) || // the interestingVariable must be the one we are just visiting
                                (from.moreInterestingVariables != null && from.moreInterestingVariables.contains(toVar)))))) { // or it must be in the set of more variables
                    Instruction insn = to.getInstruction();
                    if (insn.getType() != InstructionType.LABEL) {
                        dynamicSlice.add(insn);
                        if (!fromVars.isEmpty()) {
                            Iterator<Variable> varIt = fromVars.iterator();
                            assert varIt.hasNext() : "Iterator of a non-empty collection should have at least one element";
                            Variable first = varIt.next();
                            if (to.interestingVariable == null || to.interestingVariable.equals(first)) {
                                to.interestingVariable = first;
                                first = varIt.hasNext() ? varIt.next() : null;
                            }
                            if (first != null) {
                                if (to.moreInterestingVariables == null)
                                    to.moreInterestingVariables = new HashSet<Variable>(8);
                                to.moreInterestingVariables.add(first);
                                while (varIt.hasNext())
                                    to.moreInterestingVariables.add(varIt.next());
                            }
                        }
                    }
                    to.onDynamicSlice = true;
                }
            }

            @Override
            public void visitMethodEntry(ReadMethod method, int stackDepth)
                    throws InterruptedException {
                if (this.interestingLocalVariables.length > stackDepth &&
                        this.interestingLocalVariables[stackDepth] != null) {
                    this.interestingLocalVariables[stackDepth] = null;
                }
            }

        }, VisitorCapability.CONTROL_DEPENDENCES, VisitorCapability.DATA_DEPENDENCES_READ_AFTER_WRITE, VisitorCapability.INSTRUCTION_EXECUTIONS,
           VisitorCapability.METHOD_ENTRY_LEAVE);

        depExtractor.processBackwardTrace(threadId, multithreaded);

        return dynamicSlice;
    }

    @SuppressWarnings("static-access")
    private static Options createOptions() {
        Options options = new Options();
        options.addOption(OptionBuilder.isRequired(false).withArgName("threadid").hasArg(true).
            withDescription("thread id to select for slicing (default: main thread)").withLongOpt("threadid").create('t'));
        options.addOption(OptionBuilder.isRequired(false).hasArg(false).
            withDescription("show progress while computing the dynamic slice").withLongOpt("progress").create('p'));
        options.addOption(OptionBuilder.isRequired(false).hasArg(false).
            withDescription("print this help and exit").withLongOpt("help").create('h'));
        options.addOption(OptionBuilder.isRequired(false).hasArg(true).withArgName("value").
            withDescription("process the trace in a multithreaded way (pass 'true' or '1' to enable, anything else to disable). Default is true iff we have more than one processor").
            withLongOpt("multithreaded").create('m'));
        return options;
    }

    private static void printHelp(Options options, PrintStream out) {
        out.println("Usage: " + Slicer.class.getSimpleName() + " [<options>] <file> <slicing criterion>");
        out.println("where <file> is the input trace file, and <options> may be one or more of");
        out.println("      <slicing criterion> has the form <loc>[(<occ>)]:<var>[,<loc>[(<occ>)]:<var>]*");
        out.println("      <options> may be one or more of");
        HelpFormatter formatter = new HelpFormatter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printOptions(pw, 120, options, 5, 3);
    }

}
