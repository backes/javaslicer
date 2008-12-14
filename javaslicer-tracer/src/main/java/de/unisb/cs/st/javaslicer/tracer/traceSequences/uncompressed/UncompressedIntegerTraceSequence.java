package de.unisb.cs.st.javaslicer.tracer.traceSequences.uncompressed;

import java.io.DataOutputStream;
import java.io.IOException;

import de.hammacher.util.MyDataOutputStream;
import de.hammacher.util.MultiplexedFileWriter.MultiplexOutputStream;
import de.unisb.cs.st.javaslicer.common.TraceSequenceTypes;
import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.IntegerTraceSequence;

public class UncompressedIntegerTraceSequence implements IntegerTraceSequence {

    private boolean ready = false;

    private final MyDataOutputStream dataOut;

    private final int streamIndex;

    public UncompressedIntegerTraceSequence(final Tracer tracer) {
        final MultiplexOutputStream out = tracer.newOutputStream();
        this.dataOut = new MyDataOutputStream(out);
        this.streamIndex = out.getId();
    }

    @Override
    public void trace(final int value) throws IOException {
        assert !this.ready: "Trace cannot be extended any more";

        this.dataOut.writeInt(value);
    }

    @Override
    public void writeOut(final DataOutputStream out) throws IOException {
        finish();

        out.writeByte(TraceSequenceTypes.TYPE_INTEGER);
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
