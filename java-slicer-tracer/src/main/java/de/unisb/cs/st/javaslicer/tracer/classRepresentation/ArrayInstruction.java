package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import de.unisb.cs.st.javaslicer.tracer.traceResult.ConstantIntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceResult.ConstantLongTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceResult.ConstantTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.IntegerTraceSequence;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.ObjectTraceSequence;

public class ArrayInstruction extends Instruction {

    // when tracing, these two fields are set
    private final ObjectTraceSequence arrayTrace;
    private final IntegerTraceSequence indexTrace;

    // after reloading the trace, these two fields are set
    private final ConstantLongTraceSequence constArrayTrace;
    private final ConstantIntegerTraceSequence constIndexTrace;


    public ArrayInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode,
            final ObjectTraceSequence arrayTrace, final IntegerTraceSequence indexTrace) {
        super(readMethod, opcode, lineNumber);
        this.arrayTrace = arrayTrace;
        this.indexTrace = indexTrace;
        this.constArrayTrace = null;
        this.constIndexTrace = null;
    }

    public ArrayInstruction(final ReadMethod readMethod, final int lineNumber, final int opcode, final int index,
            final ConstantLongTraceSequence constArrayTrace, final ConstantIntegerTraceSequence constIndexTrace) {
        super(readMethod, opcode, lineNumber, index);
        this.constArrayTrace = constArrayTrace;
        this.constIndexTrace = constIndexTrace;
        this.arrayTrace = null;
        this.indexTrace = null;
    }

    @Override
    public void writeOut(final ObjectOutputStream out) throws IOException {
        assert this.arrayTrace != null && this.indexTrace != null;
        assert this.constArrayTrace == null && this.constIndexTrace == null;

        super.writeOut(out);
        this.arrayTrace.writeOut(out);
        this.indexTrace.writeOut(out);
    }

    public static ArrayInstruction readFrom(final ObjectInputStream in, final ReadMethod readMethod, final int opcode, final int index, final int lineNumber) throws IOException {
        final ConstantLongTraceSequence constArrayTrace = (ConstantLongTraceSequence) ConstantTraceSequence.readFrom(in);
        final ConstantIntegerTraceSequence constIndexTrace = (ConstantIntegerTraceSequence) ConstantTraceSequence.readFrom(in);
        return new ArrayInstruction(readMethod, lineNumber, opcode, index, constArrayTrace, constIndexTrace);
    }

}
