package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.traceResult.ThreadTraceResult.BackwardInstructionIterator;

public class ArrayInstruction extends AbstractInstruction {

    // when tracing, these two fields are set
    private final int arrayTraceSeqIndex;
    private final int indexTraceSeqIndex;

    public ArrayInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode,
            final int arrayTraceSeqIndex, final int indexTraceSeqIndex) {
        super(readMethod, opcode, lineNumber);
        this.arrayTraceSeqIndex = arrayTraceSeqIndex;
        this.indexTraceSeqIndex = indexTraceSeqIndex;
    }

    @Override
    public Instruction getNextInstance(final BackwardInstructionIterator backwardInstructionIterator) throws TracerException {
        final long objectId = backwardInstructionIterator.getNextLong(this.arrayTraceSeqIndex);
        final int index = backwardInstructionIterator.getNextInteger(this.indexTraceSeqIndex);
        return new Instance(this, objectId, index);
    }

    @Override
    public void writeOut(final DataOutput out) throws IOException {
        super.writeOut(out);
        out.writeInt(this.arrayTraceSeqIndex);
        out.writeInt(this.indexTraceSeqIndex);
    }

    public static ArrayInstruction readFrom(final DataInput in, final ReadMethod readMethod, final int opcode, final int index, final int lineNumber) throws IOException {
        final int arrayTraceSeqIndex = in.readInt();
        final int indexTraceSeqIndex = in.readInt();
        return new ArrayInstruction(readMethod, lineNumber, opcode, arrayTraceSeqIndex, indexTraceSeqIndex);
    }

    public static class Instance extends InstructionWrapper {

        private final long arrayId;
        private final int arrayIndex;

        public Instance(final ArrayInstruction arrayInstr, final long arrayId, final int arrayIndex) {
            super(arrayInstr);
            this.arrayId = arrayId;
            this.arrayIndex = arrayIndex;
        }

        public long getArrayId() {
            return this.arrayId;
        }

        public int getArrayIndex() {
            return this.arrayIndex;
        }

    }

}
