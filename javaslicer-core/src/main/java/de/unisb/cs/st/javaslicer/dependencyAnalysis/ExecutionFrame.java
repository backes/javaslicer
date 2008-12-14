package de.unisb.cs.st.javaslicer.dependencyAnalysis;

import java.util.HashSet;
import java.util.Set;

import de.hammacher.util.IntHolder;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;
import de.unisb.cs.st.javaslicer.variables.LocalVariable;
import de.unisb.cs.st.javaslicer.variables.StackEntry;

public class ExecutionFrame {

    public final Set<Instruction> interestingInstructions = new HashSet<Instruction>();
    public ReadMethod method;
    public final IntHolder operandStack = new IntHolder(0);
    public LabelMarker atCacheBlockStart;
    public boolean throwsException;

    public LocalVariable getLocalVariable(final int localVarIndex) {
        return new LocalVariable(this, localVarIndex);
    }

    public StackEntry getStackEntry(final int index) {
        return new StackEntry(this, index);
    }

}
