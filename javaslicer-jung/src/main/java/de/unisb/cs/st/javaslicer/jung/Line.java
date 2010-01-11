package de.unisb.cs.st.javaslicer.jung;

import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;

public class Line {

	private final ReadMethod method;
	private final int lineNr;

	public Line(ReadMethod method, int lineNr) {
		this.method = method;
		this.lineNr = lineNr;
	}

	public ReadMethod getMethod() {
		return this.method;
	}

	public int getLineNr() {
		return this.lineNr;
	}

	@Override
	public String toString() {
		return this.method + ":" + this.lineNr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.lineNr;
		result = prime * result + this.method.hashCode();
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
		Line other = (Line) obj;
		if (this.lineNr != other.lineNr)
			return false;
		if (!this.method.equals(other.method))
			return false;
		return true;
	}

}
