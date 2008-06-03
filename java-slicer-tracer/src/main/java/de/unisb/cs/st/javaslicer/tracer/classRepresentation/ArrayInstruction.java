package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ArrayInstruction extends Instruction {

    // when tracing, these two fields are set
    private final int arrayTraceSeqIndex;
    private final int indexTraceSeqIndex;

    public ArrayInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode,
            final int arrayTraceSeqIndex, final int indexTraceSeqIndex) {
        super(readMethod, opcode, lineNumber);
        this.arrayTraceSeqIndex = arrayTraceSeqIndex;
        this.indexTraceSeqIndex = indexTraceSeqIndex;
    }

    @Override
    public void writeOut(final DataOutput out) throws IOException {
        super.writeOut(out);
        out.writeInt(this.arrayTraceSeqIndex);
        out.writeInt(this.indexTraceSeqIndex);
    }

    public static ArrayInstruction readFrom(final ObjectInputStream in, final ReadMethod readMethod, final int opcode, final int index, final int lineNumber) throws IOException {
        final int arrayTraceSeqIndex = in.readInt();
        final int indexTraceSeqIndex = in.readInt();
        return new ArrayInstruction(readMethod, lineNumber, opcode, arrayTraceSeqIndex, indexTraceSeqIndex);
    }

}
