package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import org.objectweb.asm.Label;

public class LabelMarker extends AbstractInstruction implements Instruction {

    public LabelMarker(final ReadMethod readMethod, final int lineNumber, final Label label, final int index) {
        super(readMethod);
        // TODO Auto-generated constructor stub
    }

}
