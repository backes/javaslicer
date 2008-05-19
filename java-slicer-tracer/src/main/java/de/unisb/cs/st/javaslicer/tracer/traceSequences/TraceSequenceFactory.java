package de.unisb.cs.st.javaslicer.tracer.traceSequences;


public interface TraceSequenceFactory {

    IntegerTraceSequence createIntegerTraceSequence(int index);

    LongTraceSequence createLongTraceSequence(int index);

}
