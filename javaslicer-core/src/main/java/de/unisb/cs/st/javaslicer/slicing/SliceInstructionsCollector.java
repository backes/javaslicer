/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     SliceInstructionsCollector
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/slicing/SliceInstructionsCollector.java
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.variables.Variable;


public class SliceInstructionsCollector implements SliceVisitor {

    private final Set<Instruction> dynamicSlice = new HashSet<Instruction>();

    @Override
	public void visitMatchedInstance(InstructionInstance instance) {
        this.dynamicSlice.add(instance.getInstruction());
    }

    @Override
	public void visitSliceDependence(InstructionInstance from,
    		InstructionInstance to, Variable variable, int distance) {
        this.dynamicSlice.add(to.getInstruction());
    }

    public Set<Instruction> getDynamicSlice() {
        return Collections.unmodifiableSet(this.dynamicSlice);
    }

}
