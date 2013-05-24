/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.instructionSimulation
 *    Class:     ArrayElementsList
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/instructionSimulation/ArrayElementsList.java
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
