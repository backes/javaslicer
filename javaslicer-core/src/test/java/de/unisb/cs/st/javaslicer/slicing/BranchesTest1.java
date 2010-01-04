package de.unisb.cs.st.javaslicer.slicing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;


public class BranchesTest1 extends AbstractSlicingTest {

    @Test
    public void testAll() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/branches1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:17:{a,b,c,d,e,f}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 ISTORE 1", // definition of a (== 1)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:9 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:9 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:9 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:9 ISTORE 2", // definition of b (== 2)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:10 ICONST_3",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:10 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:10 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:10 ISTORE 3", // definition of c (== 3)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 ICONST_5",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 IF_ICMPGE L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 ILOAD 2",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 GOTO L1",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 ILOAD 3", // instruction after ":" unused
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 ISTORE 4", // definition of d

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:13 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:13 ICONST_1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:13 IF_ICMPNE L2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:13 ICONST_1",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:13 GOTO L3",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:13 ICONST_0", // false is never used here
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:13 ISTORE 5", // definition of true0 (== true)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:14 ILOAD 5",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:14 ILOAD 2",
                // "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:14 ILOAD 3", // 3rd parameter is never used
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:14 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Branches1.get(ZII)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:14 ISTORE 6", // definition of e

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:16 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:16 IFNE L4", // this branch is taken
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:16 ICONST_1", // so these instructions are
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:16 GOTO L4",  // never executed
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:16 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:16 ISTORE 7", // definition of false0 (== false)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:17 ILOAD 7",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:17 ILOAD 2", // 2nd parameter is never used
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:17 ILOAD 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:17 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Branches1.get(ZII)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:17 ISTORE 8", // definition of f

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:21 ILOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:21 IFEQ L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:22 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:22 IRETURN",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:23 ILOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get:23 IRETURN",
            });
    }

    @Test
    public void test17() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/branches1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:17:{b,c,false0}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 ISTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:9 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:9 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:9 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:9 ISTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:10 ICONST_3",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:10 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:10 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:10 ISTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:16 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:16 IFNE L4",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:16 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:16 ISTORE 7",
            });
    }

    @Test
    public void testD() throws IllegalArgumentException, IOException, URISyntaxException, InterruptedException {
        final List<Instruction> slice = getSlice("/traces/branches1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11:{d}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 AALOAD",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 ICONST_0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 INVOKEVIRTUAL java/lang/String.charAt(I)C",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 BIPUSH 48",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 ISUB",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:7 ISTORE 1", // definition of a (== 1)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:9 ICONST_2",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:9 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:9 IMUL",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:9 ISTORE 2", // definition of b (== 2)

                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 ILOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 ICONST_5",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 IF_ICMPGE L0",
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 ILOAD 2",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 GOTO L1",
                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 ILOAD 3", // instruction after ":" unused
                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 ISTORE 4", // definition of d
            });
    }

}
