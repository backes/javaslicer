package de.unisb.cs.st.javaslicer.instructionSimulation;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import de.unisb.cs.st.javaslicer.variables.Variable;

public class SimpleVariableUsage implements DynamicInformation {

    private final Collection<Variable> usedVariables;
    private final Collection<Variable> definedVariables;
    private final boolean isCatchBlock;
    private final Map<Long, Collection<Variable>> createdObjects;

    private static final Map<Long, Collection<Variable>> noCreatedObjects = Collections.emptyMap();

    public SimpleVariableUsage(final Collection<Variable> usedVariables,
            final Collection<Variable> definedVariables) {
        this(usedVariables, definedVariables, noCreatedObjects);
    }
    public SimpleVariableUsage(final Collection<Variable> usedVariables,
            final Collection<Variable> definedVariables,
            final Map<Long, Collection<Variable>> createdObjects) {
        this(usedVariables, definedVariables, false, createdObjects);
    }

    public SimpleVariableUsage(final Collection<Variable> usedVariables,
            final Collection<Variable> definedVariables, final boolean isCatchBlock) {
        this(usedVariables, definedVariables, isCatchBlock, noCreatedObjects);
    }
    public SimpleVariableUsage(final Collection<Variable> usedVariables,
            final Collection<Variable> definedVariables, final boolean isCatchBlock,
            final Map<Long, Collection<Variable>> createdObjects) {
        this.usedVariables = usedVariables;
        this.definedVariables = definedVariables;
        this.isCatchBlock = isCatchBlock;
        this.createdObjects = createdObjects;
    }

    public SimpleVariableUsage(final Variable usedVariable,
            final Variable definedVariable) {
        this(Collections.singleton(usedVariable), Collections.singleton(definedVariable));
    }

    public SimpleVariableUsage(final Collection<Variable> usedVariables, final Variable definedVariable) {
        this(usedVariables, Collections.singleton(definedVariable));
    }

    public SimpleVariableUsage(final Variable usedVariable, final Collection<Variable> definedVariables) {
        this(Collections.singleton(usedVariable), definedVariables);
    }

    public Collection<Variable> getUsedVariables() {
        return this.usedVariables;
    }

    public Collection<Variable> getDefinedVariables() {
        return this.definedVariables;
    }

    public boolean isCatchBlock() {
        return this.isCatchBlock;
    }

    public Collection<Variable> getUsedVariables(final Variable definedVariable) {
        return this.usedVariables;
    }

    @Override
    public String toString() {
        return "used:    "+getUsedVariables()+System.getProperty("line.separator")
            +"defined: "+getDefinedVariables();
    }
    public Map<Long, Collection<Variable>> getCreatedObjects() {
        return this.createdObjects;
    }

}
