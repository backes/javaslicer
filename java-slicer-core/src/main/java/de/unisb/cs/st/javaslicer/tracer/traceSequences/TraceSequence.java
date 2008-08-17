package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.io.DataOutput;
import java.io.IOException;

public interface TraceSequence {

    public static enum Type { INTEGER, LONG, OBJECT }

    // some constants
    public static byte FORMAT_SEQUITUR = 0;
    public static byte FORMAT_GZIP = 1;
    public static byte FORMAT_UNCOMPRESSED = 2;

    public static byte TYPE_INTEGER = 0;
    public static byte TYPE_LONG = 1;

    /**
     * Each trace sequence has a globally unique index.
     * @return the index of this trace sequence
     */
    int getIndex();

    void writeOut(DataOutput out) throws IOException;

    void finish() throws IOException;

}
