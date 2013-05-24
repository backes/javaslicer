/** License information:
 *    Component: javaslicer-jung
 *    Package:   de.unisb.cs.st.javaslicer.jung
 *    Class:     CreateJungGraphSliceVisitor
 *    Filename:  javaslicer-jung/src/main/java/de/unisb/cs/st/javaslicer/jung/CreateJungGraphSliceVisitor.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
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

	@Override
	public void visitMatchedInstance(InstructionInstance instance) {
		this.graph.addVertex(this.vertexTransformer.transform(instance));
	}

	@Override
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
