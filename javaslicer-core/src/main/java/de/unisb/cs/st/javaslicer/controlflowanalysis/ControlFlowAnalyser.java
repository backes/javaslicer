package de.unisb.cs.st.javaslicer.controlflowanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    public Map<Instruction, Set<Instruction>> getInvControlDependences(ReadMethod method) {
        Map<Instruction, Set<Instruction>> invControlDeps = new HashMap<Instruction, Set<Instruction>>();
        Set<Instruction> emptyInsnSet = Collections.emptySet();
        ControlFlowGraph graph = new ControlFlowGraph(method, ReachabilityNodeFactory.getInstance());
        computeReachableNodes(graph);
        for (Instruction insn: method.getInstructions()) {
            InstrNode node = graph.getNode(insn);
            ReachInstrNode reachNode = (ReachabilityNodeFactory.ReachInstrNode)node;
            if (insn.getType() == InstructionType.LABEL) {
                LabelMarker label = (LabelMarker) insn;
                if (label.isCatchBlock()) {
                    Set<AbstractInstrNode> executedIfException = reachNode.getSurelyReached();
                    InstrNode node2 = graph.getNode(
                            method.getInstructions().iterator().next());
                    Set<AbstractInstrNode> availableWithoutException = ((ReachInstrNode)node2).getReachable();
                    Set<Instruction> deps = new HashSet<Instruction>();
                    for (InstrNode succ: executedIfException) {
                        if (!availableWithoutException.contains(succ) && succ.getInstruction() != insn)
                            deps.add(succ.getInstruction());
                    }
                    invControlDeps.put(insn, deps.isEmpty() ? emptyInsnSet : deps);
                } else {
                    invControlDeps.put(insn, emptyInsnSet);
                }
            } else if (node.getOutDegree() > 1) {
                assert node.getOutDegree() == node.getSuccessors().size();
                List<Set<AbstractInstrNode>> succAvailableNodes = new ArrayList<Set<AbstractInstrNode>>(node.getOutDegree());
                Set<AbstractInstrNode> allInstrNodes = new HashSet<AbstractInstrNode>();
                for (InstrNode succ: node.getSuccessors()) {
                    ReachInstrNode reachSucc = (ReachInstrNode) succ;
                    Set<AbstractInstrNode> availableNodes = reachSucc.getSurelyReached();
                    succAvailableNodes.add(availableNodes);
                    allInstrNodes.addAll(availableNodes);
                }
                Set<Instruction> deps = new HashSet<Instruction>();
                for (InstrNode succ: allInstrNodes) {
                    for (Set<AbstractInstrNode> succAv: succAvailableNodes)
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

    private void computeReachableNodes(ControlFlowGraph cfg) {
        UniqueQueue<InstrNode> queue = new UniqueQueue<InstrNode>(true);
        for (Instruction instr: cfg.getMethod().getInstructions())
            queue.add(cfg.getNode(instr));

        InstrNode node;
        while ((node = queue.poll()) != null) {
            Iterator<InstrNode> succIt = node.getSuccessors().iterator();
            if (succIt.hasNext()) {
                boolean change = false;
                ReachInstrNode reachNode = (ReachInstrNode) node;
                ReachInstrNode succ1 = (ReachInstrNode) succIt.next();
                Set<AbstractInstrNode> reachable = reachNode.getReachable();
                change |= reachable.addAll(succ1.getReachable());
                if (succIt.hasNext()) { // more than one successor?
                    Set<AbstractInstrNode> surelyReached = new HashSet<AbstractInstrNode>(succ1.getSurelyReached());
                    do {
                        ReachInstrNode succ2 = (ReachInstrNode) succIt.next();
                        surelyReached.retainAll(succ2.getSurelyReached());
                        change |= reachable.addAll(succ2.getReachable());
                    } while (succIt.hasNext());
                    change |= reachNode.getSurelyReached().addAll(surelyReached);
                } else {
                    change |= reachNode.getSurelyReached().addAll(succ1.getSurelyReached());
                }
                if (change) {
                    for (InstrNode pred : node.getPredecessors())
                        queue.addFirst(pred);
                }
            }
        }
    }

}
