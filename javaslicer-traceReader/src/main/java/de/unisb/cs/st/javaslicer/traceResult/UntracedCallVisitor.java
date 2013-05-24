/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult
 *    Class:     UntracedCallVisitor
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/UntracedCallVisitor.java
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
