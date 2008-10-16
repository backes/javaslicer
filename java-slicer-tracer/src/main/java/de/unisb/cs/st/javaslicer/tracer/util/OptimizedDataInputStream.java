package de.unisb.cs.st.javaslicer.tracer.util;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class OptimizedDataInputStream extends FilterInputStream {

    private int lastInt = 0;
    private long lastLong = 0;
    private final boolean diff;

    public OptimizedDataInputStream(final InputStream in) {
        this(in, false);
    }

    public OptimizedDataInputStream(final InputStream in, final boolean diff) {
        super(in);
        this.diff = diff;
    }

    public OptimizedDataInputStream(final InputStream in, final int lastIntValue, final long lastLongValue) {
        this(in, true);
        this.lastInt = lastIntValue;
        this.lastLong = lastLongValue;
    }

    public static int readInt0(final InputStream in) throws IOException {
        int b0, b1, b2;
        int b3 = in.read();
        switch (b3) {
        case -1:
            throw new EOFException();
        case OptimizedDataOutputStream.MAGIC_1BYTE & 0xff:
            b0 = in.read();
            if (b0 < 0)
                throw new EOFException();
            return (byte)b0;
        case OptimizedDataOutputStream.MAGIC_2BYTES & 0xff:
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1) < 0)
                throw new EOFException();
            return ((byte)b1 << 8) | b0;
        case OptimizedDataOutputStream.MAGIC_3BYTES & 0xff:
            b2 = in.read();
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1 | b2) < 0)
                throw new EOFException();
            return ((byte)b2 << 16) | (b1 << 8) | b0;
        case OptimizedDataOutputStream.MAGIC_4BYTES & 0xff:
            b3 = in.read();
            b2 = in.read();
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1 | b2 | b3) < 0)
                throw new EOFException();
            return ((byte)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        default:
            return (byte)b3;
        }
    }

    public int readInt() throws IOException {
        final int readValue = readInt0(this.in);
        return this.diff ? this.lastInt += readValue  : readValue;
    }

    public static long readLong0(final InputStream in) throws IOException {
        int b0, b1, b2, b3, b4, b5, b6;
        int b7 = in.read();
        switch (b7) {
        case -1:
            throw new EOFException();
        case OptimizedDataOutputStream.MAGIC_1BYTE & 0xff:
            b0 = in.read();
            if (b0 < 0)
                throw new EOFException();
            return (byte)b0;
        case OptimizedDataOutputStream.MAGIC_2BYTES & 0xff:
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1) < 0)
                throw new EOFException();
            return ((byte)b1 << 8) | b0;
        case OptimizedDataOutputStream.MAGIC_3BYTES & 0xff:
            b2 = in.read();
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1 | b2) < 0)
                throw new EOFException();
            return ((byte)b2 << 16) | (b1 << 8) | b0;
        case OptimizedDataOutputStream.MAGIC_4BYTES & 0xff:
            b3 = in.read();
            b2 = in.read();
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1 | b2 | b3) < 0)
                throw new EOFException();
            return ((long)(byte)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        case OptimizedDataOutputStream.MAGIC_5BYTES & 0xff:
            b4 = in.read();
            b3 = in.read();
            b2 = in.read();
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1 | b2 | b3 | b4) < 0)
                throw new EOFException();
            return ((long)(byte)b4 << 32) | ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        case OptimizedDataOutputStream.MAGIC_6BYTES & 0xff:
            b5 = in.read();
            b4 = in.read();
            b3 = in.read();
            b2 = in.read();
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1 | b2 | b3 | b4 | b5) < 0)
                throw new EOFException();
            return ((long)(byte)b5 << 40) | ((long)b4 << 32) |
                ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        case OptimizedDataOutputStream.MAGIC_7BYTES & 0xff:
            b6 = in.read();
            b5 = in.read();
            b4 = in.read();
            b3 = in.read();
            b2 = in.read();
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1 | b2 | b3 | b4 | b5 | b6) < 0)
                throw new EOFException();
            return ((long)(byte)b6 << 48) | ((long)b5 << 40) | ((long)b4 << 32) |
                ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        case OptimizedDataOutputStream.MAGIC_8BYTES & 0xff:
            b7 = in.read();
            b6 = in.read();
            b5 = in.read();
            b4 = in.read();
            b3 = in.read();
            b2 = in.read();
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1 | b2 | b3 | b4 | b5 | b6 | b7) < 0)
                throw new EOFException();
            return ((long)(byte)b7 << 56) | ((long)b6 << 48) | ((long)b5 << 40) | ((long)b4 << 32) |
                ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        default:
            return (byte)b7;
        }
    }

    public long readLong() throws IOException {
        final long readValue = readLong0(this.in);
        return this.diff ? this.lastLong += readValue  : readValue;
    }

}
