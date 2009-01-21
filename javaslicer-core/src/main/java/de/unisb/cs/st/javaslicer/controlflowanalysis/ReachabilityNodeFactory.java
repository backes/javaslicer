package de.unisb.cs.st.javaslicer.controlflowanalysis;

import java.util.HashSet;
import java.util.Set;

import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowGraph.AbstractInstrNode;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowGraph.AbstractNodeFactory;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowGraph.InstrNode;

public class ReachabilityNodeFactory extends AbstractNodeFactory {

    protected static class ReachInstrNode extends AbstractInstrNode {

        private final Set<AbstractInstrNode> surelyReached = new HashSet<AbstractInstrNode>();
        private final Set<AbstractInstrNode> reachable = new HashSet<AbstractInstrNode>();

        public ReachInstrNode(Instruction instr) {
            super(instr);
            this.surelyReached.add(this);
            this.reachable.add(this);
        }

        public Set<AbstractInstrNode> getSurelyReached() {
            return this.surelyReached;
        }

        public Set<AbstractInstrNode> getReachable() {
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
    public InstrNode createNode(Instruction instr) {
        return new ReachInstrNode(instr);
    }

}
