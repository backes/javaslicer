/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.controlflowanalysis
 *    Class:     ReachabilityNodeFactory
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/controlflowanalysis/ReachabilityNodeFactory.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
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

        public ReachInstrNode(ControlFlowGraph cfg, Instruction instr) {
            super(cfg, instr);
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
    public InstrNode createNode(ControlFlowGraph cfg, Instruction instr) {
        return new ReachInstrNode(cfg, instr);
    }

}
