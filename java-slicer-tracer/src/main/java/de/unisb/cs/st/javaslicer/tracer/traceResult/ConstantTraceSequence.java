package de.unisb.cs.st.javaslicer.tracer.traceResult;

import java.io.IOException;
import java.io.ObjectInputStream;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;

public abstract class ConstantTraceSequence {

    public static ConstantTraceSequence readFrom(final ObjectInputStream in) throws IOException {
        final byte format = in.readByte();
        final byte type = in.readByte();
        if (type == TraceSequence.TYPE_INTEGER)
            return ConstantIntegerTraceSequence.readFrom(in, format);
        else if (type == TraceSequence.TYPE_LONG)
            return ConstantLongTraceSequence.readFrom(in, format);
        else
            throw new RuntimeException("Unknown type: " + type);
    }

}
