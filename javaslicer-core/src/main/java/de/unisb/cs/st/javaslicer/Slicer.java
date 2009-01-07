package de.unisb.cs.st.javaslicer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.ArrayStack;
import de.hammacher.util.IntegerMap;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Type;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowAnalyser;
import de.unisb.cs.st.javaslicer.dependencyAnalysis.ExecutionFrame;
import de.unisb.cs.st.javaslicer.instructionSimulation.Simulator;
import de.unisb.cs.st.javaslicer.traceResult.ThreadId;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.variableUsages.VariableUsages;
import de.unisb.cs.st.javaslicer.variables.Variable;

public class Slicer implements Opcodes {

    private final TraceResult trace;
    private final Simulator simulator = new Simulator();

    public Slicer(final TraceResult trace) {
        this.trace = trace;
    }

    public static void main(final String[] args) {

        if (args.length < 2 || args.length > 3) {
            usage();
            System.exit(-1);
        }

        final File traceFile = new File(args[0]);

        Long threadId = null;
        try {
            threadId = Long.valueOf(args[1]);
            if (args.length < 3) {
                usage();
                System.exit(-1);
            }
        } catch (final NumberFormatException e) {
            // ignore
        }

        TraceResult trace;
        try {
            trace = TraceResult.readFrom(traceFile);
        } catch (final IOException e) {
            System.err.println("Could not read the trace file: " + e);
            System.exit(-1);
            return;
        }

        SlicingCriterion sc = null;
        try {
            sc = readSlicingCriteria(args[threadId == null ? 1 : 2], trace.getReadClasses());
        } catch (final IllegalParameterException e) {
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
        final Set<Instruction> slice = new Slicer(trace).getDynamicSlice(tracing, sc.getInstance());
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

    private static void usage() {
        System.err.println("Usage: java [<javaoptions>] " + Slicer.class.getName()
                + " <trace file> [<threadId>] <loc>[(<occ>)]:<var>[,<loc>[(<occ>)]:<var>]*");
    }

    public static SlicingCriterion readSlicingCriteria(final String string, final List<ReadClass> readClasses)
            throws IllegalParameterException {
        CompoundSlicingCriterion crit = null;
        int oldPos = 0;
        while (true) {
            int bracketPos = string.indexOf('{', oldPos);
            int commaPos = string.indexOf(',', oldPos);
            while (bracketPos != -1 && bracketPos < commaPos) {
                final int closeBracketPos = string.indexOf('}', bracketPos+1);
                if (closeBracketPos == -1)
                    throw new IllegalParameterException("Couldn't find matching '}'");
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

    public Set<Instruction> getDynamicSlice(final ThreadId threadId, final SlicingCriterion.Instance slicingCriterion) {
        final Iterator<Instance> backwardInsnItr = this.trace.getBackwardIterator(threadId, null);

        final IntegerMap<Set<Instruction>> controlDependencies = new IntegerMap<Set<Instruction>>();

        final ArrayStack<ExecutionFrame> frames = new ArrayStack<ExecutionFrame>();

        final Set<Variable> interestingVariables = new HashSet<Variable>();
        final Set<Instruction> dynamicSlice = new HashSet<Instruction>();

        ExecutionFrame currentFrame = new ExecutionFrame();
        frames.push(currentFrame);

        while (backwardInsnItr.hasNext()) {
            final Instance instance = backwardInsnItr.next();
            final Instruction instruction = instance.getInstruction();

            ExecutionFrame removedFrame = null;
            boolean removedFrameIsInteresting = false;
            final int stackDepth = instance.getStackDepth();
            assert stackDepth >= 0;

            if (frames.size() != stackDepth) {
                if (frames.size() > stackDepth) {
                    assert frames.size() == stackDepth+1;
                    removedFrame = frames.pop();
                    if (!removedFrame.interestingInstances.isEmpty()) {
                        // ok, we have a control dependency since the method was called by (or for) this instruction
                        removedFrameIsInteresting = true;
                    }
                } else {
                    assert frames.size() == stackDepth-1;
                    ExecutionFrame topFrame = frames.size() == 0 ? null : frames.peek();
                    final ExecutionFrame newFrame = new ExecutionFrame();
                    if (topFrame != null && topFrame.atCacheBlockStart != null)
                        newFrame.throwsException = true;
                    frames.push(newFrame);
                }
                currentFrame = frames.peek();
            }

            // it is possible that we see successive instructions of different methods,
            // e.g. when called from native code
            if (currentFrame.method != instruction.getMethod()) {
                if (currentFrame.method == null) {
                    currentFrame.method = instruction.getMethod();
                } else {
                    currentFrame = new ExecutionFrame();
                    currentFrame.method = instruction.getMethod();
                    frames.set(stackDepth-1, currentFrame);
                }
            }

            final VariableUsages dynInfo = this.simulator.simulateInstruction(instance, currentFrame,
                    removedFrame, frames);

            if (removedFrameIsInteresting) {
                // checking if this is the instr. that called the method is impossible
                dynamicSlice.add(instruction);
                currentFrame.interestingInstructions.add(instruction);
            }

            if (slicingCriterion.matches(instance)) {
                interestingVariables.addAll(slicingCriterion.getInterestingVariables(currentFrame));
                currentFrame.interestingInstructions.addAll(slicingCriterion.getInterestingInstructions(currentFrame));
            }

            boolean isExceptionsThrowingInstance = currentFrame.throwsException &&
                (instruction.getType() != Type.LABEL || !((LabelMarker)instruction).isAdditionalLabel());
            if (!currentFrame.interestingInstructions.isEmpty() || isExceptionsThrowingInstance) {
                Set<Instruction> instrControlDependencies = controlDependencies.get(instruction.getIndex());
                if (instrControlDependencies == null) {
                    computeControlDependencies(instruction.getMethod(), controlDependencies);
                    instrControlDependencies = controlDependencies.get(instruction.getIndex());
                    assert instrControlDependencies != null;
                }
                // get all interesting instructions, that are dependent on the current one
                Set<Instruction> dependantInterestingInstructions = intersect(instrControlDependencies,
                        currentFrame.interestingInstructions);
                if (isExceptionsThrowingInstance) {
                    currentFrame.throwsException = false;
                    // in this case, we have an additional control dependency from the catching to
                    // the throwing instruction
                    for (int i = stackDepth-2; i >= 0; --i) {
                        final ExecutionFrame f = frames.get(i);
                        if (f.atCacheBlockStart != null) {
                            if (f.interestingInstructions.contains(f.atCacheBlockStart.getInstruction())) {
                                if (dependantInterestingInstructions.isEmpty())
                                    dependantInterestingInstructions = Collections.singleton(f.atCacheBlockStart.getInstruction());
                                else
                                    dependantInterestingInstructions.add(f.atCacheBlockStart.getInstruction());
                            }
                            break;
                        }
                    }
                }
                if (!dependantInterestingInstructions.isEmpty()) {
                    if (instruction.getType() != Type.LABEL)
                        dynamicSlice.add(instruction);
                    currentFrame.interestingInstructions.removeAll(dependantInterestingInstructions);
                    currentFrame.interestingInstructions.add(instruction);
                    interestingVariables.addAll(dynInfo.getUsedVariables());
                }
            }

            if (!interestingVariables.isEmpty()) {
                for (final Variable definedVariable: dynInfo.getDefinedVariables()) {
                    if (interestingVariables.contains(definedVariable)) {
                        currentFrame.interestingInstructions.add(instruction);
                        dynamicSlice.add(instruction);
                        interestingVariables.remove(definedVariable);
                        interestingVariables.addAll(dynInfo.getUsedVariables(definedVariable));
                    }
                }
            }

            if (dynInfo.isCatchBlock())
                currentFrame.atCacheBlockStart = instance;
            else if (currentFrame.atCacheBlockStart != null)
                currentFrame.atCacheBlockStart = null;

        }

        return dynamicSlice;
    }

    private void computeControlDependencies(final ReadMethod method, final IntegerMap<Set<Instruction>> controlDependencies) {
        final Map<Instruction, Set<Instruction>> deps = ControlFlowAnalyser.getInstance().getInvControlDependencies(method);
        for (final Entry<Instruction, Set<Instruction>> entry: deps.entrySet()) {
            final int index = entry.getKey().getIndex();
            assert !controlDependencies.containsKey(index);
            controlDependencies.put(index, entry.getValue());
        }
    }

    private static <T> Set<T> intersect(final Set<T> set1,
            final Set<T> set2) {
        if (set1.size() == 0 || set2.size() == 0)
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
