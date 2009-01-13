package de.unisb.cs.st.javaslicer.controlflowanalysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Implementation of Ramalingam's generic loop tree construction algorithm.
 *
 * Restrictions:
 * Ramalingam allows more general loop headers.
 * That is needed for non-reducible control flow.
 * Here, as we analyze class files from Java, only consider reducible CF.
 * Thus, each strongly connected component is given the header of that component as a loop header.
 * That simplifies the implementation a little bit.
 *
 * See also Ramalingams paper:
 * G. Ramalingam
 * On loops, dominators, and dominance frontiers
 * ACM Transactions on Programming Languages and Systems (TOPLAS)
 * Volume 24, Issue 5 (September 2002), Pages: 455 - 490
 *
 * @see http://doi.acm.org/10.1145/570886.570887
 * @author Sebastian Hack <hack@cs.uni-sb.de>
 *
 */
public class LoopTree {

	public class Loop {
		private List<Loop> children = null;
		private final Set<ControlFlowGraph.InstrNode> nodes = new HashSet<ControlFlowGraph.InstrNode>();
		private final ControlFlowGraph.InstrNode header;
		private final Loop parent;
		
		private void addChild(Loop child) {
			if (children == null)
				children = new LinkedList<Loop>();
			children.add(child);
		}

		private Loop(Loop parent, ControlFlowGraph.InstrNode header) {
			this.header = header;
			this.parent = parent;
			if (parent != null)
    			parent.addChild(this);
		}
		
		/**
		 * Get all loops which are sub-loops of this one.
		 * @return A list of all sub-loops.
		 */
		public List<Loop> getNestedLoops() {
			return Collections.unmodifiableList(children);
		}
		
		/**
		 * Get all CFG nodes for whom this loop is the innermost enclosing loop.
		 * @return The list of CFG nodes in this loop.
		 */
		public Set<ControlFlowGraph.InstrNode> getNodes() {
			return Collections.unmodifiableSet(nodes);
		}
		
		/**
		 * Get the parent loop.
		 * @return The parent loop.
		 */
		public Loop getParent() {
			return parent;
		}
		
		/**
		 * Get the loop header of this loop.
		 * @return The loop header.
		 */
		public ControlFlowGraph.InstrNode getHeader() { 
			return header; 
		}
		
		/**
		 * Is this node the root of the loop tree?
		 * @return <code>true</code> if the node is the root, <code>false</code> if not.
		 */
		public boolean isRoot() { 
			return parent == null; 
		}
		
		/**
		 * Check if this node is a leaf in the loop tree.
		 * @return <code>true</code> if the node is a leaf, <code>false</code> if not.
		 */
		public boolean isLeaf() { 
			return children == null; 
		}
	}

	private final Map<ControlFlowGraph.InstrNode, Loop> innermostMap = new HashMap<ControlFlowGraph.InstrNode, Loop>();
	private final Loop root = new Loop(null, null);

	public Loop getRoot() {
		return root;
	}
	
	public Loop getInnermostEnclosingLoop(ControlFlowGraph.InstrNode n) {
		Loop res = innermostMap.get(n);
		return res != null ? res : root;
	}

	private LoopTree(ControlFlowGraph cfg) {
		Map<ControlFlowGraph.InstrNode, Set<ControlFlowGraph.InstrNode>> sccs = SCC.compute(cfg);
		Map<ControlFlowGraph.InstrNode, ControlFlowGraph.InstrNode> deletedEdges = new HashMap<ControlFlowGraph.InstrNode, ControlFlowGraph.InstrNode>();
		
		while (!sccs.isEmpty()) {
			
			// iterate over each SCC
			for (Map.Entry<ControlFlowGraph.InstrNode, Set<ControlFlowGraph.InstrNode>> entry : sccs.entrySet()) {
				ControlFlowGraph.InstrNode header = entry.getKey();
				Set<ControlFlowGraph.InstrNode> scc = entry.getValue();
				
				// get the parent loop so far
				// if the node was not seen so far, its parent is the loop tree root
				Loop parent = getInnermostEnclosingLoop(header);
				
				// Create a new loop node in the loop tree for every header
				// and put all nodes in the SCC inside that loop
				Loop loop = new Loop(parent, header);
				for (ControlFlowGraph.InstrNode node : scc)
					innermostMap.put(node, loop);
				
				// put all loopback edges into the deleted edges map
				for (ControlFlowGraph.InstrNode loopback : header.getPredecessors()) {
					if (scc.contains(loopback))
						deletedEdges.put(loopback, header);
				}
			}
			
			// recompute SCCs
			sccs = SCC.compute(cfg, deletedEdges);
		}
		
		// add the nodes to the corresponding loop nodes
		for (Map.Entry<ControlFlowGraph.InstrNode, Loop> entry : innermostMap.entrySet()) {
			Loop loop = entry.getValue();
			ControlFlowGraph.InstrNode node = entry.getKey();
			loop.nodes.add(node);
		}
	}
	
	public static LoopTree compute(ControlFlowGraph cfg) {
		return new LoopTree(cfg);
	}
	
}
