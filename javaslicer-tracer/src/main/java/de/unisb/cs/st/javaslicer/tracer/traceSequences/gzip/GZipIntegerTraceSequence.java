/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer.traceSequences.gzip
 *    Class:     GZipIntegerTraceSequence
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/traceSequences/gzip/GZipIntegerTraceSequence.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.javaslicer.tracer.traceSequences.gzip;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.GZIPOutputStream;

import de.hammacher.util.MultiplexedFileWriter.MultiplexOutputStream;
import de.hammacher.util.MultiplexedFileWriter.MultiplexOutputStream.Reader;
import de.hammacher.util.streams.MyByteArrayInputStream;
import de.hammacher.util.streams.MyDataInputStream;
import de.hammacher.util.streams.MyDataOutputStream;
import de.hammacher.util.streams.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.common.TraceSequenceTypes;
import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.IntegerTraceSequence;

public class GZipIntegerTraceSequence implements IntegerTraceSequence {

    private static class BackwardIntegerStreamReader implements Iterator<Integer> {

        private long offset;
        private final int[] buf;
        private int bufPos;
        private final Reader mplexReader;
        private final MyDataInputStream dataIn;

        public BackwardIntegerStreamReader(final MultiplexOutputStream mplexOut, final int bufSize) throws IOException {
            final long numInts = mplexOut.length()/4;
            long startInt = (numInts - 1) / bufSize * bufSize;
            this.offset = startInt * 4;
            this.mplexReader = mplexOut.getReader(this.offset);
            this.dataIn = new MyDataInputStream(this.mplexReader);
            this.buf = new int[bufSize];
            this.bufPos = (int) (numInts - startInt - 1);
            for (int i = 0; startInt < numInts; ++startInt) {
                this.buf[i++] = this.dataIn.readInt();
            }
        }

        @Override
        public boolean hasNext() {
            try {
                if (this.bufPos >= 0)
                    return true;
                if (this.offset == 0)
                    return false;
                this.offset -= this.buf.length*4;
                this.mplexReader.seek(this.offset);
                for (int i = 0; i < this.buf.length; ++i) {
                    this.buf[i] = this.dataIn.readInt();
                }
                this.bufPos = this.buf.length - 1;
                return true;
            } catch (final IOException e) {
                close();
                return false;
            }
        }

        @Override
        public Integer next() {
            if (!hasNext())
                throw new NoSuchElementException();
            return this.buf[this.bufPos--];
        }

        // to avoid boxing
        public int nextInt() {
            if (!hasNext())
                throw new NoSuchElementException();
            return this.buf[this.bufPos--];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void close() {
            this.bufPos = -1;
            this.offset = 0;
            this.mplexReader.close();
        }
    }

    private final static int SWITCH_TO_GZIP_WHEN_GREATER = 512;

    private static final int CACHE_IF_LEQ = 507; // must be <= SWITCH_TO_GZIP_WHEN_GREATER

    private final Tracer tracer;

    private ByteArrayOutputStream baOutputStream;
    private MyDataOutputStream dataOut;

    private MultiplexOutputStream mplexOut;

    private boolean gzipped;


    public GZipIntegerTraceSequence(final Tracer tracer) {
        this.tracer = tracer;
        this.baOutputStream = new ByteArrayOutputStream(16);
        this.dataOut = new MyDataOutputStream(this.baOutputStream);
    }

    @Override
	public void trace(final int value) throws IOException {
        assert this.dataOut != null : "Trace cannot be extended any more";

        this.dataOut.writeInt(value);

        if (this.baOutputStream != null && this.baOutputStream.size() > CACHE_IF_LEQ) {
            this.mplexOut = this.tracer.newOutputStream();
            this.dataOut = new MyDataOutputStream(this.mplexOut);
            this.baOutputStream.writeTo(this.mplexOut);
            this.baOutputStream = null;
        }
    }

    @Override
    public void writeOut(final DataOutputStream out) throws IOException {
        finish();

        out.writeByte(TraceSequenceTypes.TYPE_INTEGER | (this.gzipped ? 1 : 0));
        out.writeInt(this.mplexOut.getId());
    }

    @Override
	public void finish() throws IOException {
        if (this.dataOut == null)
            return;
        this.dataOut = null;

        final MultiplexOutputStream oldMplexOut = this.mplexOut;
        this.mplexOut = this.tracer.newOutputStream();

        // now we have to inverse the integer stream
        if (this.baOutputStream != null) {
            this.gzipped = false;
            int nextPos = this.baOutputStream.size() - 4;
            final MyByteArrayInputStream bb = new MyByteArrayInputStream(this.baOutputStream.toByteArray());
            final MyDataInputStream dataIn = new MyDataInputStream(bb);
            final OptimizedDataOutputStream optOut = new OptimizedDataOutputStream(this.mplexOut, true);
            while (nextPos >= 0) {
                bb.seek(nextPos);
                optOut.writeInt(dataIn.readInt());
                nextPos -= 4;
            }
            this.baOutputStream = null;
            optOut.close();
        } else {
            assert oldMplexOut != null;
            ByteArrayOutputStream invStreamFirstPart = null;
            OptimizedDataOutputStream optOut = null;
            final BackwardIntegerStreamReader backwardReader = new BackwardIntegerStreamReader(oldMplexOut, 8*1024);
            if (oldMplexOut.length() <= 4*SWITCH_TO_GZIP_WHEN_GREATER) {
                invStreamFirstPart = new ByteArrayOutputStream();
                optOut = new OptimizedDataOutputStream(invStreamFirstPart, true);
                while (backwardReader.hasNext())
                    optOut.writeInt(backwardReader.nextInt());
            }
            if (!backwardReader.hasNext() && invStreamFirstPart != null && invStreamFirstPart.size() <= SWITCH_TO_GZIP_WHEN_GREATER) {
                this.gzipped = false;
                invStreamFirstPart.writeTo(this.mplexOut);
            } else {
                this.gzipped = true;
                final OutputStream gzipOut = new BufferedOutputStream(new GZIPOutputStream(this.mplexOut, 512), 512);
                if (invStreamFirstPart != null)
                    invStreamFirstPart.writeTo(gzipOut);
                optOut = new OptimizedDataOutputStream(gzipOut, optOut == null ? 0 : optOut.getLastIntValue(), 0l);
                while (backwardReader.hasNext())
                    optOut.writeInt(backwardReader.nextInt());
                optOut.close();
            }
            backwardReader.close();
            oldMplexOut.remove();
        }
    }

    @Override
    public boolean useMultiThreading() {
        return this.dataOut != null;
    }

}
