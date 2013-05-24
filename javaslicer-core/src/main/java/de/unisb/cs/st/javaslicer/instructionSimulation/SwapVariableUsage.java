/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.instructionSimulation
 *    Class:     SwapVariableUsage
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/instructionSimulation/SwapVariableUsage.java
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
package de.unisb.cs.st.javaslicer.instructionSimulation;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.RandomAccess;

import de.unisb.cs.st.javaslicer.variables.StackEntry;
import de.unisb.cs.st.javaslicer.variables.Variable;


/**
 * Object for the variable usages of a SWAP instruction, at the same time the
 * list of used variables of the SWAP instruction (which is the same as the defined
 * variables).
 *
 * @author Clemens Hammacher
 */
public class SwapVariableUsage extends AbstractList<StackEntry>
        implements DynamicInformation, RandomAccess {

    private final StackEntry lowerStackEntry;
    private final StackEntry upperStackEntry;

    public SwapVariableUsage(SimulationEnvironment simEnv, int stackDepth) {
    	int lowerOffset = simEnv.getOpStack(stackDepth) - 2;
    	assert lowerOffset >= 0 || simEnv.interruptedControlFlow[stackDepth];
        this.lowerStackEntry = simEnv.getOpStackEntry(stackDepth, lowerOffset);
        this.upperStackEntry = simEnv.getOpStackEntry(stackDepth, lowerOffset + 1);
    }

    @Override
	public Map<Long, Collection<? extends Variable>> getCreatedObjects() {
        return Collections.emptyMap();
    }

    @Override
	public Collection<StackEntry> getDefinedVariables() {
        return this;
    }

    @Override
	public Collection<StackEntry> getUsedVariables() {
        return this;
    }

    @Override
	public Collection<StackEntry> getUsedVariables(
            Variable definedVariable) {
        if (definedVariable == this.lowerStackEntry) {
            return Collections.singleton(this.upperStackEntry);
        } else {
            assert definedVariable == this.upperStackEntry;
            return Collections.singleton(this.lowerStackEntry);
        }
    }

    @Override
	public boolean isCatchBlock() {
        return false;
    }

    @Override
    public StackEntry get(int index) {
        assert index == 0 || index == 1;
        return index == 0 ? this.lowerStackEntry : this.upperStackEntry;
    }

    @Override
    public int size() {
        return 2;
    }

}
