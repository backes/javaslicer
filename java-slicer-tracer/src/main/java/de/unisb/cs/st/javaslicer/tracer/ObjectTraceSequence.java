package de.unisb.cs.st.javaslicer.tracer;

public class ObjectTraceSequence implements TraceSequence {

    private final TraceSequence integerTraceSequence;

    public ObjectTraceSequence(final TraceSequence integerTraceSequence) {
        this.integerTraceSequence = integerTraceSequence;
    }

    public int getIndex() {
        return this.integerTraceSequence.getIndex();
    }

    public void trace(final Object obj) {
        // TODO Auto-generated method stub

    }

}
