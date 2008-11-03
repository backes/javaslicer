package de.unisb.cs.st.javaslicer;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;


public class MethodInvokationVariableUsages implements VariableUsages {

    public class UsedVariables extends AbstractList<Variable> {

        @Override
        public Variable get(final int index) {
            assert index >= 0 && index < MethodInvokationVariableUsages.this.paramCount;
            return MethodInvokationVariableUsages.this.execFrame.getStackEntry(MethodInvokationVariableUsages.this.stackOffset+index);
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
                ? MethodInvokationVariableUsages.this.removedFrame.getLocalVariable(index)
                : MethodInvokationVariableUsages.this.execFrame.getStackEntry(MethodInvokationVariableUsages.this.stackOffset+index);
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
    protected final ExecutionFrame removedFrame;
    private Collection<Variable> usedVariables;

    public MethodInvokationVariableUsages(final int stackOffset, final int paramCount,
            final int returnedSize, final ExecutionFrame execFrame, final ExecutionFrame removedFrame) {
        this.stackOffset = stackOffset;
        this.paramCount = paramCount;
        this.returnedSize = returnedSize;
        this.execFrame = execFrame;
        this.removedFrame = removedFrame;
    }

    @Override
    public Collection<? extends Variable> getDefinedVariables() {
        return this.returnedSize == 0 && this.paramCount == 0 ? EMPTY_VARIABLE_SET : new DefinedVariables();
    }

    @Override
    public Collection<Variable> getUsedVariables() {
        if (this.usedVariables == null)
            this.usedVariables = this.paramCount == 0 ? EMPTY_VARIABLE_SET : new UsedVariables();
        return this.usedVariables;
    }

    @Override
    public Collection<Variable> getUsedVariables(final Variable definedVariable) {
        return this.returnedSize == 0 ? Collections.singleton(
                (Variable)this.execFrame.getStackEntry(this.stackOffset + ((LocalVariable)definedVariable).getVarIndex()))
            : getUsedVariables();
    }

    @Override
    public boolean isCatchBlock() {
        return false;
    }

}
