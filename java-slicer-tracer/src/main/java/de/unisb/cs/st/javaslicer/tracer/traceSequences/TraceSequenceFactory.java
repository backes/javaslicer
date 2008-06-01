package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.Type;


public interface TraceSequenceFactory {

    TraceSequence createTraceSequence(int traceSequenceIndex, Type type);

}
