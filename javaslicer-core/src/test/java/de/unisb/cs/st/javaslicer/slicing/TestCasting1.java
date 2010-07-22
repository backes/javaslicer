package de.unisb.cs.st.javaslicer.slicing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;


public class TestCasting1 extends AbstractSlicingTest {

    @Test
    public void test_d3() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/casting1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20:{d3}");
        checkSlice(slice, new String[] {
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:7 ALOAD 0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:7 ICONST_0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:7 AALOAD",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:7 ICONST_0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:7 INVOKEVIRTUAL java/lang/String.charAt(I)C",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:7 BIPUSH 48",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:7 ISUB",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:7 ISTORE 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:8 ILOAD 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:8 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:8 DSTORE 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:9 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:9 ILOAD 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:9 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:9 DMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:9 D2I",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:9 ISTORE 4",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:10 ILOAD 4",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:10 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:10 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:10 DMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:10 DSTORE 5",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:11 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:11 D2F",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:11 FSTORE 7",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:12 DLOAD 5",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:12 D2I",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:12 I2B",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:12 ISTORE 8",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:13 ILOAD 8",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:13 I2F",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:13 FLOAD 7",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:13 FMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:13 F2I",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:13 I2B",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:13 ISTORE 9",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:15 ILOAD 9",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:15 ILOAD 8",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:15 IMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:15 I2L",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:15 LSTORE 11",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:18 FLOAD 7",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:18 LLOAD 11",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:18 L2F",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:18 FMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:18 F2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:18 DSTORE 17",
        	});
    }

    @Test
    public void test_f2() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/casting1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:20:{f2}");
        checkSlice(slice, new String[] {
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:7 ALOAD 0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:7 ICONST_0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:7 AALOAD",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:7 ICONST_0",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:7 INVOKEVIRTUAL java/lang/String.charAt(I)C",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:7 BIPUSH 48",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:7 ISUB",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:7 ISTORE 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:8 ILOAD 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:8 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:8 DSTORE 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:9 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:9 ILOAD 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:9 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:9 DMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:9 D2I",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:9 ISTORE 4",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:10 ILOAD 4",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:10 I2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:10 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:10 DMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:10 DSTORE 5",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:11 DLOAD 2",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:11 D2F",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:11 FSTORE 7",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:16 FLOAD 7",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:16 F2L",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:16 LSTORE 13",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:17 DLOAD 5",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:17 LLOAD 13",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:17 L2D",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:17 DADD",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:17 D2L",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:17 LSTORE 15",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:19 LLOAD 15",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:19 ILOAD 1",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:19 I2L",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:19 LMUL",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:19 L2F",
        	    "de.unisb.cs.st.javaslicer.tracedCode.Casting1.main:19 FSTORE 19",
        	});
    }

}
