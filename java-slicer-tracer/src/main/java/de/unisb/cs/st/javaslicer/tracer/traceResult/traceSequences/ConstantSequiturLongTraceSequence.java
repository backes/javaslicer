package de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences;

import java.io.IOException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences.ConstantTraceSequence.ConstantLongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.ReverseLongArrayIterator;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.input.InputSequence;

public class ConstantSequiturLongTraceSequence implements ConstantLongTraceSequence {

    private static class BackwardIterator implements Iterator<Long> {

        private final ListIterator<Long> it;
        private long lastValue;
        private int count;

        public BackwardIterator(final ListIterator<Long> it, final int count) throws IOException {
            if (count < 0)
                throw new IOException("Illegal sequitur sequence (count < 0)");
            this.count = count;
            this.it = it;
            if (!it.hasPrevious())
                throw new IOException("Illegal sequitur sequence");
            this.lastValue = it.previous();
        }

        @Override
        public boolean hasNext() {
            return this.count != 0 && this.it.hasPrevious();
        }

        @Override
        public Long next() {
            if (this.count == 0)
                throw new NoSuchElementException();
            final long oldValue = this.lastValue;
            this.lastValue -= this.it.previous();
            --this.count;
            return oldValue;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    private final long offset;
    private final int count;
    private final InputSequence<Long> sequence;

    public ConstantSequiturLongTraceSequence(final long offset, final int count,
            final InputSequence<Long> inputSequence) {
        this.offset = offset;
        this.count = count;
        this.sequence = inputSequence;
    }

    @Override
    public Iterator<Long> backwardIterator() throws IOException {
        if (this.count <= 10) {
            final long[] values = new long[this.count];
            final ListIterator<Long> it = this.sequence.iterator(this.offset);
            int last = 0;
            for (int i = 0; i < this.count; ++i) {
                values[i] = last += it.next();
            }
            return new ReverseLongArrayIterator(values);
        }
        return new BackwardIterator(this.sequence.iterator(this.offset+this.count+1), this.count);
    }

}
