/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.variables
 *    Class:     StackEntry
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/variables/StackEntry.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
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
        return (int)this.frame + this.index;
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
