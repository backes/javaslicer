package de.unisb.cs.st.javaslicer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;

import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowAnalyser;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.tracer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.tracer.traceResult.TraceResult.ThreadId;

public class Slicer {

    public class ExecutionFrame {

        final Set<Instruction> interestingInstructions = new HashSet<Instruction>();

    }

    public class DynamicInformation {

        public Collection<Variable> getUsedVariables() {
            // TODO Auto-generated method stub
            return null;
        }

        public Variable getDefinedVariable() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    private final TraceResult trace;

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

        final SlicingCriterion sc = readSlicingCriteria(args[threadId == null ? 1 : 2]);

        TraceResult trace = null;
        try {
            trace = TraceResult.readFrom(traceFile);
        } catch (final IOException e) {
            System.err.println("Could not read the trace file: " + e);
            System.exit(-1);
        }

        final List<ThreadId> threads = trace.getThreads();
        if (threads.size() == 0) {
            System.err.println("The trace file contains no tracing information.");
            System.exit(-1);
        }

        ThreadId tracing = null;
        for (final ThreadId t: threads) {
            if (threadId == null) {
                if ("main".equals(t.getThreadName()) && (tracing == null || t.getThreadId() < tracing.getThreadId()))
                    tracing = t;
            } else if (t.getThreadId() == threadId.longValue()) {
                tracing = t;
            }
        }

        if (tracing == null) {
            System.err.println(threadId == null ? "Couldn't find the main thread."
                    : "The thread you specified was not found.");
            System.exit(-1);
        }

        final long startTime = System.nanoTime();
        final Set<Instruction> slice = new Slicer(trace).getDynamicSlice(tracing, sc);
        final long endTime = System.nanoTime();

        System.out.println("The dynamic slice for " + sc + ":");
        for (final Instruction insn: slice) {
            System.out.format((Locale)null, "%s.%s:%d %s%n",
                    insn.getMethod().getReadClass().getName(),
                    insn.getMethod().getName(),
                    insn.getLineNumber(),
                    insn.toString());
        }
        System.out.format((Locale)null, "%nComputation took %.2f seconds.%n", 1e-9*(endTime-startTime));
    }

    private static void usage() {
        System.err.println("Usage: java [<javaoptions>] " + Slicer.class.getName()
                + " <trace file> [<threadId>] <loc>[(<occ>)]:<var>[,<loc>[(<occ>)]:<var>]*");
    }

    private static SlicingCriterion readSlicingCriteria(final String string) {
        final String[] criteria = string.split(",");
        if (criteria.length == 1)
            return SimpleSlicingCriterion.parse(criteria[0]);

        final CompoundSlicingCriterion crit = new CompoundSlicingCriterion();
        for (final String c: criteria)
            crit.add(SimpleSlicingCriterion.parse(c));
        return crit;
    }

    @SuppressWarnings("unchecked")
    private Set<Instruction> getDynamicSlice(final ThreadId thread, final SlicingCriterion sc) {
        final Iterator<Instance> backwardInsnItr = this.trace.getBackwardIterator(thread.getThreadId());

        final Map<ReadMethod, Map<Instruction, Set<Instruction>>> allControlDependencies =
            LazyMap.decorate(new HashMap<ReadMethod, Map<Instruction, Set<Instruction>>>(),
                    new Transformer() {
                        @Override
                        public Object transform(final Object method) {
                            return ControlFlowAnalyser.getInstance().getControlDependencies((ReadMethod) method);
                        }
                    });

        final Stack<ExecutionFrame> frames = new Stack<ExecutionFrame>();
        final Stack<Instruction> operandStack = new Stack<Instruction>();

        final Set<Variable> interestingVariables = new HashSet<Variable>();
        final Set<Instruction> dynamicSlice = new HashSet<Instruction>();

        while (backwardInsnItr.hasNext()) {
            final Instance instance = backwardInsnItr.next();
            final Instruction instruction = instance.getInstruction();

            final DynamicInformation dynInfo = simulateInstructions(instance);

            while (frames.size() <= instance.getStackDepth()) {
                frames.push(new ExecutionFrame());
            }
            final ExecutionFrame currentFrame = frames.get(instance.getStackDepth());
            while (frames.size() + 1 > instance.getStackDepth()) {
                final ExecutionFrame lastFrame = frames.pop();
                if (!lastFrame.interestingInstructions.isEmpty()) {
                    dynamicSlice.add(instruction); // TODO check if this is the instr. that called the method
                    currentFrame.interestingInstructions.add(instruction);
                    interestingVariables.addAll(dynInfo.getUsedVariables()); // TODO should not be necessary
                }
            }

            if (sc.matches(instance)) {
                interestingVariables.addAll(sc.getInterestingVariables());
                currentFrame.interestingInstructions.add(instruction); // TODO check this
                dynamicSlice.add(instruction); // TODO check this
            }

            if (!currentFrame.interestingInstructions.isEmpty()) {
                final Set<Instruction> controlDependencies =
                    allControlDependencies.get(instance.getMethod()).get(instruction);
                final Set<Instruction> dependantInterestingInstructions = intersect(controlDependencies,
                        currentFrame.interestingInstructions);
                if (!dependantInterestingInstructions.isEmpty()) {
                    dynamicSlice.add(instruction);
                    currentFrame.interestingInstructions.removeAll(dependantInterestingInstructions);
                    currentFrame.interestingInstructions.add(instruction);
                    interestingVariables.addAll(dynInfo.getUsedVariables());
                }
            }

            final Variable definedVariable = dynInfo.getDefinedVariable();
            if (definedVariable != null && interestingVariables.contains(definedVariable)) {
                currentFrame.interestingInstructions.add(instruction);
                dynamicSlice.add(instruction);
                interestingVariables.remove(definedVariable);
                if (!dynInfo.getUsedVariables().isEmpty())
                    interestingVariables.addAll(dynInfo.getUsedVariables());
            }

        }

        return dynamicSlice;
    }

    private static <T> Set<T> intersect(final Set<T> set1,
            final Set<T> set2) {
        if (set1.size() == 0 || set2.size() == 0)
            return Collections.emptySet();

        Set<T> smallerSet, biggerSet;
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

    private DynamicInformation simulateInstructions(final Instance inst) {
        // TODO Auto-generated method stub
        return null;
    }

}
