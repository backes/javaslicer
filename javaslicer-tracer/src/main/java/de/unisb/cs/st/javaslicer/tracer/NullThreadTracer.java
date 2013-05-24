/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer
 *    Class:     NullThreadTracer
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/NullThreadTracer.java
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
package de.unisb.cs.st.javaslicer.tracer;


public class NullThreadTracer implements ThreadTracer {

    public static final NullThreadTracer instance = new NullThreadTracer();

    private NullThreadTracer() {
        // private constructor ==> singleton
    }

    @Override
	public void finish() {
        // nop
    }

    @Override
	public boolean isPaused() {
        return true;
    }

    @Override
	public void passInstruction(final int instructionIndex) {
        // nop
    }

    @Override
	public void pauseTracing() {
        // nop
    }

    @Override
	public void traceInt(final int value, final int traceSequenceIndex) {
        // nop
    }

    @Override
	public void traceLastInstructionIndex(final int traceSequenceIndex) {
        // nop
    }

    @Override
	public void traceObject(final Object obj, final int traceSequenceIndex) {
        // nop
    }

    @Override
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
