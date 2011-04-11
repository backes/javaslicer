/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestExceptions5
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestExceptions5.java
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

public class TestExceptions5 extends AbstractSlicingTest {

	@Test
	public void test() throws IllegalArgumentException, IOException,
			URISyntaxException, InterruptedException {
		final List<Instruction> slice = getSlice("/traces/exceptions5", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions5.main:38:{y}");
		checkSlice(
			slice,
			new String[] {
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions5.main:25 ICONST_3",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions5.main:25 ISTORE 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions5.main:26 ACONST_NULL",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions5.main:26 ASTORE 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions5.main:28 ALOAD 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions5.main:28 PUTFIELD de/unisb/cs/st/javaslicer/tracedCode/Exceptions5.x I",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions5.main:30 IINC 1 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions5.main:33 ALOAD 2",
				    // TODO this should be removed by more precise tracing:
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions5.main:33 ICONST_2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions5.main:33 INVOKEVIRTUAL de/unisb/cs/st/javaslicer/tracedCode/Exceptions5.set(I)V",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions5.main:35 IINC 1 3",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions5.main:37 ILOAD 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions5.main:37 ICONST_2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions5.main:37 IMUL",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions5.main:37 ISTORE 1",
				});
	}

}
