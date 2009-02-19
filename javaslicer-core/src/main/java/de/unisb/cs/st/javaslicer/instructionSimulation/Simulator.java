package de.unisb.cs.st.javaslicer.instructionSimulation;

import static org.objectweb.asm.Opcodes.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.ArrayStack;
import de.hammacher.util.IntHolder;
import de.hammacher.util.maps.LongMap;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Field;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.ArrayInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.FieldInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.IIncInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.JumpInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LdcInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MethodInvocationInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MultiANewArrayInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.VarInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.ArrayInstruction.ArrayInstrInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MultiANewArrayInstruction.MultiANewArrayInstrInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.NewArrayInstruction.NewArrayInstrInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.TypeInstruction.TypeInstrInstance;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.variables.ArrayElement;
import de.unisb.cs.st.javaslicer.variables.ObjectField;
import de.unisb.cs.st.javaslicer.variables.StackEntry;
import de.unisb.cs.st.javaslicer.variables.StackEntrySet;
import de.unisb.cs.st.javaslicer.variables.StaticField;
import de.unisb.cs.st.javaslicer.variables.Variable;

public class Simulator {

    // list of all fields corresponding to a class
    private final HashMap<String, String[]> fieldsCache = new HashMap<String, String[]>();

    // mapping from array identifier to the maximum element that has been accessed in that array
    private final LongMap<IntHolder> maxArrayElem = new LongMap<IntHolder>();

    private final TraceResult traceResult;

    public Simulator(final TraceResult traceResult) {
        this.traceResult = traceResult;
    }

