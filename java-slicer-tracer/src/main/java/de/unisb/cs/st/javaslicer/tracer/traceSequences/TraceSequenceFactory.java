package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.Type;


public interface TraceSequenceFactory {

    TraceSequence createTraceSequence(Type type, Tracer tracer) throws IOException;

}
