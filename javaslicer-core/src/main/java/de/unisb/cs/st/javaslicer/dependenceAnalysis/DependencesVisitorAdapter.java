/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.dependenceAnalysis
 *    Class:     DependencesVisitorAdapter
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/dependenceAnalysis/DependencesVisitorAdapter.java
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

import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.variables.Variable;


/**
 * An empty Implementation of the {@link DependencesVisitor} interface.
 *
 * @author Clemens Hammacher
 */
public abstract class DependencesVisitorAdapter<InstanceType> implements DependencesVisitor<InstanceType> {

    @Override
	public void discardPendingDataDependence(InstanceType from, Variable var,
            DataDependenceType type) throws InterruptedException {
        // null
    }

    @Override
	public void interrupted() throws InterruptedException {
        // null
    }

    @Override
	public void visitControlDependence(InstanceType from, InstanceType to)
            throws InterruptedException {
        // null
    }

    @Override
	public void visitDataDependence(InstanceType from, InstanceType to,
            Collection<? extends Variable> fromVars, Variable toVar, DataDependenceType type)
            throws InterruptedException {
        // null
    }

    @Override
	public void visitEnd(long numInstances) throws InterruptedException {
        // null
    }

    @Override
	public void visitInstructionExecution(InstanceType instance)
            throws InterruptedException {
        // null
    }

    @Override
	public void visitMethodEntry(ReadMethod method, int stackDepth)
            throws InterruptedException {
        // null
    }

    @Override
	public void visitMethodLeave(ReadMethod method, int stackDepth)
            throws InterruptedException {
        // null
    }

    @Override
	public void visitObjectCreation(long objectId, InstanceType instrInstance)
            throws InterruptedException {
        // null
    }

    @Override
	public void visitPendingControlDependence(InstanceType from)
            throws InterruptedException {
        // null
    }

    @Override
	public void visitPendingDataDependence(InstanceType from, Variable var,
            DataDependenceType type) throws InterruptedException {
        // null
    }

    @Override
	public void visitUntracedMethodCall(InstanceType instrInstance)
            throws InterruptedException {
        // null
    }

}
