/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult
 *    Class:     ForwardIterationInformation
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/ForwardIterationInformation.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
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
