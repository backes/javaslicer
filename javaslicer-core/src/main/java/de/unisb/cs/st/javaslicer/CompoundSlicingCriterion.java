package de.unisb.cs.st.javaslicer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.instructionSimulation.ExecutionFrame;
import de.unisb.cs.st.javaslicer.variables.Variable;

public class CompoundSlicingCriterion implements SlicingCriterion {

    public static class Instance implements SlicingCriterionInstance {

        private final List<SlicingCriterionInstance> instances;
        public Instance(CompoundSlicingCriterion compCrit) {
            this.instances = new ArrayList<SlicingCriterionInstance>(compCrit.criteria.size());
            for (final SlicingCriterion crit: compCrit.criteria)
                this.instances.add(crit.getInstance());
        }

        public Collection<Variable> getInterestingVariables(final ExecutionFrame execFrame) {
            final Set<Variable> interestingVariables = new HashSet<Variable>();
            for (final SlicingCriterionInstance crit: this.instances) {
                interestingVariables.addAll(crit.getInterestingVariables(execFrame));
            }
            return interestingVariables;
        }

        public Collection<Instruction> getInterestingInstructions(final ExecutionFrame currentFrame) {
            final Set<Instruction> interestingInstructions = new HashSet<Instruction>();
            for (final SlicingCriterionInstance crit: this.instances) {
                interestingInstructions.addAll(crit.getInterestingInstructions(currentFrame));
            }
            return interestingInstructions;
        }

        public boolean matches(final InstructionInstance instructionInstance) {
            for (final SlicingCriterionInstance crit: this.instances) {
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

    public SlicingCriterionInstance getInstance() {
        return new Instance(this);
    }

    @Override
    public String toString() {
        final Iterator<SlicingCriterion> it = this.criteria.iterator();
        final StringBuilder sb = new StringBuilder();
        if (it.hasNext())
            sb.append(it.next());
        while (it.hasNext())
            sb.append(',').append(it.next());
        return sb.toString();
    }

}
