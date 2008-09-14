package de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences;

import java.io.DataInput;
import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileReader;

public abstract class ConstantTraceSequence {

    public static ConstantTraceSequence readFrom(final DataInput in, final MultiplexedFileReader file) throws IOException {
        final byte type = in.readByte();
        if ((type & TraceSequence.TYPE_INTEGER) != 0)
            return ConstantIntegerTraceSequence.readFrom(in, type, file);
        else if ((type & TraceSequence.TYPE_LONG) != 0)
            return ConstantLongTraceSequence.readFrom(in, type, file);
        else
            throw new IOException("corrupted data (unknown trace type)");
    }

}
