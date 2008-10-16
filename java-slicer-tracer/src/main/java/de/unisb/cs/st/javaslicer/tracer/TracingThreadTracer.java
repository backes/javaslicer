package de.unisb.cs.st.javaslicer.tracer;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map.Entry;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.ObjectTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequenceFactory;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.Type;
import de.unisb.cs.st.javaslicer.tracer.util.IntegerMap;

public class TracingThreadTracer implements ThreadTracer {

    private static final boolean DEBUG_TRACE_FILE = false;

    private final long threadId;
    private final String threadName;
    private final List<Type> threadSequenceTypes;

    private final TraceSequenceFactory.PerThread traceSequenceFactory;

    private volatile int lastInstructionIndex = -1;

    private final IntegerMap<TraceSequence> sequences = new IntegerMap<TraceSequence>();

    private final Tracer tracer;
    private volatile int paused = 0;

    protected static PrintWriter debugFile;
    static {
        if (DEBUG_TRACE_FILE) {
            try {
                debugFile = new PrintWriter(new BufferedWriter(new FileWriter(new File("debug.log"))));
                Runtime.getRuntime().addShutdownHook(new Thread("debug file closer") {
                    @Override
                    public void run() {
                        debugFile.close();
                    }
                });
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    public TracingThreadTracer(final Thread thread,
            final List<Type> threadSequenceTypes, final Tracer tracer) {
        this.threadId = thread.getId();
        this.threadName = thread.getName();
        this.threadSequenceTypes = threadSequenceTypes;
        this.tracer = tracer;
        this.traceSequenceFactory = tracer.seqFactory.forThreadTracer(this);
    }

    public synchronized void traceInt(final int value, final int traceSequenceIndex) {
        if (this.paused > 0)
            return;

        pauseTracing();

        TraceSequence seq = this.sequences.get(traceSequenceIndex);
        try {
            if (seq == null) {
                seq = this.traceSequenceFactory.createTraceSequence(
                        this.threadSequenceTypes.get(traceSequenceIndex), this.tracer);
                this.sequences.put(traceSequenceIndex, seq);
            }
            assert seq instanceof IntegerTraceSequence;

            ((IntegerTraceSequence) seq).trace(value);
        } catch (final IOException e) {
            this.tracer.error(e);
            System.err.println("Error writing the trace: " + e);
            System.exit(-1);
        }

        // only unpause if there was no error until here
        unpauseTracing();
    }

    public synchronized void traceObject(final Object obj, final int traceSequenceIndex) {
        if (this.paused > 0)
            return;

        pauseTracing();

        TraceSequence seq = this.sequences.get(traceSequenceIndex);
        try {
            if (seq == null) {
                seq = this.traceSequenceFactory.createTraceSequence(
                        this.threadSequenceTypes.get(traceSequenceIndex), this.tracer);
                this.sequences.put(traceSequenceIndex, seq);
            }
            assert seq instanceof ObjectTraceSequence;

            ((ObjectTraceSequence) seq).trace(obj);
        } catch (final IOException e) {
            this.tracer.error(e);
            System.err.println("Error writing the trace: " + e);
            System.exit(-1);
        }

        // only unpause if there was no error until here
        unpauseTracing();
    }

    public void traceLastInstructionIndex(final int traceSequenceIndex) {
        traceInt(this.lastInstructionIndex, traceSequenceIndex);
    }

    public void passInstruction(final int instructionIndex) {
        if (this.paused > 0)
            return;

        if (this.tracer.debug && this.threadId == 1) {
            pauseTracing();
            debugFile.println(instructionIndex);
            unpauseTracing();
        }

        this.lastInstructionIndex = instructionIndex;
    }

    public synchronized void finish() throws IOException {
//        final long startTime = System.nanoTime();
        pauseTracing();

        for (final TraceSequence seq: this.sequences.values())
            seq.finish();

        this.traceSequenceFactory.finish();

//        final long endTime = System.nanoTime();
//        System.out.format("Finishing %s took %.3f seconds%n", this.threadName, 1e-9*(endTime - startTime));
    }

    public void writeOut(final DataOutputStream out) throws IOException {
        finish();
//        final long startTime = System.nanoTime();
        out.writeLong(this.threadId);
        out.writeUTF(this.threadName);
        this.traceSequenceFactory.writeOut(out);
        out.writeInt(this.sequences.size());
        for (final Entry<Integer, TraceSequence> seq: this.sequences.entrySet()) {
            out.writeInt(seq.getKey());
            seq.getValue().writeOut(out);
        }
        out.writeInt(this.lastInstructionIndex);
//        final long endTime = System.nanoTime();
//        System.out.format("Writing %s took %.3f seconds%n", this.threadName, 1e-9*(endTime - startTime));
    }

    public synchronized void pauseTracing() {
        ++this.paused;
    }

    public synchronized void unpauseTracing() {
        --this.paused;
        assert this.paused >= 0: "unpaused more than paused";
    }

    public boolean isPaused() {
        return this.paused > 0;
    }

    public long getThreadId() {
        return this.threadId;
    }

}
