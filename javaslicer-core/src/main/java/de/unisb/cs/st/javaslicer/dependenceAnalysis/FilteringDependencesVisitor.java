/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.dependenceAnalysis
 *    Class:     FilteringDependencesVisitor
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/dependenceAnalysis/FilteringDependencesVisitor.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
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

    @Override
	public void discardPendingDataDependence(InstanceType from,
            Variable var, DataDependenceType type) throws InterruptedException {
        if (this.filter.filter(from))
            this.visitor.discardPendingDataDependence(from, var, type);
    }

    @Override
	public void visitControlDependence(InstanceType from, InstanceType to) throws InterruptedException {
        if (this.filter.filter(from) && this.filter.filter(to))
            this.visitor.visitControlDependence(from, to);
    }

    @Override
	public void visitDataDependence(InstanceType from, InstanceType to,
            Collection<? extends Variable> fromVars, Variable toVar, DataDependenceType type) throws InterruptedException {
        if (this.filter.filter(from) && this.filter.filter(to))
            this.visitor.visitDataDependence(from, to, fromVars, toVar, type);
    }

    @Override
	public void visitEnd(long numInstances) throws InterruptedException {
        this.visitor.visitEnd(numInstances);
    }

    @Override
	public void visitInstructionExecution(InstanceType instance) throws InterruptedException {
        if (this.filter.filter(instance))
            this.visitor.visitInstructionExecution(instance);
    }

    @Override
	public void visitMethodEntry(ReadMethod method, int stackDepth)
            throws InterruptedException {
        this.visitor.visitMethodEntry(method, stackDepth);
    }

    @Override
	public void visitMethodLeave(ReadMethod method, int stackDepth)
            throws InterruptedException {
        this.visitor.visitMethodLeave(method, stackDepth);
    }

    @Override
	public void visitObjectCreation(long objectId, InstanceType instrInstance) throws InterruptedException {
        if (this.filter.filter(instrInstance))
            this.visitor.visitObjectCreation(objectId, instrInstance);
    }

    @Override
	public void visitPendingControlDependence(InstanceType from) throws InterruptedException {
        if (this.filter.filter(from))
            this.visitor.visitPendingControlDependence(from);
    }

    @Override
	public void visitPendingDataDependence(InstanceType from, Variable var,
            DataDependenceType type) throws InterruptedException {
        if (this.filter.filter(from))
            this.visitor.visitPendingDataDependence(from, var, type);
    }

    @Override
	public void visitUntracedMethodCall(InstanceType instrInstance) throws InterruptedException {
        if (this.filter.filter(instrInstance))
            this.visitor.visitUntracedMethodCall(instrInstance);
    }

    @Override
	public void interrupted() throws InterruptedException {
        this.visitor.interrupted();
    }

}
