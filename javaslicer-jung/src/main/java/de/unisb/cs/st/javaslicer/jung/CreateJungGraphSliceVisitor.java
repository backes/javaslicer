package de.unisb.cs.st.javaslicer.jung;

import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.slicing.SliceVisitor;
import de.unisb.cs.st.javaslicer.variables.Variable;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;


public class CreateJungGraphSliceVisitor implements SliceVisitor {

	private final DirectedGraph<InstructionInstance, SliceEdge> graph = new DirectedSparseMultigraph<InstructionInstance, SliceEdge>();
	private final int maxDistance;

	public CreateJungGraphSliceVisitor() {
		this(Integer.MAX_VALUE);
	}

	public CreateJungGraphSliceVisitor(int maxDistance) {
		this.maxDistance = maxDistance;
	}


	public void visitMatchedInstance(InstructionInstance instance) {
		this.graph.addVertex(instance);
	}

	public void visitSliceDependence(InstructionInstance from,
			InstructionInstance to, Variable variable, int distance) {
		if (distance <= this.maxDistance)
			this.graph.addEdge(new SliceEdge(from, to, variable), from, to);
	}

	public DirectedGraph<InstructionInstance, SliceEdge> getGraph() {
		return this.graph;
	}

}
