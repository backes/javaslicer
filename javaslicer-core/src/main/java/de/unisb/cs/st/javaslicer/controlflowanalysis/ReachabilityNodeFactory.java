/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.controlflowanalysis
 *    Class:     ReachabilityNodeFactory
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/controlflowanalysis/ReachabilityNodeFactory.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
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
