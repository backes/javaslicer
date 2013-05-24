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
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;


public class TestCasting1 extends AbstractSlicingTest {

    @Test
    public void test_d3() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/casting1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:33:{d3}");
        checkSlice(slice, new String[] {
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20 ALOAD 0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20 ICONST_0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20 AALOAD",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20 ICONST_0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20 INVOKEVIRTUAL java/lang/String.charAt(I)C",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20 BIPUSH 48",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20 ISUB",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20 ISTORE 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:21 ILOAD 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:21 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:21 DSTORE 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:22 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:22 ILOAD 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:22 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:22 DMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:22 D2I",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:22 ISTORE 4",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:23 ILOAD 4",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:23 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:23 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:23 DMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:23 DSTORE 5",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:24 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:24 D2F",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:24 FSTORE 7",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:25 DLOAD 5",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:25 D2I",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:25 I2B",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:25 ISTORE 8",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:26 ILOAD 8",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:26 I2F",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:26 FLOAD 7",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:26 FMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:26 F2I",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:26 I2B",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:26 ISTORE 9",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:28 ILOAD 9",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:28 ILOAD 8",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:28 IMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:28 I2L",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:28 LSTORE 11",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 FLOAD 7",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 LLOAD 11",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 L2F",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 FMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 F2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:31 DSTORE 17",
        	});
    }

    @Test
    public void test_f2() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/casting1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:33:{f2}");
        checkSlice(slice, new String[] {
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20 ALOAD 0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20 ICONST_0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20 AALOAD",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20 ICONST_0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20 INVOKEVIRTUAL java/lang/String.charAt(I)C",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20 BIPUSH 48",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20 ISUB",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20 ISTORE 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:21 ILOAD 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:21 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:21 DSTORE 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:22 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:22 ILOAD 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:22 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:22 DMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:22 D2I",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:22 ISTORE 4",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:23 ILOAD 4",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:23 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:23 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:23 DMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:23 DSTORE 5",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:24 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:24 D2F",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:24 FSTORE 7",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 FLOAD 7",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 F2L",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:29 LSTORE 13",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:30 DLOAD 5",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:30 LLOAD 13",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:30 L2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:30 DADD",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:30 D2L",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:30 LSTORE 15",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:32 LLOAD 15",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:32 ILOAD 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:32 I2L",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:32 LMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:32 L2F",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:32 FSTORE 19",
        	});
    }

}
