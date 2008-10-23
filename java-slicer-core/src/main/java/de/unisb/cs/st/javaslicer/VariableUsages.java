package de.unisb.cs.st.javaslicer;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public interface VariableUsages {

    public static class SimpleVariableUsage implements VariableUsages {

        private final Collection<Variable> usedVariables;
        private final Collection<Variable> definedVariables;

        public SimpleVariableUsage(final Collection<Variable> usedVariables, final Collection<Variable> definedVariables) {
            this.usedVariables = usedVariables;
            this.definedVariables = definedVariables;
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

    }

    public static final Set<Variable> EMPTY_VARIABLE_SET = Collections.emptySet();
    public static final VariableUsages EMPTY = new SimpleVariableUsage(EMPTY_VARIABLE_SET, EMPTY_VARIABLE_SET);

    public Collection<Variable> getUsedVariables();

    public Collection<Variable> getDefinedVariables();

    public Collection<Variable> getUsedVariables(Variable definedVariable);

}
