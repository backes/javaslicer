package de.unisb.cs.st.javaslicer.common.classRepresentation;

public interface TraceIterationInformationProvider {

    long getNextInstructionOccurenceNumber(int instructionIndex);

    int getNextInteger(int traceSeqIndex);

    long getNextLong(int traceSeqIndex);

}
