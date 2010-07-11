package de.unisb.cs.st.javaslicer.tracedCode;

public class Exceptions6 {

	public static class MyException extends Exception {
		int x;
		public MyException(int x) {
			this.x = x;
		}
	}

	public static void main(final String[] args) {
		int y = 3;
		try {
			y += 3;
			throw new MyException(4711);
		} catch (MyException e) {
			y += e.x;
		}
		y *= 2;
	}

}
