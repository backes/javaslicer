package de.unisb.cs.st.javaslicer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;

public class CompoundSlicingCriterion implements SlicingCriterion {

    public class Instance implements SlicingCriterion.Instance {

        private final List<SlicingCriterion.Instance> instances;
        public Instance() {
            this.instances = new ArrayList<SlicingCriterion.Instance>(CompoundSlicingCriterion.this.criteria.size());
            for (final SlicingCriterion crit: CompoundSlicingCriterion.this.criteria)
                this.instances.add(crit.getInstance());
        }

        @Override
        public Collection<Variable> getInterestingVariables(final ExecutionFrame execFrame) {
            final Set<Variable> interestingVariables = new HashSet<Variable>();
            for (final SlicingCriterion.Instance crit: this.instances) {
                interestingVariables.addAll(crit.getInterestingVariables(execFrame));
            }
            return interestingVariables;
        }

        @Override
        public boolean matches(final Instruction.Instance instructionInstance) {
            for (final SlicingCriterion.Instance crit: this.instances) {
                if (crit.matches(instructionInstance))
                    return true;
            }
            return false;
        }

    }

    protected final List<SlicingCriterion> criteria = new ArrayList<SlicingCriterion>(2);

    public void add(final SlicingCriterion slicingCriterion) {
        this.criteria.add(slicingCriterion);
    }

    @Override
    public SlicingCriterion.Instance getInstance() {
        return new Instance();
    }

}
