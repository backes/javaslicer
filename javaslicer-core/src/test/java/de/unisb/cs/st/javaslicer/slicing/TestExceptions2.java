/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestExceptions2
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestExceptions2.java
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

public class TestExceptions2 extends AbstractSlicingTest {

    @Test
    public void test25all() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/exceptions2", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:25:*");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:30 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:30 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:32 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:32 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions2.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:33 ASTORE 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:34 ALOAD 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:34 INVOKEVIRTUAL java/lang/NullPointerException.getMessage()Ljava/lang/String;",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:34 ASTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:44 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:44 IFNONNULL L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 NEW java/lang/NullPointerException",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 DUP",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 LDC \"a is null\"",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 INVOKESPECIAL java/lang/NullPointerException.<init>(Ljava/lang/String;)V",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 ATHROW",
                "java.lang.Exception.<init>:41 ALOAD 0",
                "java.lang.Exception.<init>:41 ALOAD 1",
                "java.lang.Exception.<init>:41 INVOKESPECIAL java/lang/Throwable.<init>(Ljava/lang/String;)V",
                "java.lang.NullPointerException.<init>:46 ALOAD 0",
                "java.lang.NullPointerException.<init>:46 ALOAD 1",
                "java.lang.NullPointerException.<init>:46 INVOKESPECIAL java/lang/RuntimeException.<init>(Ljava/lang/String;)V",
                "java.lang.RuntimeException.<init>:43 ALOAD 0",
                "java.lang.RuntimeException.<init>:43 ALOAD 1",
                "java.lang.RuntimeException.<init>:43 INVOKESPECIAL java/lang/Exception.<init>(Ljava/lang/String;)V",
                "java.lang.Throwable.<init>:197 ALOAD 0",
                "java.lang.Throwable.<init>:197 ALOAD 1",
                "java.lang.Throwable.<init>:197 PUTFIELD java/lang/Throwable.detailMessage Ljava/lang/String;",
                "java.lang.Throwable.getMessage:253 ALOAD 0",
                "java.lang.Throwable.getMessage:253 GETFIELD java/lang/Throwable.detailMessage Ljava/lang/String;",
                "java.lang.Throwable.getMessage:253 ARETURN",
            });
    }

    @Test
    public void test37all() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/exceptions2", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:37:*");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:30 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:30 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:32 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:32 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions2.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:33 ASTORE 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:34 ALOAD 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:34 INVOKEVIRTUAL java/lang/NullPointerException.getMessage()Ljava/lang/String;",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:34 ASTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:44 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:44 IFNONNULL L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 NEW java/lang/NullPointerException",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 DUP",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 LDC \"a is null\"",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 INVOKESPECIAL java/lang/NullPointerException.<init>(Ljava/lang/String;)V",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 ATHROW",
                "java.lang.Exception.<init>:41 ALOAD 0",
                "java.lang.Exception.<init>:41 ALOAD 1",
                "java.lang.Exception.<init>:41 INVOKESPECIAL java/lang/Throwable.<init>(Ljava/lang/String;)V",
                "java.lang.NullPointerException.<init>:46 ALOAD 0",
                "java.lang.NullPointerException.<init>:46 ALOAD 1",
                "java.lang.NullPointerException.<init>:46 INVOKESPECIAL java/lang/RuntimeException.<init>(Ljava/lang/String;)V",
                "java.lang.RuntimeException.<init>:43 ALOAD 0",
                "java.lang.RuntimeException.<init>:43 ALOAD 1",
                "java.lang.RuntimeException.<init>:43 INVOKESPECIAL java/lang/Exception.<init>(Ljava/lang/String;)V",
                "java.lang.Throwable.<init>:197 ALOAD 0",
                "java.lang.Throwable.<init>:197 ALOAD 1",
                "java.lang.Throwable.<init>:197 PUTFIELD java/lang/Throwable.detailMessage Ljava/lang/String;",
                "java.lang.Throwable.getMessage:253 ALOAD 0",
                "java.lang.Throwable.getMessage:253 GETFIELD java/lang/Throwable.detailMessage Ljava/lang/String;",
                "java.lang.Throwable.getMessage:253 ARETURN",
            });
    }

    @Test
    public void test27a() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/exceptions2", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:40:{a}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:30 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:30 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:32 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:32 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions2.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:33 ASTORE 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:34 ALOAD 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:34 INVOKEVIRTUAL java/lang/NullPointerException.getMessage()Ljava/lang/String;",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:34 ASTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:37 ALOAD 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:37 IFNULL L8",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:38 ALOAD 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:38 INVOKEVIRTUAL java/lang/String.length()I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:38 NEWARRAY T_INT",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:38 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:44 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:44 IFNONNULL L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 NEW java/lang/NullPointerException",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 DUP",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 LDC \"a is null\"",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 INVOKESPECIAL java/lang/NullPointerException.<init>(Ljava/lang/String;)V",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 ATHROW",
                "java.lang.Exception.<init>:41 ALOAD 0",
                "java.lang.Exception.<init>:41 ALOAD 1",
                "java.lang.Exception.<init>:41 INVOKESPECIAL java/lang/Throwable.<init>(Ljava/lang/String;)V",
                "java.lang.NullPointerException.<init>:46 ALOAD 0",
                "java.lang.NullPointerException.<init>:46 ALOAD 1",
                "java.lang.NullPointerException.<init>:46 INVOKESPECIAL java/lang/RuntimeException.<init>(Ljava/lang/String;)V",
                "java.lang.RuntimeException.<init>:43 ALOAD 0",
                "java.lang.RuntimeException.<init>:43 ALOAD 1",
                "java.lang.RuntimeException.<init>:43 INVOKESPECIAL java/lang/Exception.<init>(Ljava/lang/String;)V",
                "java.lang.Throwable.<init>:197 ALOAD 0",
                "java.lang.Throwable.<init>:197 ALOAD 1",
                "java.lang.Throwable.<init>:197 PUTFIELD java/lang/Throwable.detailMessage Ljava/lang/String;",
                "java.lang.Throwable.getMessage:253 ALOAD 0",
                "java.lang.Throwable.getMessage:253 GETFIELD java/lang/Throwable.detailMessage Ljava/lang/String;",
                "java.lang.Throwable.getMessage:253 ARETURN",
            });
    }

    @Test
    public void test27b() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/exceptions2", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:40:{b}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:30 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:30 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:32 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:32 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions2.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:35 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:35 ASTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:44 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:44 IFNONNULL L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 NEW java/lang/NullPointerException",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:45 ATHROW",
            });
    }

}
