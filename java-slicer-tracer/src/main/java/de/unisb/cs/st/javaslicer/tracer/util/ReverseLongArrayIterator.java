package de.unisb.cs.st.javaslicer.tracer.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ReverseLongArrayIterator implements Iterator<Long> {

    private final long[] data;
    private int index = 0;

    public ReverseLongArrayIterator(final long[] data) {
        this.data = data;
    }

    public boolean hasNext() {
        return this.index < this.data.length;
    }

    public Long next() {
        if (!hasNext())
            throw new NoSuchElementException();
        return this.data[this.index++];
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}
