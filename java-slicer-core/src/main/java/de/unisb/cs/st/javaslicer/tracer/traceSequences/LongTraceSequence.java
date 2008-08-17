package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.io.IOException;

public interface LongTraceSequence extends TraceSequence {

    public abstract void trace(final long value) throws IOException;

}
