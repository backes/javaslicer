package de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences;

import java.io.DataInput;
import java.io.IOException;
import java.util.Iterator;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileReader;

public abstract class ConstantIntegerTraceSequence extends ConstantTraceSequence {

    public abstract Iterator<Integer> backwardIterator();

    public static ConstantIntegerTraceSequence readFrom(final DataInput in, final byte format, final MultiplexedFileReader file)
            throws IOException {
        if ((format & TraceSequence.FORMAT_GZIP) != 0) {
            throw new UnsupportedOperationException();
            //return ConstantGZipIntegerTraceSequence.readFrom(in, file);
        } else if ((format & TraceSequence.FORMAT_UNCOMPRESSED) != 0) {
            return ConstantUncompressedIntegerTraceSequence.readFrom(in, file);
        } else if ((format & TraceSequence.FORMAT_SWITCHING) != 0) {
            return ConstantSwitchingIntegerTraceSequence.readFrom(in, file);
        } else {
            throw new IOException("corrupted data (unknown format: " + format + ")");
        }
    }

}
