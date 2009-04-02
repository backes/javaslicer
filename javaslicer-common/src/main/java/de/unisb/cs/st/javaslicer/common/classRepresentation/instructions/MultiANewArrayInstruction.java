package de.unisb.cs.st.javaslicer.common.classRepresentation.instructions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.StringCacheInput;
import de.hammacher.util.StringCacheOutput;
import de.hammacher.util.streams.OptimizedDataInputStream;
import de.hammacher.util.streams.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstanceFactory;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstanceInfo;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.TraceIterationInformationProvider;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod.MethodReadInformation;
import de.unisb.cs.st.javaslicer.common.exceptions.TracerException;

/**
 * Class representing a MULTIANEWARRAY instruction.
 *
 * @author Clemens Hammacher
 */
public class MultiANewArrayInstruction extends AbstractInstruction {

    public static class MultiANewArrayInstrInstanceInfo implements InstructionInstanceInfo {

        private final long[] newObjectIdentifiers;

        public MultiANewArrayInstrInstanceInfo(long[] newObjects) {
            this.newObjectIdentifiers = newObjects;
        }

        // TODO this is unsafe
        public long[] getNewObjectIdentifiers() {
            return this.newObjectIdentifiers;
        }

        @Override
        public String toString() {
            if (this.newObjectIdentifiers.length == 0)
                return "[(0)]";

            StringBuilder sb = new StringBuilder(30);
            sb.append("[(").append(this.newObjectIdentifiers.length).append(") ");
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
            return Arrays.hashCode(this.newObjectIdentifiers);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MultiANewArrayInstrInstanceInfo other =
                    (MultiANewArrayInstrInstanceInfo) obj;
            if (!Arrays.equals(this.newObjectIdentifiers, other.newObjectIdentifiers))
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

    public InstructionType getType() {
        return InstructionType.MULTIANEWARRAY;
    }

    @Override
    public InstructionInstance getNextInstance(TraceIterationInformationProvider infoProv,
            int stackDepth, long instanceNr, InstructionInstanceFactory instanceFactory)
            throws TracerException {

        int numNewObjects = infoProv.getNextInteger(this.numNewObjectIdentifiersSeqIndex);
        long[] newObjects = new long[numNewObjects];
        for (int i = 0; i < numNewObjects; ++i)
            newObjects[i] = infoProv.getNextLong(this.newObjectIdentifierSeqIndex);
        return instanceFactory.createInstructionInstance(this,
            infoProv.getNextInstructionOccurenceNumber(getIndex()), stackDepth, instanceNr,
            new MultiANewArrayInstrInstanceInfo(newObjects));
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
