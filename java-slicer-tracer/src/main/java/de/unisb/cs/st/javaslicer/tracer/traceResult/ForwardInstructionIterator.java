package de.unisb.cs.st.javaslicer.tracer.traceResult;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.hammacher.util.IntegerMap;
import de.hammacher.util.IntegerToLongMap;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences.ConstantTraceSequence.ConstantIntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceResult.traceSequences.ConstantTraceSequence.ConstantLongTraceSequence;

public class ForwardInstructionIterator implements Iterator<Instance> {

    private long instrNr;
    private int nextIndex;
    private int stackDepth;

    private int nextJumpNr;
    private final long[] jumpInstrNrs;
    private final int[] jumpTargets;
    private final byte[] stackDepthChanges;

    private final IntegerToLongMap occurrences;
    private final ThreadTraceResult threadTraceResult;
    private final IntegerMap<Iterator<Integer>> integerSequenceIterators;
    private final IntegerMap<Iterator<Long>> longSequenceIterators;

    public ForwardInstructionIterator(final ThreadTraceResult threadTraceResult, final ForwardIterationInformation forwInfo) {
        this.threadTraceResult = threadTraceResult;
        this.instrNr = forwInfo.instrCount;
        this.nextIndex = forwInfo.firstInstrIndex;
        this.jumpInstrNrs = forwInfo.jumpInstrNrs;
        this.jumpTargets = forwInfo.jumpTargets;
        this.stackDepthChanges = forwInfo.stackDepthChanges;
        this.stackDepth = 0;
        this.nextJumpNr = forwInfo.jumpInstrNrs.length-1;
        this.occurrences = forwInfo.occurrences.clone();
        this.integerSequenceIterators = new IntegerMap<Iterator<Integer>>();
        this.longSequenceIterators = new IntegerMap<Iterator<Long>>();
    }

    @Override
    public boolean hasNext() {
        return this.instrNr != 0;
    }

    @Override
    public Instance next() {
        if (!hasNext())
            throw new NoSuchElementException();
        --this.instrNr;

        while (true) {
	        final Instruction instr = this.threadTraceResult.findInstruction(this.nextIndex);
	        final Instance inst = instr.getForwardInstance(this, this.stackDepth);
	        if (inst == null) {
	        	--this.nextIndex;
	        	continue;
	        }

	        if (this.nextJumpNr != -1 && this.instrNr == this.jumpInstrNrs[this.nextJumpNr]) {
	            this.nextIndex = this.jumpTargets[this.nextJumpNr];
	            this.stackDepth += this.stackDepthChanges[this.nextJumpNr--];
	        } else {
	            --this.nextIndex;
	        }

	        return inst;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public long getNextInstructionOccurenceNumber(final int index) {
        return this.occurrences.incrementAndGet(index, -1);
    }

    public long getNextLong(final int seqIndex) throws TracerException {
        Iterator<Long> it = this.longSequenceIterators.get(seqIndex);
        if (it == null) {
            try {
                it = ((ConstantLongTraceSequence)this.threadTraceResult.sequences.get(seqIndex)).iterator();
            } catch (final IOException e) {
                throw new TracerException(e);
            }
            this.longSequenceIterators.put(seqIndex, it);
        }
        if (!it.hasNext())
            throw new TracerException("corrupted data (cannot trace backwards)");
        return it.next();
    }

    public int getNextInteger(final int seqIndex) throws TracerException {
        Iterator<Integer> it = this.integerSequenceIterators.get(seqIndex);
        if (it == null) {
            try {
                it = ((ConstantIntegerTraceSequence)this.threadTraceResult.sequences.get(seqIndex)).iterator();
            } catch (final IOException e) {
                throw new TracerException(e);
            }
            this.integerSequenceIterators.put(seqIndex, it);
        }
        if (!it.hasNext())
            throw new TracerException("corrupted data (cannot trace backwards)");
        return it.next();
    }

}
