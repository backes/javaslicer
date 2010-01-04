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
     * {@link DependencesVisitor#visitObjectCreation(long, Object)}
     */
    OBJECT_CREATION

}
