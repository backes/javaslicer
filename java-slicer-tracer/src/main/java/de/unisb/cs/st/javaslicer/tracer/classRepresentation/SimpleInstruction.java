package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.IOException;

public class SimpleInstruction extends AbstractInstruction {

    public SimpleInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode) {
        super(readMethod, opcode, lineNumber);
    }

    public static SimpleInstruction readFrom(final DataInput in, final ReadMethod readMethod, final int opcode, final int index, final int lineNumber) throws IOException {
        return new SimpleInstruction(readMethod, lineNumber, opcode);
    }

}
