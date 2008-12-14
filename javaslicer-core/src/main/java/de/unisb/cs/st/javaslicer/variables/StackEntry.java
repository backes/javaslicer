package de.unisb.cs.st.javaslicer.variables;

import de.unisb.cs.st.javaslicer.dependencyAnalysis.ExecutionFrame;

public class StackEntry implements Variable {

    private final ExecutionFrame frame;
    private final int index;

    public StackEntry(final ExecutionFrame frame, final int index) {
        this.frame = frame;
        this.index = index;
    }

    @Override
    public String toString() {
        return "stack["+this.frame.hashCode()+","+this.index+"]";
    }

    @Override
    public int hashCode() {
        return this.frame.hashCode() + this.index;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final StackEntry other = (StackEntry) obj;
        if (!this.frame.equals(other.frame))
            return false;
        if (this.index != other.index)
            return false;
        return true;
    }

}
