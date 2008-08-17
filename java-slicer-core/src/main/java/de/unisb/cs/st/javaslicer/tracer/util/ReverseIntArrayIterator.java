package de.unisb.cs.st.javaslicer.tracer.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ReverseIntArrayIterator implements Iterator<Integer> {

    private final int[] data;
    private int index;

    public ReverseIntArrayIterator(final int[] data) {
        this.data = data;
        this.index = data.length-1;
    }

    public boolean hasNext() {
        return this.index >= 0;
    }

    public Integer next() {
        if (!hasNext())
            throw new NoSuchElementException();
        return this.data[this.index--];
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}
