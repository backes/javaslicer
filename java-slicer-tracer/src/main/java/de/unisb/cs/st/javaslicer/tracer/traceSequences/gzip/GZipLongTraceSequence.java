package de.unisb.cs.st.javaslicer.tracer.traceSequences.gzip;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.GZIPOutputStream;

import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.LongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.MyByteArrayInputStream;
import de.unisb.cs.st.javaslicer.tracer.util.MyDataInputStream;
import de.unisb.cs.st.javaslicer.tracer.util.MyDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.util.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileWriter.MultiplexOutputStream;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileWriter.MultiplexOutputStream.Reader;

public class GZipLongTraceSequence implements LongTraceSequence {

    private static class BackwardLongStreamReader implements Iterator<Long> {

        private long offset;
        private final long[] buf;
        private int bufPos;
        private final Reader mplexReader;
        private final MyDataInputStream dataIn;

        public BackwardLongStreamReader(final MultiplexOutputStream mplexOut, final int bufSize) throws IOException {
            final long numLongs = mplexOut.length()/8;
            long startLong = (numLongs - 1) / bufSize * bufSize;
            this.offset = startLong * 8;
            this.mplexReader = mplexOut.getReader(this.offset);
            this.dataIn = new MyDataInputStream(this.mplexReader);
            this.buf = new long[bufSize];
            this.bufPos = (int) (numLongs - startLong - 1);
            for (int i = 0; startLong < numLongs; ++startLong) {
                this.buf[i++] = this.dataIn.readLong();
            }
        }

        @Override
        public boolean hasNext() {
            try {
                if (this.bufPos >= 0)
                    return true;
                if (this.offset == 0)
                    return false;
                this.offset -= this.buf.length*8;
                this.mplexReader.seek(this.offset);
                for (int i = 0; i < this.buf.length; ++i) {
                    this.buf[i] = this.dataIn.readLong();
                }
                this.bufPos = this.buf.length - 1;
                return true;
            } catch (final IOException e) {
                close();
                return false;
            }
        }

        @Override
        public Long next() {
            if (!hasNext())
                throw new NoSuchElementException();
            return this.buf[this.bufPos--];
        }

        // to avoid boxing
        public long nextLong() {
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
            this.mplexReader.close();
        }
    }

    private final static int SWITCH_TO_GZIP_WHEN_GREATER = 512;

    private static final int CACHE_IF_LEQ = 503; // must be <= SWITCH_TO_GZIP_WHEN_GREATER

    private final Tracer tracer;

    private ByteArrayOutputStream baOutputStream;
    private MyDataOutputStream dataOut;

    private MultiplexOutputStream mplexOut;

    private boolean gzipped;


    public GZipLongTraceSequence(final Tracer tracer) {
        this.tracer = tracer;
        this.baOutputStream = new ByteArrayOutputStream(16);
        this.dataOut = new MyDataOutputStream(this.baOutputStream);
    }

    public void trace(final long value) throws IOException {
        assert this.dataOut != null : "Trace cannot be extended any more";

        this.dataOut.writeLong(value);

        if (this.baOutputStream != null && this.baOutputStream.size() > CACHE_IF_LEQ) {
            this.mplexOut = this.tracer.newOutputStream();
            this.dataOut = new MyDataOutputStream(this.mplexOut);
            this.baOutputStream.writeTo(this.mplexOut);
            this.baOutputStream = null;
        }
    }

    public void writeOut(final DataOutputStream out) throws IOException {
        finish();

        out.writeByte(TYPE_LONG | (this.gzipped ? 1 : 0));
        out.writeInt(this.mplexOut.getId());
    }

    public void finish() throws IOException {
        if (this.dataOut == null)
            return;
        this.dataOut = null;

        final MultiplexOutputStream oldMplexOut = this.mplexOut;
        this.mplexOut = this.tracer.newOutputStream();

        // now we have to inverse the long stream
        assert this.baOutputStream != null || oldMplexOut != null;
        if (this.baOutputStream != null) {
            this.gzipped = false;
            int nextPos = this.baOutputStream.size() - 8;
            final MyByteArrayInputStream bb = new MyByteArrayInputStream(this.baOutputStream.toByteArray());
            final MyDataInputStream dataIn = new MyDataInputStream(bb);
            final OptimizedDataOutputStream optOut = new OptimizedDataOutputStream(this.mplexOut, true);
            while (nextPos >= 0) {
                bb.seek(nextPos);
                optOut.writeLong(dataIn.readLong());
                nextPos -= 8;
            }
            this.baOutputStream = null;
            optOut.close();
        } else {
            ByteArrayOutputStream invStreamFirstPart = null;
            OptimizedDataOutputStream optOut = null;
            final BackwardLongStreamReader backwardReader = new BackwardLongStreamReader(oldMplexOut, 4*1024);
            if (oldMplexOut.length() <= 8*SWITCH_TO_GZIP_WHEN_GREATER) {
                invStreamFirstPart = new ByteArrayOutputStream();
                optOut = new OptimizedDataOutputStream(invStreamFirstPart, true);
                while (backwardReader.hasNext())
                    optOut.writeLong(backwardReader.nextLong());
            }
            if (!backwardReader.hasNext() && invStreamFirstPart != null && invStreamFirstPart.size() <= SWITCH_TO_GZIP_WHEN_GREATER) {
                this.gzipped = false;
                invStreamFirstPart.writeTo(this.mplexOut);
            } else {
                this.gzipped = true;
                final OutputStream gzipOut = new BufferedOutputStream(new GZIPOutputStream(this.mplexOut, 512), 512);
                if (invStreamFirstPart != null)
                    invStreamFirstPart.writeTo(gzipOut);
                optOut = new OptimizedDataOutputStream(gzipOut, 0, optOut == null ? 0l : optOut.getLastLongValue());
                while (backwardReader.hasNext())
                    optOut.writeLong(backwardReader.nextLong());
                optOut.close();
            }
            backwardReader.close();
            oldMplexOut.remove();
        }
    }

    @Override
    public boolean useMultiThreading() {
        return this.dataOut != null;
    }

}
