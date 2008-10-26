package de.unisb.cs.st.javaslicer;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;

import de.unisb.cs.st.javaslicer.ExecutionFrame.LocalVariable;

public class MethodInvokationVariableUsages implements VariableUsages {

    public class UsedVariables extends AbstractList<Variable> {

        @Override
        public Variable get(final int index) {
            assert index >= 0 && index < MethodInvokationVariableUsages.this.paramCount;
            return new StackEntry(MethodInvokationVariableUsages.this.stackOffset+index);
        }

        @Override
        public int size() {
            return MethodInvokationVariableUsages.this.paramCount;
        }

    }

    public class DefinedVariables extends AbstractList<Variable> {

        @Override
        public Variable get(final int index) {
            assert index >= 0 && index < size();
            return MethodInvokationVariableUsages.this.returnedSize == 0
                ? MethodInvokationVariableUsages.this.execFrame.getLocalVariable(index)
                : new StackEntry(MethodInvokationVariableUsages.this.stackOffset+index);
        }

        @Override
        public int size() {
            return MethodInvokationVariableUsages.this.returnedSize == 0 ? MethodInvokationVariableUsages.this.paramCount
                : MethodInvokationVariableUsages.this.returnedSize;
        }

    }

    protected final int stackOffset;
    protected final int paramCount;
    protected final int returnedSize;
    protected final ExecutionFrame execFrame;
    private Collection<Variable> usedVariables;

    public MethodInvokationVariableUsages(final int stackOffset, final int paramCount,
            final int returnedSize, final ExecutionFrame execFrame) {
        this.stackOffset = stackOffset;
        this.paramCount = paramCount;
        this.returnedSize = returnedSize;
        this.execFrame = execFrame;
    }

    @Override
    public Collection<? extends Variable> getDefinedVariables() {
        return this.execFrame == null && this.paramCount == 0 ? EMPTY_VARIABLE_SET : new DefinedVariables();
    }

    @Override
    public Collection<Variable> getUsedVariables() {
        if (this.usedVariables == null)
            this.usedVariables = this.paramCount == 0 ? EMPTY_VARIABLE_SET : new UsedVariables();
        return this.usedVariables;
    }

    @Override
    public Collection<Variable> getUsedVariables(final Variable definedVariable) {
        return this.execFrame != null ? getUsedVariables() : Collections.singleton(
                (Variable)new StackEntry(this.stackOffset + ((LocalVariable)definedVariable).getVarIndex()-1));
    }

}
