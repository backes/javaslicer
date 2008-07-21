package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod.MethodReadInformation;
import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.traceResult.ThreadTraceResult.BackwardInstructionIterator;


public class LabelMarker extends AbstractInstruction {

    private final int traceSeqIndex;
    private final boolean additionalLabel;
    private int labelNr;

    public LabelMarker(final ReadMethod readMethod,
            final int traceSeqIndex, final boolean additionalLabel) {
        super(readMethod, -1);
        this.traceSeqIndex = traceSeqIndex;
        this.additionalLabel = additionalLabel;
        this.labelNr = -1;
    }

    private LabelMarker(final ReadMethod readMethod, final int lineNumber,
            final int traceSeqIndex, final boolean additionalLabel, final int labelNr,
            final int index) {
        super(readMethod, -1, lineNumber, index);
        this.traceSeqIndex = traceSeqIndex;
        this.additionalLabel = additionalLabel;
        this.labelNr = labelNr;
    }

    public void setLabelNr(final int labelNr) {
        this.labelNr = labelNr;
    }

    public int getTraceSeqIndex() {
        return this.traceSeqIndex;
    }

    public boolean isAdditionalLabel() {
        return this.additionalLabel;
    }

    @Override
    public int getBackwardInstructionIndex(final BackwardInstructionIterator backwardInstructionIterator) {
        return backwardInstructionIterator.getNextInteger(this.traceSeqIndex);
    }

    @Override
    public Instance getNextInstance(final BackwardInstructionIterator backwardInstructionIterator) throws TracerException,
            EOFException {
        if (this.additionalLabel) {
            // decrease the instruction counter...
            backwardInstructionIterator.getNextInstructionOccurenceNumber(getIndex());
            return null;
        }
        return super.getNextInstance(backwardInstructionIterator);
    }

    @Override
    public void writeOut(final DataOutput out) throws IOException {
        super.writeOut(out);
        out.writeInt(this.traceSeqIndex);
        out.writeBoolean(this.additionalLabel);
        out.writeInt(this.labelNr);
    }

    public static LabelMarker readFrom(final DataInput in, final MethodReadInformation methodInfo, final int opcode, final int index, final int lineNumber) throws IOException {
        final int traceSeqIndex = in.readInt();
        final boolean additionalLabel = in.readBoolean();
        final int labelNr = in.readInt();
        return new LabelMarker(methodInfo.getMethod(), lineNumber, traceSeqIndex, additionalLabel, labelNr, index);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(this.additionalLabel ? 23 : 10);
        sb.append('L').append(this.labelNr);
        if (this.additionalLabel)
            sb.append(" (additional)");
        return sb.toString();
    }

    public int getLabelNr() {
        return this.labelNr;
    }

}
