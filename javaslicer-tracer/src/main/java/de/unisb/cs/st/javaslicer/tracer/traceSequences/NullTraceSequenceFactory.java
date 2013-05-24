/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer.traceSequences
 *    Class:     NullTraceSequenceFactory
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/traceSequences/NullTraceSequenceFactory.java
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

    @Override
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
