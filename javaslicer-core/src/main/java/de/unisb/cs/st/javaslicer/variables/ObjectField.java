/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.variables
 *    Class:     ObjectField
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/variables/ObjectField.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
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
        return 31*this.fieldName.hashCode() + (int)this.objectId;
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
        if (this.objectId != other.objectId)
            return false;
        // the fieldName strings are internalized, so we can use identity comparison
        if (this.fieldName != other.fieldName) {
            assert this.fieldName != null && !this.fieldName.equals(other.fieldName);
            return false;
        }
        return true;
    }

}
