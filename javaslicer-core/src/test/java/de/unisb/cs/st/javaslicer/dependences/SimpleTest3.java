package de.unisb.cs.st.javaslicer.dependences;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractDependencesTest;
import de.unisb.cs.st.javaslicer.AbstractDependencesTest.Dependence.Type;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.InstructionInstance;

public class SimpleTest3 extends AbstractDependencesTest {

    protected static class Simple3Filter implements InstructionFilter {

        public boolean filterInstance(final InstructionInstance inst) {
            return inst.getInstruction().getMethod().getReadClass().getName().endsWith("Simple3");
        }

    }
    @Test
    public void test() throws IOException, URISyntaxException {
        final Dependence[] expectedDependences = new Dependence[] {
                new Dependence("Simple3.java:10", "Simple3.java:9",  Type.RAW),
                new Dependence("Simple3.java:11", "Simple3.java:9",  Type.RAW),
                new Dependence("Simple3.java:12", "Simple3.java:10",  Type.WAR),
                new Dependence("Simple3.java:12", "Simple3.java:11", Type.WAR),
                new Dependence("Simple3.java:14", "Simple3.java:13", Type.RAW),
        };

        compareDependences(expectedDependences, "/traces/simple3", "main", new Simple3Filter());
    }

}
