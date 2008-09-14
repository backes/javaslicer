package de.unisb.cs.st.javaslicer.tracer.traceSequences.uncompressed;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.LongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.MyDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileWriter.MultiplexOutputStream;

public class UncompressedLongTraceSequence implements LongTraceSequence {

    private boolean ready = false;

    private final MyDataOutputStream dataOut;

    private final int streamIndex;

    public UncompressedLongTraceSequence(final Tracer tracer) throws IOException {
        final MultiplexOutputStream out = tracer.newOutputStream();
        this.dataOut = new MyDataOutputStream(getOutputStream(out));
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

    public void trace(final long value) throws IOException {
        assert !this.ready: "Trace cannot be extended any more";

        this.dataOut.writeLong(value);
    }

    public void writeOut(final DataOutput out) throws IOException {
        finish();

        out.writeByte(getFormat() | TYPE_LONG);
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
