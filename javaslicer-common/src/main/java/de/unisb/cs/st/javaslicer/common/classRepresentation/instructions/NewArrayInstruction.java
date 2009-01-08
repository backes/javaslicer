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

/**
 * Class representing a NEWARRAY instruction.
 *
 * @author Clemens Hammacher
 */
public class NewArrayInstruction extends AbstractInstruction {

    public static class NewArrayInstrInstance extends AbstractInstance {

        private final long newObjectIdentifier;

        public NewArrayInstrInstance(AbstractInstruction instr,
                long occurenceNumber, int stackDepth, long newObjId) {
            super(instr, occurenceNumber, stackDepth);
            this.newObjectIdentifier = newObjId;
        }

        public long getNewObjectIdentifier() {
            return this.newObjectIdentifier;
        }

        @Override
        public String toString() {
            String s = super.toString();
            return new StringBuilder(s.length() + 10).append(s).append(" [").
                append(this.newObjectIdentifier).append(']').toString();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + (int)this.newObjectIdentifier;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            NewArrayInstrInstance other = (NewArrayInstrInstance) obj;
            if (this.newObjectIdentifier != other.newObjectIdentifier)
                return false;
            return true;
        }

    }

    private final int arrayElemType;
    private final int newObjectIdentifierSequenceIndex;

    public NewArrayInstruction(final ReadMethod readMethod, final int lineNumber,
            final int arrayElemType, int newObjIdSeqIndex) {
        super(readMethod, Opcodes.NEWARRAY, lineNumber);
        assert arrayElemType == Opcodes.T_BOOLEAN
            || arrayElemType == Opcodes.T_CHAR
            || arrayElemType == Opcodes.T_FLOAT
            || arrayElemType == Opcodes.T_DOUBLE
            || arrayElemType == Opcodes.T_BYTE
            || arrayElemType == Opcodes.T_SHORT
            || arrayElemType == Opcodes.T_INT
            || arrayElemType == Opcodes.T_LONG;
        this.arrayElemType = arrayElemType;
        this.newObjectIdentifierSequenceIndex = newObjIdSeqIndex;
    }

    private NewArrayInstruction(final ReadMethod readMethod, final int lineNumber,
            final int arrayElemType, final int index, int newObjIdSeqIndex) {
        super(readMethod, Opcodes.NEWARRAY, lineNumber, index);
        assert arrayElemType == Opcodes.T_BOOLEAN
            || arrayElemType == Opcodes.T_CHAR
            || arrayElemType == Opcodes.T_FLOAT
            || arrayElemType == Opcodes.T_DOUBLE
            || arrayElemType == Opcodes.T_BYTE
            || arrayElemType == Opcodes.T_SHORT
            || arrayElemType == Opcodes.T_INT
            || arrayElemType == Opcodes.T_LONG;
        this.arrayElemType = arrayElemType;
        this.newObjectIdentifierSequenceIndex = newObjIdSeqIndex;
    }

    /**
     * Returns the type of the array's elements.
     *
     * The integer is one of these constants:
     * <ul>
     *   <li>org.objectweb.asm.Opcodes.T_BOOLEAN (4)</li>
     *   <li>org.objectweb.asm.Opcodes.T_CHAR (5)</li>
     *   <li>org.objectweb.asm.Opcodes.T_FLOAT (6)</li>
     *   <li>org.objectweb.asm.Opcodes.T_DOUBLE (7)</li>
     *   <li>org.objectweb.asm.Opcodes.T_BYTE (8)</li>
     *   <li>org.objectweb.asm.Opcodes.T_SHORT (9)</li>
     *   <li>org.objectweb.asm.Opcodes.T_INT (10)</li>
     *   <li>org.objectweb.asm.Opcodes.T_LONG (11)</li>
     * </ul>
     *
     * @return the type the array's elements
     */
    public int getArrayElemType() {
        return this.arrayElemType;
    }

    public Type getType() {
        return Type.NEWARRAY;
    }

    @Override
    public Instance getNextInstance(TraceIterationInformationProvider infoProv,
            int stackDepth) {
        final long objectId = this.newObjectIdentifierSequenceIndex == -1 ? -1 :
            infoProv.getNextLong(this.newObjectIdentifierSequenceIndex);
        return new NewArrayInstrInstance(this,
            infoProv.getNextInstructionOccurenceNumber(getIndex()),
                stackDepth, objectId);
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        OptimizedDataOutputStream.writeInt0(this.arrayElemType, out);
        OptimizedDataOutputStream.writeInt0(this.newObjectIdentifierSequenceIndex, out);
    }

    public static NewArrayInstruction readFrom(final DataInputStream in, final MethodReadInformation methodInfo,
            @SuppressWarnings("unused") final StringCacheInput stringCache,
            @SuppressWarnings("unused") final int opcode,
            final int index, final int lineNumber) throws IOException {
        final int arrayElemType = OptimizedDataInputStream.readInt0(in);
        int newObjIdSeqIndex = OptimizedDataInputStream.readInt0(in);
        return new NewArrayInstruction(methodInfo.getMethod(), lineNumber, arrayElemType, index, newObjIdSeqIndex);
    }

    @Override
    public String toString() {
        String elemType;
        switch (this.arrayElemType) {
        case Opcodes.T_BOOLEAN:
            elemType = "T_BOOLEAN";
            break;
        case Opcodes.T_CHAR:
            elemType = "T_CHAR";
            break;
        case Opcodes.T_FLOAT:
            elemType = "T_FLOAT";
            break;
        case Opcodes.T_DOUBLE:
            elemType = "T_DOUBLE";
            break;
        case Opcodes.T_BYTE:
            elemType = "T_BYTE";
            break;
        case Opcodes.T_SHORT:
            elemType = "T_SHORT";
            break;
        case Opcodes.T_INT:
            elemType = "T_INT";
            break;
        case Opcodes.T_LONG:
            elemType = "T_LONG";
            break;
        default:
            elemType = "--ERROR--";
        }
        return new StringBuilder(elemType.length() + 9).append("NEWARRAY ").append(elemType).toString();
    }

}
