package de.unisb.cs.st.javaslicer.tracer.traceResult;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.AbstractInstruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.LabelMarker;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.util.IntegerMap;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileReader;

public class ThreadTraceResult {

    private final long threadId;
    private final String threadName;
    protected final IntegerMap<ConstantTraceSequence> sequences;
    protected final int lastInstructionIndex;

    private final TraceResult traceResult;

    public ThreadTraceResult(final long threadId, final String threadName, final IntegerMap<ConstantTraceSequence> sequences, final int lastInstructionIndex, final TraceResult traceResult) {
        this.threadId = threadId;
        this.threadName = threadName;
        this.sequences = sequences;
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
        final int lastInstructionIndex = in.readInt();
        return new ThreadTraceResult(threadId, name, sequences, lastInstructionIndex, traceResult);
    }

    public Iterator<Instruction> getBackwardIterator() {
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
        final Instruction instr = instructions.get(offset);
        assert instr.getIndex() == instructionIndex;

        return instr;
    }

    public class BackwardInstructionIterator implements Iterator<Instruction> {

        Instruction nextInstruction;
        private final Map<Integer, Iterator<Integer>> integerSequenceBackwardIterators;
        private final Map<Integer, Iterator<Long>> longSequenceBackwardIterators;

        public BackwardInstructionIterator() throws TracerException {
            final Instruction tmp = findInstruction(ThreadTraceResult.this.lastInstructionIndex, null, null);
            this.nextInstruction = tmp.getNextInstance(this);
            this.integerSequenceBackwardIterators = new IntegerMap<Iterator<Integer>>();
            this.longSequenceBackwardIterators = new IntegerMap<Iterator<Long>>();
        }

        public boolean hasNext() {
            return this.nextInstruction != null;
        }

        public Instruction next() throws TracerException {
            if (this.nextInstruction == null)
                throw new NoSuchElementException();
            final Instruction old = this.nextInstruction;
            this.nextInstruction = getNextInstruction(this.nextInstruction);
            return old;
        }

        private Instruction getNextInstruction(final Instruction old) throws TracerException {
            final ReadMethod oldMethod = old.getMethod();
            final ReadClass oldClass = oldMethod.getReadClass();
            final int backwardInstructionIndex = old.getBackwardInstructionIndex(this);
            final Instruction backwardInstruction = findInstruction(backwardInstructionIndex, oldClass, oldMethod);
            while (backwardInstruction instanceof LabelMarker)
                return getNextInstruction(backwardInstruction);
            return backwardInstruction.getNextInstance(this);
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

    }

}
