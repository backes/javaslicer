package de.unisb.cs.st.javaslicer.controlflowanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.AbstractInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.JumpInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.LabelMarker;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.LookupSwitchInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.TableSwitchInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.VarInstruction;
import de.unisb.cs.st.javaslicer.util.UniqueQueue;

public class ControlFlowGraph {

    public static abstract class InstrNode {

        private final Instruction instruction;

        public InstrNode(final Instruction instr, final Map<Instruction, InstrNode> instructionNodes) {
            assert !instructionNodes.containsKey(instr);
            instructionNodes.put(instr, this);
            this.instruction = instr;
        }

        /**
         * Returns the number of outgoing edges from this node.
         */
        public abstract int getOutDeg();

        public abstract Collection<InstrNode> getSuccessors();

        /**
         * Returns a map containing all nodes that are reachable from this one.
         *
         * If the assigned value is <code>true</code>, then the instruction is always
         * executed, otherwise it is only executed under certain conditions.
         */
        public Map<InstrNode, Boolean> getAvailableNodes() {
            return getAvailableNodes(new HashSet<InstrNode>());
        }

        private Map<InstrNode, Boolean> getAvailableNodes(final Set<InstrNode> seenNodes) {
            if (!seenNodes.add(this))
                return new HashMap<InstrNode, Boolean>();
            final Collection<InstrNode> successors = getSuccessors();
            final Map<InstrNode, Boolean> availableNodes;
            if (successors.size() == 1) {
                availableNodes = successors.iterator().next().getAvailableNodes(seenNodes);
            } else {
                final Map<InstrNode, Integer> succAvailableNodes = new HashMap<InstrNode, Integer>();
                for (final InstrNode succ: successors) {
                    for (final Entry<InstrNode, Boolean> entry: succ.getAvailableNodes(new HashSet<InstrNode>(seenNodes)).entrySet()) {
                        final Integer old = succAvailableNodes.get(entry.getKey());
                        int oldInt = old == null ? 0 : old.intValue();
                        if (entry.getValue() == Boolean.TRUE)
                            ++oldInt;
                        succAvailableNodes.put(entry.getKey(), oldInt);
                    }
                }
                availableNodes = new HashMap<InstrNode, Boolean>();
                for (final Entry<InstrNode, Integer> entry: succAvailableNodes.entrySet()) {
                    availableNodes.put(entry.getKey(), entry.getValue() == successors.size());
                }
            }
            availableNodes.put(this, true);
            return availableNodes;
        }

        public Instruction getInstruction() {
            return this.instruction;
        }

        @Override
        public String toString() {
            return this.instruction.toString();
        }

    }

    private static class LeafNode extends InstrNode {

        public LeafNode(final Instruction instr, final Map<Instruction, InstrNode> instructionNodes) {
            super(instr, instructionNodes);
        }

        @Override
        public int getOutDeg() {
            return 0;
        }

        @Override
        public Collection<InstrNode> getSuccessors() {
            return Collections.emptySet();
        }

    }

    private static class SimpleInstrNode extends InstrNode {

        private final InstrNode successor;

        public SimpleInstrNode(final AbstractInstruction instruction, final Map<Instruction, InstrNode> instructionNodes,
                final AbstractInstruction successor, final ReadMethod method) {
            super(instruction, instructionNodes);
            this.successor = getInstrNode(successor, instructionNodes, method);
        }

        @Override
        public int getOutDeg() {
            return 1;
        }

        @Override
        public Collection<InstrNode> getSuccessors() {
            return Collections.singleton(this.successor);
        }

    }

    private static class BranchingInstrNode extends InstrNode {

        private final InstrNode[] successors;

        public BranchingInstrNode(final AbstractInstruction instruction, final Map<Instruction, InstrNode> instructionNodes,
                final Collection<AbstractInstruction> successors, final ReadMethod method) {
            super(instruction, instructionNodes);
            this.successors = new InstrNode[successors.size()];
            int i = 0;
            for (final AbstractInstruction succ: successors)
                this.successors[i++] = getInstrNode(succ, instructionNodes, method);
        }

        @Override
        public int getOutDeg() {
            return this.successors.length;
        }

        @Override
        public Collection<InstrNode> getSuccessors() {
            return Arrays.asList(this.successors);
        }

    }

    private final Map<Instruction, InstrNode> instructionNodes;

    public ControlFlowGraph(final Map<Instruction, InstrNode> instructionNodes) {
        this.instructionNodes = instructionNodes;
    }

    public InstrNode getNode(final Instruction instr) {
        return this.instructionNodes.get(instr);
    }

    public static ControlFlowGraph create(final ReadMethod method) {
        final Iterator<AbstractInstruction> instrIt = method.getInstructions().iterator();
        if (!instrIt.hasNext())
            return null;

        final Map<Instruction, InstrNode> instructionNodes = new HashMap<Instruction, InstrNode>();
        while (instrIt.hasNext()) {
            getInstrNode(instrIt.next(), instructionNodes, method);
        }

        return new ControlFlowGraph(instructionNodes);
    }

    protected static InstrNode getInstrNode(final AbstractInstruction instruction,
            final Map<Instruction, InstrNode> instructionNodes, final ReadMethod method) {
        assert instruction != null;
        final InstrNode node = instructionNodes.get(instruction);
        if (node != null)
            return node;

        final Collection<AbstractInstruction> successors = getSuccessors(instruction, method);

        if (successors == null || successors.isEmpty()) {
            assert successors != null || instruction == method.getMethodLeaveLabel();
            return new LeafNode(instruction, instructionNodes);
        }
        if (successors.size() == 1)
            return new SimpleInstrNode(instruction, instructionNodes, successors.iterator().next(), method);
        if (successors.size() == 2) {
            final Iterator<AbstractInstruction> it = successors.iterator();
            return new BranchingInstrNode(instruction, instructionNodes, Arrays.asList(it.next(), it.next()), method);
        }

        return new BranchingInstrNode(instruction, instructionNodes, successors, method);
    }

    private static Collection<AbstractInstruction> getSuccessors(final AbstractInstruction instruction, final ReadMethod method) {
        final int opcode = instruction.getOpcode();
        switch (instruction.getType()) {
        case JUMP:
            // GOTO and JSR are not conditional
            if (opcode == Opcodes.GOTO || opcode == Opcodes.JSR) {
                return Collections.singleton((AbstractInstruction)((JumpInstruction)instruction).getLabel());
            }
            return Arrays.asList(((JumpInstruction)instruction).getLabel(),
                    instruction.getNext());
        case LOOKUPSWITCH:
        {
            final LookupSwitchInstruction lsi = (LookupSwitchInstruction) instruction;
            final AbstractInstruction[] successors = new AbstractInstruction[lsi.getHandlers().size()+1];
            successors[0] = lsi.getDefaultHandler();
            int i = 1;
            for (final LabelMarker lm: lsi.getHandlers().values())
                successors[i++] = lm;
            return Arrays.asList(successors);
        }
        case TABLESWITCH:
        {
            final TableSwitchInstruction tsi = (TableSwitchInstruction) instruction;
            final AbstractInstruction[] successors = new AbstractInstruction[tsi.getHandlers().length+1];
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
                final AbstractInstruction[] successors = new AbstractInstruction[callingInstructions.size()];
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
                final Queue<AbstractInstruction> queue = new UniqueQueue<AbstractInstruction>();
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
