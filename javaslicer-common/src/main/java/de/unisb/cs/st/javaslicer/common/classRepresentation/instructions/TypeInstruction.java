package de.unisb.cs.st.javaslicer.common.classRepresentation.instructions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.StringCacheInput;
import de.hammacher.util.StringCacheOutput;
import de.hammacher.util.streams.OptimizedDataInputStream;
import de.hammacher.util.streams.OptimizedDataOutputStream;
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

        public TypeInstrInstance(TypeInstruction instr,
                long occurenceNumber, int stackDepth, long newObjectIdentifier) {
            super(instr, occurenceNumber, stackDepth);
            this.newObjectIdentifier = newObjectIdentifier;
        }

        @Override
        public TypeInstruction getInstruction() {
            return (TypeInstruction) super.getInstruction();
        }

        public long getNewObjectIdentifier() {
            return this.newObjectIdentifier;
        }

        @Override
        public String toString() {
            String s = super.toString();
            if (this.newObjectIdentifier == 0)
                return s;
            return new StringBuilder(s.length() + 10).append(s).append(" [").
                append(this.newObjectIdentifier).append(']').toString();
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

    private final String className;
    private String javaClassName = null;
    private final int newObjectIdentifierSeqIndex;

    public TypeInstruction(final ReadMethod readMethod, final int opcode, final int lineNumber,
            final String className, int newObjIdSeqIndex) {
        super(readMethod, opcode, lineNumber);
        assert opcode == Opcodes.NEW
            || opcode == Opcodes.ANEWARRAY
            || opcode == Opcodes.CHECKCAST
            || opcode == Opcodes.INSTANCEOF;
        assert ((opcode == Opcodes.CHECKCAST || opcode == Opcodes.INSTANCEOF) && newObjIdSeqIndex == 0)
            || ((opcode == Opcodes.NEW || opcode == Opcodes.ANEWARRAY) && newObjIdSeqIndex != 0);
        this.className = className;
        this.newObjectIdentifierSeqIndex = newObjIdSeqIndex;
    }

    private TypeInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode, final String typeDesc, final int index, int newObjIdSeqIndex) {
        super(readMethod, opcode, lineNumber, index);
        assert opcode == Opcodes.NEW
            || opcode == Opcodes.ANEWARRAY
            || opcode == Opcodes.CHECKCAST
            || opcode == Opcodes.INSTANCEOF;
        assert ((opcode == Opcodes.CHECKCAST || opcode == Opcodes.INSTANCEOF) && newObjIdSeqIndex == 0)
            || ((opcode == Opcodes.NEW || opcode == Opcodes.ANEWARRAY) && newObjIdSeqIndex != 0);
        this.className = typeDesc;
        this.newObjectIdentifierSeqIndex = newObjIdSeqIndex;
    }

    /**
     * Returns the argument to this TypeInstruction, as internal name as used in the bytecode
     * (i.e. &quot;java/lang/Object&quot;).
     *
     * @return the argument to this TypeInstruction
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * Returns the argument to this TypeInstruction, as java class name
     * (i.e. &quot;java.lang.Object&quot;).
     *
     * @return the argument to this TypeInstruction as java class name
     */
    public String getJavaClassName() {
        if (this.javaClassName == null)
            this.javaClassName = org.objectweb.asm.Type.getObjectType(this.className).getClassName();
        return this.javaClassName;
    }

    public Type getType() {
        return Type.TYPE;
    }

    @Override
    public InstructionInstance getNextInstance(TraceIterationInformationProvider infoProv,
            int stackDepth) {
        long newObjectIdentifier = this.newObjectIdentifierSeqIndex == 0 ? 0
            : infoProv.getNextLong(this.newObjectIdentifierSeqIndex);
        return new TypeInstrInstance(this, infoProv.getNextInstructionOccurenceNumber(getIndex()), stackDepth, newObjectIdentifier);
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        stringCache.writeString(this.className, out);
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
        return new StringBuilder(instruction.length() + this.className.length() + 1).append(instruction).append(' ').append(this.className).toString();
    }

}
