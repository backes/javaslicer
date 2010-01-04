package de.unisb.cs.st.javaslicer.dependenceAnalysis;

import java.util.Collection;

import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.variables.Variable;


/**
 * An empty Implementation of the {@link DependencesVisitor} interface.
 *
 * @author Clemens Hammacher
 */
public abstract class DependencesVisitorAdapter<InstanceType> implements DependencesVisitor<InstanceType> {

    public void discardPendingDataDependence(InstanceType from, Variable var,
            DataDependenceType type) throws InterruptedException {
        // null
    }

    public void interrupted() throws InterruptedException {
        // null
    }

    public void visitControlDependence(InstanceType from, InstanceType to)
            throws InterruptedException {
        // null
    }

    public void visitDataDependence(InstanceType from, InstanceType to,
            Collection<Variable> fromVars, Variable toVar, DataDependenceType type)
            throws InterruptedException {
        // null
    }

    public void visitEnd(long numInstances) throws InterruptedException {
        // null
    }

    public void visitInstructionExecution(InstanceType instance)
            throws InterruptedException {
        // null
    }

    public void visitMethodEntry(ReadMethod method, int stackDepth)
            throws InterruptedException {
        // null
    }

    public void visitMethodLeave(ReadMethod method, int stackDepth)
            throws InterruptedException {
        // null
    }

    public void visitObjectCreation(long objectId, InstanceType instrInstance)
            throws InterruptedException {
        // null
    }

    public void visitPendingControlDependence(InstanceType from)
            throws InterruptedException {
        // null
    }

    public void visitPendingDataDependence(InstanceType from, Variable var,
            DataDependenceType type) throws InterruptedException {
        // null
    }

}
