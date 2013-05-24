/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.variables
 *    Class:     StaticField
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/variables/StaticField.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
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
