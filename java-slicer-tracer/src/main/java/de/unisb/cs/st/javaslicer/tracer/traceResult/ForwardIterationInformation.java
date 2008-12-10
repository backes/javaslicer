package de.unisb.cs.st.javaslicer.tracer.traceResult;

import de.hammacher.util.IntegerToLongMap;

public class ForwardIterationInformation {

    public final long instrCount;
    public final int firstInstrIndex;
    public final long[] jumpInstrNrs;
    public final int[] jumpTargets;
    public final IntegerToLongMap occurrences;
    public final byte[] stackDepthChanges;

    public ForwardIterationInformation(final long instrCount,
            final int firstInstrIndex,
            final long[] jumpInstrNrs, final int[] jumpTargets,
            final byte[] stackDepthChanges, final IntegerToLongMap occurrences) {
        this.instrCount = instrCount;
        this.firstInstrIndex = firstInstrIndex;
        this.jumpInstrNrs = jumpInstrNrs;
        this.jumpTargets = jumpTargets;
        this.stackDepthChanges = stackDepthChanges;
        this.occurrences = occurrences;
    }

}
