package de.unisb.cs.st.javaslicer.common.classRepresentation.instructions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.hammacher.util.OptimizedDataInputStream;
import de.hammacher.util.OptimizedDataOutputStream;
import de.hammacher.util.StringCacheInput;
import de.hammacher.util.StringCacheOutput;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionWrapper;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.common.classRepresentation.TraceIterationInformationProvider;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod.MethodReadInformation;

/**
 * Abstract superclass that builds the basis for most Instruction implementing classes.
 *
 * @author Clemens Hammacher
 */
public abstract class AbstractInstruction implements Instruction {

    private static int nextIndex = 0;

    private static final Map<Class<?>, Integer> instructions = new HashMap<Class<?>, Integer>();
    static {
        instructions.put(ArrayInstruction.class, instructions.size());
        instructions.put(FieldInstruction.class, instructions.size());
        instructions.put(IIncInstruction.class, instructions.size());
        instructions.put(IntPush.class, instructions.size());
        instructions.put(JumpInstruction.class, instructions.size());
        instructions.put(LabelMarker.class, instructions.size());
        instructions.put(LdcInstruction.class, instructions.size());
        instructions.put(LookupSwitchInstruction.class, instructions.size());
        instructions.put(MethodInvocationInstruction.class, instructions.size());
        instructions.put(MultiANewArrayInstruction.class, instructions.size());
        instructions.put(NewArrayInstruction.class, instructions.size());
        instructions.put(SimpleInstruction.class, instructions.size());
        instructions.put(TableSwitchInstruction.class, instructions.size());
        instructions.put(TypeInstruction.class, instructions.size());
        instructions.put(VarInstruction.class, instructions.size());
    }

    private static final Method[] readMethods = new Method[instructions.size()];
    static {
        for (final Entry<Class<?>, Integer> entry: instructions.entrySet()) {
            try {
                readMethods[entry.getValue()] = entry.getKey().getMethod("readFrom",
                        DataInputStream.class, MethodReadInformation.class, StringCacheInput.class,
                        int.class, int.class, int.class);
            } catch (final SecurityException e) {
                throw new RuntimeException(e);
            } catch (final NoSuchMethodException e) {
                throw new InternalError("Internal error: class " + entry.getKey()
                   + " does not implement readFrom");
            } catch (final IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final int index;
    protected final ReadMethod method;
    private final int opcode;
    private final int lineNumber;

    public AbstractInstruction(final ReadMethod readMethod, final int opcode, final int lineNumber) {
        this(readMethod, opcode, lineNumber, nextIndex++);
        if (nextIndex < 0)
            throw new RuntimeException("Integer overflow in instruction index");
    }

    protected AbstractInstruction(final ReadMethod readMethod, final int opcode, final int lineNumber, final int index) {
        this.method = readMethod;
        this.opcode = opcode;
        this.lineNumber = lineNumber;
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    public ReadMethod getMethod() {
        return this.method;
    }

    public int getOpcode() {
        return this.opcode;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public static int getNextIndex() {
        return nextIndex;
    }

    public int getBackwardInstructionIndex(final TraceIterationInformationProvider infoProv) {
        return this.index - 1;
    }

    public void writeOut(final DataOutputStream out, final StringCacheOutput stringCache) throws IOException {
        // write out type of the instruction
        final Integer typeIndex = instructions.get(getClass());
        if (typeIndex == null)
            throw new RuntimeException("Class " + getClass() + " has no index to export");
        out.writeByte(typeIndex.byteValue());
        OptimizedDataOutputStream.writeInt0(this.index, out);
        OptimizedDataOutputStream.writeInt0(this.lineNumber, out);
        OptimizedDataOutputStream.writeInt0(this.opcode, out);
    }

    public static AbstractInstruction readFrom(final DataInputStream in, final MethodReadInformation methodInfo,
            final StringCacheInput stringCache) throws IOException {
        // first determine the type
        final byte type = in.readByte();
        if (type < 0 || type >= readMethods.length)
            throw new IOException("corrupted data");
        final int index = OptimizedDataInputStream.readInt0(in);
        final int lineNumber = OptimizedDataInputStream.readInt0(in);
        final int opcode = OptimizedDataInputStream.readInt0(in);

        final Method readFromMethod = readMethods[type];
        try {
            final Object o = readFromMethod.invoke(null, in, methodInfo, stringCache, opcode, index, lineNumber);
            if (o instanceof AbstractInstruction)
                return (AbstractInstruction)o;
            throw new RuntimeException("readFrom does not return AbstractInstruction");
        } catch (final IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (final InvocationTargetException e) {
            if (e.getCause() instanceof IOException)
                throw (IOException)e.getCause();
            throw new RuntimeException(e);
        }
    }

    // must be overridden by classes with dynamic parameters (e.g. array load/store)
    public Instance getBackwardInstance(final TraceIterationInformationProvider infoProv, final int stackDepth) {
        return new AbstractInstance(this, infoProv.getNextInstructionOccurenceNumber(this.index), stackDepth);
    }
    public Instance getForwardInstance(final TraceIterationInformationProvider infoProv, final int stackDepth) {
        return new AbstractInstance(this, infoProv.getNextInstructionOccurenceNumber(this.index), stackDepth);
    }

    public AbstractInstruction getPrevious() {
        assert getIndex() >= getMethod().getInstructionNumberStart()
            && getIndex() < getMethod().getInstructionNumberEnd();
        if (getIndex() == getMethod().getInstructionNumberStart())
            return null;
        final AbstractInstruction previous = getMethod().getInstructions().get(
                getIndex()-1-getMethod().getInstructionNumberStart());
        assert previous.getIndex() == getIndex()-1;
        return previous;
    }

    public AbstractInstruction getNext() {
        assert getIndex() >= getMethod().getInstructionNumberStart()
            && getIndex() < getMethod().getInstructionNumberEnd();
        if (getIndex() + 1 == getMethod().getInstructionNumberEnd())
            return null;
        final AbstractInstruction next = getMethod().getInstructions().get(
                getIndex()+1-getMethod().getInstructionNumberStart());
        assert next.getIndex() == getIndex()+1;
        return next;
    }

    public int compareTo(final Instruction o) {
        // we want so sort by class, line number, and index. NOT by method.
        int cmp = getMethod().getReadClass().compareTo(o.getMethod().getReadClass());
        if (cmp != 0)
            return cmp;
        cmp = getLineNumber() - o.getLineNumber();
        return cmp != 0 ? cmp : getIndex() - o.getIndex();
    }

    @Override
    public int hashCode() {
        return this.index;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractInstruction other = (AbstractInstruction) obj;
        return this.index == other.index;
    }


    public static class AbstractInstance extends InstructionWrapper
            implements Instance {

        private final long occurenceNumber;
        private final int stackDepth;

        public AbstractInstance(final AbstractInstruction instr, final long occurenceNumber, final int stackDepth) {
            super(instr);
            this.occurenceNumber = occurenceNumber;
            this.stackDepth = stackDepth;
        }

        public long getOccurenceNumber() {
            return this.occurenceNumber;
        }

        public Instruction getInstruction() {
            return this.wrappedInstruction;
        }

        public int getStackDepth() {
            return this.stackDepth;
        }

    }

}
