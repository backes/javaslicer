package de.unisb.cs.st.javaslicer.traceResult;


public class ForwardIterationInformation {

    public final long instrCount;
    public final int firstInstrIndex;
    public final long[] jumpInstrNrs;
    public final int[] jumps;
    public final byte[] stackDepthChanges;

    public ForwardIterationInformation(final long instrCount,
            final int firstInstrIndex,
            final long[] jumpInstrNrs, final int[] jumps,
            final byte[] stackDepthChanges) {
        this.instrCount = instrCount;
        this.firstInstrIndex = firstInstrIndex;
        this.jumpInstrNrs = jumpInstrNrs;
        this.jumps = jumps;
        this.stackDepthChanges = stackDepthChanges;
    }

}
