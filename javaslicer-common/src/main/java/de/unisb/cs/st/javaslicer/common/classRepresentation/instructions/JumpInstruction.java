package de.unisb.cs.st.javaslicer.common.classRepresentation.instructions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.OptimizedDataInputStream;
import de.hammacher.util.OptimizedDataOutputStream;
import de.hammacher.util.StringCacheInput;
import de.hammacher.util.StringCacheOutput;
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

    public Type getType() {
        return Type.JUMP;
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
