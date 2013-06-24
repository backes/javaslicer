/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult
 *    Class:     InstanceFilter
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/InstanceFilter.java
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
package de.unisb.cs.st.javaslicer.traceResult;

import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.LabelMarker;


/**
 * This interface defines filters that are used in the traversal of an execution
 * trace to filter out certain instruction instances.
 *
 * The default implementations are {@link LabelFilter} and {@link AdditionalLabelFilter}.
 *
 * @author Clemens Hammacher
 */
public interface InstanceFilter<InstanceType> {

	/**
     * The ASM library adds "labels" at each position which is a jump target, and
     * for the start and end of try-catch blocks.
     * This filter removes all those labels, since they are not part of the actual
     * application.
     *
     * @author Clemens Hammacher
     */
    public static class LabelFilter implements InstanceFilter<InstructionInstance> {

        public static LabelFilter instance = new LabelFilter();

        @Override
		public boolean filterInstance(final InstructionInstance instrInstance) {
            return instrInstance.getInstruction().getType() == InstructionType.LABEL;
        }

        private LabelFilter() {
            // singleton!
        }

    }

    /**
     * For tracing the Java application, additional labels are inserted at some places.
     * This filter filters them out, but keeps all the labels that were added by the
     * ASM library.
     *
     * @author Clemens Hammacher
     */
    public static class AdditionalLabelFilter implements InstanceFilter<InstructionInstance> {

        public static AdditionalLabelFilter instance = new AdditionalLabelFilter();

        @Override
		public boolean filterInstance(final InstructionInstance instrInstance) {
            return (instrInstance.getInstruction().getType() == InstructionType.LABEL) &&
                (((LabelMarker)instrInstance.getInstruction()).isAdditionalLabel());
        }

        private AdditionalLabelFilter() {
            // singleton!
        }

    }

    /**
     * Returns <code>true</code> if the instance should be filtered out,
     * <code>false</code> to accept the instance.
     */
    boolean filterInstance(InstanceType instance);

}
