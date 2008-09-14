package de.unisb.cs.st.javaslicer.tracer.traceSequences.switching;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.MyDataInputStream;
import de.unisb.cs.st.javaslicer.tracer.util.MyDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.util.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileWriter.MultiplexOutputStream;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileWriter.MultiplexOutputStream.Reader;

public class SwitchingIntegerTraceSequence implements IntegerTraceSequence {

    public class MyByteArrayInputStream extends InputStream {

        private final byte[] buf;
        private int nextPos;
        private final int count;

        public MyByteArrayInputStream(final byte[] buf) {
            this.buf = buf;
            this.nextPos = 0;
            this.count = buf.length;
        }

        @Override
        public int read() throws IOException {
            if (this.nextPos >= this.count)
                return -1;
            return this.buf[this.nextPos++] & 0xff;
        }

        public void seek(final int pos) {
            if (pos < 0)
                throw new IllegalArgumentException("position for seek must be >= 0");
            this.nextPos = pos;
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

    public void writeOut(final DataOutput out) throws IOException {
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
        } else {
            ByteArrayOutputStream invStreamFirstPart = null;
            OptimizedDataOutputStream optOut = null;
            long nextPos = oldMplexOut.length() - 4;
            final Reader mplexReader = oldMplexOut.getReader(Math.max(0, nextPos));
            final MyDataInputStream dataIn = new MyDataInputStream(mplexReader);
            if (this.mplexOut.length() < 4*SWITCH_TO_GZIP_WHEN_GREATER) {
                invStreamFirstPart = new ByteArrayOutputStream();
                optOut = new OptimizedDataOutputStream(invStreamFirstPart, true);
                while (nextPos >= 0 && invStreamFirstPart.size() <= SWITCH_TO_GZIP_WHEN_GREATER) {
                    mplexReader.seek(nextPos);
                    optOut.writeInt(dataIn.readInt());
                    nextPos -= 4;
                }
            }
            if (nextPos < 0 && invStreamFirstPart != null && invStreamFirstPart.size() <= SWITCH_TO_GZIP_WHEN_GREATER) {
                this.gzipped = false;
                invStreamFirstPart.writeTo(this.mplexOut);
                optOut.close();
            } else {
                this.gzipped = true;
                final OutputStream gzipOut = new BufferedOutputStream(new GZIPOutputStream(this.mplexOut), 8192);
                if (invStreamFirstPart != null)
                    invStreamFirstPart.writeTo(gzipOut);
                optOut = new OptimizedDataOutputStream(gzipOut, optOut == null ? 0 : optOut.getLastIntValue(), 0l);
                while (nextPos >= 0) {
                    mplexReader.seek(nextPos);
                    optOut.writeInt(dataIn.readInt());
                    nextPos -= 4;
                }
                optOut.close();
            }
            mplexReader.close();
            oldMplexOut.remove();
        }
    }

}
