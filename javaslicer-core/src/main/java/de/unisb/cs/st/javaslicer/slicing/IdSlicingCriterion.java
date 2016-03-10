package de.unisb.cs.st.javaslicer.slicing;

import java.util.List;

import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.LocalVariable;

public class IdSlicingCriterion implements SlicingCriterionInstance{

	private int id;

	public IdSlicingCriterion(int id) {
		this.id = id;
	}
	
	@Override
	public boolean matches(InstructionInstance instructionInstance) {
		return instructionInstance.getId() == this.id;
	}

	@Override
	public List<LocalVariable> getLocalVariables() {
		return null;
	}

	@Override
	public boolean hasLocalVariables() {
		return false;
	}


	@Override
	public long getOccurenceNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean computeTransitiveClosure() {
		// TODO Auto-generated method stub
		return false;
	}

}
