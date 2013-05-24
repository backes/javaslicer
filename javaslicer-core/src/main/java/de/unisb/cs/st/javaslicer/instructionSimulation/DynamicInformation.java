/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.instructionSimulation
 *    Class:     DynamicInformation
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/instructionSimulation/DynamicInformation.java
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
package de.unisb.cs.st.javaslicer.instructionSimulation;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.javaslicer.variables.Variable;

public interface DynamicInformation {

    // some constants
    public static final Set<Variable> EMPTY_VARIABLE_SET = Collections.emptySet();
    public static final DynamicInformation CATCHBLOCK = new SimpleVariableUsage(EMPTY_VARIABLE_SET, EMPTY_VARIABLE_SET, true);
    public static final DynamicInformation EMPTY = new SimpleVariableUsage(EMPTY_VARIABLE_SET, EMPTY_VARIABLE_SET);

    // methods
    public Collection<? extends Variable> getUsedVariables();

    public Collection<? extends Variable> getDefinedVariables();

    public Collection<? extends Variable> getUsedVariables(Variable definedVariable);

    public Map<Long, Collection<? extends Variable>> getCreatedObjects();

    public boolean isCatchBlock();

}
