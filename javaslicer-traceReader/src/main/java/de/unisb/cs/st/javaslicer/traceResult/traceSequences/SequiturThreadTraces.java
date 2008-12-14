package de.unisb.cs.st.javaslicer.traceResult.traceSequences;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import de.hammacher.util.MultiplexedFileReader;
import de.hammacher.util.OptimizedDataInputStream;
import de.unisb.cs.st.javaslicer.common.TraceSequenceTypes;
import de.unisb.cs.st.sequitur.input.InputSequence;
import de.unisb.cs.st.sequitur.input.ObjectReader;

public class SequiturThreadTraces extends ConstantThreadTraces {

    private static final ObjectReader<Integer> INT_READER = new ObjectReader<Integer>() {
        public Integer readObject(final ObjectInputStream inputStream) throws IOException {
            return OptimizedDataInputStream.readInt0(inputStream);
        }
    };
    private static final ObjectReader<Long> LONG_READER = new ObjectReader<Long>() {
        public Long readObject(final ObjectInputStream inputStream) throws IOException {
            return OptimizedDataInputStream.readLong0(inputStream);
        }
    };

    private final InputSequence<Integer> intSequence;
    private final InputSequence<Long> longSequence;

    public SequiturThreadTraces(final DataInputStream in) throws IOException, ClassNotFoundException {
        super(TraceSequenceTypes.FORMAT_SEQUITUR);
        final ObjectInputStream objIn = new ObjectInputStream(in);
        this.intSequence = InputSequence.readFrom(objIn, INT_READER);
        this.longSequence = InputSequence.readFrom(objIn, LONG_READER);
    }

    @Override
    public ConstantTraceSequence readSequence(final DataInputStream in, final MultiplexedFileReader file) throws IOException {
        final long sequenceOffset = OptimizedDataInputStream.readLong0(in);
        final int count = OptimizedDataInputStream.readInt0(in);
        return (sequenceOffset & 1) != 0
            ? new ConstantSequiturLongTraceSequence(sequenceOffset/2, count, this.longSequence)
            : new ConstantSequiturIntegerTraceSequence(sequenceOffset/2, count, this.intSequence);
    }

}
