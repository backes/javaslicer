package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.io.DataOutput;
import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.Type;

public class NullTraceSequenceFactory implements TraceSequenceFactory {

    public class NullTraceSequence extends AbstractTraceSequence implements IntegerTraceSequence, LongTraceSequence {

        public NullTraceSequence(final int index) {
            super(index);
        }

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

    public TraceSequence createTraceSequence(final int index, final Type type, final Tracer tracer) throws IOException {
        return type == Type.OBJECT ? new ObjectTraceSequence(new NullTraceSequence(index)) : new NullTraceSequence(index);
    }

}
