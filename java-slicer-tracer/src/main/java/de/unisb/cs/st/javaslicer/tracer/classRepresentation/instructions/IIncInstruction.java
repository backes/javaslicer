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
    private final int increment;

    public IIncInstruction(final ReadMethod readMethod, final int localVarIndex, final int increment, final int lineNumber) {
        super(readMethod, Opcodes.IINC, lineNumber);
        this.localVarIndex = localVarIndex;
        this.increment = increment;
    }

    private IIncInstruction(final ReadMethod readMethod, final int localVarIndex, final int increment, final int lineNumber, final int index) {
        super(readMethod, Opcodes.IINC, lineNumber, index);
        this.localVarIndex = localVarIndex;
        this.increment = increment;
    }

    public int getLocalVarIndex() {
        return this.localVarIndex;
    }

    public int getIncrement() {
        return this.increment;
    }

    @Override
    public Type getType() {
        return Type.IINC;
    }

    @Override
    public void writeOut(final DataOutput out) throws IOException {
        super.writeOut(out);
        out.writeInt(this.localVarIndex);
        out.writeInt(this.increment);
    }

    public static IIncInstruction readFrom(final DataInput in, final MethodReadInformation methodInfo,
            @SuppressWarnings("unused") final int opcode,
            final int index, final int lineNumber) throws IOException {
        final int localVarIndex = in.readInt();
        final int increment = in.readInt();
        return new IIncInstruction(methodInfo.getMethod(), localVarIndex, increment, lineNumber, index);
    }

    @Override
    public String toString() {
        return new StringBuilder(12).append("IINC ").append(this.localVarIndex).append(' ').append(this.increment).toString();
    }

}
