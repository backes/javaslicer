package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.IOException;


public class JumpInstruction extends AbstractInstruction {

    private LabelMarker label;

    public JumpInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode, final LabelMarker label) {
        super(readMethod, opcode, lineNumber);
        this.label = label;
    }

    public void setLabel(final LabelMarker label) {
        this.label = label;
    }

    public static JumpInstruction readFrom(final DataInput in, final ReadMethod readMethod, final int opcode, final int index, final int lineNumber) throws IOException {
        return new JumpInstruction(readMethod, lineNumber, opcode, null);
    }

}
