/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.instructionSimulation
 *    Class:     Simulator
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/instructionSimulation/Simulator.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.javaslicer.instructionSimulation;

import static org.objectweb.asm.Opcodes.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.IntHolder;
import de.hammacher.util.maps.LongMap;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Field;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.ArrayInstruction.ArrayInstrInstanceInfo;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.FieldInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.FieldInstruction.FieldInstrInstanceInfo;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.IIncInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.JumpInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LdcInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MethodInvocationInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MultiANewArrayInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MultiANewArrayInstruction.MultiANewArrayInstrInstanceInfo;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.NewArrayInstruction.NewArrayInstrInstanceInfo;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.TypeInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.TypeInstruction.TypeInstrInstanceInfo;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.VarInstruction;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.variables.ArrayElement;
import de.unisb.cs.st.javaslicer.variables.LocalVariable;
import de.unisb.cs.st.javaslicer.variables.ObjectField;
import de.unisb.cs.st.javaslicer.variables.StackEntry;
import de.unisb.cs.st.javaslicer.variables.StaticField;
import de.unisb.cs.st.javaslicer.variables.Variable;

public class Simulator<InstanceType extends InstructionInstance> {

    // list of all fields corresponding to a class
    private final HashMap<String, String[]> fieldsCache = new HashMap<String, String[]>();

    // mapping from array identifier to the maximum element that has been accessed in that array
    private final LongMap<IntHolder> maxArrayElem = new LongMap<IntHolder>();

    private final TraceResult traceResult;

    public Simulator(TraceResult traceResult) {
        this.traceResult = traceResult;
    }

    public DynamicInformation simulateInstruction(InstructionInstance inst,
            SimulationEnvironment simulationEnvironment) {
        switch (inst.getInstruction().getType()) {
        case ARRAY:
            return simulateArrayInstruction(inst, simulationEnvironment);
        case FIELD:
            return simulateFieldInstruction(inst, simulationEnvironment);
        case IINC:
            Set<LocalVariable> vars = Collections.singleton(simulationEnvironment.getLocalVariable(
            	inst.getStackDepth(), ((IIncInstruction)inst.getInstruction()).getLocalVarIndex()));
            return new SimpleVariableUsage(vars, vars);
        case INT:
            // write 1
            return new SimpleVariableUsage(Collections.<Variable>emptySet(),
            	simulationEnvironment.getOpStackEntry(inst.getStackDepth(),
            		simulationEnvironment.decAndGetOpStack(inst.getStackDepth())));
        case JUMP:
            return simulateJumpInsn((JumpInstruction)inst.getInstruction(), inst.getStackDepth(), simulationEnvironment);
        case LABEL:
            if (((LabelMarker)inst.getInstruction()).isCatchBlock()) {
                // at the catch block start, the reference to the exception is pushed onto the stack
                simulationEnvironment.decAndGetOpStack(inst.getStackDepth());
                return DynamicInformation.CATCHBLOCK;
            }
            return DynamicInformation.EMPTY;
        case LDC:
            // writes 1 or 2, but we only trace the lower variable
            int stackOffset = simulationEnvironment.subAndGetOpStack(inst.getStackDepth(),
            	((LdcInstruction)inst.getInstruction()).constantIsLong() ? 2 : 1);
            return new SimpleVariableUsage(Collections.<Variable>emptySet(), simulationEnvironment.getOpStackEntry(inst.getStackDepth(), stackOffset));
        case LOOKUPSWITCH:
        case TABLESWITCH:
            return new SimpleVariableUsage(simulationEnvironment.getOpStackEntry(inst.getStackDepth(), simulationEnvironment.getAndIncOpStack(inst.getStackDepth())),
                    DynamicInformation.EMPTY_VARIABLE_SET);
        case METHODINVOCATION:
            return simulateMethodInsn(inst, simulationEnvironment);
        case MULTIANEWARRAY:
            return simulateMultiANewArrayInsn(inst, simulationEnvironment);
        case NEWARRAY:
            return simulateNewarrayInsn(inst, simulationEnvironment);
        case SIMPLE:
            return simulateSimpleInsn(inst, simulationEnvironment);
        case TYPE:
            return simulateTypeInsn(inst, simulationEnvironment);
        case VAR:
            return simulateVarInstruction(inst, simulationEnvironment);
        default:
            assert false;
            return null;
        }
    }

