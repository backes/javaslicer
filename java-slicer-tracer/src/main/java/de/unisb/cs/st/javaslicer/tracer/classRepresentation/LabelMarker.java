package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import de.unisb.cs.st.javaslicer.tracer.traceSequences.IntegerTraceSequence;


public class LabelMarker extends Instruction {

    private final IntegerTraceSequence seq;
    private final boolean additionalLabel;

    public LabelMarker(final ReadMethod readMethod, final int lineNumber,
            final IntegerTraceSequence seq, final boolean additionalLabel) {
        super(readMethod, -1, lineNumber);
        this.seq = seq;
        this.additionalLabel = additionalLabel;
    }

}
