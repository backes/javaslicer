/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.variables
 *    Class:     ArrayElement
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/variables/ArrayElement.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
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
