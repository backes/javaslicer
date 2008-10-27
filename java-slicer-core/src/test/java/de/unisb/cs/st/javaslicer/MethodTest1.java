package de.unisb.cs.st.javaslicer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;


public class MethodTest1 extends AbstractSlicingTest {

    @Test
    public void testAll() throws IllegalParameterException, IOException {
        final List<Instruction> slice = getSlice(new File("traces/method1"), "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:11:{a,b,c,d,e}");
        checkSlice(slice, new String[][] {
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ALOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "AALOAD" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "INVOKEVIRTUAL java/lang/String.charAt(I)C" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "BIPUSH 48" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ISUB" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ISTORE 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "8", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "8", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "8", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "8", "ISTORE 2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "9", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "9", "INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.getFirst(II)I" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "9", "ISTORE 3" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "10", "ILOAD 2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "10", "INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.getSecond(II)I" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "10", "ISTORE 4" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "NEWARRAY T_INT" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "DUP" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "ICONST_1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "ILOAD 2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "IASTORE" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.get(I[I)I" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "ISTORE 5" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.getFirst", "15", "ILOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.getFirst", "15", "IRETURN" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.getSecond", "19", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.getSecond", "19", "IRETURN" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.get", "23", "ALOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.get", "23", "ILOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.get", "23", "IALOAD" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.get", "23", "IRETURN" },
            });
    }

    @Test
    public void testAa() throws IllegalParameterException, IOException {
        final List<Instruction> slice = getSlice(new File("traces/method1"), "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:12:{a}");
        checkSlice(slice, new String[][] {
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ALOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "AALOAD" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "INVOKEVIRTUAL java/lang/String.charAt(I)C" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "BIPUSH 48" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ISUB" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ISTORE 1" },
            });
    }

    @Test
    public void testAb() throws IllegalParameterException, IOException {
        final List<Instruction> slice = getSlice(new File("traces/method1"), "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:7:{a}");
        checkSlice(slice, new String[][] {
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ALOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "AALOAD" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "INVOKEVIRTUAL java/lang/String.charAt(I)C" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "BIPUSH 48" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ISUB" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ISTORE 1" },
            });
    }

    @Test
    public void testC() throws IllegalParameterException, IOException {
        final List<Instruction> slice = getSlice(new File("traces/method1"), "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:12:{c}");
        checkSlice(slice, new String[][] {
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ALOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "AALOAD" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "INVOKEVIRTUAL java/lang/String.charAt(I)C" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "BIPUSH 48" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ISUB" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ISTORE 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "9", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "9", "INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.getFirst(II)I" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "9", "ISTORE 3" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.getFirst", "15", "ILOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.getFirst", "15", "IRETURN" },
            });
    }

    @Test
    public void testD() throws IllegalParameterException, IOException {
        final List<Instruction> slice = getSlice(new File("traces/method1"), "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:12:{d}");
        checkSlice(slice, new String[][] {
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ALOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "AALOAD" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "INVOKEVIRTUAL java/lang/String.charAt(I)C" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "BIPUSH 48" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ISUB" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ISTORE 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "8", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "8", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "8", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "8", "ISTORE 2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "10", "ILOAD 2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "10", "INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.getSecond(II)I" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "10", "ISTORE 4" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.getSecond", "19", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.getSecond", "19", "IRETURN" },
            });
    }

    @Test
    public void testE() throws IllegalParameterException, IOException {
        final List<Instruction> slice = getSlice(new File("traces/method1"), "main", "de.unisb.cs.st.javaslicer.tracedCode.Method1.main:12:{e}");
        checkSlice(slice, new String[][] {
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ALOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "AALOAD" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "INVOKEVIRTUAL java/lang/String.charAt(I)C" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "BIPUSH 48" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ISUB" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "7", "ISTORE 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "8", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "8", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "8", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "8", "ISTORE 2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "NEWARRAY T_INT" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "DUP" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "ICONST_1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "ILOAD 2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "IASTORE" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Method1.get(I[I)I" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.main", "11", "ISTORE 5" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.get", "23", "ALOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.get", "23", "ILOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.get", "23", "IALOAD" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Method1.get", "23", "IRETURN" },
            });
    }

}
