/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation.instructions
 *    Class:     IIncInstruction
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/instructions/IIncInstruction.java
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
	public InstructionType getType() {
        return InstructionType.IINC;
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
