package de.unisb.cs.st.javaslicer.tracer.traceResult;

import java.io.DataInput;
import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileReader;

public abstract class ConstantTraceSequence {

    public static ConstantTraceSequence readFrom(final DataInput in, final MultiplexedFileReader file) throws IOException {
        final byte format = in.readByte();
        final byte type = in.readByte();
        if (type == TraceSequence.TYPE_INTEGER)
            return ConstantIntegerTraceSequence.readFrom(in, format, file);
        else if (type == TraceSequence.TYPE_LONG)
            return ConstantLongTraceSequence.readFrom(in, format, file);
        else
            throw new IOException("corrupted data (unknown trace type)");
    }

}
