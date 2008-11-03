package de.unisb.cs.st.javaslicer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;


public class ExceptionsTest1 extends AbstractSlicingTest {

    @Test
    public void test17c() throws IllegalParameterException, IOException, URISyntaxException {
        final List<Instruction> slice = getSlice("/traces/exceptions1", "main", "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:17:{c}");
        checkSlice(slice, new String[] {
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:7 ICONST_1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:7 NEWARRAY T_INT",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:7 ASTORE 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:8 ACONST_NULL",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:8 ASTORE 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:12 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:12 ALOAD 2",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:12 INVOKESTATIC de/unisb/cs/st/javaslicer/tracedCode/Exceptions1.useArrays([I[I)I",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:14 ICONST_1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.main:14 ISTORE 3",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:34 ALOAD 0",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:34 ARRAYLENGTH",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:34 ALOAD 1",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions1.useArrays:34 ARRAYLENGTH",
            });
    }

}
