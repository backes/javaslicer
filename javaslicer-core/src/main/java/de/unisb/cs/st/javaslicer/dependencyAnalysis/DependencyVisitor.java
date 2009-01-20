package de.unisb.cs.st.javaslicer.dependencyAnalysis;

import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.InstructionInstance;
import de.unisb.cs.st.javaslicer.variables.Variable;


/**
 * A visitor that gets informed about determined dependencies of one program
 * trace.
 *
 * @author Clemens Hammacher
 */
public interface DependencyVisitor {

    public static enum DataDependencyType {
        READ_AFTER_WRITE,
        WRITE_AFTER_READ,
    }

    /**
     * Gets called if a (dynamic) data dependency has been determined.
     *
     * @param from the instruction that depends on another
     * @param to the &quot;target&quot; of the data dependency
     * @param var the variable through which the dependency exists
     * @param type the type of the data dependency (read after write / write after read)
     */
    void visitDataDependency(InstructionInstance from, InstructionInstance to,
            Variable var, DataDependencyType type);

    /**
     * Gets called if a dynamic occurence of a control dependency has been determined.
     *
     * @param from the instruction that depends on another
     * @param to the &quot;target&quot; of the control dependency, i.e. the instruction
     *           that controls the execution of <code>from</code>
     */
    void visitControlDependency(InstructionInstance from, InstructionInstance to);

    /**
     * Gets called for every instruction in the execution trace.
     *
     * @param instance the instruction instance that has just been visited
     */
    void visitInstructionExecution(InstructionInstance instance);

    /**
     * Gets called if there might be a data dependency that gets visited later.
     *
     * If the type is {@link DataDependencyType#READ_AFTER_WRITE}, then most probably
     * {@link #visitDataDependency(InstructionInstance, InstructionInstance, Variable, DataDependencyType)}
     * gets called exactly one (may also be that it is not called in some cases), but
     * definitively, at some later point
     * {@link #discardPendingDataDependency(InstructionInstance, Variable, DataDependencyType)}
     * is called.
     *
     * If the type is {@link DataDependencyType#WRITE_AFTER_READ}, then
     * {@link #visitDataDependency(InstructionInstance, InstructionInstance, Variable, DataDependencyType)}
     * may get called one or several times, and finally
     * {@link #discardPendingDataDependency(InstructionInstance, Variable, DataDependencyType)}
     * is called.
     *
     * @param from the instruction that depends on another
     * @param var the variable through which the dependency exists
     * @param type the type of the data dependency (read after write / write after read)
     */
    void visitPendingDataDependency(InstructionInstance from, Variable var, DataDependencyType type);

    /**
     * Gets called if the instruction <code>from</code> has a control dependency that
     * is not known yet. At some later time, {@link #visitControlDependency(InstructionInstance, InstructionInstance)}
     * is called for that instance.
     *
     * @param from the instruction instance that has a control dependency
     */
    void visitPendingControlDependency(InstructionInstance from);

    /**
     * Gets called if all data dependencies on the specific variable <code>var</code>
     * have been visited for the instruction instance <code>from</code>.
     *
     * @param from the instruction that depends on another
     * @param var the variable through which the dependency exists
     * @param type the type of the data dependency (read after write / write after read)
     */
    void discardPendingDataDependency(InstructionInstance from, Variable var, DataDependencyType type);

    /**
     * Gets called each time a new method in entered.
     *
     * @param method the method that is entered
     */
    void visitMethodEntry(ReadMethod method);

    /**
     * Gets called each time a method is left.
     *
     * Because the trace is processed backwards, a {@link #visitMethodEntry(ReadMethod)} is called
     * later.
     *
     * @param method the method that is left
     */
    void visitMethodLeave(ReadMethod method);

}
