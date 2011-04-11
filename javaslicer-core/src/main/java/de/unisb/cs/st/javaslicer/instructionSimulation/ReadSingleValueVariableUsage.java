/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.instructionSimulation
 *    Class:     ReadSingleValueVariableUsage
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/instructionSimulation/ReadSingleValueVariableUsage.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.instructionSimulation;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import de.unisb.cs.st.javaslicer.variables.Variable;

public class ReadSingleValueVariableUsage implements DynamicInformation {

    private final Variable usedVariable;

    public ReadSingleValueVariableUsage(Variable usedVariable) {
    	this.usedVariable = usedVariable;
    }

    @Override
	public Collection<Variable> getUsedVariables() {
        return Collections.singleton(this.usedVariable);
    }

    @Override
	public Collection<Variable> getDefinedVariables() {
        return Collections.emptySet();
    }

    @Override
	public boolean isCatchBlock() {
        return false;
    }

    @Override
	public Collection<Variable> getUsedVariables(final Variable definedVariable) {
    	assert (false);
        return getUsedVariables();
    }

    @Override
    public String toString() {
        return "used:    "+this.usedVariable+System.getProperty("line.separator")
            +"defined: none";
    }
    @Override
	public Map<Long, Collection<? extends Variable>> getCreatedObjects() {
        return Collections.emptyMap();
    }

}
