package de.unisb.cs.st.javaslicer.jung;

import org.apache.commons.collections15.Transformer;

import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.slicing.SliceVisitor;
import de.unisb.cs.st.javaslicer.variables.Variable;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;


public class CreateJungGraphSliceVisitor<VertexType> implements SliceVisitor {

	private final DirectedGraph<VertexType, SliceEdge<VertexType>> graph = new DirectedSparseMultigraph<VertexType, SliceEdge<VertexType>>();
	private Transformer<InstructionInstance, VertexType> vertexTransformer;
	private final int maxDistance;


	public CreateJungGraphSliceVisitor(Transformer<InstructionInstance, VertexType> vertexTransformer) {
		this(vertexTransformer, Integer.MAX_VALUE);
	}

	public CreateJungGraphSliceVisitor(Transformer<InstructionInstance, VertexType> vertexTransformer,
			int maxDistance) {
		this.vertexTransformer = vertexTransformer;
		this.maxDistance = maxDistance;
	}

	public void visitMatchedInstance(InstructionInstance instance) {
		this.graph.addVertex(this.vertexTransformer.transform(instance));
	}

	public void visitSliceDependence(InstructionInstance from,
			InstructionInstance to, Variable variable, int distance) {
		if (distance <= this.maxDistance) {
			VertexType fromVertex = this.vertexTransformer.transform(from);
			VertexType toVertex = this.vertexTransformer.transform(to);
			if (fromVertex != null && toVertex != null) {
				this.graph.addEdge(new SliceEdge<VertexType>(fromVertex, toVertex, variable), fromVertex, toVertex);
			}
		}
	}

	public DirectedGraph<VertexType, SliceEdge<VertexType>> getGraph() {
		return this.graph;
	}

}
