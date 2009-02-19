package de.unisb.cs.st.javaslicer.variables;

public class ObjectField implements Variable {

    private final long objectId;
    private final String fieldName;

    public ObjectField(final long objectId, final String fieldName) {
        this.objectId = objectId;
        this.fieldName = fieldName;
    }

    public long getObjectId() {
        return this.objectId;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    @Override
    public String toString() {
        return "field["+this.objectId+","+this.fieldName+"]";
    }

    @Override
    public int hashCode() {
        // the fieldName strings are internalized, so we can use identity comparison
        return this.fieldName.hashCode() + (int)this.objectId;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ObjectField other = (ObjectField) obj;
        // the fieldName strings are internalized, so we can use identity comparison
        if (this.fieldName != other.fieldName) {
            assert this.fieldName != null && !this.fieldName.equals(other.fieldName);
            return false;
        }
        if (this.objectId != other.objectId)
            return false;
        return true;
    }

}
