package de.unisb.cs.st.javaslicer.tracer.traceSequences.uncompressed;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileWriter.MultiplexOutputStream;

public class UncompressedIntegerTraceSequence extends IntegerTraceSequence {

    private boolean ready = false;

    private int lastValue = 0;

    private final DataOutputStream dataOut;

    private final int streamIndex;

    public UncompressedIntegerTraceSequence(final int index, final Tracer tracer) throws IOException {
        super(index);
        final MultiplexOutputStream out = tracer.newOutputStream();
        this.dataOut = new DataOutputStream(getOutputStream(out));
        this.streamIndex = out.getId();
    }

    /**
     * Subclasses may override this
     */
    protected OutputStream getOutputStream(final MultiplexOutputStream out) throws IOException {
        return out;
    }

    @Override
    public void trace(final int value) throws IOException {
        if (this.ready)
            throw new RuntimeException("Trace cannot be extended any more");
        final int diffVal = value - this.lastValue;
        this.lastValue = value;

        this.dataOut.writeInt(diffVal);
    }

    public void writeOut(final DataOutput out) throws IOException {
        finish();

        out.writeByte(getFormat());
        out.writeByte(TYPE_INTEGER);
        out.writeInt(this.streamIndex);
    }

    protected byte getFormat() {
        return FORMAT_UNCOMPRESSED;
    }

    public void finish() throws IOException {
        if (this.ready)
            return;
        this.ready = true;
        this.dataOut.writeInt(this.lastValue);
        this.dataOut.close();
    }

}
