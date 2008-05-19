package de.unisb.cs.st.javaslicer.tracer.traceSequences;

public abstract class LongTraceSequence extends AbstractTraceSequence {

    public LongTraceSequence(final int index) {
        super(index);
    }

    public abstract void trace(final long value);

}
