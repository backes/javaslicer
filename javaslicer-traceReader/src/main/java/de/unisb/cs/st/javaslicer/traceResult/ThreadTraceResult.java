/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult
 *    Class:     ThreadTraceResult
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/ThreadTraceResult.java
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
    protected final long numCrossedLabels;
    protected final int lastStackDepth;
    protected final ReadMethod[] lastStackMethods;

    private final TraceResult traceResult;

    private SoftReference<ForwardIterationInformation> forwardIterationInformation
        = new SoftReference<ForwardIterationInformation>(null);
    private final Object forwardIterationInfoLock = new Object();

    public ThreadTraceResult(long threadId, String threadName,
            IntegerMap<ConstantTraceSequence> sequences, int lastInstructionIndex,
            long numCrossedLabels, TraceResult traceResult, int lastStackDepth, ReadMethod[] lastStackMethods) {
        this.id = new ThreadId(threadId, threadName);
        this.sequences = sequences;
        this.numCrossedLabels = numCrossedLabels;
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

    public static ThreadTraceResult readFrom(DataInputStream in, TraceResult traceResult, MultiplexedFileReader file) throws IOException {
        long threadId = in.readLong();
        String name = in.readUTF();
        ConstantThreadTraces threadTraces = ConstantThreadTraces.readFrom(in);
        int numSequences = in.readInt();
        IntegerMap<ConstantTraceSequence> sequences = new IntegerMap<ConstantTraceSequence>(numSequences*4/3+1);
        while (numSequences-- > 0) {
            int nr = in.readInt();
            ConstantTraceSequence seq = threadTraces.readSequence(in, file);
            if (sequences.put(nr, seq) != null)
                throw new IOException("corrupted data");
        }
        int lastInstructionIndex = in.readInt();
        long numCrossedLabels = in.readLong();
        int lastStackDepth = in.readInt();
        ReadMethod[] lastStackMethods = new ReadMethod[lastStackDepth];
        for (int i = 0; i < lastStackDepth; ++i) {
            Instruction instr = traceResult.getInstruction(in.readInt());
            if (instr == null)
                throw new IOException("corrupted data");
            lastStackMethods[i] = instr .getMethod();
        }
        return new ThreadTraceResult(threadId, name, sequences, lastInstructionIndex, numCrossedLabels, traceResult, lastStackDepth, lastStackMethods);
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
     * @param instanceFactory a factory which is used to create the instruction instance objects.
     *                        may be used to return special objects which can be annotated by the user
     *                        of this function.
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
     * @param instanceFactory a factory which is used to create the instruction instance objects.
     *                        may be used to return special objects which can be annotated by the user
     *                        of this function.
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
        int[] stackDepthChange = new int[16];

        BackwardTraceIterator<AbstractInstructionInstance> backwardIt = getBackwardIterator(null, new AbstractInstructionInstanceFactory());
        int lastIndex = 0;
        int curStackDepth = 1;
        while (backwardIt.hasNext()) {
            InstructionInstance instr = backwardIt.next();
            int index = instr.getInstruction().getIndex();
            if (index != lastIndex-1 && instrCount > 1) {
                if (numJumps == jumpInstrNrs.length) {
                    long[] newJumpInstrNrs = new long[2*numJumps];
                    System.arraycopy(jumpInstrNrs, 0, newJumpInstrNrs, 0, numJumps);
                    jumpInstrNrs = newJumpInstrNrs;
                    int[] newJumps = new int[2*numJumps];
                    System.arraycopy(jumps, 0, newJumps, 0, numJumps);
                    jumps = newJumps;
                    int[] newStackDepthChange = new int[2*numJumps];
                    System.arraycopy(stackDepthChange, 0, newStackDepthChange, 0, numJumps);
                    stackDepthChange = newStackDepthChange;
                }
                jumpInstrNrs[numJumps] = instrCount;
                jumps[numJumps] = lastIndex - index;
                int newStackDepth = instr.getStackDepth();
                stackDepthChange[numJumps] = curStackDepth - newStackDepth;
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
            int[] newStackDepthChange = new int[numJumps];
            System.arraycopy(stackDepthChange, 0, newStackDepthChange, 0, numJumps);
            stackDepthChange = newStackDepthChange;
        }

        return new ForwardIterationInformation(instrCount, lastIndex, jumpInstrNrs, jumps, stackDepthChange);
    }

    public Instruction findInstruction(int instructionIndex) {
        return this.traceResult.getInstruction(instructionIndex);
    }

    @Override
	public int compareTo(ThreadTraceResult o) {
        return this.getId().compareTo(o.getId());
    }

}
