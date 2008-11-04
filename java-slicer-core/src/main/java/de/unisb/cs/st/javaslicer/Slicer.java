package de.unisb.cs.st.javaslicer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowAnalyser;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction.Type;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.ArrayInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.FieldInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.IIncInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.JumpInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.LabelMarker;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.LdcInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.MethodInvocationInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.MultiANewArrayInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.TypeInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.VarInstruction;
import de.unisb.cs.st.javaslicer.tracer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.tracer.traceResult.TraceResult.ThreadId;
import de.unisb.cs.st.javaslicer.tracer.util.IntegerMap;

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
            return;
        }

        final long startTime = System.nanoTime();
        final Set<Instruction> slice = new Slicer(trace).getDynamicSlice(tracing.getThreadId(), sc.getInstance());
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
        System.out.format((Locale)null, "%nComputation took %.2f seconds.%n", 1e-9*(endTime-startTime));
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

    public Set<Instruction> getDynamicSlice(final long threadId, final SlicingCriterion.Instance slicingCriterion) {
        final Iterator<Instance> backwardInsnItr = this.trace.getBackwardIterator(threadId);

        final IntegerMap<Set<Instruction>> controlDependencies = new IntegerMap<Set<Instruction>>();

        final Stack<ExecutionFrame> frames = new Stack<ExecutionFrame>();
        frames.push(new ExecutionFrame());

        final Set<Variable> interestingVariables = new HashSet<Variable>();
        final Set<Instruction> dynamicSlice = new HashSet<Instruction>();

        while (backwardInsnItr.hasNext()) {
            final Instance instance = backwardInsnItr.next();
            final Instruction instruction = instance.getInstruction();

            ExecutionFrame removedFrame = null;
            boolean removedFrameIsInteresting = false;
            while (frames.size() > instance.getStackDepth()) {
                assert frames.size() == instance.getStackDepth()+1;
                removedFrame = frames.pop();
                if (!removedFrame.interestingInstructions.isEmpty()) {
                    // ok, we have a control dependency since the method was called by (or for) this instruction
                    removedFrameIsInteresting = true;
                }
            }

            ExecutionFrame topFrame = null;
            while (frames.size() < instance.getStackDepth()) {
                if (topFrame == null && frames.size() > 0)
                    topFrame = frames.peek();
                final ExecutionFrame newFrame = new ExecutionFrame();
                if (topFrame != null && topFrame.atCacheBlockStart != null)
                    newFrame.throwsException = true;
                frames.push(newFrame);
            }
            ExecutionFrame currentFrame = frames.get(instance.getStackDepth()-1);
            if (currentFrame.method == null) {
                currentFrame.method = instruction.getMethod();
            } else if (instance.getStackDepth() == 1 && currentFrame.method != instance.getMethod()) {
                currentFrame = new ExecutionFrame();
                currentFrame.method = instruction.getMethod();
                frames.set(instance.getStackDepth()-1, currentFrame);
            }
            assert currentFrame.method == instance.getMethod();

            final VariableUsages dynInfo = simulateInstruction(instance, currentFrame, removedFrame, frames);

            if (removedFrameIsInteresting) {
                dynamicSlice.add(instruction); // TODO check if this is the instr. that called the method
                currentFrame.interestingInstructions.add(instruction);
            }

            if (slicingCriterion.matches(instance)) {
                interestingVariables.addAll(slicingCriterion.getInterestingVariables(currentFrame));
            }

            if (!currentFrame.interestingInstructions.isEmpty() || currentFrame.throwsException) {
                Set<Instruction> instrControlDependencies = controlDependencies.get(instruction.getIndex());
                if (instrControlDependencies == null) {
                    computeControlDependencies(instruction.getMethod(), controlDependencies);
                    instrControlDependencies = controlDependencies.get(instruction.getIndex());
                    assert instrControlDependencies != null;
                }
                // get all interesting instructions, that are dependent on the current one
                Set<Instruction> dependantInterestingInstructions = intersect(instrControlDependencies,
                        currentFrame.interestingInstructions);
                if (currentFrame.throwsException) {
                    currentFrame.throwsException = false;
                    // in this case, we have an additional control dependency from the catching to
                    // the throwing instruction
                    for (int i = instance.getStackDepth()-2; i >= 0; --i) {
                        final ExecutionFrame f = frames.get(i);
                        if (f.atCacheBlockStart != null) {
                            if (f.interestingInstructions.contains(f.atCacheBlockStart)) {
                                if (dependantInterestingInstructions.isEmpty())
                                    dependantInterestingInstructions = Collections.singleton((Instruction)f.atCacheBlockStart);
                                else
                                    dependantInterestingInstructions.add(f.atCacheBlockStart);
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

            for (final Variable definedVariable: dynInfo.getDefinedVariables()) {
                if (interestingVariables.contains(definedVariable)) {
                    currentFrame.interestingInstructions.add(instruction);
                    dynamicSlice.add(instruction);
                    interestingVariables.remove(definedVariable);
                    interestingVariables.addAll(dynInfo.getUsedVariables(definedVariable));
                }
            }

            if (dynInfo.isCatchBlock())
                currentFrame.atCacheBlockStart = (LabelMarker) instruction;
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

    private VariableUsages simulateInstruction(final Instance inst,
            final ExecutionFrame executionFrame, final ExecutionFrame removedFrame, final Stack<ExecutionFrame> allFrames) {
        final Variable var;
        Collection<Variable> vars;
        switch (inst.getType()) {
        case ARRAY:
            return simulateArrayInstruction((ArrayInstruction.Instance)inst, executionFrame);
        case FIELD:
            return simulateFieldInstruction((FieldInstruction.Instance)inst, executionFrame);
        case IINC:
            var = executionFrame.getLocalVariable(((IIncInstruction)inst.getInstruction()).getLocalVarIndex());
            vars = Collections.singleton(var);
            return new SimpleVariableUsage(vars, vars);
        case INT:
            vars = Collections.emptySet();
            return new SimpleVariableUsage(vars, Collections.singleton(
                    (Variable)executionFrame.getStackEntry(executionFrame.operandStack.decrementAndGet())));
        case JUMP:
            return simulateJumpInsn((JumpInstruction)inst.getInstruction(), executionFrame);
        case LABEL:
            if (((LabelMarker)inst.getInstruction()).isCatchBlock()) {
                executionFrame.operandStack.decrementAndGet();
                return VariableUsages.CATCHBLOCK;
            }
            return VariableUsages.EMPTY;
        case LDC:
            vars = Collections.emptySet();
            if (((LdcInstruction)inst.getInstruction()).constantIsLong()) {
                final int stackHeight = executionFrame.operandStack.addAndGet(-2);
                return new SimpleVariableUsage(vars, Arrays.asList((Variable)executionFrame.getStackEntry(stackHeight),
                        executionFrame.getStackEntry(stackHeight+1)));
            }
            return new SimpleVariableUsage(vars, executionFrame.getStackEntry(executionFrame.operandStack.decrementAndGet()));
        case LOOKUPSWITCH:
        case TABLESWITCH:
            return new SimpleVariableUsage(executionFrame.getStackEntry(executionFrame.operandStack.getAndIncrement()),
                    VariableUsages.EMPTY_VARIABLE_SET);
        case METHODINVOCATION:
            return simulateMethodInsn((MethodInvocationInstruction)inst.getInstruction(),
                    executionFrame, removedFrame);
        case MULTIANEWARRAY:
            return stackManipulation(executionFrame, ((MultiANewArrayInstruction)inst.getInstruction()).getDimension(), 1);
        case NEWARRAY:
            return stackManipulation(executionFrame, 1, 1);
        case SIMPLE:
            return simulateSimpleInsn(inst, executionFrame, allFrames);
        case TYPE:
            return simulateTypeInsn((TypeInstruction)inst.getInstruction(), executionFrame);
        case VAR:
            return simulateVarInstruction((VarInstruction) inst.getInstruction(), executionFrame);
        default:
            assert false;
            return null;
        }
    }

    private VariableUsages simulateTypeInsn(final TypeInstruction instruction, final ExecutionFrame frame) {
        switch (instruction.getOpcode()) {
        case Opcodes.NEW:
            return stackManipulation(frame, 0, 1);
        case Opcodes.ANEWARRAY:
            return stackManipulation(frame, 1, 1);
        case Opcodes.CHECKCAST:
            return new SimpleVariableUsage(frame.getStackEntry(frame.operandStack.get()-1), VariableUsages.EMPTY_VARIABLE_SET);
        case Opcodes.INSTANCEOF:
            return stackManipulation(frame, 1, 1);
        default:
            assert false;
            return null;
        }
    }

    private VariableUsages simulateJumpInsn(final JumpInstruction instruction, final ExecutionFrame frame) {
        switch (instruction.getOpcode()) {
        case IFEQ: case IFNE: case IFLT: case IFGE: case IFGT: case IFLE:
            return new SimpleVariableUsage(frame.getStackEntry(frame.operandStack.getAndIncrement()), VariableUsages.EMPTY_VARIABLE_SET);

        case IF_ICMPEQ: case IF_ICMPNE: case IF_ICMPLT: case IF_ICMPGE:
        case IF_ICMPGT: case IF_ICMPLE: case IF_ACMPEQ: case IF_ACMPNE:
        case IFNULL: case IFNONNULL:
            final int oldSize = frame.operandStack.getAndAdd(2);
            return new SimpleVariableUsage(Arrays.asList((Variable)frame.getStackEntry(oldSize), frame.getStackEntry(oldSize+1)),
                    VariableUsages.EMPTY_VARIABLE_SET);

        case GOTO:
            return VariableUsages.EMPTY;

        case JSR:
            return new SimpleVariableUsage(VariableUsages.EMPTY_VARIABLE_SET, frame.getStackEntry(frame.operandStack.decrementAndGet()));

        default:
            assert false;
            return null;
        }
    }

    private VariableUsages simulateArrayInstruction(final ArrayInstruction.Instance inst, final ExecutionFrame frame) {
        switch (inst.getOpcode()) {
        case IALOAD: case FALOAD: case AALOAD: case BALOAD: case CALOAD: case SALOAD:
            int stackDepth = frame.operandStack.getAndIncrement();
            ArrayElement arrayElem = new ArrayElement(inst.getArrayId(), inst.getArrayIndex());
            return new SimpleVariableUsage(Arrays.asList(frame.getStackEntry(stackDepth-1), frame.getStackEntry(stackDepth),
                    arrayElem), frame.getStackEntry(stackDepth-1));
        case LALOAD: case DALOAD:
            stackDepth = frame.operandStack.get();
            arrayElem = new ArrayElement(inst.getArrayId(), inst.getArrayIndex());
            return new SimpleVariableUsage(Arrays.asList(frame.getStackEntry(stackDepth-2), frame.getStackEntry(stackDepth-1),
                    arrayElem), Arrays.asList((Variable)frame.getStackEntry(stackDepth-2), frame.getStackEntry(stackDepth-1)));
        case IASTORE: case FASTORE: case AASTORE: case BASTORE: case CASTORE: case SASTORE:
            stackDepth = frame.operandStack.getAndAdd(3);
            arrayElem = new ArrayElement(inst.getArrayId(), inst.getArrayIndex());
            return new SimpleVariableUsage(Arrays.asList((Variable)frame.getStackEntry(stackDepth),
                    frame.getStackEntry(stackDepth+1), frame.getStackEntry(stackDepth+2)),
                    arrayElem);
        case LASTORE: case DASTORE:
            stackDepth = frame.operandStack.getAndAdd(4);
            arrayElem = new ArrayElement(inst.getArrayId(), inst.getArrayIndex());
            return new SimpleVariableUsage(Arrays.asList((Variable)frame.getStackEntry(stackDepth),
                    frame.getStackEntry(stackDepth+1), frame.getStackEntry(stackDepth+2), frame.getStackEntry(stackDepth+3)),
                    arrayElem);
        default:
            assert false;
            return null;
        }
    }

    private VariableUsages simulateMethodInsn(final MethodInvocationInstruction inst,
            final ExecutionFrame executionFrame, final ExecutionFrame removedFrame) {
        final boolean hasRemovedFrame = removedFrame != null
            && inst.getMethodName().equals(removedFrame.method.getName())
            && inst.getMethodDesc().equals(removedFrame.method.getDesc());
        int paramCount = inst.getOpcode() == INVOKESTATIC ? 0 : 1;
        for (int param = inst.getParameterCount()-1; param >= 0; --param)
            paramCount += inst.parameterIsLong(param) ? 2 : 1;
        final int returnedSize = inst.returnValueIsLong() ? 2 : 1;
        if (!hasRemovedFrame) {
            final int parametersStackOffset = executionFrame.operandStack.getAndAdd(paramCount-returnedSize)-1;
            return new MethodInvokationVariableUsages(parametersStackOffset,
                    paramCount, returnedSize, executionFrame, null);
        }

        final int parametersStackOffset = executionFrame.operandStack.getAndAdd(paramCount)+returnedSize-1;
        return new MethodInvokationVariableUsages(parametersStackOffset,
                paramCount, 0, executionFrame, removedFrame);
    }

    private VariableUsages simulateFieldInstruction(final FieldInstruction.Instance instance, final ExecutionFrame frame) {
        int stackDepth;
        final FieldInstruction instruction = (FieldInstruction) instance.getInstruction();
        switch (instruction.getOpcode()) {
        case GETFIELD:
            if (instruction.isLongValue()) {
                stackDepth = frame.operandStack.decrementAndGet();
                return new SimpleVariableUsage(Arrays.asList(frame.getStackEntry(stackDepth-1),
                            new ObjectField(instance.getObjectId(), instruction.getFieldName())),
                        Arrays.asList((Variable)frame.getStackEntry(stackDepth-1), frame.getStackEntry(stackDepth)));
            }
            stackDepth = frame.operandStack.get();
            return new SimpleVariableUsage(Arrays.asList(frame.getStackEntry(stackDepth-1),
                        new ObjectField(instance.getObjectId(), instruction.getFieldName())),
                    frame.getStackEntry(stackDepth-1));
        case GETSTATIC:
            if (instruction.isLongValue()) {
                stackDepth = frame.operandStack.addAndGet(-2);
                return new SimpleVariableUsage(new ObjectField(-1, instruction.getFieldName()),
                        Arrays.asList((Variable)frame.getStackEntry(stackDepth), frame.getStackEntry(stackDepth+1)));
            }
            stackDepth = frame.operandStack.decrementAndGet();
            return new SimpleVariableUsage(new ObjectField(-1, instruction.getFieldName()),
                    Arrays.asList((Variable)frame.getStackEntry(stackDepth)));
        case PUTFIELD:
            if (instruction.isLongValue()) {
                stackDepth = frame.operandStack.getAndAdd(3);
                return new SimpleVariableUsage(Arrays.asList((Variable)frame.getStackEntry(stackDepth),
                        frame.getStackEntry(stackDepth+1), frame.getStackEntry(stackDepth+2)),
                        new ObjectField(instance.getObjectId(), instruction.getFieldName()));
            }
            stackDepth = frame.operandStack.getAndAdd(2);
            return new SimpleVariableUsage(Arrays.asList((Variable)frame.getStackEntry(stackDepth),
                    frame.getStackEntry(stackDepth+1)),
                    new ObjectField(instance.getObjectId(), instruction.getFieldName()));
        case PUTSTATIC:
            if (instruction.isLongValue()) {
                stackDepth = frame.operandStack.getAndAdd(2);
                return new SimpleVariableUsage(Arrays.asList((Variable)frame.getStackEntry(stackDepth),
                        frame.getStackEntry(stackDepth+1)),
                        new ObjectField(-1, instruction.getFieldName()));
            }
            stackDepth = frame.operandStack.getAndIncrement();
            return new SimpleVariableUsage(Collections.singleton((Variable)frame.getStackEntry(stackDepth)),
                    new ObjectField(-1, instruction.getFieldName()));
        default:
            assert false;
            return null;
        }
    }

    private VariableUsages simulateVarInstruction(final VarInstruction inst, final ExecutionFrame frame) {
        switch (inst.getOpcode()) {
        case ILOAD: case FLOAD: case ALOAD:
            int stackDepth = frame.operandStack.decrementAndGet();
            return new SimpleVariableUsage(frame.getLocalVariable(inst.getLocalVarIndex()),
                    frame.getStackEntry(stackDepth));
        case LLOAD: case DLOAD:
            stackDepth = frame.operandStack.addAndGet(-2);
            return new SimpleVariableUsage(frame.getLocalVariable(inst.getLocalVarIndex()),
                    Arrays.asList((Variable)frame.getStackEntry(stackDepth), frame.getStackEntry(stackDepth+1)));
        case ISTORE: case FSTORE: case ASTORE:
            stackDepth = frame.operandStack.getAndIncrement();
            return new SimpleVariableUsage(frame.getStackEntry(stackDepth),
                    frame.getLocalVariable(inst.getLocalVarIndex()));
        case LSTORE: case DSTORE:
            stackDepth = frame.operandStack.getAndAdd(2);
            return new SimpleVariableUsage(
                    Arrays.asList((Variable)frame.getStackEntry(stackDepth), frame.getStackEntry(stackDepth+1)),
                    frame.getLocalVariable(inst.getLocalVarIndex()));
        case RET:
            final Set<Variable> emptySet = Collections.emptySet();
            return new SimpleVariableUsage(frame.getLocalVariable(inst.getLocalVarIndex()),
                    emptySet);
        default:
            assert false;
            return null;
        }
    }

    private VariableUsages simulateSimpleInsn(final Instance inst, final ExecutionFrame frame,
            final Stack<ExecutionFrame> allFrames) {
        switch (inst.getOpcode()) {
        case DUP:
            int stackHeight = frame.operandStack.decrementAndGet();
            return new SimpleVariableUsage(frame.getStackEntry(stackHeight-1), frame.getStackEntry(stackHeight));
        case DUP2:
            stackHeight = frame.operandStack.addAndGet(-2);
            return new SimpleVariableUsage(
                    Arrays.asList((Variable)frame.getStackEntry(stackHeight-2), frame.getStackEntry(stackHeight-1)),
                    Arrays.asList((Variable)frame.getStackEntry(stackHeight), frame.getStackEntry(stackHeight+1)));
        case DUP_X1:
            stackHeight = frame.operandStack.decrementAndGet();
            return new SimpleVariableUsage(
                    Arrays.asList((Variable)frame.getStackEntry(stackHeight-2), frame.getStackEntry(stackHeight-1)),
                    Arrays.asList((Variable)frame.getStackEntry(stackHeight-2), frame.getStackEntry(stackHeight-1),
                            frame.getStackEntry(stackHeight)));
        case DUP_X2:
            stackHeight = frame.operandStack.decrementAndGet();
            return new SimpleVariableUsage(
                    Arrays.asList((Variable)frame.getStackEntry(stackHeight-3), frame.getStackEntry(stackHeight-2),
                            frame.getStackEntry(stackHeight-1)),
                    Arrays.asList((Variable)frame.getStackEntry(stackHeight-3), frame.getStackEntry(stackHeight-2),
                            frame.getStackEntry(stackHeight-1), frame.getStackEntry(stackHeight)));
        case DUP2_X1:
            stackHeight = frame.operandStack.addAndGet(-2);
            return new SimpleVariableUsage(
                    Arrays.asList((Variable)frame.getStackEntry(stackHeight-3), frame.getStackEntry(stackHeight-2),
                            frame.getStackEntry(stackHeight-1)),
                    Arrays.asList((Variable)frame.getStackEntry(stackHeight-3), frame.getStackEntry(stackHeight-2),
                            frame.getStackEntry(stackHeight-1), frame.getStackEntry(stackHeight), frame.getStackEntry(stackHeight+1)));
        case DUP2_X2:
            stackHeight = frame.operandStack.addAndGet(-2);
            return new SimpleVariableUsage(
                    Arrays.asList((Variable)frame.getStackEntry(stackHeight-4), frame.getStackEntry(stackHeight-3),
                            frame.getStackEntry(stackHeight-2), frame.getStackEntry(stackHeight-1)),
                    Arrays.asList((Variable)frame.getStackEntry(stackHeight-4), frame.getStackEntry(stackHeight-3),
                            frame.getStackEntry(stackHeight-2), frame.getStackEntry(stackHeight-1),
                            frame.getStackEntry(stackHeight), frame.getStackEntry(stackHeight+1)));

        case IRETURN: case FRETURN: case ARETURN:
            if (frame.throwsException)
                frame.throwsException = false;
            ExecutionFrame lowerFrame = inst.getStackDepth() < 2 ? null : allFrames.get(inst.getStackDepth()-2);
            return new SimpleVariableUsage(frame.getStackEntry(frame.operandStack.getAndIncrement()),
                    lowerFrame == null ? VariableUsages.EMPTY_VARIABLE_SET :
                        Collections.singleton((Variable)lowerFrame.getStackEntry(lowerFrame.operandStack.decrementAndGet())));
        case DRETURN: case LRETURN:
            if (frame.throwsException)
                frame.throwsException = false;
            final int thisFrameStackHeight = frame.operandStack.getAndAdd(2);
            lowerFrame = inst.getStackDepth() < 2 ? null : allFrames.get(inst.getStackDepth()-2);
            final int lowerFrameStackHeight = lowerFrame.operandStack.addAndGet(-2);
            return new SimpleVariableUsage(Arrays.asList((Variable)frame.getStackEntry(thisFrameStackHeight),
                        frame.getStackEntry(thisFrameStackHeight+1)),
                    Arrays.asList((Variable)lowerFrame.getStackEntry(lowerFrameStackHeight),
                            lowerFrame.getStackEntry(lowerFrameStackHeight+1)));

        case NOP:
        case RETURN:
            if (frame.throwsException)
                frame.throwsException = false;
            return VariableUsages.EMPTY;

        case ACONST_NULL: case ICONST_M1: case ICONST_0: case ICONST_1: case ICONST_2: case ICONST_3:
        case ICONST_4: case ICONST_5: case FCONST_0: case FCONST_1: case FCONST_2:
            return stackManipulation(frame, 0, 1);

        case DCONST_0: case DCONST_1: case LCONST_0: case LCONST_1:
            return stackManipulation(frame, 0, 2);

        case ATHROW:
            // first search the frame where the exception is catched
            ExecutionFrame catchingFrame = null;
            for (int i = inst.getStackDepth()-2; i >= 0; --i) {
                final ExecutionFrame f = allFrames.get(i);
                if (f.atCacheBlockStart != null) {
                    catchingFrame = f;
                    break;
                }
            }
            return new SimpleVariableUsage(frame.getStackEntry(frame.operandStack.getAndIncrement()),
                    catchingFrame == null ? VariableUsages.EMPTY_VARIABLE_SET :
                        Collections.singleton((Variable)catchingFrame.getStackEntry(catchingFrame.operandStack.get())));

        case MONITORENTER: case MONITOREXIT:
        case POP:
            return stackManipulation(frame, 1, 0);

        case I2F: case F2I: case D2F: case I2B: case I2C: case I2S:
        case ARRAYLENGTH:
        case INEG: case FNEG:
            return stackManipulation(frame, 1, 1);

        case I2L: case I2D: case F2L: case F2D:
            return stackManipulation(frame, 1, 2);

        case POP2:
            return stackManipulation(frame, 2, 0);

        case L2I: case L2F: case D2I:
        case FCMPL: case FCMPG:
        case IADD: case FADD: case ISUB: case FSUB: case IMUL: case FMUL: case IDIV: case FDIV: case IREM:
        case FREM: case ISHL: case ISHR: case IUSHR: case IAND: case IOR: case IXOR:
            return stackManipulation(frame, 2, 1);

        case L2D: case D2L:
        case LNEG: case DNEG:
        case SWAP:
            return stackManipulation(frame, 2, 2);

        case LCMP: case DCMPL: case DCMPG:
            return stackManipulation(frame, 4, 1);

        case LADD: case DADD: case LSUB: case DSUB: case LMUL: case DMUL: case LDIV: case DDIV: case LREM:
        case DREM: case LSHL: case LSHR: case LUSHR: case LAND: case LOR: case LXOR:
            return stackManipulation(frame, 4, 2);

        default:
            assert false;
            return null;
        }
    }

    private VariableUsages stackManipulation(final ExecutionFrame frame, final int read, final int write) {
        final int oldStackSize = read == write ? frame.operandStack.get() : frame.operandStack.getAndAdd(read - write);
        final int newStackSize = oldStackSize + read - write;
        final Variable[] readVarsArr;
        final Variable[] writtenVarsArr;
        Collection<Variable> readVars;
        final Collection<Variable> writtenVars;
        if (read == 0) {
            readVars = Collections.emptySet();
        } else if (read == 1) {
            readVars = Collections.singleton((Variable)frame.getStackEntry(newStackSize-1));
        } else {
            readVarsArr = new Variable[read];
            for (int i = 0; i < read; ++i)
                readVarsArr[i] = frame.getStackEntry(newStackSize-i-1);
            readVars = Arrays.asList(readVarsArr);
        }
        if (write == read) {
            writtenVars = readVars;
        } else if (write == 0) {
            writtenVars = Collections.emptySet();
        } else if (write == 1) {
            writtenVars = Collections.singleton((Variable)frame.getStackEntry(oldStackSize-1));
        } else {
            writtenVarsArr = new Variable[write];
            for (int i = 0; i < write; ++i)
                writtenVarsArr[i] = frame.getStackEntry(oldStackSize-i-1);
            writtenVars = Arrays.asList(writtenVarsArr);
        }
        return new SimpleVariableUsage(readVars, writtenVars);
    }

}
