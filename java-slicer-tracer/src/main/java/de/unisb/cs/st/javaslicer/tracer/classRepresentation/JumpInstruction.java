package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import org.objectweb.asm.Label;

public class JumpInstruction extends AbstractInstruction implements Instruction {

    private final int opcode;
    private final int traceSequenceIndex;

    public JumpInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode, final Label label, final int traceSequenceIndex) {
        super(readMethod);
        this.opcode = opcode;
        this.traceSequenceIndex = traceSequenceIndex;
    }

}
