package de.unisb.cs.st.javaslicer.traceResult.traceSequences;

import java.io.IOException;
import java.util.Iterator;
import java.util.ListIterator;

public interface ConstantTraceSequence {

    public interface ConstantIntegerTraceSequence extends ConstantTraceSequence {

        public abstract ListIterator<Integer> iterator() throws IOException;
        public abstract Iterator<Integer> backwardIterator() throws IOException;

    }

    public interface ConstantLongTraceSequence extends ConstantTraceSequence {

        public abstract ListIterator<Long> iterator() throws IOException;
        public abstract Iterator<Long> backwardIterator() throws IOException;

    }

}
