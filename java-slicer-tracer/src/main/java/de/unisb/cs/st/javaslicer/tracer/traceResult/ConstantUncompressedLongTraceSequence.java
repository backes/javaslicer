package de.unisb.cs.st.javaslicer.tracer.traceResult;

import java.io.DataInput;
import java.io.IOException;
import java.util.Iterator;

import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileReader;

public class ConstantUncompressedLongTraceSequence extends ConstantLongTraceSequence {

    private final MultiplexedFileReader file;
    private final int streamIndex;

    public ConstantUncompressedLongTraceSequence(final MultiplexedFileReader file, final int streamIndex) {
        this.file = file;
        this.streamIndex = streamIndex;
    }

    @Override
    public Iterator<Long> backwardIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    public static ConstantUncompressedLongTraceSequence readFrom(final DataInput in, final MultiplexedFileReader file)
            throws IOException {
        final int streamIndex = in.readInt();
        if (streamIndex < 0 || streamIndex >= file.getNoStreams())
            throw new IOException("corrupted data");
        return new ConstantUncompressedLongTraceSequence(file, streamIndex);
    }

}
