package de.unisb.cs.st.javaslicer.tracer;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.ObjectIdentifier;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequenceFactory;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.LongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.Type;
import de.unisb.cs.st.javaslicer.tracer.util.IntegerMap;

public class TracingThreadTracer implements ThreadTracer {

    private static class WriteOutJob {
        public final int[] seqNr;
        public final int[] intSeqVal;
        public final long[] longSeqVal;
        public final int count;

        public WriteOutJob(final int[] seqNr, final int[] intSeqVal, final long[] longSeqVal, final int count) {
            super();
            this.seqNr = seqNr;
            this.intSeqVal = intSeqVal;
            this.longSeqVal = longSeqVal;
            this.count = count;
        }

    }
    protected static class WriteOutThread extends Thread {

        public final LinkedBlockingQueue<WriteOutJob> jobs = new LinkedBlockingQueue<WriteOutJob>(MAX_CACHED_BLOCKS);

        private final TraceSequenceFactory.PerThread traceSequenceFactory;
        private final IntegerMap<TraceSequence> sequences = new IntegerMap<TraceSequence>();
        private final List<Type> threadSequenceTypes;

        private final Tracer tracer;

        public CountDownLatch ready = new CountDownLatch(1);

        public WriteOutThread(final String threadName, final TraceSequenceFactory.PerThread traceSequenceFactory,
                final List<Type> threadSequenceTypes, final Tracer tracer) {
            super("Writer for " + threadName);
            this.traceSequenceFactory = traceSequenceFactory;
            this.threadSequenceTypes = threadSequenceTypes;
            this.tracer = tracer;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    WriteOutJob job;
                    try {
                        job = this.jobs.take();
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                    final int count = job.count;
                    final int[] seqNr = job.seqNr;
                    if (job.intSeqVal != null) {
                        final int[] intSeqVal = job.intSeqVal;
                        for (int i = 0; i < count; ++i) {
                            TraceSequence seq = this.sequences.get(seqNr[i]);
                            if (seq == null) {
                                seq = this.traceSequenceFactory.createTraceSequence(
                                        this.threadSequenceTypes.get(seqNr[i]), this.tracer);
                                this.sequences.put(job.seqNr[i], seq);
                            }
                            assert seq instanceof IntegerTraceSequence;

                            ((IntegerTraceSequence) seq).trace(intSeqVal[i]);
                        }
                    } else if (job.longSeqVal != null) {
                        final long[] longSeqVal = job.longSeqVal;
                        for (int i = 0; i < count; ++i) {
                            TraceSequence seq = this.sequences.get(seqNr[i]);
                            if (seq == null) {
                                seq = this.traceSequenceFactory.createTraceSequence(
                                        this.threadSequenceTypes.get(seqNr[i]), this.tracer);
                                this.sequences.put(job.seqNr[i], seq);
                            }
                            assert seq instanceof LongTraceSequence;

                            ((LongTraceSequence) seq).trace(longSeqVal[i]);
                        }
                    } else {
                        finish();
                        break;
                    }
                }
            } catch (final IOException e) {
                System.err.println("Error writing the trace: " + e);
                this.tracer.error(e);
            }
        }

        private void finish() throws IOException {
//          final long startTime = System.nanoTime();

            for (final TraceSequence seq: this.sequences.values())
                seq.finish();

            this.traceSequenceFactory.finish();

            this.ready.countDown();

//            final long endTime = System.nanoTime();
//            System.out.format("Finishing %s took %.3f seconds%n", this.threadName, 1e-9*(endTime - startTime));

        }

        public void writeOut(final DataOutputStream out) throws IOException {
            this.traceSequenceFactory.writeOut(out);
            out.writeInt(this.sequences.size());
            for (final Entry<Integer, TraceSequence> seq: this.sequences.entrySet()) {
                out.writeInt(seq.getKey());
                seq.getValue().writeOut(out);
            }
        }

    }

    private static final boolean DEBUG_TRACE_FILE = false;

    private final long threadId;
    private final String threadName;

    private volatile int lastInstructionIndex = -1;

    private final Tracer tracer;
    private volatile int paused = 0;

    protected static int MAX_CACHED_BLOCKS = 10;

    private static int CACHE_SIZE = 1<<18;
    private int[] intSeqNr = new int[CACHE_SIZE];
    private int[] intSeqVal = new int[CACHE_SIZE];
    private int intSeqIndex = 0;
    private int[] longSeqNr = new int[CACHE_SIZE];
    private long[] longSeqVal = new long[CACHE_SIZE];
    private int longSeqIndex = 0;

    private final WriteOutThread writeOutThread;

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
        this.tracer = tracer;
        this.writeOutThread = new WriteOutThread(this.threadName, tracer.seqFactory.forThreadTracer(this),
                threadSequenceTypes, tracer);
    }

    public synchronized void traceInt(final int value, final int traceSequenceIndex) {
        if (this.paused > 0)
            return;

        this.intSeqNr[this.intSeqIndex] = traceSequenceIndex;
        this.intSeqVal[this.intSeqIndex] = value;
        if (++this.intSeqIndex == CACHE_SIZE) {
            pauseTracing();
            this.writeOutThread.jobs.add(new WriteOutJob(this.intSeqNr, this.intSeqVal, null, CACHE_SIZE));
            unpauseTracing();
            this.intSeqIndex = 0;
            this.intSeqNr = new int[CACHE_SIZE];
            this.intSeqVal = new int[CACHE_SIZE];
        }
    }

    public synchronized void traceObject(final Object obj, final int traceSequenceIndex) {
        if (this.paused > 0)
            return;

        final long objId = ObjectIdentifier.instance.getObjectId(obj);
        this.longSeqNr[this.longSeqIndex] = traceSequenceIndex;
        this.longSeqVal[this.longSeqIndex] = objId;
        if (++this.longSeqIndex == CACHE_SIZE) {
            pauseTracing();
            this.writeOutThread.jobs.add(new WriteOutJob(this.longSeqNr, null, this.longSeqVal, CACHE_SIZE));
            unpauseTracing();
            this.longSeqIndex = 0;
            this.longSeqNr = new int[CACHE_SIZE];
            this.longSeqVal = new long[CACHE_SIZE];
        }
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
        if (this.writeOutThread.ready.getCount() == 0)
            return;
        pauseTracing();

        this.writeOutThread.jobs.add(new WriteOutJob(null, null, null, 0));
        try {
            this.writeOutThread.ready.await();
        } catch (final InterruptedException e) {
            this.tracer.error(e);
        }
    }

    public void writeOut(final DataOutputStream out) throws IOException {
        finish();
//        final long startTime = System.nanoTime();
        out.writeLong(this.threadId);
        out.writeUTF(this.threadName);
        this.writeOutThread.writeOut(out);
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
