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

	public Collection<? extends Variable> getUsedVariables() {
		Collection<? extends Variable> oldUsed = this.dynInfo.getUsedVariables();
		if (oldUsed.isEmpty() || oldUsed == this.usedVars)
			return this.usedVars;
		HashSet<Variable> union = new HashSet<Variable>(oldUsed);
		union.addAll(this.usedVars);
		return union;
	}

	public Collection<? extends Variable> getDefinedVariables() {
		Collection<? extends Variable> oldDef = this.dynInfo.getDefinedVariables();
		if (oldDef.isEmpty())
			return Collections.singleton(this.definedVar);
		HashSet<Variable> union = new HashSet<Variable>(oldDef);
		union.add(this.definedVar);
		return union;
	}

	public Collection<? extends Variable> getUsedVariables(Variable definedVariable) {
		if (definedVariable.equals(this.definedVar))
			return this.usedVars;
		return this.dynInfo.getUsedVariables(definedVariable);
	}

	public Map<Long, Collection<? extends Variable>> getCreatedObjects() {
		return this.dynInfo.getCreatedObjects();
	}

	public boolean isCatchBlock() {
		return this.dynInfo.isCatchBlock();
	}

}
