/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.dependenceAnalysis
 *    Class:     VisitorCapability
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/dependenceAnalysis/VisitorCapability.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.dependenceAnalysis;

import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;

public enum VisitorCapability {

    /**
     * combines {@link #DATA_DEPENDENCES_READ_AFTER_WRITE} and
     * {@link #DATA_DEPENDENCES_WRITE_AFTER_READ}
     */
    DATA_DEPENDENCES_ALL,

    /**
     * enables invokations of
     * {@link DependencesVisitor#visitDataDependence(Object, Object, java.util.Collection, de.unisb.cs.st.javaslicer.variables.Variable, DataDependenceType)}
     * with {@link DataDependenceType#READ_AFTER_WRITE}
     */
    DATA_DEPENDENCES_READ_AFTER_WRITE,
    /**
     * enables invokations of
     * {@link DependencesVisitor#visitDataDependence(Object, Object, java.util.Collection, de.unisb.cs.st.javaslicer.variables.Variable, DataDependenceType)}
     * with {@link DataDependenceType#WRITE_AFTER_READ}
     */
    DATA_DEPENDENCES_WRITE_AFTER_READ,

    /**
     * enables invokations of
     * {@link DependencesVisitor#visitDataDependence(Object, Object, java.util.Collection, de.unisb.cs.st.javaslicer.variables.Variable, DataDependenceType)}
     * with {@link DataDependenceType#READ_AFTER_WRITE}
     */
    CONTROL_DEPENDENCES,

    /**
     * enables invokations of
     * {@link DependencesVisitor#visitInstructionExecution(Object)}
     */
    INSTRUCTION_EXECUTIONS,

    /**
     * enables invokations of
     * {@link DependencesVisitor#visitPendingControlDependence(Object)}
     */
    PENDING_CONTROL_DEPENDENCES,

    /**
     * combines {@link #PENDING_DATA_DEPENDENCES_READ_AFTER_WRITE} and
     * {@link #PENDING_DATA_DEPENDENCES_WRITE_AFTER_READ}
     */
    PENDING_DATA_DEPENDENCES_ALL,

    /**
     * enables invokations of
     * {@link DependencesVisitor#visitPendingControlDependence(Object)}
     * with {@link DataDependenceType#READ_AFTER_WRITE}
     */
    PENDING_DATA_DEPENDENCES_READ_AFTER_WRITE,

    /**
     * enables invokations of
     * {@link DependencesVisitor#visitPendingControlDependence(Object)}
     * with {@link DataDependenceType#WRITE_AFTER_READ}
     */
    PENDING_DATA_DEPENDENCES_WRITE_AFTER_READ,

    /**
     * enables invokations of
     * {@link DependencesVisitor#visitMethodEntry(ReadMethod, int)} and
     * {@link DependencesVisitor#visitMethodLeave(ReadMethod, int)}
     */
    METHOD_ENTRY_LEAVE,

    /**
     * enables invokations of
     * {@link DependencesVisitor#visitUntracedMethodCall(Object)}
     */
    UNTRACED_METHOD_CALLS,

    /**
     * enables invokations of
     * {@link DependencesVisitor#visitObjectCreation(long, Object)}
     */
    OBJECT_CREATION

}
