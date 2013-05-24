/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestMethod1
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestMethod1.java
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

import org.junit.Ignore;
import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;


public class TestMethod1 extends AbstractSlicingTest {

    @Test
    public void testAll() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/method1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24:{a,b,c,d,e}");
        checkSlice(slice, new String[] {
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ALOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 AALOAD",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 BIPUSH 48",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ISUB",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ISTORE 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:21 ICONST_2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:21 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:21 IMUL",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:21 ISTORE 2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:22 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:22 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.getFirst(II)I",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:22 ISTORE 3",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:23 ILOAD 2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:23 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.getSecond(II)I",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:23 ISTORE 4",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 ICONST_2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 NEWARRAY T_INT",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 DUP",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 ICONST_1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 ILOAD 2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 IASTORE",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.get(I[I)I",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 ISTORE 5",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.getFirst:28 ILOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.getFirst:28 IRETURN",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.getSecond:32 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.getSecond:32 IRETURN",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.get:36 ALOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.get:36 ILOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.get:36 IALOAD",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.get:36 IRETURN",
            });
    }

    @Test
    public void testAa() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/method1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:25:{a}");
        checkSlice(slice, new String[] {
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ALOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 AALOAD",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 BIPUSH 48",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ISUB",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ISTORE 1",
            });
    }

    @Test
    public void testAb() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/method1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20:{a}");
        checkSlice(slice, new String[] {
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ALOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 AALOAD",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 BIPUSH 48",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ISUB",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ISTORE 1",
            });
    }

    @Test
    public void testC() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/method1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:25:{c}");
        checkSlice(slice, new String[] {
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ALOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 AALOAD",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 BIPUSH 48",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ISUB",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ISTORE 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:22 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:22 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.getFirst(II)I",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:22 ISTORE 3",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.getFirst:28 ILOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.getFirst:28 IRETURN",
            });
    }

    @Test
    public void testD() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/method1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:25:{d}");
        checkSlice(slice, new String[] {
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ALOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 AALOAD",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 BIPUSH 48",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ISUB",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ISTORE 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:21 ICONST_2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:21 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:21 IMUL",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:21 ISTORE 2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:23 ILOAD 2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:23 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.getSecond(II)I",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:23 ISTORE 4",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.getSecond:32 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.getSecond:32 IRETURN",
            });
    }

    @Test
    public void testE() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/method1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:25:{e}");
        checkSlice(slice, new String[] {
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ALOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 AALOAD",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 BIPUSH 48",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ISUB",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ISTORE 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:21 ICONST_2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:21 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:21 IMUL",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:21 ISTORE 2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 ICONST_2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 NEWARRAY T_INT",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 DUP",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 ICONST_1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 ILOAD 2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 IASTORE",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.get(I[I)I",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:24 ISTORE 5",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.get:36 ALOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.get:36 ILOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.get:36 IALOAD",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.get:36 IRETURN",
            });
    }

    // TODO fix handling of parameters
    @Ignore
    @Test
    public void testGetFirstB() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/method1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.getFirst:{b}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ISTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:21 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:21 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:21 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:21 ISTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:22 ILOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:22 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.getFirst(II)I",
            });
    }

    // TODO fix handling of parameters
    @Ignore
    @Test
    public void testGetSecondA() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/method1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.getSecond:{a}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:20 ISTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:23 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:23 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.getSecond(II)I",
            });
    }

}
