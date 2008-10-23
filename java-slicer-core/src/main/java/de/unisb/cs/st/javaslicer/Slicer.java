package de.unisb.cs.st.javaslicer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.javaslicer.VariableUsages.SimpleVariableUsage;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowAnalyser;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.FieldInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.IIncInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.VarInstruction;
import de.unisb.cs.st.javaslicer.tracer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.tracer.traceResult.TraceResult.ThreadId;

public class Slicer implements Opcodes {

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

        TraceResult trace = null;
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
        final Set<Instruction> slice = new Slicer(trace).getDynamicSlice(tracing, sc.getInstance());
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

    private static SlicingCriterion readSlicingCriteria(final String string, final List<ReadClass> readClasses)
            throws IllegalParameterException {
        final String[] criteria = string.split(",");
        if (criteria.length == 1)
            return SimpleSlicingCriterion.parse(criteria[0], readClasses);

        final CompoundSlicingCriterion crit = new CompoundSlicingCriterion();
        for (final String c: criteria)
            crit.add(SimpleSlicingCriterion.parse(c, readClasses));
        return crit;
    }

    @SuppressWarnings("unchecked")
    private Set<Instruction> getDynamicSlice(final ThreadId thread, final SlicingCriterion.Instance slicingCriterion) {
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
        final AtomicInteger operandStack = new AtomicInteger(0);

        final Set<Variable> interestingVariables = new HashSet<Variable>();
        final Set<Instruction> dynamicSlice = new HashSet<Instruction>();

        while (backwardInsnItr.hasNext()) {
            final Instance instance = backwardInsnItr.next();
            final Instruction instruction = instance.getInstruction();

            while (frames.size() < instance.getStackDepth()) {
                frames.push(new ExecutionFrame());
            }
            final ExecutionFrame currentFrame = frames.get(instance.getStackDepth()-1);
            final VariableUsages dynInfo = simulateInstruction(instance, operandStack, currentFrame);

            while (frames.size() > instance.getStackDepth()) {
                final ExecutionFrame lastFrame = frames.pop();
                if (!lastFrame.interestingInstructions.isEmpty()) {
                    dynamicSlice.add(instruction); // TODO check if this is the instr. that called the method
                    currentFrame.interestingInstructions.add(instruction);
                    interestingVariables.addAll(dynInfo.getUsedVariables()); // TODO should not be necessary
                }
            }

            if (slicingCriterion.matches(instance)) {
                interestingVariables.addAll(slicingCriterion.getInterestingVariables(currentFrame));
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

            for (final Variable definedVariable: dynInfo.getDefinedVariables()) {
                if (interestingVariables.contains(definedVariable)) {
                    currentFrame.interestingInstructions.add(instruction);
                    dynamicSlice.add(instruction);
                    interestingVariables.remove(definedVariable);
                    interestingVariables.addAll(dynInfo.getUsedVariables(definedVariable));
                }
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

    private VariableUsages simulateInstruction(final Instance inst, final AtomicInteger operandStack, final ExecutionFrame executionFrame) {
        final Variable var;
        Collection<Variable> vars = Collections.emptySet();
        switch (inst.getType()) {
        case FIELD:
            return simulateFieldInstruction((FieldInstruction.Instance)inst, operandStack);
        case IINC:
            var = executionFrame.getLocalVariable(((IIncInstruction)inst.getInstruction()).getLocalVarIndex());
            vars = Collections.singleton(var);
            return new SimpleVariableUsage(vars, vars);
        case INT:
            return new SimpleVariableUsage(vars,
                    Collections.singleton((Variable)new StackEntry(operandStack.decrementAndGet())));
        case LABEL:
            return VariableUsages.EMPTY;
        case SIMPLE:
            return simulateSimpleInsn(inst, operandStack);
        case VAR:
            return simulateVarInstruction((VarInstruction) inst.getInstruction(), operandStack, executionFrame);
        default:
            assert false;
            return null;
        }
    }

    private VariableUsages simulateFieldInstruction(final FieldInstruction.Instance instance, final AtomicInteger operandStack) {
        int stackDepth;
        final FieldInstruction instruction = (FieldInstruction) instance.getInstruction();
        switch (instruction.getOpcode()) {
        case GETFIELD:
            if (instruction.isLongValue()) {
                stackDepth = operandStack.decrementAndGet();
                return new SimpleVariableUsage(Arrays.asList(new StackEntry(stackDepth-1),
                            new ObjectField(instance.getObjectId(), instruction.getFieldName())),
                        Arrays.asList((Variable)new StackEntry(stackDepth-1), new StackEntry(stackDepth)));
            }
            stackDepth = operandStack.get();
            return new SimpleVariableUsage(Arrays.asList(new StackEntry(stackDepth-1),
                        new ObjectField(instance.getObjectId(), instruction.getFieldName())),
                    Collections.singleton((Variable)new StackEntry(stackDepth-1)));
        case GETSTATIC:
            if (instruction.isLongValue()) {
                stackDepth = operandStack.addAndGet(-2);
                return new SimpleVariableUsage(Collections.singleton((Variable)new ObjectField(-1, instruction.getFieldName())),
                        Arrays.asList((Variable)new StackEntry(stackDepth), new StackEntry(stackDepth+1)));
            }
            stackDepth = operandStack.decrementAndGet();
            return new SimpleVariableUsage(Collections.singleton((Variable)new ObjectField(-1, instruction.getFieldName())),
                    Arrays.asList((Variable)new StackEntry(stackDepth)));
        case PUTFIELD:
            if (instruction.isLongValue()) {
                stackDepth = operandStack.getAndAdd(3);
                return new SimpleVariableUsage(Arrays.asList((Variable)new StackEntry(stackDepth),
                        new StackEntry(stackDepth+1), new StackEntry(stackDepth+2)),
                        Collections.singleton((Variable)new ObjectField(instance.getObjectId(), instruction.getFieldName())));
            }
            stackDepth = operandStack.getAndAdd(2);
            return new SimpleVariableUsage(Arrays.asList((Variable)new StackEntry(stackDepth),
                    new StackEntry(stackDepth+1)),
                    Collections.singleton((Variable)new ObjectField(instance.getObjectId(), instruction.getFieldName())));
        case PUTSTATIC:
            if (instruction.isLongValue()) {
                stackDepth = operandStack.getAndAdd(2);
                return new SimpleVariableUsage(Arrays.asList((Variable)new StackEntry(stackDepth),
                        new StackEntry(stackDepth+1)),
                        Collections.singleton((Variable)new ObjectField(-1, instruction.getFieldName())));
            }
            stackDepth = operandStack.getAndIncrement();
            return new SimpleVariableUsage(Collections.singleton((Variable)new StackEntry(stackDepth)),
                    Collections.singleton((Variable)new ObjectField(-1, instruction.getFieldName())));
        default:
            assert false;
            return null;
        }
    }

    private VariableUsages simulateVarInstruction(final VarInstruction inst, final AtomicInteger operandStack,
            final ExecutionFrame executionFrame) {
        switch (inst.getOpcode()) {
        case ILOAD: case FLOAD: case ALOAD:
            int stackDepth = operandStack.addAndGet(-2);
            return new SimpleVariableUsage(
                    Collections.singleton(executionFrame.getLocalVariable(inst.getLocalVarIndex())),
                    Arrays.asList((Variable)new StackEntry(stackDepth), new StackEntry(stackDepth+1)));
        case LLOAD: case DLOAD:
            stackDepth = operandStack.decrementAndGet();
            return new SimpleVariableUsage(
                    Collections.singleton(executionFrame.getLocalVariable(inst.getLocalVarIndex())),
                    Collections.singleton((Variable)new StackEntry(stackDepth)));
        case ISTORE: case FSTORE: case ASTORE:
            stackDepth = operandStack.getAndIncrement();
            return new SimpleVariableUsage(
                    Collections.singleton((Variable)new StackEntry(stackDepth)),
                    Collections.singleton(executionFrame.getLocalVariable(inst.getLocalVarIndex())));
        case LSTORE: case DSTORE:
            stackDepth = operandStack.getAndAdd(2);
            return new SimpleVariableUsage(
                    Arrays.asList((Variable)new StackEntry(stackDepth), new StackEntry(stackDepth+1)),
                    Collections.singleton(executionFrame.getLocalVariable(inst.getLocalVarIndex())));
        case RET:
            final Set<Variable> emptySet = Collections.emptySet();
            return new SimpleVariableUsage(Collections.singleton(executionFrame.getLocalVariable(inst.getLocalVarIndex())),
                    emptySet);
        default:
            assert false;
            return null;
        }
    }

    private VariableUsages simulateSimpleInsn(final Instance inst, final AtomicInteger operandStackSize) {
        switch (inst.getOpcode()) {
        case NOP:
            return stackManipulation(operandStackSize, 0, 0);

        case ACONST_NULL: case ICONST_M1: case ICONST_0: case ICONST_1: case ICONST_2: case ICONST_3:
        case ICONST_4: case ICONST_5: case LCONST_0: case LCONST_1: case FCONST_0: case FCONST_1: case FCONST_2:
        case DCONST_0: case DCONST_1:
            return stackManipulation(operandStackSize, 0, 1);

        case POP: case POP2: case DUP: case DUP_X1: case DUP_X2: case DUP2: case DUP2_X1: case DUP2_X2: case SWAP:
        case IADD: case LADD: case FADD: case DADD: case ISUB: case LSUB: case FSUB: case DSUB: case IMUL:
        case LMUL: case FMUL: case DMUL: case IDIV: case LDIV: case FDIV: case DDIV: case IREM: case LREM:
        case FREM: case DREM: case INEG: case LNEG: case FNEG: case DNEG: case ISHL: case LSHL: case ISHR:
        case LSHR: case IUSHR: case LUSHR: case IAND: case LAND: case IOR: case LOR: case IXOR: case LXOR:

        case I2F: case F2I:
        case D2F: case I2B: case I2C: case I2S:
            return stackManipulation(operandStackSize, 1, 1);
        case I2L: case I2D: case F2L: case F2D:
            return stackManipulation(operandStackSize, 1, 2);
        case L2I: case L2F: case D2I:
            return stackManipulation(operandStackSize, 2, 1);
        case L2D: case D2L:
            return stackManipulation(operandStackSize, 2, 2);

        case LCMP: case DCMPL: case DCMPG:
            return stackManipulation(operandStackSize, 4, 1);
        case FCMPL: case FCMPG:
            return stackManipulation(operandStackSize, 2, 1);

        case IRETURN: case FRETURN: case ARETURN:
            return stackManipulation(operandStackSize, 1, 0);
        case DRETURN: case LRETURN:
            return stackManipulation(operandStackSize, 2, 0);
        case RETURN:
            return stackManipulation(operandStackSize, 0, 0);

        case ARRAYLENGTH:
            return stackManipulation(operandStackSize, 1, 1);

        case ATHROW:
            return stackManipulation(operandStackSize, 1, 0);
        case MONITORENTER: case MONITOREXIT:
            return stackManipulation(operandStackSize, 1, 0);
        default:
            assert false;
            return null;
        }
    }

    private VariableUsages stackManipulation(final AtomicInteger operandStackSize, final int read, final int write) {
        final int oldStackSize = read == write ? operandStackSize.get() : operandStackSize.getAndAdd(read - write);
        final int newStackSize = oldStackSize + read - write;
        final Variable[] readVarsArr;
        final Variable[] writtenVarsArr;
        Collection<Variable> readVars;
        final Collection<Variable> writtenVars;
        if (read == 0) {
            readVars = Collections.emptySet();
        } else if (read == 1) {
            readVars = Collections.singleton((Variable)new StackEntry(newStackSize-1));
        } else {
            readVarsArr = new Variable[read];
            for (int i = 0; i < read; ++i)
                readVarsArr[i] = new StackEntry(newStackSize-i-1);
            readVars = Arrays.asList(readVarsArr);
        }
        if (write == read) {
            writtenVars = readVars;
        } else if (write == 0) {
            writtenVars = Collections.emptySet();
        } else if (write == 1) {
            writtenVars = Collections.singleton((Variable)new StackEntry(oldStackSize-1));
        } else {
            writtenVarsArr = new Variable[write];
            for (int i = 0; i < write; ++i)
                writtenVarsArr[i] = new StackEntry(oldStackSize-i-1);
            writtenVars = Arrays.asList(writtenVarsArr);
        }
        return new SimpleVariableUsage(readVars, writtenVars);
    }

}
