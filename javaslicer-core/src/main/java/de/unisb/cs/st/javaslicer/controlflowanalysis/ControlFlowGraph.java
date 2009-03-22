package de.unisb.cs.st.javaslicer.controlflowanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.Graph;
import de.hammacher.util.UniqueQueue;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.TryCatchBlock;
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
public class ControlFlowGraph implements Graph<ControlFlowGraph.InstrNode> {

    /**
     * Representation of one node in the CFG.
     *
     * @author Clemens Hammacher
     */
    public interface InstrNode extends Graph.Node<InstrNode> {

        /**
         * Returns the number of outgoing edges from this node.
         * @return the out degree of the node
         */
        int getOutDegree();

        /**
         * Returns the number of incoming edges of this node.
         * @return the in degree of the node
         */
        int getInDegree();

        Collection<InstrNode> getSuccessors();
        Collection<InstrNode> getPredecessors();

        Instruction getInstruction();

        /**
         * For internal use only
         */
        void addSuccessor(InstrNode successor);
        /**
         * For internal use only
         */
        void addPredecessor(InstrNode predecessor);

        ControlFlowGraph getGraph();

    }

    /**
     * Basic implementation of the interface {@link InstrNode}.
     *
     * @author Clemens Hammacher
     */
    public static class AbstractInstrNode implements InstrNode {

        private final List<InstrNode> successors = new ArrayList<InstrNode>(0);
        private final List<InstrNode> predecessors = new ArrayList<InstrNode>(0);
        private final Instruction instruction;
        private final ControlFlowGraph cfg;

        public AbstractInstrNode(final ControlFlowGraph cfg, final Instruction instr) {
            if (cfg == null || instr == null)
                throw new NullPointerException();
            this.cfg = cfg;
            this.instruction = instr;
        }

        public Instruction getInstruction() {
            return this.instruction;
        }

        public Collection<InstrNode> getSuccessors() {
            return this.successors;
        }

        public Collection<InstrNode> getPredecessors() {
            return this.predecessors;
        }

        public int getOutDegree() {
            return this.successors.size();
        }

        public int getInDegree() {
            return this.predecessors.size();
        }

        public void addSuccessor(InstrNode successor) {
            this.successors.add(successor);
        }

        public void addPredecessor(InstrNode predecessor) {
            this.predecessors.add(predecessor);
        }

        @Override
        public int hashCode() {
            return this.instruction.hashCode();
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

        public ControlFlowGraph getGraph() {
            return this.cfg;
        }

        public String getLabel() {
            return toString();
        }

    }

    public interface NodeFactory {

        InstrNode createNode(ControlFlowGraph cfg, Instruction instruction);

    }

    public static class AbstractNodeFactory implements NodeFactory {

        public InstrNode createNode(ControlFlowGraph cfg, Instruction instr) {
            return new AbstractInstrNode(cfg, instr);
        }

    }

    private final ReadMethod method;
    protected final InstrNode[] instructionNodes;

    /**
     * Computes the <b>control flow graph</b> for one method, using the usual
     * {@link AbstractNodeFactory}.
     *
     * @param method the method for which the CFG is computed
     */
    public ControlFlowGraph(final ReadMethod method) {
        this(method, new AbstractNodeFactory());
    }

    /**
     * Computes the <b>control flow graph</b> for one method.
     *
     * @param method the method for which the CFG is computed
     * @param nodeFactory the factory that creates the nodes of the CFG
     */
    public ControlFlowGraph(final ReadMethod method, final NodeFactory nodeFactory) {
        this(method, nodeFactory, false);
    }

    /**
     * Computes the <b>control flow graph</b> for one method.
     *
     * @param method the method for which the CFG is computed
     * @param nodeFactory the factory that creates the nodes of the CFG
     * @param addTryCatchEdges controls whether an edge should be inserted from each
     *                         instruction within a try block to the first instruction
     *                         in the catch block
     */
    public ControlFlowGraph(final ReadMethod method, final NodeFactory nodeFactory,
            boolean addTryCatchEdges) {
        this.method = method;
        this.instructionNodes = new InstrNode[method.getInstructionNumberEnd() - method.getInstructionNumberStart()];
        for (final Instruction instr: method.getInstructions()) {
            getInstrNode(instr, nodeFactory);
        }
        // now add the edges from try blocks to catch/finally blocks
        // TODO find out for each bytecode instruction which exceptions they can throw
        if (addTryCatchEdges) {
            for (TryCatchBlock tcb: method.getTryCatchBlocks()) {
                InstrNode tcbHandler = getInstrNode(tcb.getHandler(), nodeFactory);
                for (Instruction inst = tcb.getStart(); inst != null && inst != tcb.getEnd(); inst = inst.getNext()) {
                    InstrNode instrNode = getInstrNode(inst, nodeFactory);
                    instrNode.addSuccessor(tcbHandler);
                    tcbHandler.addPredecessor(instrNode);
                }
            }
        }
    }

    /**
     * Returns the root of this CFG, which is just the Node corresponding to the
     * first instruction of this CFG's method, or null if the method contains no
     * instructions.
     */
    public InstrNode getRootNode() {
        return this.instructionNodes.length == 0 ? null : this.instructionNodes[0];
    }

    /**
     * Returns the method on which this CFG was built.
     *
     * @return the method on which this CFG was built.
     */
    public ReadMethod getMethod() {
        return this.method;
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
        int idx = instr.getIndex() - this.method.getInstructionNumberStart();
        if (idx >= 0 && idx < this.instructionNodes.length)
            return this.instructionNodes[idx];
        return null;
    }

    protected InstrNode getInstrNode(final Instruction instruction,
            final NodeFactory nodeFactory) {
        int idx = instruction.getIndex() - this.method.getInstructionNumberStart();
        final InstrNode node = this.instructionNodes[idx];
        if (node != null)
            return node;

        InstrNode newNode = nodeFactory.createNode(this, instruction);
        this.instructionNodes[idx] = newNode;
        for (Instruction succ: getSuccessors(instruction)) {
            InstrNode succNode = getInstrNode(succ, nodeFactory);
            newNode.addSuccessor(succNode);
            succNode.addPredecessor(newNode);
        }
        return newNode;
    }

    private static Collection<Instruction> getSuccessors(final Instruction instruction) {
        final int opcode = instruction.getOpcode();
        Instruction nextInstruction = instruction.getNext();
        switch (instruction.getType()) {
        case JUMP:
            // GOTO and JSR are not conditional
            if (opcode == Opcodes.GOTO || opcode == Opcodes.JSR) {
                return Collections.singleton((Instruction)((JumpInstruction)instruction).getLabel());
            }
            assert nextInstruction != null;
            return Arrays.asList(((JumpInstruction)instruction).getLabel(),
                    nextInstruction);
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
            if (instruction == instruction.getMethod().getAbnormalTerminationLabel())
                return Collections.emptySet();
            break;
        default:
            break;
        }
        assert nextInstruction != null;
        return Collections.singleton(nextInstruction);
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

    public Collection<InstrNode> getNodes() {
        return Collections.unmodifiableList(Arrays.asList(this.instructionNodes));
    }

}
