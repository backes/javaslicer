/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult
 *    Class:     ForwardIterationInformation
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/ForwardIterationInformation.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
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
