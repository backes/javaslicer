package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import org.objectweb.asm.Opcodes;

public class LdcInstruction extends Instruction {

    public LdcInstruction(final ReadMethod readMethod, final int lineNumber, final Object constant) {
        super(readMethod, Opcodes.LDC, lineNumber);
        // TODO Auto-generated constructor stub
    }

}
