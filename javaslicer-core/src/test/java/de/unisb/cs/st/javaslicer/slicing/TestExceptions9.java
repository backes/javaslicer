/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestExceptions9
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestExceptions9.java
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

public class TestExceptions9 extends AbstractSlicingTest {

	@Test
	public void test() throws IllegalArgumentException, IOException,
			URISyntaxException, InterruptedException {
		final List<Instruction> slice = getSlice("/traces/exceptions9", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:27:*");
		checkSlice(
			slice,
			new String[] {
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:20 ICONST_2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:20 NEWARRAY T_INT",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:20 ASTORE 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:21 ACONST_NULL",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:21 ASTORE 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:23 ALOAD 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:23 ICONST_0",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:23 ALOAD 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:23 ICONST_0",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:23 ICONST_2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:23 INVOKESTATIC java/lang/System.arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:25 ICONST_3",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:25 NEWARRAY T_INT",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:25 ASTORE 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:27 GETSTATIC java/lang/System.out Ljava/io/PrintStream;",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:27 ALOAD 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:27 ARRAYLENGTH",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:27 INVOKEVIRTUAL java/io/PrintStream.println(I)V",
				});
	}

}
