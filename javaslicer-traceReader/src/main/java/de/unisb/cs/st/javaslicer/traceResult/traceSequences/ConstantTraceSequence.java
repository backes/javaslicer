package de.unisb.cs.st.javaslicer.traceResult.traceSequences;

import java.io.IOException;
import java.util.Iterator;
import java.util.ListIterator;

public interface ConstantTraceSequence {

    public interface ConstantIntegerTraceSequence extends ConstantTraceSequence {

        ListIterator<Integer> iterator() throws IOException;
        Iterator<Integer> backwardIterator() throws IOException;

    }

    public interface ConstantLongTraceSequence extends ConstantTraceSequence {

        ListIterator<Long> iterator() throws IOException;
        Iterator<Long> backwardIterator() throws IOException;

    }

}
