/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.instructionSimulation
 *    Class:     ObjectFieldList
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/instructionSimulation/ObjectFieldList.java
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
