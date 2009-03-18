package de.unisb.cs.st.javaslicer.controlflowanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.hammacher.util.UniqueQueue;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowGraph.AbstractInstrNode;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowGraph.InstrNode;
import de.unisb.cs.st.javaslicer.controlflowanalysis.ReachabilityNodeFactory.ReachInstrNode;

public class ControlFlowAnalyser {

    private static ControlFlowAnalyser instance = new ControlFlowAnalyser();

    private ControlFlowAnalyser() {
        // private constructor
    }

    public static ControlFlowAnalyser getInstance() {
        return instance;
    }

    /**
     * Computes the (inverted) control dependences for one method.
     *
     * @param method the method for which the dependences are computed
     * @return a map that contains for every instruction all instructions that are dependent on this one
     */
    public Map<Instruction, Set<Instruction>> getInvControlDependences(final ReadMethod method) {
        final Map<Instruction, Set<Instruction>> invControlDeps = new HashMap<Instruction, Set<Instruction>>();
        final Set<Instruction> emptyInsnSet = Collections.emptySet();
        final ControlFlowGraph graph = new ControlFlowGraph(method, ReachabilityNodeFactory.getInstance());
        computeReachableNodes(graph);
        for (final Instruction insn: method.getInstructions()) {
            final InstrNode node = graph.getNode(insn);
            final ReachInstrNode reachNode = (ReachabilityNodeFactory.ReachInstrNode)node;
            if (insn.getType() == InstructionType.LABEL) {
                final LabelMarker label = (LabelMarker) insn;
                if (label.isCatchBlock()) {
                    final Set<AbstractInstrNode> executedIfException = reachNode.getSurelyReached();
                    final InstrNode node2 = graph.getNode(
                            method.getInstructions().iterator().next());
                    final Set<AbstractInstrNode> availableWithoutException = ((ReachInstrNode)node2).getReachable();
                    final Set<Instruction> deps = new HashSet<Instruction>();
                    for (final InstrNode succ: executedIfException) {
                        if (!availableWithoutException.contains(succ) && succ.getInstruction() != insn)
                            deps.add(succ.getInstruction());
                    }
                    invControlDeps.put(insn, deps.isEmpty() ? emptyInsnSet : deps);
                } else {
                    invControlDeps.put(insn, emptyInsnSet);
                }
            } else if (node.getOutDegree() > 1) {
                assert node.getOutDegree() == node.getSuccessors().size();
                final List<Set<AbstractInstrNode>> succAvailableNodes = new ArrayList<Set<AbstractInstrNode>>(node.getOutDegree());
                final Set<AbstractInstrNode> allInstrNodes = new HashSet<AbstractInstrNode>();
                for (final InstrNode succ: node.getSuccessors()) {
                    final ReachInstrNode reachSucc = (ReachInstrNode) succ;
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
        final Queue<InstrNode> queue = new UniqueQueue<InstrNode>(true);
        for (final Instruction instr: cfg.getMethod().getInstructions())
            queue.add(cfg.getNode(instr));

        InstrNode node;
        while ((node = queue.poll()) != null) {
            boolean change = true;
            final ReachInstrNode reachNode = (ReachInstrNode) node;
            final Set<AbstractInstrNode> reachable = reachNode.getReachable();
            final Iterator<InstrNode> succIt = node.getSuccessors().iterator();
            final Set<AbstractInstrNode> surelyReached = new HashSet<AbstractInstrNode>();
            if (succIt.hasNext()) {
                final ReachInstrNode succ = (ReachInstrNode) succIt.next();
                surelyReached.addAll(succ.getSurelyReached());
                change |= reachable.addAll(succ.getReachable());
            }
            while (succIt.hasNext()) {
                final ReachInstrNode succ = (ReachInstrNode) succIt.next();
                surelyReached.retainAll(succ.getSurelyReached());
                change |= reachable.addAll(succ.getReachable());
            }
            change |= reachNode.getSurelyReached().addAll(surelyReached);
            if (change) {
                queue.addAll(node.getPredecessors());
            }
        }
    }

}
