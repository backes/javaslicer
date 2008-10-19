package de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences.ConstantTraceSequence.ConstantIntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.EmptyIterator;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileReader;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileReader.MultiplexInputStream;

public class ConstantUncompressedIntegerTraceSequence implements ConstantIntegerTraceSequence {

    protected final MultiplexedFileReader file;
    protected final int streamIndex;

    public ConstantUncompressedIntegerTraceSequence(final MultiplexedFileReader file, final int streamIndex) {
        this.file = file;
        this.streamIndex = streamIndex;
    }

    @Override
    public Iterator<Integer> backwardIterator() {
        try {
            return new BackwardIterator();
        } catch (final IOException e) {
            return new EmptyIterator<Integer>();
        }
    }

    public static ConstantUncompressedIntegerTraceSequence readFrom(final DataInput in, final MultiplexedFileReader file)
            throws IOException {
        final int streamIndex = in.readInt();
        if (!file.getStreamIds().contains(streamIndex))
            throw new IOException("corrupted data");
        return new ConstantUncompressedIntegerTraceSequence(file, streamIndex);
    }

    public class BackwardIterator implements Iterator<Integer> {

        private final MultiplexInputStream iStream;
        private final DataInputStream dataIn;
        private long nextPos;

        public BackwardIterator() throws IOException {
            this.iStream = ConstantUncompressedIntegerTraceSequence.this.file.getInputStream(ConstantUncompressedIntegerTraceSequence.this.streamIndex);
            this.dataIn = new DataInputStream(this.iStream);
            this.nextPos = this.iStream.getDataLength()-4;
            if ((this.nextPos & 3) != 0)
                throw new IOException("corrupted data (illegal stream length)");
        }

        public boolean hasNext() {
            return this.nextPos >= 0;
        }

        public Integer next() {
            if (this.nextPos < 0)
                throw new NoSuchElementException();
            try {
                this.iStream.seek(this.nextPos);
                this.nextPos -= 4;
                return this.dataIn.readInt();
            } catch (final IOException e) {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
