package de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod.MethodReadInformation;

/**
 * Class representing a local variable instruction (one of *LOAD, *STORE, RET).
 *
 * @author Clemens Hammacher
 */
public class VarInstruction extends AbstractInstruction {

    private final int localVarIndex;

    public VarInstruction(final ReadMethod readMethod, final int opcode, final int localVarIndex) {
        super(readMethod, opcode);
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

    @Override
    public void writeOut(final DataOutput out) throws IOException {
        super.writeOut(out);
        out.writeInt(this.localVarIndex);
    }

    public static VarInstruction readFrom(final DataInput in, final MethodReadInformation methodInfo, final int opcode, final int index, final int lineNumber) throws IOException {
        final int localVarIndex = in.readInt();
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
