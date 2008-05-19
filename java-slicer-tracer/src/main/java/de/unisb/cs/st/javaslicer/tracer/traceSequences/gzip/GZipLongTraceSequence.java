package de.unisb.cs.st.javaslicer.tracer.traceSequences.gzip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.LongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;

public class GZipLongTraceSequence extends LongTraceSequence {

    private boolean started = false;
    private boolean ready = false;

    private long lastValue;

    private ByteArrayOutputStream rawOut;
    private GZIPOutputStream zipOut;

    public GZipLongTraceSequence(final int index) {
        super(index);
    }

    @Override
    public void trace(final long value) {
        long diffVal = value;
        if (!this.started) {
            this.rawOut = new ByteArrayOutputStream();
            try {
                this.zipOut = new GZIPOutputStream(this.rawOut);
            } catch (final IOException e) {
                // should never occur
                throw new RuntimeException(e);
            }
            this.started = true;
        } else if (this.ready) {
            throw new RuntimeException("Trace cannot be extended any more");
        } else {
            diffVal -= this.lastValue;
            this.lastValue = value;
        }

        final byte[] buf = new byte[8];
        if (diffVal != 0) {
            buf[0] = (byte) (diffVal >> 56);
            buf[1] = (byte) (diffVal >> 48);
            buf[2] = (byte) (diffVal >> 40);
            buf[3] = (byte) (diffVal >> 32);
            buf[4] = (byte) (diffVal >> 24);
            buf[5] = (byte) (diffVal >> 16);
            buf[6] = (byte) (diffVal >> 8);
            buf[7] = (byte) diffVal;
        }
        try {
            this.zipOut.write(buf, 0, 8);
        } catch (final IOException e) {
            // should never occur
            throw new RuntimeException(e);
        }
    }

    public void writeOut(final ObjectOutputStream out) throws IOException {
        final byte[] data;
        if (!this.started) {
            data = new byte[0];
        } else {
            if (!this.ready) {
                this.zipOut.close();
                this.ready = true;
            }
            data = this.rawOut.toByteArray();
        }
        out.writeByte(TraceSequence.FORMAT_GZIP);
        out.writeByte(TraceSequence.TYPE_LONG);
        out.writeInt(data.length);
        out.write(data);
    }

}
