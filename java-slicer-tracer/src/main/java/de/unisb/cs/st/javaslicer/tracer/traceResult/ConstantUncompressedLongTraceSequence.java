package de.unisb.cs.st.javaslicer.tracer.traceResult;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.unisb.cs.st.javaslicer.tracer.util.EmptyIterator;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileReader;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileReader.MultiplexInputStream;

public class ConstantUncompressedLongTraceSequence extends ConstantLongTraceSequence {

    protected final MultiplexedFileReader file;
    protected final int streamIndex;

    public ConstantUncompressedLongTraceSequence(final MultiplexedFileReader file, final int streamIndex) {
        this.file = file;
        this.streamIndex = streamIndex;
    }

    @Override
    public Iterator<Long> backwardIterator() {
        try {
            return new BackwardIterator();
        } catch (final IOException e) {
            return new EmptyIterator<Long>();
        }
    }

    public static ConstantUncompressedLongTraceSequence readFrom(final DataInput in, final MultiplexedFileReader file)
            throws IOException {
        final int streamIndex = in.readInt();
        if (streamIndex < 0 || streamIndex >= file.getNoStreams())
            throw new IOException("corrupted data");
        return new ConstantUncompressedLongTraceSequence(file, streamIndex);
    }

    public class BackwardIterator implements Iterator<Long> {

        private final MultiplexInputStream iStream;
        private final DataInput dataIn;
        private long nextPos;

        public BackwardIterator() throws IOException {
            this.iStream = ConstantUncompressedLongTraceSequence.this.file.getInputStream(ConstantUncompressedLongTraceSequence.this.streamIndex);
            this.dataIn = new DataInputStream(this.iStream);
            this.nextPos = this.iStream.getDataLength()-8;
        }

        public boolean hasNext() {
            return this.nextPos >= 0;
        }

        public Long next() {
            if (this.nextPos < 0)
                throw new NoSuchElementException();
            try {
                this.iStream.seek(this.nextPos);
                this.nextPos -= 8;
                return this.dataIn.readLong();
            } catch (final IOException e) {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
