/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.instructionSimulation
 *    Class:     ReadSingleValueVariableUsage
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/instructionSimulation/ReadSingleValueVariableUsage.java
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
