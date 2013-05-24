/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.variables
 *    Class:     ArrayElement
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/variables/ArrayElement.java
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

public class ArrayElement implements Variable {

    private final long arrayId;
    private final int arrayIndex;

    public ArrayElement(final long arrayId, final int arrayIndex) {
        this.arrayId = arrayId;
        this.arrayIndex = arrayIndex;
    }

    public long getArrayId() {
        return this.arrayId;
    }

    public int getArrayIndex() {
        return this.arrayIndex;
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
