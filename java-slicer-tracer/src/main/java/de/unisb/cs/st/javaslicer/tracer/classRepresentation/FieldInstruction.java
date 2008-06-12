package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.traceResult.ThreadTraceResult.BackwardInstructionIterator;


public class FieldInstruction extends AbstractInstruction {

    private final String ownerInternalClassName;
    private final String fieldName;
    private final String fieldDesc;
    private final int objectTraceSeqIndex;

    public FieldInstruction(final ReadMethod readMethod, final int opcode, final int lineNumber,
            final String ownerInternalClassName, final String fieldName,
            final String fieldDesc, final int objectTraceSeqIndex) {
        super(readMethod, opcode, lineNumber);
        this.ownerInternalClassName = ownerInternalClassName;
        this.fieldName = fieldName;
        this.fieldDesc = fieldDesc;
        this.objectTraceSeqIndex = objectTraceSeqIndex;
    }

    public String getOwnerInternalClassName() {
        return this.ownerInternalClassName;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public String getFieldDesc() {
        return this.fieldDesc;
    }

    @Override
    public Instance getNextInstance(final BackwardInstructionIterator backwardInstructionIterator) throws TracerException, EOFException {
        final long objectId = this.objectTraceSeqIndex == -1 ? -1 :
            backwardInstructionIterator.getNextLong(this.objectTraceSeqIndex);
        return new Instance(this, objectId,
                backwardInstructionIterator.getNextInstructionOccurenceNumber(getIndex()));
    }

    @Override
    public void writeOut(final DataOutput out) throws IOException {
        super.writeOut(out);
        out.writeUTF(this.fieldDesc);
        out.writeUTF(this.fieldName);
        out.writeInt(this.objectTraceSeqIndex);
        out.writeUTF(this.ownerInternalClassName);
    }

    public static FieldInstruction readFrom(final DataInput in, final ReadMethod readMethod, final int opcode, final int index, final int lineNumber) throws IOException {
        final String fieldDesc = in.readUTF();
        final String fieldName = in.readUTF();
        final int objectTraceSeqIndex = in.readInt();
        final String ownerInternalClassName = in.readUTF();
        return new FieldInstruction(readMethod, opcode, lineNumber, ownerInternalClassName, fieldName, fieldDesc, objectTraceSeqIndex);
    }

    public static class Instance extends AbstractInstance {

        private final long objectId;

        public Instance(final FieldInstruction fieldInstr, final long objectId, final long occurenceNumber) {
            super(fieldInstr, occurenceNumber);
            this.objectId = objectId;
        }

        public long getObjectId() {
            return this.objectId;
        }

    }

}
