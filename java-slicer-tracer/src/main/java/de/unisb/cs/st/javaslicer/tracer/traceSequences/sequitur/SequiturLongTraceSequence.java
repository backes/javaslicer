package de.unisb.cs.st.javaslicer.tracer.traceSequences.sequitur;

import java.io.DataOutputStream;
import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.LongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.output.OutputSequence;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.output.SharedOutputGrammar;

public class SequiturLongTraceSequence implements LongTraceSequence {

    private boolean ready = false;

    private long startRuleNumber;

    private long lastValue = 0;

    private final OutputSequence<Long> sequiturSeq;

    public SequiturLongTraceSequence(final SharedOutputGrammar<Long> grammar) {
        this.sequiturSeq = new OutputSequence<Long>(grammar);
    }

    public void trace(final long value) {
        assert !this.ready: "Trace cannot be extended any more";

        final long write = value - this.lastValue;
        this.lastValue = value;

        this.sequiturSeq.append(write);
    }

    public void writeOut(final DataOutputStream out) throws IOException {
        finish();

        OptimizedDataOutputStream.writeLong0(2*this.startRuleNumber, out);
    }

    public void finish() {
        if (this.ready)
            return;
        this.ready = true;
        this.sequiturSeq.append(this.lastValue);
        this.startRuleNumber = this.sequiturSeq.getStartRuleNumber();
    }

    @Override
    public boolean useMultiThreading() {
        return false;
    }
}
