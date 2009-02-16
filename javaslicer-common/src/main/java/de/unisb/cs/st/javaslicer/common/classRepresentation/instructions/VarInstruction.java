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
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod.MethodReadInformation;

/**
 * Class representing a local variable instruction (one of *LOAD, *STORE, RET).
 *
 * @author Clemens Hammacher
 */
public class VarInstruction extends AbstractInstruction {

    private final int localVarIndex;

    public VarInstruction(final ReadMethod readMethod, final int opcode, final int lineNumber, final int localVarIndex) {
        super(readMethod, opcode, lineNumber);
        assert opcode == Opcodes.ILOAD
            || opcode == Opcodes.LLOAD
            || opcode == Opcodes.FLOAD
            || opcode == Opcodes.DLOAD
            || opcode == Opcodes.ALOAD
            || opcode == Opcodes.ISTORE
            || opcode == Opcodes.LSTORE
            || opcode == Opcodes.FSTORE
            || opcode == Opcodes.DSTORE
            || opcode == Opcodes.ASTORE
            || opcode == Opcodes.RET;
        this.localVarIndex = localVarIndex;
    }

    private VarInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode, final int localVarIndex, final int index) {
        super(readMethod, opcode, lineNumber, index);
        assert opcode == Opcodes.ILOAD
            || opcode == Opcodes.LLOAD
            || opcode == Opcodes.FLOAD
            || opcode == Opcodes.DLOAD
            || opcode == Opcodes.ALOAD
            || opcode == Opcodes.ISTORE
            || opcode == Opcodes.LSTORE
            || opcode == Opcodes.FSTORE
            || opcode == Opcodes.DSTORE
            || opcode == Opcodes.ASTORE
            || opcode == Opcodes.RET;
        this.localVarIndex = localVarIndex;
    }

    public int getLocalVarIndex() {
        return this.localVarIndex;
    }

    public Type getType() {
        return Type.VAR;
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        OptimizedDataOutputStream.writeInt0(this.localVarIndex, out);
    }

    public static VarInstruction readFrom(final DataInputStream in, final MethodReadInformation methodInfo,
            @SuppressWarnings("unused") final StringCacheInput stringCache,
            final int opcode, final int index, final int lineNumber) throws IOException {
        final int localVarIndex = OptimizedDataInputStream.readInt0(in);
        return new VarInstruction(methodInfo.getMethod(), lineNumber, opcode, localVarIndex, index);
    }

    @Override
    public String toString() {
        String instruction;
        switch (getOpcode()) {
        case Opcodes.ILOAD:
            instruction = "ILOAD";
            break;
        case Opcodes.LLOAD:
            instruction = "LLOAD";
            break;
        case Opcodes.FLOAD:
            instruction = "FLOAD";
            break;
        case Opcodes.DLOAD:
            instruction = "DLOAD";
            break;
        case Opcodes.ALOAD:
            instruction = "ALOAD";
            break;
        case Opcodes.ISTORE:
            instruction = "ISTORE";
            break;
        case Opcodes.LSTORE:
            instruction = "LSTORE";
            break;
        case Opcodes.FSTORE:
            instruction = "FSTORE";
            break;
        case Opcodes.DSTORE:
            instruction = "DSTORE";
            break;
        case Opcodes.ASTORE:
            instruction = "ASTORE";
            break;
        case Opcodes.RET:
            instruction = "RET";
            break;
        default:
            instruction = "-ERROR-";
        }
        return new StringBuilder(instruction.length() + 11).append(instruction).append(' ').append(this.localVarIndex).toString();
    }

}
