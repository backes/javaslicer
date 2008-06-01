package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import de.unisb.cs.st.javaslicer.tracer.traceResult.ConstantIntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceResult.ConstantLongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceResult.ConstantTraceSequence;

public class ArrayInstruction extends Instruction {

    // when tracing, these two fields are set
    private final int arrayTraceSeqIndex;
    private final int indexTraceSeqIndex;

    // after reloading the trace, these two fields are set
    private final ConstantLongTraceSequence constArrayTrace;
    private final ConstantIntegerTraceSequence constIndexTrace;


    public ArrayInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode,
            final int arrayTraceSeqIndex, final int indexTraceSeqIndex) {
        super(readMethod, opcode, lineNumber);
        this.arrayTraceSeqIndex = arrayTraceSeqIndex;
        this.indexTraceSeqIndex = indexTraceSeqIndex;
        this.constArrayTrace = null;
        this.constIndexTrace = null;
    }

    public ArrayInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode, final int index,
            final ConstantLongTraceSequence constArrayTrace, final ConstantIntegerTraceSequence constIndexTrace) {
        super(readMethod, opcode, lineNumber, index);
        this.constArrayTrace = constArrayTrace;
        this.constIndexTrace = constIndexTrace;
        this.arrayTraceSeqIndex = -1;
        this.indexTraceSeqIndex = -1;
    }

    @Override
    public void writeOut(final ObjectOutputStream out) throws IOException {
        assert this.arrayTraceSeqIndex != -1 && this.indexTraceSeqIndex != -1;
        assert this.constArrayTrace == null && this.constIndexTrace == null;

        super.writeOut(out);
        out.writeInt(this.arrayTraceSeqIndex);
        out.writeInt(this.indexTraceSeqIndex);
    }

    public static ArrayInstruction readFrom(final ObjectInputStream in, final ReadMethod readMethod, final int opcode, final int index, final int lineNumber) throws IOException {
        final ConstantLongTraceSequence constArrayTrace = (ConstantLongTraceSequence) ConstantTraceSequence.readFrom(in);
        final ConstantIntegerTraceSequence constIndexTrace = (ConstantIntegerTraceSequence) ConstantTraceSequence.readFrom(in);
        return new ArrayInstruction(readMethod, lineNumber, opcode, index, constArrayTrace, constIndexTrace);
    }

}
