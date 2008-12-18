package de.unisb.cs.st.javaslicer.slicing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.IllegalParameterException;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;


public class SimpleSlicingTest1 extends AbstractSlicingTest {

    @Test
    public void test1() throws IllegalParameterException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/simple1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7:{a}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ISTORE 1",
            });
    }

    @Test
    public void test2a() throws IllegalParameterException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/simple1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8:{b}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ISTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 ISTORE 2",
            });
    }

    @Test
    public void test2b() throws IllegalParameterException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/simple1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8:{b,c,d}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ISTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 ISTORE 2",
            });
    }

    @Test
    public void test2c() throws IllegalParameterException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/simple1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8:{a,b}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ISTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 ISTORE 2",
            });
    }

    @Test
    public void test3() throws IllegalParameterException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/simple1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:9:{c}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ISTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 ISTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:9 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:9 ILOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:9 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:9 ISTORE 3",
            });
    }

    @Test
    public void test4() throws IllegalParameterException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/simple1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:10:{d}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:7 ISTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:8 ISTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:9 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:9 ILOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:9 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:9 ISTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:10 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:10 ILOAD 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:10 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:10 ISTORE 4",
            });
    }

}
