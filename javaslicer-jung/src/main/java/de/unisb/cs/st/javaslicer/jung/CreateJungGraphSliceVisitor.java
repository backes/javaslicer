/** License information:
 *    Component: javaslicer-jung
 *    Package:   de.unisb.cs.st.javaslicer.jung
 *    Class:     CreateJungGraphSliceVisitor
 *    Filename:  javaslicer-jung/src/main/java/de/unisb/cs/st/javaslicer/jung/CreateJungGraphSliceVisitor.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
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
