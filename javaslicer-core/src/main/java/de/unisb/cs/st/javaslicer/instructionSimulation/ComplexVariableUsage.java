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

    public Collection<Variable> getDefinedVariables() {
        return this.definedVariablesAndDependences.keySet();
    }

    public Collection<Variable> getUsedVariables() {
        return this.allUsedVariables;
    }

    public Collection<Variable> getUsedVariables(final Variable definedVariable) {
        assert this.definedVariablesAndDependences.containsKey(definedVariable);
        return this.definedVariablesAndDependences.get(definedVariable);
    }

    public boolean isCatchBlock() {
        return false;
    }

    public Map<Long, Collection<Variable>> getCreatedObjects() {
        return Collections.emptyMap();
    }

}
