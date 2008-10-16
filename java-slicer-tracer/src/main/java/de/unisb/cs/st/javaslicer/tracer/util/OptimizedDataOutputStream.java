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
        if (this.diff) {
            writeInt0(value - this.lastInt, this.out);
            this.lastInt = value;
        } else
            writeInt0(value, this.out);
    }

    public static void writeInt0(final int value, final OutputStream out) throws IOException {
        if ((value | ~LOWER_7_BITS) == value || (value & LOWER_7_BITS) == value) {
            if (value < -124)
                out.write(MAGIC_1BYTE);
            out.write(value);
        } else if ((value | ~LOWER_15_BITS) == value || (value & LOWER_15_BITS) == value) {
            out.write(MAGIC_2BYTES);
            out.write(value >>> 8);
            out.write(value);
        } else if ((value | ~LOWER_23_BITS) == value || (value & LOWER_23_BITS) == value) {
            out.write(MAGIC_3BYTES);
            out.write(value >>> 16);
            out.write(value >>> 8);
            out.write(value);
        } else {
            out.write(MAGIC_4BYTES);
            out.write(value >>> 24);
            out.write(value >>> 16);
            out.write(value >>> 8);
            out.write(value);
        }
    }

    public void writeLong(final long value) throws IOException {
        if (this.diff) {
            writeLong0(value - this.lastLong, this.out);
            this.lastLong = value;
        } else
            writeLong0(value, this.out);
    }

    public static void writeLong0(final long value, final OutputStream out) throws IOException {
        if ((value | ~LOWER_7_BITS) == value || (value & LOWER_7_BITS) == value) {
            if (value < -124 || value > 123)
                out.write(MAGIC_1BYTE);
            out.write((int) value);
        } else if ((value | ~LOWER_15_BITS) == value || (value & LOWER_15_BITS) == value) {
            out.write(MAGIC_2BYTES);
            out.write((int)(value >>> 8));
            out.write((int)value);
        } else if ((value | ~LOWER_23_BITS) == value || (value & LOWER_23_BITS) == value) {
            out.write(MAGIC_3BYTES);
            out.write((int)(value >>> 16));
            out.write((int)(value >>> 8));
            out.write((int)value);
        } else if ((value | ~LOWER_31_BITS) == value || (value & LOWER_31_BITS) == value) {
            out.write(MAGIC_4BYTES);
            out.write((int)(value >>> 24));
            out.write((int)(value >>> 16));
            out.write((int)(value >>> 8));
            out.write((int)value);
        } else if ((value | ~LOWER_39_BITS) == value || (value & LOWER_39_BITS) == value) {
            out.write(MAGIC_5BYTES);
            out.write((int)(value >>> 32));
            out.write((int)(value >>> 24));
            out.write((int)(value >>> 16));
            out.write((int)(value >>> 8));
            out.write((int)value);
        } else if ((value | ~LOWER_47_BITS) == value || (value & LOWER_47_BITS) == value) {
            out.write(MAGIC_6BYTES);
            out.write((int)(value >>> 40));
            out.write((int)(value >>> 32));
            out.write((int)(value >>> 24));
            out.write((int)(value >>> 16));
            out.write((int)(value >>> 8));
            out.write((int)value);
        } else if ((value | ~LOWER_55_BITS) == value || (value & LOWER_55_BITS) == value) {
            out.write(MAGIC_7BYTES);
            out.write((int)(value >>> 48));
            out.write((int)(value >>> 40));
            out.write((int)(value >>> 32));
            out.write((int)(value >>> 24));
            out.write((int)(value >>> 16));
            out.write((int)(value >>> 8));
            out.write((int)value);
        } else {
            out.write(MAGIC_8BYTES);
            out.write((int)(value >>> 56));
            out.write((int)(value >>> 48));
            out.write((int)(value >>> 40));
            out.write((int)(value >>> 32));
            out.write((int)(value >>> 24));
            out.write((int)(value >>> 16));
            out.write((int)(value >>> 8));
            out.write((int)value);
        }
    }

    public int getLastIntValue() {
        return this.lastInt;
    }

    public long getLastLongValue() {
        return this.lastLong;
    }

}
