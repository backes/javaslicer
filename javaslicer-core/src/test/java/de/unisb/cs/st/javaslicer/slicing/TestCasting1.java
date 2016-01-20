/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestCasting1
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestCasting1.java
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


public class TestCasting1 extends AbstractSlicingTest {

    @Test
    public void test_d3() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/casting1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:42:{d3}");
        checkSlice(slice, new String[] {
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 ALOAD 0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 ICONST_0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 AALOAD",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 ICONST_0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 INVOKEVIRTUAL java/lang/String.charAt(I)C",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 BIPUSH 48",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 ISUB",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 ISTORE 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:30 ILOAD 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:30 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:30 DSTORE 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 ILOAD 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 DMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 D2I",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 ISTORE 4",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:32 ILOAD 4",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:32 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:32 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:32 DMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:32 DSTORE 5",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:33 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:33 D2F",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:33 FSTORE 7",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:34 DLOAD 5",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:34 D2I",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:34 I2B",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:34 ISTORE 8",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:35 ILOAD 8",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:35 I2F",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:35 FLOAD 7",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:35 FMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:35 F2I",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:35 I2B",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:35 ISTORE 9",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:37 ILOAD 9",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:37 ILOAD 8",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:37 IMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:37 I2L",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:37 LSTORE 11",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:40 FLOAD 7",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:40 LLOAD 11",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:40 L2F",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:40 FMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:40 F2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:40 DSTORE 17",
        	});
    }

    @Test
    public void test_f2() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/casting1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:42:{f2}");
        checkSlice(slice, new String[] {
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 ALOAD 0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 ICONST_0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 AALOAD",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 ICONST_0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 INVOKEVIRTUAL java/lang/String.charAt(I)C",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 BIPUSH 48",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 ISUB",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 ISTORE 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:30 ILOAD 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:30 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:30 DSTORE 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 ILOAD 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 DMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 D2I",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 ISTORE 4",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:32 ILOAD 4",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:32 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:32 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:32 DMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:32 DSTORE 5",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:33 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:33 D2F",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:33 FSTORE 7",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:38 FLOAD 7",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:38 F2L",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:38 LSTORE 13",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:39 DLOAD 5",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:39 LLOAD 13",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:39 L2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:39 DADD",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:39 D2L",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:39 LSTORE 15",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:41 LLOAD 15",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:41 ILOAD 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:41 I2L",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:41 LMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:41 L2F",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:41 FSTORE 19",
        	});
    }

}
