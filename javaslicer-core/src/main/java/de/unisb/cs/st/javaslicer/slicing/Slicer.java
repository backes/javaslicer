package de.unisb.cs.st.javaslicer.slicing;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
import de.unisb.cs.st.javaslicer.common.classRepresentation.AbstractInstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstanceFactory;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstanceInfo;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.LocalVariable;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.AbstractInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MethodInvocationInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.VarInstruction;
import de.unisb.cs.st.javaslicer.common.progress.ConsoleProgressMonitor;
import de.unisb.cs.st.javaslicer.common.progress.ProgressMonitor;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.DataDependenceType;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.DependencesExtractor;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.DependencesVisitorAdapter;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.VisitorCapability;
import de.unisb.cs.st.javaslicer.slicing.SlicingCriterion.SlicingCriterionInstance;
import de.unisb.cs.st.javaslicer.traceResult.PrintUniqueUntracedMethods;
import de.unisb.cs.st.javaslicer.traceResult.ThreadId;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.traceResult.UntracedCallVisitor;
import de.unisb.cs.st.javaslicer.variables.Variable;

public class Slicer {

    private static class SlicerInstance extends AbstractInstructionInstance {

        public boolean onDynamicSlice = false;

        // these variables are used to resolve which data dependences to follow:
        public boolean allDataInteresting = false;
        public boolean onlyIfAfterCriterion = false;
        public Variable interestingVariable = null;
        public Set<Variable> moreInterestingVariables = null;

        public Set<SlicerInstance> predecessors; // only set on labels

        public int criterionDistance;

        public SlicerInstance(AbstractInstruction instr, long occurenceNumber,
                int stackDepth, long instanceNr,
                InstructionInstanceInfo additionalInfo) {
            super(instr, occurenceNumber, stackDepth, instanceNr, additionalInfo);
            this.criterionDistance = Integer.MAX_VALUE;
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
    private final List<SliceVisitor> sliceVisitors = new ArrayList<SliceVisitor>(1);
    private final List<UntracedCallVisitor> untracedCallVisitors = new ArrayList<UntracedCallVisitor>(1);

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
            sc = SlicingCriterion.parseAll(slicingCriterionString, trace.getReadClasses());
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

        boolean warnUntracedMethods = cmdLine.hasOption("warn-untraced");

        SliceInstructionsCollector collector = new SliceInstructionsCollector();
        slicer.addSliceVisitor(collector);
        if (warnUntracedMethods)
            slicer.addUntracedCallVisitor(new PrintUniqueUntracedMethods());
        slicer.process(tracing, sc, multithreaded);
        Set<Instruction> slice = collector.getDynamicSlice();
        long endTime = System.nanoTime();

        Instruction[] sliceArray = slice.toArray(new Instruction[slice.size()]);
        Arrays.sort(sliceArray);

        System.out.println("The dynamic slice for criterion " + sc + ":");
        for (Instruction insn: sliceArray) {
            System.out.format((Locale)null, "%s.%s:%d %s%n",
                    insn.getMethod().getReadClass().getName(),
                    insn.getMethod().getName(),
                    insn.getLineNumber(),
                    insn.toString());
        }
        System.out.format((Locale)null, "%nSlice consists of %d bytecode instructions.%n", sliceArray.length);
        System.out.format((Locale)null, "Computation took %.2f seconds.%n", 1e-9*(endTime-startTime));
    }

    public void addProgressMonitor(ProgressMonitor progressMonitor) {
        this.progressMonitors.add(progressMonitor);
    }

    public void addSliceVisitor(SliceVisitor sliceVisitor) {
        this.sliceVisitors.add(sliceVisitor);
    }

    public void addUntracedCallVisitor(UntracedCallVisitor untracedCallVisitor) {
        this.untracedCallVisitors.add(untracedCallVisitor);
    }

