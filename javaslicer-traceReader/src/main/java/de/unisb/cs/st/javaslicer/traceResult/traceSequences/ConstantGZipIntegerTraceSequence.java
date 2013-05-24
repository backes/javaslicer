/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult.traceSequences
 *    Class:     ConstantGZipIntegerTraceSequence
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/traceSequences/ConstantGZipIntegerTraceSequence.java
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
package de.unisb.cs.st.javaslicer.traceResult.traceSequences;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.zip.GZIPInputStream;

import de.hammacher.util.MultiplexedFileReader;
import de.hammacher.util.MultiplexedFileReader.MultiplexInputStream;
import de.hammacher.util.iterators.EmptyIterator;
import de.hammacher.util.streams.OptimizedDataInputStream;
import de.unisb.cs.st.javaslicer.traceResult.traceSequences.ConstantTraceSequence.ConstantIntegerTraceSequence;

public class ConstantGZipIntegerTraceSequence implements ConstantIntegerTraceSequence {

    protected final MultiplexedFileReader file;
    protected final boolean gzipped;
    protected final int streamIndex;

    public ConstantGZipIntegerTraceSequence(final MultiplexedFileReader file, final boolean gzipped, final int streamIndex) {
        this.file = file;
        this.gzipped = gzipped;
        this.streamIndex = streamIndex;
    }

    @Override
	public Iterator<Integer> backwardIterator() {
        try {
            return this.gzipped ? new GZippedBackwardIterator(this.file, this.streamIndex)
                : new NoGzipBackwardIterator(this.file, this.streamIndex);
        } catch (final IOException e) {
            return EmptyIterator.getInstance();
        }
    }

    @Override
	public ListIterator<Integer> iterator() {
        throw new UnsupportedOperationException();
    }

    public static ConstantGZipIntegerTraceSequence readFrom(final DataInput in, final MultiplexedFileReader file,
            final byte format)
            throws IOException {
        final boolean gzipped = (format & 1) == 1;
        final int streamIndex = in.readInt();
        if (!file.getStreamIds().contains(streamIndex))
            throw new IOException("corrupted data");
        return new ConstantGZipIntegerTraceSequence(file, gzipped, streamIndex);
    }

    private static class GZippedBackwardIterator implements Iterator<Integer> {

        private final MultiplexInputStream multiplexedStream;
        private final OptimizedDataInputStream dataIn;
        private boolean error;
        private final PushbackInputStream pushBackInput;

        public GZippedBackwardIterator(final MultiplexedFileReader file, final int streamIndex) throws IOException {
            this.multiplexedStream = file.getInputStream(streamIndex);
            final InputStream gzipStream = new BufferedInputStream(new GZIPInputStream(this.multiplexedStream, 512), 512);
            this.pushBackInput = new PushbackInputStream(gzipStream, 1);
            this.dataIn = new OptimizedDataInputStream(this.pushBackInput, true);
        }

        @Override
		public boolean hasNext() {
            if (this.error)
                return false;
            int read;
            try {
                if ((read = this.pushBackInput.read()) != -1) {
                    this.pushBackInput.unread(read);
                    return true;
                }
                return false;
            } catch (final IOException e) {
                this.error = true;
                return false;
            }
        }

        @Override
		public Integer next() {
            try {
                return this.dataIn.readInt();
            } catch (final IOException e) {
                this.error = true;
                return null;
            }
        }

        @Override
		public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    private static class NoGzipBackwardIterator implements Iterator<Integer> {

        private final MultiplexInputStream multiplexedStream;
        private final OptimizedDataInputStream dataIn;
        private boolean error;

        public NoGzipBackwardIterator(final MultiplexedFileReader file, final int streamIndex) throws IOException {
            this.multiplexedStream = file.getInputStream(streamIndex);
            this.dataIn = new OptimizedDataInputStream(this.multiplexedStream, true);
        }

        @Override
		public boolean hasNext() {
            if (this.error)
                return false;
            try {
                return !this.multiplexedStream.isEOF();
            } catch (final IOException e) {
                this.error = true;
                return false;
            }
        }

        @Override
		public Integer next() {
            try {
                return this.dataIn.readInt();
            } catch (final IOException e) {
                this.error = true;
                return null;
            }
        }

        @Override
		public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
