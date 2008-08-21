package de.unisb.cs.st.javaslicer.tracer;

import java.io.IOException;

public interface ThreadTracer {

    void traceInt(final int value, final int traceSequenceIndex);

    void traceObject(final Object obj, final int traceSequenceIndex);

    void traceLastInstructionIndex(final int traceSequenceIndex);

    void passInstruction(final int instructionIndex);

    void finish() throws IOException;

    void pauseTracing();

    void unpauseTracing();

    boolean isPaused();

}
