/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     TestExceptions7
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/slicing/TestExceptions7.java
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
package de.unisb.cs.st.javaslicer.slicing;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;

public class TestExceptions7 extends AbstractSlicingTest {

    @Test
    public void test() throws IllegalArgumentException, IOException {
        final List<Instruction> slice = getSlice("/traces/exceptions7", "main",
                "de.unisb.cs.st.javaslicer.tracedCode.Exceptions7.main:39");
        checkSlice(
            slice,
            new String[] {
                    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions7.main:31 LCONST_1",
                    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions7.main:31 LSTORE 1",
                    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions7.main:35 LDC 2147483647",
                    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions7.main:35 LDC 2",
                    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions7.main:35 LLOAD 1",
                    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions7.main:35 LMUL",
                    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions7.main:35 INVOKESTATIC java/lang/Math.min(JJ)J",
                    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions7.main:35 DUP2",
                    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions7.main:35 LSTORE 1",
                    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions7.main:35 L2I",
                    "de.unisb.cs.st.javaslicer.tracedCode.Exceptions7.main:35 NEWARRAY T_INT",
            });
    }

}
