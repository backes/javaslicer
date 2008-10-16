package de.unisb.cs.st.javaslicer.tracer.traceSequences.sequitur;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.LongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.output.ObjectWriter;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.output.OutputSequence;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.output.SharedOutputGrammar;

public class SequiturLongTraceSequence implements LongTraceSequence {

    private static final ObjectWriter<Long> OBJECT_WRITER = new ObjectWriter<Long>() {
                @Override
                public void writeObject(final Long object, final ObjectOutputStream outputStream) throws IOException {
                    OptimizedDataOutputStream.writeLong0(object.longValue(), outputStream);
                }
            };

    private boolean ready = false;

    private long startRuleNumber;

    private long lastValue = 0;

    private final OutputSequence<Long> sequiturSeq;

    public SequiturLongTraceSequence(final SharedOutputGrammar<Long> grammar) {
        this.sequiturSeq = new OutputSequence<Long>(grammar, OBJECT_WRITER);
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
        this.startRuleNumber = this.sequiturSeq.getStartRuleNumber();
    }

}
