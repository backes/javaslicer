package de.unisb.cs.st.javaslicer.traceResult;

import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Type;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;


/**
 * This interface defines filters that are used in the traversal of an execution
 * trace to filter out certain instances.
 *
 * The default implementations are {@link LabelFilter} and {@link AdditionalLabelFilter}.
 *
 * @author Clemens Hammacher
 */
public interface InstanceFilter {

    public static class LabelFilter implements InstanceFilter {

        public static LabelFilter instance = new LabelFilter();

        /**
         * Returns true if the instance should be filtered out
         */
        public boolean filterInstance(Instance instance) {
            return instance.getInstruction().getType() == Type.LABEL;
        }

        private LabelFilter() {
            // singleton!
        }

    }

    public static class AdditionalLabelFilter implements InstanceFilter {

        public static AdditionalLabelFilter instance = new AdditionalLabelFilter();

        public boolean filterInstance(Instance instance) {
            return (instance.getInstruction().getType() == Type.LABEL) &&
                (((LabelMarker)instance.getInstruction()).isAdditionalLabel());
        }

        private AdditionalLabelFilter() {
            // singleton!
        }

    }

    boolean filterInstance(Instruction.Instance instance);

}
