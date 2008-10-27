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
 * Class representing an instruction that just pushes an integer onto the stack (
 * except ICONST_*, so just BIPUSH and SIPUSH).
 *
 * @author Clemens Hammacher
 */
public class IntPush extends AbstractInstruction {

    private final int operand;

    public IntPush(final ReadMethod readMethod, final int opcode, final int operand, final int lineNumber) {
        super(readMethod, opcode, lineNumber);
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
    public Type getType() {
        return Type.INT;
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        OptimizedDataOutputStream.writeInt0(this.operand, out);
    }

    public static IntPush readFrom(final DataInputStream in, final MethodReadInformation methodInfo,
            @SuppressWarnings("unused") final StringCacheInput stringCache,
            final int opcode, final int index, final int lineNumber) throws IOException {
        final int operand = OptimizedDataInputStream.readInt0(in);
        return new IntPush(methodInfo.getMethod(), lineNumber, opcode, operand, index);
    }

    @Override
    public String toString() {
        return new StringBuilder(18).append(getOpcode() == Opcodes.BIPUSH ? "BIPUSH " : "SIPUSH ")
            .append(this.operand).toString();
    }

}
