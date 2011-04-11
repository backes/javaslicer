/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestBranches1
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestBranches1.java
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


public class TestBranches1 extends AbstractSlicingTest {

    @Test
    public void testAll() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/branches1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:30:{a,b,c,d,e,f}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ISTORE 1", // definition of a (== 1)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 ISTORE 2", // definition of b (== 2)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:23 ICONST_3",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:23 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:23 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:23 ISTORE 3", // definition of c (== 3)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:24 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:24 ICONST_5",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:24 IF_ICMPGE L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:24 ILOAD 2",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 GOTO L1",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 ILOAD 3", // instruction after ":" unused
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:24 ISTORE 4", // definition of d

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:26 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:26 ICONST_1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:26 IF_ICMPNE L2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:26 ICONST_1",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:13 GOTO L3",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:13 ICONST_0", // false is never used here
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:26 ISTORE 5", // definition of true0 (== true)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:27 ILOAD 5",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:27 ILOAD 2",
                // "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:14 ILOAD 3", // 3rd parameter is never used
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:27 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Branches1.get(ZII)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:27 ISTORE 6", // definition of e

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 IFNE L4", // this branch is taken
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:16 ICONST_1", // so these instructions are
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:16 GOTO L4",  // never executed
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ISTORE 7", // definition of false0 (== false)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:30 ILOAD 7",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:17 ILOAD 2", // 2nd parameter is never used
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:30 ILOAD 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:30 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Branches1.get(ZII)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:30 ISTORE 8", // definition of f

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:34 ILOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:34 IFEQ L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:35 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:35 IRETURN",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:36 ILOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:36 IRETURN",
            });
    }

    @Test
    public void test17() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/branches1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:30:{b,c,false0}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ISTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 ISTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:23 ICONST_3",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:23 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:23 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:23 ISTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 IFNE L4",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ISTORE 7",
            });
    }

    @Test
    public void testD() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/branches1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:24:{d}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ISTORE 1", // definition of a (== 1)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 ISTORE 2", // definition of b (== 2)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:24 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:24 ICONST_5",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:24 IF_ICMPGE L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:24 ILOAD 2",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 GOTO L1",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 ILOAD 3", // instruction after ":" unused
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:24 ISTORE 4", // definition of d
            });
    }

}
