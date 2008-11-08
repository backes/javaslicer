package de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences;

import java.io.IOException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences.ConstantTraceSequence.ConstantIntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.input.InputSequence;

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

        @Override
        public boolean hasNext() {
            return this.count != 0 && this.it.hasPrevious();
        }

        @Override
        public Integer next() {
            if (this.count == 0)
                throw new NoSuchElementException();
            final int oldValue = this.lastValue;
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
    private final InputSequence<Integer> sequence;

    public ConstantSequiturIntegerTraceSequence(final long offset,
            final int count, final InputSequence<Integer> inputSequence) {
        this.offset = offset;
        this.count = count;
        this.sequence = inputSequence;
    }

    @Override
    public Iterator<Integer> backwardIterator() throws IOException {
        return new BackwardIterator(this.sequence.iterator(this.offset+this.count+1), this.count);
    }

}
