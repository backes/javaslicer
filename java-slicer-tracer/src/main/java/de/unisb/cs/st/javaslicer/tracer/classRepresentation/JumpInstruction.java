package de.unisb.cs.st.javaslicer.tracer.classRepresentation;


public class JumpInstruction extends Instruction {

    private LabelMarker label;

    public JumpInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode, final LabelMarker label) {
        super(readMethod, opcode, lineNumber);
        this.label = label;
    }

    public void setLabel(final LabelMarker label) {
        this.label = label;
    }

}
