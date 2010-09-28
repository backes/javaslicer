/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.variables
 *    Class:     StaticField
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/variables/StaticField.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
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
