package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.ObjectTraceSequence;

public class ArrayInstruction extends Instruction {

    public ArrayInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode,
            final ObjectTraceSequence arrayTrace, final IntegerTraceSequence indexTrace) {
        super(readMethod, opcode, lineNumber);
        // TODO Auto-generated constructor stub
    }

}
