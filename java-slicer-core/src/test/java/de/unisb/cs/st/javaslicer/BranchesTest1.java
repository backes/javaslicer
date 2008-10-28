package de.unisb.cs.st.javaslicer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;


public class BranchesTest1 extends AbstractSlicingTest {

    @Test
    public void testAll() throws IllegalParameterException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/branches1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:17:{a,b,c,d,e,f}");
        checkSlice(slice, new String[][] {
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "7", "ALOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "7", "AALOAD" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "7", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "7", "INVOKEVIRTUAL java/lang/String.charAt(I)C" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "7", "BIPUSH 48" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "7", "ISUB" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "7", "ISTORE 1" }, // definition of a (== 1)

                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "9", "ICONST_2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "9", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "9", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "9", "ISTORE 2" }, // definition of b (== 2)

                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "10", "ICONST_3" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "10", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "10", "IMUL" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "10", "ISTORE 3" }, // definition of c (== 3)

                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "11", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "11", "ICONST_5" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "11", "IF_ICMPGE L0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "11", "ILOAD 2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "11", "GOTO L1" },
                //new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "11", "ILOAD 3" }, // instruction after ":" unused
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "11", "ISTORE 4" }, // definition of d

                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "13", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "13", "ICONST_1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "13", "IF_ICMPNE L2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "13", "ICONST_1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "13", "GOTO L3" },
                //new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "13", "ICONST_0" }, // false is never used here
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "13", "ISTORE 5" }, // definition of true0 (== true)

                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "14", "ILOAD 5" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "14", "ILOAD 2" },
                // new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "14", "ILOAD 3" }, // 3rd parameter is never used
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "14", "INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Branches1.get(ZII)I" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "14", "ISTORE 6" }, // definition of e

                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "16", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "16", "IFNE L4" }, // this branch is taken
                //new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "16", "ICONST_1" }, // so these instructions are
                //new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "16", "GOTO L4" },  // never executed
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "16", "ICONST_0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "16", "ISTORE 7" }, // definition of false0 (== false)

                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "17", "ILOAD 7" },
                //new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "17", "ILOAD 2" }, // 2nd parameter is never used
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "17", "ILOAD 3" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "17", "INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Branches1.get(ZII)I" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main", "17", "ISTORE 8" }, // definition of f

                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get", "21", "ILOAD 0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get", "21", "IFEQ L0" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get", "22", "ILOAD 1" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get", "22", "IRETURN" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get", "23", "ILOAD 2" },
                new String[] { "de.unisb.cs.st.javaslicer.tracedCode.Branches1.get", "23", "IRETURN" },
            });
    }

}
