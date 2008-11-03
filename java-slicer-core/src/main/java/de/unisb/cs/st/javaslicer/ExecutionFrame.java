package de.unisb.cs.st.javaslicer;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions.LabelMarker;

public class ExecutionFrame {

    public final Set<Instruction> interestingInstructions = new HashSet<Instruction>();
    public ReadMethod method;
    public final AtomicInteger operandStack = new AtomicInteger(0);
    public LabelMarker atCacheBlockStart;
    public boolean throwsException;

    public LocalVariable getLocalVariable(final int localVarIndex) {
        return new LocalVariable(this, localVarIndex);
    }

    public StackEntry getStackEntry(final int index) {
        return new StackEntry(this, index);
    }

}
