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

    public Collection<Variable> getUsedVariables() {
        return Collections.singleton(this.usedVariable);
    }

    public Collection<Variable> getDefinedVariables() {
        return Collections.emptySet();
    }

    public boolean isCatchBlock() {
        return false;
    }

    public Collection<Variable> getUsedVariables(final Variable definedVariable) {
    	assert (false);
        return getUsedVariables();
    }

    @Override
    public String toString() {
        return "used:    "+this.usedVariable+System.getProperty("line.separator")
            +"defined: none";
    }
    public Map<Long, Collection<Variable>> getCreatedObjects() {
        return Collections.emptyMap();
    }

}
