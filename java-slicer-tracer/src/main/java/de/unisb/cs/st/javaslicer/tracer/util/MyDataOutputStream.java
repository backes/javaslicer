package de.unisb.cs.st.javaslicer.tracer.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MyDataOutputStream extends FilterOutputStream {

    public MyDataOutputStream(final OutputStream out) {
        super(out);
    }

    public void writeInt(final int value) throws IOException {
        this.out.write(value >>> 24);
        this.out.write(value >>> 16);
        this.out.write(value >>> 8);
        this.out.write(value);
    }

    public void writeLong(final long value) throws IOException {
        this.out.write((int) (value >>> 56));
        this.out.write((int)(value >>> 48));
        this.out.write((int)(value >>> 40));
        this.out.write((int)(value >>> 32));
        this.out.write((int)(value >>> 24));
        this.out.write((int)(value >>> 16));
        this.out.write((int)(value >>> 8));
        this.out.write((int)value);
    }

}
