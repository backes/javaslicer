package de.unisb.cs.st.javaslicer.common.classRepresentation.instructions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.OptimizedDataInputStream;
import de.hammacher.util.OptimizedDataOutputStream;
import de.hammacher.util.StringCacheInput;
import de.hammacher.util.StringCacheOutput;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.TraceIterationInformationProvider;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod.MethodReadInformation;

/**
 * Class representing a MULTIANEWARRAY instruction.
 *
 * @author Clemens Hammacher
 */
public class MultiANewArrayInstruction extends AbstractInstruction {

    public static class MultiANewArrayInstrInstance extends AbstractInstance {

        private final long[] newObjectIdentifiers;

        public MultiANewArrayInstrInstance(AbstractInstruction instr,
                long occurenceNumber, int stackDepth, long[] newObjects) {
            super(instr, occurenceNumber, stackDepth);
            this.newObjectIdentifiers = newObjects;
        }

        public long[] getNewObjectIdentifiers() {
            return this.newObjectIdentifiers;
        }

        @Override
        public String toString() {
            String s = super.toString();
            if (this.newObjectIdentifiers.length == 0)
                return s + " [(0)]";

            StringBuilder sb = new StringBuilder(s.length() + 30).append(s);
            sb.append(" [(").append(this.newObjectIdentifiers.length).append(") ");
            for (int i = 0; i < this.newObjectIdentifiers.length; ++i) {
                if (i != 0)
                    sb.append(", ");
                sb.append(this.newObjectIdentifiers[i]);
                if (i == 5)
                    return sb.append(", ...]").toString();
            }
            return sb.append(']').toString();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + Arrays.hashCode(this.newObjectIdentifiers);
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
            MultiANewArrayInstrInstance other =
                    (MultiANewArrayInstrInstance) obj;
            if (!Arrays
                .equals(this.newObjectIdentifiers, other.newObjectIdentifiers))
                return false;
            return true;
        }

    }

    private final String typeDesc;
    private final int dims;
    private final int numNewObjectIdentifiersSeqIndex;
    private final int newObjectIdentifierSeqIndex;

    public MultiANewArrayInstruction(final ReadMethod readMethod, final int lineNumber,
            final String desc, final int dims, int numNewObjIdSeqIndex, int newObjIdSeqIndex) {
        super(readMethod, Opcodes.MULTIANEWARRAY, lineNumber);
        this.typeDesc = desc;
        this.dims = dims;
        this.numNewObjectIdentifiersSeqIndex = numNewObjIdSeqIndex;
        this.newObjectIdentifierSeqIndex = newObjIdSeqIndex;
    }

    private MultiANewArrayInstruction(final ReadMethod readMethod, final String desc,
            final int dims, final int lineNumber, final int index,
            int numNewObjIdSeqIndex, int newObjIdSeqIndex) {
        super(readMethod, Opcodes.MULTIANEWARRAY, lineNumber, index);
        this.typeDesc = desc;
        this.dims = dims;
        this.numNewObjectIdentifiersSeqIndex = numNewObjIdSeqIndex;
        this.newObjectIdentifierSeqIndex = newObjIdSeqIndex;
    }

    public int getDimension() {
        return this.dims;
    }

    public String getTypeDesc() {
        return this.typeDesc;
    }

    public Type getType() {
        return Type.MULTIANEWARRAY;
    }

    @Override
    public InstructionInstance getNextInstance(TraceIterationInformationProvider infoProv,
            int stackDepth) {
        int numNewObjects = infoProv.getNextInteger(this.numNewObjectIdentifiersSeqIndex);
        long[] newObjects = new long[numNewObjects];
        for (int i = 0; i < numNewObjects; ++i)
            newObjects[i] = infoProv.getNextLong(this.newObjectIdentifierSeqIndex);
        return new MultiANewArrayInstrInstance(this, infoProv.getNextInstructionOccurenceNumber(getIndex()),
            stackDepth, newObjects);
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        stringCache.writeString(this.typeDesc, out);
        OptimizedDataOutputStream.writeInt0(this.dims, out);
        OptimizedDataOutputStream.writeInt0(this.numNewObjectIdentifiersSeqIndex, out);
        OptimizedDataOutputStream.writeInt0(this.newObjectIdentifierSeqIndex, out);
    }

    public static MultiANewArrayInstruction readFrom(final DataInputStream in, final MethodReadInformation methodInfo,
            final StringCacheInput stringCache,
            @SuppressWarnings("unused") final int opcode,
            final int index, final int lineNumber) throws IOException {
        final String typeDesc = stringCache.readString(in);
        final int dims = OptimizedDataInputStream.readInt0(in);
        int numNewObjIdSeqIndex = OptimizedDataInputStream.readInt0(in);
        int newObjIdSeqIndex = OptimizedDataInputStream.readInt0(in);
        return new MultiANewArrayInstruction(methodInfo.getMethod(), typeDesc, dims, lineNumber, index, numNewObjIdSeqIndex, newObjIdSeqIndex);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(19+this.typeDesc.length());
        sb.append("MULTIANEWARRAY ").append(this.typeDesc).append(' ').append(this.dims);
        return sb.toString();
    }

}
