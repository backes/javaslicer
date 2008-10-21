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
import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowAnalyser;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.IIncInstruction;
import de.unisb.cs.st.javaslicer.tracer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.tracer.traceResult.TraceResult.ThreadId;

public class Slicer implements Opcodes {

    protected static class ExecutionFrame {

        public final Set<Instruction> interestingInstructions = new HashSet<Instruction>();

        public Variable getLocalVariable(final int localVarIndex) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    private static class DynamicInformation {

        @SuppressWarnings("unchecked")
        public static final DynamicInformation EMPTY = new DynamicInformation(Collections.EMPTY_SET, null);

        private final Collection<Variable> usedVariables;
        private final Variable definedVariables;


        public DynamicInformation(final Collection<Variable> usedVariables, final Variable definedVariables) {
            this.usedVariables = usedVariables;
            this.definedVariables = definedVariables;
        }

        public Collection<Variable> getUsedVariables() {
            return this.usedVariables;
        }

        public Variable getDefinedVariable() {
            return this.definedVariables;
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
        frames.push(new ExecutionFrame());
        final Stack<Instruction> operandStack = new Stack<Instruction>();

        final Set<Variable> interestingVariables = new HashSet<Variable>();
        final Set<Instruction> dynamicSlice = new HashSet<Instruction>();

        while (backwardInsnItr.hasNext()) {
            final Instance instance = backwardInsnItr.next();
            final Instruction instruction = instance.getInstruction();

            final DynamicInformation dynInfo = simulateInstruction(instance, operandStack, frames.peek());

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

    private DynamicInformation simulateInstruction(final Instance inst, final Stack<Instruction> operandStack, final ExecutionFrame executionFrame) {
        final Variable var;
        final Collection<Variable> vars;
        switch (inst.getType()) {
        case IINC:
            var = executionFrame.getLocalVariable(((IIncInstruction)inst).getLocalVarIndex());
            return new DynamicInformation(Collections.singleton(var), var);
        case INT:
            operandStack.push(inst.getInstruction());
            return DynamicInformation.EMPTY;
        case SIMPLE:
            return simpulateSimpleInsn(inst, operandStack, executionFrame);
        }
        // TODO Auto-generated method stub
        return null;
    }

    private DynamicInformation simpulateSimpleInsn(final Instance inst, final Stack<Instruction> operandStack,
            final ExecutionFrame executionFrame) {
        DynamicInformation dynInfo;
        Collection<Variable> vars;
        switch (inst.getOpcode()) {
        case NOP:
            return DynamicInformation.EMPTY;

        case ACONST_NULL: case ICONST_M1: case ICONST_0: case ICONST_1: case ICONST_2: case ICONST_3:
        case ICONST_4: case ICONST_5: case LCONST_0: case LCONST_1: case FCONST_0: case FCONST_1: case FCONST_2:
        case DCONST_0: case DCONST_1:
            operandStack.push(inst.getInstruction());
            return DynamicInformation.EMPTY;

        case POP: case POP2: case DUP: case DUP_X1: case DUP_X2: case DUP2: case DUP2_X1: case DUP2_X2: case SWAP:
        case IADD: case LADD: case FADD: case DADD: case ISUB: case LSUB: case FSUB: case DSUB: case IMUL:
        case LMUL: case FMUL: case DMUL: case IDIV: case LDIV: case FDIV: case DDIV: case IREM: case LREM:
        case FREM: case DREM: case INEG: case LNEG: case FNEG: case DNEG: case ISHL: case LSHL: case ISHR:
        case LSHR: case IUSHR: case LUSHR: case IAND: case LAND: case IOR: case LOR: case IXOR: case LXOR:

        case I2L: case I2F: case I2D: case L2I: case L2F: case L2D: case F2I: case F2L: case F2D: case D2I:
        case D2L: case D2F: case I2B: case I2C: case I2S:
            vars = Collections.singleton((Variable)new StackEntry(operandStack.size()-1));
            dynInfo = new DynamicInformation(vars, null);
            operandStack.push(inst.getInstruction());
            return dynInfo;

        case LCMP: case FCMPL: case FCMPG: case DCMPL: case DCMPG:
        case IRETURN: case LRETURN: case FRETURN: case DRETURN: case ARETURN: case RETURN:
        case ARRAYLENGTH: case ATHROW: case MONITORENTER: case MONITOREXIT:
        default:
            assert false;
            return null;
        }
    }

}
