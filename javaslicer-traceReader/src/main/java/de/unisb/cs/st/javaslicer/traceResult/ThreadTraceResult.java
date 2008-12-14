package de.unisb.cs.st.javaslicer.traceResult;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;

import de.hammacher.util.IntegerMap;
import de.hammacher.util.MultiplexedFileReader;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.traceResult.traceSequences.ConstantThreadTraces;
import de.unisb.cs.st.javaslicer.traceResult.traceSequences.ConstantTraceSequence;

public class ThreadTraceResult implements Comparable<ThreadTraceResult> {

    public static class ThreadId implements Comparable<ThreadId> {

        private final long threadId;
        private final String threadName;

        public ThreadId(final long threadId, final String threadName) {
            this.threadId = threadId;
            this.threadName = threadName;
        }

        public long getJavaThreadId() {
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ (int) (this.threadId ^ (this.threadId >>> 32));
			result = prime * result + this.threadName.hashCode();
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final ThreadId other = (ThreadId) obj;
			if (this.threadId != other.threadId)
				return false;
			if (!this.threadName.equals(other.threadName))
				return false;
			return true;
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
        return getId().getJavaThreadId();
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

    /**
     * Returns an iterator that iterates backwards through the execution trace.
     *
     * This iteration is very cheap since no information has to be cached (in
     * contrast to the Iterator returned by {@link #getIterator()}.
     * The trace is generated while reading in the trace file.
     *
     * @return an iterator that iterates backwards through the execution trace
     */
    public Iterator<Instance> getBackwardIterator() {
        return new BackwardInstructionIterator(this);
    }

    /**
     * Returns an iterator that is able to iterate in any direction through the execution trace.
     *
     * This iteration is usually much more expensive (especially with respect to memory
     * consumption) than the Iterator returned by {@link #getBackwardIterator()}.
     * So whenever you just need to iterate backwards, you should use that backward iterator.
     *
     * @return an iterator that is able to iterate in any direction through the execution trace
     */
    public ListIterator<Instance> getIterator() {
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
        int numJumps = 0;
        long instrCount = 0;
        long[] jumpInstrNrs = new long[16];
        int[] jumps = new int[16];
        byte[] stackDepthChange = new byte[16];

        final Iterator<Instance> backwardIt = getBackwardIterator();
        int lastIndex = 0;
        int curStackDepth = 1;
        while (backwardIt.hasNext()) {
            final Instance instr = backwardIt.next();
            final int index = instr.getIndex();
            if (index != lastIndex-1 && instrCount > 1) {
                if (numJumps == jumpInstrNrs.length) {
                    jumpInstrNrs = Arrays.copyOf(jumpInstrNrs, 2*numJumps);
                    jumps = Arrays.copyOf(jumps, 2*numJumps);
                    stackDepthChange = Arrays.copyOf(stackDepthChange, 2*numJumps);
                }
                jumpInstrNrs[numJumps] = instrCount;
                jumps[numJumps] = lastIndex - index;
                // TODO can the stack depth change by more than 256??
                final int newStackDepth = instr.getStackDepth();
                stackDepthChange[numJumps] = (byte) (curStackDepth - newStackDepth);
                ++numJumps;
                curStackDepth = newStackDepth;
            }
            lastIndex = index;
            ++instrCount;
        }

        if (numJumps != jumpInstrNrs.length) {
            jumpInstrNrs = Arrays.copyOf(jumpInstrNrs, numJumps);
            jumps = Arrays.copyOf(jumps, numJumps);
            stackDepthChange = Arrays.copyOf(stackDepthChange, numJumps);
        }

        return new ForwardIterationInformation(instrCount, lastIndex, jumpInstrNrs, jumps, stackDepthChange);
    }

    public Instruction findInstruction(final int instructionIndex) {
        return this.traceResult.getInstruction(instructionIndex);
    }

    public int compareTo(final ThreadTraceResult o) {
        return this.getId().compareTo(o.getId());
    }

}
