package de.unisb.cs.st.javaslicer.tracer.traceSequences.switching;

import java.io.IOException;
import java.io.OutputStream;

import de.unisb.cs.st.javaslicer.tracer.ThreadTracer;
import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequenceFactory;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.Type;

public class SwitchingTraceSequenceFactory implements TraceSequenceFactory, TraceSequenceFactory.PerThread {

    @Override
    public TraceSequence createTraceSequence(final Type type, final Tracer tracer) {
        switch (type) {
        case INTEGER:
            return new SwitchingIntegerTraceSequence(tracer);
        case LONG:
            return new SwitchingLongTraceSequence(tracer);
        default:
            assert false;
            return null;
        }
    }

    @Override
    public void finish() {
        // nop
    }

    @Override
    public PerThread forThreadTracer(final ThreadTracer tt) {
        return this;
    }

    @Override
    public void writeOut(final OutputStream out) throws IOException {
        out.write(TraceSequence.FORMAT_GZIP);
    }

}
