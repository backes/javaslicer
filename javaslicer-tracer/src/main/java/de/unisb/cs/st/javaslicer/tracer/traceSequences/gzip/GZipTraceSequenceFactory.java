/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer.traceSequences.gzip
 *    Class:     GZipTraceSequenceFactory
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/traceSequences/gzip/GZipTraceSequenceFactory.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.tracer.traceSequences.gzip;

import java.io.IOException;
import java.io.OutputStream;

import de.unisb.cs.st.javaslicer.common.TraceSequenceTypes;
import de.unisb.cs.st.javaslicer.common.TraceSequenceTypes.Type;
import de.unisb.cs.st.javaslicer.tracer.ThreadTracer;
import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequenceFactory;

public class GZipTraceSequenceFactory implements TraceSequenceFactory, TraceSequenceFactory.PerThread {

    public TraceSequence createTraceSequence(final Type type, final Tracer tracer) {
        switch (type) {
        case INTEGER:
            return new GZipIntegerTraceSequence(tracer);
        case LONG:
            return new GZipLongTraceSequence(tracer);
        default:
            assert false;
            return null;
        }
    }

    @Override
    public void finish() {
        // nop
    }

    @Override
    public PerThread forThreadTracer(final ThreadTracer tt) {
        return this;
    }

    @Override
    public void writeOut(final OutputStream out) throws IOException {
        out.write(TraceSequenceTypes.FORMAT_GZIP);
    }

    @Override
    public boolean shouldAutoFlushFile() {
        return false;
    }

}
