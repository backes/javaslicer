package de.unisb.cs.st.javaslicer.traceResult;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.ListIterator;

import de.hammacher.util.MultiplexedFileReader;
import de.hammacher.util.maps.IntegerMap;
import de.unisb.cs.st.javaslicer.common.classRepresentation.AbstractInstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.AbstractInstructionInstanceFactory;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstanceFactory;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.traceResult.traceSequences.ConstantThreadTraces;
import de.unisb.cs.st.javaslicer.traceResult.traceSequences.ConstantTraceSequence;

public class ThreadTraceResult implements Comparable<ThreadTraceResult> {

    private final ThreadId id;
    protected final IntegerMap<ConstantTraceSequence> sequences;
    protected final int lastInstructionIndex;
    protected final int lastStackDepth;
    protected final ReadMethod[] lastStackMethods;

    private final TraceResult traceResult;

    private SoftReference<ForwardIterationInformation> forwardIterationInformation
        = new SoftReference<ForwardIterationInformation>(null);
    private final Object forwardIterationInfoLock = new Object();

    public ThreadTraceResult(final long threadId, final String threadName,
            final IntegerMap<ConstantTraceSequence> sequences, final int lastInstructionIndex,
            final TraceResult traceResult, final int lastStackDepth, ReadMethod[] lastStackMethods) {
        this.id = new ThreadId(threadId, threadName);
        this.sequences = sequences;
        this.lastInstructionIndex = lastInstructionIndex;
        this.traceResult = traceResult;
        this.lastStackDepth = lastStackDepth;
        this.lastStackMethods = lastStackMethods;
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
        ReadMethod[] lastStackMethods = new ReadMethod[lastStackDepth];
        for (int i = 0; i < lastStackDepth; ++i) {
            Instruction instr = traceResult.getInstruction(in.readInt());
            if (instr == null)
                throw new IOException("corrupted data");
            lastStackMethods[i] = instr .getMethod();
        }
        return new ThreadTraceResult(threadId, name, sequences, lastInstructionIndex, traceResult, lastStackDepth, lastStackMethods);
    }

    /**
     * Returns an iterator that iterates backwards through the execution trace.
     *
     * This iteration is very cheap since no information has to be cached (in
     * contrast to the Iterator returned by {@link #getForwardIterator(InstructionInstanceFactory)}.
     * The trace is generated while reading in the trace file.
     *
     * @param filter   a filter to ignore certain instruction instances.
     *                 may be <code>null</code>.
     * @param instanceFactory
     * @return an iterator that iterates backwards through the execution trace.
     *         the iterator extends {@link Iterator} over {@link InstructionInstance}.
     */
    public <InstanceType extends InstructionInstance> BackwardTraceIterator<InstanceType> getBackwardIterator(
            InstanceFilter<? super InstanceType> filter, InstructionInstanceFactory<? extends InstanceType> instanceFactory) {
        return new BackwardTraceIterator<InstanceType>(this, filter, instanceFactory);
    }

    /**
     * Returns an iterator that is able to iterate in any direction through the execution trace.
     *
     * This iteration is usually much more expensive (especially with respect to memory
     * consumption) than the Iterator returned by {@link #getBackwardIterator(InstanceFilter, InstructionInstanceFactory)}.
     * So whenever you just need to iterate backwards, you should use that backward iterator.
     * @param instanceFactory
     *
     * @return an iterator that is able to iterate in any direction through the execution trace.
     *         the iterator extends {@link ListIterator} over {@link InstructionInstance}.
     */
    public <InstanceType extends InstructionInstance> ForwardTraceIterator<InstanceType> getForwardIterator(
            InstructionInstanceFactory<InstanceType> instanceFactory) {
        ForwardIterationInformation forwInfo;
        synchronized (this.forwardIterationInfoLock) {
            forwInfo = this.forwardIterationInformation.get();
            if (forwInfo == null) {
                forwInfo = getForwardInformation();
                this.forwardIterationInformation = new SoftReference<ForwardIterationInformation>(forwInfo);
            }
        }
        return new ForwardTraceIterator<InstanceType>(this, forwInfo, instanceFactory);
    }

    private ForwardIterationInformation getForwardInformation() {
        int numJumps = 0;
        long instrCount = 0;
        long[] jumpInstrNrs = new long[16];
        int[] jumps = new int[16];
        byte[] stackDepthChange = new byte[16];

        final BackwardTraceIterator<AbstractInstructionInstance> backwardIt = getBackwardIterator(null, new AbstractInstructionInstanceFactory());
        int lastIndex = 0;
        int curStackDepth = 1;
        while (backwardIt.hasNext()) {
            final InstructionInstance instr = backwardIt.next();
            final int index = instr.getInstruction().getIndex();
            if (index != lastIndex-1 && instrCount > 1) {
                if (numJumps == jumpInstrNrs.length) {
                    long[] newJumpInstrNrs = new long[2*numJumps];
                    System.arraycopy(jumpInstrNrs, 0, newJumpInstrNrs, 0, numJumps);
                    jumpInstrNrs = newJumpInstrNrs;
                    int[] newJumps = new int[2*numJumps];
                    System.arraycopy(jumps, 0, newJumps, 0, numJumps);
                    jumps = newJumps;
                    byte[] newStackDepthChange = new byte[2*numJumps];
                    System.arraycopy(stackDepthChange, 0, newStackDepthChange, 0, numJumps);
                    stackDepthChange = newStackDepthChange;
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
            long[] newJumpInstrNrs = new long[numJumps];
            System.arraycopy(jumpInstrNrs, 0, newJumpInstrNrs, 0, numJumps);
            jumpInstrNrs = newJumpInstrNrs;
            int[] newJumps = new int[numJumps];
            System.arraycopy(jumps, 0, newJumps, 0, numJumps);
            jumps = newJumps;
            byte[] newStackDepthChange = new byte[numJumps];
            System.arraycopy(stackDepthChange, 0, newStackDepthChange, 0, numJumps);
            stackDepthChange = newStackDepthChange;
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
