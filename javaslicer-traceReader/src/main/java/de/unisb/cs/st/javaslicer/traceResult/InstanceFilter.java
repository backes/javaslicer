/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult
 *    Class:     InstanceFilter
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/InstanceFilter.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.traceResult;

import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;


/**
 * This interface defines filters that are used in the traversal of an execution
 * trace to filter out certain instances.
 *
 * The default implementations are {@link LabelFilter} and {@link AdditionalLabelFilter}.
 *
 * @author Clemens Hammacher
 */
public interface InstanceFilter<InstanceType> {

    public static class LabelFilter implements InstanceFilter<InstructionInstance> {

        public static LabelFilter instance = new LabelFilter();

        public boolean filterInstance(final InstructionInstance instrInstance) {
            return instrInstance.getInstruction().getType() == InstructionType.LABEL;
        }

        private LabelFilter() {
            // singleton!
        }

    }

    public static class AdditionalLabelFilter implements InstanceFilter<InstructionInstance> {

        public static AdditionalLabelFilter instance = new AdditionalLabelFilter();

        public boolean filterInstance(final InstructionInstance instrInstance) {
            return (instrInstance.getInstruction().getType() == InstructionType.LABEL) &&
                (((LabelMarker)instrInstance.getInstruction()).isAdditionalLabel());
        }

        private AdditionalLabelFilter() {
            // singleton!
        }

    }

    /**
     * Returns <code>true</code> if the instance should be filtered out.
     * <code>false</code> to accept.
     */
    boolean filterInstance(InstanceType instance);

}
