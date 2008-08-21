package de.unisb.cs.st.javaslicer.tracer.util;

import java.io.IOException;
import java.io.OutputStream;

public class MyDataOutputStream extends OutputStream {

    private final OutputStream out;

    public MyDataOutputStream(final OutputStream out) {
        super();
        this.out = out;
    }

    public void writeInt(final int value) throws IOException {
        this.out.write(value >>> 24);
        this.out.write(value >>> 16);
        this.out.write(value >>> 8);
        this.out.write(value);
    }

    public void writeLong(final long value) throws IOException {
        this.out.write((int)(value >>> 56));
        this.out.write((int)(value >>> 48));
        this.out.write((int)(value >>> 40));
        this.out.write((int)(value >>> 32));
        this.out.write((int)(value >>> 24));
        this.out.write((int)(value >>> 16));
        this.out.write((int)(value >>> 8));
        this.out.write((int)value);
    }

    @Override
    public void write(final int b) throws IOException {
        this.out.write(b);
    }

    @Override
    public void close() throws IOException {
        this.out.close();
    }

    @Override
    public void flush() throws IOException {
        this.out.flush();
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        this.out.write(b, off, len);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        this.out.write(b);
    }

}
