package de.unisb.cs.st.javaslicer.slicing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;

public class TestExceptions6 extends AbstractSlicingTest {

	@Test
	public void test() throws IllegalArgumentException, IOException,
			URISyntaxException, InterruptedException {
		final List<Instruction> slice = getSlice("/traces/exceptions6", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:20:{y}");
		checkSlice(
			slice,
			new String[] {
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:13 ICONST_3",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:13 ISTORE 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:15 IINC 1 3",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:16 NEW de/unisb/cs/st/javaslicer/tracedCode/Exceptions6$MyException",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:16 DUP",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:16 SIPUSH 4711",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:16 INVOKESPECIAL de/unisb/cs/st/javaslicer/tracedCode/Exceptions6$MyException.<init>(I)V",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:17 ASTORE 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:18 ILOAD 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:18 ALOAD 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:18 GETFIELD de/unisb/cs/st/javaslicer/tracedCode/Exceptions6$MyException.x I",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:18 IADD",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:18 ISTORE 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:20 ILOAD 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:20 ICONST_2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:20 IMUL",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:20 ISTORE 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6$MyException.<init>:8 ALOAD 0",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6$MyException.<init>:8 ILOAD 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6$MyException.<init>:8 PUTFIELD de/unisb/cs/st/javaslicer/tracedCode/Exceptions6$MyException.x I",
				});
	}

}
