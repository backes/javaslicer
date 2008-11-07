package de.unisb.cs.st.javaslicer.tracer.traceSequences.sequitur;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import de.unisb.cs.st.javaslicer.tracer.ThreadTracer;
import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequenceFactory;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.Type;
import de.unisb.cs.st.javaslicer.tracer.util.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.output.ObjectWriter;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.output.OutputSequence;

public class SequiturTraceSequenceFactory implements TraceSequenceFactory {

    public static class PerThread implements TraceSequenceFactory.PerThread {

        private static ObjectWriter<Long> LONG_WRITER = new ObjectWriter<Long>() {
            public void writeObject(final Long object, final ObjectOutputStream outputStream) throws IOException {
                OptimizedDataOutputStream.writeLong0(object.longValue(), outputStream);
            }
        };
        private static ObjectWriter<Integer> INT_WRITER = new ObjectWriter<Integer>() {
            public void writeObject(final Integer object, final ObjectOutputStream outputStream) throws IOException {
                OptimizedDataOutputStream.writeInt0(object.intValue(), outputStream);
            }
        };

        private final OutputSequence<Integer> intSequence = new OutputSequence<Integer>(INT_WRITER);
        private final OutputSequence<Long> longSequence = new OutputSequence<Long>(LONG_WRITER);
        private final AtomicLong intSequenceLength = new AtomicLong(0);
        private final AtomicLong longSequenceLength = new AtomicLong(0);

        private List<SequiturIntegerTraceSequence> intSequences = new ArrayList<SequiturIntegerTraceSequence>();
        private List<SequiturLongTraceSequence> longSequences = new ArrayList<SequiturLongTraceSequence>();

        @Override
        public synchronized TraceSequence createTraceSequence(final Type type, final Tracer tracer) throws IOException {
            if (this.intSequences == null)
                throw new IOException("sequence factory already finished");
            SequiturIntegerTraceSequence intTraceSequence;
            SequiturLongTraceSequence longTraceSequence;
            switch (type) {
            case INTEGER:
                intTraceSequence = new SequiturIntegerTraceSequence(this.intSequence, this.intSequenceLength);
                this.intSequences.add(intTraceSequence);
                return intTraceSequence;
            case LONG:
                longTraceSequence = new SequiturLongTraceSequence(this.longSequence, this.longSequenceLength);
                this.longSequences.add(longTraceSequence);
                return longTraceSequence;
            default:
                assert false;
                return null;
            }
        }

        @Override
        public synchronized void finish() {
            if (this.intSequences == null)
                return;
            for (final SequiturIntegerTraceSequence intSeq: this.intSequences)
                intSeq.finish();
            for (final SequiturLongTraceSequence longSeq: this.longSequences)
                longSeq.finish();
            this.intSequences = null;
            this.longSequences = null;
            this.intSequence.finish();
            this.longSequence.finish();
        }

        @Override
        public void writeOut(final OutputStream out) throws IOException {
            finish();
            out.write(TraceSequence.FORMAT_SEQUITUR);
            final ObjectOutputStream objOut = new ObjectOutputStream(out);
            this.intSequence.writeOut(objOut, true);
            this.longSequence.writeOut(objOut, true);
        }

    }

    @Override
    public PerThread forThreadTracer(final ThreadTracer tt) {
        return new PerThread();
    }

    @Override
    public boolean shouldAutoFlushFile() {
        return true;
    }

}
