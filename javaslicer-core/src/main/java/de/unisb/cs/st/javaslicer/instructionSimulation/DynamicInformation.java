package de.unisb.cs.st.javaslicer.instructionSimulation;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.javaslicer.variables.Variable;

public interface DynamicInformation {

    // some constants
    public static final Set<Variable> EMPTY_VARIABLE_SET = Collections.emptySet();
    public static final DynamicInformation CATCHBLOCK = new SimpleVariableUsage(EMPTY_VARIABLE_SET, EMPTY_VARIABLE_SET, true);
    public static final DynamicInformation EMPTY = new SimpleVariableUsage(EMPTY_VARIABLE_SET, EMPTY_VARIABLE_SET);

    // methods
    public Collection<? extends Variable> getUsedVariables();

    public Collection<? extends Variable> getDefinedVariables();

    public Collection<? extends Variable> getUsedVariables(Variable definedVariable);

    public Map<Long, Collection<Variable>> getCreatedObjects();

    public boolean isCatchBlock();

}
