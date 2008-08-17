package de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod.MethodReadInformation;

/**
 * Class representing a TABLESWITCH instruction.
 *
 * @author Clemens Hammacher
 */
public class TableSwitchInstruction extends AbstractInstruction {

    private LabelMarker defaultHandler;
    private LabelMarker[] handlers;
    private final int min;
    private final int max;

    public TableSwitchInstruction(final ReadMethod readMethod, final int min, final int max, final LabelMarker defaultHandler, final LabelMarker[] handlers) {
        super(readMethod, Opcodes.TABLESWITCH);
        this.min = min;
        this.max = max;
        this.defaultHandler = defaultHandler;
        this.handlers = handlers;
    }

    private TableSwitchInstruction(final ReadMethod readMethod, final int min, final int max, final int lineNumber, final LabelMarker defaultHandler, final LabelMarker[] handlers, final int index) {
        super(readMethod, Opcodes.TABLESWITCH, lineNumber, index);
        this.min = min;
        this.max = max;
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
        return this.max;
    }

    @Override
    public void writeOut(final DataOutput out) throws IOException {
        super.writeOut(out);
        out.writeInt(this.min);
        out.writeInt(this.max);
        out.writeInt(this.defaultHandler.getLabelNr());
        for (final LabelMarker lm: this.handlers) {
            out.writeInt(lm.getLabelNr());
        }
    }

    public static TableSwitchInstruction readFrom(final DataInput in, final MethodReadInformation methodInfo, final int opcode, final int index, final int lineNumber) throws IOException {
        final int min = in.readInt();
        final int max = in.readInt();
        final LabelMarker defaultHandler = methodInfo.getLabel(in.readInt());
        final int handlersSize = max - min + 1;
        final LabelMarker[] handlers = new LabelMarker[handlersSize];
        for (int i = 0; i < handlersSize; ++i)
            handlers[i] = methodInfo.getLabel(in.readInt());
        return new TableSwitchInstruction(methodInfo.getMethod(), min, max, lineNumber, defaultHandler, handlers, index);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TABLESWITCH (");
        for (int i = 0; i < this.handlers.length; ++i) {
            if (i > 0)
                sb.append(", ");
            sb.append(this.min+i).append(" => ").append(this.handlers[i]);
        }
        sb.append(')');
        return sb.toString();
    }

}
