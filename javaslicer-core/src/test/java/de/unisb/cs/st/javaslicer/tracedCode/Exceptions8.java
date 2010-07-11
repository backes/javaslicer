package de.unisb.cs.st.javaslicer.tracedCode;


public class Exceptions8 {

	public static void main(final String[] args) {
		String x = null;
		int z;
		try {
			z = 2 + x.length(); // when the exception is throws, "2" remains on the operand stack
		} catch (NullPointerException e) {
			z = 4711;
		}
		System.out.println(z);
	}

}
