package de.unisb.cs.st.javaslicer.tracer.traceSequences.sequitur;

import java.io.DataOutputStream;
import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.output.OutputSequence;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.output.SharedOutputGrammar;

public class SequiturIntegerTraceSequence implements IntegerTraceSequence {

    private boolean ready = false;

    private long startRuleNumber;

    private int lastValue = 0;

    private final OutputSequence<Integer> sequiturSeq;

    public SequiturIntegerTraceSequence(final SharedOutputGrammar<Integer> grammar) {
        this.sequiturSeq = new OutputSequence<Integer>(grammar);
    }

    public void trace(final int value) {
        assert !this.ready: "Trace cannot be extended any more";

        final int write = value - this.lastValue;
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

    public void ensureInvariants() {
        if (!this.ready)
            this.sequiturSeq.ensureInvariants();
    }
}
