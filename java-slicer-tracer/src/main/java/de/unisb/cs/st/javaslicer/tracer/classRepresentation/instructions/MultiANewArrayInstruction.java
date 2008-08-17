package de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod.MethodReadInformation;

/**
 * Class representing a MULTIANEWARRAY instruction.
 *
 * @author Clemens Hammacher
 */
public class MultiANewArrayInstruction extends AbstractInstruction {

    private final String typeDesc;
    private final int dims;

    public MultiANewArrayInstruction(final ReadMethod readMethod, final String desc, final int dims) {
        super(readMethod, Opcodes.MULTIANEWARRAY);
        this.typeDesc = desc;
        this.dims = dims;
    }

    private MultiANewArrayInstruction(final ReadMethod readMethod, final String desc, final int dims, final int lineNumber, final int index) {
        super(readMethod, Opcodes.MULTIANEWARRAY, lineNumber, index);
        this.typeDesc = desc;
        this.dims = dims;
    }

    @Override
    public void writeOut(final DataOutput out) throws IOException {
        super.writeOut(out);
        out.writeUTF(this.typeDesc);
        out.writeInt(this.dims);
    }

    public static MultiANewArrayInstruction readFrom(final DataInput in, final MethodReadInformation methodInfo, final int opcode, final int index, final int lineNumber) throws IOException {
        final String typeDesc = in.readUTF();
        final int dims = in.readInt();
        return new MultiANewArrayInstruction(methodInfo.getMethod(), typeDesc, dims, lineNumber, index);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(19+this.typeDesc.length());
        sb.append("MULTIANEWARRAY ").append(this.typeDesc).append(' ').append(this.dims);
        return sb.toString();
    }

}
