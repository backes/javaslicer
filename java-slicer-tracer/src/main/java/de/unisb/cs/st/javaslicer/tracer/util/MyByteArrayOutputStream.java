package de.unisb.cs.st.javaslicer.tracer.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream that stores written data in the underlying byte array,
 * but does not extend it when it's full.
 *
 * @author Clemens Hammacher
 */
public class MyByteArrayOutputStream extends OutputStream {

    private final byte[] buf;
    private int nextPos;

    public MyByteArrayOutputStream(final byte[] buf) {
        this.buf = buf;
        this.nextPos = 0;
    }

    @Override
    public void write(final int b) throws IOException {
        if (this.nextPos >= this.buf.length)
            throw new IOException("Buffer overflow");
        this.buf[this.nextPos++] = (byte) b;
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        if (b == null)
            throw new NullPointerException();
        if (off < 0 || len < 0 || len > b.length - off)
            throw new IndexOutOfBoundsException();
        if (this.nextPos + len > this.buf.length)
            throw new IOException("Buffer overflow");

        System.arraycopy(b, off, this.buf, this.nextPos, len);
        this.nextPos += len;
    }

    public void seek(final int pos) {
        if (pos < 0)
            throw new IllegalArgumentException("position for seek must be >= 0");
        this.nextPos = pos;
    }

}