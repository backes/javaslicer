package de.unisb.cs.st.javaslicer.tracer.traceResult;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Iterator;

import de.hammacher.util.IntegerMap;
import de.hammacher.util.IntegerToLongMap;
import de.hammacher.util.MultiplexedFileReader;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences.ConstantThreadTraces;
import de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences.ConstantTraceSequence;

public class ThreadTraceResult implements Comparable<ThreadTraceResult> {

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

    private final ThreadId id;
    protected final IntegerMap<ConstantTraceSequence> sequences;
    protected final int lastInstructionIndex;
    protected final int lastStackDepth;

    private final TraceResult traceResult;

    private SoftReference<ForwardIterationInformation> forwardIterationInformation
        = new SoftReference<ForwardIterationInformation>(null);
    private final Object forwardIterationInfoLock = new Object();

    public ThreadTraceResult(final long threadId, final String threadName,
            final IntegerMap<ConstantTraceSequence> sequences, final int lastInstructionIndex,
            final TraceResult traceResult, final int lastStackDepth) {
        this.id = new ThreadId(threadId, threadName);
        this.sequences = sequences;
        this.lastInstructionIndex = lastInstructionIndex;
        this.traceResult = traceResult;
        this.lastStackDepth = lastStackDepth;
    }

    public ThreadId getId() {
        return this.id;
    }

    public long getJavaThreadId() {
        return getId().getThreadId();
    }

    public String getThreadName() {
        return getId().getThreadName();
    }

    public static ThreadTraceResult readFrom(final DataInputStream in, final TraceResult traceResult, final MultiplexedFileReader file) throws IOException {
        final long threadId = in.readLong();
        final String name = in.readUTF();
        final ConstantThreadTraces threadTraces = ConstantThreadTraces.readFrom(in);
        int numSequences = in.readInt();
        final IntegerMap<ConstantTraceSequence> sequences = new IntegerMap<ConstantTraceSequence>(numSequences*4/3+1);
        while (numSequences-- > 0) {
            final int nr = in.readInt();
            final ConstantTraceSequence seq = threadTraces.readSequence(in, file);
            if (sequences.put(nr, seq) != null)
                throw new IOException("corrupted data");
        }
        final int lastInstructionIndex = in.readInt();
        final int lastStackDepth = in.readInt();
        return new ThreadTraceResult(threadId, name, sequences, lastInstructionIndex, traceResult, lastStackDepth);
    }

    public Iterator<Instance> getBackwardIterator() {
        return new BackwardInstructionIterator(this);
    }

    public Iterator<Instance> getForwardIterator() {
        ForwardIterationInformation forwInfo;
        synchronized (this.forwardIterationInfoLock) {
            forwInfo = this.forwardIterationInformation.get();
            if (forwInfo == null) {
                forwInfo = getForwardInformation();
                this.forwardIterationInformation = new SoftReference<ForwardIterationInformation>(forwInfo);
            }
        }
        return new ForwardInstructionIterator(this, forwInfo);
    }

    private ForwardIterationInformation getForwardInformation() {
        final int numJumps = 0;
        long instrCount = 0;
        long[] jumpInstrNrs = new long[16];
        int[] jumpTargets = new int[16];
        byte[] stackDepthChange = new byte[16];
        final IntegerToLongMap occurrences = new IntegerToLongMap();

        final Iterator<Instance> backwardIt = getBackwardIterator();
        int lastIndex = 0;
        int curStackDepth = 0;
        while (backwardIt.hasNext()) {
            ++instrCount;
            final Instance instr = backwardIt.next();
            final int index = instr.getIndex();
            occurrences.increment(index);
            if (index != lastIndex-1 && instrCount > 0) {
                if (numJumps == jumpInstrNrs.length) {
                    jumpInstrNrs = Arrays.copyOf(jumpInstrNrs, 2*numJumps);
                    jumpTargets = Arrays.copyOf(jumpTargets, 2*numJumps);
                    stackDepthChange = Arrays.copyOf(stackDepthChange, 2*numJumps);
                }
                jumpInstrNrs[numJumps] = instrCount;
                jumpTargets[numJumps] = lastIndex;
                // TODO can the stack depth change by more than 256??
                final int newStackDepth = instr.getStackDepth();
                stackDepthChange[numJumps] = (byte) (newStackDepth - curStackDepth);
                curStackDepth = newStackDepth;
            }
            lastIndex = index;
        }

        if (numJumps != jumpInstrNrs.length) {
            jumpInstrNrs = Arrays.copyOf(jumpInstrNrs, numJumps);
            jumpTargets = Arrays.copyOf(jumpTargets, numJumps);
            stackDepthChange = Arrays.copyOf(stackDepthChange, numJumps);
        }

        return new ForwardIterationInformation(instrCount, lastIndex, jumpInstrNrs, jumpTargets, stackDepthChange, occurrences);
    }

    public Instruction findInstruction(final int instructionIndex) {
        return this.traceResult.getInstruction(instructionIndex);
    }

    @Override
    public int compareTo(final ThreadTraceResult o) {
        return this.getId().compareTo(o.getId());
    }

}
