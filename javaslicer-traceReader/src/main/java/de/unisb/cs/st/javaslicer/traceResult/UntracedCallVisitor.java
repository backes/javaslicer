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
