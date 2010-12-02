/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer.traceSequences.sequitur
 *    Class:     SequiturLongTraceSequence
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/traceSequences/sequitur/SequiturLongTraceSequence.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.tracer.traceSequences.sequitur;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import de.hammacher.util.streams.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.LongTraceSequence;
import de.unisb.cs.st.sequitur.output.OutputSequence;

public class SequiturLongTraceSequence implements LongTraceSequence {

    private boolean ready = false;

    private long sequenceOffset;

    private long[] values = new long[10];
    private int count = 0;

    private long lastValue = 0;

    private final OutputSequence<Long> sequiturSeq;
    private final AtomicLong sequiturSeqLength;

    public SequiturLongTraceSequence(final OutputSequence<Long> outputSequence, final AtomicLong outputSeqLength) {
        this.sequiturSeq = outputSequence;
        this.sequiturSeqLength = outputSeqLength;
    }

    @Override
	public void trace(final long value) {
        assert !this.ready: "Trace cannot be extended any more";

        if (this.count == this.values.length)
            this.values = Arrays.copyOf(this.values, this.values.length*3/2);
        this.values[this.count++] = value - this.lastValue;
        this.lastValue = value;
    }

    @Override
    public void writeOut(final DataOutputStream out) throws IOException {
        finish();

        OptimizedDataOutputStream.writeLong0(2*this.sequenceOffset+1, out);
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
