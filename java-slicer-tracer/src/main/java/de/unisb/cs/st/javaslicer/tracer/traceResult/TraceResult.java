package de.unisb.cs.st.javaslicer.tracer.traceResult;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.tracer.traceResult.ThreadTraceResult.BackwardInstructionIterator;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileReader;

public class TraceResult {

    public static class ThreadId implements Comparable<ThreadId> {

        private final long threadId;
        private final String threadName;

        public ThreadId(final long threadId, final String threadName) {
            this.threadId = threadId;
            this.threadName = threadName;
        }

        public long getThreadId() {
            return this.threadId;
        }

        public String getThreadName() {
            return this.threadName;
        }

        @Override
        public String toString() {
            return this.threadId + ": " + this.threadName;
        }

        @Override
        public int compareTo(final ThreadId other) {
            if (this.threadId == other.threadId) {
                final int nameCmp = this.threadName.compareTo(other.threadName);
                if (nameCmp == 0 && this != other)
                    return System.identityHashCode(this) - System.identityHashCode(other);
                return nameCmp;
            }
            return Long.signum(this.threadId - other.threadId);
        }

    }

    private final List<ReadClass> readClasses;
    private final List<ThreadTraceResult> threadTraces;
    public boolean debug = false;

    public TraceResult(final List<ReadClass> readClasses, final List<ThreadTraceResult> threadTraces) {
        this.readClasses = readClasses;
        this.threadTraces = threadTraces;
    }

    public static TraceResult readFrom(final File filename) throws IOException {
        final MultiplexedFileReader file = new MultiplexedFileReader(filename);
        if (file.getNoStreams() < 2)
            throw new IOException("corrupted data");
        final DataInputStream readClassesInputStream = new DataInputStream(file.getInputStream(0));
        final ArrayList<ReadClass> readClasses = new ArrayList<ReadClass>();
        while (readClassesInputStream.available() > 0)
            readClasses.add(ReadClass.readFrom(readClassesInputStream));
        readClasses.trimToSize();

        final DataInputStream threadTracersInputStream = new DataInputStream(file.getInputStream(1));
        final ArrayList<ThreadTraceResult> threadTraces = new ArrayList<ThreadTraceResult>();
        final TraceResult traceResult = new TraceResult(readClasses, threadTraces);
        while (threadTracersInputStream.available() > 0)
            threadTraces.add(ThreadTraceResult.readFrom(threadTracersInputStream, traceResult, file));
        threadTraces.trimToSize();

        return traceResult;
    }

    public Iterator<Instance> getBackwardIterator(final long threadId) {
        for (final ThreadTraceResult res: this.threadTraces)
            if (res.getThreadId() == threadId)
                return res.getBackwardIterator();
        return null;
    }

    /**
     * Returns a sorted, not modifiable List of all threads that are represented
     * by traces in this TraceResult.
     *
     * @return the sorted list of {@link ThreadId}s.
     */
    public List<ThreadId> getThreads() {
        final ThreadId[] list = new ThreadId[this.threadTraces.size()];
        for (int i = 0; i < this.threadTraces.size(); ++i) {
            final ThreadTraceResult tt = this.threadTraces.get(i);
            list[i] = new ThreadId(tt.getThreadId(), tt.getThreadName());
        }
        Arrays.sort(list);
        return Arrays.asList(list);
    }

    public List<ReadClass> getReadClasses() {
        return this.readClasses;
    }

    public static void main(final String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: java " + TraceResult.class.getName() + " <file> [<threadId>]");
            System.exit(-1);
        }
        final File traceFile = new File(args[0]);
        Long threadToTrace = null;
        if (args.length > 1) {
            try {
                threadToTrace = Long.valueOf(args[1]);
            } catch (final NumberFormatException e) {
                System.err.println("Second parameter indicates the thread id to trace. Must be an integer.");
            }
        }

        System.out.println("Opening and reading trace file...");
        TraceResult tr = null;
        try {
            tr = readFrom(traceFile);
        } catch (final IOException e) {
            System.err.println("Error opening trace file: " + e);
            System.exit(-1);
        }

        final List<ThreadId> threads = tr.getThreads();
        if (threads.size() == 0) {
            System.err.println("The trace file contains no tracing information.");
            System.exit(-1);
        }

        System.out.println("The trace file contains traces for these threads:");
        ThreadId tracing = null;
        for (final ThreadId t: threads) {
            if (threadToTrace == null) {
                if ("main".equals(t.getThreadName()) && (tracing == null || t.getThreadId() < tracing.getThreadId()))
                    tracing = t;
            } else if (t.getThreadId() == threadToTrace.longValue()) {
                tracing = t;
            }
            System.out.format("%15d: %s%n", t.getThreadId(), t.getThreadName());
        }
        System.out.println();

        if (tracing == null) {
            System.out.println(threadToTrace == null ? "Couldn't find a main thread."
                    : "The thread you selected was not found.");
            System.exit(-1);
        }

        System.out.println(threadToTrace == null ? "Selected:" : "You selected:");
        System.out.format("%15d: %s%n", tracing.getThreadId(), tracing.getThreadName());

        final String format = "%8d: %-100s -> %7d %s%n";

        System.out.println();
        System.out.println("The backward trace:");
        final Iterator<Instance> it = tr.getBackwardIterator(tracing.getThreadId());
        long nr = 0;
        while (it.hasNext()) {
            final Instance inst = it.next();
            final ReadMethod method = inst.getMethod();
            final ReadClass class0 = method.getReadClass();
            System.out.format(format, nr++, class0.getName()+"."
                    +method.getName()+":"+inst.getLineNumber(),
                    inst.getOccurenceNumber(), inst.toString());
        }

        final BackwardInstructionIterator it2 = (BackwardInstructionIterator) it;

        System.out.println();
        System.out.println("No instructions: " + it2.getNoInstructions()
                + " (+ " + it2.getNoAdditionalInstructions() + " additional = "
                + (it2.getNoInstructions() + it2.getNoAdditionalInstructions())
                + " total instructions)");

        System.out.println("Ready");
    }

}
