/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation.instructions
 *    Class:     LdcInstruction
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/instructions/LdcInstruction.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.common.classRepresentation.instructions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.StringCacheInput;
import de.hammacher.util.StringCacheOutput;
import de.hammacher.util.streams.OptimizedDataInputStream;
import de.hammacher.util.streams.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod.MethodReadInformation;

/**
 * Class representing an LDC instruction.
 *
 * @author Clemens Hammacher
 */
public class LdcInstruction extends AbstractInstruction {

    private final Object constant;
    private final boolean isLongObject;

    public LdcInstruction(final ReadMethod readMethod, final int lineNumber, final Object constant) {
        super(readMethod, Opcodes.LDC, lineNumber);
        assert constant instanceof Number || constant instanceof String || constant instanceof org.objectweb.asm.Type;
        this.constant = constant;
        this.isLongObject = constant instanceof Long || constant instanceof Double;
    }

    private LdcInstruction(final ReadMethod readMethod, final int lineNumber, final Object constant, final int index) {
        super(readMethod, Opcodes.LDC, lineNumber, index);
        assert constant instanceof Number || constant instanceof String || constant instanceof org.objectweb.asm.Type;
        this.constant = constant;
        this.isLongObject = constant instanceof Long || constant instanceof Double;
    }

    public Object getConstant() {
        return this.constant;
    }

    public boolean constantIsLong() {
        return this.isLongObject;
    }

    public InstructionType getType() {
        return InstructionType.LDC;
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        if (this.constant instanceof Integer) {
            out.writeByte(0);
            OptimizedDataOutputStream.writeInt0((Integer)this.constant, out);
        } else if (this.constant instanceof Double) {
            out.writeByte(1);
            out.writeDouble((Double)this.constant);
        } else if (this.constant instanceof String) {
            out.writeByte(2);
            stringCache.writeString((String)this.constant, out);
        } else if (this.constant instanceof Long) {
            out.writeByte(3);
            OptimizedDataOutputStream.writeLong0((Long)this.constant, out);
        } else if (this.constant instanceof Float) {
            out.writeByte(4);
            out.writeFloat((Float)this.constant);
        } else if (this.constant instanceof org.objectweb.asm.Type) {
            out.writeByte(5);
            stringCache.writeString(((org.objectweb.asm.Type)this.constant).getDescriptor(), out);
        } else {
            throw new InternalError("Unknown LDC constant type: " + this.constant.getClass().getName());
        }
    }

    public static LdcInstruction readFrom(final DataInputStream in, final MethodReadInformation methodInfo,
            final StringCacheInput stringCache,
            @SuppressWarnings("unused") final int opcode,
            final int index, final int lineNumber) throws IOException {
        final byte type = in.readByte();
        Object constant;
        switch (type) {
        case 0:
            constant = Integer.valueOf(OptimizedDataInputStream.readInt0(in));
            break;
        case 1:
            constant = Double.valueOf(in.readDouble());
            break;
        case 2:
            constant = stringCache.readString(in);
            break;
        case 3:
            constant = Long.valueOf(OptimizedDataInputStream.readLong0(in));
            break;
        case 4:
            constant = Float.valueOf(in.readFloat());
            break;
        case 5:
            constant = org.objectweb.asm.Type.getType(stringCache.readString(in));
            break;
        default:
            throw new IOException("corrupted data");
        }
        return new LdcInstruction(methodInfo.getMethod(), lineNumber, constant, index);
    }

    @Override
    public String toString() {
        if (this.constant instanceof String) {
            final String s = (String) this.constant;
            return new StringBuilder(6+s.length()).append("LDC \"").append(s).append('"').toString();
        }
        return "LDC " + this.constant;
    }

}
