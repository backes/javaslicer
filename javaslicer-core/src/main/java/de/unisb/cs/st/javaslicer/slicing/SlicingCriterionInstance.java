/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     SlicingCriterionInstance
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/slicing/SlicingCriterionInstance.java
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

import java.util.List;

import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.LocalVariable;


/**
 * A dynamic instance of a slicing criterion.
 *
 * There are three forms of slicing criteria:
 * <ol>
 * <li>slice only for control dependences
 *    (hasLocalVariables() == false and computeTransitiveClosure() == false)</li>
 * <li>slice for specific local variables
 *    (hasLocalVariables() == true and computeTransitiveClosure() == false)</li>
 * <li>compute the full transitive closure (data and control dependences)
 *    (hasLocalVariables() == false and computeTransitiveClosure() == true)</li>
 * </ol>
 *
 * @author Clemens Hammacher
 */
public interface SlicingCriterionInstance {

	boolean matches(InstructionInstance instructionInstance);

	List<LocalVariable> getLocalVariables();

	boolean hasLocalVariables();

	boolean computeTransitiveClosure();

	long getOccurenceNumber();

}
