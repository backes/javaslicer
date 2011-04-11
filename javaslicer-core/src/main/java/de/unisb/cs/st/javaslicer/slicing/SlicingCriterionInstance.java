/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.slicing
 *    Class:     SlicingCriterionInstance
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/slicing/SlicingCriterionInstance.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.slicing;

import java.util.List;

import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.LocalVariable;


public interface SlicingCriterionInstance {

	boolean matches(InstructionInstance instructionInstance);

	List<LocalVariable> getLocalVariables();

	boolean hasLocalVariables();

	boolean matchAllData();

	long getOccurenceNumber();

}
