package de.unisb.cs.st.javaslicer.tracedCode;

public class Exceptions3 {

	int x;

	public static void main(final String[] args) {
		int y = 3;
		try {
			Exceptions3 foo = null;
			y = foo.x;
		} catch (Throwable t) {
			y += 2;
		}
		y *= 2;
	}

}
