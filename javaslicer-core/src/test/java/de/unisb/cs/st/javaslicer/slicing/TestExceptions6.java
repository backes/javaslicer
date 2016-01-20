/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestExceptions6
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestExceptions6.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.javaslicer.slicing;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;

public class TestExceptions6 extends AbstractSlicingTest {

	@Test
	public void test() throws IllegalArgumentException, IOException {
		final List<Instruction> slice = getSlice("/traces/exceptions6", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:42:{y}");
		checkSlice(
			slice,
			new String[] {
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:35 ICONST_3",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:35 ISTORE 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:37 IINC 1 3",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:38 NEW de/unisb/cs/st/javaslicer/tracedCode/Exceptions6$MyException",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:38 DUP",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:38 SIPUSH 4711",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:38 INVOKESPECIAL de/unisb/cs/st/javaslicer/tracedCode/Exceptions6$MyException.<init>(I)V",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:39 ASTORE 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:40 ILOAD 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:40 ALOAD 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:40 GETFIELD de/unisb/cs/st/javaslicer/tracedCode/Exceptions6$MyException.x I",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:40 IADD",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:40 ISTORE 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:42 ILOAD 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:42 ICONST_2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:42 IMUL",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6.main:42 ISTORE 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6$MyException.<init>:30 ALOAD 0",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6$MyException.<init>:30 ILOAD 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions6$MyException.<init>:30 PUTFIELD de/unisb/cs/st/javaslicer/tracedCode/Exceptions6$MyException.x I",
				});
	}

}
