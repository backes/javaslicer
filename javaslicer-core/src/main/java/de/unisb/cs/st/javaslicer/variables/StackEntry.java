package de.unisb.cs.st.javaslicer.variables;


public class StackEntry implements Variable {

    private final long frame;
    private final int index;

    public StackEntry(long frame, int index) {
        this.frame = frame;
        this.index = index;
    }

    public long getFrame() {
        return this.frame;
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public String toString() {
        return "stack["+this.frame+","+this.index+"]";
    }

    @Override
    public int hashCode() {
        return (int)this.frame + this.index;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StackEntry other = (StackEntry) obj;
        if (this.index != other.index)
            return false;
        if (this.frame != other.frame)
            return false;
        return true;
    }

}
