package de.unisb.cs.st.javaslicer.variables;


public class StaticField implements Variable {

    private final String ownerInternalClassName;
    private final String fieldName;

    public StaticField(String ownerInternalClassName, String fieldName) {
        this.ownerInternalClassName = ownerInternalClassName;
        this.fieldName = fieldName;
    }


    public String getOwnerInternalClassName() {
        return this.ownerInternalClassName;
    }


    public String getFieldName() {
        return this.fieldName;
    }

    @Override
    public String toString() {
        return "staticfield["+this.ownerInternalClassName+","+this.fieldName+"]";
    }

    @Override
    public int hashCode() {
        return this.ownerInternalClassName.hashCode() + this.fieldName.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final StaticField other = (StaticField) obj;
        // the owner and fieldName strings are internalized, so we can use identity comparison
        if (this.fieldName != other.fieldName) {
            assert this.fieldName != null && !this.fieldName.equals(other.fieldName);
            return false;
        }
        if (this.ownerInternalClassName != other.ownerInternalClassName) {
            assert this.ownerInternalClassName != null && !this.ownerInternalClassName.equals(other.ownerInternalClassName);
            return false;
        }
        return true;
    }

}
