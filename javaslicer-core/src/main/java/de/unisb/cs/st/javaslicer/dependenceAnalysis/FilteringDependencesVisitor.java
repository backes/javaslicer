/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.dependenceAnalysis
 *    Class:     FilteringDependencesVisitor
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/dependenceAnalysis/FilteringDependencesVisitor.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.dependenceAnalysis;

import java.util.Collection;

import de.hammacher.util.Filter;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.variables.Variable;


public class FilteringDependencesVisitor<InstanceType> implements
        DependencesVisitor<InstanceType> {

    private final Filter<? super InstanceType> filter;
    private final DependencesVisitor<InstanceType> visitor;


    public FilteringDependencesVisitor(Filter<? super InstanceType> filter,
            DependencesVisitor<InstanceType> visitor) {
        this.filter = filter;
        this.visitor = visitor;
    }

    public void discardPendingDataDependence(InstanceType from,
            Variable var, DataDependenceType type) throws InterruptedException {
        if (this.filter.filter(from))
            this.visitor.discardPendingDataDependence(from, var, type);
    }

    public void visitControlDependence(InstanceType from, InstanceType to) throws InterruptedException {
        if (this.filter.filter(from) && this.filter.filter(to))
            this.visitor.visitControlDependence(from, to);
    }

    public void visitDataDependence(InstanceType from, InstanceType to,
            Collection<? extends Variable> fromVars, Variable toVar, DataDependenceType type) throws InterruptedException {
        if (this.filter.filter(from) && this.filter.filter(to))
            this.visitor.visitDataDependence(from, to, fromVars, toVar, type);
    }

    public void visitEnd(long numInstances) throws InterruptedException {
        this.visitor.visitEnd(numInstances);
    }

    public void visitInstructionExecution(InstanceType instance) throws InterruptedException {
        if (this.filter.filter(instance))
            this.visitor.visitInstructionExecution(instance);
    }

    public void visitMethodEntry(ReadMethod method, int stackDepth)
            throws InterruptedException {
        this.visitor.visitMethodEntry(method, stackDepth);
    }

    public void visitMethodLeave(ReadMethod method, int stackDepth)
            throws InterruptedException {
        this.visitor.visitMethodLeave(method, stackDepth);
    }

    public void visitObjectCreation(long objectId, InstanceType instrInstance) throws InterruptedException {
        if (this.filter.filter(instrInstance))
            this.visitor.visitObjectCreation(objectId, instrInstance);
    }

    public void visitPendingControlDependence(InstanceType from) throws InterruptedException {
        if (this.filter.filter(from))
            this.visitor.visitPendingControlDependence(from);
    }

    public void visitPendingDataDependence(InstanceType from, Variable var,
            DataDependenceType type) throws InterruptedException {
        if (this.filter.filter(from))
            this.visitor.visitPendingDataDependence(from, var, type);
    }

    public void visitUntracedMethodCall(InstanceType instrInstance) throws InterruptedException {
        if (this.filter.filter(instrInstance))
            this.visitor.visitUntracedMethodCall(instrInstance);
    }

    public void interrupted() throws InterruptedException {
        this.visitor.interrupted();
    }

}
