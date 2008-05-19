package de.unisb.cs.st.javaslicer.tracer.traceSequences.gzip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;

public class GZipIntegerTraceSequence extends IntegerTraceSequence {

    private boolean ready = false;

    private final ByteArrayOutputStream rawOut;
    private final GZIPOutputStream zipOut;

    public GZipIntegerTraceSequence(final int index) {
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
    public void trace(final int value) {
        if (this.ready)
            throw new RuntimeException("Trace cannot be extended any more");

        final byte[] buf = new byte[4];
        buf[0] = (byte) (value >> 24);
        buf[1] = (byte) (value >> 16);
        buf[2] = (byte) (value >> 8);
        buf[3] = (byte) value;
        try {
            this.zipOut.write(buf, 0, 4);
        } catch (final IOException e) {
            // should never occur
            throw new RuntimeException(e);
        }
    }

    public void writeOut(final ObjectOutputStream out) throws IOException {
        this.ready = true;
        out.writeByte(TraceSequence.FORMAT_GZIP);
        out.writeByte(TraceSequence.TYPE_INTEGER);
        final byte[] data = this.rawOut.toByteArray();
        out.writeInt(data.length);
        out.write(data);
    }

}
