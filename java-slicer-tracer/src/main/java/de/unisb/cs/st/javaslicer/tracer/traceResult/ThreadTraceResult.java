package de.unisb.cs.st.javaslicer.tracer.traceResult;

import java.io.DataInput;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.AbstractInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.LabelMarker;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.util.IntegerMap;
import de.unisb.cs.st.javaslicer.tracer.util.IntegerToIntegerMap;
import de.unisb.cs.st.javaslicer.tracer.util.IntegerToLongMap;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileReader;

public class ThreadTraceResult {

    private final long threadId;
    private final String threadName;
    protected final IntegerMap<ConstantTraceSequence> sequences;
    protected final IntegerToLongMap instructionOccurences;
    protected final int lastInstructionIndex;

    private final TraceResult traceResult;

    public ThreadTraceResult(final long threadId, final String threadName, final IntegerMap<ConstantTraceSequence> sequences, final IntegerToLongMap instructionOccurences, final int lastInstructionIndex, final TraceResult traceResult) {
        this.threadId = threadId;
        this.threadName = threadName;
        this.sequences = sequences;
        this.instructionOccurences = instructionOccurences;
        this.lastInstructionIndex = lastInstructionIndex;
        this.traceResult = traceResult;
    }

    public long getThreadId() {
        return this.threadId;
    }

    public String getThreadName() {
        return this.threadName;
    }

    public static ThreadTraceResult readFrom(final DataInput in, final TraceResult traceResult, final MultiplexedFileReader file) throws IOException {
        final long threadId = in.readLong();
        final String name = in.readUTF();
        int numSequences = in.readInt();
        final IntegerMap<ConstantTraceSequence> sequences = new IntegerMap<ConstantTraceSequence>();
        while (numSequences-- > 0) {
            final int nr = in.readInt();
            final ConstantTraceSequence seq = ConstantTraceSequence.readFrom(in, file);
            if (sequences.put(nr, seq) != null)
                throw new IOException("corrupted data");
        }
        int numInstructionsOccured = in.readInt();
        final IntegerToLongMap instructionOccurences = new IntegerToLongMap(8,
                IntegerToIntegerMap.DEFAULT_LOAD_FACTOR, IntegerToIntegerMap.DEFAULT_SWITCH_TO_MAP_RATIO,
                IntegerToIntegerMap.DEFAULT_SWITCH_TO_LIST_RATIO, -1);
        while (numInstructionsOccured-- > 0) {
            final int nr = in.readInt();
            final long occured = in.readLong();
            if (instructionOccurences.put(nr, occured) != -1)
                throw new IOException("corrupted data");
        }
        final int lastInstructionIndex = in.readInt();
        return new ThreadTraceResult(threadId, name, sequences, instructionOccurences, lastInstructionIndex, traceResult);
    }

    public Iterator<Instance> getBackwardIterator() {
        return new BackwardInstructionIterator();
    }

    public Instruction findInstruction(final int instructionIndex, final ReadClass tryClass, final ReadMethod tryMethod) {
        // note: when the instructionIndex is illegal, we will notice that in the last
        // step. all other steps should perform normally

        final ReadClass instrClass;
        final ReadMethod instrMethod;

        if (tryMethod == null || tryMethod.getInstructionNumberStart() > instructionIndex
                || tryMethod.getInstructionNumberEnd() <= instructionIndex) {
            int left, right, mid;
            if (tryClass == null || tryClass.getInstructionNumberStart() > instructionIndex
                    || tryClass.getInstructionNumberEnd() <= instructionIndex) {
                // first search for the correct class
                left = 0;
                right = this.traceResult.getReadClasses().size();
                while ((mid = (left + right) / 2) != left) {
                    final ReadClass midClass = this.traceResult.getReadClasses().get(mid);
                    if (midClass.getInstructionNumberStart() <= instructionIndex) {
                        left = mid;
                    } else { // midClass.getInstructionNumberStart() > instructionIndex
                        right = mid;
                    }
                }

                // now we know that mid and left both point to the class we need
                instrClass = this.traceResult.getReadClasses().get(left);
            } else
                instrClass = tryClass;
            final ArrayList<ReadMethod> methods = instrClass.getMethods();

            // and now we search for the correct method
            left = 0;
            right = methods.size();
            while ((mid = (left + right) / 2) != left) {
                final ReadMethod midMethod = methods.get(mid);
                if (midMethod.getInstructionNumberStart() <= instructionIndex) {
                    left = mid;
                } else { // midMethod.getInstructionNumberStart() > instructionIndex
                    right = mid;
                }
            }

            // yeah: we have the correct method
            instrMethod = methods.get(left);
        } else
            instrMethod = tryMethod;

        // now search for the instruction
        final ArrayList<AbstractInstruction> instructions = instrMethod.getInstructions();

        // we can just compute the offset of the instruction
        final int offset = instructionIndex - instrMethod.getInstructionNumberStart();
        if (offset < 0 || offset >= instructions.size())
            return null; // no instruction found

        final Instruction instr = instructions.get(offset);
        assert instr.getIndex() == instructionIndex;

        return instr;
    }

