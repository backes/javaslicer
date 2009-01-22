package de.unisb.cs.st.javaslicer.dependenceAnalysis;

import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.InstructionInstance;
import de.unisb.cs.st.javaslicer.variables.Variable;


/**
 * An empty Implementation of the {@link DependencesVisitor} interface.
 *
 * @author Clemens Hammacher
 */
public abstract class DependencesVisitorAdapter implements DependencesVisitor {

    public void discardPendingDataDependence(InstructionInstance from, Variable var,
            DataDependenceType type) {
        // null
    }

    public void visitControlDependence(InstructionInstance from, InstructionInstance to) {
        // null
    }

    public void visitDataDependence(InstructionInstance from, InstructionInstance to, Variable var,
            DataDependenceType type) {
        // null
    }

    public void visitInstructionExecution(InstructionInstance instance) {
        // null
    }

    public void visitPendingControlDependence(InstructionInstance from) {
        // null
    }

    public void visitPendingDataDependence(InstructionInstance from, Variable var,
            DataDependenceType type) {
        // null
    }

    public void visitMethodEntry(ReadMethod method) {
        // null
    }

    public void visitMethodLeave(ReadMethod method) {
        // null
    }

}
