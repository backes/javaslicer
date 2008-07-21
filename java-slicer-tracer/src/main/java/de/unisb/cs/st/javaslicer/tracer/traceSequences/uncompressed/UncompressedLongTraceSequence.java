package de.unisb.cs.st.javaslicer.tracer.traceSequences.uncompressed;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.LongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileWriter.MultiplexOutputStream;

public class UncompressedLongTraceSequence extends LongTraceSequence {

    private boolean ready = false;

    private final DataOutputStream dataOut;

    private final int streamIndex;

    public UncompressedLongTraceSequence(final int index, final Tracer tracer) throws IOException {
        super(index);
        final MultiplexOutputStream out = tracer.newOutputStream();
        this.dataOut = new DataOutputStream(getOutputStream(out));
        this.streamIndex = out.getId();
    }

    /**
     * Subclasses may override this.
     *
     * @throws IOException if an I/O error occures
     */
    protected OutputStream getOutputStream(final MultiplexOutputStream out) throws IOException {
        return out;
    }

    @Override
    public void trace(final long value) throws IOException {
        if (this.ready)
            throw new RuntimeException("Trace cannot be extended any more");

        this.dataOut.writeLong(value);
    }

    public void writeOut(final DataOutput out) throws IOException {
        finish();

        out.writeByte(getFormat());
        out.writeByte(TYPE_LONG);
        out.writeInt(this.streamIndex);
    }

    protected byte getFormat() {
        return FORMAT_UNCOMPRESSED;
    }

    public void finish() throws IOException {
        if (this.ready)
            return;
        this.ready = true;
        this.dataOut.close();
    }

}
