package de.unisb.cs.st.javaslicer.variables;

import de.unisb.cs.st.javaslicer.instructionSimulation.ExecutionFrame;

public class LocalVariable<InstanceType> implements Variable {

    private final ExecutionFrame<InstanceType> frame;
    private final int varIndex;

    public LocalVariable(final ExecutionFrame<InstanceType> executionFrame, final int localVarIndex) {
        assert localVarIndex >= 0;
        this.frame = executionFrame;
        this.varIndex = localVarIndex;
    }

    public ExecutionFrame<InstanceType> getFrame() {
        return this.frame;
    }

    public int getVarIndex() {
        return this.varIndex;
    }

    public String getVarName() {
        if (this.frame != null && this.frame.method != null) {
            for (de.unisb.cs.st.javaslicer.common.classRepresentation.LocalVariable
                    var: this.frame.method.getLocalVariables()) {
                if (var.getIndex() == this.varIndex)
                    return var.getName();
            }
        }
        return "unknown_var_" + this.varIndex;
    }

    @Override
    public String toString() {
        return "local["+this.frame.frameNr+","+this.varIndex+" ("+getVarName()+")]";
    }

    @Override
    public int hashCode() {
        return this.frame.hashCode() + this.varIndex;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final LocalVariable<?> other = (LocalVariable<?>) obj;
        if (!this.frame.equals(other.frame))
            return false;
        if (this.varIndex != other.varIndex)
            return false;
        return true;
    }

}
