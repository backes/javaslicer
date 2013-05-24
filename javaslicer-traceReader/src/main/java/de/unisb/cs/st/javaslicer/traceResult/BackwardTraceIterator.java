/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult
 *    Class:     BackwardTraceIterator
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/BackwardTraceIterator.java
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.iterators.EmptyIterator;
import de.hammacher.util.maps.IntegerMap;
import de.hammacher.util.maps.IntegerToLongMap;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstanceFactory;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.TraceIterator;
import de.unisb.cs.st.javaslicer.common.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.common.progress.ProgressInformationProvider;
import de.unisb.cs.st.javaslicer.traceResult.traceSequences.ConstantTraceSequence;
import de.unisb.cs.st.javaslicer.traceResult.traceSequences.ConstantTraceSequence.ConstantIntegerTraceSequence;
import de.unisb.cs.st.javaslicer.traceResult.traceSequences.ConstantTraceSequence.ConstantLongTraceSequence;

public class BackwardTraceIterator<InstanceType extends InstructionInstance>
        implements Iterator<InstanceType>, TraceIterator, ProgressInformationProvider {

    public static final boolean WRITE_ITERATION_DEBUG_FILE = false;

    private final ThreadTraceResult threadTraceResult;
    private final InstanceFilter<? super InstanceType> filter;
    private final InstructionInstanceFactory<? extends InstanceType> instanceFactory;

    private InstanceType nextInstruction;
    private final IntegerMap<Iterator<Integer>> integerSequenceBackwardIterators;
    private final IntegerMap<Iterator<Long>> longSequenceBackwardIterators;
    private final IntegerToLongMap instructionNextOccurenceNumber;

    private int stackDepth;

    private long instancesCount = 0;
    private long filteredInstancesCount = 0;
    private final PrintWriter debugFileWriter;

    // to approximate the percentage done
	private long numCrossedLabels = 0;

    public BackwardTraceIterator(final ThreadTraceResult threadTraceResult,
            final InstanceFilter<? super InstanceType> filter,
            InstructionInstanceFactory<? extends InstanceType> instanceFactory)
            throws TracerException {
        this.filter = filter;
        this.threadTraceResult = threadTraceResult;
        this.instanceFactory = instanceFactory;
        this.integerSequenceBackwardIterators = new IntegerMap<Iterator<Integer>>();
        this.longSequenceBackwardIterators = new IntegerMap<Iterator<Long>>();
        this.instructionNextOccurenceNumber = new IntegerToLongMap();
        if (WRITE_ITERATION_DEBUG_FILE) {
            PrintWriter debugFileWriterTmp = null;
            try {
                debugFileWriterTmp = new PrintWriter(new FileOutputStream(new File("iteration_debug.log")));
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        BackwardTraceIterator.this.debugFileWriter.close();
                    }
                });
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
            }
            this.debugFileWriter = debugFileWriterTmp;
        } else
            this.debugFileWriter = null;
        this.stackDepth = this.threadTraceResult.lastStackDepth;
        this.nextInstruction = getNextInstruction(this.threadTraceResult.lastInstructionIndex);
    }

    @Override
	public boolean hasNext() {
        if (this.nextInstruction != null)
            return true;
        if (WRITE_ITERATION_DEBUG_FILE)
            this.debugFileWriter.close();
        return false;
    }

    @Override
	public InstanceType next() throws TracerException {
        if (this.nextInstruction == null)
            throw new NoSuchElementException();
        final InstanceType old = this.nextInstruction;
        this.nextInstruction = getNextInstruction(this.nextInstruction.getInstruction().getBackwardInstructionIndex(this));
        return old;
    }

    public List<ReadMethod> getInitialStackMethods() {
        return Collections.unmodifiableList(Arrays.asList(this.threadTraceResult.lastStackMethods));
    }

    private InstanceType getNextInstruction(final int nextIndex) throws TracerException {
        int index = nextIndex;
        while (true) {
            if (WRITE_ITERATION_DEBUG_FILE) {
                this.debugFileWriter.println(index);
            }
            final Instruction backwardInstruction = this.threadTraceResult.findInstruction(index);
            if (backwardInstruction == null) {
                assert index == -1;
                return null;
            }
            assert backwardInstruction.getIndex() == index;
            int tmpStackDepth = this.stackDepth;
            int opcode;
            if (backwardInstruction == backwardInstruction.getMethod().getMethodEntryLabel()) {
                --this.stackDepth;
                assert this.stackDepth >= 0 : "enter method occured more often than leave method";
            } else if (backwardInstruction == backwardInstruction.getMethod().getAbnormalTerminationLabel()
                    || ((opcode = backwardInstruction.getOpcode()) >= Opcodes.IRETURN
                            && opcode <= Opcodes.RETURN)) {
                // info: the return statements opcodes lie between 172 (IRETURN) and 177 (RETURN)
                this.stackDepth = ++tmpStackDepth;
            }
            final InstanceType instance = backwardInstruction.getNextInstance(this, tmpStackDepth,
                this.instancesCount, this.instanceFactory);
            assert instance != null;

            ++this.instancesCount;
            if (this.filter != null && this.filter.filterInstance(instance)) {
                ++this.filteredInstancesCount;
            } else {
                return instance;
            }
            index = backwardInstruction.getBackwardInstructionIndex(this);
        }
    }

    @Override
	public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
	public long getNextLong(final int seqIndex) throws TracerException {
        Iterator<Long> it = this.longSequenceBackwardIterators.get(seqIndex);
        if (it == null) {
            try {
                ConstantTraceSequence sequence = this.threadTraceResult.sequences.get(seqIndex);
                if (sequence == null)
                    throw new TracerException("corrupted data (cannot trace backwards)");
                it = ((ConstantLongTraceSequence)sequence).backwardIterator();
            } catch (final IOException e) {
                throw new TracerException(e);
            }
            this.longSequenceBackwardIterators.put(seqIndex, it);
        }
        if (!it.hasNext())
            throw new TracerException("corrupted data (cannot trace backwards)");
        Long ret = it.next();
        if (!it.hasNext())
            this.longSequenceBackwardIterators.put(seqIndex, EmptyIterator.<Long>getInstance());
        return ret;
    }

    @Override
	public int getNextInteger(final int seqIndex) throws TracerException {
        Iterator<Integer> it = this.integerSequenceBackwardIterators.get(seqIndex);
        if (it == null) {
            try {
                it = ((ConstantIntegerTraceSequence)this.threadTraceResult.sequences.get(seqIndex)).backwardIterator();
            } catch (final IOException e) {
                throw new TracerException(e);
            }
            this.integerSequenceBackwardIterators.put(seqIndex, it);
        }
        if (!it.hasNext())
            throw new TracerException("corrupted data (cannot trace backwards)");
        Integer ret = it.next();
        if (!it.hasNext())
            this.integerSequenceBackwardIterators.put(seqIndex, EmptyIterator.<Integer>getInstance());
        return ret;
    }

    @Override
	public long getNextInstructionOccurenceNumber(final int instructionIndex) {
        final long nr = this.instructionNextOccurenceNumber.incrementAndGet(instructionIndex, 1);
        return nr - 1;
    }

    public long getNumInstructions() {
        return this.instancesCount;
    }

    public long getNumFilteredInstructions() {
        return this.filteredInstancesCount;
    }

	@Override
	public void incNumCrossedLabels() {
		++this.numCrossedLabels;
	}

	@Override
	public double getPercentageDone() {
    	return this.threadTraceResult.numCrossedLabels == 0 ? 0 : (100. * this.numCrossedLabels / this.threadTraceResult.numCrossedLabels);
    }

}
