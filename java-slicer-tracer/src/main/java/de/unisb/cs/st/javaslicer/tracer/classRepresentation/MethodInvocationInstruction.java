package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

public class MethodInvocationInstruction extends AbstractInstruction {

    private final String internalClassName;
    private final String methodName;
    private final String methodDesc;

    public MethodInvocationInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode,
            final String internalClassName, final String methodName, final String methodDesc) {
        super(readMethod, opcode, lineNumber);
        this.internalClassName = internalClassName;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
    }

    public String getInternalClassName() {
        return this.internalClassName;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public String getMethodDesc() {
        return this.methodDesc;
    }

    @Override
    public String toString() {
        switch (getOpcode()) {
        case Opcodes.INVOKEVIRTUAL:
            return "INVOKEVIRTUAL";
        case Opcodes.INVOKESPECIAL:
            return "INVOKESPECIAL";
        case Opcodes.INVOKESTATIC:
            return "INVOKESTATIC";
        case Opcodes.INVOKEINTERFACE:
            return "INVOKEINTERFACE";
        default:
            assert false;
            return "--ERROR--";
        }
    }

    @Override
    public void writeOut(final DataOutput out) throws IOException {
        super.writeOut(out);
        out.writeUTF(this.internalClassName);
        out.writeUTF(this.methodDesc);
        out.writeUTF(this.methodName);
    }

    public static MethodInvocationInstruction readFrom(final DataInput in, final ReadMethod readMethod, final int opcode, final int index, final int lineNumber) throws IOException {
        final String internalClassName = in.readUTF();
        final String methodDesc = in.readUTF();
        final String methodName = in.readUTF();
        return new MethodInvocationInstruction(readMethod, lineNumber, opcode, internalClassName, methodDesc, methodName);
    }

}
