package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.unisb.cs.st.javaslicer.tracer.ThreadTracer;
import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.LongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.Type;

public class NullTraceSequenceFactory implements TraceSequenceFactory, TraceSequenceFactory.PerThread {

    public class NullTraceSequence implements IntegerTraceSequence, LongTraceSequence {

        public void finish() {
            // null
        }

        public void writeOut(final DataOutputStream out) {
            // null
        }

        public void trace(final int value) {
            // null
        }

        public void trace(final long value) {
            // null
        }

    }

    public TraceSequence createTraceSequence(final Type type, final Tracer tracer) {
        return new NullTraceSequence();
    }

    @Override
    public void finish() {
        // null
    }

    @Override
    public PerThread forThreadTracer(final ThreadTracer tt) {
        return this;
    }

    @Override
    public void writeOut(final OutputStream out) throws IOException {
        out.write(0);
    }

}
