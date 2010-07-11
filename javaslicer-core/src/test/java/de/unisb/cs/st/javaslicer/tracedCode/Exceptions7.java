package de.unisb.cs.st.javaslicer.tracedCode;

import java.util.ArrayList;
import java.util.List;

public class Exceptions7 {

	public static void main(final String[] args) {
		int size = 1;
		List<int[]> arrays = new ArrayList<int[]>();
		try {
			while (true) {
				arrays.add(new int[size = 2*size]);
			}
		} catch (Throwable t) {
			System.out.println("Catched Exception: " + t);
			System.out.println("after creating array of size " + size);
		}
	}

}
