/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.dependences
 *    Class:     SimpleTest3
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/dependences/SimpleTest3.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.dependences;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractDependencesTest;
import de.unisb.cs.st.javaslicer.AbstractDependencesTest.Dependence.Type;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;

public class SimpleTest3 extends AbstractDependencesTest {

    protected static class Simple3Filter implements InstructionFilter {

        public boolean filterInstance(final InstructionInstance inst) {
            return inst.getInstruction().getMethod().getReadClass().getName().endsWith("Simple3");
        }

    }
    @Test
    public void test() throws IOException, URISyntaxException, InterruptedException {
        final Dependence[] expectedDependences = new Dependence[] {
                new Dependence("Simple3.java:10", "Simple3.java:9",  Type.RAW),
                new Dependence("Simple3.java:11", "Simple3.java:9",  Type.RAW),
                new Dependence("Simple3.java:12", "Simple3.java:10", Type.WAR),
                new Dependence("Simple3.java:12", "Simple3.java:11", Type.WAR),
                new Dependence("Simple3.java:14", "Simple3.java:13", Type.RAW),
        };

        compareDependences(expectedDependences, "/traces/simple3", "main", new Simple3Filter());
    }

}
