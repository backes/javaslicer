package de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences;

import java.util.Iterator;

public interface ConstantTraceSequence {

    public interface ConstantIntegerTraceSequence extends ConstantTraceSequence {

        public abstract Iterator<Integer> backwardIterator();

    }

    public interface ConstantLongTraceSequence extends ConstantTraceSequence {

        public abstract Iterator<Long> backwardIterator();

    }

}
