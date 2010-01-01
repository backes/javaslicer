package de.unisb.cs.st.javaslicer.common.classRepresentation;

import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;

public interface TraceIterator {

    long getNextInstructionOccurenceNumber(int instructionIndex);

    int getNextInteger(int traceSeqIndex);

    long getNextLong(int traceSeqIndex);

    /**
     * Callback method for {@link LabelMarker} to tell the iterator that a label has
     * been crossed. This is used for progress monitoring. The total number
     * of crossed labels is known a priori.
     */
	void incNumCrossedLabels();

	double getPercentageDone();

}
