package de.unisb.cs.st.javaslicer.tracer.traceSequences.gzip;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.LongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequenceFactory;

public class GZipTraceSequenceFactory implements TraceSequenceFactory {

    public IntegerTraceSequence createIntegerTraceSequence(final int index) {
        return new GZipIntegerTraceSequence(index);
    }

    public LongTraceSequence createLongTraceSequence(final int index) {
        return new GZipLongTraceSequence(index);
    }

}
