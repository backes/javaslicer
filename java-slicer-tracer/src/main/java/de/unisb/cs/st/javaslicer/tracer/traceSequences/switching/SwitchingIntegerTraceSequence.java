package de.unisb.cs.st.javaslicer.tracer.traceSequences.switching;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.GZIPOutputStream;

import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.MyByteArrayInputStream;
import de.unisb.cs.st.javaslicer.tracer.util.MyDataInputStream;
import de.unisb.cs.st.javaslicer.tracer.util.MyDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.util.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileWriter.MultiplexOutputStream;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileWriter.MultiplexOutputStream.Reader;

public class SwitchingIntegerTraceSequence implements IntegerTraceSequence {

    private static class BackwardIntegerStreamReader implements Iterator<Integer> {

        private long offset;
        private final int[] buf;
        private int bufPos;
        private final Reader mplexReader;
        private final MyDataInputStream dataIn;

        public BackwardIntegerStreamReader(final MultiplexOutputStream mplexOut, final int bufSize) throws IOException {
            final long numInts = mplexOut.length()/4;
            long startInt = (numInts - 1) / bufSize * bufSize;
            this.offset = startInt * 4;
            this.mplexReader = mplexOut.getReader(this.offset);
            this.dataIn = new MyDataInputStream(this.mplexReader);
            this.buf = new int[bufSize];
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
                this.mplexReader.seek(this.offset);
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

        // to avoid boxing
        public int nextInt() {
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

    private static final int CACHE_IF_LEQ = 507; // must be <= SWITCH_TO_GZIP_WHEN_GREATER

    private final Tracer tracer;

    private ByteArrayOutputStream baOutputStream;
    private MyDataOutputStream dataOut;

    private MultiplexOutputStream mplexOut;

    private boolean gzipped;


    public SwitchingIntegerTraceSequence(final Tracer tracer) {
        this.tracer = tracer;
        this.baOutputStream = new ByteArrayOutputStream(16);
        this.dataOut = new MyDataOutputStream(this.baOutputStream);
    }

    public void trace(final int value) throws IOException {
        assert this.dataOut != null : "Trace cannot be extended any more";

        this.dataOut.writeInt(value);

        if (this.baOutputStream != null && this.baOutputStream.size() > CACHE_IF_LEQ) {
            this.mplexOut = this.tracer.newOutputStream();
            this.dataOut = new MyDataOutputStream(this.mplexOut);
            this.baOutputStream.writeTo(this.mplexOut);
            this.baOutputStream = null;
        }
    }

    public void writeOut(final DataOutputStream out) throws IOException {
        finish();

        out.writeByte(getFormat() | TYPE_INTEGER);
        out.writeBoolean(this.gzipped);
        out.writeInt(this.mplexOut.getId());
    }

    protected byte getFormat() {
        return FORMAT_SWITCHING;
    }

    public void finish() throws IOException {
        if (this.dataOut == null)
            return;
        this.dataOut = null;

        final MultiplexOutputStream oldMplexOut = this.mplexOut;
        this.mplexOut = this.tracer.newOutputStream();

        // now we have to inverse the integer stream
        assert this.baOutputStream != null || oldMplexOut != null;
        if (this.baOutputStream != null) {
            this.gzipped = false;
            int nextPos = this.baOutputStream.size() - 4;
            final MyByteArrayInputStream bb = new MyByteArrayInputStream(this.baOutputStream.toByteArray());
            final MyDataInputStream dataIn = new MyDataInputStream(bb);
            final OptimizedDataOutputStream optOut = new OptimizedDataOutputStream(this.mplexOut, true);
            while (nextPos >= 0) {
                bb.seek(nextPos);
                optOut.writeInt(dataIn.readInt());
                nextPos -= 4;
            }
            this.baOutputStream = null;
            optOut.close();
        } else {
            ByteArrayOutputStream invStreamFirstPart = null;
            OptimizedDataOutputStream optOut = null;
            final BackwardIntegerStreamReader backwardReader = new BackwardIntegerStreamReader(oldMplexOut, 8*1024);
            if (oldMplexOut.length() <= 4*SWITCH_TO_GZIP_WHEN_GREATER) {
                invStreamFirstPart = new ByteArrayOutputStream();
                optOut = new OptimizedDataOutputStream(invStreamFirstPart, true);
                while (backwardReader.hasNext())
                    optOut.writeInt(backwardReader.nextInt());
            }
            if (!backwardReader.hasNext() && invStreamFirstPart != null && invStreamFirstPart.size() <= SWITCH_TO_GZIP_WHEN_GREATER) {
                this.gzipped = false;
                invStreamFirstPart.writeTo(this.mplexOut);
            } else {
                this.gzipped = true;
                final OutputStream gzipOut = new BufferedOutputStream(new GZIPOutputStream(this.mplexOut, 512), 512);
                if (invStreamFirstPart != null)
                    invStreamFirstPart.writeTo(gzipOut);
                optOut = new OptimizedDataOutputStream(gzipOut, optOut == null ? 0 : optOut.getLastIntValue(), 0l);
                while (backwardReader.hasNext())
                    optOut.writeInt(backwardReader.nextInt());
                optOut.close();
            }
            backwardReader.close();
            oldMplexOut.remove();
        }
    }

}
