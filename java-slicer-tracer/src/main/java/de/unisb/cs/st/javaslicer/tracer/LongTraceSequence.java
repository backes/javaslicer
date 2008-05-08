package de.unisb.cs.st.javaslicer.tracer;

public class LongTraceSequence implements TraceSequence {

    private final int index;

    public LongTraceSequence(final int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    public void trace(final long value) {
        // TODO Auto-generated method stub
        // also use Thread.currentThread().getId();
    }

}
