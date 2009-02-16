package de.unisb.cs.st.javaslicer.tracer.traceSequences.sequitur;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import de.hammacher.util.streams.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.LongTraceSequence;
import de.unisb.cs.st.sequitur.output.OutputSequence;

public class SequiturLongTraceSequence implements LongTraceSequence {

    private boolean ready = false;

    private long sequenceOffset;

    private long[] values = new long[10];
    private int count = 0;

    private long lastValue = 0;

    private final OutputSequence<Long> sequiturSeq;
    private final AtomicLong sequiturSeqLength;

    public SequiturLongTraceSequence(final OutputSequence<Long> outputSequence, final AtomicLong outputSeqLength) {
        this.sequiturSeq = outputSequence;
        this.sequiturSeqLength = outputSeqLength;
    }

    public void trace(final long value) {
        assert !this.ready: "Trace cannot be extended any more";

        if (this.count == this.values.length)
            this.values = Arrays.copyOf(this.values, this.values.length*3/2);
        this.values[this.count++] = value - this.lastValue;
        this.lastValue = value;
    }

    @Override
    public void writeOut(final DataOutputStream out) throws IOException {
        finish();

        OptimizedDataOutputStream.writeLong0(2*this.sequenceOffset+1, out);
        OptimizedDataOutputStream.writeInt0(this.count, out);
    }

    @Override
    public void finish() {
        if (this.ready)
            return;
        this.ready = true;
        synchronized (this.sequiturSeq) {
            for (int i = 0; i < this.count; ++i)
                this.sequiturSeq.append(this.values[i]);
            this.values = null;
            if (this.count > 10) {
                this.sequiturSeq.append(this.lastValue);
                this.sequenceOffset = this.sequiturSeqLength.getAndAdd(this.count+1);
            } else {
                this.sequenceOffset = this.sequiturSeqLength.getAndAdd(this.count);
            }
        }
    }

    @Override
    public boolean useMultiThreading() {
        return false;
    }

}
