/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer.traceSequences.uncompressed
 *    Class:     UncompressedLongTraceSequence
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/traceSequences/uncompressed/UncompressedLongTraceSequence.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.tracer.traceSequences.uncompressed;

import java.io.DataOutputStream;
import java.io.IOException;

import de.hammacher.util.MultiplexedFileWriter.MultiplexOutputStream;
import de.hammacher.util.streams.MyDataOutputStream;
import de.unisb.cs.st.javaslicer.common.TraceSequenceTypes;
import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.LongTraceSequence;

public class UncompressedLongTraceSequence implements LongTraceSequence {

    private boolean ready = false;

    private final MyDataOutputStream dataOut;

    private final int streamIndex;

    public UncompressedLongTraceSequence(final Tracer tracer) {
        final MultiplexOutputStream out = tracer.newOutputStream();
        this.dataOut = new MyDataOutputStream(out);
        this.streamIndex = out.getId();
    }

    @Override
    public void trace(final long value) throws IOException {
        assert !this.ready: "Trace cannot be extended any more";

        this.dataOut.writeLong(value);
    }

    @Override
    public void writeOut(final DataOutputStream out) throws IOException {
        finish();

        out.writeByte(TraceSequenceTypes.TYPE_LONG);
        out.writeInt(this.streamIndex);
    }

    @Override
    public void finish() throws IOException {
        if (this.ready)
            return;
        this.ready = true;
        this.dataOut.close();
    }

    @Override
    public boolean useMultiThreading() {
        return false;
    }
}
