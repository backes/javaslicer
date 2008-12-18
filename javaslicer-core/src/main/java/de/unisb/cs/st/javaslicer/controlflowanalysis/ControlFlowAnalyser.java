package de.unisb.cs.st.javaslicer.controlflowanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Type;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowGraph.AbstractInstrNode;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowGraph.InstrNode;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ReachabilityNodeFactory.ReachNode;

public class ControlFlowAnalyser {

    private static ControlFlowAnalyser instance = new ControlFlowAnalyser();

    private ControlFlowAnalyser() {
        // private constructor
    }

    public static ControlFlowAnalyser getInstance() {
        return instance;
    }

    /**
     * Computes the (inverted) control dependencies for one method.
     *
     * @param method the method for which the dependencies are computed
     * @return a map that contains for every instruction all instructions that are dependent on this one
     */
    public Map<Instruction, Set<Instruction>> getInvControlDependencies(final ReadMethod method) {
        final Map<Instruction, Set<Instruction>> invControlDeps = new HashMap<Instruction, Set<Instruction>>();
        final Set<Instruction> emptyInsnSet = Collections.emptySet();
        final ControlFlowGraph graph = new ControlFlowGraph(method, ReachabilityNodeFactory.getInstance());
        computeReachableNodes(graph);
        for (final Instruction insn: method.getInstructions()) {
            final InstrNode node = graph.getNode(insn);
            final ReachNode reachNode = (ReachabilityNodeFactory.ReachNode)node;
            if (insn.getType() == Type.LABEL) {
                final LabelMarker label = (LabelMarker) insn;
                if (label.isCatchBlock()) {
                    final Set<AbstractInstrNode> executedIfException = reachNode.getSurelyReached();
                    final InstrNode node2 = graph.getNode(
                            method.getInstructions().iterator().next());
                    final Set<AbstractInstrNode> availableWithoutException = ((ReachNode)node2).getReachable();
                    final Set<Instruction> deps = new HashSet<Instruction>();
                    for (final InstrNode succ: executedIfException) {
                        if (!availableWithoutException.contains(succ) && succ.getInstruction() != insn)
                            deps.add(succ.getInstruction());
                    }
                    invControlDeps.put(insn, deps.isEmpty() ? emptyInsnSet : deps);
                } else
                    invControlDeps.put(insn, emptyInsnSet);
            } else if (node.getOutDeg() > 1) {
                assert node.getOutDeg() == node.getSuccessors().size();
                final List<Set<AbstractInstrNode>> succAvailableNodes = new ArrayList<Set<AbstractInstrNode>>(node.getOutDeg());
                final Set<AbstractInstrNode> allInstrNodes = new HashSet<AbstractInstrNode>();
                for (final InstrNode succ: node.getSuccessors()) {
                    final ReachNode reachSucc = (ReachNode) succ;
                    final Set<AbstractInstrNode> availableNodes = reachSucc.getSurelyReached();
                    succAvailableNodes.add(availableNodes);
                    allInstrNodes.addAll(availableNodes);
                }
                final Set<Instruction> deps = new HashSet<Instruction>();
                for (final InstrNode succ: allInstrNodes) {
                    for (final Set<AbstractInstrNode> succAv: succAvailableNodes)
                        if (!succAv.contains(succ))
                            deps.add(succ.getInstruction());
                }
                invControlDeps.put(insn, deps.isEmpty() ? emptyInsnSet : deps);
            } else {
                invControlDeps.put(insn, emptyInsnSet);
            }
        }
        return invControlDeps;
    }

    private void computeReachableNodes(final ControlFlowGraph cfg) {
        boolean stable = false;
        while (!stable) {
            stable = true;
            for (final InstrNode node: cfg.instructionNodes.values()) {
                final ReachNode reachNode = (ReachNode) node;
                final Set<AbstractInstrNode> reachable = reachNode.getReachable();
                final Iterator<InstrNode> succIt = node.getSuccessors().iterator();
                final Set<AbstractInstrNode> surelyReached = new HashSet<AbstractInstrNode>();
                if (succIt.hasNext()) {
                    final ReachNode succ = (ReachNode) succIt.next();
                    surelyReached.addAll(succ.getSurelyReached());
                    stable &= !reachable.addAll(succ.getReachable());
                }
                while (succIt.hasNext()) {
                    final ReachNode succ = (ReachNode) succIt.next();
                    surelyReached.retainAll(succ.getSurelyReached());
                    stable &= !reachable.addAll(succ.getReachable());
                }
                stable &= !reachNode.getSurelyReached().addAll(surelyReached);
            }
        }
    }

}
