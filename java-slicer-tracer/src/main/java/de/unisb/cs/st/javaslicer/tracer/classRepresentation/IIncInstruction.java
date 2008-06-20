package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

public class IIncInstruction extends AbstractInstruction {

    private final int localVarIndex;

    public IIncInstruction(final ReadMethod readMethod, final int localVarIndex, final int lineNumber) {
        super(readMethod, Opcodes.IINC, lineNumber);
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

    public static IIncInstruction readFrom(final DataInput in, final ReadMethod readMethod, final int opcode, final int index, final int lineNumber) throws IOException {
        final int localVarIndex = in.readInt();
        return new IIncInstruction(readMethod, localVarIndex, lineNumber);
    }

    @Override
    public String toString() {
        return "IINC";
    }

}
