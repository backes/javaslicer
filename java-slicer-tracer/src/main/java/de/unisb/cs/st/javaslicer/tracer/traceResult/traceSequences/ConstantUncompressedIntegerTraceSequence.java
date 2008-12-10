package de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.hammacher.util.MultiplexedFileReader;
import de.hammacher.util.MultiplexedFileReader.MultiplexInputStream;
import de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences.ConstantTraceSequence.ConstantIntegerTraceSequence;

public class ConstantUncompressedIntegerTraceSequence implements ConstantIntegerTraceSequence {

    protected final MultiplexedFileReader file;
    protected final int streamIndex;

    public ConstantUncompressedIntegerTraceSequence(final MultiplexedFileReader file, final int streamIndex) {
        this.file = file;
        this.streamIndex = streamIndex;
    }

    @Override
    public Iterator<Integer> backwardIterator() throws IOException {
        return new BackwardIterator(this.file, this.streamIndex, 8*1024);
    }

    @Override
    public Iterator<Integer> iterator() throws IOException {
        return new ForwardIterator(this.file, this.streamIndex);
    }

    public static ConstantUncompressedIntegerTraceSequence readFrom(final DataInput in, final MultiplexedFileReader file)
            throws IOException {
        final int streamIndex = in.readInt();
        if (!file.getStreamIds().contains(streamIndex))
            throw new IOException("corrupted data");
        return new ConstantUncompressedIntegerTraceSequence(file, streamIndex);
    }

    private static class BackwardIterator implements Iterator<Integer> {

        private long offset;
        private final int[] buf;
        private int bufPos;
        private final MultiplexInputStream inputStream;
        private final DataInputStream dataIn;

        public BackwardIterator(final MultiplexedFileReader file, final int streamIndex, final int bufSize) throws IOException {
            this.inputStream = file.getInputStream(streamIndex);
            final long numInts = this.inputStream.getDataLength()/4;
            if (numInts * 4 != this.inputStream.getDataLength())
                throw new IOException("Stream's length not dividable by 4");

            long startInt = (numInts - 1) / bufSize * bufSize;
            this.offset = startInt * 4;
            this.inputStream.seek(this.offset);
            this.dataIn = new DataInputStream(this.inputStream);
            this.buf = new int[startInt == 0 ? (int)numInts : bufSize];
            this.bufPos = (int) (numInts - startInt - 1);
            for (int i = 0; startInt < numInts; ++startInt) {
                this.buf[i++] = this.dataIn.readInt();
            }
        }

        @Override
        public boolean hasNext() {
            try {
                if (this.bufPos >= 0)
                    return true;
                if (this.offset == 0)
                    return false;
                this.offset -= this.buf.length*4;
                this.inputStream.seek(this.offset);
                for (int i = 0; i < this.buf.length; ++i) {
                    this.buf[i] = this.dataIn.readInt();
                }
                this.bufPos = this.buf.length - 1;
                return true;
            } catch (final IOException e) {
                close();
                return false;
            }
        }

        @Override
        public Integer next() {
            if (!hasNext())
                throw new NoSuchElementException();
            return this.buf[this.bufPos--];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void close() {
            this.bufPos = -1;
            this.offset = 0;
            this.inputStream.close();
        }
    }

    private static class ForwardIterator implements Iterator<Integer> {

        private final MultiplexInputStream inputStream;
        private final DataInputStream dataIn;

        public ForwardIterator(final MultiplexedFileReader file, final int streamIndex) throws IOException {
            this.inputStream = file.getInputStream(streamIndex);
            this.dataIn = new DataInputStream(this.inputStream);
        }

        @Override
        public boolean hasNext() {
            try {
                return this.inputStream.isEOF();
            } catch (final IOException e) {
                return false;
            }
        }

        @Override
        public Integer next() {
            if (!hasNext())
                throw new NoSuchElementException();
            try {
                return this.dataIn.readInt();
            } catch (final IOException e) {
                throw new NoSuchElementException(e.toString());
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
