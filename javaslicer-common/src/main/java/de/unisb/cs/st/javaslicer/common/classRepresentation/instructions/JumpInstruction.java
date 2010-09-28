/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation.instructions
 *    Class:     JumpInstruction
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/instructions/JumpInstruction.java
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
 * Class representing a conditional or unconditional jump instructions (one of
 * IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE,
 * IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE, GOTO, JSR, IFNULL and IFNONNULL).
 *
 * @author Clemens Hammacher
 */
public class JumpInstruction extends AbstractInstruction {

    private LabelMarker label;

    public JumpInstruction(final ReadMethod readMethod, final int opcode, final int lineNumber,
            final LabelMarker label) {
        super(readMethod, opcode, lineNumber);
        this.label = label;
    }

    private JumpInstruction(final ReadMethod readMethod, final int lineNumber,
            final int opcode, final LabelMarker label, final int index) {
        super(readMethod, opcode, lineNumber, index);
        this.label = label;
    }

    public LabelMarker getLabel() {
        return this.label;
    }

    public void setLabel(final LabelMarker label) {
        this.label = label;
    }

    public InstructionType getType() {
        return InstructionType.JUMP;
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        OptimizedDataOutputStream.writeInt0(this.label.getLabelNr(), out);
    }

    public static JumpInstruction readFrom(final DataInputStream in, final MethodReadInformation methodInfo,
            @SuppressWarnings("unused") final StringCacheInput stringCache,
            final int opcode, final int index, final int lineNumber) throws IOException {
        final int labelNr = OptimizedDataInputStream.readInt0(in);
        return new JumpInstruction(methodInfo.getMethod(), lineNumber, opcode, methodInfo.getLabel(labelNr), index);
    }

    @Override
    public String toString() {
        String condition;
        switch (getOpcode()) {
        case Opcodes.IFEQ:
            condition = "IFEQ";
            break;
        case Opcodes.IFNE:
            condition = "IFNE";
            break;
        case Opcodes.IFLT:
            condition = "IFLT";
            break;
        case Opcodes.IFGE:
            condition = "IFGE";
            break;
        case Opcodes.IFGT:
            condition = "IFGT";
            break;
        case Opcodes.IFLE:
            condition = "IFLE";
            break;
        case Opcodes.IF_ICMPEQ:
            condition = "IF_ICMPEQ";
            break;
        case Opcodes.IF_ICMPNE:
            condition = "IF_ICMPNE";
            break;
        case Opcodes.IF_ICMPLT:
            condition = "IF_ICMPLT";
            break;
        case Opcodes.IF_ICMPGE:
            condition = "IF_ICMPGE";
            break;
        case Opcodes.IF_ICMPGT:
            condition = "IF_ICMPGT";
            break;
        case Opcodes.IF_ICMPLE:
            condition = "IF_ICMPLE";
            break;
        case Opcodes.IF_ACMPEQ:
            condition = "IF_ACMPEQ";
            break;
        case Opcodes.IF_ACMPNE:
            condition = "IF_ACMPNE";
            break;
        case Opcodes.GOTO:
            condition = "GOTO";
            break;
        case Opcodes.JSR:
            condition = "JSR";
            break;
        case Opcodes.IFNULL:
            condition = "IFNULL";
            break;
        case Opcodes.IFNONNULL:
            condition = "IFNONNULL";
            break;
        default:
            assert false;
            condition = "--ERROR--";
            break;
        }

        return condition + " " + this.label;
    }
}
