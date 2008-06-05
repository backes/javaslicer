package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

public class IntPush extends AbstractInstruction {

    private final int operand;

    public IntPush(final ReadMethod readMethod, final int lineNumber, final int opcode, final int operand) {
        super(readMethod, opcode, lineNumber);
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

    public static IntPush readFrom(final DataInput in, final ReadMethod readMethod, final int opcode, final int index, final int lineNumber) throws IOException {
        final int operand = in.readInt();
        return new IntPush(readMethod, lineNumber, opcode, operand);
    }


}
