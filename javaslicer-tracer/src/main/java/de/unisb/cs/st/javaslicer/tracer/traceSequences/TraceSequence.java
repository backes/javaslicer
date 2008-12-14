package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.io.DataOutputStream;
import java.io.IOException;

public interface TraceSequence {

    public interface IntegerTraceSequence extends TraceSequence {
        public abstract void trace(final int value) throws IOException;
    }

    public interface LongTraceSequence extends TraceSequence {
        public abstract void trace(final long value) throws IOException;
    }

    void writeOut(DataOutputStream out) throws IOException;

    void finish() throws IOException;

    /**
     * Determines whether the individual sequences are {@link #finish()}ed
     * in parallel.
     *
     * @return <code>true</code> iff the sequences should be finished in parallel
     */
    boolean useMultiThreading();

}
