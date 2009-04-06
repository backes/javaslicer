package de.unisb.cs.st.javaslicer.variables;

import de.unisb.cs.st.javaslicer.instructionSimulation.ExecutionFrame;

public class StackEntry<InstanceType> implements Variable {

    private final ExecutionFrame<InstanceType> frame;
    private final int index;

    public StackEntry(final ExecutionFrame<InstanceType> frame, final int index) {
        assert index >= 0;
        this.frame = frame;
        this.index = index;
    }

    public ExecutionFrame<InstanceType> getFrame() {
        return this.frame;
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public String toString() {
        return "stack["+this.frame.frameNr+","+this.index+"]";
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
        final StackEntry<?> other = (StackEntry<?>) obj;
        if (!this.frame.equals(other.frame))
            return false;
        if (this.index != other.index)
            return false;
        return true;
    }

}
