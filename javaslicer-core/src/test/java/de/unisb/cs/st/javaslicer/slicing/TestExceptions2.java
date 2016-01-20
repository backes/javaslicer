/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestExceptions2
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestExceptions2.java
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
import de.unisb.cs.st.javaslicer.SliceEntry;
import de.unisb.cs.st.javaslicer.SliceEntryFilter;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;

public class TestExceptions2 extends AbstractSlicingTest {

    @Override
    protected SliceEntryFilter getSliceEntryFilter() {
        return new SliceEntryFilter() {
            @Override
            public boolean keepEntry(SliceEntry entry) {
                return entry.method.startsWith("de.");
            }
        };
    }

    @Test
    public void test34all() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/exceptions2", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:34:*");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:29 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:29 ASTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:33 ALOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:33 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions2.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:34 ASTORE 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:55 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:55 IFNONNULL L1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:56 NEW java/lang/NullPointerException",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:56 ATHROW",
            });
    }

    @Test
    public void test46all() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/exceptions2", "main",
            "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:46:*");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:39 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:39 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:41 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:41 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions2.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:42 ASTORE 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:43 ALOAD 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:43 INVOKEVIRTUAL java/lang/NullPointerException.getMessage()Ljava/lang/String;",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:43 ASTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:46 ALOAD 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:46 IFNULL L8",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:53 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:53 IFNONNULL L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:54 NEW java/lang/NullPointerException",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:54 DUP",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:54 LDC \"a is null\"",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:54 INVOKESPECIAL java/lang/NullPointerException.<init>(Ljava/lang/String;)V",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:54 ATHROW",
            });
    }

    @Test
    public void test49a() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/exceptions2", "main",
            "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:49:{a}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:39 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:39 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:41 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:41 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions2.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:42 ASTORE 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:43 ALOAD 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:43 INVOKEVIRTUAL java/lang/NullPointerException.getMessage()Ljava/lang/String;",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:43 ASTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:46 ALOAD 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:46 IFNULL L8",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:47 ALOAD 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:47 INVOKEVIRTUAL java/lang/String.length()I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:47 NEWARRAY T_INT",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:47 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:53 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:53 IFNONNULL L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:54 NEW java/lang/NullPointerException",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:54 DUP",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:54 LDC \"a is null\"",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:54 INVOKESPECIAL java/lang/NullPointerException.<init>(Ljava/lang/String;)V",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:54 ATHROW",
            });
    }

    @Test
    public void test36b() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/exceptions2", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:49:{b}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:39 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:39 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:41 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:41 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions2.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:44 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:44 ASTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:53 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:53 IFNONNULL L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:54 NEW java/lang/NullPointerException",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:54 ATHROW",
            });
    }

}
