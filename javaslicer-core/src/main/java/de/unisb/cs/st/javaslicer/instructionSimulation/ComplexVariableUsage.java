/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.instructionSimulation
 *    Class:     ComplexVariableUsage
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/instructionSimulation/ComplexVariableUsage.java
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
