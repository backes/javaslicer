package de.unisb.cs.st.javaslicer.instructionSimulation;

import java.util.AbstractList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import de.hammacher.util.IntHolder;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.variables.LocalVariable;
import de.unisb.cs.st.javaslicer.variables.StackEntry;
import de.unisb.cs.st.javaslicer.variables.Variable;

public class ExecutionFrame<InstanceType> {


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

    public Set<InstanceType> interestingInstances = null;
    public Set<Instruction> interestingInstructions = null;
    public ReadMethod method;
    public final IntHolder operandStack = new IntHolder(0);

    /**
     * <code>true</code> if this frame is at the start of a catchblock
     */
    public InstanceType atCatchBlockStart;

    /**
     * <code>true</code> if the next visited instruction in this frame must
     * have thrown an exception
     */
    public boolean throwsException;

    /**
     * <code>true</code> if this frame was aborted abnormally (NOT by a RETURN
     * instruction), or catched an exception. In both cases, the control flow
     * was interrupted, so the stack entry indexes cannot be computed precisely
     * any more.
     */
    public boolean interruptedControlFlow = false;

    /**
     * <code>true</code> iff this frame was aborted abnormally (NOT by a RETURN
     * instruction)
     */
    public boolean abnormalTermination = false;

    /**
     * holds the stack entry whose value was used as the return value of
     * the method
     */
    public StackEntry<InstanceType> returnValue = null;

    /**
     * is set to true if the methods entry label has been passed
     */
    public boolean finished = false;

    public Instruction lastInstruction = null;

    int maxStackEntry = -1;
    int maxLocalVariable = -1;
    int minStackEntry = 0;

    public LocalVariable<InstanceType> getLocalVariable(final int localVarIndex) {
        assert localVarIndex >= 0 || this.interruptedControlFlow;
        int index = Math.max(0, localVarIndex);
        this.maxLocalVariable = Math.max(this.maxLocalVariable, index);
        return new LocalVariable<InstanceType>(this, index);
    }

    public StackEntry<InstanceType> getStackEntry(final int index) {
        if (index < 0) {
            assert this.interruptedControlFlow;
            this.minStackEntry = Math.min(this.minStackEntry, index);
            this.maxStackEntry = Math.max(this.maxStackEntry, 0);
            return new StackEntry<InstanceType>(this, 0);
        }
        this.maxStackEntry = Math.max(this.maxStackEntry, index);
        return new StackEntry<InstanceType>(this, index);
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
        ExecutionFrame<?> other = (ExecutionFrame<?>) obj;
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
