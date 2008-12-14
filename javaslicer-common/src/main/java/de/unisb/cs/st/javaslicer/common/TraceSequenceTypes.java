package de.unisb.cs.st.javaslicer.common;

public interface TraceSequenceTypes {

    public static enum Type { INTEGER, LONG }

    // some constants
    public static final byte FORMAT_SEQUITUR = 1<<0;
    public static final byte FORMAT_GZIP = 1<<1;
    public static final byte FORMAT_UNCOMPRESSED = 1<<2;

    public static final byte TYPE_INTEGER = 1<<5;
    public static final byte TYPE_LONG = 1<<6;

}
