/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestSimpleSlicing1
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestSimpleSlicing1.java
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


public class TestSimpleSlicing1 extends AbstractSlicingTest {

    @Test
    public void test29a() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/simple1", "main",
            "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29:{a}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ISTORE 1",
            });
    }

    @Test
    public void test30b() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/simple1", "main",
            "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30:{b}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ISTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 ISTORE 2",
            });
    }

    @Test
    public void test30bcd() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/simple1", "main",
            "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30:{b,c,d}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ISTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 ISTORE 2",
            });
    }

    @Test
    public void test30ab() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/simple1", "main",
            "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30:{a,b}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ISTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 ISTORE 2",
            });
    }

    @Test
    public void test31c() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/simple1", "main",
            "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:31:{c}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ISTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 ISTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:31 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:31 ILOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:31 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:31 ISTORE 3",
            });
    }

    @Test
    public void test32d() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/simple1", "main",
            "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:32:{d}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:29 ISTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:30 ISTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:31 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:31 ILOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:31 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:31 ISTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:32 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:32 ILOAD 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:32 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:32 ISTORE 4",
            });
    }

}
