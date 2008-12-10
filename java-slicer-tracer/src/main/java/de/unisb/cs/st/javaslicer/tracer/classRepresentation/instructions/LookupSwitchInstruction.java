package de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import org.objectweb.asm.Opcodes;

import de.hammacher.util.IntegerMap;
import de.hammacher.util.OptimizedDataInputStream;
import de.hammacher.util.OptimizedDataOutputStream;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.StringCacheInput;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.StringCacheOutput;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod.MethodReadInformation;

/**
 * Class representing a LOOKUPSWITCH instruction.
 *
 * @author Clemens Hammacher
 */
public class LookupSwitchInstruction extends AbstractInstruction {

    private LabelMarker defaultHandler;
    private IntegerMap<LabelMarker> handlers;

    public LookupSwitchInstruction(final ReadMethod readMethod, final int lineNumber,
            final LabelMarker defaultHandler, final IntegerMap<LabelMarker> handlers) {
        super(readMethod, Opcodes.LOOKUPSWITCH, lineNumber);
        this.defaultHandler = defaultHandler;
        this.handlers = handlers;
    }

    private LookupSwitchInstruction(final ReadMethod readMethod, final int lineNumber,
            final LabelMarker defaultHandler, final IntegerMap<LabelMarker> handlers, final int index) {
        super(readMethod, Opcodes.LOOKUPSWITCH, lineNumber, index);
        this.defaultHandler = defaultHandler;
        this.handlers = handlers;
    }

    public LabelMarker getDefaultHandler() {
        return this.defaultHandler;
    }

    public void setDefaultHandler(final LabelMarker defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    public IntegerMap<LabelMarker> getHandlers() {
        return this.handlers;
    }

    public void setHandlers(final IntegerMap<LabelMarker> handlers) {
        this.handlers = handlers;
    }

    @Override
    public Type getType() {
        return Type.LOOKUPSWITCH;
    }

    @Override
    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        super.writeOut(out, stringCache);
        OptimizedDataOutputStream.writeInt0(this.defaultHandler.getLabelNr(), out);
        OptimizedDataOutputStream.writeInt0(this.handlers.size(), out);
        for (final Entry<Integer, LabelMarker> e: this.handlers.entrySet()) {
            OptimizedDataOutputStream.writeInt0(e.getKey(), out);
            OptimizedDataOutputStream.writeInt0(e.getValue().getLabelNr(), out);
        }
    }

    public static LookupSwitchInstruction readFrom(final DataInputStream in, final MethodReadInformation methodInfo,
            @SuppressWarnings("unused") final StringCacheInput stringCache,
            @SuppressWarnings("unused") final int opcode,
            final int index, final int lineNumber) throws IOException {
        final LabelMarker defaultHandler = methodInfo.getLabel(OptimizedDataInputStream.readInt0(in));
        int handlerSize = OptimizedDataInputStream.readInt0(in);
        final IntegerMap<LabelMarker> handlers = new IntegerMap<LabelMarker>(handlerSize*4/3+1);
        while (handlerSize-- > 0) {
            final int key = OptimizedDataInputStream.readInt0(in);
            final LabelMarker lm = methodInfo.getLabel(OptimizedDataInputStream.readInt0(in));
            handlers.put(key, lm);
        }
        return new LookupSwitchInstruction(methodInfo.getMethod(), lineNumber, defaultHandler, handlers, index);
    }

    @Override
    public String toString() {
        final List<Entry<Integer, LabelMarker>> sortedHandlers =
            new ArrayList<Entry<Integer,LabelMarker>>(this.handlers.entrySet());
        Collections.sort(sortedHandlers, new Comparator<Entry<Integer, LabelMarker>>() {
            public int compare(final Entry<Integer, LabelMarker> o1, final Entry<Integer, LabelMarker> o2) {
                return o1.getKey() - o2.getKey();
            }
        });
        final StringBuilder sb = new StringBuilder();
        sb.append("LOOKUPSWITCH (");
        boolean first = true;
        for (final Entry<Integer, LabelMarker> entry : sortedHandlers) {
            if (first)
                first = false;
            else
                sb.append(", ");
            sb.append(entry.getKey().intValue()).append(" => ").append(entry.getValue());
        }
        sb.append(')');
        return sb.toString();
    }

}
