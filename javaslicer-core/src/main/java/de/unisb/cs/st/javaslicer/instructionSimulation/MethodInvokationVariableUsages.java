/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.instructionSimulation
 *    Class:     MethodInvokationVariableUsages
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/instructionSimulation/MethodInvokationVariableUsages.java
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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import de.unisb.cs.st.javaslicer.variables.LocalVariable;
import de.unisb.cs.st.javaslicer.variables.StackEntry;
import de.unisb.cs.st.javaslicer.variables.Variable;


public class MethodInvokationVariableUsages<InstanceType> implements DynamicInformation {

    private final SimulationEnvironment simEnv;
    private final int stackDepth;
    private final int stackOffset;
    private final int paramCount;
    private Collection<StackEntry> usedVariables;
    private final boolean hasReturn; // did this method invocation return a value?
	private final boolean hasRemovedFrame;

    public MethodInvokationVariableUsages(SimulationEnvironment simEnv, int stackDepth, int stackOffset,
    		int paramCount, boolean hasReturn, boolean hasRemovedFrame) {
    	this.simEnv = simEnv;
    	this.stackDepth = stackDepth;
        this.stackOffset = stackOffset;
        this.paramCount = paramCount;
        this.hasReturn = hasReturn;
        this.hasRemovedFrame = hasRemovedFrame;
    }

    @Override
	public Collection<? extends Variable> getDefinedVariables() {
        // if we have no removedFrame, then the defined variable is the return value.
        // it is sufficient to take the lower variable of double sized values (long & double).
        if (!this.hasRemovedFrame)
            return this.hasReturn ? Collections.<Variable>singleton(this.simEnv.getOpStackEntry(this.stackDepth, this.stackOffset)) : EMPTY_VARIABLE_SET;

        // if the method has no parameters (and is static), then we can just return an empty set
        if (this.paramCount == 0)
        	return EMPTY_VARIABLE_SET;

        return this.simEnv.getLocalVariables(this.stackDepth+1, 0, this.paramCount);
    }

    @Override
	public Collection<? extends Variable> getUsedVariables() {
        if (this.paramCount == 0)
            return EMPTY_VARIABLE_SET;
        if (this.usedVariables == null)
        	this.usedVariables = this.simEnv.getOpStackEntries(this.stackDepth, this.stackOffset, this.paramCount);
        return this.usedVariables;
    }

    @Override
	public Collection<? extends Variable> getUsedVariables(final Variable definedVariable) {
        // if we have no information about the executed method, we assume that all parameters had an influence on the outcome
        if (!this.hasRemovedFrame) {
            assert definedVariable instanceof StackEntry; // stack entry in the "old" frame (return value)
            return getUsedVariables();
        }

        assert definedVariable instanceof LocalVariable; // local variable in the new frame
        int varIndex = ((LocalVariable)definedVariable).getVarIndex();
        assert varIndex >= 0 && varIndex < this.paramCount;
        // it has been defined by the stack entry in the calling frame at the corresponding position
        return Collections.<Variable>singleton(this.simEnv.getOpStackEntry(this.stackDepth, this.stackOffset + varIndex));
    }

    @Override
	public boolean isCatchBlock() {
        return false;
    }

    @Override
	public Map<Long, Collection<? extends Variable>> getCreatedObjects() {
        return Collections.emptyMap();
    }

}
