package de.unisb.cs.st.javaslicer.tracer;

import java.io.IOException;

public interface ThreadTracer {

    void traceInt(int value, int traceSequenceIndex);

    void traceObject(Object obj, int traceSequenceIndex);

    void traceLastInstructionIndex(int traceSequenceIndex);

    void passInstruction(int instructionIndex);

    void finish() throws IOException;

    void pauseTracing();

    void unpauseTracing();

    boolean isPaused();

    void enterMethod(int instructionIndex);

    void leaveMethod(int instructionIndex);

}
