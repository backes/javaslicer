package de.unisb.cs.st.javaslicer.tracer.traceResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import de.unisb.cs.st.javaslicer.tracer.util.ReverseLongArrayIterator;

public class ConstantGZipLongTraceSequence extends ConstantLongTraceSequence {

    private final long[] data;

    public ConstantGZipLongTraceSequence(final long[] data) {
        this.data = data;
    }

    @Override
    public Iterator<Long> backwardIterator() {
        return new ReverseLongArrayIterator(this.data);
    }

    public static ConstantGZipLongTraceSequence readFrom(final ObjectInputStream in) throws IOException {
        final byte[] gzipData = new byte[in.readInt()];
        in.read(gzipData);
        final GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(gzipData));
        final byte[] nextLong = new byte[8];
        long[] longData = new long[16];
        int dataCnt = 0;
        int read;
        long lastVal = 0;
        while ((read = gzipIn.read(nextLong)) == 8) {
            long l = (nextLong[0] << 56) | (nextLong[1] << 48) | (nextLong[2] << 40) |
                     (nextLong[3] << 32) | (nextLong[4] << 24) | (nextLong[5] << 16) |
                     (nextLong[6] << 8) | nextLong[7];
            if (dataCnt != 0) {
                l += lastVal;
            }
            lastVal = l;
            if (longData.length <= dataCnt)
                longData = Arrays.copyOf(longData, 2*longData.length);
            longData[dataCnt++] = l;
        }
        if (read != 0)
            throw new RuntimeException("Corrupt data");
        return new ConstantGZipLongTraceSequence(Arrays.copyOf(longData, dataCnt));
    }

}
