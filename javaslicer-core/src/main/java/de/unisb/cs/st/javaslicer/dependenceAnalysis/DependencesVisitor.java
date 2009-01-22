package de.unisb.cs.st.javaslicer.dependenceAnalysis;

import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.InstructionInstance;
import de.unisb.cs.st.javaslicer.variables.Variable;


/**
 * A visitor that gets informed about determined dependencies of one program
 * trace.
 *
 * @author Clemens Hammacher
 */
public interface DependencesVisitor {

    public static enum DataDependenceType {
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
    void visitDataDependence(InstructionInstance from, InstructionInstance to,
            Variable var, DataDependenceType type);

    /**
     * Gets called if a dynamic occurence of a control dependency has been determined.
     *
     * @param from the instruction that depends on another
     * @param to the &quot;target&quot; of the control dependency, i.e. the instruction
     *           that controls the execution of <code>from</code>
     */
    void visitControlDependence(InstructionInstance from, InstructionInstance to);

    /**
     * Gets called for every instruction in the execution trace.
     *
     * @param instance the instruction instance that has just been visited
     */
    void visitInstructionExecution(InstructionInstance instance);

    /**
     * Gets called if there might be a data dependency that gets visited later.
     *
     * If the type is {@link DataDependenceType#READ_AFTER_WRITE}, then most probably
     * {@link #visitDataDependence(InstructionInstance, InstructionInstance, Variable, DataDependenceType)}
     * gets called exactly one (may also be that it is not called in some cases), but
     * definitively, at some later point
     * {@link #discardPendingDataDependence(InstructionInstance, Variable, DataDependenceType)}
     * is called.
     *
     * If the type is {@link DataDependenceType#WRITE_AFTER_READ}, then
     * {@link #visitDataDependence(InstructionInstance, InstructionInstance, Variable, DataDependenceType)}
     * may get called one or several times, and finally
     * {@link #discardPendingDataDependence(InstructionInstance, Variable, DataDependenceType)}
     * is called.
     *
     * @param from the instruction that depends on another
     * @param var the variable through which the dependency exists
     * @param type the type of the data dependency (read after write / write after read)
     */
    void visitPendingDataDependence(InstructionInstance from, Variable var, DataDependenceType type);

    /**
     * Gets called if the instruction <code>from</code> has a control dependency that
     * is not known yet. At some later time, {@link #visitControlDependence(InstructionInstance, InstructionInstance)}
     * is called for that instance.
     *
     * @param from the instruction instance that has a control dependency
     */
    void visitPendingControlDependence(InstructionInstance from);

    /**
     * Gets called if all data dependencies on the specific variable <code>var</code>
     * have been visited for the instruction instance <code>from</code>.
     *
     * @param from the instruction that depends on another
     * @param var the variable through which the dependency exists
     * @param type the type of the data dependency (read after write / write after read)
     */
    void discardPendingDataDependence(InstructionInstance from, Variable var, DataDependenceType type);

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
