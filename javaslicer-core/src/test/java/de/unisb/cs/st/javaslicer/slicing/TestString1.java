/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestString1
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestString1.java
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


public class TestString1 extends AbstractSlicingTest {

    @Test
    public void testAll() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/string1", "main", "de.unisb.cs.st.javaslicer.tracedCode.String1.main:*");
        /*
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:20 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:20 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:20 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:20 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:20 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:20 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:20 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:20 ISTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:21 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:21 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:21 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:21 ISTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:22 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:22 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:22 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:22 ISTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:23 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:23 ILOAD 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:23 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:23 ISTORE 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:24 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:24 ILOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:24 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:24 ISTORE 5",
            });
        */
    }

}
