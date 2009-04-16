package de.unisb.cs.st.javaslicer.dependenceAnalysis;

import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.variables.Variable;


/**
 * An empty Implementation of the {@link DependencesVisitor} interface.
 *
 * @author Clemens Hammacher
 */
public abstract class DependencesVisitorAdapter<InstanceType> implements DependencesVisitor<InstanceType> {

    public void visitEnd(long numInstances) {
        // null
    }

    public void discardPendingDataDependence(InstanceType from, Variable var,
            DataDependenceType type) {
        // null
    }

    public void visitControlDependence(InstanceType from, InstanceType to) {
        // null
    }

    public void visitDataDependence(InstanceType from, InstanceType to, Variable var,
            DataDependenceType type) {
        // null
    }

    public void visitInstructionExecution(InstanceType instance) {
        // null
    }

    public void visitPendingControlDependence(InstanceType from) {
        // null
    }

    public void visitPendingDataDependence(InstanceType from, Variable var,
            DataDependenceType type) {
        // null
    }

    public void visitMethodEntry(ReadMethod method) {
        // null
    }

    public void visitMethodLeave(ReadMethod method) {
        // null
    }

    public void visitObjectCreation(long objectId,
            InstanceType instrInstance) {
        // null
    }

    public void interrupted() throws InterruptedException {
        // null
    }

}
