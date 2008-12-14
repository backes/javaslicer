package de.unisb.cs.st.javaslicer.controlflowanalysis;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowGraph.InstrNode;

public class ReachabilityNodeFactory extends SimpleNodeFactory {

    protected static interface ReachNode {
        Set<InstrNode> getSurelyReached();
        Set<InstrNode> getReachable();
    }

    private static class ReachLeafNode extends LeafNode implements ReachNode {

        private final Set<InstrNode> surelyReached = new HashSet<InstrNode>();
        private final Set<InstrNode> reachable = new HashSet<InstrNode>();

        public ReachLeafNode(final Instruction instr, final ControlFlowGraph cfg) {
            super(instr, cfg);
            this.surelyReached.add(this);
            this.reachable.add(this);
        }

        public Set<InstrNode> getSurelyReached() {
            return this.surelyReached;
        }

        public Set<InstrNode> getReachable() {
            return this.reachable;
        }

    }

    private static class ReachSimpleInstrNode extends SimpleInstrNode implements ReachNode {

        private final Set<InstrNode> surelyReached = new HashSet<InstrNode>();
        private final Set<InstrNode> reachable = new HashSet<InstrNode>();

        public ReachSimpleInstrNode(final Instruction instruction,
                final ControlFlowGraph cfg, final Instruction successor,
                final ReachabilityNodeFactory factory) {
            super(instruction, cfg, successor, factory);
            this.surelyReached.add(this);
            this.reachable.add(this);
        }

        public Set<InstrNode> getSurelyReached() {
            return this.surelyReached;
        }

        public Set<InstrNode> getReachable() {
            return this.reachable;
        }

    }

    private static class ReachBranchingInstrNode extends BranchingInstrNode implements ReachNode {

        private final Set<InstrNode> surelyReached = new HashSet<InstrNode>();
        private final Set<InstrNode> reachable = new HashSet<InstrNode>();

        public ReachBranchingInstrNode(final Instruction instruction,
                final ControlFlowGraph cfg, final Collection<Instruction> successors,
                final ReachabilityNodeFactory factory) {
            super(instruction, cfg, successors, factory);
            this.surelyReached.add(this);
            this.reachable.add(this);
        }

        public Set<InstrNode> getSurelyReached() {
            return this.surelyReached;
        }

        public Set<InstrNode> getReachable() {
            return this.reachable;
        }

    }

    private static final ReachabilityNodeFactory instance = new ReachabilityNodeFactory();

    private ReachabilityNodeFactory() {
        // private ==> singleton
    }

    public static ReachabilityNodeFactory getInstance() {
        return instance;
    }

    @Override
    public InstrNode createNode(final ControlFlowGraph cfg, final Instruction instruction, final Collection<Instruction> successors) {
        assert instruction != null;

        if (successors == null || successors.isEmpty())
            return new ReachLeafNode(instruction, cfg);
        if (successors.size() == 1)
            return new ReachSimpleInstrNode(instruction, cfg, successors.iterator().next(), this);

        return new ReachBranchingInstrNode(instruction, cfg, successors, this);
    }

}
