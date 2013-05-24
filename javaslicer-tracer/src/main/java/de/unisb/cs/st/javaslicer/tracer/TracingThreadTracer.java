/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer
 *    Class:     TracingThreadTracer
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/TracingThreadTracer.java
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
package de.unisb.cs.st.javaslicer.tracer;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import de.hammacher.util.maps.IntegerMap;
import de.unisb.cs.st.javaslicer.common.TraceSequenceTypes.Type;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.Identifiable;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.ObjectIdentifier;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequence.LongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequenceFactory;

public class TracingThreadTracer implements ThreadTracer {

    public static class Finisher implements Callable<Boolean> {

        private final TraceSequence seq;

        public Finisher(final TraceSequence seq) {
            this.seq = seq;
        }

        @Override
        public Boolean call() throws Exception {
            this.seq.finish();
            return Boolean.TRUE;
        }

    }
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
    private static class WriteOutThread extends UntracedThread {

        private static final ThreadPoolExecutor finishers;
        static {
            final int numThreads = Runtime.getRuntime().availableProcessors()+1;
            finishers = new ThreadPoolExecutor(numThreads, numThreads,
                    30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                    new ThreadFactory() {
                        private final AtomicInteger nextId = new AtomicInteger(0);

                        @Override
						public Thread newThread(final Runnable r) {
                            final Thread t = new UntracedThread(r, "sequence finisher " + this.nextId.getAndIncrement());
                            if (!t.isDaemon())
                                t.setDaemon(true);
                            if (t.getPriority() != Thread.NORM_PRIORITY)
                                t.setPriority(Thread.NORM_PRIORITY);
                            return t;
                        }
                    });
            finishers.allowCoreThreadTimeOut(true);
        }


        public final BlockingQueue<WriteOutJob> jobs = new ArrayBlockingQueue<WriteOutJob>(MAX_CACHED_BLOCKS);

        private final TraceSequenceFactory.PerThread traceSequenceFactory;
        private final IntegerMap<TraceSequence> sequences = new IntegerMap<TraceSequence>();
        private final List<Type> threadSequenceTypes;

        private final Tracer tracer;

        public CountDownLatch ready = new CountDownLatch(1);

        private final int minPrio;
        private final int maxPrio;

