/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestExceptions6
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestExceptions6.java
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

public class TestExceptions6 extends AbstractSlicingTest {

	@Test
	public void test() throws IllegalArgumentException, IOException,
			URISyntaxException, InterruptedException {
		final List<Instruction> slice = getSlice("/traces/exceptions6", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:33:{y}");
		checkSlice(
			slice,
			new String[] {
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:26 ICONST_3",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:26 ISTORE 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:28 IINC 1 3",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:29 NEW de/unisb/cs/st/javaslicer/tracedCode/Exceptions6$MyException",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:29 DUP",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:29 SIPUSH 4711",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:29 INVOKESPECIAL de/unisb/cs/st/javaslicer/tracedCode/Exceptions6$MyException.<init>(I)V",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:30 ASTORE 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:31 ILOAD 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:31 ALOAD 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:31 GETFIELD de/unisb/cs/st/javaslicer/tracedCode/Exceptions6$MyException.x I",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:31 IADD",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:31 ISTORE 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:33 ILOAD 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:33 ICONST_2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:33 IMUL",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:33 ISTORE 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6$MyException.<init>:21 ALOAD 0",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6$MyException.<init>:21 ILOAD 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6$MyException.<init>:21 PUTFIELD de/unisb/cs/st/javaslicer/tracedCode/Exceptions6$MyException.x I",
				});
	}

}
