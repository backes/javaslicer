package de.unisb.cs.st.javaslicer.traceResult;

import java.io.IOException;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import de.hammacher.util.IntegerMap;
import de.hammacher.util.IntegerToLongMap;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.TraceIterationInformationProvider;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.common.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.traceResult.traceSequences.ConstantTraceSequence.ConstantIntegerTraceSequence;
import de.unisb.cs.st.javaslicer.traceResult.traceSequences.ConstantTraceSequence.ConstantLongTraceSequence;

public class ForwardInstructionIterator implements ListIterator<Instance>, TraceIterationInformationProvider {

    private long backwardInstrNr;
    private int nextIndex;
    private int stackDepth;
    private final long traceLength;

    private int nextJumpNr;
    private final long[] jumpInstrNrs;
    private final int[] jumps;
    private final byte[] stackDepthChanges;

    private final IntegerToLongMap occurrences;
    private final ThreadTraceResult threadTraceResult;
    private final IntegerMap<ListIterator<Integer>> integerSequenceIterators;
    private final IntegerMap<ListIterator<Long>> longSequenceIterators;
    private boolean backwards;

    public ForwardInstructionIterator(final ThreadTraceResult threadTraceResult, final ForwardIterationInformation forwInfo) {
        this.threadTraceResult = threadTraceResult;
        this.backwardInstrNr = forwInfo.instrCount;
        this.traceLength = forwInfo.instrCount;
        this.nextIndex = forwInfo.firstInstrIndex;
        this.jumpInstrNrs = forwInfo.jumpInstrNrs;
        this.jumps = forwInfo.jumps;
        this.stackDepthChanges = forwInfo.stackDepthChanges;
        this.stackDepth = 1;
        this.nextJumpNr = forwInfo.jumpInstrNrs.length-1;
        this.occurrences = new IntegerToLongMap();
        this.integerSequenceIterators = new IntegerMap<ListIterator<Integer>>();
        this.longSequenceIterators = new IntegerMap<ListIterator<Long>>();
    }

    public boolean hasNext() {
        return this.backwardInstrNr != 0;
    }

    public Instance next() {
        if (!hasNext())
            throw new NoSuchElementException();
        --this.backwardInstrNr;

        this.backwards=false;
        while (true) {
            final Instruction instr = this.threadTraceResult.findInstruction(this.nextIndex);
            final Instance inst = instr.getNextInstance(this, this.stackDepth);
            if (inst == null) {
                ++this.nextIndex;
                continue;
            }

            if (this.nextJumpNr != -1 && this.backwardInstrNr == this.jumpInstrNrs[this.nextJumpNr]) {
                this.nextIndex += this.jumps[this.nextJumpNr];
                this.stackDepth += this.stackDepthChanges[this.nextJumpNr--];
            } else {
                ++this.nextIndex;
            }

            return inst;
        }
    }

    public Instance previous() {
        if (!hasPrevious())
            throw new NoSuchElementException();

        if (this.nextJumpNr +1 != this.jumpInstrNrs.length
                && this.backwardInstrNr == this.jumpInstrNrs[this.nextJumpNr+1]) {
            this.nextIndex -= this.jumps[++this.nextJumpNr];
            this.stackDepth -= this.stackDepthChanges[this.nextJumpNr];
        } else {
            --this.nextIndex;
        }

        this.backwards=true;
        while (true) {
            final Instruction instr = this.threadTraceResult.findInstruction(this.nextIndex);
            final Instance inst = instr.getNextInstance(this, this.stackDepth);
            if (inst == null) {
                --this.nextIndex;
                continue;
            }

            ++this.backwardInstrNr;

            return inst;
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void add(final Instance e) {
        throw new UnsupportedOperationException();
    }

    public boolean hasPrevious() {
        return this.backwardInstrNr != this.traceLength;
    }

    public int nextIndex() {
        return (int)Math.min(Integer.MAX_VALUE, this.traceLength - this.backwardInstrNr);
    }

    public int previousIndex() {
        return this.nextIndex-1;
    }

    public void set(final Instance e) {
        throw new UnsupportedOperationException();

    }

    public long getNextInstructionOccurenceNumber(final int index) {
        if (this.backwards)
            return this.occurrences.incrementAndGet(index, -1);
        return this.occurrences.incrementAndGet(index, 1) - 1;
    }

    public long getNextLong(final int seqIndex) throws TracerException {
        ListIterator<Long> it = this.longSequenceIterators.get(seqIndex);
        if (it == null) {
            try {
                it = ((ConstantLongTraceSequence)this.threadTraceResult.sequences.get(seqIndex)).iterator();
            } catch (final IOException e) {
                throw new TracerException(e);
            }
            this.longSequenceIterators.put(seqIndex, it);
        }
        if (this.backwards) {
            if (!it.hasPrevious())
                throw new TracerException("corrupted data (cannot trace backwards)");
            return it.previous();
        }
        if (!it.hasNext())
            throw new TracerException("corrupted data (cannot trace backwards)");
        return it.next();
    }

    public int getNextInteger(final int seqIndex) throws TracerException {
        ListIterator<Integer> it = this.integerSequenceIterators.get(seqIndex);
        if (it == null) {
            try {
                it = ((ConstantIntegerTraceSequence)this.threadTraceResult.sequences.get(seqIndex)).iterator();
            } catch (final IOException e) {
                throw new TracerException(e);
            }
            this.integerSequenceIterators.put(seqIndex, it);
        }
        if (this.backwards) {
            if (!it.hasPrevious())
                throw new TracerException("corrupted data (cannot trace backwards)");
            return it.previous();
        }
        if (!it.hasNext())
            throw new TracerException("corrupted data (cannot trace backwards)");
        return it.next();
    }

}
