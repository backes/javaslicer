package de.unisb.cs.st.javaslicer.controlflowanalysis;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowGraph.InstrNode;

public class SimpleNodeFactory implements ControlFlowGraph.NodeFactory {

    protected static class LeafNode extends InstrNode {

        public LeafNode(final Instruction instr, final ControlFlowGraph cfg) {
            super(instr, cfg);
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

    protected static class SimpleInstrNode extends InstrNode {

        private final InstrNode successor;

        public SimpleInstrNode(final Instruction instruction,
                final ControlFlowGraph cfg, final Instruction successor,
                final SimpleNodeFactory factory) {
            super(instruction, cfg);
            this.successor = cfg.getInstrNode(successor, instruction.getMethod(), factory);
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

    protected static class BranchingInstrNode extends InstrNode {

        private final InstrNode[] successors;

        public BranchingInstrNode(final Instruction instruction,
                final ControlFlowGraph cfg, final Collection<Instruction> successors,
                final SimpleNodeFactory factory) {
            super(instruction, cfg);
            this.successors = new InstrNode[successors.size()];
            int i = 0;
            for (final Instruction succ: successors)
                this.successors[i++] = cfg.getInstrNode(succ, instruction.getMethod(), factory);
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

    private static final SimpleNodeFactory instance = new SimpleNodeFactory();

    protected SimpleNodeFactory() {
        // singleton
    }

    public static SimpleNodeFactory getInstance() {
        return instance;
    }

    public InstrNode createNode(final ControlFlowGraph cfg, final Instruction instruction, final Collection<Instruction> successors) {
        assert instruction != null;

        if (successors == null || successors.isEmpty())
            return new LeafNode(instruction, cfg);
        if (successors.size() == 1)
            return new SimpleInstrNode(instruction, cfg, successors.iterator().next(), this);

        return new BranchingInstrNode(instruction, cfg, successors, this);
    }

}
