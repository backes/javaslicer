package de.unisb.cs.st.javaslicer.common.classRepresentation.instructions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.hammacher.util.StringCacheInput;
import de.hammacher.util.StringCacheOutput;
import de.hammacher.util.streams.OptimizedDataInputStream;
import de.hammacher.util.streams.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.TraceIterationInformationProvider;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod.MethodReadInformation;


/**
 * This is no read instruction, but a marker for jump targets.
 *
 * @author Clemens Hammacher
 */
public class LabelMarker extends AbstractInstruction {

    private final int traceSeqIndex;
    private final boolean isAdditionalLabel;
    private int labelNr;
    private final boolean isCatchBlock;

    public LabelMarker(final ReadMethod readMethod, final int traceSeqIndex,
            final int lineNumber,
            final boolean isAdditionalLabel, final boolean isCatchBlock, final int labelNr) {
        super(readMethod, -1, lineNumber);
        this.traceSeqIndex = traceSeqIndex;
        this.isAdditionalLabel = isAdditionalLabel;
        this.labelNr = labelNr;
        this.isCatchBlock = isCatchBlock;
    }

    private LabelMarker(final ReadMethod readMethod, final int lineNumber,
            final int traceSeqIndex, final boolean isAdditionalLabel, final boolean isCatchBlock,
            final int labelNr, final int index) {
        super(readMethod, -1, lineNumber, index);
        this.traceSeqIndex = traceSeqIndex;
        this.isAdditionalLabel = isAdditionalLabel;
        this.labelNr = labelNr;
        this.isCatchBlock = isCatchBlock;
    }

    public void setLabelNr(final int labelNr) {
        this.labelNr = labelNr;
    }

    public int getTraceSeqIndex() {
        return this.traceSeqIndex;
    }

    public boolean isAdditionalLabel() {
        return this.isAdditionalLabel;
    }

    public int getLabelNr() {
        return this.labelNr;
    }

    public boolean isCatchBlock() {
        return this.isCatchBlock;
    }

    public InstructionType getType() {
        return InstructionType.LABEL;
    }

    @Override
    public int getBackwardInstructionIndex(final TraceIterationInformationProvider infoProv) {
        return infoProv.getNextInteger(this.traceSeqIndex);
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        OptimizedDataOutputStream.writeInt0(this.traceSeqIndex, out);
        out.writeByte((this.isAdditionalLabel ? 2 : 0) + (this.isCatchBlock ? 1 : 0));
        OptimizedDataOutputStream.writeInt0(this.labelNr, out);
    }

    public static LabelMarker readFrom(final DataInputStream in, final MethodReadInformation methodInfo,
            @SuppressWarnings("unused") final StringCacheInput stringCache,
            @SuppressWarnings("unused") final int opcode,
            final int index, final int lineNumber) throws IOException {
        final int traceSeqIndex = OptimizedDataInputStream.readInt0(in);
        final byte booleans = in.readByte();
        final boolean isAdditionalLabel = (booleans & 2) != 0;
        final boolean isCatchBlock = (booleans & 1) != 0;
        final int labelNr = OptimizedDataInputStream.readInt0(in);
        return new LabelMarker(methodInfo.getMethod(), lineNumber, traceSeqIndex, isAdditionalLabel, isCatchBlock,
                labelNr, index);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(this.isAdditionalLabel ? 23 : 10);
        sb.append('L').append(this.labelNr);
        if (this.isAdditionalLabel)
            sb.append(" (additional)");
        return sb.toString();
    }

}
