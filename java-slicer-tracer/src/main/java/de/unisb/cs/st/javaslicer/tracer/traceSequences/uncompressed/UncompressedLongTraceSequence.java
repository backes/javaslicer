package de.unisb.cs.st.javaslicer.tracer.traceSequences.uncompressed;

import java.io.DataOutputStream;
import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.LongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.MyDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileWriter.MultiplexOutputStream;

public class UncompressedLongTraceSequence implements LongTraceSequence {

    private boolean ready = false;

    private final MyDataOutputStream dataOut;

    private final int streamIndex;

    public UncompressedLongTraceSequence(final Tracer tracer) {
        final MultiplexOutputStream out = tracer.newOutputStream();
        this.dataOut = new MyDataOutputStream(out);
        this.streamIndex = out.getId();
    }

    @Override
    public void trace(final long value) throws IOException {
        assert !this.ready: "Trace cannot be extended any more";

        this.dataOut.writeLong(value);
    }

    @Override
    public void writeOut(final DataOutputStream out) throws IOException {
        finish();

        out.writeByte(TYPE_LONG);
        out.writeInt(this.streamIndex);
    }

    @Override
    public void finish() throws IOException {
        if (this.ready)
            return;
        this.ready = true;
        this.dataOut.close();
    }

    @Override
    public boolean useMultiThreading() {
        return false;
    }
}
