/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestExceptions9
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestExceptions9.java
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

public class TestExceptions9 extends AbstractSlicingTest {

	@Test
	public void test() throws IllegalArgumentException, IOException {
		final List<Instruction> slice = getSlice("/traces/exceptions9", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:36:*");
		checkSlice(
			slice,
			new String[] {
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:29 ICONST_2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:29 NEWARRAY T_INT",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:29 ASTORE 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:30 ACONST_NULL",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:30 ASTORE 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:32 ALOAD 1",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:32 ICONST_0",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:32 ALOAD 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:32 ICONST_0",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:32 ICONST_2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:32 INVOKESTATIC java/lang/System.arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:34 ICONST_3",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:34 NEWARRAY T_INT",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:34 ASTORE 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:36 GETSTATIC java/lang/System.out Ljava/io/PrintStream;",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:36 ALOAD 2",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:36 ARRAYLENGTH",
				    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions9.main:36 INVOKEVIRTUAL java/io/PrintStream.println(I)V",
				});
	}

}
