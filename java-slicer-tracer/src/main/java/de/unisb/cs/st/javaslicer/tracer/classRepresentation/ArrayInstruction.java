package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

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
    public Instance getNextInstance(final BackwardInstructionIterator backwardInstructionIterator) throws TracerException, EOFException {
        final long objectId = backwardInstructionIterator.getNextLong(this.arrayTraceSeqIndex);
        final int index = backwardInstructionIterator.getNextInteger(this.indexTraceSeqIndex);
        return new Instance(this, objectId, index, backwardInstructionIterator.getNextInstructionOccurenceNumber(getIndex()));
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

    @Override
    public String toString() {
        switch (getOpcode()) {
        case Opcodes.IALOAD:
            return "IALOAD";
        case Opcodes.LALOAD:
            return "LALOAD";
        case Opcodes.FALOAD:
            return "FALOAD";
        case Opcodes.DALOAD:
            return "DALOAD";
        case Opcodes.AALOAD:
            return "AALOAD";
        case Opcodes.BALOAD:
            return "BALOAD";
        case Opcodes.CALOAD:
            return "CALOAD";
        case Opcodes.SALOAD:
            return "SALOAD";

        // array store:
        case Opcodes.IASTORE:
            return "IASTORE";
        case Opcodes.LASTORE:
            return "LASTORE";
        case Opcodes.FASTORE:
            return "FASTORE";
        case Opcodes.DASTORE:
            return "DASTORE";
        case Opcodes.AASTORE:
            return "AASTORE";
        case Opcodes.BASTORE:
            return "BASTORE";
        case Opcodes.CASTORE:
            return "CASTORE";
        case Opcodes.SASTORE:
            return "SASTORE";

        default:
            assert false;
            return "--ERROR--";
        }
    }

    public static class Instance extends AbstractInstance {

        private final long arrayId;
        private final int arrayIndex;

        public Instance(final ArrayInstruction arrayInstr, final long arrayId, final int arrayIndex, final long occurenceNumber) {
            super(arrayInstr, occurenceNumber);
            this.arrayId = arrayId;
            this.arrayIndex = arrayIndex;
        }

        public long getArrayId() {
            return this.arrayId;
        }

        public int getArrayIndex() {
            return this.arrayIndex;
        }

        @Override
        public String toString() {
            final String type = super.toString();
            final StringBuilder sb = new StringBuilder(type.length() + 20);
            sb.append(type).append(" [").append(this.arrayId).append(", ").append(this.arrayIndex).append(']');
            return sb.toString();
        }

    }

}
