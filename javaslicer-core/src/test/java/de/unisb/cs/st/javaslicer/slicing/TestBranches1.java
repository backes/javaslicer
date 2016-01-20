/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestBranches1
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestBranches1.java
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


public class TestBranches1 extends AbstractSlicingTest {

    @Test
    public void testAll() throws IOException {
        final List<Instruction> slice = getSlice("/traces/branches1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:39:{a,b,c,d,e,f}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ISTORE 1", // definition of a (== 1)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:31 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:31 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:31 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:31 ISTORE 2", // definition of b (== 2)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:32 ICONST_3",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:32 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:32 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:32 ISTORE 3", // definition of c (== 3)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:33 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:33 ICONST_5",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:33 IF_ICMPGE L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:33 ILOAD 2",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 GOTO L1",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ILOAD 3", // instruction after ":" unused
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:33 ISTORE 4", // definition of d

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:35 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:35 ICONST_1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:35 IF_ICMPNE L2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:35 ICONST_1",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 GOTO L3",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 ICONST_0", // false is never used here
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:35 ISTORE 5", // definition of true0 (== true)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:36 ILOAD 5",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:36 ILOAD 2",
                // "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:23 ILOAD 3", // 3rd parameter is never used
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:36 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Branches1.get(ZII)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:36 ISTORE 6", // definition of e

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:38 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:38 IFNE L4", // this branch is taken
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:25 ICONST_1", // so these instructions are
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:25 GOTO L4",  // never executed
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:38 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:38 ISTORE 7", // definition of false0 (== false)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:39 ILOAD 7",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:26 ILOAD 2", // 2nd parameter is never used
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:39 ILOAD 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:39 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Branches1.get(ZII)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:39 ISTORE 8", // definition of f

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:43 ILOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:43 IFEQ L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:44 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:44 IRETURN",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:45 ILOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:45 IRETURN",
            });
    }

    @Test
    public void test26() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/branches1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:39:{b,c,false0}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ISTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:31 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:31 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:31 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:31 ISTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:32 ICONST_3",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:32 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:32 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:32 ISTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:38 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:38 IFNE L4",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:38 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:38 ISTORE 7",
            });
    }

    @Test
    public void testD() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/branches1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:33:{d}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:29 ISTORE 1", // definition of a (== 1)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:31 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:31 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:31 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:31 ISTORE 2", // definition of b (== 2)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:33 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:33 ICONST_5",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:33 IF_ICMPGE L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:33 ILOAD 2",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 GOTO L1",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ILOAD 3", // instruction after ":" unused
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:33 ISTORE 4", // definition of d
            });
    }

}
