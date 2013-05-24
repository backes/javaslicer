/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.controlflowanalysis
 *    Class:     ControlFlowGraph
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/controlflowanalysis/ControlFlowGraph.java
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
package de.unisb.cs.st.javaslicer.controlflowanalysis;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.UniqueQueue;
import de.hammacher.util.graph.Graph;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
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


    public static class InstrList extends AbstractList<InstrNode> {

        private final InstrNode[] nodes;
        private final int[] nonNullPositions;

        public InstrList(InstrNode[] nodes, int[] nonNullPositions) {
            this.nodes = nodes;
            this.nonNullPositions = nonNullPositions;
        }

        @Override
        public InstrNode get(int index) {
            if (index < 0 || index >= this.nonNullPositions.length)
                throw new IndexOutOfBoundsException();
            return this.nodes[this.nonNullPositions[index]];
        }

        @Override
        public int size() {
            return this.nonNullPositions.length;
        }

    }

    /**
     * Representation of one node in the CFG.
     *
     * @author Clemens Hammacher
     */
    public static interface InstrNode extends Graph.Node<InstrNode> {

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

        // concretisation:
        @Override
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

        public AbstractInstrNode(ControlFlowGraph cfg, Instruction instr) {
            if (cfg == null || instr == null)
                throw new NullPointerException();
            this.cfg = cfg;
            this.instruction = instr;
        }

        @Override
		public Instruction getInstruction() {
            return this.instruction;
        }

        @Override
		public Collection<InstrNode> getSuccessors() {
            return this.successors;
        }

        @Override
		public Collection<InstrNode> getPredecessors() {
            return this.predecessors;
        }

        @Override
		public int getOutDegree() {
            return this.successors.size();
        }

        @Override
		public int getInDegree() {
            return this.predecessors.size();
        }

        @Override
		public void addSuccessor(InstrNode successor) {
            this.successors.add(successor);
        }

        @Override
		public void addPredecessor(InstrNode predecessor) {
            this.predecessors.add(predecessor);
        }

        @Override
        public int hashCode() {
            return this.instruction.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            AbstractInstrNode other = (AbstractInstrNode) obj;
            if (!this.instruction.equals(other.instruction))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return this.instruction.toString();
        }

        @Override
		public ControlFlowGraph getGraph() {
            return this.cfg;
        }

        @Override
		public String getLabel() {
            return toString();
        }

    }

    public interface NodeFactory {

        InstrNode createNode(ControlFlowGraph cfg, Instruction instruction);

    }

    public static class AbstractNodeFactory implements NodeFactory {

        @Override
		public InstrNode createNode(ControlFlowGraph cfg, Instruction instr) {
            return new AbstractInstrNode(cfg, instr);
        }

    }

    private final ReadMethod method;
    protected final InstrNode[] instructionNodes;
    private int[] nonNullPositions;

    /**
     * Computes the <b>control flow graph</b> for one method, using the usual
     * {@link AbstractNodeFactory}.
     *
     * @param method the method for which the CFG is computed
     */
    public ControlFlowGraph(ReadMethod method) {
        this(method, new AbstractNodeFactory());
    }

    /**
     * Computes the <b>control flow graph</b> for one method.
     *
     * @param method the method for which the CFG is computed
     * @param nodeFactory the factory that creates the nodes of the CFG
     */
    public ControlFlowGraph (ReadMethod method, NodeFactory nodeFactory) {
        this(method, nodeFactory, false, false);
    }

    /**
     * Computes the <b>control flow graph</b> for one method.
     *
     * @param method the method for which the CFG is computed
     * @param nodeFactory the factory that creates the nodes of the CFG
     * @param addTryCatchEdges controls whether an edge should be inserted from each
     *                         instruction within a try block to the first instruction
     *                         in the catch block
     * @param excludeLabels if <code>true</code>, all Labels and goto instruction are excluded from the
     *                      CFG
     */
    public ControlFlowGraph (ReadMethod method, NodeFactory nodeFactory,
            boolean addTryCatchEdges, boolean excludeLabels) {
        this.method = method;
        this.instructionNodes = new InstrNode[method.getInstructionNumberEnd() - method.getInstructionNumberStart()];
        for (Instruction instr: method.getInstructions()) {
            getInstrNode(instr, nodeFactory, excludeLabels);
        }
        // now add the edges from try blocks to catch/finally blocks
        if (addTryCatchEdges) {
            for (TryCatchBlock tcb: method.getTryCatchBlocks()) {
                LabelMarker handler = tcb.getHandler();
                Instruction nonLabel = excludeLabels ? followLabelsAndGotos(handler) : handler;
                assert nonLabel != null;
				InstrNode tcbHandler = getNode(nonLabel);
                for (Instruction inst = tcb.getStart(); inst != null && inst != tcb.getEnd(); inst = inst.getNext()) {
                    InstrNode instrNode = getNode(inst);
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
     *
     * If the CFG was created with <b>excludeLabels</b> and the first instruction is a label,
     * then the node for the first non-label instruction is returned.
     */
    public InstrNode getRootNode() {
    	// search for the first non-label node
        int idx = 0;
        if (idx < this.instructionNodes.length) {
            InstrNode instrNode = this.instructionNodes[idx];
            while (instrNode == null && idx < this.instructionNodes.length) {
                instrNode = this.instructionNodes[idx++];
            }
            return instrNode;
        }
        return null;
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
     * If the CFG was created with <b>excludeLabels</b> and the given instruction is a label,
     * then the node for the next non-label instruction is returned.
     *
     * @param instr the {@link Instruction} for which the node is requested
     * @return the node corresponding to the given {@link Instruction}, or
     *         <code>null</code> if the instruction is not contained in the method of this CFG
     */
    public InstrNode getNode(Instruction instr) {
        int idx = instr.getIndex() - this.method.getInstructionNumberStart();
        if (idx >= 0 && idx < this.instructionNodes.length) {
            InstrNode instrNode = this.instructionNodes[idx];
            while (instrNode == null && idx < this.instructionNodes.length) {
                assert instr.getType() == InstructionType.LABEL;
                instrNode = this.instructionNodes[idx++];
            }
            return instrNode;
        }
        return null;
    }

    private Instruction followLabelsAndGotos(Instruction instr) {
    	Instruction nonLabel = instr;
    	while (nonLabel != null) {
    		if (nonLabel.getType() == InstructionType.LABEL) {
    			nonLabel = nonLabel.getNext();
    		} else if (nonLabel.getOpcode() == Opcodes.GOTO) {
				nonLabel = ((JumpInstruction) nonLabel).getLabel().getNext();
    		} else
    			break;
    	}
    	return nonLabel;
    }

    protected InstrNode getInstrNode(Instruction instruction,
            NodeFactory nodeFactory, boolean excludeLabels) {
        int idx = instruction.getIndex() - this.method.getInstructionNumberStart();
        InstrNode node = this.instructionNodes[idx];
        if (node != null || (excludeLabels && (instruction.getType() == InstructionType.LABEL || instruction.getOpcode() == Opcodes.GOTO)))
            return node;

        InstrNode newNode = nodeFactory.createNode(this, instruction);
        this.instructionNodes[idx] = newNode;
        for (Instruction succ: getSuccessors(instruction)) {
        	Instruction nonLabel = excludeLabels ? followLabelsAndGotos(succ) : succ;
        	if (nonLabel == null)
        		continue;
            InstrNode succNode = getInstrNode(nonLabel, nodeFactory, excludeLabels);
            newNode.addSuccessor(succNode);
            succNode.addPredecessor(newNode);
        }
        return newNode;
    }

    private static Collection<Instruction> getSuccessors(Instruction instruction) {
        int opcode = instruction.getOpcode();
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
            LookupSwitchInstruction lsi = (LookupSwitchInstruction) instruction;
            Instruction[] successors = new AbstractInstruction[lsi.getHandlers().size()+1];
            successors[0] = lsi.getDefaultHandler();
            int i = 1;
            for (LabelMarker lm: lsi.getHandlers().values())
                successors[i++] = lm;
            return Arrays.asList(successors);
        }
        case TABLESWITCH:
        {
            TableSwitchInstruction tsi = (TableSwitchInstruction) instruction;
            Instruction[] successors = new AbstractInstruction[tsi.getHandlers().length+1];
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
                List<JumpInstruction> callingInstructions = getJsrInstructions((VarInstruction) instruction);
                Instruction[] successors = new AbstractInstruction[callingInstructions.size()];
                int i = 0;
                for (JumpInstruction instr: callingInstructions)
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
    private static List<JumpInstruction> getJsrInstructions(VarInstruction retInstruction) {
        assert retInstruction.getOpcode() == Opcodes.RET;
        List<JumpInstruction> list = new ArrayList<JumpInstruction>();
        for (AbstractInstruction instr: retInstruction.getMethod().getInstructions()) {
            if (instr.getOpcode() == Opcodes.JSR) {
                Queue<Instruction> queue = new UniqueQueue<Instruction>();
                queue.add(((JumpInstruction)instr).getLabel());
                while (!queue.isEmpty()) {
                    Instruction instr2 = queue.poll();
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

    @Override
	public List<InstrNode> getNodes() {
        if (this.nonNullPositions == null) {
            int numNonNull = 0;
            int[] newNonNullPositions = new int[this.instructionNodes.length];
            for (int i = 0; i < this.instructionNodes.length; ++i) {
                if (this.instructionNodes[i] != null) {
                    newNonNullPositions[numNonNull++] = i;
                }
            }
            this.nonNullPositions = new int[numNonNull];
            System.arraycopy(newNonNullPositions, 0, this.nonNullPositions, 0, numNonNull);

        }
        return new InstrList(this.instructionNodes, this.nonNullPositions);
    }

}
