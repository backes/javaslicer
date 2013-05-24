/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult.traceSequences
 *    Class:     ConstantThreadTraces
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/traceSequences/ConstantThreadTraces.java
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

import java.io.DataInputStream;
import java.io.IOException;

import de.hammacher.util.MultiplexedFileReader;
import de.unisb.cs.st.javaslicer.common.TraceSequenceTypes;

public class ConstantThreadTraces {

    private final byte format;

    public ConstantThreadTraces(final byte format) {
        this.format = format;
    }

    public static ConstantThreadTraces readFrom(final DataInputStream in) throws IOException {
        final byte format = in.readByte();
        switch (format) {
        case 0:
            // just for debugging (NullThreadTracer)
            return new ConstantThreadTraces((byte) 0);
        case TraceSequenceTypes.FORMAT_GZIP:
            return new ConstantThreadTraces(TraceSequenceTypes.FORMAT_GZIP);
        case TraceSequenceTypes.FORMAT_SEQUITUR:
            try {
                return new SequiturThreadTraces(in);
            } catch (final ClassNotFoundException e) {
                // this exception can occur in the ObjectInputStream that the sequences are read from
                throw new IOException(e.toString());
            }
        case TraceSequenceTypes.FORMAT_UNCOMPRESSED:
            return new ConstantThreadTraces(TraceSequenceTypes.FORMAT_UNCOMPRESSED);
        default:
            throw new IOException("corrupted data (unknown trace sequence format)");
        }
    }

    public ConstantTraceSequence readSequence(final DataInputStream in, final MultiplexedFileReader file) throws IOException {
        final byte type = in.readByte();
        if ((type & TraceSequenceTypes.TYPE_INTEGER) != 0) {
            switch (this.format) {
            case TraceSequenceTypes.FORMAT_GZIP:
                return ConstantGZipIntegerTraceSequence.readFrom(in, file, type);
            case TraceSequenceTypes.FORMAT_UNCOMPRESSED:
                return ConstantUncompressedIntegerTraceSequence.readFrom(in, file);
            default:
                throw new AssertionError("should not get here");
            }
        } else if ((type & TraceSequenceTypes.TYPE_LONG) != 0) {
            switch (this.format) {
            case TraceSequenceTypes.FORMAT_GZIP:
                return ConstantGzipLongTraceSequence.readFrom(in, file, type);
            case TraceSequenceTypes.FORMAT_UNCOMPRESSED:
                return ConstantUncompressedLongTraceSequence.readFrom(in, file);
            default:
                throw new AssertionError("should not get here");
            }
        } else
            throw new IOException("corrupted data (unknown trace type)");
    }

}
