/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer.traceSequences.sequitur
 *    Class:     SequiturIntegerTraceSequence
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/traceSequences/sequitur/SequiturIntegerTraceSequence.java
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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import de.hammacher.util.streams.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.IntegerTraceSequence;
import de.unisb.cs.st.sequitur.output.OutputSequence;

public class SequiturIntegerTraceSequence implements IntegerTraceSequence {

    private boolean ready = false;

    private long sequenceOffset;

    private int[] values = new int[10];
    private int count = 0;

    private int lastValue = 0;

    private final OutputSequence<Integer> sequiturSeq;
    private final AtomicLong sequiturSeqLength;

    public SequiturIntegerTraceSequence(final OutputSequence<Integer> outputSequence, final AtomicLong outputSeqLength) {
        this.sequiturSeq = outputSequence;
        this.sequiturSeqLength = outputSeqLength;
    }

    @Override
	public void trace(final int value) {
        assert !this.ready: "Trace cannot be extended any more";

        if (this.count == this.values.length)
            this.values = Arrays.copyOf(this.values, this.values.length*3/2);
        this.values[this.count++] = value - this.lastValue;
        this.lastValue = value;
    }

    @Override
    public void writeOut(final DataOutputStream out) throws IOException {
        finish();

        OptimizedDataOutputStream.writeLong0(2*this.sequenceOffset, out);
        OptimizedDataOutputStream.writeInt0(this.count, out);
    }

    @Override
    public void finish() {
        if (this.ready)
            return;
        this.ready = true;
        synchronized (this.sequiturSeq) {
            for (int i = 0; i < this.count; ++i)
                this.sequiturSeq.append(this.values[i]);
            this.values = null;
            if (this.count > 10) {
                this.sequiturSeq.append(this.lastValue);
                this.sequenceOffset = this.sequiturSeqLength.getAndAdd(this.count+1);
            } else {
                this.sequenceOffset = this.sequiturSeqLength.getAndAdd(this.count);
            }
        }
    }

    @Override
    public boolean useMultiThreading() {
        return false;
    }

}