    public void process(ThreadId threadId, final List<SlicingCriterion> sc, boolean multithreaded) throws InterruptedException {
        DependencesExtractor<SlicerInstance> depExtractor = DependencesExtractor.forTrace(this.trace, SlicerInstanceFactory.instance);
        for (ProgressMonitor mon : this.progressMonitors)
            depExtractor.addProgressMonitor(mon);

        VisitorCapability[] capabilities = { VisitorCapability.CONTROL_DEPENDENCES, VisitorCapability.DATA_DEPENDENCES_READ_AFTER_WRITE, VisitorCapability.INSTRUCTION_EXECUTIONS,
                VisitorCapability.METHOD_ENTRY_LEAVE, VisitorCapability.CONTROL_DEPENDENCES };
        if (this.untracedCallVisitors.size() > 0)
        	capabilities[capabilities.length-1] = VisitorCapability.UNTRACED_METHOD_CALLS;

        final List<SliceVisitor> sliceVisitors0 = Slicer.this.sliceVisitors;
        final List<UntracedCallVisitor> untracedCallVisitors0 = Slicer.this.untracedCallVisitors;
        depExtractor.registerVisitor(new DependencesVisitorAdapter<SlicerInstance>() {
            private final List<SlicingCriterionInstance> slicingCritInst = instantiateSlicingCriteria(sc);
            @SuppressWarnings("unchecked")
            private IntegerMap<Object>[] interestingLocalVariables = (IntegerMap<Object>[]) new IntegerMap<?>[0];
            private long[] critOccurenceNumbers = new long[2]; // 0 if not in a criterion
            private final SliceVisitor[] sliceVisitorsArray = sliceVisitors0.toArray(new SliceVisitor[sliceVisitors0.size()]);
            private final UntracedCallVisitor[] untracedCallsVisitorsArray = untracedCallVisitors0.toArray(new UntracedCallVisitor[untracedCallVisitors0.size()]);

            private ReadMethod enteredMethod;
            private IntegerMap<Object> enteredMethodInterestingLocalVariables;

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
                Instruction instruction = instance.getInstruction();
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
                                Instruction insn = instruction;
                                if (insn.getType() != InstructionType.LABEL)
                                    for (SliceVisitor vis : this.sliceVisitorsArray)
                                        vis.visitMatchedInstance(instance);
                                instance.onDynamicSlice = true;
                                instance.criterionDistance = 0;
                            }
                        }
                    } else if (this.critOccurenceNumbers[stackDepth] != 0) {
                        this.critOccurenceNumbers[stackDepth] = 0;
                    }
                }
                if (this.interestingLocalVariables.length > stackDepth &&
                        this.interestingLocalVariables[stackDepth] != null) {
                    switch (instruction.getOpcode()) {
                        case Opcodes.ISTORE:
                        case Opcodes.ASTORE:
                        case Opcodes.LSTORE:
                        case Opcodes.FSTORE:
                        case Opcodes.DSTORE:
                            VarInstruction varInsn = (VarInstruction) instruction;
                            if (this.interestingLocalVariables[stackDepth].containsKey(varInsn.getLocalVarIndex())) {
                                this.interestingLocalVariables[stackDepth].remove(varInsn.getLocalVarIndex());
                                if (this.interestingLocalVariables[stackDepth].isEmpty())
                                    this.interestingLocalVariables[stackDepth] = null;
                                for (SliceVisitor vis : this.sliceVisitorsArray)
                                    vis.visitMatchedInstance(instance);
                                instance.onDynamicSlice = true;
                                // and we want to know where the data comes from...
                                instance.allDataInteresting = true;
                                instance.criterionDistance = 0;
                            }
                            break;
                        case Opcodes.INVOKEINTERFACE:
                        case Opcodes.INVOKESPECIAL:
                        case Opcodes.INVOKESTATIC:
                        case Opcodes.INVOKEVIRTUAL:
                            if (this.enteredMethod != null) {
                                MethodInvocationInstruction mtdInvInsn = (MethodInvocationInstruction) instruction;
                                int paramCount = instruction.getOpcode() == INVOKESTATIC ? 0 : 1;
                                for (int param = mtdInvInsn.getParameterCount()-1; param >= 0; --param)
                                    paramCount += mtdInvInsn.parameterIsLong(param) ? 2 : 1;
                                boolean enteredMethodMatches = this.enteredMethod.getName().equals(mtdInvInsn.getInvokedMethodName())
                                    && this.enteredMethod.getDesc().equals(mtdInvInsn.getInvokedMethodDesc());
                                if (enteredMethodMatches) {
                                    boolean localVarsMatched = false;
                                    for (int varNr = 0; varNr < paramCount; ++varNr) {
                                        if (this.interestingLocalVariables[stackDepth].containsKey(varNr)) {
                                            this.interestingLocalVariables[stackDepth].remove(varNr);
                                            if (this.interestingLocalVariables[stackDepth].isEmpty())
                                                this.interestingLocalVariables[stackDepth] = null;
                                            localVarsMatched = true;
                                            instance.onDynamicSlice = true;
                                            // and we want to know where the data comes from...
                                            // TODO
                                            instance.allDataInteresting = true;
                                            instance.criterionDistance = 0;
                                        }
                                    }
                                    if (localVarsMatched)
                                        for (SliceVisitor vis : this.sliceVisitorsArray)
                                            vis.visitMatchedInstance(instance);
                                }
                            }
                    }
                }
                this.enteredMethod = null;
            }

            @Override
            public void visitControlDependence(SlicerInstance from,
                    SlicerInstance to) {
                if (from.onDynamicSlice) {
                    Instruction insn = to.getInstruction();
                    if (insn.getType() == InstructionType.LABEL) {
                    	if (to.predecessors == null)
                			to.predecessors = new HashSet<SlicerInstance>(4);
                    	else if (to.predecessors.size() == 1)
                			to.predecessors = new HashSet<SlicerInstance>(to.predecessors);
                    	to.predecessors.add(from);
                    	if (from.criterionDistance < to.criterionDistance)
                    		to.criterionDistance = from.criterionDistance;
                    } else if (from.predecessors != null) {
                    	for (SlicerInstance pred : from.predecessors) {
	                        int distance = pred.criterionDistance+1;
	                        for (SliceVisitor vis : this.sliceVisitorsArray)
	                            vis.visitSliceDependence(pred, to, null, distance);
	                    	if (distance < to.criterionDistance)
	                    		to.criterionDistance = distance;
                    	}
                    } else {
                        int distance = from.criterionDistance+1;
                        for (SliceVisitor vis : this.sliceVisitorsArray)
                            vis.visitSliceDependence(from, to, null, distance);
                    	if (distance < to.criterionDistance)
                    		to.criterionDistance = distance;
                    }
                    to.onDynamicSlice = true;
                    // since "to" controls the execution of "from", we want to track all data dependences of "to"
                    // to find out why it took this decision
                    // exception: method invocations; here we only want to track why the method was executed,
                    // but not the data that it consumed
                    if (to.getInstruction().getType() != InstructionType.METHODINVOCATION) {
                        // besides method invocations, there are only 3 possibilities of control dependences:
                        // - "to" is a condition jump or a switch, which directly controls the execution of "from"
                        // - "to" is the label of a catch block (and "from" an instruction within the catch block)
                        // - "from" is the label of a catch block (which depends on the throwing instance)
                        assert to.getInstruction().getType() == InstructionType.JUMP ||
                            to.getInstruction().getType() == InstructionType.LOOKUPSWITCH ||
                            to.getInstruction().getType() == InstructionType.TABLESWITCH ||
                            (from.getInstruction().getType() == InstructionType.LABEL && ((LabelMarker)from.getInstruction()).isCatchBlock()) ||
                            (to.getInstruction().getType() == InstructionType.LABEL && ((LabelMarker)to.getInstruction()).isCatchBlock());
                        to.allDataInteresting = true;
                    }
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
                    assert insn.getType() != InstructionType.LABEL;
                    if (from.onlyIfAfterCriterion) {
                        to.criterionDistance = 0;
                        for (SliceVisitor vis : this.sliceVisitorsArray)
                            vis.visitMatchedInstance(to);
                    } else {
                        int distance = from.criterionDistance+1;
                    	if (distance < to.criterionDistance)
                    		to.criterionDistance = distance;
                        for (SliceVisitor vis : this.sliceVisitorsArray)
                            vis.visitSliceDependence(from, to, toVar, distance);
                    }
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
                    to.onDynamicSlice = true;
                }
            }

            @Override
            public void visitMethodLeave(ReadMethod method, int stackDepth)
                    throws InterruptedException {
                if (this.interestingLocalVariables.length > stackDepth &&
                        this.interestingLocalVariables[stackDepth] != null) {
                    this.interestingLocalVariables[stackDepth] = null;
                }
            }

            @Override
            public void visitMethodEntry(ReadMethod method, int stackDepth)
                    throws InterruptedException {
                if (this.interestingLocalVariables.length > stackDepth &&
                        this.interestingLocalVariables[stackDepth] != null) {
                    this.enteredMethod = method;
                    this.enteredMethodInterestingLocalVariables = this.interestingLocalVariables[stackDepth];
                    this.interestingLocalVariables[stackDepth] = null;
                }
            }

            @Override
            public void visitUntracedMethodCall(SlicerInstance instrInstance)
                    throws InterruptedException {
                for (UntracedCallVisitor vis : this.untracedCallsVisitorsArray)
                    vis.visitUntracedMethodCall(instrInstance);
            }

        }, capabilities);

        depExtractor.processBackwardTrace(threadId, multithreaded);
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
        options.addOption(OptionBuilder.isRequired(false).hasArg(false).
            withDescription("warn once for each method which is called but not traced").withLongOpt("warn-untraced").create('u'));
        return options;
    }

    private static void printHelp(Options options, PrintStream out) {
        out.println("Usage: " + Slicer.class.getSimpleName() + " [<options>] <file> <slicing criterion>");
        out.println("where <file> is the input trace file");
        out.println("      <slicing criterion> has the form <loc>[(<occ>)]:<var>[,<loc>[(<occ>)]:<var>]*");
        out.println("      <options> may be one or more of");
        HelpFormatter formatter = new HelpFormatter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printOptions(pw, 120, options, 5, 3);
    }

}
