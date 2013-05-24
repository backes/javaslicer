/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult
 *    Class:     ForwardTraceIterator
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/ForwardTraceIterator.java
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

import java.io.IOException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import de.hammacher.util.maps.IntegerMap;
import de.hammacher.util.maps.IntegerToLongMap;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstanceFactory;
import de.unisb.cs.st.javaslicer.common.classRepresentation.TraceIterator;
import de.unisb.cs.st.javaslicer.common.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.traceResult.traceSequences.ConstantTraceSequence.ConstantIntegerTraceSequence;
import de.unisb.cs.st.javaslicer.traceResult.traceSequences.ConstantTraceSequence.ConstantLongTraceSequence;

public class ForwardTraceIterator<InstanceType extends InstructionInstance>
        implements Iterator<InstructionInstance>, TraceIterator {

    private long backwardInstrNr;
    private int nextIndex;
    private int stackDepth;

    private int nextJumpNr;
    private final long[] jumpInstrNrs;
    private final int[] jumps;
    private final int[] stackDepthChanges;

    private final IntegerToLongMap occurrences;
    private final ThreadTraceResult threadTraceResult;
    private final IntegerMap<ListIterator<Integer>> integerSequenceIterators;
    private final IntegerMap<ListIterator<Long>> longSequenceIterators;
    private final InstructionInstanceFactory<? extends InstanceType> instanceFactory;

    // for progress approximation
	private long numCrossedLabels = 0;

    public ForwardTraceIterator(ThreadTraceResult threadTraceResult,
            ForwardIterationInformation forwInfo, InstructionInstanceFactory<? extends InstanceType> instanceFactory) {
        this.threadTraceResult = threadTraceResult;
        this.backwardInstrNr = forwInfo.instrCount;
        this.nextIndex = forwInfo.firstInstrIndex;
        this.jumpInstrNrs = forwInfo.jumpInstrNrs;
        this.jumps = forwInfo.jumps;
        this.stackDepthChanges = forwInfo.stackDepthChanges;
        this.stackDepth = 1;
        this.nextJumpNr = forwInfo.jumpInstrNrs.length-1;
        this.occurrences = new IntegerToLongMap();
        this.integerSequenceIterators = new IntegerMap<ListIterator<Integer>>();
        this.longSequenceIterators = new IntegerMap<ListIterator<Long>>();
        this.instanceFactory = instanceFactory;
    }

    @Override
	public boolean hasNext() {
        return this.backwardInstrNr != 0;
    }

    @Override
	public InstructionInstance next() {
        if (!hasNext())
            throw new NoSuchElementException();
        --this.backwardInstrNr;

        while (true) {
            Instruction instr = this.threadTraceResult.findInstruction(this.nextIndex);
            InstructionInstance inst = instr.getNextInstance(this, this.stackDepth, this.backwardInstrNr, this.instanceFactory);
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

    @Override
	public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
	public long getNextInstructionOccurenceNumber(int index) {
        return this.occurrences.incrementAndGet(index, 1) - 1;
    }

    @Override
	public long getNextLong(int seqIndex) throws TracerException {
        ListIterator<Long> it = this.longSequenceIterators.get(seqIndex);
        if (it == null) {
            try {
                it = ((ConstantLongTraceSequence)this.threadTraceResult.sequences.get(seqIndex)).iterator();
            } catch (IOException e) {
                throw new TracerException(e);
            }
            this.longSequenceIterators.put(seqIndex, it);
        }
        if (!it.hasNext())
            throw new TracerException("corrupted data (cannot trace backwards)");
        return it.next();
    }

    @Override
	public int getNextInteger(int seqIndex) throws TracerException {
        ListIterator<Integer> it = this.integerSequenceIterators.get(seqIndex);
        if (it == null) {
            try {
                it = ((ConstantIntegerTraceSequence)this.threadTraceResult.sequences.get(seqIndex)).iterator();
            } catch (IOException e) {
                throw new TracerException(e);
            }
            this.integerSequenceIterators.put(seqIndex, it);
        }
        if (!it.hasNext())
            throw new TracerException("corrupted data (cannot trace backwards)");
        return it.next();
    }

	@Override
	public void incNumCrossedLabels() {
		++this.numCrossedLabels;
	}

	public double getPercentageDone() {
        return this.threadTraceResult.numCrossedLabels == 0 ? 0 : (100. * this.numCrossedLabels / this.threadTraceResult.numCrossedLabels);
    }

}
