package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.traceResult.ThreadTraceResult.BackwardInstructionIterator;


public class LabelMarker extends AbstractInstruction {

    private final int traceSeqIndex;
    private final boolean additionalLabel;

    public LabelMarker(final ReadMethod readMethod, final int lineNumber,
            final int traceSeqIndex, final boolean additionalLabel) {
        super(readMethod, -1, lineNumber);
        this.traceSeqIndex = traceSeqIndex;
        this.additionalLabel = additionalLabel;
    }

    @Override
    public int getBackwardInstructionIndex(final BackwardInstructionIterator backwardInstructionIterator) {
        return backwardInstructionIterator.getNextInteger(this.traceSeqIndex);
    }

    @Override
    public Instance getNextInstance(final BackwardInstructionIterator backwardInstructionIterator) throws TracerException,
            EOFException {
        return this.additionalLabel ? null : super.getNextInstance(backwardInstructionIterator);
    }

    @Override
    public void writeOut(final DataOutput out) throws IOException {
        super.writeOut(out);
        out.writeInt(this.traceSeqIndex);
        out.writeBoolean(this.additionalLabel);
    }

    public static LabelMarker readFrom(final DataInput in, final ReadMethod readMethod, final int opcode, final int index, final int lineNumber) throws IOException {
        final int traceSeqIndex = in.readInt();
        final boolean additionalLabel = in.readBoolean();
        return new LabelMarker(readMethod, lineNumber, traceSeqIndex, additionalLabel);
    }


}
