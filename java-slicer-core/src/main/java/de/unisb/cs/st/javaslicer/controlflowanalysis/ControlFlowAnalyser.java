package de.unisb.cs.st.javaslicer.controlflowanalysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.JumpInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.LabelMarker;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.LookupSwitchInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.TableSwitchInstruction;

public class ControlFlowAnalyser {

    private static ControlFlowAnalyser instance = new ControlFlowAnalyser();

    private ControlFlowAnalyser() {
        // private constructor
    }

    public static ControlFlowAnalyser getInstance() {
        return instance;
    }

    /**
     *
     * @param method
     * @return a map that contains for every instruction all instructions that are dependent on this one
     */
    public Map<Instruction, Set<Instruction>> getControlDependencies(final ReadMethod method) {
        final Map<Instruction, Set<Instruction>> invControlDeps = new HashMap<Instruction, Set<Instruction>>();
        final Set<Instruction> emptyInsnSet = Collections.emptySet();
        for (final Instruction insn: method.getInstructions()) {
            final int opcode = insn.getOpcode();
            switch (insn.getType()) {
            case JUMP:
                // GOTO and JSR are not conditional
                if (opcode == Opcodes.GOTO || opcode == Opcodes.JSR) {
                    invControlDeps.put(insn, emptyInsnSet);
                } else {
                    invControlDeps.put(insn, getBasicBlock(((JumpInstruction)insn).getLabel()));
                }
                break;
            case LOOKUPSWITCH:
            {
                final LookupSwitchInstruction lsi = (LookupSwitchInstruction) insn;
                final Set<Instruction> dependants = new HashSet<Instruction>();
                dependants.addAll(getBasicBlock(lsi.getDefaultHandler()));
                for (final LabelMarker lm: lsi.getHandlers().values())
                    dependants.addAll(getBasicBlock(lm));
                invControlDeps.put(insn, dependants);
                break;
            }
            case TABLESWITCH:
            {
                final TableSwitchInstruction tsi = (TableSwitchInstruction) insn;
                final Set<Instruction> dependants = new HashSet<Instruction>();
                dependants.addAll(getBasicBlock(tsi.getDefaultHandler()));
                for (final LabelMarker lm: tsi.getHandlers())
                    dependants.addAll(getBasicBlock(lm));
                invControlDeps.put(insn, dependants);
                break;
            }
            default:
                invControlDeps.put(insn, emptyInsnSet);
                break;
            }
        }
        return invControlDeps;
    }

    private Set<Instruction> getBasicBlock(final Instruction startInsn) {
        final Set<Instruction> basicBlock = new HashSet<Instruction>();
        basicBlock.add(startInsn);
        searchNextBranch:
        for (Instruction next = startInsn.getNext(); next != null; next = next.getNext()) {
            switch (next.getType()) {
                case JUMP:
                    final int opcode = next.getOpcode();
                    // GOTO and JSR are not conditional
                    if (opcode != Opcodes.GOTO && opcode != Opcodes.JSR)
                        break searchNextBranch;
                    break;
                case TABLESWITCH:
                case LOOKUPSWITCH:
                    break searchNextBranch;
                default:
                    break;
            }
            basicBlock.add(next);
        }
        return basicBlock;
    }

}
