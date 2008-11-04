package de.unisb.cs.st.javaslicer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;


public class ExceptionsTest2 extends AbstractSlicingTest {

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
                /* These three instructions to not change anything on the fact that the exception is thrown
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 DUP",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 LDC \"a is null\"",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 INVOKESPECIAL java/lang/NullPointerException.<init>(Ljava/lang/String;)V",
                */
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions2.useArrays:32 ATHROW",
            });
    }

}
