package de.unisb.cs.st.javaslicer.controlflowanalysis;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/**
 * Compute strongly-connected components in a control-flow graph.
 * This is done by Tarjan's famous algorithm.
 * @author Sebastian Hack <hack@cs.uni-sb.de>
 */
public class SCC implements Comparator<ControlFlowGraph.InstrNode> {
	
	private static class Info {
		public int dfsnum, low;
		
		public Info(int dfsnum) {
			this.low = this.dfsnum = dfsnum;
		}
	}
	
	private final Map<ControlFlowGraph.InstrNode, Info> info = new HashMap<ControlFlowGraph.InstrNode, Info>();
	private final Map<ControlFlowGraph.InstrNode, Set<ControlFlowGraph.InstrNode>> sccs = new HashMap<ControlFlowGraph.InstrNode, Set<ControlFlowGraph.InstrNode>>();
	private final Map<ControlFlowGraph.InstrNode, ControlFlowGraph.InstrNode> excludeEdges;
	private final ControlFlowGraph cfg;
	
	private Stack<ControlFlowGraph.InstrNode> list = new Stack<ControlFlowGraph.InstrNode>();
	private int num = 0;
	
	private SCC(ControlFlowGraph cfg, Map<ControlFlowGraph.InstrNode, ControlFlowGraph.InstrNode> excludeEdges) {
		this.cfg = cfg;
		this.excludeEdges = excludeEdges;
		visit(cfg.get
	}
	
	private Info visit(ControlFlowGraph.InstrNode n) {
		Info nn = new Info(num++);
		info.put(n, nn);
		list.push(n);
		
		ControlFlowGraph.InstrNode ignoreSucc = excludeEdges.get(n);
		for (ControlFlowGraph.InstrNode succ : n.getSuccessors()) {
			if (succ.equals(ignoreSucc))
				continue;
			
			int min = info.containsKey(succ) ? info.get(succ).dfsnum : visit(succ).low;
			nn.low = Math.min(nn.low, min);
		}
		
		if (nn.low == nn.dfsnum) {
			Set<ControlFlowGraph.InstrNode> scc = new TreeSet<ControlFlowGraph.InstrNode>(this);
			
			ControlFlowGraph.InstrNode v;
			do {
				v = list.pop();
				scc.add(v);
			} while(v != n);
			
			sccs.put(n, scc);
		}
		
		return nn;
	}
	
	public int compare(ControlFlowGraph.InstrNode o1, ControlFlowGraph.InstrNode o2) {
		Info n1 = info.get(o1);
		Info n2 = info.get(o2);
		if (n1.dfsnum > n2.dfsnum)
			return 1;
		if (n1.dfsnum < n2.dfsnum)
			return -1;
		return 0;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<ControlFlowGraph.InstrNode, Set<ControlFlowGraph.InstrNode>> compute(ControlFlowGraph cfg) {
		return compute(cfg, Collections.EMPTY_MAP);
	}
	
	public static Map<ControlFlowGraph.InstrNode, Set<ControlFlowGraph.InstrNode>> compute(ControlFlowGraph cfg, Map<ControlFlowGraph.InstrNode, ControlFlowGraph.InstrNode> ignoreEdges) {
		SCC sccs = new SCC(cfg, ignoreEdges);
		return Collections.unmodifiableMap(sccs.sccs);
	}

}
