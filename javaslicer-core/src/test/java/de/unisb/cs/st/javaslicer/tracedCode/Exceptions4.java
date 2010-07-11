package de.unisb.cs.st.javaslicer.tracedCode;

public class Exceptions4 {

	public static void main(final String[] args) {
		String x = null;
		try {
			x = x.toString();
		} catch (NullPointerException e) {
			x = "null";
			System.out.println("Exception as expected");
		} finally {
			System.out.println(x);
		}
	}

}
