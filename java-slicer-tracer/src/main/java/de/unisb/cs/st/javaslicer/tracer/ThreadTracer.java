package de.unisb.cs.st.javaslicer.tracer;

import java.io.BufferedWriter;
import java.io.DataOutput;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map.Entry;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.ObjectTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.Type;
import de.unisb.cs.st.javaslicer.tracer.util.IntegerMap;

public class ThreadTracer {

    private final long threadId;
    private final String threadName;
    private final List<Type> threadSequenceTypes;

    // this is the variable modified during runtime of the instrumented program
    private int lastInstructionIndex = -1;

    private final IntegerMap<TraceSequence> sequences = new IntegerMap<TraceSequence>();

    private final Tracer tracer;
    private int paused = 0;

    // TODO remove
    protected static PrintWriter debugFile;
    static {
        try {
            debugFile = new PrintWriter(new BufferedWriter(new FileWriter(new File("debug.log"))));
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                debugFile.close();
            }
        });
    }

    public ThreadTracer(final Thread thread,
            final List<Type> threadSequenceTypes, final Tracer tracer) {
        this.threadId = thread.getId();
        this.threadName = thread.getName();
        this.threadSequenceTypes = threadSequenceTypes;
        this.tracer = tracer;
    }

    public void traceInt(final int value, final int traceSequenceIndex) {
        if (this.paused > 0)
            return;
        ++this.paused;

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
            // and do _NOT_ unpause
            return;
        }

        --this.paused;
    }

    public void traceObject(final Object obj, final int traceSequenceIndex) {
        if (this.paused > 0)
            return;
        this.paused++;

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
            // and do _NOT_ unpause
            return;
        }

        --this.paused;
    }

    public void traceLastInstructionIndex(final int traceSequenceIndex) {
        traceInt(this.lastInstructionIndex, traceSequenceIndex);
    }

    public void passInstruction(final int instructionIndex) {
        if (this.paused > 0)
            return;

        if (Tracer.debug && this.threadId == 1) {
            ++this.paused;
            debugFile.println(instructionIndex);
            --this.paused;
        }

        this.lastInstructionIndex = instructionIndex;
    }

    public void finish() throws IOException {
        this.paused++;
        // TODO remove concurrentmodificationexception on sequences
        try {
            Thread.sleep(10);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        for (final TraceSequence seq: this.sequences.values())
            seq.finish();
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
        out.writeInt(this.lastInstructionIndex);
    }

    public void pauseTracing() {
        ++this.paused;
    }

    public void unpauseTracing() {
        --this.paused;
        assert this.paused >= 0: "unpaused more than paused";
    }

    public boolean isPaused() {
        return this.paused > 0;
    }

}
