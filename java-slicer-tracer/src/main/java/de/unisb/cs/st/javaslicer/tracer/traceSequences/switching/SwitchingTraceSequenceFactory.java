package de.unisb.cs.st.javaslicer.tracer.traceSequences.switching;

import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.ObjectTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequenceFactory;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.Type;

public class SwitchingTraceSequenceFactory implements TraceSequenceFactory {

    @Override
    public TraceSequence createTraceSequence(final Type type, final Tracer tracer) throws IOException {
        switch (type) {
        case INTEGER:
            return new SwitchingIntegerTraceSequence(tracer);
        case LONG:
            return new SwitchingLongTraceSequence(tracer);
        case OBJECT:
            return new ObjectTraceSequence(new SwitchingLongTraceSequence(tracer));
        default:
            assert false;
            return null;
        }
    }

}
