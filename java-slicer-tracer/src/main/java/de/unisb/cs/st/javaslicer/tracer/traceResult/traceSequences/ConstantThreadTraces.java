package de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences;

import java.io.DataInputStream;
import java.io.IOException;

import de.hammacher.util.MultiplexedFileReader;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;

public class ConstantThreadTraces {

    private final byte format;

    public ConstantThreadTraces(final byte format) {
        this.format = format;
    }

    public static ConstantThreadTraces readFrom(final DataInputStream in) throws IOException {
        final byte format = in.readByte();
        switch (format) {
        case 0:
            // just for debugging (NullThreadTracer)
            return new ConstantThreadTraces((byte) 0);
        case TraceSequence.FORMAT_GZIP:
            return new ConstantThreadTraces(TraceSequence.FORMAT_GZIP);
        case TraceSequence.FORMAT_SEQUITUR:
            try {
                return new SequiturThreadTraces(in);
            } catch (final ClassNotFoundException e) {
                // this exception can occur in the ObjectInputStream that the sequences are read from
                throw new IOException(e);
            }
        case TraceSequence.FORMAT_UNCOMPRESSED:
            return new ConstantThreadTraces(TraceSequence.FORMAT_UNCOMPRESSED);
        default:
            throw new IOException("corrupted data (unknown trace sequence format)");
        }
    }

    public ConstantTraceSequence readSequence(final DataInputStream in, final MultiplexedFileReader file) throws IOException {
        final byte type = in.readByte();
        if ((type & TraceSequence.TYPE_INTEGER) != 0) {
            switch (this.format) {
            case TraceSequence.FORMAT_GZIP:
                return ConstantGZipIntegerTraceSequence.readFrom(in, file, type);
            case TraceSequence.FORMAT_UNCOMPRESSED:
                return ConstantUncompressedIntegerTraceSequence.readFrom(in, file);
            default:
                throw new AssertionError("should not get here");
            }
        } else if ((type & TraceSequence.TYPE_LONG) != 0) {
            switch (this.format) {
            case TraceSequence.FORMAT_GZIP:
                return ConstantGzipLongTraceSequence.readFrom(in, file, type);
            case TraceSequence.FORMAT_UNCOMPRESSED:
                return ConstantUncompressedLongTraceSequence.readFrom(in, file);
            default:
                throw new AssertionError("should not get here");
            }
        } else
            throw new IOException("corrupted data (unknown trace type)");
    }

}
