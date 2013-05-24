/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.instructionSimulation
 *    Class:     AdditionalDataDependence
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/instructionSimulation/AdditionalDataDependence.java
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
import java.util.HashSet;
import java.util.Map;

import de.unisb.cs.st.javaslicer.variables.Variable;


public class AdditionalDataDependence implements DynamicInformation {

	private final DynamicInformation dynInfo;
	private final Variable definedVar;
	private final Collection<? extends Variable> usedVars;

	private AdditionalDataDependence(DynamicInformation dynInfo,
			Variable definedVar, Collection<? extends Variable> usedVars) {
		this.dynInfo = dynInfo;
		this.definedVar = definedVar;
		this.usedVars = usedVars;
	}

	public static DynamicInformation annotate(DynamicInformation dynInfo,
			Variable definedVar, Collection<? extends Variable> usedVars) {
		assert (!dynInfo.getDefinedVariables().contains(definedVar));
		return new AdditionalDataDependence(dynInfo, definedVar, usedVars);
	}

	@Override
	public Collection<? extends Variable> getUsedVariables() {
		Collection<? extends Variable> oldUsed = this.dynInfo.getUsedVariables();
		if (oldUsed.isEmpty() || oldUsed == this.usedVars)
			return this.usedVars;
		HashSet<Variable> union = new HashSet<Variable>(oldUsed);
		union.addAll(this.usedVars);
		return union;
	}

	@Override
	public Collection<? extends Variable> getDefinedVariables() {
		Collection<? extends Variable> oldDef = this.dynInfo.getDefinedVariables();
		if (oldDef.isEmpty())
			return Collections.singleton(this.definedVar);
		HashSet<Variable> union = new HashSet<Variable>(oldDef);
		union.add(this.definedVar);
		return union;
	}

	@Override
	public Collection<? extends Variable> getUsedVariables(Variable definedVariable) {
		if (definedVariable.equals(this.definedVar))
			return this.usedVars;
		return this.dynInfo.getUsedVariables(definedVariable);
	}

	@Override
	public Map<Long, Collection<? extends Variable>> getCreatedObjects() {
		return this.dynInfo.getCreatedObjects();
	}

	@Override
	public boolean isCatchBlock() {
		return this.dynInfo.isCatchBlock();
	}

}
