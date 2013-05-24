/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.variables
 *    Class:     ObjectField
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/variables/ObjectField.java
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
