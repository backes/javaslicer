package de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileReader;
import de.unisb.cs.st.javaslicer.tracer.util.OptimizedDataInputStream;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.input.ObjectReader;
import de.unisb.cs.st.javaslicer.tracer.util.sequitur.input.SharedInputGrammar;

public class SequiturThreadTraces extends ConstantThreadTraces {

    private static final ObjectReader<Integer> INT_READER = new ObjectReader<Integer>() {
        @Override
        public Integer readObject(final ObjectInputStream inputStream) throws IOException {
            return OptimizedDataInputStream.readInt0(inputStream);
        }
    };
    private static final ObjectReader<Long> LONG_READER = new ObjectReader<Long>() {
        @Override
        public Long readObject(final ObjectInputStream inputStream) throws IOException {
            return OptimizedDataInputStream.readLong0(inputStream);
        }
    };

    private final SharedInputGrammar<Integer> intGrammar;
    private final SharedInputGrammar<Long> longGrammar;

    public SequiturThreadTraces(final DataInputStream in) throws IOException, ClassNotFoundException {
        super(TraceSequence.FORMAT_SEQUITUR);
        final ObjectInputStream objIn = new ObjectInputStream(in);
        this.intGrammar = SharedInputGrammar.readFrom(objIn, INT_READER);
        this.longGrammar = SharedInputGrammar.readFrom(objIn, LONG_READER);
    }

    @Override
    public ConstantTraceSequence readSequence(final DataInputStream in, final MultiplexedFileReader file) throws IOException {
        final long startRuleNumber = OptimizedDataInputStream.readLong0(in);
        return (startRuleNumber & 1) != 0
            ? new ConstantSequiturLongTraceSequence(startRuleNumber/2, this.longGrammar)
            : new ConstantSequiturIntegerTraceSequence(startRuleNumber/2, this.intGrammar);
    }

}
