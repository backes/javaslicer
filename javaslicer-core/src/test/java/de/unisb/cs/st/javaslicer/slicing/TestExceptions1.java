/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestExceptions1
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestExceptions1.java
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


public class TestExceptions1 extends AbstractSlicingTest {

    @Test
    public void test38c() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main",
            "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:38:{c}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:29 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:29 ASTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:33 ALOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:33 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:35 ICONST_1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:35 ISTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:56 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:56 ARRAYLENGTH",
            });
    }

    @Test
    public void test46c() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main",
            "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:46:{c}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:39 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:39 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:41 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:41 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:43 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:43 ISTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:56 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:56 ARRAYLENGTH",
            });
    }

    @Test
    public void test34() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main",
            "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:34");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:29 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:29 ASTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:33 ALOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:33 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:56 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:56 ARRAYLENGTH",
            });
    }

    @Test
    public void test34all() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main",
            "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:34:*");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:29 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:29 ASTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:33 ALOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:33 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:34 ASTORE 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:56 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:56 ARRAYLENGTH",
            });
    }

    @Test
    public void test52() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main",
            "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:52:*");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:28 ICONST_1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:28 NEWARRAY T_INT",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:28 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:38 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:38 ASTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:39 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:39 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:41 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:41 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:43 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:43 ISTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:46 ALOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:46 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:52 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:52 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:52 ILOAD 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:52 IASTORE",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:56 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:56 ARRAYLENGTH",
            });
    }

    @Test
    public void test42() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main",
            "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:42");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:39 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:39 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:41 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:41 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:56 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:56 ARRAYLENGTH",
            });
    }

    @Test
    public void test42all() throws IllegalArgumentException, IOException {
        // check that the exception in line 42 only depends on a being null, not on b
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main",
            "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:42:*");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:39 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:39 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:41 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:41 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:42 ASTORE 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:56 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:56 ARRAYLENGTH",
            });
    }

}
