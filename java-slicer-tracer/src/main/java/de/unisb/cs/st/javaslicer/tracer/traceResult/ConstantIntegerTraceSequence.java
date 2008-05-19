package de.unisb.cs.st.javaslicer.tracer.traceResult;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;

public abstract class ConstantIntegerTraceSequence extends ConstantTraceSequence {

    public abstract Iterator<Integer> backwardIterator();

    public static ConstantIntegerTraceSequence readFrom(final ObjectInputStream in, final byte format) throws IOException {
        if (format == TraceSequence.FORMAT_GZIP) {
            return ConstantGZipIntegerTraceSequence.readFrom(in);
        } else if (format == TraceSequence.FORMAT_GZIP) {
            // TODO
            throw new UnsupportedOperationException();
        } else {
            throw new RuntimeException("Unknown format: " + format);
        }
    }

}
