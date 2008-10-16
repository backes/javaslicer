package de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences;

import java.util.Iterator;
import java.util.ListIterator;

import de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences.ConstantTraceSequence.ConstantLongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.input.InputSequence;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.input.SharedInputGrammar;

public class ConstantSequiturLongTraceSequence implements ConstantLongTraceSequence {

    public class BackwardIterator implements Iterator<Long> {

        private final ListIterator<Long> it;

        public BackwardIterator(final ListIterator<Long> it) {
            this.it = it;
        }

        @Override
        public boolean hasNext() {
            return this.it.hasPrevious();
        }

        @Override
        public Long next() {
            return this.it.previous();
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
    public Iterator<Long> backwardIterator() {
        return new BackwardIterator(this.sequence.iterator(this.sequence.getLength()));
    }

}
