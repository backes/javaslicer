package de.unisb.cs.st.javaslicer.dependenceAnalysis;

import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.DependencesVisitor.DataDependenceType;

public enum VisitorCapability {

    /**
     * combines {@link #DATA_DEPENDENCES_READ_AFTER_WRITE} and
     * {@link #DATA_DEPENDENCES_WRITE_AFTER_READ}
     */
    DATA_DEPENDENCES_ALL,

    /**
     * enables invokations of
     * {@link DependencesVisitor#visitDataDependence(InstructionInstance, InstructionInstance, de.unisb.cs.st.javaslicer.variables.Variable, DataDependenceType)}
     * with {@link DataDependenceType#READ_AFTER_WRITE}
     */
    DATA_DEPENDENCES_READ_AFTER_WRITE,
    /**
     * enables invokations of
     * {@link DependencesVisitor#visitDataDependence(InstructionInstance, InstructionInstance, de.unisb.cs.st.javaslicer.variables.Variable, DataDependenceType)}
     * with {@link DataDependenceType#WRITE_AFTER_READ}
     */
    DATA_DEPENDENCES_WRITE_AFTER_READ,

    /**
     * enables invokations of
     * {@link DependencesVisitor#visitDataDependence(InstructionInstance, InstructionInstance, de.unisb.cs.st.javaslicer.variables.Variable, DataDependenceType)}
     * with {@link DataDependenceType#READ_AFTER_WRITE}
     */
    CONTROL_DEPENDENCES,

    /**
     * enables invokations of
     * {@link DependencesVisitor#visitInstructionExecution(InstructionInstance)}
     */
    INSTRUCTION_EXECUTIONS,

    /**
     * enables invokations of
     * {@link DependencesVisitor#visitPendingControlDependence(InstructionInstance)}
     */
    PENDING_CONTROL_DEPENDENCES,

    /**
     * combines {@link #PENDING_DATA_DEPENDENCES_READ_AFTER_WRITE} and
     * {@link #PENDING_DATA_DEPENDENCES_WRITE_AFTER_READ}
     */
    PENDING_DATA_DEPENDENCES_ALL,

    /**
     * enables invokations of
     * {@link DependencesVisitor#visitPendingControlDependence(InstructionInstance)}
     * with {@link DataDependenceType#READ_AFTER_WRITE}
     */
    PENDING_DATA_DEPENDENCES_READ_AFTER_WRITE,

    /**
     * enables invokations of
     * {@link DependencesVisitor#visitPendingControlDependence(InstructionInstance)}
     * with {@link DataDependenceType#WRITE_AFTER_READ}
     */
    PENDING_DATA_DEPENDENCES_WRITE_AFTER_READ,

    /**
     * enables invokations of
     * {@link DependencesVisitor#visitMethodEntry(ReadMethod)} and
     * {@link DependencesVisitor#visitMethodLeave(ReadMethod)}
     */
    METHOD_ENTRY_LEAVE,

    /**
     * enables invokations of
     * {@link DependencesVisitor#visitObjectCreation(long, InstructionInstance)}
     */
    OBJECT_CREATION

}
