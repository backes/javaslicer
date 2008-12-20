package de.unisb.cs.st.javaslicer.dependencies;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractDependenciesTest;
import de.unisb.cs.st.javaslicer.AbstractDependenciesTest.Dependency.Type;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance;

public class SimpleTest3 extends AbstractDependenciesTest {

    protected static class Simple3Filter implements InstructionFilter {

        public boolean filterInstance(Instance inst) {
            return inst.getInstruction().getMethod().getReadClass().getName().endsWith("Simple3");
        }

    }
    @Test
    public void test() throws IOException, URISyntaxException {
        Dependency[] expectedDependencies = new Dependency[] {
                new Dependency("Simple3.java:10", "Simple3.java:9",  Type.RAW),
                new Dependency("Simple3.java:11", "Simple3.java:9",  Type.RAW),
                new Dependency("Simple3.java:12", "Simple3.java:10",  Type.WAR),
                new Dependency("Simple3.java:12", "Simple3.java:11", Type.WAR),
                new Dependency("Simple3.java:14", "Simple3.java:13", Type.RAW),
        };

        compareDependencies(expectedDependencies, "/traces/simple3", "main", new Simple3Filter());
    }

}