    public DynamicInformation simulateInstruction(final InstructionInstance inst,
            final ExecutionFrame executionFrame, final ExecutionFrame removedFrame,
            final ArrayStack<ExecutionFrame> allFrames) {
        switch (inst.getInstruction().getType()) {
        case ARRAY:
            return simulateArrayInstruction((ArrayInstruction.ArrayInstrInstance)inst, executionFrame);
        case FIELD:
            return simulateFieldInstruction((FieldInstruction.FieldInstrInstance)inst, executionFrame);
        case IINC:
            Collection<Variable> vars = Collections.singleton((Variable)executionFrame.getLocalVariable(
                ((IIncInstruction)inst.getInstruction()).getLocalVarIndex()));
            return new SimpleVariableUsage(vars, vars);
        case INT:
            // write 1
            return new SimpleVariableUsage(Collections.<Variable>emptySet(), new StackEntrySet(executionFrame,
                    executionFrame.operandStack.decrementAndGet(), 1));
        case JUMP:
            return simulateJumpInsn((JumpInstruction)inst.getInstruction(), executionFrame);
        case LABEL:
            if (((LabelMarker)inst.getInstruction()).isCatchBlock()) {
                // at the catch block start, the reference to the exception is pushed onto the stack
                executionFrame.operandStack.decrementAndGet();
                return DynamicInformation.CATCHBLOCK;
            }
            return DynamicInformation.EMPTY;
        case LDC:
            // writes 1 or 2, but we only trace the lower variable
            final int stackOffset = (((LdcInstruction)inst.getInstruction()).constantIsLong())
                ? executionFrame.operandStack.addAndGet(-2)
                : executionFrame.operandStack.decrementAndGet();
            return new SimpleVariableUsage(Collections.<Variable>emptySet(), new StackEntrySet(executionFrame, stackOffset, 1));
        case LOOKUPSWITCH:
        case TABLESWITCH:
            return new SimpleVariableUsage(executionFrame.getStackEntry(executionFrame.operandStack.getAndIncrement()),
                    DynamicInformation.EMPTY_VARIABLE_SET);
        case METHODINVOCATION:
            return simulateMethodInsn((MethodInvocationInstruction)inst.getInstruction(),
                    executionFrame, removedFrame);
        case MULTIANEWARRAY:
            return simulateMultiANewArrayInsn((MultiANewArrayInstrInstance) inst, executionFrame);
        case NEWARRAY:
            return simulateNewarrayInsn((NewArrayInstrInstance) inst, executionFrame);
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

    private DynamicInformation simulateMultiANewArrayInsn(final MultiANewArrayInstrInstance inst,
            final ExecutionFrame executionFrame) {

        final LongMap<Collection<Variable>> createdObjects = new LongMap<Collection<Variable>>();
        for (final long createdObj: inst.getNewObjectIdentifiers()) {
            final IntHolder h = this.maxArrayElem.remove(createdObj);
            createdObjects.put(createdObj, new ArrayElementsList(
                    h == null ? 0 : (h.get()+1), createdObj));
        }

        return stackManipulation(executionFrame,
            ((MultiANewArrayInstruction)inst.getInstruction()).getDimension(), 1,
            createdObjects);
    }

    private DynamicInformation simulateNewarrayInsn(final NewArrayInstrInstance inst,
            final ExecutionFrame frame) {
        final IntHolder h = this.maxArrayElem.remove(inst.getNewObjectIdentifier());
        final StackEntry stackEntry = frame.getStackEntry(frame.operandStack.get()-1);
        final Collection<Variable> stackEntryColl = Collections.singleton((Variable)stackEntry);
        final Map<Long, Collection<Variable>> createdObjects =
            Collections.singletonMap(inst.getNewObjectIdentifier(),
                (Collection<Variable>)new ArrayElementsList(h == null ? 0 : (h.get()+1),
                                          inst.getNewObjectIdentifier()));
        return new SimpleVariableUsage(stackEntryColl, stackEntryColl, createdObjects);
    }

    private DynamicInformation simulateTypeInsn(final TypeInstrInstance inst, final ExecutionFrame frame) {
        switch (inst.getOpcode()) {
        case Opcodes.NEW:
            final Collection<Variable> definedVariables = Collections.singleton(
                (Variable)frame.getStackEntry(frame.operandStack.decrementAndGet()));
            return new SimpleVariableUsage(DynamicInformation.EMPTY_VARIABLE_SET,
                definedVariables,
                Collections.singletonMap(inst.getNewObjectIdentifier(),
                    getAllFields(inst.getInstruction().getJavaClassName(),
                        inst.getNewObjectIdentifier())));
        case Opcodes.ANEWARRAY:
            final int stackSize = frame.operandStack.get()-1;
            final IntHolder h = this.maxArrayElem.remove(inst.getNewObjectIdentifier());
            final Collection<Variable> stackEntryColl = Collections.singleton((Variable)frame.getStackEntry(stackSize));
            return new SimpleVariableUsage(stackEntryColl, stackEntryColl,
                Collections.singletonMap(inst.getNewObjectIdentifier(),
                    (Collection<Variable>)new ArrayElementsList(h == null ? 0 : (h.get()+1), inst.getNewObjectIdentifier())));
        case Opcodes.CHECKCAST:
            return new SimpleVariableUsage(frame.getStackEntry(frame.operandStack.get()-1), DynamicInformation.EMPTY_VARIABLE_SET);
        case Opcodes.INSTANCEOF:
            return stackManipulation(frame, 1, 1);
        default:
            assert false;
            return null;
        }
    }

    private Collection<Variable> getAllFields(final String className, final long objId) {
        String[] cachedFields = this.fieldsCache.get(className);
        if (cachedFields == null) {
            final HashSet<String> allFields = new HashSet<String>();
            String tmpClassName = className;
            while (tmpClassName != null) {
                final ReadClass clazz = this.traceResult.findReadClass(tmpClassName);
                if (clazz == null) {
                    //assert "java.lang.Object".equals(tmpClassName);
                    break;
                }
                for (final Field field: clazz.getFields())
                    allFields.add(field.getName());
                tmpClassName = clazz.getSuperClassName();
            }
            cachedFields = allFields.toArray(new String[allFields.size()]);
            this.fieldsCache.put(className, cachedFields);
        }
        return new ObjectFieldList(objId, cachedFields);
    }

    private DynamicInformation simulateJumpInsn(final JumpInstruction instruction, final ExecutionFrame frame) {
        switch (instruction.getOpcode()) {
        case IFEQ: case IFNE: case IFLT: case IFGE: case IFGT: case IFLE:
        case IFNULL: case IFNONNULL:
            // read 1 stack entry and compare it to zero / null
            return new SimpleVariableUsage(frame.getStackEntry(frame.operandStack.getAndIncrement()),
                DynamicInformation.EMPTY_VARIABLE_SET);

        case IF_ICMPEQ: case IF_ICMPNE: case IF_ICMPLT: case IF_ICMPGE:
        case IF_ICMPGT: case IF_ICMPLE: case IF_ACMPEQ: case IF_ACMPNE:
            // read two stack entries and compare them
            return new SimpleVariableUsage(new StackEntrySet(frame, frame.operandStack.getAndAdd(2), 2),
                DynamicInformation.EMPTY_VARIABLE_SET);

        case GOTO:
            return DynamicInformation.EMPTY;

        case JSR:
            // pushes the return address onto the stack
            // since this is no "data" (in our sense), we do not trace it.
            return DynamicInformation.EMPTY;

        default:
            assert false;
            return null;
        }
    }

    private DynamicInformation simulateArrayInstruction(final ArrayInstrInstance inst,
            final ExecutionFrame frame) {
        IntHolder h = this.maxArrayElem.get(inst.getArrayId());
        if (h == null)
            this.maxArrayElem.put(inst.getArrayId(), h = new IntHolder(inst.getArrayIndex()));
        else if (inst.getArrayIndex() > h.get())
            h.set(inst.getArrayIndex());

        switch (inst.getOpcode()) {
        case IALOAD: case FALOAD: case AALOAD: case BALOAD: case CALOAD: case SALOAD:
            // read 2, write 1
            int stackOffset = frame.operandStack.getAndIncrement()-1;
            Variable lowerVar = frame.getStackEntry(stackOffset);
            ArrayElement arrayElem = new ArrayElement(inst.getArrayId(), inst.getArrayIndex());
            return new SimpleVariableUsage(Arrays.asList(lowerVar, frame.getStackEntry(stackOffset+1),
                    arrayElem), lowerVar);
        case LALOAD: case DALOAD:
            // read 2, write 2 (but we only trace the lower written value)
            stackOffset = frame.operandStack.get()-2;
            arrayElem = new ArrayElement(inst.getArrayId(), inst.getArrayIndex());
            lowerVar = frame.getStackEntry(stackOffset);
            return new SimpleVariableUsage(Arrays.asList(lowerVar, frame.getStackEntry(stackOffset+1),
                    arrayElem), lowerVar);
        case IASTORE: case FASTORE: case AASTORE: case BASTORE: case CASTORE: case SASTORE:
            // read 3, write 0
            stackOffset = frame.operandStack.getAndAdd(3);
            arrayElem = new ArrayElement(inst.getArrayId(), inst.getArrayIndex());
            return new SimpleVariableUsage(new StackEntrySet(frame, stackOffset, 3),
                    arrayElem);
        case LASTORE: case DASTORE:
            // read 4 (but we only trace the lower 3), write 0
            stackOffset = frame.operandStack.getAndAdd(4);
            arrayElem = new ArrayElement(inst.getArrayId(), inst.getArrayIndex());
            return new SimpleVariableUsage(new StackEntrySet(frame, stackOffset, 3),
                    arrayElem);
        default:
            assert false;
            return null;
        }
    }

    private DynamicInformation simulateMethodInsn(final MethodInvocationInstruction inst,
            final ExecutionFrame executionFrame, final ExecutionFrame removedFrame) {
        int paramCount = inst.getOpcode() == INVOKESTATIC ? 0 : 1;
        for (int param = inst.getParameterCount()-1; param >= 0; --param)
            paramCount += inst.parameterIsLong(param) ? 2 : 1;
        final byte returnedSize = executionFrame.atCacheBlockStart == null ? inst.getReturnedSize() : 0;
        boolean hasReturn = returnedSize != 0;
        final boolean hasRemovedFrame = removedFrame != null
            && inst.getMethodName().equals(removedFrame.method.getName())
            && inst.getMethodDesc().equals(removedFrame.method.getDesc())
            && (!hasReturn || removedFrame.returnValue != null);
        final int parametersStackOffset = executionFrame.operandStack.getAndAdd(paramCount-returnedSize)-returnedSize;

        return new MethodInvokationVariableUsages(parametersStackOffset,
                paramCount, hasReturn, executionFrame, hasRemovedFrame ? removedFrame : null);
    }

    private DynamicInformation simulateFieldInstruction(final FieldInstruction.FieldInstrInstance instance, final ExecutionFrame frame) {
        int stackOffset;
        Variable lowerVar;
        final FieldInstruction instruction = (FieldInstruction) instance.getInstruction();
        switch (instruction.getOpcode()) {
        case GETFIELD:
            // read 1, write 1 or 2 (we only trace the lower one of 2)
            stackOffset = instruction.isLongValue()
                ? frame.operandStack.decrementAndGet()-1
                : frame.operandStack.get()-1;
            lowerVar = frame.getStackEntry(stackOffset);
            return new SimpleVariableUsage(Arrays.asList(lowerVar,
                        new ObjectField(instance.getObjectId(), instruction.getFieldName())),
                        lowerVar);
        case GETSTATIC:
            // read 0, write 1 or 2 (we only trace the lower one of 2)
            stackOffset = instruction.isLongValue()
                ? frame.operandStack.addAndGet(-2)
                : frame.operandStack.decrementAndGet();
            return new SimpleVariableUsage(new StaticField(instruction.getOwnerInternalClassName(), instruction.getFieldName()),
                    frame.getStackEntry(stackOffset));
        case PUTFIELD:
            // read 2 or 3 (only trace 2), write 0
            stackOffset = frame.operandStack.getAndAdd(instruction.isLongValue() ? 3 : 2);
            return new SimpleVariableUsage(new StackEntrySet(frame, stackOffset, 2),
                    new ObjectField(instance.getObjectId(), instruction.getFieldName()));
        case PUTSTATIC:
            // read 1 or 2 (only trace 1), write 0
            stackOffset = instruction.isLongValue()
                ? frame.operandStack.getAndAdd(2)
                : frame.operandStack.getAndIncrement();
            return new SimpleVariableUsage(frame.getStackEntry(stackOffset),
                    new StaticField(instruction.getOwnerInternalClassName(), instruction.getFieldName()));
        default:
            assert false;
            return null;
        }
    }

    private DynamicInformation simulateVarInstruction(final VarInstruction inst, final ExecutionFrame frame) {
        switch (inst.getOpcode()) {
        case ILOAD: case FLOAD: case ALOAD:
            // read 0, write 1 stack entry
            int stackOffset = frame.operandStack.decrementAndGet();
            return new SimpleVariableUsage(frame.getLocalVariable(inst.getLocalVarIndex()),
                    new StackEntrySet(frame, stackOffset, 1));
        case LLOAD: case DLOAD:
            // read 0, write 2 stack entries (but we only trace the lower one)
            stackOffset = frame.operandStack.addAndGet(-2);
            return new SimpleVariableUsage(frame.getLocalVariable(inst.getLocalVarIndex()),
                    new StackEntrySet(frame, stackOffset, 1));
        case ISTORE: case FSTORE: case ASTORE:
            // read 1, write 0
            stackOffset = frame.operandStack.getAndIncrement();
            return new SimpleVariableUsage(new StackEntrySet(frame, stackOffset, 1),
                    frame.getLocalVariable(inst.getLocalVarIndex()));
        case LSTORE: case DSTORE:
            // read 2 (but only trace 1), write 0
            stackOffset = frame.operandStack.getAndAdd(2);
            return new SimpleVariableUsage(new StackEntrySet(frame, stackOffset, 1),
                    frame.getLocalVariable(inst.getLocalVarIndex()));
        case RET:
            // RET reads a local variable, but since this is no "data" (in our sense), we
            // do not trace that
            return DynamicInformation.EMPTY;

        default:
            assert false;
            return null;
        }
    }

    private DynamicInformation simulateSimpleInsn(final InstructionInstance inst, final ExecutionFrame frame,
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
            frame.throwsException = false;
            assert frame.returnValue == null;
            StackEntry ret = frame.getStackEntry(frame.operandStack.getAndIncrement());
            frame.returnValue = ret;
            ExecutionFrame lowerFrame = null;
            if (inst.getStackDepth() >= 2) {
                lowerFrame = allFrames.get(inst.getStackDepth()-2);
                Instruction prev = lowerFrame.lastInstruction != null ? lowerFrame.lastInstruction.getPrevious() : null;
                if (prev != null && prev instanceof MethodInvocationInstruction) {
                    MethodInvocationInstruction m = (MethodInvocationInstruction) prev;
                    if (!m.getMethodName().equals(frame.method.getName()) || !m.getMethodDesc().equals(frame.method.getDesc())) {
                        lowerFrame = null;
                    }
                }
            }

            // it is sufficient to trace the lower variable of the double-sized value (long or double)
            return new SimpleVariableUsage(ret,
                    lowerFrame == null ? DynamicInformation.EMPTY_VARIABLE_SET :
                        Arrays.asList((Variable)lowerFrame.getStackEntry(lowerFrame.operandStack.get()-1)));
        case DRETURN: case LRETURN:
            frame.throwsException = false;
            assert frame.returnValue == null;
            ret = frame.getStackEntry(frame.operandStack.getAndAdd(2));
            frame.returnValue = ret;
            lowerFrame = null;
            if (inst.getStackDepth() >= 2) {
                lowerFrame = allFrames.get(inst.getStackDepth()-2);
                Instruction prev = lowerFrame.lastInstruction != null ? lowerFrame.lastInstruction.getPrevious() : null;
                if (prev != null && prev instanceof MethodInvocationInstruction) {
                    MethodInvocationInstruction m = (MethodInvocationInstruction) prev;
                    if (!m.getMethodName().equals(frame.method.getName()) || !m.getMethodDesc().equals(frame.method.getDesc())) {
                        lowerFrame = null;
                    }
                }
            }

            // it is sufficient to trace the lower variable of the double-sized value (long or double)
            return new SimpleVariableUsage(ret,
                    lowerFrame == null ? DynamicInformation.EMPTY_VARIABLE_SET :
                        Arrays.asList((Variable)lowerFrame.getStackEntry(lowerFrame.operandStack.get()-2)));

        case RETURN:
            assert frame.returnValue == null;
            frame.throwsException = false;
            return DynamicInformation.EMPTY;

        case NOP:
            return DynamicInformation.EMPTY;

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
                    catchingFrame == null ? DynamicInformation.EMPTY_VARIABLE_SET :
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

    private DynamicInformation stackManipulation(final ExecutionFrame frame, final int read,
            final int write) {
        final Map<Long, Collection<Variable>> createdObjects = Collections.emptyMap();
        return stackManipulation(frame, read, write, createdObjects);
    }

    private DynamicInformation stackManipulation(final ExecutionFrame frame, final int read,
            final int write, final Map<Long, Collection<Variable>> createdObjects) {
        final int stackOffset = frame.operandStack.getAndAdd(read - write) - write;
        return new StackManipulation(frame, read, write, stackOffset, createdObjects);
    }

}
