package de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.StringCacheInput;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.StringCacheOutput;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod.MethodReadInformation;

/**
 * Class representing an instruction that takes a type as argument (NEW, ANEWARRAY, CHECKCAST, INSTANCEOF).
 *
 * @author Clemens Hammacher
 */
public class TypeInstruction extends AbstractInstruction {

    private final String typeDesc;

    public TypeInstruction(final ReadMethod readMethod, final int opcode, final int lineNumber, final String typeDesc) {
        super(readMethod, opcode, lineNumber);
        assert opcode == Opcodes.NEW
            || opcode == Opcodes.ANEWARRAY
            || opcode == Opcodes.CHECKCAST
            || opcode == Opcodes.INSTANCEOF;
        this.typeDesc = typeDesc;
    }

    private TypeInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode, final String typeDesc, final int index) {
        super(readMethod, opcode, lineNumber, index);
        assert opcode == Opcodes.NEW
            || opcode == Opcodes.ANEWARRAY
            || opcode == Opcodes.CHECKCAST
            || opcode == Opcodes.INSTANCEOF;
        this.typeDesc = typeDesc;
    }

    public String getTypeDesc() {
        return this.typeDesc;
    }

    @Override
    public Type getType() {
        return Type.TYPE;
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        stringCache.writeString(this.typeDesc, out);
    }

    public static TypeInstruction readFrom(final DataInputStream in, final MethodReadInformation methodInfo, final StringCacheInput stringCache,
            final int opcode, final int index, final int lineNumber) throws IOException {
        final String type = stringCache.readString(in);
        return new TypeInstruction(methodInfo.getMethod(), lineNumber, opcode, type, index);
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
