package de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences;

import java.io.IOException;
import java.util.Iterator;

public interface ConstantTraceSequence {

    public interface ConstantIntegerTraceSequence extends ConstantTraceSequence {

        public abstract Iterator<Integer> iterator() throws IOException;
        public abstract Iterator<Integer> backwardIterator() throws IOException;

    }

    public interface ConstantLongTraceSequence extends ConstantTraceSequence {

        public abstract Iterator<Long> iterator() throws IOException;
        public abstract Iterator<Long> backwardIterator() throws IOException;

    }

}
