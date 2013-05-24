/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.dependenceAnalysis
 *    Class:     VisitorCapability
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/dependenceAnalysis/VisitorCapability.java
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
