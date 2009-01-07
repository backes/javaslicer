package de.unisb.cs.st.javaslicer.traceResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.IntegerMap;
import de.hammacher.util.IntegerToLongMap;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.TraceIterationInformationProvider;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Type;
import de.unisb.cs.st.javaslicer.common.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.traceResult.traceSequences.ConstantTraceSequence.ConstantIntegerTraceSequence;
import de.unisb.cs.st.javaslicer.traceResult.traceSequences.ConstantTraceSequence.ConstantLongTraceSequence;

public class BackwardInstructionIterator implements Iterator<Instance>, TraceIterationInformationProvider {

    public static final boolean WRITE_ITERATION_DEBUG_FILE = false;

    private final ThreadTraceResult threadTraceResult;
    private final InstanceFilter filter;

    private Instance nextInstruction;
    private final IntegerMap<Iterator<Integer>> integerSequenceBackwardIterators;
    private final IntegerMap<Iterator<Long>> longSequenceBackwardIterators;
    private final IntegerToLongMap instructionNextOccurenceNumber;

    private int stackDepth;

    private int instructionCount = 0;
    private int filteredInstructionCount = 0;
    private final PrintWriter debugFileWriter;

    public BackwardInstructionIterator(final ThreadTraceResult threadTraceResult, InstanceFilter filter)
            throws TracerException {
        this.filter = filter;
        this.threadTraceResult = threadTraceResult;
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
                        BackwardInstructionIterator.this.debugFileWriter.close();
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

    public boolean hasNext() {
        if (this.nextInstruction != null)
            return true;
        if (WRITE_ITERATION_DEBUG_FILE)
            this.debugFileWriter.close();
        return false;
    }

    public Instance next() throws TracerException {
        if (this.nextInstruction == null)
            throw new NoSuchElementException();
        final Instance old = this.nextInstruction;
        this.nextInstruction = getNextInstruction(this.nextInstruction.getInstruction().getBackwardInstructionIndex(this));
        return old;
    }

    private Instance getNextInstruction(final int nextIndex) throws TracerException {
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
            int stackDepth = this.stackDepth;
            if (backwardInstruction == backwardInstruction.getMethod().getMethodEntryLabel()) {
                --this.stackDepth;
                assert this.stackDepth >= 0 : "enter method occured more often than leave method";
            } else if (backwardInstruction == backwardInstruction.getMethod().getMethodLeaveLabel()) {
                this.stackDepth = ++stackDepth;
            } else if (backwardInstruction.getType() == Type.SIMPLE) {
                switch (backwardInstruction.getOpcode()) {
                case Opcodes.IRETURN: case Opcodes.LRETURN: case Opcodes.FRETURN:
                case Opcodes.DRETURN: case Opcodes.ARETURN: case Opcodes.RETURN:
                    this.stackDepth = ++stackDepth;
                }
            }
            final Instance instance = backwardInstruction.getNextInstance(this, stackDepth);
            assert instance != null;

            if (this.filter != null && this.filter.filterInstance(instance)) {
                ++this.filteredInstructionCount;
            } else {
                ++this.instructionCount;
                return instance;
            }
            index = backwardInstruction.getBackwardInstructionIndex(this);
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public long getNextLong(final int seqIndex) throws TracerException {
        Iterator<Long> it = this.longSequenceBackwardIterators.get(seqIndex);
        if (it == null) {
            try {
                it = ((ConstantLongTraceSequence)this.threadTraceResult.sequences.get(seqIndex)).backwardIterator();
            } catch (final IOException e) {
                throw new TracerException(e);
            }
            this.longSequenceBackwardIterators.put(seqIndex, it);
        }
        if (!it.hasNext())
            throw new TracerException("corrupted data (cannot trace backwards)");
        return it.next();
    }

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
        return it.next();
    }

    public long getNextInstructionOccurenceNumber(final int instructionIndex) {
        final long nr = this.instructionNextOccurenceNumber.incrementAndGet(instructionIndex, 1);
        return nr - 1;
    }

    public int getNumInstructions() {
        return this.instructionCount;
    }

    public int getNumFilteredInstructions() {
        return this.filteredInstructionCount;
    }

}
