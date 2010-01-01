package de.unisb.cs.st.javaslicer.slicing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;


public class MethodTest1 extends AbstractSlicingTest {

    @Test
    public void testAll() throws IllegalArgumentException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/method1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11:{a,b,c,d,e}");
        checkSlice(slice, new String[] {
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ALOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 AALOAD",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 BIPUSH 48",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ISUB",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ISTORE 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:8 ICONST_2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:8 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:8 IMUL",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:8 ISTORE 2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:9 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:9 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.getFirst(II)I",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:9 ISTORE 3",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:10 ILOAD 2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:10 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.getSecond(II)I",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:10 ISTORE 4",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 ICONST_2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 NEWARRAY T_INT",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 DUP",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 ICONST_1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 ILOAD 2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 IASTORE",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.get(I[I)I",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 ISTORE 5",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.getFirst:15 ILOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.getFirst:15 IRETURN",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.getSecond:19 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.getSecond:19 IRETURN",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.get:23 ALOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.get:23 ILOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.get:23 IALOAD",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.get:23 IRETURN",
            });
    }

    @Test
    public void testAa() throws IllegalArgumentException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/method1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:12:{a}");
        checkSlice(slice, new String[] {
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ALOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 AALOAD",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 BIPUSH 48",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ISUB",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ISTORE 1",
            });
    }

    @Test
    public void testAb() throws IllegalArgumentException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/method1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7:{a}");
        checkSlice(slice, new String[] {
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ALOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 AALOAD",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 BIPUSH 48",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ISUB",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ISTORE 1",
            });
    }

    @Test
    public void testC() throws IllegalArgumentException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/method1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:12:{c}");
        checkSlice(slice, new String[] {
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ALOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 AALOAD",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 BIPUSH 48",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ISUB",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ISTORE 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:9 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:9 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.getFirst(II)I",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:9 ISTORE 3",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.getFirst:15 ILOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.getFirst:15 IRETURN",
            });
    }

    @Test
    public void testD() throws IllegalArgumentException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/method1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:12:{d}");
        checkSlice(slice, new String[] {
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ALOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 AALOAD",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 BIPUSH 48",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ISUB",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ISTORE 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:8 ICONST_2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:8 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:8 IMUL",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:8 ISTORE 2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:10 ILOAD 2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:10 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.getSecond(II)I",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:10 ISTORE 4",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.getSecond:19 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.getSecond:19 IRETURN",
            });
    }

    @Test
    public void testE() throws IllegalArgumentException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/method1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:12:{e}");
        checkSlice(slice, new String[] {
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ALOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 AALOAD",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ICONST_0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 BIPUSH 48",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ISUB",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7 ISTORE 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:8 ICONST_2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:8 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:8 IMUL",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:8 ISTORE 2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 ILOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 ICONST_2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 NEWARRAY T_INT",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 DUP",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 ICONST_1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 ILOAD 2",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 IASTORE",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.get(I[I)I",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11 ISTORE 5",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.get:23 ALOAD 1",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.get:23 ILOAD 0",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.get:23 IALOAD",
                 "de.unisb.cs.st.javaslicer.tracedCode.Method1.get:23 IRETURN",
            });
    }

}
