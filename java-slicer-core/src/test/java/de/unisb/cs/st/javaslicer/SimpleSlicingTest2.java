package de.unisb.cs.st.javaslicer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;


public class SimpleSlicingTest2 extends AbstractSlicingTest {

    @Test
    public void testAll() throws IllegalParameterException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/simple2", "main", "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:11:{a,b,c,d,e}");
        checkSlice(slice, new String[][] {
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ALOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "AALOAD" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "INVOKEVIRTUAL java/lang/String.charAt(I)C" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "BIPUSH 48" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ISUB" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ISTORE 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "8", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "8", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "8", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "8", "ISTORE 2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "9", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "9", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "9", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "9", "ISTORE 3" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "10", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "10", "ILOAD 3" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "10", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "10", "ISTORE 4" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "11", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "11", "ILOAD 2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "11", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "11", "ISTORE 5" },
            });
    }

    @Test
    public void testA() throws IllegalParameterException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/simple2", "main", "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:7:{a}");
        checkSlice(slice, new String[][] {
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ALOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "AALOAD" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "INVOKEVIRTUAL java/lang/String.charAt(I)C" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "BIPUSH 48" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ISUB" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ISTORE 1" },
            });
    }

    @Test
    public void testC() throws IllegalParameterException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/simple2", "main", "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:9:{c}");
        checkSlice(slice, new String[][] {
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ALOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "AALOAD" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "INVOKEVIRTUAL java/lang/String.charAt(I)C" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "BIPUSH 48" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ISUB" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ISTORE 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "9", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "9", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "9", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "9", "ISTORE 3" },
            });
    }

    @Test
    public void testE() throws IllegalParameterException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/simple2", "main", "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:11:{e}");
        checkSlice(slice, new String[][] {
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ALOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "AALOAD" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "INVOKEVIRTUAL java/lang/String.charAt(I)C" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "BIPUSH 48" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ISUB" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ISTORE 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "8", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "8", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "8", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "8", "ISTORE 2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "11", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "11", "ILOAD 2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "11", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "11", "ISTORE 5" },
            });
    }

    @Test
    public void testCundE() throws IllegalParameterException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/simple2", "main", "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main:11:{c,e}");
        checkSlice(slice, new String[][] {
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ALOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "AALOAD" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "INVOKEVIRTUAL java/lang/String.charAt(I)C" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "BIPUSH 48" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ISUB" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "7", "ISTORE 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "8", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "8", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "8", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "8", "ISTORE 2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "9", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "9", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "9", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "9", "ISTORE 3" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "11", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "11", "ILOAD 2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "11", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Simple2.main", "11", "ISTORE 5" },
            });
    }

}
