package de.unisb.cs.st.javaslicer.variables;

import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;


public class LocalVariable implements Variable {

    private final long frame;
    private final int varIndex;
	private final ReadMethod method;

    public LocalVariable(long frame, int localVarIndex, ReadMethod method) {
        assert localVarIndex >= 0;
        this.frame = frame;
        this.varIndex = localVarIndex;
        this.method = method;
    }

    public long getFrame() {
        return this.frame;
    }

    public int getVarIndex() {
        return this.varIndex;
    }

    public String getVarName() {
        if (this.method != null) {
        	de.unisb.cs.st.javaslicer.common.classRepresentation.LocalVariable[] localVarArr = this.method.getLocalVariables();
        	if (localVarArr.length > this.varIndex && localVarArr[this.varIndex] != null)
        		return localVarArr[this.varIndex].getName();
        }
        return "unknown_var_" + this.varIndex;
    }

    @Override
    public String toString() {
        return "local["+this.frame+","+this.varIndex+" ("+getVarName()+")]";
    }

    @Override
    public int hashCode() {
        return 31*(int)this.frame + this.varIndex;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LocalVariable other = (LocalVariable) obj;
        if (this.varIndex != other.varIndex)
            return false;
        if (this.frame != other.frame)
            return false;
        return true;
    }

}
