package de.unisb.cs.st.javaslicer.common.classRepresentation.instructions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.OptimizedDataInputStream;
import de.hammacher.util.OptimizedDataOutputStream;
import de.hammacher.util.StringCacheInput;
import de.hammacher.util.StringCacheOutput;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.TraceIterationInformationProvider;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod.MethodReadInformation;
import de.unisb.cs.st.javaslicer.common.exceptions.TracerException;


/**
 * Class representing a field instruction (GETFIELD, PUTFIELD, GETSTATIC or PUTSTATIC).
 *
 * @author Clemens Hammacher
 */
public class FieldInstruction extends AbstractInstruction {

    public static class Instance extends AbstractInstance {

        private final long objectId;

        public Instance(final FieldInstruction fieldInstr, final long occurenceNumber, final int stackDepth, final long objectId) {
            super(fieldInstr, occurenceNumber, stackDepth);
            this.objectId = objectId;
        }

        public long getObjectId() {
            return this.objectId;
        }

    }

    private final String ownerInternalClassName;
    private final String fieldName;
    private final String fieldDesc;
    private final int objectTraceSeqIndex;
    private final boolean longValue;

    public FieldInstruction(final ReadMethod readMethod, final int opcode,
            final int lineNumber, final String ownerInternalClassName,
            final String fieldName, final String fieldDesc,
            final int objectTraceSeqIndex) {
        super(readMethod, opcode, lineNumber);
        this.ownerInternalClassName = ownerInternalClassName;
        this.fieldName = fieldName.intern();
        this.fieldDesc = fieldDesc;
        this.objectTraceSeqIndex = objectTraceSeqIndex;
        this.longValue = org.objectweb.asm.Type.getType(fieldDesc).getSize() == 2;
    }

    private FieldInstruction(final ReadMethod readMethod, final int opcode, final int lineNumber,
            final String ownerInternalClassName, final String fieldName,
            final String fieldDesc, final int objectTraceSeqIndex, final int index) {
        super(readMethod, opcode, lineNumber, index);
        this.ownerInternalClassName = ownerInternalClassName;
        this.fieldName = fieldName.intern();
        this.fieldDesc = fieldDesc;
        this.objectTraceSeqIndex = objectTraceSeqIndex;
        this.longValue = org.objectweb.asm.Type.getType(fieldDesc).getSize() == 2;
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

    public boolean isLongValue() {
        return this.longValue;
    }

    public Type getType() {
        return Type.FIELD;
    }

    @Override
    public Instance getBackwardInstance(final TraceIterationInformationProvider infoProv, final int stackDepth) throws TracerException {
        final long objectId = this.objectTraceSeqIndex == -1 ? -1 :
            infoProv.getNextLong(this.objectTraceSeqIndex);
        return new Instance(this, infoProv.getNextInstructionOccurenceNumber(getIndex()),
                stackDepth, objectId);
    }

    @Override
    public Instance getForwardInstance(final TraceIterationInformationProvider infoProv, final int stackDepth) throws TracerException {
        final long objectId = this.objectTraceSeqIndex == -1 ? -1 :
            infoProv.getNextLong(this.objectTraceSeqIndex);
        return new Instance(this, infoProv.getNextInstructionOccurenceNumber(getIndex()),
                stackDepth, objectId);
    }

    @Override
    public String toString() {
        String type;
        switch (getOpcode()) {
        case Opcodes.PUTSTATIC:
            type = "PUTSTATIC";
            break;
        case Opcodes.GETSTATIC:
            type = "GETSTATIC";
            break;

        case Opcodes.GETFIELD:
            type = "GETFIELD";
            break;

        case Opcodes.PUTFIELD:
            type = "PUTFIELD";
            break;

        default:
            assert false;
            type = "--ERROR--";
            break;
        }

        final StringBuilder sb = new StringBuilder(type.length() + this.ownerInternalClassName.length() + this.fieldName.length() + this.fieldDesc.length() + 3);
        sb.append(type).append(' ').append(this.ownerInternalClassName).append('.').append(this.fieldName).append(' ').append(this.fieldDesc);
        return sb.toString();
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        stringCache.writeString(this.fieldDesc, out);
        stringCache.writeString(this.fieldName, out);
        OptimizedDataOutputStream.writeInt0(this.objectTraceSeqIndex, out);
        stringCache.writeString(this.ownerInternalClassName, out);
    }

    public static FieldInstruction readFrom(final DataInputStream in, final MethodReadInformation methodInfo,
            final StringCacheInput stringCache,
            final int opcode, final int index, final int lineNumber) throws IOException {
        final String fieldDesc = stringCache.readString(in);
        final String fieldName = stringCache.readString(in);
        final int objectTraceSeqIndex = OptimizedDataInputStream.readInt0(in);
        final String ownerInternalClassName = stringCache.readString(in);
        return new FieldInstruction(methodInfo.getMethod(), opcode, lineNumber, ownerInternalClassName, fieldName, fieldDesc, objectTraceSeqIndex, index);
    }

}
