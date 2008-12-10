package de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.OptimizedDataInputStream;
import de.hammacher.util.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.StringCacheInput;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.StringCacheOutput;
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
    public Type getType() {
        return Type.TABLESWITCH;
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
            if (i > 0)
                sb.append(", ");
            sb.append(this.min+i).append(" => ").append(this.handlers[i]);
        }
        sb.append(')');
        return sb.toString();
    }

}
