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
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;


public class TestExceptions1 extends AbstractSlicingTest {

    @Test
    public void test29c() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:29:{c}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:20 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:20 ASTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:24 ALOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:24 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:26 ICONST_1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:26 ISTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:47 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:47 ARRAYLENGTH",
            });
    }

    @Test
    public void test37c() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:37:{c}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:30 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:30 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:32 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:32 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:34 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:34 ISTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:47 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:47 ARRAYLENGTH",
            });
    }

    @Test
    public void test25() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:25");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:20 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:20 ASTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:24 ALOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:24 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:25 ASTORE 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:47 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:47 ARRAYLENGTH",
            });
    }

    @Test
    public void test25all() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:25:*");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:20 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:20 ASTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:24 ALOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:24 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:25 ASTORE 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:47 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:47 ARRAYLENGTH",
            });
    }

    @Test
    public void test43() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:43:*");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:19 ICONST_1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:19 NEWARRAY T_INT",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:19 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:29 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:29 ASTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:30 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:30 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:32 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:32 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:34 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:34 ISTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:37 ALOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:37 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:43 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:43 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:43 ILOAD 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:43 IASTORE",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:47 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:47 ARRAYLENGTH",
            });
    }

    @Test
    public void test33() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:33");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:30 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:30 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:32 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:32 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:33 ASTORE 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:47 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:47 ARRAYLENGTH",
            });
    }

    @Test
    public void test33all() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:33:*");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:30 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:30 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:32 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:32 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:33 ASTORE 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:47 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:47 ARRAYLENGTH",
            });
    }

}
