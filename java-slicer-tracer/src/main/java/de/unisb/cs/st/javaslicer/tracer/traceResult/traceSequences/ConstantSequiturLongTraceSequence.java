package de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences;

import java.io.IOException;
import java.util.Iterator;
import java.util.ListIterator;

import de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences.ConstantTraceSequence.ConstantLongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.input.InputSequence;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.input.SharedInputGrammar;

public class ConstantSequiturLongTraceSequence implements ConstantLongTraceSequence {

    public class BackwardIterator implements Iterator<Long> {

        private final ListIterator<Long> it;

        private long lastValue;

        public BackwardIterator(final ListIterator<Long> it) throws IOException {
            this.it = it;
            if (!it.hasPrevious())
                throw new IOException("Illegal sequitur sequence");
            this.lastValue = it.previous();
        }

        @Override
        public boolean hasNext() {
            return this.it.hasPrevious();
        }

        @Override
        public Long next() {
            final long oldValue = this.lastValue;
            this.lastValue -= this.it.previous();
            return oldValue;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    private final InputSequence<Long> sequence;

    public ConstantSequiturLongTraceSequence(final long startRuleNumber, final SharedInputGrammar<Long> grammar) {
        this.sequence = new InputSequence<Long>(startRuleNumber, grammar);
    }

    @Override
    public Iterator<Long> backwardIterator() throws IOException {
        return new BackwardIterator(this.sequence.iterator(this.sequence.getLength()));
    }

}
