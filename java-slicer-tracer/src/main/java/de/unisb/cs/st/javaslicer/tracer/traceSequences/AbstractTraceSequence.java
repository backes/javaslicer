package de.unisb.cs.st.javaslicer.tracer.traceSequences;

public abstract class AbstractTraceSequence implements TraceSequence {

    private final int index;

    public AbstractTraceSequence(final int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

}
