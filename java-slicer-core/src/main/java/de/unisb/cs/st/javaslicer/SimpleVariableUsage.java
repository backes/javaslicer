package de.unisb.cs.st.javaslicer;

import java.util.Collection;
import java.util.Collections;

public class SimpleVariableUsage implements VariableUsages {

    private final Collection<Variable> usedVariables;
    private final Collection<Variable> definedVariables;

    public SimpleVariableUsage(final Collection<Variable> usedVariables, final Collection<Variable> definedVariables) {
        this.usedVariables = usedVariables;
        this.definedVariables = definedVariables;
    }

    public SimpleVariableUsage(final Variable usedVariable, final Variable definedVariable) {
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

    @Override
    public Collection<Variable> getUsedVariables(final Variable definedVariable) {
        return this.usedVariables;
    }

    @Override
    public String toString() {
        return "used:    "+getUsedVariables()+System.getProperty("line.separator")
            +"defined: "+getDefinedVariables();
    }

}