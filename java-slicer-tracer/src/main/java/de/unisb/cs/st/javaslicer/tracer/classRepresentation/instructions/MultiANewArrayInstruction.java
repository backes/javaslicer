package de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.StringCacheInput;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.StringCacheOutput;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod.MethodReadInformation;
import de.unisb.cs.st.javaslicer.tracer.util.OptimizedDataInputStream;
import de.unisb.cs.st.javaslicer.tracer.util.OptimizedDataOutputStream;

/**
 * Class representing a MULTIANEWARRAY instruction.
 *
 * @author Clemens Hammacher
 */
public class MultiANewArrayInstruction extends AbstractInstruction {

    private final String typeDesc;
    private final int dims;

    public MultiANewArrayInstruction(final ReadMethod readMethod, final int lineNumber, final String desc, final int dims) {
        super(readMethod, Opcodes.MULTIANEWARRAY, lineNumber);
        this.typeDesc = desc;
        this.dims = dims;
    }

    private MultiANewArrayInstruction(final ReadMethod readMethod, final String desc, final int dims, final int lineNumber, final int index) {
        super(readMethod, Opcodes.MULTIANEWARRAY, lineNumber, index);
        this.typeDesc = desc;
        this.dims = dims;
    }

    public int getDimension() {
        return this.dims;
    }

    public String getTypeDesc() {
        return this.typeDesc;
    }

    @Override
    public Type getType() {
        return Type.MULTIANEWARRAY;
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        stringCache.writeString(this.typeDesc, out);
        OptimizedDataOutputStream.writeInt0(this.dims, out);
    }

    public static MultiANewArrayInstruction readFrom(final DataInputStream in, final MethodReadInformation methodInfo,
            final StringCacheInput stringCache,
            @SuppressWarnings("unused") final int opcode,
            final int index, final int lineNumber) throws IOException {
        final String typeDesc = stringCache.readString(in);
        final int dims = OptimizedDataInputStream.readInt0(in);
        return new MultiANewArrayInstruction(methodInfo.getMethod(), typeDesc, dims, lineNumber, index);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(19+this.typeDesc.length());
        sb.append("MULTIANEWARRAY ").append(this.typeDesc).append(' ').append(this.dims);
        return sb.toString();
    }

}
