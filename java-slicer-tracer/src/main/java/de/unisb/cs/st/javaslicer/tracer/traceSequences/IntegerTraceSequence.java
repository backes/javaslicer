package de.unisb.cs.st.javaslicer.tracer.traceSequences;

public abstract class IntegerTraceSequence extends AbstractTraceSequence {

    public IntegerTraceSequence(final int index) {
        super(index);
    }

    public abstract void trace(final int value);

}
