package de.unisb.cs.st.javaslicer.tracer.traceSequences.gzip;

import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.ObjectTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequenceFactory;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.Type;

public class GZipTraceSequenceFactory implements TraceSequenceFactory {

    public TraceSequence createTraceSequence(final int traceSequenceIndex, final Type type, final Tracer tracer) throws IOException {
        switch (type) {
        case INTEGER:
            return new GZipIntegerTraceSequence(traceSequenceIndex, tracer);
        case LONG:
            return new GZipLongTraceSequence(traceSequenceIndex, tracer);
        case OBJECT:
            return new ObjectTraceSequence(new GZipLongTraceSequence(traceSequenceIndex, tracer));
        default:
            assert false;
            return null;
        }
    }

}
