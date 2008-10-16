package de.unisb.cs.st.javaslicer.tracer.traceSequences.sequitur;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.output.ObjectWriter;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.output.OutputSequence;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.output.SharedOutputGrammar;

public class SequiturIntegerTraceSequence implements IntegerTraceSequence {

    private static final ObjectWriter<Integer> OBJECT_WRITER = new ObjectWriter<Integer>() {
                @Override
                public void writeObject(final Integer object, final ObjectOutputStream outputStream) throws IOException {
                    OptimizedDataOutputStream.writeInt0(object.intValue(), outputStream);
                }
            };

    private boolean ready = false;

    private long startRuleNumber;

    private int lastValue = 0;

    private final OutputSequence<Integer> sequiturSeq;

    public SequiturIntegerTraceSequence(final SharedOutputGrammar<Integer> grammar) {
        this.sequiturSeq = new OutputSequence<Integer>(grammar, OBJECT_WRITER);
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
        this.startRuleNumber = this.sequiturSeq.getStartRuleNumber();
    }

}
