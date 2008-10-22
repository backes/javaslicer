package de.unisb.cs.st.javaslicer;

import java.util.HashSet;
import java.util.Set;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;

public class ExecutionFrame {

    public class LocalVariable implements Variable {

        private final ExecutionFrame frame;
        private final int varIndex;

        public LocalVariable(final ExecutionFrame executionFrame, final int localVarIndex) {
            this.frame = executionFrame;
            this.varIndex = localVarIndex;
        }

    }

    public final Set<Instruction> interestingInstructions = new HashSet<Instruction>();

    public Variable getLocalVariable(final int localVarIndex) {
        return new LocalVariable(this, localVarIndex);
    }

}