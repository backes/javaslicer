package de.unisb.cs.st.javaslicer.traceResult;


public class ForwardIterationInformation {

    public final long instrCount;
    public final int firstInstrIndex;
    public final long[] jumpInstrNrs;
    public final int[] jumps;
    public final int[] stackDepthChanges;

    public ForwardIterationInformation(long instrCount,
            int firstInstrIndex,
            long[] jumpInstrNrs, int[] jumps,
            int[] stackDepthChanges) {
        this.instrCount = instrCount;
        this.firstInstrIndex = firstInstrIndex;
        this.jumpInstrNrs = jumpInstrNrs;
        this.jumps = jumps;
        this.stackDepthChanges = stackDepthChanges;
    }

}
