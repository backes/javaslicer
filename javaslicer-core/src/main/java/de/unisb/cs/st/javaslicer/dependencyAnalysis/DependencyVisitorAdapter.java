package de.unisb.cs.st.javaslicer.dependencyAnalysis;

import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.InstructionInstance;
import de.unisb.cs.st.javaslicer.variables.Variable;


/**
 * An empty Implementation of the {@link DependencyVisitor} interface.
 *
 * @author Clemens Hammacher
 */
public abstract class DependencyVisitorAdapter implements DependencyVisitor {

    public void discardPendingDataDependency(InstructionInstance from, Variable var,
            DataDependencyType type) {
        // null
    }

    public void visitControlDependency(InstructionInstance from, InstructionInstance to) {
        // null
    }

    public void visitDataDependency(InstructionInstance from, InstructionInstance to, Variable var,
            DataDependencyType type) {
        // null
    }

    public void visitInstructionExecution(InstructionInstance instance) {
        // null
    }

    public void visitPendingControlDependency(InstructionInstance from) {
        // null
    }

    public void visitPendingDataDependency(InstructionInstance from, Variable var,
            DataDependencyType type) {
        // null
    }

    public void visitMethodEntry(ReadMethod method) {
        // null
    }

    public void visitMethodLeave(ReadMethod method) {
        // null
    }

}
