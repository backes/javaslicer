package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.io.DataOutputStream;
import java.io.IOException;

public interface TraceSequence {

    public interface IntegerTraceSequence extends TraceSequence {
        public abstract void trace(final int value) throws IOException;
    }

    public interface LongTraceSequence extends TraceSequence {
        public abstract void trace(final long value) throws IOException;
    }

    public static enum Type { INTEGER, LONG }

    // some constants
    public static final byte FORMAT_SEQUITUR = 1<<0;
    public static final byte FORMAT_GZIP = 1<<1;
    public static final byte FORMAT_UNCOMPRESSED = 1<<2;

    public static final byte TYPE_INTEGER = 1<<5;
    public static final byte TYPE_LONG = 1<<6;

    void writeOut(DataOutputStream out) throws IOException;

    void finish() throws IOException;

    boolean useMultiThreading();

}
