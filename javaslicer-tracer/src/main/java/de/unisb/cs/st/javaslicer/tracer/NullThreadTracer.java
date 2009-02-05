package de.unisb.cs.st.javaslicer.tracer;


public class NullThreadTracer implements ThreadTracer {

    public static final NullThreadTracer instance = new NullThreadTracer();

    private NullThreadTracer() {
        // private constructor ==> singleton
    }

    public void finish() {
        // nop
    }

    public boolean isPaused() {
        return true;
    }

    public void passInstruction(final int instructionIndex) {
        // nop
    }

    public void pauseTracing() {
        // nop
    }

    public void traceInt(final int value, final int traceSequenceIndex) {
        // nop
    }

    public void traceLastInstructionIndex(final int traceSequenceIndex) {
        // nop
    }

    public void traceObject(final Object obj, final int traceSequenceIndex) {
        // nop
    }

    public void resumeTracing() {
        // nop
    }

    @Override
    public void enterMethod(final int instructionIndex) {
        // nop
    }

    @Override
    public void leaveMethod(final int instructionIndex) {
        // nop
    }

    @Override
    public void objectAllocated(final int instructionIndex, final int traceSequenceNr) {
        // nop
    }

    @Override
    public void objectInitialized(final Object obj) {
        // nop
    }

}
