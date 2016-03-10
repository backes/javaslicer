package de.unisb.cs.st.javaslicer.slicing;

import java.util.LinkedList;
import java.util.List;

public class DynamicSlicingCriterion implements SlicingCriterion {

	private IdSlicingCriterion instance;

	public DynamicSlicingCriterion(int id) {
		this.instance = new IdSlicingCriterion(id);
	}
	
	@Override
	public SlicingCriterionInstance getInstance() {
		return instance;
	}

	public static List<SlicingCriterion> parseAll(String slicingCriterionString) {
		List<SlicingCriterion> ls = new LinkedList<SlicingCriterion>();
		for (String id : slicingCriterionString.split(",")) {
			ls.add(new DynamicSlicingCriterion(Integer.parseInt(id)));
		}
		return ls;
	}

}
