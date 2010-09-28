/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer
 *    Class:     NullThreadTracer
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/NullThreadTracer.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
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
