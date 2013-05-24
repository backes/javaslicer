/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.instructionSimulation
 *    Class:     ObjectFieldList
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/instructionSimulation/ObjectFieldList.java
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
import java.util.NoSuchElementException;

import de.unisb.cs.st.javaslicer.variables.ObjectField;


public class ObjectFieldList extends AbstractList<ObjectField> {

    private final long objectId;
    private final String[] fieldNames;

    public ObjectFieldList(long objId, String[] fieldNames) {
        this.objectId = objId;
        this.fieldNames = fieldNames;
    }

    @Override
    public ObjectField get(int index) {
        if (index < 0 || index >= this.fieldNames.length)
            throw new NoSuchElementException();
        return new ObjectField(this.objectId, this.fieldNames[index]);
    }

    @Override
    public int size() {
        return this.fieldNames.length;
    }

}
