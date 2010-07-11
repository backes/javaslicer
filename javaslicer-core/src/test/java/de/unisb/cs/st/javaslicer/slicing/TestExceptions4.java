package de.unisb.cs.st.javaslicer.slicing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;

public class TestExceptions4 extends AbstractSlicingTest {

	@Test
	public void test() throws IllegalArgumentException, IOException,
			URISyntaxException, InterruptedException {
		final List<Instruction> slice = getSlice("/traces/exceptions4", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions4.main:13:*");
		checkSlice(
			slice,
			new String[] {
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions4.main:6 ACONST_NULL",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions4.main:6 ASTORE 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions4.main:8 ALOAD 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions4.main:8 INVOKEVIRTUAL java/lang/String.toString()Ljava/lang/String;",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions4.main:10 LDC \"null\"",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions4.main:10 ASTORE 1",
				});
	}

}
