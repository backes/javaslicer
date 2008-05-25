package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import org.objectweb.asm.Opcodes;

public class NewArrayInstruction extends Instruction {

    public NewArrayInstruction(final ReadMethod readMethod, final int lineNumber, final int operand) {
        super(readMethod, Opcodes.NEWARRAY, lineNumber);
        // TODO Auto-generated constructor stub
    }

}
