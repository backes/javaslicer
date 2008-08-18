package de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod.MethodReadInformation;

/**
 * Class representing an IINC instruction.
 *
 * @author Clemens Hammacher
 */
public class IIncInstruction extends AbstractInstruction {

    private final int localVarIndex;

    public IIncInstruction(final ReadMethod readMethod, final int localVarIndex) {
        super(readMethod, Opcodes.IINC);
        this.localVarIndex = localVarIndex;
    }

    private IIncInstruction(final ReadMethod readMethod, final int localVarIndex, final int lineNumber, final int index) {
        super(readMethod, Opcodes.IINC, lineNumber, index);
        this.localVarIndex = localVarIndex;
    }

    public int getLocalVarIndex() {
        return this.localVarIndex;
    }

    @Override
    public Type getType() {
        return Type.IINC;
    }

    @Override
    public void writeOut(final DataOutput out) throws IOException {
        super.writeOut(out);
        out.writeInt(this.localVarIndex);
    }

    public static IIncInstruction readFrom(final DataInput in, final MethodReadInformation methodInfo, final int opcode, final int index, final int lineNumber) throws IOException {
        final int localVarIndex = in.readInt();
        return new IIncInstruction(methodInfo.getMethod(), localVarIndex, lineNumber, index);
    }

    @Override
    public String toString() {
        return "IINC";
    }

}
