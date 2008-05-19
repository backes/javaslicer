package de.unisb.cs.st.javaslicer.tracer.traceResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import de.unisb.cs.st.javaslicer.tracer.util.ReverseIntArrayIterator;

public class ConstantGZipIntegerTraceSequence extends ConstantIntegerTraceSequence {

    private final int[] data;

    public ConstantGZipIntegerTraceSequence(final int[] data) {
        this.data = data;
    }

    @Override
    public Iterator<Integer> backwardIterator() {
        return new ReverseIntArrayIterator(this.data);
    }

    public static ConstantGZipIntegerTraceSequence readFrom(final ObjectInputStream in) throws IOException {
        final byte[] gzipData = new byte[in.readInt()];
        in.read(gzipData);
        final GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(gzipData));
        final byte[] nextInt = new byte[4];
        int[] intData = new int[16];
        int dataCnt = 0;
        int read;
        int lastVal = 0;
        while ((read = gzipIn.read(nextInt)) == 4) {
            read = (nextInt[0] << 24) | (nextInt[1] << 16) | (nextInt[2] << 8) | nextInt[3];
            if (dataCnt != 0) {
                read += lastVal;
            }
            lastVal = read;
            if (intData.length <= dataCnt)
                intData = Arrays.copyOf(intData, 2*intData.length);
            intData[dataCnt++] = read;
        }
        if (read != 0)
            throw new RuntimeException("Corrupt data");
        return new ConstantGZipIntegerTraceSequence(Arrays.copyOf(intData, dataCnt));
    }

}
