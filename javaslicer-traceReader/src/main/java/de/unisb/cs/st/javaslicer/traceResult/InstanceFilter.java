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
 * trace to filter out certain instances.
 *
 * The default implementations are {@link LabelFilter} and {@link AdditionalLabelFilter}.
 *
 * @author Clemens Hammacher
 */
public interface InstanceFilter<InstanceType> {

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
     * Returns <code>true</code> if the instance should be filtered out.
     * <code>false</code> to accept.
     */
    boolean filterInstance(InstanceType instance);

}
