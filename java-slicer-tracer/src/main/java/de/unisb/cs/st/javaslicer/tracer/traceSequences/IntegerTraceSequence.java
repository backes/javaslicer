package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.io.IOException;

public interface IntegerTraceSequence extends TraceSequence {

    public abstract void trace(final int value) throws IOException;

}
