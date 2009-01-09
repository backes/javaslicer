package de.unisb.cs.st.javaslicer.instructionSimulation;

import static org.objectweb.asm.Opcodes.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.ArrayStack;
import de.hammacher.util.ListUtils;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.ArrayInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.FieldInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.IIncInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.JumpInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LdcInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MethodInvocationInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MultiANewArrayInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.VarInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MultiANewArrayInstruction.MultiANewArrayInstrInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.TypeInstruction.TypeInstrInstance;
import de.unisb.cs.st.javaslicer.dependencyAnalysis.ExecutionFrame;
import de.unisb.cs.st.javaslicer.variableUsages.MethodInvokationVariableUsages;
import de.unisb.cs.st.javaslicer.variableUsages.SimpleVariableUsage;
import de.unisb.cs.st.javaslicer.variableUsages.StackManipulation;
import de.unisb.cs.st.javaslicer.variableUsages.VariableUsages;
import de.unisb.cs.st.javaslicer.variables.ArrayElement;
import de.unisb.cs.st.javaslicer.variables.ObjectField;
import de.unisb.cs.st.javaslicer.variables.StackEntrySet;
import de.unisb.cs.st.javaslicer.variables.Variable;

public class Simulator {

    public VariableUsages simulateInstruction(final Instance inst,
            final ExecutionFrame executionFrame, final ExecutionFrame removedFrame,
            final ArrayStack<ExecutionFrame> allFrames) {
        final Variable var;
        Collection<Variable> vars;
        switch (inst.getInstruction().getType()) {
        case ARRAY:
            return simulateArrayInstruction((ArrayInstruction.ArrayInstrInstance)inst, executionFrame);
        case FIELD:
            return simulateFieldInstruction((FieldInstruction.FieldInstrInstance)inst, executionFrame);
        case IINC:
            var = executionFrame.getLocalVariable(((IIncInstruction)inst.getInstruction()).getLocalVarIndex());
            vars = Collections.singleton(var);
            return new SimpleVariableUsage(vars, vars);
        case INT:
            vars = Collections.emptySet();
            return new SimpleVariableUsage(vars, new StackEntrySet(executionFrame,
                    executionFrame.operandStack.getAndDecrement(), 1));
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
                final int stackHeight = executionFrame.operandStack.getAndAdd(-2);
                return new SimpleVariableUsage(vars, new StackEntrySet(executionFrame, stackHeight, 2));
            }
            return new SimpleVariableUsage(vars, new StackEntrySet(executionFrame, executionFrame.operandStack.getAndDecrement(), 1));
        case LOOKUPSWITCH:
        case TABLESWITCH:
            return new SimpleVariableUsage(executionFrame.getStackEntry(executionFrame.operandStack.getAndIncrement()),
                    VariableUsages.EMPTY_VARIABLE_SET);
        case METHODINVOCATION:
            return simulateMethodInsn((MethodInvocationInstruction)inst.getInstruction(),
                    executionFrame, removedFrame);
        case MULTIANEWARRAY:
            return stackManipulation(executionFrame,
                ((MultiANewArrayInstruction)inst.getInstruction()).getDimension(), 1,
                ListUtils.longArrayAsList(((MultiANewArrayInstrInstance)inst).getNewObjectIdentifiers()));
        case NEWARRAY:
            return stackManipulation(executionFrame, 1, 1);
        case SIMPLE:
            return simulateSimpleInsn(inst, executionFrame, allFrames);
        case TYPE:
            return simulateTypeInsn((TypeInstrInstance)inst, executionFrame);
        case VAR:
            return simulateVarInstruction((VarInstruction) inst.getInstruction(), executionFrame);
        default:
            assert false;
            return null;
        }
    }

    private VariableUsages simulateTypeInsn(final TypeInstrInstance inst, final ExecutionFrame frame) {
        switch (inst.getOpcode()) {
        case Opcodes.NEW:
            return new SimpleVariableUsage(VariableUsages.EMPTY_VARIABLE_SET,
                Collections.singleton((Variable)frame.getStackEntry(frame.operandStack.decrementAndGet())),
                Collections.singleton(inst.getNewObjectIdentifier()));
        case Opcodes.ANEWARRAY:
            final int stackSize = frame.operandStack.get()-1;
            return new SimpleVariableUsage(
                Collections.singleton((Variable)frame.getStackEntry(stackSize)),
                Collections.singleton((Variable)frame.getStackEntry(stackSize)),
                Collections.singleton(inst.getNewObjectIdentifier()));
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
            final int oldSize = frame.operandStack.addAndGet(2);
            return new SimpleVariableUsage(new StackEntrySet(frame, oldSize, 2),
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

    private VariableUsages simulateArrayInstruction(final ArrayInstruction.ArrayInstrInstance inst, final ExecutionFrame frame) {
        switch (inst.getOpcode()) {
        case IALOAD: case FALOAD: case AALOAD: case BALOAD: case CALOAD: case SALOAD:
            int stackDepth = frame.operandStack.getAndIncrement();
            ArrayElement arrayElem = new ArrayElement(inst.getArrayId(), inst.getArrayIndex());
            return new SimpleVariableUsage(Arrays.asList(frame.getStackEntry(stackDepth-1), frame.getStackEntry(stackDepth),
                    arrayElem), new StackEntrySet(frame, stackDepth, 1));
        case LALOAD: case DALOAD:
            stackDepth = frame.operandStack.get();
            arrayElem = new ArrayElement(inst.getArrayId(), inst.getArrayIndex());
            return new SimpleVariableUsage(Arrays.asList(frame.getStackEntry(stackDepth-2), frame.getStackEntry(stackDepth-1),
                    arrayElem), new StackEntrySet(frame, stackDepth, 2));
        case IASTORE: case FASTORE: case AASTORE: case BASTORE: case CASTORE: case SASTORE:
            stackDepth = frame.operandStack.addAndGet(3);
            arrayElem = new ArrayElement(inst.getArrayId(), inst.getArrayIndex());
            return new SimpleVariableUsage(new StackEntrySet(frame, stackDepth, 3),
                    arrayElem);
        case LASTORE: case DASTORE:
            stackDepth = frame.operandStack.addAndGet(4);
            arrayElem = new ArrayElement(inst.getArrayId(), inst.getArrayIndex());
            return new SimpleVariableUsage(new StackEntrySet(frame, stackDepth, 4),
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

    private VariableUsages simulateFieldInstruction(final FieldInstruction.FieldInstrInstance instance, final ExecutionFrame frame) {
        int stackDepth;
        final FieldInstruction instruction = (FieldInstruction) instance.getInstruction();
        switch (instruction.getOpcode()) {
        case GETFIELD:
            if (instruction.isLongValue()) {
                stackDepth = frame.operandStack.decrementAndGet();
                return new SimpleVariableUsage(Arrays.asList(frame.getStackEntry(stackDepth-1),
                            new ObjectField(instance.getObjectId(), instruction.getFieldName())),
                            new StackEntrySet(frame, stackDepth+1, 2));
            }
            stackDepth = frame.operandStack.get();
            return new SimpleVariableUsage(Arrays.asList(frame.getStackEntry(stackDepth-1),
                        new ObjectField(instance.getObjectId(), instruction.getFieldName())),
                        new StackEntrySet(frame, stackDepth, 1));
        case GETSTATIC:
            if (instruction.isLongValue()) {
                stackDepth = frame.operandStack.getAndAdd(-2);
                return new SimpleVariableUsage(new ObjectField(-1, instruction.getFieldName()),
                        new StackEntrySet(frame, stackDepth, 2));
            }
            stackDepth = frame.operandStack.getAndDecrement();
            return new SimpleVariableUsage(new ObjectField(-1, instruction.getFieldName()),
                    new StackEntrySet(frame, stackDepth, 1));
        case PUTFIELD:
            if (instruction.isLongValue()) {
                stackDepth = frame.operandStack.addAndGet(3);
                return new SimpleVariableUsage(new StackEntrySet(frame, stackDepth, 3),
                        new ObjectField(instance.getObjectId(), instruction.getFieldName()));
            }
            stackDepth = frame.operandStack.addAndGet(2);
            return new SimpleVariableUsage(new StackEntrySet(frame, stackDepth, 2),
                    new ObjectField(instance.getObjectId(), instruction.getFieldName()));
        case PUTSTATIC:
            if (instruction.isLongValue()) {
                stackDepth = frame.operandStack.addAndGet(2);
                return new SimpleVariableUsage(new StackEntrySet(frame, stackDepth, 2),
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
                    new StackEntrySet(frame, stackDepth+1, 1));
        case LLOAD: case DLOAD:
            stackDepth = frame.operandStack.addAndGet(-2);
            return new SimpleVariableUsage(frame.getLocalVariable(inst.getLocalVarIndex()),
                    new StackEntrySet(frame, stackDepth+2, 2));
        case ISTORE: case FSTORE: case ASTORE:
            stackDepth = frame.operandStack.getAndIncrement();
            return new SimpleVariableUsage(new StackEntrySet(frame, stackDepth+1, 1),
                    frame.getLocalVariable(inst.getLocalVarIndex()));
        case LSTORE: case DSTORE:
            stackDepth = frame.operandStack.getAndAdd(2);
            return new SimpleVariableUsage(
                    new StackEntrySet(frame, stackDepth+2, 2),
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
            final ArrayStack<ExecutionFrame> allFrames) {
        switch (inst.getInstruction().getOpcode()) {
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
            final int lowerFrameStackHeight = lowerFrame == null ? 0 : lowerFrame.operandStack.addAndGet(-2);
            return new SimpleVariableUsage(Arrays.asList((Variable)frame.getStackEntry(thisFrameStackHeight),
                        frame.getStackEntry(thisFrameStackHeight+1)),
                    lowerFrame == null ? VariableUsages.EMPTY_VARIABLE_SET :
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

    private VariableUsages stackManipulation(final ExecutionFrame frame, final int read,
            final int write) {
        Collection<Long> createdObjects = Collections.emptySet();
        return stackManipulation(frame, read, write, createdObjects);
    }

    private VariableUsages stackManipulation(final ExecutionFrame frame, final int read,
            final int write, Collection<Long> createdObjects) {
        final int oldStackSize = read == write ? frame.operandStack.get() : frame.operandStack.getAndAdd(read - write);
        return new StackManipulation(frame, read, write, oldStackSize, createdObjects);
    }

}
