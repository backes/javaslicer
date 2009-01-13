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
    public interface InstrNode {

        /**
         * Returns the number of outgoing edges from this node.
         * @return the out degree of the node
         */
        int getOutDeg();

        /**
         * Returns the number of incoming edges of this node.
         * @return the in degree of the node
         */
        int getInDeg();

        Collection<InstrNode> getSuccessors();
        Collection<InstrNode> getPredecessors();

        Instruction getInstruction();

        /**
         * For internal use only
         */
        void addPredecessor(InstrNode predecessor);

    }

    /**
     * Basic implementation of the interface {@link InstrNode}.
     *
     * @author Clemens Hammacher
     */
    public static abstract class AbstractInstrNode implements InstrNode {

        private final List<InstrNode> predecessors = new ArrayList<InstrNode>(1);
        private final Instruction instruction;

        public AbstractInstrNode(final Instruction instr, final ControlFlowGraph cfg) {
            assert instr != null;
            this.instruction = instr;
            assert !cfg.instructionNodes.containsKey(instr);
            cfg.instructionNodes.put(instr, this);
        }

        public abstract int getOutDeg();

        public abstract Collection<InstrNode> getSuccessors();

        public Collection<InstrNode> getPredecessors() {
            return this.predecessors;
        }

        public int getInDeg() {
            return this.predecessors.size();
        }

        public Instruction getInstruction() {
            return this.instruction;
        }

        public void addPredecessor(InstrNode predecessor) {
            this.predecessors.add(predecessor);
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
            final AbstractInstrNode other = (AbstractInstrNode) obj;
            if (!this.instruction.equals(other.instruction))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return this.instruction.toString();
        }

    }

    public interface NodeFactory {

        AbstractInstrNode createNode(ControlFlowGraph cfg, Instruction instruction, Collection<Instruction> successors);

    }


    protected final Map<Instruction, AbstractInstrNode> instructionNodes;
    
    protected final InstrNode root;

    /**
     * Computes the <b>control flow graph</b> for one method, using the usual
     * {@link SimpleNodeFactory}.
     *
     * @param method the method for which the CFG is computed
     */
    public ControlFlowGraph(final ReadMethod method) {
        this(method, SimpleNodeFactory.getInstance());
    }

    /**
     * Computes the <b>control flow graph</b> for one method.
     *
     * @param method the method for which the CFG is computed
     * @param nodeFactory the factory that creates the nodes of the CFG
     */
    public ControlFlowGraph(final ReadMethod method, final NodeFactory nodeFactory) {
        this.instructionNodes = new HashMap<Instruction, AbstractInstrNode>();
        List<AbstractInstruction> instructions = method.getInstructions();
        for (final Instruction instr: method.getInstructions()) {
            getInstrNode(instr, nodeFactory);
        }
        this.root = getInstrNode(instructions.get(0), nodeFactory);
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
    
    /**
     * Get the root of the CFG.
     * @return The root.
     */
    public InstrNode getRoot() {
    	return root;
    }

    protected AbstractInstrNode getInstrNode(final Instruction instruction,
            final NodeFactory nodeFactory) {
        assert instruction != null;
        final AbstractInstrNode node = this.instructionNodes.get(instruction);
        if (node != null)
            return node;

        final Collection<Instruction> successors = getSuccessors(instruction);
        return nodeFactory.createNode(this, instruction, successors);
    }

    private static Collection<Instruction> getSuccessors(final Instruction instruction) {
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
                final List<JumpInstruction> callingInstructions = getJsrInstructions((VarInstruction) instruction);
                final Instruction[] successors = new AbstractInstruction[callingInstructions.size()];
                int i = 0;
                for (final JumpInstruction instr: callingInstructions)
                    successors[i++] = instr.getNext();
                return Arrays.asList(successors);
            }
            break;
        case LABEL:
            if (instruction == instruction.getMethod().getMethodLeaveLabel())
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
    private static List<JumpInstruction> getJsrInstructions(final VarInstruction retInstruction) {
        assert retInstruction.getOpcode() == Opcodes.RET;
        final List<JumpInstruction> list = new ArrayList<JumpInstruction>();
        for (final AbstractInstruction instr: retInstruction.getMethod().getInstructions()) {
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
                    queue.addAll(getSuccessors(instr));
                }
            }
        }

        return list;
    }

}
