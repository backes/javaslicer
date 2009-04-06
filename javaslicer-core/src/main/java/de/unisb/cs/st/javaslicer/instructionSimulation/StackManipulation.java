package de.unisb.cs.st.javaslicer.instructionSimulation;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import de.unisb.cs.st.javaslicer.variables.StackEntrySet;
import de.unisb.cs.st.javaslicer.variables.Variable;

public class StackManipulation<InstanceType> implements DynamicInformation {

    private final ExecutionFrame<InstanceType> frame;
    private final int read;
    private final int write;
    private final int stackOffset;
    private Collection<Variable> usedVars = null;
    private final Map<Long, Collection<Variable>> createdObjects;

    public StackManipulation(final ExecutionFrame<InstanceType> frame, final int read, final int write,
            final int stackOffset, final Map<Long, Collection<Variable>> createdObjects) {
        this.frame = frame;
        this.read = read;
        this.write = write;
        this.stackOffset = stackOffset;
        this.createdObjects = createdObjects;
    }

    public Collection<? extends Variable> getDefinedVariables() {
        if (this.write == 0)
            return Collections.emptySet();

        final Collection<Variable> definedVars;
        if (this.write == 1) {
            definedVars = Collections.singleton((Variable)this.frame.getStackEntry(this.stackOffset));
        } else {
            definedVars = new StackEntrySet<InstanceType>(this.frame, this.stackOffset, this.write);
        }
        if (this.read == this.write)
            this.usedVars = definedVars;
        return definedVars;
    }

    public Collection<? extends Variable> getUsedVariables() {
        if (this.usedVars != null)
            return this.usedVars;

        if (this.read == 0)
            this.usedVars = Collections.emptySet();
        else if (this.read == 1)
            this.usedVars = Collections.singleton((Variable)this.frame.getStackEntry(
                    this.stackOffset));
        else
            this.usedVars = new StackEntrySet<InstanceType>(this.frame, this.stackOffset, this.read);

        return this.usedVars;
    }

    public Collection<? extends Variable> getUsedVariables(final Variable definedVariable) {
        return getUsedVariables();
    }

    public boolean isCatchBlock() {
        return false;
    }

    public Map<Long, Collection<Variable>> getCreatedObjects() {
        return this.createdObjects;
    }

}
