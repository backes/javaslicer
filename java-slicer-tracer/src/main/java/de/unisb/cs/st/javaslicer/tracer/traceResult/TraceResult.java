package de.unisb.cs.st.javaslicer.tracer.traceResult;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadClass;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;

public class TraceResult {

    private final List<ReadClass> readClasses;
    private final List<ConstantTraceSequence> sequences;
    protected final int lastInstructionIndex;

    public TraceResult(final List<ReadClass> readClasses, final List<ConstantTraceSequence> sequences, final int lastInstructionIndex) {
        this.readClasses = readClasses;
        this.sequences = sequences;
        this.lastInstructionIndex = lastInstructionIndex;
    }

    public static TraceResult readFrom(final ObjectInputStream in) throws IOException {
        int numClasses = in.readInt();
        final List<ReadClass> readClasses = new ArrayList<ReadClass>(numClasses);
        while (numClasses-- > 0)
            readClasses.add(ReadClass.readFrom(in));

        int numSeq = in.readInt();
        final List<ConstantTraceSequence> sequences = new ArrayList<ConstantTraceSequence>(numSeq);
        while (numSeq-- > 0)
            sequences.add(ConstantTraceSequence.readFrom(in));
        final int lastInstructionIndex = in.read();

        if (in.read() != -1)
            throw new IOException("Corrupt data");

        return new TraceResult(readClasses, sequences, lastInstructionIndex);
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
                right = this.readClasses.size();
                while ((mid = (left + right) / 2) != left) {
                    final ReadClass midClass = this.readClasses.get(mid);
                    if (midClass.getInstructionNumberStart() <= instructionIndex) {
                        left = mid;
                    } else { // midClass.getInstructionNumberStart() > instructionIndex
                        right = mid;
                    }
                }

                // now we know that mid and left both point to the class we need
                instrClass = this.readClasses.get(left);
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
        final ArrayList<Instruction> instructions = instrMethod.getInstructions();

        // we can just compute the offset of the instruction
        final int offset = instructionIndex - instrMethod.getInstructionNumberStart();
        if (offset >= instrMethod.getInstructionNumberEnd()) {
            // then this instruction does not exist
            return null;
        }

        final Instruction instr = instructions.get(offset);
        assert instr.getIndex() == instructionIndex;

        return instr;
    }

    public class BackwardInstructionIterator implements Iterator<Instruction> {

        Instruction nextInstruction;

        public BackwardInstructionIterator() {
            this.nextInstruction = findInstruction(TraceResult.this.lastInstructionIndex, null, null);
        }

        public boolean hasNext() {
            return this.nextInstruction != null;
        }

        public Instruction next() {
            if (this.nextInstruction == null)
                throw new NoSuchElementException();
            final Instruction old = this.nextInstruction;
            final ReadMethod oldMethod = old.getMethod();
            final ReadClass oldClass = oldMethod.getReadClass();
            this.nextInstruction = findInstruction(old.getBackwardInstructionIndex(), oldClass, oldMethod);
            return old;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
