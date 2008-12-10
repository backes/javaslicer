package de.unisb.cs.st.javaslicer.tracer.sequitur.input;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

// package-private
class DataInput {

    private static final byte MAGIC_1BYTE      = (byte) 255;
    private static final byte MAGIC_2BYTES     = (byte) 254;
    private static final byte MAGIC_3BYTES     = (byte) 253;
    private static final byte MAGIC_4BYTES     = (byte) 252;
    private static final byte MAGIC_5BYTES     = (byte) 251;
    private static final byte MAGIC_6BYTES     = (byte) 250;
    private static final byte MAGIC_7BYTES     = (byte) 249;
    private static final byte MAGIC_8BYTES     = (byte) 248;

    private DataInput() {
        // cannot be instantiated
    }

    public static int readInt(final InputStream in) throws IOException {
        int b0, b1, b2;
        int b3 = in.read();
        switch (b3) {
        case -1:
            throw new EOFException();
        case MAGIC_1BYTE & 0xff:
            b0 = in.read();
            if (b0 < 0)
                throw new EOFException();
            return b0;
        case MAGIC_2BYTES & 0xff:
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1) < 0)
                throw new EOFException();
            return (b1 << 8) | b0;
        case MAGIC_3BYTES & 0xff:
            b2 = in.read();
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1 | b2) < 0)
                throw new EOFException();
            return  (b2 << 16) | (b1 << 8) | b0;
        case MAGIC_4BYTES & 0xff:
            b3 = in.read();
            b2 = in.read();
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1 | b2 | b3) < 0)
                throw new EOFException();
            return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        default:
            return  b3;
        }
    }

    public static long readLong(final InputStream in) throws IOException {
        int b0, b1, b2, b3, b4, b5, b6;
        int b7 = in.read();
        switch (b7) {
        case -1:
            throw new EOFException();
        case MAGIC_1BYTE & 0xff:
            b0 = in.read();
            if (b0 < 0)
                throw new EOFException();
            return b0;
        case MAGIC_2BYTES & 0xff:
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1) < 0)
                throw new EOFException();
            return (b1 << 8) | b0;
        case MAGIC_3BYTES & 0xff:
            b2 = in.read();
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1 | b2) < 0)
                throw new EOFException();
            return (b2 << 16) | (b1 << 8) | b0;
        case MAGIC_4BYTES & 0xff:
            b3 = in.read();
            b2 = in.read();
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1 | b2 | b3) < 0)
                throw new EOFException();
            return ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        case MAGIC_5BYTES & 0xff:
            b4 = in.read();
            b3 = in.read();
            b2 = in.read();
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1 | b2 | b3 | b4) < 0)
                throw new EOFException();
            return ((long)b4 << 32) | ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        case MAGIC_6BYTES & 0xff:
            b5 = in.read();
            b4 = in.read();
            b3 = in.read();
            b2 = in.read();
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1 | b2 | b3 | b4 | b5) < 0)
                throw new EOFException();
            return ((long)b5 << 40) | ((long)b4 << 32) |
                ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        case MAGIC_7BYTES & 0xff:
            b6 = in.read();
            b5 = in.read();
            b4 = in.read();
            b3 = in.read();
            b2 = in.read();
            b1 = in.read();
            b0 = in.read();
            if ((b0 | b1 | b2 | b3 | b4 | b5 | b6) < 0)
                throw new EOFException();
            return ((long)b6 << 48) | ((long)b5 << 40) | ((long)b4 << 32) |
                ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        case MAGIC_8BYTES & 0xff:
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
            return ((long)b7 << 56) | ((long)b6 << 48) | ((long)b5 << 40) | ((long)b4 << 32) |
                ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        default:
            return b7;
        }
    }

}
