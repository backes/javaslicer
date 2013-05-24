/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.dependenceAnalysis
 *    Class:     DependencesVisitor
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/dependenceAnalysis/DependencesVisitor.java
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
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MethodInvocationInstruction;
import de.unisb.cs.st.javaslicer.variables.Variable;


/**
 * A visitor that gets informed about determined dependences of one program
 * trace as well as some other possibly interesting information.
 *
 * @author Clemens Hammacher
 */
public interface DependencesVisitor<InstanceType> {

    /**
     * Gets called when the {@link DependencesExtractor} finished tracersing the trace.
     * @param numInstances the total number of instances that have been visited
     *                     during the tracersal of the trace
     */
    void visitEnd(long numInstances) throws InterruptedException;

    /**
     * Gets called if a (dynamic) data dependence has been determined.
     *
     * @param from the instruction that depends on another
     * @param to the &quot;target&quot; of the data dependence
     * @param fromVars the set of variables used to produce the value of <code>toVar</code>
     *        (possibly empty, e.g. for LDC or ICONST).
     *        WARNING: this parameter is only set for read after write dependencies, not for write after read dependencies (there it is <code>null</code>)!!
     * @param toVar the variable through which the dependence exists (was written by <code>to</code> and read by <code>from</code>)
     * @param type the type of the data dependence (read after write / write after read)
     */
    void visitDataDependence(InstanceType from, InstanceType to,
            Collection<? extends Variable> fromVars, Variable toVar, DataDependenceType type) throws InterruptedException;

    /**
     * Gets called if a dynamic occurence of a control dependence has been determined.
     *
     * @param from the instruction that depends on another
     * @param to the &quot;target&quot; of the control dependence, i.e. the instruction
     *           that controls the execution of <code>from</code>
     */
    void visitControlDependence(InstanceType from, InstanceType to) throws InterruptedException;

    /**
     * Gets called for every instruction in the execution trace.
     *
     * @param instance the instruction instance that has just been visited
     */
    void visitInstructionExecution(InstanceType instance) throws InterruptedException;

    /**
     * Gets called if there might be a data dependence that gets visited later.
     *
     * If the type is {@link DataDependenceType#READ_AFTER_WRITE}, then most probably
     * {@link #visitDataDependence(Object, Object, Collection, Variable, DataDependenceType)}
     * gets called exactly one (may also be that it is not called in some cases), but
     * definitively, at some later point
     * {@link #discardPendingDataDependence(Object, Variable, DataDependenceType)}
     * is called.
     *
     * If the type is {@link DataDependenceType#WRITE_AFTER_READ}, then
     * {@link #visitDataDependence(Object, Object, Collection, Variable, DataDependenceType)}
     * may get called one or several times, and finally
     * {@link #discardPendingDataDependence(Object, Variable, DataDependenceType)}
     * is called.
     *
     * @param from the instruction that depends on another
     * @param var the variable through which the dependence exists
     * @param type the type of the data dependence (read after write / write after read)
     */
    void visitPendingDataDependence(InstanceType from, Variable var, DataDependenceType type) throws InterruptedException;

    /**
     * Gets called if the instruction <code>from</code> has a control dependence that
     * is not known yet. At some later time, {@link #visitControlDependence(Object, Object)}
     * is called for that instance.
     *
     * @param from the instruction instance that has a control dependence
     */
    void visitPendingControlDependence(InstanceType from) throws InterruptedException;

    /**
     * Gets called if all data dependences on the specific variable <code>var</code>
     * have been visited for the instruction instance <code>from</code>.
     *
     * @param from the instruction that depends on another
     * @param var the variable through which the dependence exists
     * @param type the type of the data dependence (read after write / write after read)
     */
    void discardPendingDataDependence(InstanceType from, Variable var, DataDependenceType type) throws InterruptedException;

    /**
     * Gets called each time a new method in entered. If the trace is iterated backwards,
     * this means that we already processed all instructions of this method.
     *
     * @param method the method that is entered
     * @param stackDepth the stack depth of the entered method.
     */
    void visitMethodEntry(ReadMethod method, int stackDepth) throws InterruptedException;

    /**
     * Gets called each time a method is left.
     *
     * Because the trace is processed backwards, a {@link #visitMethodEntry(ReadMethod, int)} is called
     * later.
     *
     * @param method the method that is left
     * @param stackDepth the stack depth of the left method.
     */
    void visitMethodLeave(ReadMethod method, int stackDepth) throws InterruptedException;

    /**
     * Gets called each time a method call is encountered, but the called method was not traced.
     * This is most often the case for native methods, but there are also methods in the java api
     * that are excluded from tracing.
     *
     * @param instrInstance the instruction instance of the method call (the unterlying instruction
     *                      is a {@link MethodInvocationInstruction})
     */
    void visitUntracedMethodCall(InstanceType instrInstance) throws InterruptedException;

    /**
     * Gets called if an instruction (NEW, NEWARRAY, ANEWARRAY, MULTIANEWARRAY) creates an object.
     *
     * @param objectId the identity of the object which is being created
     * @param instrInstance the instruction instance which created the object
     */
    void visitObjectCreation(long objectId, InstanceType instrInstance) throws InterruptedException;

    /**
     * Gets called if the traversal of the trace was interrupted (by Thread.interrupt()).
     *
     * The visitor can do necessary clean-up work in this method.
     */
    void interrupted() throws InterruptedException;

}
