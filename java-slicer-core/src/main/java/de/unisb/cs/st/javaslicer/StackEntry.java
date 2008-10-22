package de.unisb.cs.st.javaslicer;

public class StackEntry implements Variable {

    private final int index;

    public StackEntry(final int index) {
        this.index = index;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.index;
        return result;
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
        if (this.index != other.index)
            return false;
        return true;
    }

}
