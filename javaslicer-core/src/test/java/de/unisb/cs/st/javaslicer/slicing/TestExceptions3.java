package de.unisb.cs.st.javaslicer.slicing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;

public class TestExceptions3 extends AbstractSlicingTest {

	@Test
	public void test() throws IllegalArgumentException, IOException,
			URISyntaxException, InterruptedException {
		final List<Instruction> slice = getSlice("/traces/exceptions3", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:15:*");
		checkSlice(
			slice,
			new String[] {
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:8 ICONST_3",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:8 ISTORE 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:10 ACONST_NULL",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:10 ASTORE 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:11 ALOAD 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:11 GETFIELD de/unisb/cs/st/javaslicer/tracedCode/Exceptions3.x I",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:13 IINC 1 2",
				});
	}

}
