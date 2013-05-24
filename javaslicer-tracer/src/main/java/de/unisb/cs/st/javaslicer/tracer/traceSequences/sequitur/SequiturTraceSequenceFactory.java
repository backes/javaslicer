/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer.traceSequences.sequitur
 *    Class:     SequiturTraceSequenceFactory
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/traceSequences/sequitur/SequiturTraceSequenceFactory.java
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
package de.unisb.cs.st.javaslicer.tracer.traceSequences.sequitur;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import de.hammacher.util.streams.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.common.TraceSequenceTypes;
import de.unisb.cs.st.javaslicer.common.TraceSequenceTypes.Type;
import de.unisb.cs.st.javaslicer.tracer.ThreadTracer;
import de.unisb.cs.st.javaslicer.tracer.Tracer;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequenceFactory;
import de.unisb.cs.st.sequitur.output.ObjectWriter;
import de.unisb.cs.st.sequitur.output.OutputSequence;

public class SequiturTraceSequenceFactory implements TraceSequenceFactory {

    public static class PerThread implements TraceSequenceFactory.PerThread {

        private static ObjectWriter<Long> LONG_WRITER = new ObjectWriter<Long>() {
            @Override
			public void writeObject(final Long object, final ObjectOutputStream outputStream) throws IOException {
                OptimizedDataOutputStream.writeLong0(object.longValue(), outputStream);
            }
        };
        private static ObjectWriter<Integer> INT_WRITER = new ObjectWriter<Integer>() {
            @Override
			public void writeObject(final Integer object, final ObjectOutputStream outputStream) throws IOException {
                OptimizedDataOutputStream.writeInt0(object.intValue(), outputStream);
            }
        };

        private final OutputSequence<Integer> intSequence = new OutputSequence<Integer>(INT_WRITER);
        private final OutputSequence<Long> longSequence = new OutputSequence<Long>(LONG_WRITER);
        private final AtomicLong intSequenceLength = new AtomicLong(0);
        private final AtomicLong longSequenceLength = new AtomicLong(0);

        private List<SequiturIntegerTraceSequence> intSequences = new ArrayList<SequiturIntegerTraceSequence>();
        private List<SequiturLongTraceSequence> longSequences = new ArrayList<SequiturLongTraceSequence>();

        @Override
		public synchronized TraceSequence createTraceSequence(final Type type, final Tracer tracer) throws IOException {
            if (this.intSequences == null)
                throw new IOException("sequence factory already finished");
            SequiturIntegerTraceSequence intTraceSequence;
            SequiturLongTraceSequence longTraceSequence;
            switch (type) {
            case INTEGER:
                intTraceSequence = new SequiturIntegerTraceSequence(this.intSequence, this.intSequenceLength);
                this.intSequences.add(intTraceSequence);
                return intTraceSequence;
            case LONG:
                longTraceSequence = new SequiturLongTraceSequence(this.longSequence, this.longSequenceLength);
                this.longSequences.add(longTraceSequence);
                return longTraceSequence;
            default:
                assert false;
                return null;
            }
        }

        @Override
        public synchronized void finish() {
            if (this.intSequences == null)
                return;
            for (final SequiturIntegerTraceSequence intSeq: this.intSequences)
                intSeq.finish();
            for (final SequiturLongTraceSequence longSeq: this.longSequences)
                longSeq.finish();
            this.intSequences = null;
            this.longSequences = null;
            this.intSequence.finish();
            this.longSequence.finish();
        }

        @Override
        public void writeOut(final OutputStream out) throws IOException {
            finish();
            out.write(TraceSequenceTypes.FORMAT_SEQUITUR);
            final ObjectOutputStream objOut = new ObjectOutputStream(out);
            this.intSequence.writeOut(objOut, true);
            this.longSequence.writeOut(objOut, true);
            objOut.flush();
        }

    }

    @Override
    public PerThread forThreadTracer(final ThreadTracer tt) {
        return new PerThread();
    }

    @Override
    public boolean shouldAutoFlushFile() {
        return true;
    }

}
