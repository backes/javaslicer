package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.io.DataOutput;
import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.LongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.Type;

public class NullTraceSequenceFactory implements TraceSequenceFactory {

    public class NullTraceSequence implements IntegerTraceSequence, LongTraceSequence {

        public void finish() throws IOException {
            // null
        }

        public void writeOut(final DataOutput out) throws IOException {
            // null
        }

        public void trace(final int value) throws IOException {
            // null
        }

        public void trace(final long value) throws IOException {
            // null
        }

    }

    public TraceSequence createTraceSequence(final Type type, final Tracer tracer) throws IOException {
        return type == Type.OBJECT ? new ObjectTraceSequence(new NullTraceSequence()) : new NullTraceSequence();
    }

}
