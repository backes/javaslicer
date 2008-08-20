package de.unisb.cs.st.javaslicer.tracer.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class OptimizedDataOutputStream extends FilterOutputStream {

    protected static final byte MAGIC_1BYTE  = (byte) 0x80; // -128
    protected static final byte MAGIC_2BYTES = (byte) 0x81; // -127
    protected static final byte MAGIC_3BYTES = (byte) 0x7f; //  127
    protected static final byte MAGIC_4BYTES = (byte) 0x82; // -126
    protected static final byte MAGIC_5BYTES = (byte) 0x7e; //  126
    protected static final byte MAGIC_6BYTES = (byte) 0x83; // -125
    protected static final byte MAGIC_7BYTES = (byte) 0x7d; //  125
    protected static final byte MAGIC_8BYTES = (byte) 0x84; // -124

    public OptimizedDataOutputStream(final OutputStream out) {
        super(out);
    }

    public void writeInt(final int value) throws IOException {
        final byte b0 = (byte)value;
        if (b0 == value) {
            switch (b0) {
            case MAGIC_1BYTE:
            case MAGIC_2BYTES:
            case MAGIC_3BYTES:
            case MAGIC_4BYTES:
                break;
            default:
                this.out.write(b0);
                return;
            }
        }
        final byte b3 = (byte)(value >>> 24);
        final byte b2 = (byte)(value >>> 16);
        final byte b1 = (byte)(value >>> 8);
        if (b3 != 0) {
            this.out.write(MAGIC_4BYTES);
            this.out.write(b3);
            this.out.write(b2);
            this.out.write(b1);
            this.out.write(b0);
        } else if (b2 != 0) {
            this.out.write(MAGIC_3BYTES);
            this.out.write(b2);
            this.out.write(b1);
            this.out.write(b0);
        } else if (b1 != 0) {
            this.out.write(MAGIC_2BYTES);
            this.out.write(b1);
            this.out.write(b0);
        } else {
            this.out.write(MAGIC_1BYTE);
            this.out.write(b0);
        }
    }

    public void writeLong(final long value) throws IOException {
        final byte b0 = (byte)value;
        if (b0 == value) {
            switch (b0) {
            case MAGIC_1BYTE:
            case MAGIC_2BYTES:
            case MAGIC_3BYTES:
            case MAGIC_4BYTES:
            case MAGIC_5BYTES:
            case MAGIC_6BYTES:
            case MAGIC_7BYTES:
            case MAGIC_8BYTES:
                break;
            default:
                this.out.write(b0);
                return;
            }
        }
        final byte b7 = (byte)(value >>> 56);
        final byte b6 = (byte)(value >>> 48);
        final byte b5 = (byte)(value >>> 40);
        final byte b4 = (byte)(value >>> 32);
        final byte b3 = (byte)(value >>> 24);
        final byte b2 = (byte)(value >>> 16);
        final byte b1 = (byte)(value >>> 8);
        if (b7 != 0) {
            this.out.write(MAGIC_8BYTES);
            this.out.write(b7);
            this.out.write(b6);
            this.out.write(b5);
            this.out.write(b4);
            this.out.write(b3);
            this.out.write(b2);
            this.out.write(b1);
            this.out.write(b0);
        } else if (b6 != 0) {
            this.out.write(MAGIC_7BYTES);
            this.out.write(b6);
            this.out.write(b5);
            this.out.write(b4);
            this.out.write(b3);
            this.out.write(b2);
            this.out.write(b1);
            this.out.write(b0);
        } else if (b5 != 0) {
            this.out.write(MAGIC_6BYTES);
            this.out.write(b5);
            this.out.write(b4);
            this.out.write(b3);
            this.out.write(b2);
            this.out.write(b1);
            this.out.write(b0);
        } else if (b4 != 0) {
            this.out.write(MAGIC_5BYTES);
            this.out.write(b4);
            this.out.write(b3);
            this.out.write(b2);
            this.out.write(b1);
            this.out.write(b0);
        } else if (b3 != 0) {
            this.out.write(MAGIC_4BYTES);
            this.out.write(b3);
            this.out.write(b2);
            this.out.write(b1);
            this.out.write(b0);
        } else if (b2 != 0) {
            this.out.write(MAGIC_3BYTES);
            this.out.write(b2);
            this.out.write(b1);
            this.out.write(b0);
        } else if (b1 != 0) {
            this.out.write(MAGIC_2BYTES);
            this.out.write(b1);
            this.out.write(b0);
        } else {
            this.out.write(MAGIC_1BYTE);
            this.out.write(b0);
        }
    }

}
