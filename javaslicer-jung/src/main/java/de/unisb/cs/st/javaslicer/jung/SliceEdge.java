package de.unisb.cs.st.javaslicer.jung;

import de.unisb.cs.st.javaslicer.variables.Variable;


public class SliceEdge<VertexType> {

	private final VertexType start;
	private final VertexType end;
	private final Variable variable;

	public SliceEdge(VertexType start, VertexType end,
			Variable variable) {
		if (start == null || end == null)
			throw new NullPointerException("start node and end node must not be null");
		this.start = start;
		this.end = end;
		this.variable = variable;
	}

	public VertexType getStart() {
		return this.start;
	}

	public VertexType getEnd() {
		return this.end;
	}

	public Variable getVariable() {
		return this.variable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.end.hashCode();
		result = prime * result + this.start.hashCode();
		result = prime * result + (this.variable == null ? 0 : this.variable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SliceEdge<?> other = (SliceEdge<?>) obj;
		if (!this.end.equals(other.end))
			return false;
		if (!this.start.equals(other.start))
			return false;
		if (this.variable == null) {
			if (other.variable != null)
				return false;
		} else if (!this.variable.equals(other.variable))
			return false;
		return true;
	}


}
