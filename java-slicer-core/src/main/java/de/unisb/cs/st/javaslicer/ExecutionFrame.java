package de.unisb.cs.st.javaslicer;

import java.util.HashSet;
import java.util.Set;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;

public class ExecutionFrame {

    public static class LocalVariable implements Variable {

        private final ExecutionFrame frame;
        private final int varIndex;

        public LocalVariable(final ExecutionFrame executionFrame, final int localVarIndex) {
            this.frame = executionFrame;
            this.varIndex = localVarIndex;
        }

        public ExecutionFrame getFrame() {
            return this.frame;
        }

        public int getVarIndex() {
            return this.varIndex;
        }

        @Override
        public String toString() {
            return "local["+this.frame.hashCode()+","+this.varIndex+"]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.frame.hashCode();
            result = prime * result + this.varIndex;
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final LocalVariable other = (LocalVariable) obj;
            if (!this.frame.equals(other.frame))
                return false;
            if (this.varIndex != other.varIndex)
                return false;
            return true;
        }

    }

    public final Set<Instruction> interestingInstructions = new HashSet<Instruction>();
    public ReadMethod method;

    public LocalVariable getLocalVariable(final int localVarIndex) {
        return new LocalVariable(this, localVarIndex);
    }

}