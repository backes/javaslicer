package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.io.IOException;

public abstract class LongTraceSequence extends AbstractTraceSequence {

    public LongTraceSequence(final int index) {
        super(index);
    }

    public abstract void trace(final long value) throws IOException;

}
