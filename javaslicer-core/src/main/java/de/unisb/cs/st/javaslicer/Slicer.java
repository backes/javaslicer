package de.unisb.cs.st.javaslicer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.ArrayStack;
import de.hammacher.util.maps.IntegerMap;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;
import de.unisb.cs.st.javaslicer.common.progress.ConsoleProgressMonitor;
import de.unisb.cs.st.javaslicer.common.progress.ProgressMonitor;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowAnalyser;
import de.unisb.cs.st.javaslicer.instructionSimulation.DynamicInformation;
import de.unisb.cs.st.javaslicer.instructionSimulation.ExecutionFrame;
import de.unisb.cs.st.javaslicer.instructionSimulation.Simulator;
import de.unisb.cs.st.javaslicer.traceResult.BackwardTraceIterator;
import de.unisb.cs.st.javaslicer.traceResult.ThreadId;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.variables.Variable;

public class Slicer implements Opcodes {

    private final TraceResult trace;
    private final Simulator<InstructionInstance> simulator;
    private final List<ProgressMonitor> progressMonitors = new ArrayList<ProgressMonitor>(1);

    public Slicer(final TraceResult trace) {
        this.trace = trace;
        this.simulator = new Simulator<InstructionInstance>(trace);
    }

    public static void main(final String[] args) {

        boolean showProgress = false;

        int argPos = 0;

        while (argPos < args.length) {
            if ("--progress".equals(args[argPos])) {
                showProgress = true;
            } else {
                // no more options
                break;
            }
            ++argPos;
        }

        if (argPos >= args.length) {
            printUsage(System.err);
            System.exit(-1);
        }

        final File traceFile = new File(args[argPos++]);

    	Long threadId = null;
        try {
            threadId = Long.valueOf(args[argPos]);
            ++argPos;
        } catch (final NumberFormatException e) {
            // ignore
        }

        TraceResult trace;
        try {
            trace = TraceResult.readFrom(traceFile);
        } catch (final IOException e) {
            System.err.format("Could not read the trace file \"%s\": %s%n", traceFile, e);
            System.exit(-1);
            return;
        }

        SlicingCriterion sc = null;
        if (argPos >= args.length) {
            printUsage(System.err);
            System.exit(-1);
        }
        try {
            sc = readSlicingCriteria(args[argPos], trace.getReadClasses());
        } catch (final IllegalArgumentException e) {
            System.err.println("Error parsing slicing criterion: " + e.getMessage());
            System.exit(-1);
            return;
        }

        final List<ThreadId> threads = trace.getThreads();
        if (threads.size() == 0) {
            System.err.println("The trace file contains no tracing information.");
            System.exit(-1);
        }

        ThreadId tracing = null;
        for (final ThreadId t: threads) {
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

        final long startTime = System.nanoTime();
        Slicer slicer = new Slicer(trace);
        if (showProgress)
            slicer.addProgressMonitor(new ConsoleProgressMonitor());
        final Set<Instruction> slice = slicer.getDynamicSlice(tracing, sc.getInstance());
        final long endTime = System.nanoTime();

        final List<Instruction> sliceList = new ArrayList<Instruction>(slice);
        Collections.sort(sliceList);

        System.out.println("The dynamic slice for criterion " + sc + ":");
        for (final Instruction insn: sliceList) {
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

    private static void printUsage(PrintStream out) {
        out.println("Usage: java [<javaoptions>] " + Slicer.class.getName()
                + " [<sliceroptions>] <trace file> [<threadId>] <loc>[(<occ>)]:<var>[,<loc>[(<occ>)]:<var>]*");
        out.println("  where <sliceroptions> can be a combination of");
    }

    public static SlicingCriterion readSlicingCriteria(final String string, final List<ReadClass> readClasses)
            throws IllegalArgumentException {
        CompoundSlicingCriterion crit = null;
        int oldPos = 0;
        while (true) {
            int bracketPos = string.indexOf('{', oldPos);
            int commaPos = string.indexOf(',', oldPos);
            while (bracketPos != -1 && bracketPos < commaPos) {
                final int closeBracketPos = string.indexOf('}', bracketPos+1);
                if (closeBracketPos == -1)
                    throw new IllegalArgumentException("Couldn't find matching '}'");
                bracketPos = string.indexOf('{', closeBracketPos+1);
                commaPos = string.indexOf(',', closeBracketPos+1);
            }

            final SlicingCriterion newCrit = SimpleSlicingCriterion.parse(
                    string.substring(oldPos, commaPos == -1 ? string.length() : commaPos),
                    readClasses);
            oldPos = commaPos+1;

            if (crit == null) {
                if (commaPos == -1)
                    return newCrit;
                crit = new CompoundSlicingCriterion();
            }

            crit.add(newCrit);

            if (commaPos == -1)
                return crit;
        }
    }

    public Set<Instruction> getDynamicSlice(final ThreadId threadId, final SlicingCriterion.SlicingCriterionInstance slicingCriterion) {
        final BackwardTraceIterator<InstructionInstance> backwardInsnItr = this.trace.getBackwardIterator(threadId, null);

        final IntegerMap<Set<Instruction>> controlDependences = new IntegerMap<Set<Instruction>>();

        final ArrayStack<ExecutionFrame<InstructionInstance>> frames = new ArrayStack<ExecutionFrame<InstructionInstance>>();

        final Set<Variable> interestingVariables = new HashSet<Variable>();
        final Set<Instruction> dynamicSlice = new HashSet<Instruction>();

        ExecutionFrame<InstructionInstance> currentFrame = new ExecutionFrame<InstructionInstance>();
        frames.push(currentFrame);

        for (ProgressMonitor mon : this.progressMonitors)
            mon.start(backwardInsnItr);
        try {
            while (backwardInsnItr.hasNext()) {
                final InstructionInstance instance = backwardInsnItr.next();
                final Instruction instruction = instance.getInstruction();

                ExecutionFrame<InstructionInstance> removedFrame = null;
                boolean removedFrameIsInteresting = false;
                final int stackDepth = instance.getStackDepth();
                assert stackDepth > 0;

                if (frames.size() != stackDepth) {
                    if (frames.size() > stackDepth) {
                        assert frames.size() == stackDepth+1;
                        removedFrame = frames.pop();
                        if (removedFrame.interestingInstances != null && !removedFrame.interestingInstructions.isEmpty()) {
                            // ok, we have a control dependence since the method was called by (or for) this instruction
                            removedFrameIsInteresting = true;
                        }
                        currentFrame = frames.peek();
                    } else {
                        assert frames.size() == stackDepth-1;
                        final ExecutionFrame<InstructionInstance> newFrame = new ExecutionFrame<InstructionInstance>();
                        // assertion: if the current frame catched an exception, then the new frame
                        // must have thrown it
                        assert currentFrame.atCatchBlockStart == null
                            || instruction == instruction.getMethod().getAbnormalTerminationLabel();
                        newFrame.method = instruction.getMethod();
                        if (instruction == newFrame.method.getAbnormalTerminationLabel()) {
                            newFrame.throwsException = newFrame.interruptedControlFlow = true;
                        }
                        frames.push(newFrame);
                        currentFrame = newFrame;
                    }
                }

                // it is possible that we see successive instructions of different methods,
                // e.g. when called from native code
                if (currentFrame.method  == null) {
                    assert currentFrame.returnValue == null;
                    currentFrame.method = instruction.getMethod();
                } else if (currentFrame.finished || currentFrame.method != instruction.getMethod()) {
                    currentFrame = new ExecutionFrame<InstructionInstance>();
                    currentFrame.method = instruction.getMethod();
                    frames.set(stackDepth-1, currentFrame);
                }

                if (instruction == instruction.getMethod().getMethodEntryLabel())
                    currentFrame.finished = true;
                currentFrame.lastInstruction = instruction;

                final DynamicInformation dynInfo = this.simulator.simulateInstruction(instance, currentFrame,
                        removedFrame, frames);

                if (removedFrameIsInteresting) {
                    // checking if this is the instr. that called the method is impossible
                    dynamicSlice.add(instruction);
                    if (currentFrame.interestingInstructions == null)
                        currentFrame.interestingInstructions = new HashSet<Instruction>();
                    currentFrame.interestingInstructions.add(instruction);
                }

                if (slicingCriterion.matches(instance)) {
                    interestingVariables.addAll(slicingCriterion.getInterestingVariables(currentFrame));
                    Collection<Instruction> newInterestingInstructions = slicingCriterion.getInterestingInstructions(currentFrame);
                    if (!newInterestingInstructions.isEmpty()) {
                        if (currentFrame.interestingInstructions == null)
                            currentFrame.interestingInstructions = new HashSet<Instruction>();
                        currentFrame.interestingInstructions.addAll(newInterestingInstructions);
                    }
                }

                final boolean isExceptionsThrowingInstance = currentFrame.throwsException &&
                    (instruction.getType() != InstructionType.LABEL || !((LabelMarker)instruction).isAdditionalLabel());
                if ((currentFrame.interestingInstructions != null && !currentFrame.interestingInstructions.isEmpty())
                        || isExceptionsThrowingInstance) {
                    Set<Instruction> instrControlDependences = controlDependences.get(instruction.getIndex());
                    if (instrControlDependences == null) {
                        computeControlDependences(instruction.getMethod(), controlDependences);
                        instrControlDependences = controlDependences.get(instruction.getIndex());
                        assert instrControlDependences != null;
                    }
                    // get all interesting instructions, that are dependent on the current one
                    Set<Instruction> dependantInterestingInstructions = currentFrame.interestingInstructions == null
                        ? Collections.<Instruction>emptySet()
                        : intersect(instrControlDependences, currentFrame.interestingInstructions);
                    if (isExceptionsThrowingInstance) {
                        currentFrame.throwsException = false;
                        // in this case, we have an additional control dependence from the catching to
                        // the throwing instruction
                        for (int i = stackDepth-2; i >= 0; --i) {
                            final ExecutionFrame<InstructionInstance> f = frames.get(i);
                            if (f.atCatchBlockStart != null) {
                                if (f.interestingInstructions != null &&
                                        f.interestingInstructions.contains(f.atCatchBlockStart.getInstruction())) {
                                    if (dependantInterestingInstructions.isEmpty())
                                        dependantInterestingInstructions = Collections.singleton(f.atCatchBlockStart.getInstruction());
                                    else
                                        dependantInterestingInstructions.add(f.atCatchBlockStart.getInstruction());
                                }
                                break;
                            }
                        }
                    }
                    if (!dependantInterestingInstructions.isEmpty()) {
                        if (instruction.getType() != InstructionType.LABEL)
                            dynamicSlice.add(instruction);
                        if (currentFrame.interestingInstructions == null)
                            currentFrame.interestingInstructions = new HashSet<Instruction>();
                        else
                            currentFrame.interestingInstructions.removeAll(dependantInterestingInstructions);
                        currentFrame.interestingInstructions.add(instruction);
                        interestingVariables.addAll(dynInfo.getUsedVariables());
                    }
                }

                if (!interestingVariables.isEmpty()) {
                    for (final Variable definedVariable: dynInfo.getDefinedVariables()) {
                        if (interestingVariables.contains(definedVariable)) {
                            if (currentFrame.interestingInstructions == null)
                                currentFrame.interestingInstructions = new HashSet<Instruction>();
                            currentFrame.interestingInstructions.add(instruction);
                            dynamicSlice.add(instruction);
                            interestingVariables.remove(definedVariable);
                            interestingVariables.addAll(dynInfo.getUsedVariables(definedVariable));
                        }
                    }
                }

                if (dynInfo.isCatchBlock()) {
                    currentFrame.atCatchBlockStart = instance;
                } else if (currentFrame.atCatchBlockStart != null) {
                    currentFrame.atCatchBlockStart = null;
                }

            }
        } finally {
            for (ProgressMonitor mon : this.progressMonitors)
                mon.end();
        }

        return dynamicSlice;
    }

    private void computeControlDependences(final ReadMethod method, final IntegerMap<Set<Instruction>> controlDependences) {
        final Map<Instruction, Set<Instruction>> deps = ControlFlowAnalyser.getInstance().getInvControlDependences(method);
        for (final Entry<Instruction, Set<Instruction>> entry: deps.entrySet()) {
            final int index = entry.getKey().getIndex();
            assert !controlDependences.containsKey(index);
            controlDependences.put(index, entry.getValue());
        }
    }

    private static <T> Set<T> intersect(final Set<T> set1,
            final Set<T> set2) {
        if (set1.isEmpty() || set2.isEmpty())
            return Collections.emptySet();

        Set<T> smallerSet;
        Set<T> biggerSet;
        if (set1.size() < set2.size()) {
            smallerSet = set1;
            biggerSet = set2;
        } else {
            smallerSet = set2;
            biggerSet = set1;
        }

        Set<T> intersection = null;
        for (final T obj: smallerSet) {
            if (biggerSet.contains(obj)) {
                if (intersection == null)
                    intersection = new HashSet<T>();
                intersection.add(obj);
            }
        }

        if (intersection == null)
            return Collections.emptySet();
        return intersection;
    }

}
