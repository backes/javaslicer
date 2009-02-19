package de.unisb.cs.st.javaslicer.instructionSimulation;

import java.util.AbstractList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import de.hammacher.util.IntHolder;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.InstructionInstance;
import de.unisb.cs.st.javaslicer.variables.LocalVariable;
import de.unisb.cs.st.javaslicer.variables.StackEntry;
import de.unisb.cs.st.javaslicer.variables.Variable;

public class ExecutionFrame {


    public class FrameVariableList extends AbstractList<Variable> {

        @Override
        public Variable get(int index) {
            if (index <= ExecutionFrame.this.maxLocalVariable) {
                if (index < 0)
                    throw new NoSuchElementException();
                return getLocalVariable(index);
            }
            int stackEntry = index - ExecutionFrame.this.maxLocalVariable + ExecutionFrame.this.minStackEntry - 1;
            if (stackEntry <= ExecutionFrame.this.maxStackEntry)
                return getStackEntry(stackEntry);
            throw new NoSuchElementException();
        }

        @Override
        public int size() {
            return ExecutionFrame.this.maxLocalVariable+ExecutionFrame.this.maxStackEntry-ExecutionFrame.this.minStackEntry+2;
        }

    }

    private static int nextFrameNr = 0;
    public final int frameNr = nextFrameNr++;

    public final Set<InstructionInstance> interestingInstances = new HashSet<InstructionInstance>();
    public final Set<Instruction> interestingInstructions = new HashSet<Instruction>();
    public ReadMethod method;
    public final IntHolder operandStack = new IntHolder(0);
    public InstructionInstance atCacheBlockStart;
    public boolean throwsException;
    public boolean abnormalTermination = false;
    public Variable returnValue = null;
    public boolean finished = false; // is set to true if the entry label has been passed
    public Instruction lastInstruction = null;

    int maxStackEntry = -1;
    int maxLocalVariable = -1;
    int minStackEntry = 0;

    public LocalVariable getLocalVariable(final int localVarIndex) {
        assert localVarIndex >= 0 || this.abnormalTermination;
        int index = Math.max(0, localVarIndex);
        this.maxLocalVariable = Math.max(this.maxLocalVariable, index);
        return new LocalVariable(this, index);
    }

    public StackEntry getStackEntry(final int index) {
        if (index < 0) {
            assert this.abnormalTermination;
            this.minStackEntry = Math.min(this.minStackEntry, index);
            this.maxStackEntry = Math.max(this.maxStackEntry, 0);
            return new StackEntry(this, 0);
        }
        this.maxStackEntry = Math.max(this.maxStackEntry, index);
        return new StackEntry(this, index);
    }

    public List<Variable> getAllVariables() {
        return new FrameVariableList();
    }


    @Override
    public int hashCode() {
        return this.frameNr;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExecutionFrame other = (ExecutionFrame) obj;
        if (this.frameNr != other.frameNr)
            return false;
        if (this.lastInstruction == null) {
            if (other.lastInstruction != null)
                return false;
        } else if (!this.lastInstruction.equals(other.lastInstruction))
            return false;
        if (this.method == null) {
            if (other.method != null)
                return false;
        } else if (!this.method.equals(other.method))
            return false;
        return true;
    }

    @Override
    public String toString() {
        if (this.method == null)
            return "frame "+this.frameNr+": ??";
        return "frame "+this.frameNr+": "+this.method.getReadClass().getName()+"."+this.method.getName()+this.method.getDesc();
    }

}
