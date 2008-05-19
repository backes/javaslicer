package de.unisb.cs.st.javaslicer.tracer.traceSequences.gzip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.LongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;

public class GZipLongTraceSequence extends LongTraceSequence {

    private boolean ready = false;

    private final ByteArrayOutputStream rawOut;
    private final GZIPOutputStream zipOut;

    public GZipLongTraceSequence(final int index) {
        super(index);
        this.rawOut = new ByteArrayOutputStream();
        try {
            this.zipOut = new GZIPOutputStream(this.rawOut);
        } catch (final IOException e) {
            // should never occur
            throw new RuntimeException(e);
        }
    }

    @Override
    public void trace(final long value) {
        if (this.ready)
            throw new RuntimeException("Trace cannot be extended any more");

        final byte[] buf = new byte[8];
        buf[0] = (byte) (value >> 56);
        buf[1] = (byte) (value >> 48);
        buf[2] = (byte) (value >> 40);
        buf[3] = (byte) (value >> 32);
        buf[4] = (byte) (value >> 24);
        buf[5] = (byte) (value >> 16);
        buf[6] = (byte) (value >> 8);
        buf[7] = (byte) value;
        try {
            this.zipOut.write(buf, 0, 8);
        } catch (final IOException e) {
            // should never occur
            throw new RuntimeException(e);
        }
    }

    public void writeOut(final ObjectOutputStream out) throws IOException {
        this.ready = true;
        out.writeByte(TraceSequence.FORMAT_GZIP);
        out.writeByte(TraceSequence.TYPE_LONG);
        final byte[] data = this.rawOut.toByteArray();
        out.writeInt(data.length);
        out.write(data);
    }

}
