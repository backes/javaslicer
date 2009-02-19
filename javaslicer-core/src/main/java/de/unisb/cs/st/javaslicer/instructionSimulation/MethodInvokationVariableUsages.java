package de.unisb.cs.st.javaslicer.instructionSimulation;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import de.unisb.cs.st.javaslicer.variables.LocalVariable;
import de.unisb.cs.st.javaslicer.variables.StackEntry;
import de.unisb.cs.st.javaslicer.variables.Variable;


public class MethodInvokationVariableUsages implements DynamicInformation {

    public class UsedVariables extends AbstractList<Variable> {

        @Override
        public Variable get(final int index) {
            assert index >= 0 && index < size();
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
            return MethodInvokationVariableUsages.this.removedFrame.getLocalVariable(index);
        }

        @Override
        public int size() {
            return MethodInvokationVariableUsages.this.paramCount;
        }

    }

    protected final int stackOffset;
    protected final int paramCount;
    protected final ExecutionFrame execFrame;
    protected final ExecutionFrame removedFrame;
    protected Collection<Variable> usedVariables;
    protected final boolean hasReturn;

    public MethodInvokationVariableUsages(final int stackOffset, final int paramCount,
            boolean hasReturn, final ExecutionFrame execFrame, final ExecutionFrame removedFrame) {
        assert stackOffset >= 0 || execFrame.abnormalTermination;
        this.stackOffset = stackOffset;
        this.paramCount = paramCount;
        this.hasReturn = hasReturn;
        this.execFrame = execFrame;
        this.removedFrame = removedFrame;
    }

    public Collection<? extends Variable> getDefinedVariables() {
        // if we have no removedFrame, then the defined variable is the return value.
        // it is sufficient to take the lower variable of double sized values (long & double).
        if (this.removedFrame == null)
            return this.hasReturn ? Collections.singleton(this.execFrame.getStackEntry(this.stackOffset)) : EMPTY_VARIABLE_SET;

        // if the method has no parameters (and is static), then we can just return an empty set
        return this.paramCount == 0 ? EMPTY_VARIABLE_SET : new DefinedVariables();
    }

    public Collection<Variable> getUsedVariables() {
        if (this.paramCount == 0)
            return EMPTY_VARIABLE_SET;
        if (this.usedVariables == null)
            this.usedVariables = new UsedVariables();
        return this.usedVariables;
    }

    public Collection<Variable> getUsedVariables(final Variable definedVariable) {
        // if we have no information about the executed method, we assume that all parameters had an influence on the outcome
        if (this.removedFrame == null) {
            assert definedVariable instanceof StackEntry; // stack entry in the "old" frame (return value)
            return getUsedVariables();
        }

        assert definedVariable instanceof LocalVariable; // local variable in the new frame
        int varIndex = ((LocalVariable)definedVariable).getVarIndex();
        assert varIndex < this.paramCount;
        // it has been defined by the stack entry in the old frame at the corresponding position
        return Collections.singleton((Variable)this.execFrame.getStackEntry(this.stackOffset + varIndex));
    }

    public boolean isCatchBlock() {
        return false;
    }

    public Map<Long, Collection<Variable>> getCreatedObjects() {
        return Collections.emptyMap();
    }

}
