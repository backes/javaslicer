package de.unisb.cs.st.javaslicer.slicing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;

public class TestExceptions8 extends AbstractSlicingTest {

	@Test
	public void test() throws IllegalArgumentException, IOException,
			URISyntaxException, InterruptedException {
		final List<Instruction> slice = getSlice("/traces/exceptions8", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions8.main:14:*");
		checkSlice(
			slice,
			new String[] {
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions8.main:7 ACONST_NULL",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions8.main:7 ASTORE 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions8.main:10 ALOAD 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions8.main:10 INVOKEVIRTUAL java/lang/String.length()I",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions8.main:12 SIPUSH 4711",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions8.main:12 ISTORE 2",
				});
	}

}
