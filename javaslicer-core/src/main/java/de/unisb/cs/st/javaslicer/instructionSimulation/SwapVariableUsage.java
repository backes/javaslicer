/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.instructionSimulation
 *    Class:     SwapVariableUsage
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/instructionSimulation/SwapVariableUsage.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
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

    public Map<Long, Collection<? extends Variable>> getCreatedObjects() {
        return Collections.emptyMap();
    }

    public Collection<StackEntry> getDefinedVariables() {
        return this;
    }

    public Collection<StackEntry> getUsedVariables() {
        return this;
    }

    public Collection<StackEntry> getUsedVariables(
            Variable definedVariable) {
        if (definedVariable == this.lowerStackEntry) {
            return Collections.singleton(this.upperStackEntry);
        } else {
            assert definedVariable == this.upperStackEntry;
            return Collections.singleton(this.lowerStackEntry);
        }
    }

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
