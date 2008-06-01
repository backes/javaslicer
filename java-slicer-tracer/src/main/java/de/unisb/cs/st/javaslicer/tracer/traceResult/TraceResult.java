package de.unisb.cs.st.javaslicer.tracer.traceResult;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;

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

    public static TraceResult readFrom(final ObjectInputStream in) throws IOException {
        int numClasses = in.readInt();
        final List<ReadClass> readClasses = new ArrayList<ReadClass>(numClasses);
        while (numClasses-- > 0)
            readClasses.add(ReadClass.readFrom(in));

        int numThreadTracers = in.readInt();
        final List<ThreadTraceResult> threadTraces = new ArrayList<ThreadTraceResult>(numThreadTracers);
        final TraceResult traceResult = new TraceResult(readClasses, threadTraces);
        while (numThreadTracers-- > 0)
            threadTraces.add(ThreadTraceResult.readFrom(in, traceResult));

        if (in.read() != -1)
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

}