        public WriteOutThread(final String threadName, final TraceSequenceFactory.PerThread traceSequenceFactory,
                final List<Type> threadSequenceTypes, final Tracer tracer, final int minPrio, final int maxPrio) {
            super("Trace Writer for " + threadName);
            setDaemon(true);
            setPriority(minPrio);
            this.minPrio = minPrio;
            this.maxPrio = maxPrio;
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
                        this.tracer.error(e);
                        return;
                    }
                    adjustPriority();
                    final int count = job.count;
                    final int[] seqNr = job.seqNr;
                    if (job.intSeqVal != null) {
                        final int[] intSeqVal = job.intSeqVal;
                        for (int i = 0; i < count; ++i) {
                            TraceSequence seq = this.sequences.get(seqNr[i]);
                            if (seq == null) {
                                seq = this.traceSequenceFactory.createTraceSequence(
                                        this.threadSequenceTypes.get(seqNr[i]), this.tracer);
                                this.sequences.put(seqNr[i], seq);
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
                                this.sequences.put(seqNr[i], seq);
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

        public void addJob(final WriteOutJob job) {
            try {
                this.jobs.put(job);
                adjustPriority();
            } catch (final InterruptedException e) {
                System.err.println(e);
                this.tracer.error(e);
            }
        }

        private void adjustPriority() {
            this.setPriority(this.minPrio + (this.maxPrio - this.minPrio) * this.jobs.size() / MAX_CACHED_BLOCKS);
        }

        private void finish() throws IOException {
            final List<Future<Boolean>> finishing = new ArrayList<Future<Boolean>>();
            for (final TraceSequence seq: this.sequences.values()) {
                if (seq.useMultiThreading()) {
                    final Finisher task = new Finisher(seq);
                    finishing.add(finishers.submit(task));
                } else {
                    seq.finish();
                }
            }

            for (final Future<Boolean> future: finishing) {
                try {
                    future.get();
                } catch (final InterruptedException e) {
                    this.tracer.error(e);
                } catch (final ExecutionException e) {
                    if (e.getCause() instanceof IOException)
                        throw (IOException)e.getCause();
                    this.tracer.error(e);
                }
            }

            this.traceSequenceFactory.finish();

            this.ready.countDown();
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

    public static final boolean DEBUG_TRACE_FILE = false;

    private final long threadId;
    private final String threadName;

    private volatile int lastInstructionIndex = -1;
    private int[] objectAllocationTraceSequence = new int[4];
    private int uninitializedObjects = 0;

    private final Tracer tracer;
    private volatile int paused = 0;

    protected static final int MAX_CACHED_BLOCKS = 5;

    private static int CACHE_SIZE = 1<<18;
    private int[] intSeqNr = new int[CACHE_SIZE];
    private int[] intSeqVal = new int[CACHE_SIZE];
    private int intSeqIndex = 0;
    private int[] longSeqNr = new int[CACHE_SIZE];
    private long[] longSeqVal = new long[CACHE_SIZE];
    private int longSeqIndex = 0;

    private final WriteOutThread writeOutThread;

    // an array holding the index of one instruction of each method that we are in
    private int stackSize = 0;
    private int[] methodStack = new int[16];

	private long numCrossedLabels = 0;

    protected static final PrintWriter debugFile;
    static {
        PrintWriter tmpDebugFile = null;
        if (DEBUG_TRACE_FILE) {
            try {
                tmpDebugFile = new PrintWriter(new BufferedWriter(new FileWriter(new File("debug.log"))));
                final PrintWriter tmpDebugFile2 = tmpDebugFile;
                Runtime.getRuntime().addShutdownHook(new UntracedThread("debug file closer") {
                    @Override
                    public void run() {
                        tmpDebugFile2.close();
                    }
                });
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        debugFile = tmpDebugFile;
    }

    public TracingThreadTracer(final Thread thread,
            final List<Type> threadSequenceTypes, final Tracer tracer) {
        this.threadId = thread.getId();
        this.threadName = thread.getName();
        this.tracer = tracer;
        this.writeOutThread = new WriteOutThread(this.threadName, tracer.seqFactory.forThreadTracer(this),
                threadSequenceTypes, tracer, Thread.MIN_PRIORITY, Thread.MAX_PRIORITY);
        this.writeOutThread.start();
    }

    @Override
	public synchronized void traceInt(final int value, final int traceSequenceIndex) {
        if (this.paused > 0)
            return;

        this.intSeqNr[this.intSeqIndex] = traceSequenceIndex;
        this.intSeqVal[this.intSeqIndex] = value;
        if (++this.intSeqIndex == CACHE_SIZE) {
            pauseTracing();
            this.writeOutThread.addJob(new WriteOutJob(this.intSeqNr, this.intSeqVal, null, CACHE_SIZE));
            resumeTracing();
            this.intSeqIndex = 0;
            this.intSeqNr = new int[CACHE_SIZE];
            this.intSeqVal = new int[CACHE_SIZE];
        }
    }

    @Override
	public synchronized void traceObject(final Object obj, final int traceSequenceIndex) {
        if (this.paused > 0)
            return;

        final long objId;
        if (obj instanceof Identifiable) {
            objId = ((Identifiable)obj).__tracing_get_object_id();
        } else if (obj == null) {
            objId = 0;
        } else {
            pauseTracing();
            objId = ObjectIdentifier.instance.getObjectId(obj);
            resumeTracing();
        }
        assert obj == null || objId != 0;
        traceLong(traceSequenceIndex, objId);
    }

    /**
     * Unsynchronized, no check for pause. For internal use only!!
     */
    private void traceLong(final int traceSequenceIndex, final long value) {
        this.longSeqNr[this.longSeqIndex] = traceSequenceIndex;
        this.longSeqVal[this.longSeqIndex] = value;
        if (++this.longSeqIndex == CACHE_SIZE) {
            pauseTracing();
            this.writeOutThread.addJob(new WriteOutJob(this.longSeqNr, null, this.longSeqVal, CACHE_SIZE));
            resumeTracing();
            this.longSeqIndex = 0;
            this.longSeqNr = new int[CACHE_SIZE];
            this.longSeqVal = new long[CACHE_SIZE];
        }
    }

    @Override
	public synchronized void traceLastInstructionIndex(final int traceSequenceIndex) {
        if (this.paused > 0)
            return;

    	this.numCrossedLabels++;
        traceInt(this.lastInstructionIndex, traceSequenceIndex);
    }

    @Override
	public void passInstruction(final int instructionIndex) {
        if (this.paused > 0)
            return;

        if (DEBUG_TRACE_FILE) {
            if (this.threadId == 1) {
                pauseTracing();
                debugFile.println(instructionIndex);
                resumeTracing();
            }
        }

        this.lastInstructionIndex = instructionIndex;
    }

    @Override
	public synchronized void finish() {
        pauseTracing();

        // this ensures that the finishing tasks are only done once!
        if (this.writeOutThread.ready.getCount() == 0)
            return;

        while (this.uninitializedObjects > 0) {
            assert this.objectAllocationTraceSequence[this.uninitializedObjects-1] != 0;
            traceLong(this.objectAllocationTraceSequence[--this.uninitializedObjects], 0);
        }

        if (this.intSeqIndex != 0)
            this.writeOutThread.addJob(new WriteOutJob(this.intSeqNr, this.intSeqVal, null, this.intSeqIndex));
        if (this.longSeqIndex != 0)
            this.writeOutThread.addJob(new WriteOutJob(this.longSeqNr, null, this.longSeqVal, this.longSeqIndex));

        this.writeOutThread.addJob(new WriteOutJob(null, null, null, 0));
        try {
            this.writeOutThread.ready.await();
        } catch (final InterruptedException e) {
            this.tracer.error(e);
        }
    }

    public synchronized void writeOut(final DataOutputStream out) throws IOException {
        finish();
        out.writeLong(this.threadId);
        out.writeUTF(this.threadName);
        this.writeOutThread.writeOut(out);
        out.writeInt(this.lastInstructionIndex);
        out.writeLong(this.numCrossedLabels);
        out.writeInt(this.stackSize);
        for (int i = 0; i < this.stackSize; ++i)
            out.writeInt(this.methodStack[i]);
    }

    @Override
	public synchronized void pauseTracing() {
        ++this.paused;
    }

    @Override
	public synchronized void resumeTracing() {
        --this.paused;
        assert this.paused >= 0: "resumed more than paused";
    }

    @Override
	public boolean isPaused() {
        return this.paused > 0;
    }

    public long getThreadId() {
        return this.threadId;
    }

    @Override
    public synchronized void enterMethod(final int instructionIndex) {
        if (this.paused > 0)
            return;

        if (this.stackSize == this.methodStack.length) {
        	int[] newMethodStack = new int[2*this.stackSize];
        	System.arraycopy(this.methodStack, 0, newMethodStack, 0, this.stackSize);
            this.methodStack = newMethodStack;
        }
        this.methodStack[this.stackSize] = instructionIndex;
        ++this.stackSize;

        passInstruction(instructionIndex);
    }

    @Override
    public synchronized void leaveMethod(final int instructionIndex) {
        if (this.paused > 0)
            return;
        --this.stackSize;

        passInstruction(instructionIndex);
    }

    @Override
    public synchronized void objectAllocated(final int instructionIndex, final int traceSequenceNr) {
        if (this.paused > 0)
            return;

        if (this.uninitializedObjects == this.objectAllocationTraceSequence.length) {
        	int[] newArr = new int[2*this.uninitializedObjects];
        	System.arraycopy(this.objectAllocationTraceSequence, 0, newArr, 0, this.uninitializedObjects);
            this.objectAllocationTraceSequence = newArr;
        }
        this.objectAllocationTraceSequence[this.uninitializedObjects++] = traceSequenceNr;
        assert traceSequenceNr != 0;

        passInstruction(instructionIndex);
    }

    @Override
    public synchronized void objectInitialized(final Object obj) {
        if (this.paused == 0) {
            assert this.uninitializedObjects > 0 && this.objectAllocationTraceSequence[this.uninitializedObjects-1] != 0;
            traceObject(obj, this.objectAllocationTraceSequence[--this.uninitializedObjects]);
        }
    }

}
