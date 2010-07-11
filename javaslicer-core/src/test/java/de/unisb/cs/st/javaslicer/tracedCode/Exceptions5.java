package de.unisb.cs.st.javaslicer.tracedCode;

public class Exceptions5 {

	int x;

	public void set(int z) {
		this.x = z;
	}

	public static void main(final String[] args) {
		int y = 3;
		Exceptions5 foo = null;
		try {
			foo.x = y;
		} catch (Throwable t) {
			y += 2;
		}
		try {
			foo.set(2);
		} catch (NullPointerException t) {
			y += 3;
		}
		y *= 2;
		return;
	}

}
