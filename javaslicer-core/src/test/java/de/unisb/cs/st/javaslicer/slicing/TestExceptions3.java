/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestExceptions3
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestExceptions3.java
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

public class TestExceptions3 extends AbstractSlicingTest {

	@Test
	public void test() throws IllegalArgumentException, IOException,
			URISyntaxException, InterruptedException {
		final List<Instruction> slice = getSlice("/traces/exceptions3", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:28:*");
		checkSlice(
			slice,
			new String[] {
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:21 ICONST_3",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:21 ISTORE 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:23 ACONST_NULL",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:23 ASTORE 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:24 ALOAD 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:24 GETFIELD de/unisb/cs/st/javaslicer/tracedCode/Exceptions3.x I",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:26 IINC 1 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:28 ILOAD 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:28 ICONST_2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:28 IMUL",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions3.main:28 ISTORE 1",
				});
	}

}
