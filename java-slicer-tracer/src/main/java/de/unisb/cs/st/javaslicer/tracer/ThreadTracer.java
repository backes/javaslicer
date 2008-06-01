package de.unisb.cs.st.javaslicer.tracer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.ObjectTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.Type;

public class ThreadTracer {

    private final long threadId;
    private final String threadName;
    private final List<Type> threadSequenceTypes;

    // this is the variable modified during runtime of the instrumented program
    private int lastInstructionIndex = -1;

    private boolean trace = true;

    // TODO replace by an own implementation that switch from map to list at a given fill ratio
    private final Map<Integer, TraceSequence> sequences = new HashMap<Integer, TraceSequence>();

    public ThreadTracer(final long threadId, final String threadName,
            final List<Type> threadSequenceTypes) {
        this.threadId = threadId;
        this.threadName = threadName;
        this.threadSequenceTypes = threadSequenceTypes;
    }

    public void traceInt(final int value, final int traceSequenceIndex) {
        if (!this.trace)
            return;
        this.trace = false;
        try {
            TraceSequence seq = this.sequences.get(traceSequenceIndex);
            if (seq == null) {
                seq = Tracer.seqFactory.createTraceSequence(traceSequenceIndex,
                        this.threadSequenceTypes.get(traceSequenceIndex));
                this.sequences.put(traceSequenceIndex, seq);
            }
            assert seq instanceof IntegerTraceSequence;
            ((IntegerTraceSequence) seq).trace(value);
        } finally {
            this.trace = true;
        }
    }

    public void traceObject(final Object obj, final int traceSequenceIndex) {
        if (!this.trace)
            return;
        this.trace = false;
        try {
            TraceSequence seq = this.sequences.get(traceSequenceIndex);
            if (seq == null) {
                seq = Tracer.seqFactory.createTraceSequence(traceSequenceIndex,
                        this.threadSequenceTypes.get(traceSequenceIndex));
                this.sequences.put(traceSequenceIndex, seq);
            }
            assert seq instanceof ObjectTraceSequence;
            ((ObjectTraceSequence) seq).trace(obj);
        } finally {
            this.trace = true;
        }
    }

    public void setLastInstructionIndex(final int lastInstructionIndex) {
        if (!this.trace)
            return;
        this.lastInstructionIndex = lastInstructionIndex;
    }

    public void writeOut(final ObjectOutputStream out) throws IOException {
        out.writeLong(this.threadId);
        out.writeUTF(this.threadName);
        out.writeInt(this.sequences.size());
        for (final Entry<Integer, TraceSequence> seq: this.sequences.entrySet()) {
            out.writeInt(seq.getKey());
            seq.getValue().writeOut(out);
        }
        out.writeInt(this.lastInstructionIndex);
    }

    public int getLastInstructionIndex() {
        return this.lastInstructionIndex;
    }

    public boolean setTracingEnabled(boolean newState) {
        if (this.trace == newState)
            return this.trace;
        this.trace = newState;
        return !newState;
    }

}
