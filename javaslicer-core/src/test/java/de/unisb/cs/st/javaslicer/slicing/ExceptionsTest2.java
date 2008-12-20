package de.unisb.cs.st.javaslicer.slicing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.IllegalParameterException;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;


public class ExceptionsTest2 extends AbstractSlicingTest {

    // TODO include these tests
    @Ignore
    @Test
    public void test24() throws IllegalParameterException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/exceptions2", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:24");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:17 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:17 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:19 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:19 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions2.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:20 ASTORE 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:21 ALOAD 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:21 INVOKEVIRTUAL java/lang/NullPointerException.getMessage()Ljava/lang/String;",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:21 ASTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:31 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:31 IFNONNULL L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 NEW java/lang/NullPointerException",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 DUP",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 LDC \"a is null\"",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 INVOKESPECIAL java/lang/NullPointerException.<init>(Ljava/lang/String;)V",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 ATHROW",
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

    @Ignore
    @Test
    public void test27a() throws IllegalParameterException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/exceptions2", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:27:{a}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:17 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:17 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:19 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:19 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions2.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:20 ASTORE 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:21 ALOAD 4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:21 INVOKEVIRTUAL java/lang/NullPointerException.getMessage()Ljava/lang/String;",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:21 ASTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:24 ALOAD 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:24 IFNULL L4",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:25 ALOAD 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:25 INVOKEVIRTUAL java/lang/String.length()I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:25 NEWARRAY T_INT",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:25 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:31 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:31 IFNONNULL L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 NEW java/lang/NullPointerException",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 DUP",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 LDC \"a is null\"",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 INVOKESPECIAL java/lang/NullPointerException.<init>(Ljava/lang/String;)V",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 ATHROW",
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

    @Ignore
    @Test
    public void test27b() throws IllegalParameterException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/exceptions2", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:27:{b}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:17 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:17 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:19 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:19 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions2.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:22 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.main:22 ASTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:31 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:31 IFNONNULL L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 NEW java/lang/NullPointerException",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 ATHROW",
            });
    }

}
