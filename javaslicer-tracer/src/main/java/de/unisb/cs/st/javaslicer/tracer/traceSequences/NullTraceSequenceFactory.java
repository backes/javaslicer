/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer.traceSequences
 *    Class:     NullTraceSequenceFactory
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/traceSequences/NullTraceSequenceFactory.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.tracer.traceSequences;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.unisb.cs.st.javaslicer.common.TraceSequenceTypes.Type;
import de.unisb.cs.st.javaslicer.tracer.ThreadTracer;
import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.LongTraceSequence;

public class NullTraceSequenceFactory implements TraceSequenceFactory, TraceSequenceFactory.PerThread {

    public static class NullTraceSequence implements IntegerTraceSequence, LongTraceSequence {

        @Override
        public void finish() {
            // null
        }

        @Override
        public void writeOut(final DataOutputStream out) {
            // null
        }

        @Override
        public void trace(final int value) {
            // null
        }

        @Override
        public void trace(final long value) {
            // null
        }

        @Override
        public boolean useMultiThreading() {
            return false;
        }

    }

    public TraceSequence createTraceSequence(final Type type, final Tracer tracer) {
        return new NullTraceSequence();
    }

    @Override
    public void finish() {
        // null
    }

    @Override
    public PerThread forThreadTracer(final ThreadTracer tt) {
        return this;
    }

    @Override
    public void writeOut(final OutputStream out) throws IOException {
        out.write(0);
    }

    @Override
    public boolean shouldAutoFlushFile() {
        return false;
    }

}
