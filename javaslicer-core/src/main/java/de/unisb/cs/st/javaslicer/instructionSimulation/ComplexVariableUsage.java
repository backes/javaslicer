/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.instructionSimulation
 *    Class:     ComplexVariableUsage
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/instructionSimulation/ComplexVariableUsage.java
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

public class ComplexVariableUsage implements DynamicInformation {

    private final Collection<Variable> allUsedVariables;
    private final Map<Variable, Collection<Variable>> definedVariablesAndDependences;

    public ComplexVariableUsage(final Collection<Variable> allUsedVariables,
            final Map<Variable, Collection<Variable>> definedVariablesAndDependences) {
        this.allUsedVariables = allUsedVariables;
        this.definedVariablesAndDependences = definedVariablesAndDependences;
        assert allSubsets(definedVariablesAndDependences.values(), allUsedVariables);
    }

    private boolean allSubsets(final Collection<Collection<Variable>> sets, final Collection<Variable> superSet) {
        for (final Collection<Variable> set: sets)
            if (!superSet.containsAll(set))
                return false;
        return true;
    }

    @Override
	public Collection<Variable> getDefinedVariables() {
        return this.definedVariablesAndDependences.keySet();
    }

    @Override
	public Collection<Variable> getUsedVariables() {
        return this.allUsedVariables;
    }

    @Override
	public Collection<Variable> getUsedVariables(final Variable definedVariable) {
        assert this.definedVariablesAndDependences.containsKey(definedVariable);
        return this.definedVariablesAndDependences.get(definedVariable);
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
