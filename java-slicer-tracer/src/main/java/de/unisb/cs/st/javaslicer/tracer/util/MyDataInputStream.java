package de.unisb.cs.st.javaslicer.tracer.util;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MyDataInputStream extends FilterInputStream {

    public MyDataInputStream(final InputStream in) {
        super(in);
    }

    public int readInt() throws IOException {
        final int b3 = this.in.read();
        final int b2 = this.in.read();
        final int b1 = this.in.read();
        final int b0 = this.in.read();
        if ((b0 | b1 | b2 | b3) < 0)
            throw new EOFException();
        return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
    }

    public long readLong() throws IOException {
        final int b7 = this.in.read();
        final int b6 = this.in.read();
        final int b5 = this.in.read();
        final int b4 = this.in.read();
        final int b3 = this.in.read();
        final int b2 = this.in.read();
        final int b1 = this.in.read();
        final int b0 = this.in.read();
        if ((b0 | b1 | b2 | b3 | b4 | b5 | b6 | b7) < 0)
            throw new EOFException();
        return ((long)b7 << 56) | ((long)b6 << 48) | ((long)b5 << 40) | ((long)b4 << 32) |
            ((long)b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
    }

}
