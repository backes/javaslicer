/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.instructionSimulation
 *    Class:     ArrayElementsList
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/instructionSimulation/ArrayElementsList.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.instructionSimulation;

import java.util.AbstractList;

import de.unisb.cs.st.javaslicer.variables.ArrayElement;


public class ArrayElementsList extends AbstractList<ArrayElement> {

    private final int numArrayElems;
    private final long arrayId;

    public ArrayElementsList(final int numArrayElems, final long arrayId) {
        this.numArrayElems = numArrayElems;
        this.arrayId = arrayId;
    }

    @Override
    public ArrayElement get(final int index) {
        if (index < 0 || index >= this.numArrayElems)
            throw new IndexOutOfBoundsException("index: " + index + "; size: " + size());
        return new ArrayElement(this.arrayId, index);
    }

    @Override
    public int size() {
        return this.numArrayElems;
    }

}
