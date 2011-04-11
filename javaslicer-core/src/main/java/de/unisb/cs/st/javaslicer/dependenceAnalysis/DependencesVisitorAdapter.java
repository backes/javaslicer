/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.dependenceAnalysis
 *    Class:     DependencesVisitorAdapter
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/dependenceAnalysis/DependencesVisitorAdapter.java
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
