package de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences;

import java.io.DataInputStream;
import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileReader;

public class ConstantThreadTraces {

    private final byte format;

    public ConstantThreadTraces(final byte format) {
        this.format = format;
    }

    public static ConstantThreadTraces readFrom(final DataInputStream in) throws IOException {
        final byte format = in.readByte();
        if ((format & TraceSequence.FORMAT_GZIP) != 0)
            return new ConstantThreadTraces(TraceSequence.FORMAT_GZIP);
        else if ((format & TraceSequence.FORMAT_SEQUITUR) != 0)
            try {
                return new SequiturThreadTraces(in);
            } catch (final ClassNotFoundException e) {
                throw new IOException(e);
            }
        else if ((format & TraceSequence.FORMAT_SWITCHING) != 0)
            return new ConstantThreadTraces(TraceSequence.FORMAT_SWITCHING);
        else if ((format & TraceSequence.FORMAT_UNCOMPRESSED) != 0)
            return new ConstantThreadTraces(TraceSequence.FORMAT_UNCOMPRESSED);
        else if (format == 0)
            return new ConstantThreadTraces((byte) 0);
        else
            throw new IOException("corrupted data (unknown trace sequence format)");
    }

    public ConstantTraceSequence readSequence(final DataInputStream in, final MultiplexedFileReader file) throws IOException {
        final byte type = in.readByte();
        if ((type & TraceSequence.TYPE_INTEGER) != 0) {
            switch (this.format) {
            case TraceSequence.FORMAT_GZIP:
                throw new UnsupportedOperationException();
            case TraceSequence.FORMAT_SWITCHING:
                return ConstantSwitchingIntegerTraceSequence.readFrom(in, file);
            case TraceSequence.FORMAT_UNCOMPRESSED:
                return ConstantUncompressedIntegerTraceSequence.readFrom(in, file);
            default:
                throw new AssertionError("should not get here");
            }
        } else if ((type & TraceSequence.TYPE_LONG) != 0) {
            switch (this.format) {
            case TraceSequence.FORMAT_GZIP:
                throw new UnsupportedOperationException();
            case TraceSequence.FORMAT_SWITCHING:
                return ConstantSwitchingLongTraceSequence.readFrom(in, file);
            case TraceSequence.FORMAT_UNCOMPRESSED:
                return ConstantUncompressedLongTraceSequence.readFrom(in, file);
            default:
                throw new AssertionError("should not get here");
            }
        } else
            throw new IOException("corrupted data (unknown trace type)");
    }

}
