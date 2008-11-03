package de.unisb.cs.st.javaslicer.controlflowanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
        for (final Instruction insn: method.getInstructions()) {
            final InstrNode node = graph.getNode(insn);
            if (insn.getType() == Type.LABEL) {
                final LabelMarker label = (LabelMarker) insn;
                if (label.isCatchBlock()) {
                    // TODO this may not be correct in all cases
                    final Map<InstrNode, Boolean> availableIfException = node.getAvailableNodes();
                    final Map<InstrNode, Boolean> availableWithoutException = graph.getNode(
                            method.getInstructions().iterator().next()).getAvailableNodes();
                    final Set<Instruction> deps = new HashSet<Instruction>();
                    for (final InstrNode succ: availableIfException.keySet()) {
                        if (!availableWithoutException.containsKey(succ) && succ.getInstruction() != insn)
                            deps.add(succ.getInstruction());
                    }
                    invControlDeps.put(insn, deps.isEmpty() ? emptyInsnSet : deps);
                } else
                    invControlDeps.put(insn, emptyInsnSet);
            } else if (node.getOutDeg() > 1) {
                assert node.getOutDeg() == node.getSuccessors().size();
                final List<Map<InstrNode, Boolean>> succAvailableNodes = new ArrayList<Map<InstrNode,Boolean>>(node.getOutDeg());
                final Set<InstrNode> allInstrNodes = new HashSet<InstrNode>();
                for (final InstrNode succ: node.getSuccessors()) {
                    // if value == true, then it is always executed, otherwise only under certain conditions
                    final Map<InstrNode, Boolean> availableNodes = succ.getAvailableNodes();
                    succAvailableNodes.add(availableNodes);
                    allInstrNodes.addAll(availableNodes.keySet());
                }
                final Set<Instruction> deps = new HashSet<Instruction>();
                for (final InstrNode succ: allInstrNodes) {
                    final Iterator<Map<InstrNode, Boolean>> it = succAvailableNodes.iterator();
                    final Boolean b = it.next().get(succ);
                    while (it.hasNext()) {
                        final Boolean b2 = it.next().get(succ);
                        if (b2 != b) {
                            deps.add(succ.getInstruction());
                            break;
                        }
                    }
                }
                invControlDeps.put(insn, deps.isEmpty() ? emptyInsnSet : deps);
            } else {
                invControlDeps.put(insn, emptyInsnSet);
            }
        }
        return invControlDeps;
    }

}
