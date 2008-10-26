package de.unisb.cs.st.javaslicer;

public class ArrayElement implements Variable {

    private final long arrayId;
    private final int arrayIndex;

    public ArrayElement(final long arrayId, final int arrayIndex) {
        this.arrayId = arrayId;
        this.arrayIndex = arrayIndex;
    }

    @Override
    public String toString() {
        return "array["+this.arrayId+","+this.arrayIndex+"]";
    }

    @Override
    public int hashCode() {
        return (int)this.arrayId + this.arrayIndex;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ArrayElement other = (ArrayElement) obj;
        if (this.arrayId != other.arrayId)
            return false;
        if (this.arrayIndex != other.arrayIndex)
            return false;
        return true;
    }

}
