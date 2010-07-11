package de.unisb.cs.st.javaslicer.slicing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;

public class TestExceptions9 extends AbstractSlicingTest {

	@Test
	public void test() throws IllegalArgumentException, IOException,
			URISyntaxException, InterruptedException {
		final List<Instruction> slice = getSlice("/traces/exceptions9", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:14:*");
		checkSlice(
			slice,
			new String[] {
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:7 ICONST_2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:7 NEWARRAY T_INT",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:7 ASTORE 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:8 ACONST_NULL",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:8 ASTORE 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:10 ALOAD 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:10 ICONST_0",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:10 ALOAD 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:10 ICONST_0",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:10 ICONST_2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:10 INVOKESTATIC java/lang/System.arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:12 ICONST_3",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:12 NEWARRAY T_INT",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:12 ASTORE 2",
				});
	}

}
