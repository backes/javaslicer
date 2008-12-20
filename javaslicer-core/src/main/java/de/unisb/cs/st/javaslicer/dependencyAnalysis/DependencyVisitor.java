package de.unisb.cs.st.javaslicer.dependencyAnalysis;

import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.variables.Variable;


/**
 * A visitor that gets informed about determined dependencies of one program
 * trace.
 *
 * @author Clemens Hammacher
 */
public interface DependencyVisitor {

    enum DataDependencyType {
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
    void visitDataDependency(Instance from, Instance to,
            Variable var, DataDependencyType type);

    /**
     * Gets called if a dynamic occurence of a control dependency has been determined.
     *
     * @param from the instruction that depends on another
     * @param to the &quot;target&quot; of the control dependency, i.e. the instruction
     *           that controls the execution of <code>from</code>
     */
    void visitControlDependency(Instance from, Instance to);

    /**
     * Gets called for every instruction in the execution trace.
     *
     * @param instance the instruction instance that has just been visited
     */
    void visitInstructionExecution(Instance instance);

    /**
     * Gets called if there might be a data dependency that gets visited later.
     *
     * If the type is {@link DataDependencyType#READ_AFTER_WRITE}, then there definitively
     * exists such a dependency, and {@link #visitDataDependency(Instance, Instance, Variable, DataDependencyType)}
     * gets called later.
     *
     * If the type is {@link DataDependencyType#WRITE_AFTER_READ}, then
     * {@link #visitDataDependency(Instance, Instance, Variable, DataDependencyType)}
     * may get called one or several times, and finally
     * {@link #discardPendingDataDependencies(Instance, Variable, DataDependencyType)}
     * is called.
     *
     * @param from the instruction that depends on another
     * @param var the variable through which the dependency exists
     * @param type the type of the data dependency (read after write / write after read)
     */
    void visitPendingDataDependency(Instance from, Variable var, DataDependencyType type);

    /**
     * Gets called if the instruction <code>from</code> has a control dependency that
     * is not known yet. At some later time, {@link #visitControlDependency(Instance, Instance)}
     * is called for that instance.
     *
     * @param from the instruction instance that has a control dependency
     */
    void visitPendingControlDependency(Instance from);

    /**
     * Gets called if all data dependencies have been visited for the instruction
     * instance <code>from</code>.
     *
     * @param from the instruction that depends on another
     * @param var the variable through which the dependency exists
     * @param type the type of the data dependency (read after write / write after read)
     */
    void discardPendingDataDependencies(Instance from, Variable var, DataDependencyType type);

}
