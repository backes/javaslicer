package de.unisb.cs.st.javaslicer.tracer.traceResult;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileReader;

public class TraceResult {

    public static class ThreadId {

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

    }

    private final List<ReadClass> readClasses;
    private final List<ThreadTraceResult> threadTraces;

    public TraceResult(final List<ReadClass> readClasses, final List<ThreadTraceResult> threadTraces) {
        this.readClasses = readClasses;
        this.threadTraces = threadTraces;
    }

    public static TraceResult readFrom(final File filename) throws IOException {
        final MultiplexedFileReader file = new MultiplexedFileReader(filename);
        if (file.getNoStreams() < 1)
            throw new IOException("corrupted data");
        final DataInputStream mainInStream = new DataInputStream(file.getInputStream(0));
        int numClasses = mainInStream.readInt();
        final List<ReadClass> readClasses = new ArrayList<ReadClass>(numClasses);
        while (numClasses-- > 0)
            readClasses.add(ReadClass.readFrom(mainInStream));

        int numThreadTracers = mainInStream.readInt();
        final List<ThreadTraceResult> threadTraces = new ArrayList<ThreadTraceResult>(numThreadTracers);
        final TraceResult traceResult = new TraceResult(readClasses, threadTraces);
        while (numThreadTracers-- > 0)
            threadTraces.add(ThreadTraceResult.readFrom(mainInStream, traceResult, file));

        if (mainInStream.read() != -1)
            throw new IOException("Corrupt data");

        return traceResult;
    }

    public Iterator<Instruction> getBackwardIterator(final long threadId) {
        for (final ThreadTraceResult res: this.threadTraces)
            if (res.getThreadId() == threadId)
                return res.getBackwardIterator();
        return null;
    }

    public List<ThreadId> getThreads() {
        final List<ThreadId> list = new ArrayList<ThreadId>(this.threadTraces.size());
        for (final ThreadTraceResult t: this.threadTraces)
            list.add(new ThreadId(t.getThreadId(), t.getThreadName()));
        return list;
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
                if (tracing == null)
                    tracing = t;
            } else if (t.getThreadId() == threadToTrace)
                tracing = t;
            System.out.format("%15d: %s%n", t.getThreadId(), t.getThreadName());
        }
        System.out.println();

        System.out.println(threadToTrace == null ? "Selecting first thread:" : "You selected:");
        System.out.format("%15d: %s%n", tracing.getThreadId(), tracing.getThreadName());

        System.out.println();
        System.out.println("The backward trace:");
        final Iterator<Instruction> it = tr.getBackwardIterator(tracing.getThreadId());
        while (it.hasNext()) {
            final Instruction instr = it.next();
            final ReadMethod method = instr.getMethod();
            final ReadClass class0 = method.getReadClass();
            System.out.format("%-50s: %s%n", class0.getClassName()+"."
                    +method.getName()+":"+instr.getLineNumber(), instr);
        }

    }

}
