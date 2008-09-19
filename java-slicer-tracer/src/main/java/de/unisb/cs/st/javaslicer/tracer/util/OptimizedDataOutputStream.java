package de.unisb.cs.st.javaslicer.tracer.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class OptimizedDataOutputStream extends FilterOutputStream {

    protected static final byte MAGIC_1BYTE      = (byte) -128;
    protected static final byte MAGIC_2BYTES     = (byte) -127;
    protected static final byte MAGIC_3BYTES     = (byte) -126;
    protected static final byte MAGIC_4BYTES     = (byte) -125;
    protected static final byte MAGIC_5BYTES     = (byte) 127;
    protected static final byte MAGIC_6BYTES     = (byte) 126;
    protected static final byte MAGIC_7BYTES     = (byte) 125;
    protected static final byte MAGIC_8BYTES     = (byte) 124;
    private static final int  LOWER_7_BITS  = 0x7f;
    private static final int  LOWER_15_BITS = 0x7fff;
    private static final int  LOWER_23_BITS = 0x7fffff;
    private static final int  LOWER_31_BITS = 0x7fffffff;
    private static final long LOWER_39_BITS = 0x7fffffffffL;
    private static final long LOWER_47_BITS = 0x7fffffffffffL;
    private static final long LOWER_55_BITS = 0x7fffffffffffffL;

    private int lastInt = 0;
    private long lastLong = 0;
    private final boolean diff;

    public OptimizedDataOutputStream(final OutputStream out) {
        this(out, false);
    }

    public OptimizedDataOutputStream(final OutputStream out, final boolean diff) {
        super(out);
        this.diff = diff;
    }

    public OptimizedDataOutputStream(final OutputStream out, final int lastIntValue, final long lastLongValue) {
        this(out, true);
        this.lastInt = lastIntValue;
        this.lastLong = lastLongValue;
    }

    public void writeInt(final int value) throws IOException {
        int write;
        if (this.diff) {
            write = value - this.lastInt;
            this.lastInt = value;
        } else
            write = value;
        if ((write | ~LOWER_7_BITS) == write || (write & LOWER_7_BITS) == write) {
            if (write < -124)
                this.out.write(MAGIC_1BYTE);
            this.out.write(write);
        } else if ((write | ~LOWER_15_BITS) == write || (write & LOWER_15_BITS) == write) {
            this.out.write(MAGIC_2BYTES);
            this.out.write(write >> 8);
            this.out.write(write);
        } else if ((write | ~LOWER_23_BITS) == write || (write & LOWER_23_BITS) == write) {
            this.out.write(MAGIC_3BYTES);
            this.out.write(write >> 16);
            this.out.write(write >> 8);
            this.out.write(write);
        } else {
            this.out.write(MAGIC_4BYTES);
            this.out.write(write >> 24);
            this.out.write(write >> 16);
            this.out.write(write >> 8);
            this.out.write(write);
        }
    }

    public void writeLong(final long value) throws IOException {
        long write;
        if (this.diff) {
            write = value - this.lastLong;
            this.lastLong = value;
        } else
            write = value;
        if ((write | ~LOWER_7_BITS) == write || (write & LOWER_7_BITS) == write) {
            if (write < -124 || write > 123)
                this.out.write(MAGIC_1BYTE);
            this.out.write((int) write);
        } else if ((write | ~LOWER_15_BITS) == write || (write & LOWER_15_BITS) == write) {
            this.out.write(MAGIC_2BYTES);
            this.out.write((int)(write >> 8));
            this.out.write((int)write);
        } else if ((write | ~LOWER_23_BITS) == write || (write & LOWER_23_BITS) == write) {
            this.out.write(MAGIC_3BYTES);
            this.out.write((int)(write >> 16));
            this.out.write((int)(write >> 8));
            this.out.write((int)write);
        } else if ((write | ~LOWER_31_BITS) == write || (write & LOWER_31_BITS) == write) {
            this.out.write(MAGIC_4BYTES);
            this.out.write((int)(write >> 24));
            this.out.write((int)(write >> 16));
            this.out.write((int)(write >> 8));
            this.out.write((int)write);
        } else if ((write | ~LOWER_39_BITS) == write || (write & LOWER_39_BITS) == write) {
            this.out.write(MAGIC_5BYTES);
            this.out.write((int)(write >> 32));
            this.out.write((int)(write >> 24));
            this.out.write((int)(write >> 16));
            this.out.write((int)(write >> 8));
            this.out.write((int)write);
        } else if ((write | ~LOWER_47_BITS) == write || (write & LOWER_47_BITS) == write) {
            this.out.write(MAGIC_6BYTES);
            this.out.write((int)(write >> 40));
            this.out.write((int)(write >> 32));
            this.out.write((int)(write >> 24));
            this.out.write((int)(write >> 16));
            this.out.write((int)(write >> 8));
            this.out.write((int)write);
        } else if ((write | ~LOWER_55_BITS) == write || (write & LOWER_55_BITS) == write) {
            this.out.write(MAGIC_7BYTES);
            this.out.write((int)(write >> 48));
            this.out.write((int)(write >> 40));
            this.out.write((int)(write >> 32));
            this.out.write((int)(write >> 24));
            this.out.write((int)(write >> 16));
            this.out.write((int)(write >> 8));
            this.out.write((int)write);
        } else {
            this.out.write(MAGIC_8BYTES);
            this.out.write((int)(write >> 56));
            this.out.write((int)(write >> 48));
            this.out.write((int)(write >> 40));
            this.out.write((int)(write >> 32));
            this.out.write((int)(write >> 24));
            this.out.write((int)(write >> 16));
            this.out.write((int)(write >> 8));
            this.out.write((int)write);
        }
    }

    public int getLastIntValue() {
        return this.lastInt;
    }

    public long getLastLongValue() {
        return this.lastLong;
    }

}
