package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

public class NewArrayInstruction extends AbstractInstruction {

    private final int arrayElemType;

    public NewArrayInstruction(final ReadMethod readMethod, final int lineNumber, final int arrayElemType) {
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
    }

    public int getArrayElemType() {
        return this.arrayElemType;
    }

    @Override
    public void writeOut(final DataOutput out) throws IOException {
        super.writeOut(out);
        out.writeInt(this.arrayElemType);
    }

    public static NewArrayInstruction readFrom(final DataInput in, final ReadMethod readMethod, final int opcode, final int index, final int lineNumber) throws IOException {
        final int arrayElemType = in.readInt();
        return new NewArrayInstruction(readMethod, lineNumber, arrayElemType);
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
