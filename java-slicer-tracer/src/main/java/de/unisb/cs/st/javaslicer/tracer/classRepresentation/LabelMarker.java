package de.unisb.cs.st.javaslicer.tracer.classRepresentation;



public class LabelMarker extends Instruction {

    private final int traceSeqIndex;
    private final boolean additionalLabel;

    public LabelMarker(final ReadMethod readMethod, final int lineNumber,
            final int traceSeqIndex, final boolean additionalLabel) {
        super(readMethod, -1, lineNumber);
        this.traceSeqIndex = traceSeqIndex;
        this.additionalLabel = additionalLabel;
    }

}