    private DynamicInformation simulateMultiANewArrayInsn(InstructionInstance inst,
            SimulationEnvironment simulationEnvironment) {
        assert inst.getInstruction().getType() == InstructionType.MULTIANEWARRAY;
        MultiANewArrayInstrInstanceInfo info = (MultiANewArrayInstrInstanceInfo) inst.getAdditionalInfo();

        LongMap<Collection<? extends Variable>> createdObjects = new LongMap<Collection<? extends Variable>>();
        for (long createdObj: info.getNewObjectIdentifiers()) {
            IntHolder h = this.maxArrayElem.remove(createdObj);
            createdObjects.put(createdObj, new ArrayElementsList(
                    h == null ? 0 : (h.get()+1), createdObj));
        }

        return stackManipulation(simulationEnvironment, inst.getStackDepth(),
            ((MultiANewArrayInstruction)inst.getInstruction()).getDimension(), 1,
            createdObjects);
    }

    private DynamicInformation simulateNewarrayInsn(InstructionInstance inst,
            SimulationEnvironment simulationEnvironment) {
        assert inst.getInstruction().getType() == InstructionType.NEWARRAY;
        NewArrayInstrInstanceInfo info = (NewArrayInstrInstanceInfo) inst.getAdditionalInfo();
        IntHolder h = this.maxArrayElem.remove(info.getNewObjectIdentifier());
        StackEntry stackEntry = simulationEnvironment.getOpStackEntry(inst.getStackDepth(),
        	simulationEnvironment.getOpStack(inst.getStackDepth()) - 1);
        Collection<Variable> stackEntryColl = Collections.singleton((Variable)stackEntry);
        Map<Long, Collection<? extends Variable>> createdObjects =
            Collections.<Long, Collection<? extends Variable>>singletonMap(info.getNewObjectIdentifier(),
                new ArrayElementsList(h == null ? 0 : (h.get()+1),
                                          info.getNewObjectIdentifier()));
        return new SimpleVariableUsage(stackEntryColl, stackEntryColl, createdObjects);
    }

    private DynamicInformation simulateTypeInsn(InstructionInstance inst, SimulationEnvironment simEnv) {
        assert inst.getInstruction().getType() == InstructionType.TYPE;
        TypeInstrInstanceInfo info = (TypeInstrInstanceInfo) inst.getAdditionalInfo();
        int stackDepth = inst.getStackDepth();
        switch (inst.getInstruction().getOpcode()) {
        case Opcodes.NEW:
            return new SimpleVariableUsage(DynamicInformation.EMPTY_VARIABLE_SET,
            	Collections.<Variable>singleton(simEnv.getOpStackEntry(stackDepth, simEnv.decAndGetOpStack(stackDepth))),
                Collections.<Long, Collection<? extends Variable>>singletonMap(info.getNewObjectIdentifier(),
                    getAllFields(((TypeInstruction)inst.getInstruction()).getJavaClassName(),
                        info.getNewObjectIdentifier())));
        case Opcodes.ANEWARRAY:
            int stackSize = simEnv.getOpStack(stackDepth)-1;
            IntHolder h = this.maxArrayElem.remove(info.getNewObjectIdentifier());
            Collection<Variable> stackEntryColl = Collections.<Variable>singleton(simEnv.getOpStackEntry(stackDepth, stackSize));
            return new SimpleVariableUsage(stackEntryColl, stackEntryColl,
                Collections.<Long, Collection<? extends Variable>>singletonMap(info.getNewObjectIdentifier(),
                    new ArrayElementsList(h == null ? 0 : (h.get()+1), info.getNewObjectIdentifier())));
        case Opcodes.CHECKCAST:
            return new SimpleVariableUsage(simEnv.getOpStackEntry(stackDepth, simEnv.getOpStack(stackDepth)-1), DynamicInformation.EMPTY_VARIABLE_SET);
        case Opcodes.INSTANCEOF:
            return stackManipulation(simEnv, stackDepth, 1, 1);
        default:
            assert false;
            return null;
        }
    }

