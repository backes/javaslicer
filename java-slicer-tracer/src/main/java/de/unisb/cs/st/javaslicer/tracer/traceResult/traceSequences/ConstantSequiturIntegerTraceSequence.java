package de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences;

import java.util.Iterator;
import java.util.ListIterator;

import de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences.ConstantTraceSequence.ConstantIntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.input.InputSequence;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.input.SharedInputGrammar;

public class ConstantSequiturIntegerTraceSequence implements ConstantIntegerTraceSequence {

    public class BackwardIterator implements Iterator<Integer> {

        private final ListIterator<Integer> it;

        public BackwardIterator(final ListIterator<Integer> it) {
            this.it = it;
        }

        @Override
        public boolean hasNext() {
            return this.it.hasPrevious();
        }

        @Override
        public Integer next() {
            return this.it.previous();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    private final InputSequence<Integer> sequence;

    public ConstantSequiturIntegerTraceSequence(final long startRuleNumber, final SharedInputGrammar<Integer> grammar) {
        this.sequence = new InputSequence<Integer>(startRuleNumber, grammar);
    }

    @Override
    public Iterator<Integer> backwardIterator() {
        return new BackwardIterator(this.sequence.iterator(this.sequence.getLength()));
    }

}
