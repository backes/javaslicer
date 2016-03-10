package edu.columbia.cs.psl.javaslicer.tracedTests;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestBranches1 {
	private static int get(final boolean cond, final int ifTrue, final int ifFalse) {
		if (cond)
			return ifTrue;
		return ifFalse;
	}

	int z;
	@Test
	public void testBranches() throws Exception {
		final int a = z - '0'; // this expression must not be constant!

		final int b = 2 * a;
		final int c = 3 * a;
		final int d = a < 5 ? b : c;

		final boolean true0 = a == 1;
		final int e = get(true0, b, c);

		final boolean false0 = a == 0;
		final int f = get(false0, b, c);
	}
}
