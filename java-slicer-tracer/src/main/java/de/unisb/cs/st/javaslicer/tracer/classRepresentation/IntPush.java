package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod.MethodReadInformation;

public class IntPush extends AbstractInstruction {

    private final int operand;

    public IntPush(final ReadMethod readMethod, final int opcode, final int operand) {
        super(readMethod, opcode);
        assert opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH;
        this.operand = operand;
    }

    private IntPush(final ReadMethod readMethod, final int lineNumber, final int opcode, final int operand, final int index) {
        super(readMethod, opcode, lineNumber, index);
        assert opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH;
        this.operand = operand;
    }

    public int getOperand() {
        return this.operand;
    }

    @Override
    public void writeOut(final DataOutput out) throws IOException {
        super.writeOut(out);
        out.writeInt(this.operand);
    }

    public static IntPush readFrom(final DataInput in, final MethodReadInformation methodInfo, final int opcode, final int index, final int lineNumber) throws IOException {
        final int operand = in.readInt();
        return new IntPush(methodInfo.getMethod(), lineNumber, opcode, operand, index);
    }

    @Override
    public String toString() {
        return new StringBuilder(18).append(getOpcode() == Opcodes.BIPUSH ? "BIPUSH " : "SIPUSH ")
            .append(this.operand).toString();
    }

}
