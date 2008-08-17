package de.unisb.cs.st.javaslicer.tracer.util;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class OptimizedDataInputStream extends FilterInputStream {

    public OptimizedDataInputStream(final InputStream in) {
        super(in);
    }

    public int readInt() throws IOException {
        int b0, b1, b2;
        int b3 = this.in.read();
        switch (b3) {
        case -1:
            throw new EOFException();
        case OptimizedDataOutputStream.MAGIC_1BYTE & 0xff:
            b0 = this.in.read();
            if (b0 < 0)
                throw new EOFException();
            return b0;
        case OptimizedDataOutputStream.MAGIC_2BYTES & 0xff:
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1) < 0)
                throw new EOFException();
            return (b1 << 8) | b0;
        case OptimizedDataOutputStream.MAGIC_3BYTES & 0xff:
            b2 = this.in.read();
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1 | b2) < 0)
                throw new EOFException();
            return (b2 << 16) | (b1 << 8) | b0;
        case OptimizedDataOutputStream.MAGIC_4BYTES & 0xff:
            b3 = this.in.read();
            b2 = this.in.read();
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1 | b2 | b3) < 0)
                throw new EOFException();
            return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        default:
            return (byte)b3;
        }
    }

    public long readLong() throws IOException {
        int b0, b1, b2, b3, b4, b5, b6;
        int b7 = this.in.read();
        switch (b7) {
        case -1:
            throw new EOFException();
        case OptimizedDataOutputStream.MAGIC_1BYTE & 0xff:
            b0 = this.in.read();
            if (b0 < 0)
                throw new EOFException();
            return b0;
        case OptimizedDataOutputStream.MAGIC_2BYTES & 0xff:
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1) < 0)
                throw new EOFException();
            return (b1 << 8) | b0;
        case OptimizedDataOutputStream.MAGIC_3BYTES & 0xff:
            b2 = this.in.read();
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1 | b2) < 0)
                throw new EOFException();
            return (b2 << 16) | (b1 << 8) | b0;
        case OptimizedDataOutputStream.MAGIC_4BYTES & 0xff:
            b3 = this.in.read();
            b2 = this.in.read();
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1 | b2 | b3) < 0)
                throw new EOFException();
            return ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        case OptimizedDataOutputStream.MAGIC_5BYTES & 0xff:
            b4 = this.in.read();
            b3 = this.in.read();
            b2 = this.in.read();
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1 | b2 | b3 | b4) < 0)
                throw new EOFException();
            return ((long)b4 << 32) | ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        case OptimizedDataOutputStream.MAGIC_6BYTES & 0xff:
            b5 = this.in.read();
            b4 = this.in.read();
            b3 = this.in.read();
            b2 = this.in.read();
            b1 = this.in.read();
            b0 = this.in.read();
            if ((b0 | b1 | b2 | b3 | b4 | b5) < 0)
                throw new EOFException();
            return ((long)b5 << 40) | ((long)b4 << 32) |
                ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
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
            return ((long)b6 << 48) | ((long)b5 << 40) | ((long)b4 << 32) |
                ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
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
            return ((long)b7 << 56) | ((long)b6 << 48) | ((long)b5 << 40) | ((long)b4 << 32) |
                ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        default:
            return (byte)b7;
        }
    }

}
