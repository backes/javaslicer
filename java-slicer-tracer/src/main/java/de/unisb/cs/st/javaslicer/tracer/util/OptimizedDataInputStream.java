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

    public int readInt() throws IOException {
        int b0, b1, b2;
        int b3 = this.in.read();
        int readValue;
        switch (b3) {
        case -1:
            throw new EOFException();
        case OptimizedDataOutputStream.MAGIC_1BYTE & 0xff:
            b0 = this.in.read();
            if (b0 < 0)
                throw new EOFException();
            readValue = (byte)b0;
            break;
        case OptimizedDataOutputStream.MAGIC_2BYTES & 0xff:
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1) < 0)
                throw new EOFException();
            readValue = ((byte)b1 << 8) | b0;
            break;
        case OptimizedDataOutputStream.MAGIC_3BYTES & 0xff:
            b2 = this.in.read();
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1 | b2) < 0)
                throw new EOFException();
            readValue = ((byte)b2 << 16) | (b1 << 8) | b0;
            break;
        case OptimizedDataOutputStream.MAGIC_4BYTES & 0xff:
            b3 = this.in.read();
            b2 = this.in.read();
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1 | b2 | b3) < 0)
                throw new EOFException();
            readValue = ((byte)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
            break;
        default:
            readValue = (byte)b3;
            break;
        }
        return this.diff ? this.lastInt += readValue : readValue;
    }

    public long readLong() throws IOException {
        int b0, b1, b2, b3, b4, b5, b6;
        int b7 = this.in.read();
        long readValue;
        switch (b7) {
        case -1:
            throw new EOFException();
        case OptimizedDataOutputStream.MAGIC_1BYTE & 0xff:
            b0 = this.in.read();
            if (b0 < 0)
                throw new EOFException();
            readValue = (byte)b0;
            break;
        case OptimizedDataOutputStream.MAGIC_2BYTES & 0xff:
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1) < 0)
                throw new EOFException();
            readValue = ((byte)b1 << 8) | b0;
            break;
        case OptimizedDataOutputStream.MAGIC_3BYTES & 0xff:
            b2 = this.in.read();
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1 | b2) < 0)
                throw new EOFException();
            readValue = ((byte)b2 << 16) | (b1 << 8) | b0;
            break;
        case OptimizedDataOutputStream.MAGIC_4BYTES & 0xff:
            b3 = this.in.read();
            b2 = this.in.read();
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1 | b2 | b3) < 0)
                throw new EOFException();
            readValue = ((long)(byte)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
            break;
        case OptimizedDataOutputStream.MAGIC_5BYTES & 0xff:
            b4 = this.in.read();
            b3 = this.in.read();
            b2 = this.in.read();
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1 | b2 | b3 | b4) < 0)
                throw new EOFException();
            readValue = ((long)(byte)b4 << 32) | ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
            break;
        case OptimizedDataOutputStream.MAGIC_6BYTES & 0xff:
            b5 = this.in.read();
            b4 = this.in.read();
            b3 = this.in.read();
            b2 = this.in.read();
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1 | b2 | b3 | b4 | b5) < 0)
                throw new EOFException();
            readValue = ((long)(byte)b5 << 40) | ((long)b4 << 32) |
                ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
            break;
        case OptimizedDataOutputStream.MAGIC_7BYTES & 0xff:
            b6 = this.in.read();
            b5 = this.in.read();
            b4 = this.in.read();
            b3 = this.in.read();
            b2 = this.in.read();
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1 | b2 | b3 | b4 | b5 | b6) < 0)
                throw new EOFException();
            readValue = ((long)(byte)b6 << 48) | ((long)b5 << 40) | ((long)b4 << 32) |
                ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
            break;
        case OptimizedDataOutputStream.MAGIC_8BYTES & 0xff:
            b7 = this.in.read();
            b6 = this.in.read();
            b5 = this.in.read();
            b4 = this.in.read();
            b3 = this.in.read();
            b2 = this.in.read();
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1 | b2 | b3 | b4 | b5 | b6 | b7) < 0)
                throw new EOFException();
            readValue = ((long)(byte)b7 << 56) | ((long)b6 << 48) | ((long)b5 << 40) | ((long)b4 << 32) |
                ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
            break;
        default:
            readValue = (byte)b7;
            break;
        }
        return this.diff ? this.lastLong += readValue : readValue;
    }

}
