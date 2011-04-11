/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestExceptions7
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestExceptions7.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.slicing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;

public class TestExceptions7 extends AbstractSlicingTest {

	@Test
	public void test() throws IllegalArgumentException, IOException,
			URISyntaxException, InterruptedException {
		final List<Instruction> slice = getSlice("/traces/exceptions7", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions7.main:31");
		checkSlice(
			slice,
			new String[] {
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:30 ACONST_NULL",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:30 ASTORE 1",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:32 ALOAD 1",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:32 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions2.useArrays([I[I)I",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:33 ASTORE 4",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:34 ALOAD 4",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:34 INVOKEVIRTUAL java/lang/NullPointerException.getMessage()Ljava/lang/String;",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:34 ASTORE 3",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:44 ALOAD 0",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:44 IFNONNULL L0",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 NEW java/lang/NullPointerException",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 DUP",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 LDC \"a is null\"",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 INVOKESPECIAL java/lang/NullPointerException.<init>(Ljava/lang/String;)V",
					"de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 ATHROW",
					"java.lang.Exception.<init>:54 ALOAD 0",
					"java.lang.Exception.<init>:54 ALOAD 1",
					"java.lang.Exception.<init>:54 INVOKESPECIAL java/lang/Throwable.<init>(Ljava/lang/String;)V",
					"java.lang.NullPointerException.<init>:59 ALOAD 0",
					"java.lang.NullPointerException.<init>:59 ALOAD 1",
					"java.lang.NullPointerException.<init>:59 INVOKESPECIAL java/lang/RuntimeException.<init>(Ljava/lang/String;)V",
					"java.lang.RuntimeException.<init>:56 ALOAD 0",
					"java.lang.RuntimeException.<init>:56 ALOAD 1",
					"java.lang.RuntimeException.<init>:56 INVOKESPECIAL java/lang/Exception.<init>(Ljava/lang/String;)V",
					"java.lang.Throwable.<init>:210 ALOAD 0",
					"java.lang.Throwable.<init>:210 ALOAD 1",
					"java.lang.Throwable.<init>:210 PUTFIELD java/lang/Throwable.detailMessage Ljava/lang/String;",
					"java.lang.Throwable.getMessage:266 ALOAD 0",
					"java.lang.Throwable.getMessage:266 GETFIELD java/lang/Throwable.detailMessage Ljava/lang/String;",
					"java.lang.Throwable.getMessage:266 ARETURN", });
	}

}
