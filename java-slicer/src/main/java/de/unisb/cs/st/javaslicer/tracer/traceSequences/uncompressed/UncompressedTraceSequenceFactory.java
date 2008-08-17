package de.unisb.cs.st.javaslicer.tracer.traceSequences.uncompressed;

import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.ObjectTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequenceFactory;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.Type;

public class UncompressedTraceSequenceFactory implements TraceSequenceFactory {

    public TraceSequence createTraceSequence(final int traceSequenceIndex, final Type type, final Tracer tracer) throws IOException {
        switch (type) {
        case INTEGER:
            return new UncompressedIntegerTraceSequence(traceSequenceIndex, tracer);
        case LONG:
            return new UncompressedLongTraceSequence(traceSequenceIndex, tracer);
        case OBJECT:
            return new ObjectTraceSequence(new UncompressedLongTraceSequence(traceSequenceIndex, tracer));
        default:
            assert false;
            return null;
        }
    }

}
