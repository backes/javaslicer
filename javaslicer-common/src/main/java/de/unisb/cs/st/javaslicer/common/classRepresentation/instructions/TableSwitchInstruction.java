/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.classRepresentation.instructions
 *    Class:     TableSwitchInstruction
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/classRepresentation/instructions/TableSwitchInstruction.java
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
package de.unisb.cs.st.javaslicer.common.classRepresentation.instructions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.StringCacheInput;
import de.hammacher.util.StringCacheOutput;
import de.hammacher.util.streams.OptimizedDataInputStream;
import de.hammacher.util.streams.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionType;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod.MethodReadInformation;

/**
 * Class representing a TABLESWITCH instruction.
 *
 * @author Clemens Hammacher
 */
public class TableSwitchInstruction extends AbstractInstruction {

    private LabelMarker defaultHandler;
    private LabelMarker[] handlers;
    private final int min;

    public TableSwitchInstruction(final ReadMethod readMethod, final int lineNumber, final int min,
            @SuppressWarnings("unused") final int max, final LabelMarker defaultHandler, final LabelMarker[] handlers) {
        super(readMethod, Opcodes.TABLESWITCH, lineNumber);
        // in initialization, defaultHandler and handlers is null...
        //assert min + handlers.length - 1 == max;
        this.min = min;
        this.defaultHandler = defaultHandler;
        this.handlers = handlers;
    }

    private TableSwitchInstruction(final ReadMethod readMethod, final int min,
            final int lineNumber, final LabelMarker defaultHandler, final LabelMarker[] handlers, final int index) {
        super(readMethod, Opcodes.TABLESWITCH, lineNumber, index);
        this.min = min;
        this.defaultHandler = defaultHandler;
        this.handlers = handlers;
    }

    public LabelMarker getDefaultHandler() {
        return this.defaultHandler;
    }

    public void setDefaultHandler(final LabelMarker defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    public LabelMarker[] getHandlers() {
        return this.handlers;
    }

    public void setHandlers(final LabelMarker[] handlers) {
        this.handlers = handlers;
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.min + this.handlers.length - 1;
    }

    @Override
	public InstructionType getType() {
        return InstructionType.TABLESWITCH;
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        OptimizedDataOutputStream.writeInt0(this.min, out);
        OptimizedDataOutputStream.writeInt0(this.defaultHandler.getLabelNr(), out);
        OptimizedDataOutputStream.writeInt0(this.handlers.length, out);
        for (final LabelMarker lm: this.handlers) {
            OptimizedDataOutputStream.writeInt0(lm.getLabelNr(), out);
        }
    }

    public static TableSwitchInstruction readFrom(final DataInputStream in, final MethodReadInformation methodInfo,
            @SuppressWarnings("unused") final StringCacheInput stringCache,
            @SuppressWarnings("unused") final int opcode,
            final int index, final int lineNumber) throws IOException {
        final int min = OptimizedDataInputStream.readInt0(in);
        final LabelMarker defaultHandler = methodInfo.getLabel(OptimizedDataInputStream.readInt0(in));
        final int handlersSize = OptimizedDataInputStream.readInt0(in);
        final LabelMarker[] handlers = new LabelMarker[handlersSize];
        for (int i = 0; i < handlersSize; ++i)
            handlers[i] = methodInfo.getLabel(OptimizedDataInputStream.readInt0(in));
        return new TableSwitchInstruction(methodInfo.getMethod(), min, lineNumber, defaultHandler, handlers, index);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TABLESWITCH (");
        for (int i = 0; i < this.handlers.length; ++i) {
            sb.append(this.min+i).append(" => ").append(this.handlers[i]).append(", ");
        }
        sb.append("* => ").append(this.defaultHandler);
        sb.append(')');
        return sb.toString();
    }

}
