/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation.instructions
 *    Class:     IntPush
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/instructions/IntPush.java
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
 * Class representing an instruction that just pushes an integer onto the stack
 * (except ICONST_*, so just BIPUSH and SIPUSH).
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
	public InstructionType getType() {
        return InstructionType.INT;
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
