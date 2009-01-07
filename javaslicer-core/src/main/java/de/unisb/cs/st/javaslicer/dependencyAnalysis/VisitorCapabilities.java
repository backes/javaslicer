package de.unisb.cs.st.javaslicer.dependencyAnalysis;

import de.unisb.cs.st.javaslicer.dependencyAnalysis.DependencyVisitor.DataDependencyType;

public enum VisitorCapabilities {

    /**
     * combines {@link #DATA_DEPENDENCIES_READ_AFTER_WRITE} and
     * {@link #DATA_DEPENDENCIES_WRITE_AFTER_READ}
     */
    DATA_DEPENDENCIES_ALL,

    /**
     * enables invokations of
     * {@link DependencyVisitor#visitDataDependency(de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance, de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance, de.unisb.cs.st.javaslicer.variables.Variable, de.unisb.cs.st.javaslicer.dependencyAnalysis.DependencyVisitor.DataDependencyType)}
     * with {@link DataDependencyType#READ_AFTER_WRITE}
     */
    DATA_DEPENDENCIES_READ_AFTER_WRITE,
    /**
     * enables invokations of
     * {@link DependencyVisitor#visitDataDependency(de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance, de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance, de.unisb.cs.st.javaslicer.variables.Variable, de.unisb.cs.st.javaslicer.dependencyAnalysis.DependencyVisitor.DataDependencyType)}
     * with {@link DataDependencyType#WRITE_AFTER_READ}
     */
    DATA_DEPENDENCIES_WRITE_AFTER_READ,

    /**
     * enables invokations of
     * {@link DependencyVisitor#visitDataDependency(de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance, de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance, de.unisb.cs.st.javaslicer.variables.Variable, de.unisb.cs.st.javaslicer.dependencyAnalysis.DependencyVisitor.DataDependencyType)}
     * with {@link DataDependencyType#READ_AFTER_WRITE}
     */
    CONTROL_DEPENDENCIES,

    /**
     * enables invokations of
     * {@link DependencyVisitor#visitInstructionExecution(de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance)}
     */
    INSTRUCTION_EXECUTIONS,

    /**
     * enables invokations of
     * {@link DependencyVisitor#visitPendingControlDependency(de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance)}
     */
    PENDING_CONTROL_DEPENDENCIES,

    /**
     * combines {@link #PENDING_DATA_DEPENDENCIES_READ_AFTER_WRITE} and
     * {@link #PENDING_DATA_DEPENDENCIES_WRITE_AFTER_READ}
     */
    PENDING_DATA_DEPENDENCIES_ALL,

    /**
     * enables invokations of
     * {@link DependencyVisitor#visitPendingControlDependency(de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance)}
     * with {@link DataDependencyType#READ_AFTER_WRITE}
     */
    PENDING_DATA_DEPENDENCIES_READ_AFTER_WRITE,

    /**
     * enables invokations of
     * {@link DependencyVisitor#visitPendingControlDependency(de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance)}
     * with {@link DataDependencyType#WRITE_AFTER_READ}
     */
    PENDING_DATA_DEPENDENCIES_WRITE_AFTER_READ,

    /**
     * enables invokations of
     * {@link DependencyVisitor#visitMethodEntry(de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod)} and
     * {@link DependencyVisitor#visitMethodLeave(de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod)}
     */
    METHOD_ENTRY_LEAVE

}
