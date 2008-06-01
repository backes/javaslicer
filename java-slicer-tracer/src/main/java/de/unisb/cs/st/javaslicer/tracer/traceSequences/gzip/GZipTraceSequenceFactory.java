package de.unisb.cs.st.javaslicer.tracer.traceSequences.gzip;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.ObjectTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequenceFactory;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.Type;

public class GZipTraceSequenceFactory implements TraceSequenceFactory {

    public TraceSequence createTraceSequence(final int traceSequenceIndex, final Type type) {
        switch (type) {
        case INTEGER:
            return new GZipIntegerTraceSequence(traceSequenceIndex);
        case LONG:
            return new GZipLongTraceSequence(traceSequenceIndex);
        case OBJECT:
            return new ObjectTraceSequence(new GZipLongTraceSequence(traceSequenceIndex));
        default:
            assert false;
            return null;
        }
    }

}
