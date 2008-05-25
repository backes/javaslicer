package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.ObjectTraceSequence;

public class FieldInstruction extends Instruction {

    public FieldInstruction(final ReadMethod readMethod, final int opcode, final int lineNumber,
            final String owner, final String name,
            final String desc, final ObjectTraceSequence objectTrace) {
        super(readMethod, opcode, lineNumber);
        // TODO Auto-generated constructor stub
    }

}
