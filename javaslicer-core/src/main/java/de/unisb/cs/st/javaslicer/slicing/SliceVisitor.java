package de.unisb.cs.st.javaslicer.slicing;

import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.variables.Variable;


/**
 * A Visitor which gets informed about all nodes and their depedencies in the dynamic slice.
 *
 * @author Clemens Hammacher
 */
public interface SliceVisitor {

	/**
	 * Visit an instruction instance which directly matched the slicing criterion.
	 * These are typically root nodes in the slice graph (but they don't have to).
	 *
	 * @param instance the instruction instance which matched the slicing criterion
	 */
    void visitMatchedInstance(InstructionInstance instance);

    /**
     * Visit a dependency in the slice graph.
     * <code>from</code> is an instruction instance which is already in the graph,
     * and <code>to</code> is the instance on which <code>from</code> depends.
     *
     * @param from the origin of the dependency (the depending instance)
     * @param to the destination of the dependency (the instance influencing <code>from</code>)
     * @param variable <code>null</code> if the dependence is a control dependence, otherwise
     *        (if it is a data dependence) the variable that <code>to</code> writes and
     *        <code>from</code> reads.
     * @param distance the distance from the instance which matched the slicing criterion
     */
    void visitSliceDependence(InstructionInstance from, InstructionInstance to,
    		Variable variable, int distance);

}
