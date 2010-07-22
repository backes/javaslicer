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
