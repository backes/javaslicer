package de.unisb.cs.st.javaslicer.traceResult.traceSequences;

import java.io.IOException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import de.hammacher.util.iterators.IntArrayIterator;
import de.hammacher.util.iterators.ReverseIntArrayIterator;
import de.hammacher.util.iterators.SingletonIterator;
import de.unisb.cs.st.javaslicer.traceResult.traceSequences.ConstantTraceSequence.ConstantIntegerTraceSequence;
import de.unisb.cs.st.sequitur.input.InputSequence;

public class ConstantSequiturIntegerTraceSequence implements ConstantIntegerTraceSequence {

    private static class BackwardIterator implements Iterator<Integer> {

        private final ListIterator<Integer> it;
        private int lastValue;
        private int count;

        public BackwardIterator(final ListIterator<Integer> it, final int count) throws IOException {
            if (count < 0)
                throw new IOException("Illegal sequitur sequence (count < 0)");
            this.count = count;
            this.it = it;
            if (!it.hasPrevious())
                throw new IOException("Illegal sequitur sequence");
            this.lastValue = it.previous();
        }

        public boolean hasNext() {
            return this.count != 0 && this.it.hasPrevious();
        }

        public Integer next() {
            if (this.count == 0)
                throw new NoSuchElementException();
            final int oldValue = this.lastValue;
            this.lastValue -= this.it.previous();
            --this.count;
            return oldValue;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    private static class ForwardIterator implements ListIterator<Integer> {

        private final ListIterator<Integer> it;
        private int lastValue;
        private final int count;
        private int pos;

        public ForwardIterator(final ListIterator<Integer> it, final int count) throws IOException {
            if (count < 0)
                throw new IOException("Illegal sequitur sequence (count < 0)");
            this.count = count;
            this.pos = 0;
            this.it = it;
            this.lastValue = 0;
        }

        public boolean hasNext() {
            return this.pos != this.count && this.it.hasNext();
        }

        public Integer next() {
            if (!hasNext())
                throw new NoSuchElementException();
            this.lastValue += this.it.next();
            ++this.pos;
            return this.lastValue;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void add(final Integer e) {
            throw new UnsupportedOperationException();
        }

        public boolean hasPrevious() {
            return this.pos != 0 && this.it.hasPrevious();
        }

        public int nextIndex() {
            return this.pos;
        }

        public Integer previous() {
            if (!hasPrevious())
                throw new NoSuchElementException();
            final int ret = this.lastValue;
            this.lastValue -= this.it.previous();
            --this.pos;
            return ret;
        }

        public int previousIndex() {
            return this.pos - 1;
        }

        public void set(final Integer e) {
            throw new UnsupportedOperationException();
        }

    }

    private final long offset;
    private final int count;
    private final InputSequence<Integer> sequence;

    public ConstantSequiturIntegerTraceSequence(final long offset,
            final int count, final InputSequence<Integer> inputSequence) {
        this.offset = offset;
        this.count = count;
        this.sequence = inputSequence;
    }

    public Iterator<Integer> backwardIterator() throws IOException {
        if (this.count <= 10) {
            if (this.count == 1)
                return new SingletonIterator<Integer>(this.sequence.iterator(this.offset).next());
            final int[] values = new int[this.count];
            final ListIterator<Integer> it = this.sequence.iterator(this.offset);
            int last = 0;
            for (int i = 0; i < this.count; ++i) {
                values[i] = last += it.next();
            }
            return new ReverseIntArrayIterator(values);
        }
        return new BackwardIterator(this.sequence.iterator(this.offset+this.count+1), this.count);
    }

    public ListIterator<Integer> iterator() throws IOException {
        if (this.count <= 10) {
            if (this.count == 1)
                return new SingletonIterator<Integer>(this.sequence.iterator(this.offset).next());
            final int[] values = new int[this.count];
            final ListIterator<Integer> it = this.sequence.iterator(this.offset);
            int last = 0;
            for (int i = 0; i < this.count; ++i) {
                values[i] = last += it.next();
            }
            return new IntArrayIterator(values);
        }
        return new ForwardIterator(this.sequence.iterator(this.offset), this.count);
    }

}
