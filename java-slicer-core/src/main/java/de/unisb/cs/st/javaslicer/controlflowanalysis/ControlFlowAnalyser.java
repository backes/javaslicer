package de.unisb.cs.st.javaslicer.controlflowanalysis;

import java.util.ArrayList;
import java.util.Collection;
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
    public Map<Instruction, Collection<Instruction>> getInvControlDependencies(final ReadMethod method) {
        final Map<Instruction, Collection<Instruction>> invControlDeps = new HashMap<Instruction, Collection<Instruction>>();
        final Set<Instruction> emptyInsnSet = Collections.emptySet();
        final ControlFlowGraph graph = ControlFlowGraph.create(method);
        for (final Instruction insn: method.getInstructions()) {
            final InstrNode node = graph.getNode(insn);
            if (node.getOutDeg() > 1) {
                assert node.getOutDeg() == node.getSuccessors().size();
                final List<Map<InstrNode, Boolean>> succAvailableNodes = new ArrayList<Map<InstrNode,Boolean>>(node.getOutDeg());
                final Set<InstrNode> allInstrNodes = new HashSet<InstrNode>();
                for (final InstrNode succ: node.getSuccessors()) {
                    // if value == true, then it is always executed, otherwise only under certain conditions
                    final Map<InstrNode, Boolean> availableNodes = succ.getAvailableNodes();
                    succAvailableNodes.add(availableNodes);
                    allInstrNodes.addAll(availableNodes.keySet());
                }
                final ArrayList<Instruction> deps = new ArrayList<Instruction>();
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
                if (deps.isEmpty())
                    invControlDeps.put(insn, emptyInsnSet);
                else {
                    deps.trimToSize();
                    invControlDeps.put(insn, deps);
                }
            } else {
                invControlDeps.put(insn, emptyInsnSet);
            }
        }
        return invControlDeps;
    }

}
