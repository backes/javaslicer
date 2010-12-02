/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation.instructions
 *    Class:     VarInstruction
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/instructions/VarInstruction.java
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
 * Class representing a local variable instruction (one of *LOAD, *STORE, RET).
 *
 * @author Clemens Hammacher
 */
public class VarInstruction extends AbstractInstruction {

    private final int localVarIndex;

    public VarInstruction(final ReadMethod readMethod, final int opcode, final int lineNumber, final int localVarIndex) {
        super(readMethod, opcode, lineNumber);
        assert opcode == Opcodes.ILOAD
            || opcode == Opcodes.LLOAD
            || opcode == Opcodes.FLOAD
            || opcode == Opcodes.DLOAD
            || opcode == Opcodes.ALOAD
            || opcode == Opcodes.ISTORE
            || opcode == Opcodes.LSTORE
            || opcode == Opcodes.FSTORE
            || opcode == Opcodes.DSTORE
            || opcode == Opcodes.ASTORE
            || opcode == Opcodes.RET;
        this.localVarIndex = localVarIndex;
    }

    private VarInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode, final int localVarIndex, final int index) {
        super(readMethod, opcode, lineNumber, index);
        assert opcode == Opcodes.ILOAD
            || opcode == Opcodes.LLOAD
            || opcode == Opcodes.FLOAD
            || opcode == Opcodes.DLOAD
            || opcode == Opcodes.ALOAD
            || opcode == Opcodes.ISTORE
            || opcode == Opcodes.LSTORE
            || opcode == Opcodes.FSTORE
            || opcode == Opcodes.DSTORE
            || opcode == Opcodes.ASTORE
            || opcode == Opcodes.RET;
        this.localVarIndex = localVarIndex;
    }

    public int getLocalVarIndex() {
        return this.localVarIndex;
    }

    @Override
	public InstructionType getType() {
        return InstructionType.VAR;
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        OptimizedDataOutputStream.writeInt0(this.localVarIndex, out);
    }

    public static VarInstruction readFrom(final DataInputStream in, final MethodReadInformation methodInfo,
            @SuppressWarnings("unused") final StringCacheInput stringCache,
            final int opcode, final int index, final int lineNumber) throws IOException {
        final int localVarIndex = OptimizedDataInputStream.readInt0(in);
        return new VarInstruction(methodInfo.getMethod(), lineNumber, opcode, localVarIndex, index);
    }

    @Override
    public String toString() {
        String instruction;
        switch (getOpcode()) {
        case Opcodes.ILOAD:
            instruction = "ILOAD";
            break;
        case Opcodes.LLOAD:
            instruction = "LLOAD";
            break;
        case Opcodes.FLOAD:
            instruction = "FLOAD";
            break;
        case Opcodes.DLOAD:
            instruction = "DLOAD";
            break;
        case Opcodes.ALOAD:
            instruction = "ALOAD";
            break;
        case Opcodes.ISTORE:
            instruction = "ISTORE";
            break;
        case Opcodes.LSTORE:
            instruction = "LSTORE";
            break;
        case Opcodes.FSTORE:
            instruction = "FSTORE";
            break;
        case Opcodes.DSTORE:
            instruction = "DSTORE";
            break;
        case Opcodes.ASTORE:
            instruction = "ASTORE";
            break;
        case Opcodes.RET:
            instruction = "RET";
            break;
        default:
            instruction = "-ERROR-";
        }
        return new StringBuilder(instruction.length() + 11).append(instruction).append(' ').append(this.localVarIndex).toString();
    }

}
