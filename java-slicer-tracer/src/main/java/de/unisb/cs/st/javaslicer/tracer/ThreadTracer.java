package de.unisb.cs.st.javaslicer.tracer;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.ObjectTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.Type;
import de.unisb.cs.st.javaslicer.tracer.util.IntegerMap;
import de.unisb.cs.st.javaslicer.tracer.util.IntegerToIntegerMap;
import de.unisb.cs.st.javaslicer.tracer.util.IntegerToLongMap;

public class ThreadTracer {

    private final long threadId;
    private final String threadName;
    private final List<Type> threadSequenceTypes;

    // this is the variable modified during runtime of the instrumented program
    private int lastInstructionIndex = -1;

    private boolean trace = true;

    private final IntegerMap<TraceSequence> sequences = new IntegerMap<TraceSequence>();
    private final IntegerToLongMap instructionOccurences = new IntegerToLongMap(128,
            IntegerToIntegerMap.DEFAULT_LOAD_FACTOR, IntegerToIntegerMap.DEFAULT_SWITCH_TO_MAP_RATIO,
            IntegerToIntegerMap.DEFAULT_SWITCH_TO_LIST_RATIO, 0);

    private final Tracer tracer;

    public ThreadTracer(final Thread thread,
            final List<Type> threadSequenceTypes, final Tracer tracer) {
        this.threadId = thread.getId();
        this.threadName = thread.getName();
        this.threadSequenceTypes = threadSequenceTypes;
        this.tracer = tracer;
    }

    public void traceInt(final int value, final int traceSequenceIndex) {
        if (!this.trace)
            return;
        this.trace = false;

        TraceSequence seq = this.sequences.get(traceSequenceIndex);
        try {
            if (seq == null) {
                seq = Tracer.seqFactory.createTraceSequence(traceSequenceIndex,
                        this.threadSequenceTypes.get(traceSequenceIndex), this.tracer);
                this.sequences.put(traceSequenceIndex, seq);
            }
            assert seq instanceof IntegerTraceSequence;

            ((IntegerTraceSequence) seq).trace(value);
        } catch (final IOException e) {
            System.err.println("Error writing the trace: " + e.getMessage());
            // and do _NOT_ set trace to true
            return;
        }

        this.trace = true;
    }

    public void traceObject(final Object obj, final int traceSequenceIndex) {
        if (!this.trace)
            return;
        this.trace = false;

        TraceSequence seq = this.sequences.get(traceSequenceIndex);
        try {
            if (seq == null) {
                seq = Tracer.seqFactory.createTraceSequence(traceSequenceIndex,
                        this.threadSequenceTypes.get(traceSequenceIndex), this.tracer);
                this.sequences.put(traceSequenceIndex, seq);
            }
            assert seq instanceof ObjectTraceSequence;

            ((ObjectTraceSequence) seq).trace(obj);
        } catch (final IOException e) {
            System.err.println("Error writing the trace: " + e.getMessage());
            // and do _NOT_ set trace to true
            return;
        }

        this.trace = true;
    }

    public void traceLastInstructionIndex(final int traceSequenceIndex) {
        traceInt(this.lastInstructionIndex, traceSequenceIndex);
    }

    public void passInstruction(final int instructionIndex) {
        if (!this.trace)
            return;
        this.trace = false;
        this.lastInstructionIndex = instructionIndex;
        this.instructionOccurences.increment(instructionIndex);
        this.trace = true;
    }

    public void finish() throws IOException {
        this.trace = false;
        for (final TraceSequence seq: this.sequences.values())
            seq.finish();
    }

    public boolean setTracingEnabled(boolean newState) {
        if (this.trace == newState)
            return this.trace;
        this.trace = newState;
        return !newState;
    }

    public void writeOut(final DataOutput out) throws IOException {
        finish();
        out.writeLong(this.threadId);
        out.writeUTF(this.threadName);
        out.writeInt(this.sequences.size());
        for (final Entry<Integer, TraceSequence> seq: this.sequences.entrySet()) {
            out.writeInt(seq.getKey());
            seq.getValue().writeOut(out);
        }
        out.writeInt(this.instructionOccurences.size());
        for (final Entry<Integer, Long> seq: this.instructionOccurences.entrySet()) {
            out.writeInt(seq.getKey());
            out.writeLong(seq.getValue());
        }
        out.writeInt(this.lastInstructionIndex);
    }

}
