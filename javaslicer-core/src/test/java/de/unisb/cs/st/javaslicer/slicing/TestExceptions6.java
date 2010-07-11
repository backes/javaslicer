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
		final List<Instruction> slice = getSlice("/traces/exceptions6", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:20:*");
		checkSlice(
			slice,
			new String[] {
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:17 ACONST_NULL",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:17 ASTORE 1",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:19 ALOAD 1",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:19 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions2.useArrays([I[I)I",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:20 ASTORE 4",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:21 ALOAD 4",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:21 INVOKEVIRTUAL java/lang/NullPointerException.getMessage()Ljava/lang/String;",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:21 ASTORE 3",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:31 ALOAD 0",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:31 IFNONNULL L0",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 NEW java/lang/NullPointerException",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 DUP",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 LDC \"a is null\"",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 INVOKESPECIAL java/lang/NullPointerException.<init>(Ljava/lang/String;)V",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 ATHROW",
					"java.lang.Exception.<init>:41 ALOAD 0",
					"java.lang.Exception.<init>:41 ALOAD 1",
					"java.lang.Exception.<init>:41 INVOKESPECIAL java/lang/Throwable.<init>(Ljava/lang/String;)V",
					"java.lang.NullPointerException.<init>:46 ALOAD 0",
					"java.lang.NullPointerException.<init>:46 ALOAD 1",
					"java.lang.NullPointerException.<init>:46 INVOKESPECIAL java/lang/RuntimeException.<init>(Ljava/lang/String;)V",
					"java.lang.RuntimeException.<init>:43 ALOAD 0",
					"java.lang.RuntimeException.<init>:43 ALOAD 1",
					"java.lang.RuntimeException.<init>:43 INVOKESPECIAL java/lang/Exception.<init>(Ljava/lang/String;)V",
					"java.lang.Throwable.<init>:197 ALOAD 0",
					"java.lang.Throwable.<init>:197 ALOAD 1",
					"java.lang.Throwable.<init>:197 PUTFIELD java/lang/Throwable.detailMessage Ljava/lang/String;",
					"java.lang.Throwable.getMessage:253 ALOAD 0",
					"java.lang.Throwable.getMessage:253 GETFIELD java/lang/Throwable.detailMessage Ljava/lang/String;",
					"java.lang.Throwable.getMessage:253 ARETURN", });
	}

}
