/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestExceptions1
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestExceptions1.java
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


public class TestExceptions1 extends AbstractSlicingTest {

    @Test
    public void test16c() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:16:{c}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:7 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:7 ASTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:11 ALOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:11 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:13 ICONST_1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:13 ISTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:34 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:34 ARRAYLENGTH",
            });
    }

    @Test
    public void test24c() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:24:{c}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:17 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:17 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:19 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:19 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:21 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:21 ISTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:34 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:34 ARRAYLENGTH",
            });
    }

    @Test
    public void test30() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:30:*");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:6 ICONST_1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:6 NEWARRAY T_INT",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:6 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:16 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:16 ASTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:17 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:17 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:19 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:19 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:21 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:21 ISTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:24 ALOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:24 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:34 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:34 ARRAYLENGTH",
            });
    }

}
