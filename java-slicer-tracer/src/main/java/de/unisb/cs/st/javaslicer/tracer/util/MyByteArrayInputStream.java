package de.unisb.cs.st.javaslicer.tracer.util;

import java.io.IOException;
import java.io.InputStream;

public class MyByteArrayInputStream extends InputStream {

    private final byte[] buf;
    private int nextPos;

    public MyByteArrayInputStream(final byte[] buf) {
        this.buf = buf;
        this.nextPos = 0;
    }

    @Override
    public int read() throws IOException {
        if (this.nextPos >= this.buf.length)
            return -1;
        return this.buf[this.nextPos++] & 0xff;
    }

    @Override
    public int read(final byte b[], final int off, final int len) {
        if (b == null)
            throw new NullPointerException();
        if (off < 0 || len < 0 || len > b.length - off)
            throw new IndexOutOfBoundsException();

        final int read = Math.min(len, this.buf.length - this.nextPos);
        if (read <= 0)
            return len == 0 ? 0 : -1;

        System.arraycopy(this.buf, this.nextPos, b, off, read);
        this.nextPos += read;
        return read;
    }

    public void seek(final int pos) {
        if (pos < 0)
            throw new IllegalArgumentException("position for seek must be >= 0");
        this.nextPos = pos;
    }

}