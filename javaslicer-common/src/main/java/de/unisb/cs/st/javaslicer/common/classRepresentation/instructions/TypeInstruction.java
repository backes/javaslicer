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
 * Class representing an instruction that takes a type as argument (NEW, ANEWARRAY, CHECKCAST, INSTANCEOF).
 *
 * @author Clemens Hammacher
 */
public class TypeInstruction extends AbstractInstruction {

    public static class TypeInstrInstance extends AbstractInstance {

        private final long newObjectIdentifier;

        public TypeInstrInstance(AbstractInstruction instr,
                long occurenceNumber, int stackDepth, long newObjectIdentifier) {
            super(instr, occurenceNumber, stackDepth);
            this.newObjectIdentifier = newObjectIdentifier;
        }

        public long getNewObjectIdentifier() {
            return this.newObjectIdentifier;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result
                + (int)this.newObjectIdentifier;
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
            TypeInstrInstance other = (TypeInstrInstance) obj;
            if (this.newObjectIdentifier != other.newObjectIdentifier)
                return false;
            return true;
        }

    }

    private final String typeDesc;
    private final int newObjectIdentifierSeqIndex;

    public TypeInstruction(final ReadMethod readMethod, final int opcode, final int lineNumber, final String typeDesc, int newObjIdSeqIndex) {
        super(readMethod, opcode, lineNumber);
        assert opcode == Opcodes.NEW
            || opcode == Opcodes.ANEWARRAY
            || opcode == Opcodes.CHECKCAST
            || opcode == Opcodes.INSTANCEOF;
        assert opcode == Opcodes.CHECKCAST || opcode == Opcodes.INSTANCEOF || newObjIdSeqIndex == 0;
        this.typeDesc = typeDesc;
        this.newObjectIdentifierSeqIndex = newObjIdSeqIndex;
    }

    private TypeInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode, final String typeDesc, final int index, int newObjIdSeqIndex) {
        super(readMethod, opcode, lineNumber, index);
        assert opcode == Opcodes.NEW
            || opcode == Opcodes.ANEWARRAY
            || opcode == Opcodes.CHECKCAST
            || opcode == Opcodes.INSTANCEOF;
        assert opcode == Opcodes.NEW || opcode == Opcodes.ANEWARRAY || newObjIdSeqIndex == 0;
        this.typeDesc = typeDesc;
        this.newObjectIdentifierSeqIndex = newObjIdSeqIndex;
    }

    public String getTypeDesc() {
        return this.typeDesc;
    }

    public Type getType() {
        return Type.TYPE;
    }

    @Override
    public Instance getNextInstance(TraceIterationInformationProvider infoProv,
            int stackDepth) {
        long newObjectIdentifier = getOpcode() == Opcodes.NEW || getOpcode() == Opcodes.ANEWARRAY
            ? infoProv.getNextLong(this.newObjectIdentifierSeqIndex) : 0;
        return new TypeInstrInstance(this, infoProv.getNextInstructionOccurenceNumber(getIndex()), stackDepth, newObjectIdentifier);
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        stringCache.writeString(this.typeDesc, out);
        if (getOpcode() == Opcodes.NEW || getOpcode() == Opcodes.ANEWARRAY)
            OptimizedDataOutputStream.writeInt0(this.newObjectIdentifierSeqIndex, out);
    }

    public static TypeInstruction readFrom(final DataInputStream in, final MethodReadInformation methodInfo, final StringCacheInput stringCache,
            final int opcode, final int index, final int lineNumber) throws IOException {
        final String type = stringCache.readString(in);
        int newObjIdSeqIndex = opcode == Opcodes.NEW || opcode == Opcodes.ANEWARRAY
            ? OptimizedDataInputStream.readInt0(in) : 0;
        return new TypeInstruction(methodInfo.getMethod(), lineNumber, opcode, type, index, newObjIdSeqIndex);
    }

    @Override
    public String toString() {
        String instruction;
        switch (getOpcode()) {
        case Opcodes.NEW:
            instruction = "NEW";
            break;
        case Opcodes.ANEWARRAY:
            instruction = "ANEWARRAY";
            break;
        case Opcodes.CHECKCAST:
            instruction = "CHECKCAST";
            break;
        case Opcodes.INSTANCEOF:
            instruction = "INSTANCEOF";
            break;
        default:
            instruction = "-ERROR-";
        }
        return new StringBuilder(instruction.length() + this.typeDesc.length() + 1).append(instruction).append(' ').append(this.typeDesc).toString();
    }

}
