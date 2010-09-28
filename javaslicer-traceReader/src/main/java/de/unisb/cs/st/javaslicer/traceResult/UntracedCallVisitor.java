/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult
 *    Class:     UntracedCallVisitor
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/UntracedCallVisitor.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.traceResult;

import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MethodInvocationInstruction;


/**
 * A Visitor which gets informed about all calls to untraced methods.
 *
 * @author Clemens Hammacher
 */
public interface UntracedCallVisitor {

    /**
     * Gets called each time a method call is encountered, but the called method was not traced.
     * This is most often the case for native methods, but there are also methods in the java api
     * that are excluded from tracing.
     *
     * @param instrInstance the instruction instance of the method call (the unterlying instruction
     *                      is a {@link MethodInvocationInstruction})
     */
    void visitUntracedMethodCall(InstructionInstance instrInstance) throws InterruptedException;

}
