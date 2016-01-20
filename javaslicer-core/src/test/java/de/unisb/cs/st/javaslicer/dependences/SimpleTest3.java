/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.dependences
 *    Class:     SimpleTest3
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/dependences/SimpleTest3.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
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

        @Override
		public boolean filterInstance(InstructionInstance inst) {
            return inst.getInstruction().getMethod().getReadClass().getName().endsWith("Simple3");
        }

    }
    @Test
    public void test() throws IOException {
        final Dependence[] expectedDependences = new Dependence[] {
                new Dependence("Simple3.java:32", "Simple3.java:31",  Type.RAW),
                new Dependence("Simple3.java:33", "Simple3.java:31",  Type.RAW),
                new Dependence("Simple3.java:34", "Simple3.java:32", Type.WAR),
                new Dependence("Simple3.java:34", "Simple3.java:33", Type.WAR),
                new Dependence("Simple3.java:36", "Simple3.java:35", Type.RAW),
        };

        compareDependences(expectedDependences, "/traces/simple3", "main", new Simple3Filter());
    }

}
