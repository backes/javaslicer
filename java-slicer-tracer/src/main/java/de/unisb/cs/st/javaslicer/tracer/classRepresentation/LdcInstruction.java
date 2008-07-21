package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod.MethodReadInformation;

public class LdcInstruction extends AbstractInstruction {

    private final Object constant;

    public LdcInstruction(final ReadMethod readMethod, final Object constant) {
        super(readMethod, Opcodes.LDC);
        assert constant instanceof Number || constant instanceof String || constant instanceof Type;
        this.constant = constant;
    }

    private LdcInstruction(final ReadMethod readMethod, final int lineNumber, final Object constant, final int index) {
        super(readMethod, Opcodes.LDC, lineNumber, index);
        assert constant instanceof Number || constant instanceof String || constant instanceof Type;
        this.constant = constant;
    }

    public Object getConstant() {
        return this.constant;
    }

    @Override
    public void writeOut(final DataOutput out) throws IOException {
        super.writeOut(out);
        if (this.constant instanceof Integer) {
            out.writeByte(0);
            out.writeInt((Integer)this.constant);
        } else if (this.constant instanceof Double) {
            out.writeByte(1);
            out.writeDouble((Double)this.constant);
        } else if (this.constant instanceof String) {
            out.writeByte(2);
            out.writeUTF((String)this.constant);
        } else if (this.constant instanceof Long) {
            out.writeByte(3);
            out.writeLong((Long)this.constant);
        } else if (this.constant instanceof Float) {
            out.writeByte(4);
            out.writeFloat((Float)this.constant);
        } else if (this.constant instanceof Type) {
            out.writeByte(5);
            out.writeUTF(((Type)this.constant).getDescriptor());
        }
    }

    public static LdcInstruction readFrom(final DataInput in, final MethodReadInformation methodInfo, final int opcode, final int index, final int lineNumber) throws IOException {
        final byte type = in.readByte();
        Object constant;
        switch (type) {
        case 0:
            constant = Integer.valueOf(in.readInt());
            break;
        case 1:
            constant = Double.valueOf(in.readDouble());
            break;
        case 2:
            constant = in.readUTF();
            break;
        case 3:
            constant = Long.valueOf(in.readLong());
            break;
        case 4:
            constant = Float.valueOf(in.readFloat());
            break;
        case 5:
            constant = Type.getType(in.readUTF());
            break;
        default:
            throw new IOException("corrupted data");
        }
        return new LdcInstruction(methodInfo.getMethod(), lineNumber, constant, index);
    }

    @Override
    public String toString() {
        return "LDC " + this.constant;
    }

}
