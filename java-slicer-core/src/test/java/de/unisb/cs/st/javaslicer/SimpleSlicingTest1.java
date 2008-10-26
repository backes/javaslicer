package de.unisb.cs.st.javaslicer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;


public class SimpleSlicingTest1 extends AbstractSlicingTest {

    @Test
    public void test2a() throws IllegalParameterException, IOException {
        final List<Instruction> slice = getSlice(new File("traces/simple1"), "main", "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8:{b}");
        checkSlice(slice, new String[][] {
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "ALOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "AALOAD" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "INVOKEVIRTUAL java/lang/String.charAt(I)C" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "BIPUSH 48" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "ISUB" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "ISTORE 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "8", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "8", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "8", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "8", "ISTORE 2" },
            });
    }

    @Test
    public void test2b() throws IllegalParameterException, IOException {
        final List<Instruction> slice = getSlice(new File("traces/simple1"), "main", "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8:{b,c,d}");
        checkSlice(slice, new String[][] {
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "ALOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "AALOAD" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "INVOKEVIRTUAL java/lang/String.charAt(I)C" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "BIPUSH 48" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "ISUB" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "ISTORE 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "8", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "8", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "8", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "8", "ISTORE 2" },
            });
    }

    @Test
    public void test4() throws IllegalParameterException, IOException {
        final List<Instruction> slice = getSlice(new File("traces/simple1"), "main", "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:10:{d}");
        checkSlice(slice, new String[][] {
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "ALOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "AALOAD" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "INVOKEVIRTUAL java/lang/String.charAt(I)C" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "BIPUSH 48" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "ISUB" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "7", "ISTORE 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "8", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "8", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "8", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "8", "ISTORE 2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "9", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "9", "ILOAD 2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "9", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "9", "ISTORE 3" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "10", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "10", "ILOAD 3" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "10", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main", "10", "ISTORE 4" },
            });
    }

}
