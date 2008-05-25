package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import org.objectweb.asm.Opcodes;

public class IIncInstruction extends Instruction {

    public IIncInstruction(final ReadMethod readMethod, final int var, final int lineNumber) {
        super(readMethod, Opcodes.IINC, lineNumber);
        // TODO Auto-generated constructor stub
    }

}
