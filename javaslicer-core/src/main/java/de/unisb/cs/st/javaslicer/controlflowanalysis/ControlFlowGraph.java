package de.unisb.cs.st.javaslicer.controlflowanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.UniqueQueue;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.AbstractInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.JumpInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LookupSwitchInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.TableSwitchInstruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.VarInstruction;

/**
 * A representation of the <b>control flow graph (CFG)</b> for one method.
 *
 * @author Clemens Hammacher
 */
public class ControlFlowGraph {

    /**
     * Representation of one node in the CFG.
     *
     * @author Clemens Hammacher
     */
    public static abstract class InstrNode {

        private final Instruction instruction;

        public InstrNode(final Instruction instr, final ControlFlowGraph cfg) {
            assert instr != null;
            assert !cfg.instructionNodes.containsKey(instr);
            cfg.instructionNodes.put(instr, this);
            this.instruction = instr;
        }

        /**
         * Returns the number of outgoing edges from this node.
         * @return the out degree of the node
         */
        public abstract int getOutDeg();

        public abstract Collection<InstrNode> getSuccessors();

        public Instruction getInstruction() {
            return this.instruction;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.instruction.hashCode();
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final InstrNode other = (InstrNode) obj;
            if (this.instruction == null) {
                if (other.instruction != null)
                    return false;
            } else if (!this.instruction.equals(other.instruction))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return this.instruction.toString();
        }

    }

    public interface NodeFactory {

        InstrNode createNode(ControlFlowGraph cfg, Instruction instruction, Collection<Instruction> successors);

    }


    protected final Map<Instruction, InstrNode> instructionNodes;

    /**
     * Computes the <b>control flow graph</b> for one method.
     *
     * @param method the method for which the CFG is computed
     * @param nodeFactory the factory that creates the nodes of the CFG
     */
    public ControlFlowGraph(final ReadMethod method, final NodeFactory nodeFactory) {
        this.instructionNodes = new HashMap<Instruction, InstrNode>();
        for (final Instruction instr: method.getInstructions()) {
            getInstrNode(instr, method, nodeFactory);
        }
    }

    /**
     * Return the node of the CFG associated to the given {@link Instruction}.
     * If the instruction is not contained in the method that this CFG corresponds
     * to, then <code>null</code> is returned.
     *
     * @param instr the {@link Instruction} for which the node is requested
     * @return the node corresponding to the given {@link Instruction}, or
     *         <code>null</code> if the instruction is not contained in the method of this CFG
     */
    public InstrNode getNode(final Instruction instr) {
        return this.instructionNodes.get(instr);
    }

    protected InstrNode getInstrNode(final Instruction instruction,
            final ReadMethod method, final NodeFactory nodeFactory) {
        assert instruction != null;
        final InstrNode node = this.instructionNodes.get(instruction);
        if (node != null)
            return node;

        final Collection<Instruction> successors = getSuccessors(instruction, method);

        return nodeFactory.createNode(this, instruction, successors);
    }

    private static Collection<Instruction> getSuccessors(final Instruction instruction, final ReadMethod method) {
        final int opcode = instruction.getOpcode();
        switch (instruction.getType()) {
        case JUMP:
            // GOTO and JSR are not conditional
            if (opcode == Opcodes.GOTO || opcode == Opcodes.JSR) {
                return Collections.singleton((Instruction)((JumpInstruction)instruction).getLabel());
            }
            return Arrays.asList(((JumpInstruction)instruction).getLabel(),
                    instruction.getNext());
        case LOOKUPSWITCH:
        {
            final LookupSwitchInstruction lsi = (LookupSwitchInstruction) instruction;
            final Instruction[] successors = new AbstractInstruction[lsi.getHandlers().size()+1];
            successors[0] = lsi.getDefaultHandler();
            int i = 1;
            for (final LabelMarker lm: lsi.getHandlers().values())
                successors[i++] = lm;
            return Arrays.asList(successors);
        }
        case TABLESWITCH:
        {
            final TableSwitchInstruction tsi = (TableSwitchInstruction) instruction;
            final Instruction[] successors = new AbstractInstruction[tsi.getHandlers().length+1];
            successors[0] = tsi.getDefaultHandler();
            System.arraycopy(tsi.getHandlers(), 0, successors, 1, tsi.getHandlers().length);
            return Arrays.asList(successors);
        }
        case SIMPLE:
            switch (opcode) {
            case Opcodes.IRETURN: case Opcodes.LRETURN: case Opcodes.FRETURN:
            case Opcodes.DRETURN: case Opcodes.ARETURN: case Opcodes.RETURN:
                return Collections.emptySet();

            default:
                break;
            }
            break;
        case VAR:
            if (opcode == Opcodes.RET) {
                final List<JumpInstruction> callingInstructions = getJsrInstructions(method, (VarInstruction) instruction);
                final Instruction[] successors = new AbstractInstruction[callingInstructions.size()];
                int i = 0;
                for (final JumpInstruction instr: callingInstructions)
                    successors[i++] = instr.getNext();
                return Arrays.asList(successors);
            }
            break;
        case LABEL:
            if (instruction == method.getMethodLeaveLabel())
                return null;
            break;
        default:
            break;
        }
        return Collections.singleton(instruction.getNext());
    }

    /**
     * Returns all <code>jsr</code> instructions that may end up in the given <code>ret</code> instructions.
     */
    private static List<JumpInstruction> getJsrInstructions(final ReadMethod method, final VarInstruction retInstruction) {
        assert retInstruction.getOpcode() == Opcodes.RET;
        final List<JumpInstruction> list = new ArrayList<JumpInstruction>();
        for (final AbstractInstruction instr: method.getInstructions()) {
            if (instr.getOpcode() == Opcodes.JSR) {
                final Queue<Instruction> queue = new UniqueQueue<Instruction>();
                queue.add(((JumpInstruction)instr).getLabel());
                while (!queue.isEmpty()) {
                    final Instruction instr2 = queue.poll();
                    if (instr2.getOpcode() == Opcodes.RET) {
                        if (instr2 == retInstruction) {
                            list.add((JumpInstruction)instr);
                        }
                        break;
                    }
                    queue.addAll(getSuccessors(instr, method));
                }
            }
        }

        return list;
    }

}
