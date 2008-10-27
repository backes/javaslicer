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
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        OptimizedDataOutputStream.writeInt0(this.localVarIndex, out);
        OptimizedDataOutputStream.writeInt0(this.increment, out);
    }

    public static IIncInstruction readFrom(final DataInputStream in, final MethodReadInformation methodInfo,
            @SuppressWarnings("unused") final StringCacheInput stringCache,
            @SuppressWarnings("unused") final int opcode,
            final int index, final int lineNumber) throws IOException {
        final int localVarIndex = OptimizedDataInputStream.readInt0(in);
        final int increment = OptimizedDataInputStream.readInt0(in);
        return new IIncInstruction(methodInfo.getMethod(), localVarIndex, increment, lineNumber, index);
    }

    @Override
    public String toString() {
        return new StringBuilder(12).append("IINC ").append(this.localVarIndex).append(' ').append(this.increment).toString();
    }

}
