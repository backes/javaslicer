package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import org.objectweb.asm.Label;

public class JumpInstruction extends Instruction {

    private final int opcode;

    public JumpInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode, final Label label) {
        super(readMethod);
        this.opcode = opcode;
    }

}
