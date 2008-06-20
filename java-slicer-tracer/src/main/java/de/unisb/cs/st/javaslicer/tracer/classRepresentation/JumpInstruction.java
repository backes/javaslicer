package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.objectweb.asm.Opcodes;


public class JumpInstruction extends AbstractInstruction {

    private int labelNr;

    public JumpInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode, final LabelMarker label) {
        this(readMethod, lineNumber, opcode, label == null ? -1 : label.getLabelNr());
    }

    private JumpInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode, final int labelNr) {
        super(readMethod, opcode, lineNumber);
        this.labelNr = labelNr;
    }

    public void setLabel(final LabelMarker label) {
        this.labelNr = label.getLabelNr();
    }

    @Override
    public void writeOut(final DataOutput out) throws IOException {
        super.writeOut(out);
        out.writeInt(this.labelNr);
    }

    public static JumpInstruction readFrom(final DataInput in, final ReadMethod readMethod, final int opcode, final int index, final int lineNumber) throws IOException {
        final int labelNr = in.readInt();
        return new JumpInstruction(readMethod, lineNumber, opcode, labelNr);
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

        return condition + " L" + this.labelNr;
    }
}
