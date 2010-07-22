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
