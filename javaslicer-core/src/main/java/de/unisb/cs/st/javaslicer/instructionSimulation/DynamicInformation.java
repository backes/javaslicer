/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.instructionSimulation
 *    Class:     DynamicInformation
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/instructionSimulation/DynamicInformation.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
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
