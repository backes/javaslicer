/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation.instructions
 *    Class:     LdcInstruction
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/instructions/LdcInstruction.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
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

    @Override
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
