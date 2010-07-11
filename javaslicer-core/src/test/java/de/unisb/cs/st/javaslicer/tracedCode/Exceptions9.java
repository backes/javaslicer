package de.unisb.cs.st.javaslicer.tracedCode;


public class Exceptions9 {

	public static void main(final String[] args) {
		int[] x = new int[2];
		int[] y = null;
		try {
			System.arraycopy(x, 0, y, 0, 2); // exception from native call
		} catch (NullPointerException e) {
			y = new int[3];
		}
		System.out.println(y.length);
	}

}
