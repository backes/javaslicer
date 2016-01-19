/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.variables
 *    Class:     StackEntry
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/variables/StackEntry.java
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


public class StackEntry implements Variable {

    private final long frame;
    private final int index;

    public StackEntry(long frame, int index) {
        this.frame = frame;
        this.index = index;
    }

    public long getFrame() {
        return this.frame;
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public String toString() {
        return "stack["+this.frame+","+this.index+"]";
    }

    @Override
    public int hashCode() {
        return (int)(31 * this.frame) + this.index;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StackEntry other = (StackEntry) obj;
        if (this.index != other.index)
            return false;
        if (this.frame != other.frame)
            return false;
        return true;
    }

}
