package de.unisb.cs.st.javaslicer.controlflowanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.javaslicer.controlflowanalysis.ControlFlowGraph.InstrNode;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction.Type;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.LabelMarker;

public class ControlFlowAnalyser {

    private static ControlFlowAnalyser instance = new ControlFlowAnalyser();

    private ControlFlowAnalyser() {
        // private constructor
    }

    public static ControlFlowAnalyser getInstance() {
        return instance;
    }

    /**
     *
     * @param method
     * @return a map that contains for every instruction all instructions that are dependent on this one
     */
    public Map<Instruction, Set<Instruction>> getInvControlDependencies(final ReadMethod method) {
        final Map<Instruction, Set<Instruction>> invControlDeps = new HashMap<Instruction, Set<Instruction>>();
        final Set<Instruction> emptyInsnSet = Collections.emptySet();
        final ControlFlowGraph graph = ControlFlowGraph.create(method);
        graph.computeReachable();
        for (final Instruction insn: method.getInstructions()) {
            final InstrNode node = graph.getNode(insn);
            if (insn.getType() == Type.LABEL) {
                final LabelMarker label = (LabelMarker) insn;
                if (label.isCatchBlock()) {
                    final Set<InstrNode> executedIfException = node.getSurelyReached();
                    final Set<InstrNode> availableWithoutException = graph.getNode(
                            method.getInstructions().iterator().next()).getReachableNodes();
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
                final List<Set<InstrNode>> succAvailableNodes = new ArrayList<Set<InstrNode>>(node.getOutDeg());
                final Set<InstrNode> allInstrNodes = new HashSet<InstrNode>();
                for (final InstrNode succ: node.getSuccessors()) {
                    final Set<InstrNode> availableNodes = succ.getSurelyReached();
                    succAvailableNodes.add(availableNodes);
                    allInstrNodes.addAll(availableNodes);
                }
                final Set<Instruction> deps = new HashSet<Instruction>();
                for (final InstrNode succ: allInstrNodes) {
                    for (final Set<InstrNode> succAv: succAvailableNodes)
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

}