    private Collection<ObjectField> getAllFields(String className, long objId) {
        String[] cachedFields = this.fieldsCache.get(className);
        if (cachedFields == null) {
            HashSet<String> allFields = new HashSet<String>();
            String tmpClassName = className;
            while (tmpClassName != null) {
                ReadClass clazz = this.traceResult.findReadClass(tmpClassName);
                if (clazz == null) {
                    //assert "java.lang.Object".equals(tmpClassName);
                    break;
                }
                for (Field field: clazz.getFields())
                    allFields.add(field.getName());
                tmpClassName = clazz.getSuperClassName();
            }
            cachedFields = allFields.toArray(new String[allFields.size()]);
            this.fieldsCache.put(className, cachedFields);
        }
        return new ObjectFieldList(objId, cachedFields);
    }

    private DynamicInformation simulateJumpInsn(JumpInstruction inst, int stackDepth, SimulationEnvironment simulationEnvironment) {
        switch (inst.getOpcode()) {
        case IFEQ: case IFNE: case IFLT: case IFGE: case IFGT: case IFLE:
        case IFNULL: case IFNONNULL:
            // read 1 stack entry and compare it to zero / null
            return new ReadSingleValueVariableUsage(simulationEnvironment.getOpStackEntry(stackDepth, simulationEnvironment.getAndIncOpStack(stackDepth)));

        case IF_ICMPEQ: case IF_ICMPNE: case IF_ICMPLT: case IF_ICMPGE:
        case IF_ICMPGT: case IF_ICMPLE: case IF_ACMPEQ: case IF_ACMPNE:
            // read two stack entries and compare them
            return new SimpleVariableUsage(simulationEnvironment.getOpStackEntries(stackDepth, simulationEnvironment.getAndAddOpStack(stackDepth, 2), 2),
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

    private DynamicInformation simulateArrayInstruction(InstructionInstance inst,
            SimulationEnvironment simulationEnvironment) {
        assert inst.getInstruction().getType() == InstructionType.ARRAY;
        ArrayInstrInstanceInfo arrInfo = (ArrayInstrInstanceInfo) inst.getAdditionalInfo();
        long arrayId = arrInfo.getArrayId();
        int arrayIndex = arrInfo.getArrayIndex();
        IntHolder h = this.maxArrayElem.get(arrayId);
        if (h == null)
            this.maxArrayElem.put(arrayId, h = new IntHolder(arrayIndex));
        else if (arrayIndex > h.get())
            h.set(arrayIndex);

        int stackDepth = inst.getStackDepth();

        switch (inst.getInstruction().getOpcode()) {
        case IALOAD: case FALOAD: case AALOAD: case BALOAD: case CALOAD: case SALOAD:
            // read 2, write 1
            int stackOffset = simulationEnvironment.getAndIncOpStack(stackDepth)-1;
            Variable lowerVar = simulationEnvironment.getOpStackEntry(stackDepth, stackOffset);
            ArrayElement arrayElem = new ArrayElement(arrayId, arrayIndex);
            return new SimpleVariableUsage(Arrays.asList(lowerVar, simulationEnvironment.getOpStackEntry(stackDepth, stackOffset+1),
                    arrayElem), lowerVar);
        case LALOAD: case DALOAD:
            // read 2, write 2 (but we only trace the lower written value)
            stackOffset = simulationEnvironment.getOpStack(stackDepth)-2;
            arrayElem = new ArrayElement(arrayId, arrayIndex);
            lowerVar = simulationEnvironment.getOpStackEntry(stackDepth, stackOffset);
            return new SimpleVariableUsage(Arrays.asList(lowerVar, simulationEnvironment.getOpStackEntry(stackDepth, stackOffset+1),
                    arrayElem), lowerVar);
        case IASTORE: case FASTORE: case AASTORE: case BASTORE: case CASTORE: case SASTORE:
            // read 3, write 0
            stackOffset = simulationEnvironment.getAndAddOpStack(stackDepth, 3);
            arrayElem = new ArrayElement(arrayId, arrayIndex);
            return new SimpleVariableUsage(simulationEnvironment.getOpStackEntries(stackDepth, stackOffset, 3),
                    arrayElem);
        case LASTORE: case DASTORE:
            // read 4 (but we only trace the lower 3), write 0
            stackOffset = simulationEnvironment.getAndAddOpStack(stackDepth, 4);
            arrayElem = new ArrayElement(arrayId, arrayIndex);
            return new SimpleVariableUsage(simulationEnvironment.getOpStackEntries(stackDepth, stackOffset, 3),
                    arrayElem);
        default:
            assert false;
            return null;
        }
    }

    private DynamicInformation simulateMethodInsn(InstructionInstance inst,
            SimulationEnvironment simEnv) {
    	MethodInvocationInstruction instr = (MethodInvocationInstruction)inst.getInstruction();
        int paramCount = instr.getOpcode() == INVOKESTATIC ? 0 : 1;
        for (int param = instr.getParameterCount()-1; param >= 0; --param)
            paramCount += instr.parameterIsLong(param) ? 2 : 1;
        boolean removedFrameMatches = simEnv.removedMethod != null
            && instr.getInvokedMethodName().equals(simEnv.removedMethod.getName())
            && instr.getInvokedMethodDesc().equals(simEnv.removedMethod.getDesc());
        // if we threw an exception, then we didn't produce a value on the stack
        int stackDepth = inst.getStackDepth();
        byte returnedSize = simEnv.throwsException[stackDepth] ? 0 : instr.getReturnedSize();
        boolean hasReturn = returnedSize != 0;
        int parametersStackOffset = (paramCount == returnedSize
            ? simEnv.getOpStack(stackDepth)
            : simEnv.getAndAddOpStack(stackDepth, paramCount-returnedSize)) - returnedSize;

        // we have to handle two special cases:
        // 1. this very instruction threw an exception (NPE because arg0 was null)
        // 2. the called method threw an exception, but we do not know where since it was not traced

        // unfortunately we cannot destinguish these cases...
        // we overapproximate by taking all cases where we don't have a removedFrame, and we know that we throw an exception
        // TODO do we actually have to do anything in these cases??

        return new MethodInvokationVariableUsages<InstanceType>(simEnv, stackDepth, parametersStackOffset,
                paramCount, hasReturn, removedFrameMatches);
    }

    private DynamicInformation simulateFieldInstruction(InstructionInstance instance, SimulationEnvironment simulationEnvironment) {
        assert instance.getInstruction().getType() == InstructionType.FIELD;
        FieldInstrInstanceInfo info = (FieldInstrInstanceInfo) instance.getAdditionalInfo();
        int stackOffset;
        Variable lowerVar;
        FieldInstruction instruction = (FieldInstruction) instance.getInstruction();
        int stackDepth = instance.getStackDepth();
        switch (instruction.getOpcode()) {
        case GETFIELD:
        	assert ((info.getObjectId() == 0) == (simulationEnvironment.throwsException[stackDepth]));
            // read 1, write 1 or 2 (we only trace the lower one of 2), or write 0 on exception
            stackOffset = simulationEnvironment.throwsException[stackDepth]
            	? simulationEnvironment.getAndIncOpStack(stackDepth)
                : instruction.isLongValue()
                	? simulationEnvironment.decAndGetOpStack(stackDepth)-1
                	: simulationEnvironment.getOpStack(stackDepth)-1;
            lowerVar = simulationEnvironment.getOpStackEntry(stackDepth, stackOffset);
            if (simulationEnvironment.throwsException[stackDepth]) {
            	return new ReadSingleValueVariableUsage(lowerVar);
            }
            return new SimpleVariableUsage(Arrays.asList(lowerVar,
                    new ObjectField(info.getObjectId(), instruction.getFieldName())), lowerVar);
        case GETSTATIC:
            // read 0, write 1 or 2 (we only trace the lower one of 2)
            stackOffset = instruction.isLongValue()
                ? simulationEnvironment.subAndGetOpStack(stackDepth, 2)
                : simulationEnvironment.decAndGetOpStack(stackDepth);
            return new SimpleVariableUsage(new StaticField(instruction.getOwnerInternalClassName(), instruction.getFieldName()),
                    simulationEnvironment.getOpStackEntry(stackDepth, stackOffset));
        case PUTFIELD:
            // read 2 or 3 (only trace 2), write 0
            stackOffset = simulationEnvironment.getAndAddOpStack(stackDepth, instruction.isLongValue() ? 3 : 2);
        	// if we threw an instruction, then we did not write to the object field
        	assert ((info.getObjectId() == 0) == (simulationEnvironment.throwsException[stackDepth]));
            if (simulationEnvironment.throwsException[stackDepth]) {
            	// on an exception, we only read the object reference
            	return new ReadSingleValueVariableUsage(simulationEnvironment.getOpStackEntry(stackDepth, stackOffset));
            }
            return new SimpleVariableUsage(simulationEnvironment.getOpStackEntries(stackDepth, stackOffset, 2),
            	new ObjectField(info.getObjectId(), instruction.getFieldName()));
        case PUTSTATIC:
            // read 1 or 2 (only trace 1), write 0
            stackOffset = instruction.isLongValue()
                ? simulationEnvironment.getAndAddOpStack(stackDepth, 2)
                : simulationEnvironment.getAndIncOpStack(stackDepth);
            return new SimpleVariableUsage(simulationEnvironment.getOpStackEntry(stackDepth, stackOffset),
                    new StaticField(instruction.getOwnerInternalClassName(), instruction.getFieldName()));
        default:
            assert false;
            return null;
        }
    }

    private DynamicInformation simulateVarInstruction(InstructionInstance inst, SimulationEnvironment simEnv) {
    	int stackDepth = inst.getStackDepth();
    	VarInstruction instr = (VarInstruction) inst.getInstruction();
        switch (inst.getInstruction().getOpcode()) {
        case ILOAD: case FLOAD: case ALOAD:
            // read 0, write 1 stack entry
            int stackOffset = simEnv.decAndGetOpStack(stackDepth);
            return new SimpleVariableUsage(simEnv.getLocalVariable(stackDepth, instr.getLocalVarIndex()),
                    Collections.singleton(simEnv.getOpStackEntry(stackDepth, stackOffset)));
        case LLOAD: case DLOAD:
            // read 0, write 2 stack entries (but we only trace the lower one)
            stackOffset = simEnv.subAndGetOpStack(stackDepth, 2);
            return new SimpleVariableUsage(simEnv.getLocalVariable(stackDepth, instr.getLocalVarIndex()),
                Collections.singleton(simEnv.getOpStackEntry(stackDepth, stackOffset)));
        case ISTORE: case FSTORE: case ASTORE:
            // read 1, write 0
            stackOffset = simEnv.getAndIncOpStack(stackDepth);
            return new SimpleVariableUsage(Collections.singleton(simEnv.getOpStackEntry(stackDepth, stackOffset)),
                    simEnv.getLocalVariable(stackDepth, instr.getLocalVarIndex()));
        case LSTORE: case DSTORE:
            // read 2 (but only trace 1), write 0
            stackOffset = simEnv.getAndAddOpStack(stackDepth, 2);
            return new SimpleVariableUsage(Collections.singleton(simEnv.getOpStackEntry(stackDepth, stackOffset)),
                simEnv.getLocalVariable(stackDepth, instr.getLocalVarIndex()));
        case RET:
            // RET reads a local variable, but since this is no "data" (in our sense), we
            // do not trace that
            return DynamicInformation.EMPTY;

        default:
            assert false;
            return null;
        }
    }

    private DynamicInformation simulateSimpleInsn(InstructionInstance inst, SimulationEnvironment simEnv) {
    	int stackDepth = inst.getStackDepth();
    	// TODO improvement: define which variables are defined by which ones
    	// FIXME only define and read the lower parts of long values
        switch (inst.getInstruction().getOpcode()) {
        case DUP:
            int stackHeight = simEnv.decAndGetOpStack(stackDepth);
            return new SimpleVariableUsage(simEnv.getOpStackEntry(stackDepth, stackHeight-1), simEnv.getOpStackEntry(stackDepth, stackHeight));
        case DUP2:
            stackHeight = simEnv.subAndGetOpStack(stackDepth, 2);
            return new SimpleVariableUsage(simEnv.getOpStackEntries(stackDepth, stackHeight-2, 2),
            	simEnv.getOpStackEntries(stackDepth, stackHeight, 2));
        case DUP_X1:
            stackHeight = simEnv.decAndGetOpStack(stackDepth);
            return new SimpleVariableUsage(simEnv.getOpStackEntries(stackDepth, stackHeight-2, 2),
            	simEnv.getOpStackEntries(stackDepth, stackHeight-2, 3));
        case DUP_X2:
            stackHeight = simEnv.decAndGetOpStack(stackDepth);
            return new SimpleVariableUsage(simEnv.getOpStackEntries(stackDepth, stackHeight-3, 3),
            	simEnv.getOpStackEntries(stackDepth, stackHeight-3, 4));
        case DUP2_X1:
            stackHeight = simEnv.subAndGetOpStack(stackDepth, 2);
            return new SimpleVariableUsage(simEnv.getOpStackEntries(stackDepth, stackHeight-3, 3),
            	simEnv.getOpStackEntries(stackDepth, stackHeight-3, 5));
        case DUP2_X2:
            stackHeight = simEnv.subAndGetOpStack(stackDepth, 2);
            return new SimpleVariableUsage(simEnv.getOpStackEntries(stackDepth, stackHeight-4, 4),
            	simEnv.getOpStackEntries(stackDepth, stackHeight-4, 6));

        case IRETURN: case FRETURN: case ARETURN:
        case DRETURN: case LRETURN:
        	int returnedSize = inst.getInstruction().getOpcode() == DRETURN || inst.getInstruction().getOpcode() == LRETURN ? 2 : 1;
            simEnv.throwsException[stackDepth] = false;
            StackEntry stackEntry = simEnv.getOpStackEntry(stackDepth, simEnv.getAndAddOpStack(stackDepth, returnedSize));

            Set<? extends Variable> written = DynamicInformation.EMPTY_VARIABLE_SET;
            if (stackDepth >= 2) {
                Instruction prev = simEnv.lastInstruction[stackDepth-1] == null ? null : simEnv.lastInstruction[stackDepth-1].getPrevious();
                if (prev instanceof MethodInvocationInstruction) {
                    MethodInvocationInstruction m = (MethodInvocationInstruction) prev;
                    if (!m.getInvokedMethodName().equals(simEnv.method[stackDepth-1].getName()) ||
                            !m.getInvokedMethodDesc().equals(simEnv.method[stackDepth-1].getDesc())) {
                        written = Collections.singleton(simEnv.getOpStackEntry(stackDepth - 1, simEnv.getOpStack(stackDepth - 1) - returnedSize));
                    }
                }
            }

            // it is sufficient to trace the lower variable of the double-sized value (long or double)
            return new SimpleVariableUsage(stackEntry, written);

        case RETURN:
            simEnv.throwsException[stackDepth] = false;
            return DynamicInformation.EMPTY;

        case NOP:
            return DynamicInformation.EMPTY;

        case ACONST_NULL: case ICONST_M1: case ICONST_0: case ICONST_1: case ICONST_2: case ICONST_3:
        case ICONST_4: case ICONST_5: case FCONST_0: case FCONST_1: case FCONST_2:
            return stackManipulation(simEnv, stackDepth, 0, 1);

        case DCONST_0: case DCONST_1: case LCONST_0: case LCONST_1:
            return stackManipulation(simEnv, stackDepth, 0, 2);

        case ATHROW:
        	// the data dependence to the catching frame is modelled in DirectSlicer / DependencesExtractor
            return new SimpleVariableUsage(simEnv.getOpStackEntry(stackDepth, simEnv.getAndIncOpStack(stackDepth)),
                    DynamicInformation.EMPTY_VARIABLE_SET);

        case MONITORENTER: case MONITOREXIT:
        case POP:
            return stackManipulation(simEnv, stackDepth, 1, 0);

        case I2F: case F2I: case I2B: case I2C: case I2S:
        case ARRAYLENGTH:
        case INEG: case FNEG:
            return stackManipulation(simEnv, stackDepth, 1, 1);

        case I2L: case I2D: case F2L: case F2D:
            // these operations write two entries, but we only trace the lower one
            Set<StackEntry> stackEntryColl = Collections.singleton(
                simEnv.getOpStackEntry(stackDepth, simEnv.decAndGetOpStack(stackDepth) - 1));
            return new SimpleVariableUsage(stackEntryColl, stackEntryColl);

        case POP2:
            return stackManipulation(simEnv, stackDepth, 2, 0);

        case L2I: case D2I: case L2F: case D2F:
            // these operations read two entries, but we only trace the lower one
            stackEntryColl = Collections.singleton(
                simEnv.getOpStackEntry(stackDepth, simEnv.getAndIncOpStack(stackDepth) - 1));
            return new SimpleVariableUsage(stackEntryColl, stackEntryColl);

        case FCMPL: case FCMPG:
        case IADD: case FADD: case ISUB: case FSUB: case IMUL: case FMUL: case IDIV: case FDIV: case IREM:
        case FREM: case ISHL: case ISHR: case IUSHR: case IAND: case IOR: case IXOR:
            return stackManipulation(simEnv, stackDepth, 2, 1);

        case L2D: case D2L:
        case LNEG: case DNEG:
            // reads one double-sized value and writes one. we only trace the lower parts
            stackEntryColl = Collections.singleton(simEnv.getOpStackEntry(stackDepth, simEnv.getOpStack(stackDepth) - 2));
            return new SimpleVariableUsage(stackEntryColl, stackEntryColl);

        case SWAP:
            return new SwapVariableUsage(simEnv, stackDepth);

        case LCMP: case DCMPL: case DCMPG:
            // reads two double-sized values. we only trace the lower parts
            // writes one single-sized value
            int stackOffset = simEnv.getAndAddOpStack(stackDepth, 3) - 1;
            stackEntry = simEnv.getOpStackEntry(stackDepth, stackOffset);
            return new SimpleVariableUsage(Arrays.asList(stackEntry, simEnv.getOpStackEntry(stackDepth, stackOffset + 2)),
                stackEntry);

        case LADD: case DADD: case LSUB: case DSUB: case LMUL: case DMUL: case LDIV: case DDIV: case LREM:
        case DREM: case LAND: case LOR: case LXOR:
            // reads two double-sized values and writes one. we only trace the lower parts
            stackOffset = simEnv.getAndAddOpStack(stackDepth, 2);
            stackEntry = simEnv.getOpStackEntry(stackDepth, stackOffset - 2);
            return new SimpleVariableUsage(Arrays.asList(stackEntry, simEnv.getOpStackEntry(stackDepth, stackOffset)),
                stackEntry);

        case LSHL: case LSHR: case LUSHR:
            // reads one double-sized and one single-sized value and writes one double-sized. we only trace the lower parts
            stackOffset = simEnv.getAndIncOpStack(stackDepth);
            stackEntry = simEnv.getOpStackEntry(stackDepth, stackOffset-2);
            return new SimpleVariableUsage(Arrays.asList(stackEntry, simEnv.getOpStackEntry(stackDepth, stackOffset)),
                stackEntry);

        default:
            assert false;
            return null;
        }
    }

    private DynamicInformation stackManipulation(SimulationEnvironment simEnv, int stackDepth, int read,
            int write) {
        return stackManipulation(simEnv, stackDepth, read, write, Collections.<Long, Collection<? extends Variable>>emptyMap());
    }

    private DynamicInformation stackManipulation(SimulationEnvironment simEnv, int stackDepth, int read,
            int write, Map<Long, Collection<? extends Variable>> createdObjects) {
        int stackOffset = (read == write ? simEnv.getOpStack(stackDepth) : simEnv.getAndAddOpStack(stackDepth, read - write)) - write;
        return new StackManipulation<InstanceType>(simEnv, stackDepth, read, write, stackOffset, createdObjects);
    }

}