    public long getTraceLength() {
        long traceLength = 0;
        for (final Entry<Integer, Long> e: this.instructionOccurences.entrySet()) {
            final Instruction instr = findInstruction(e.getKey(), null, null);
            if (instr != null && !(instr instanceof LabelMarker && ((LabelMarker)instr).isAdditionalLabel()))
                traceLength += e.getValue();
        }
        return traceLength;
    }

    public class BackwardInstructionIterator implements Iterator<Instance> {

        private Instance nextInstruction;
        private final IntegerMap<Iterator<Integer>> integerSequenceBackwardIterators;
        private final IntegerMap<Iterator<Long>> longSequenceBackwardIterators;
        private final IntegerToLongMap instructionNextOccurenceNumber;

        private int instructionCount = 0;
        private int additionalInstructionCount = 0;

        public BackwardInstructionIterator() throws TracerException {
            this.integerSequenceBackwardIterators = new IntegerMap<Iterator<Integer>>();
            this.longSequenceBackwardIterators = new IntegerMap<Iterator<Long>>();
            this.instructionNextOccurenceNumber = ThreadTraceResult.this.instructionOccurences.clone();
            try {
                this.nextInstruction = getNextInstruction(null, ThreadTraceResult.this.lastInstructionIndex);
            } catch (final EOFException e) {
                this.nextInstruction = null;
            }
        }

        public boolean hasNext() {
            return this.nextInstruction != null;
        }

        public Instance next() throws TracerException {
            if (this.nextInstruction == null)
                throw new NoSuchElementException();
            final Instance old = this.nextInstruction;
            try {
                this.nextInstruction = getNextInstruction(this.nextInstruction);
            } catch (final EOFException e) {
                this.nextInstruction = null;
            }
            return old;
        }

        private Instance getNextInstruction(final Instruction old) throws TracerException, EOFException {
            return getNextInstruction(old.getMethod(), old.getBackwardInstructionIndex(this));
        }

        // TODO remove
        private int i = 0;
        private Instance getNextInstruction(final ReadMethod oldMethod, final int nextIndex) throws TracerException, EOFException {
            int index = nextIndex;
            while (true) {
                // TODO remove
                this.i++;
                try {
                    final PrintWriter iterationDebugWriter = new PrintWriter(new FileOutputStream(new File("iteration_debug.log"), this.i > 1));
                    iterationDebugWriter.println(index);
                    iterationDebugWriter.close();
                } catch (final FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                final Instruction backwardInstruction = findInstruction(index,
                        oldMethod == null ? null : oldMethod.getReadClass(), oldMethod);
                if (backwardInstruction == null)
                    return null;
                final Instance instance = backwardInstruction.getNextInstance(this);
                if (instance == null) {
                    ++this.additionalInstructionCount;
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
                it = ((ConstantLongTraceSequence)ThreadTraceResult.this.sequences.get(seqIndex)).backwardIterator();
                this.longSequenceBackwardIterators.put(seqIndex, it);
            }
            if (!it.hasNext())
                throw new TracerException("corrupted data (cannot trace backwards)");
            return it.next();
        }

        public int getNextInteger(final int seqIndex) throws TracerException {
            Iterator<Integer> it = this.integerSequenceBackwardIterators.get(seqIndex);
            if (it == null) {
                it = ((ConstantIntegerTraceSequence)ThreadTraceResult.this.sequences.get(seqIndex)).backwardIterator();
                this.integerSequenceBackwardIterators.put(seqIndex, it);
            }
            if (!it.hasNext())
                throw new TracerException("corrupted data (cannot trace backwards)");
            return it.next();
        }

        public long getNextInstructionOccurenceNumber(final int instructionIndex) throws EOFException {
            final long nr = this.instructionNextOccurenceNumber.incrementAndGet(instructionIndex, -1);
            if (nr < 0)
                throw new EOFException("reached beginning of the trace");
            return nr;
        }

        public int getNoInstructions() {
            return this.instructionCount;
        }

        public int getNoAdditionalInstructions() {
            return this.additionalInstructionCount;
        }

        public boolean visitedAllInstructions() {
            for (final Long nextOccNr: this.instructionNextOccurenceNumber.values())
                if (nextOccNr != 0)
                    return false;
            return true;
        }

    }

}
