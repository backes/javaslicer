package de.unisb.cs.st.javaslicer.tracer.util;

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class SingletonIterator<T> implements ListIterator<T> {

    private boolean atBeginning = true;
    private final T value;

    public SingletonIterator(final T value) {
        this(value, true);
    }

    public SingletonIterator(final T value, final boolean atBeginning) {
        this.value = value;
        this.atBeginning = atBeginning;
    }

    @Override
    public boolean hasNext() {
        return this.atBeginning;
    }

    @Override
    public T next() {
        if (!this.atBeginning)
            throw new NoSuchElementException();
        this.atBeginning = false;
        return this.value;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(final T e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPrevious() {
        return !this.atBeginning;
    }

    @Override
    public int nextIndex() {
        return this.atBeginning ? 0 : 1;
    }

    @Override
    public T previous() {
        if (this.atBeginning)
            throw new NoSuchElementException();
        this.atBeginning = true;
        return this.value;
    }

    @Override
    public int previousIndex() {
        return this.atBeginning ? -1 : 0;
    }

    @Override
    public void set(final T e) {
        throw new UnsupportedOperationException();
    }

}
