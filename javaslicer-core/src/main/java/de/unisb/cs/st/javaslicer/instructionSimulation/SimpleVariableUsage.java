/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.instructionSimulation
 *    Class:     SimpleVariableUsage
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/instructionSimulation/SimpleVariableUsage.java
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

import de.unisb.cs.st.javaslicer.variables.Variable;

public class SimpleVariableUsage implements DynamicInformation {

    private final Collection<? extends Variable> usedVariables;
    private final Collection<? extends Variable> definedVariables;
    private final boolean isCatchBlock;
    private final Map<Long, Collection<? extends Variable>> createdObjects;

    public SimpleVariableUsage(final Collection<? extends Variable> usedVariables,
            final Collection<? extends Variable> definedVariables) {
        this(usedVariables, definedVariables, Collections.<Long, Collection<? extends Variable>>emptyMap());
    }
    public SimpleVariableUsage(final Collection<? extends Variable> usedVariables,
            final Collection<? extends Variable> definedVariables,
            final Map<Long, Collection<? extends Variable>> createdObjects) {
        this(usedVariables, definedVariables, false, createdObjects);
    }

    public SimpleVariableUsage(final Collection<? extends Variable> usedVariables,
            final Collection<? extends Variable> definedVariables, final boolean isCatchBlock) {
        this(usedVariables, definedVariables, isCatchBlock, Collections.<Long, Collection<? extends Variable>>emptyMap());
    }
    public SimpleVariableUsage(final Collection<? extends Variable> usedVariables,
            final Collection<? extends Variable> definedVariables, final boolean isCatchBlock,
            final Map<Long, Collection<? extends Variable>> createdObjects) {
        this.usedVariables = usedVariables;
        this.definedVariables = definedVariables;
        this.isCatchBlock = isCatchBlock;
        this.createdObjects = createdObjects;
    }

    public SimpleVariableUsage(final Variable usedVariable,
            final Variable definedVariable) {
        this(Collections.singleton(usedVariable), Collections.singleton(definedVariable));
    }

    public SimpleVariableUsage(final Collection<? extends Variable> usedVariables, final Variable definedVariable) {
        this(usedVariables, Collections.singleton(definedVariable));
    }

    public SimpleVariableUsage(final Variable usedVariable, final Collection<? extends Variable> definedVariables) {
        this(Collections.singleton(usedVariable), definedVariables);
    }

    @Override
	public Collection<? extends Variable> getUsedVariables() {
        return this.usedVariables;
    }

    @Override
	public Collection<? extends Variable> getDefinedVariables() {
        return this.definedVariables;
    }

    @Override
	public boolean isCatchBlock() {
        return this.isCatchBlock;
    }

    @Override
	public Collection<? extends Variable> getUsedVariables(final Variable definedVariable) {
        return this.usedVariables;
    }

    @Override
    public String toString() {
        return "used:    "+getUsedVariables()+System.getProperty("line.separator")
            +"defined: "+getDefinedVariables();
    }
    @Override
	public Map<Long, Collection<? extends Variable>> getCreatedObjects() {
        return this.createdObjects;
    }

}
