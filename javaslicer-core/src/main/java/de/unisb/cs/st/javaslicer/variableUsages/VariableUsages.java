package de.unisb.cs.st.javaslicer.variableUsages;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import de.unisb.cs.st.javaslicer.variables.Variable;

public interface VariableUsages {

    public static final Set<Variable> EMPTY_VARIABLE_SET = Collections.emptySet();
    public static final VariableUsages CATCHBLOCK = new SimpleVariableUsage(EMPTY_VARIABLE_SET, EMPTY_VARIABLE_SET, true);
    public static final VariableUsages EMPTY = new SimpleVariableUsage(EMPTY_VARIABLE_SET, EMPTY_VARIABLE_SET);

    public Collection<? extends Variable> getUsedVariables();

    public Collection<? extends Variable> getDefinedVariables();

    public Collection<? extends Variable> getUsedVariables(Variable definedVariable);

    public Collection<Long> getCreatedObjects();

    public boolean isCatchBlock();

}
