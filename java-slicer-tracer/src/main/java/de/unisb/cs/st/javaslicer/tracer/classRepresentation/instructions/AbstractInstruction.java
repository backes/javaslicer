package de.unisb.cs.st.javaslicer.tracer.classRepresentation.instructions;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.InstructionWrapper;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.ReadMethod.MethodReadInformation;
import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.traceResult.ThreadTraceResult.BackwardInstructionIterator;

/**
 * Abstract superclass that builds the basis for most Instruction implementing classes.
 *
 * @author Clemens Hammacher
 */
public abstract class AbstractInstruction implements Instruction {

    private static int nextIndex = 0;

    // TODO optimize this
    private static final Class<?>[] instructions =
        new Class<?>[] {
            ArrayInstruction.class,
            FieldInstruction.class,
            IIncInstruction.class,
            IntPush.class,
            JumpInstruction.class,
            LabelMarker.class,
            LdcInstruction.class,
            LookupSwitchInstruction.class,
            MethodInvocationInstruction.class,
            MultiANewArrayInstruction.class,
            NewArrayInstruction.class,
            SimpleInstruction.class,
            TableSwitchInstruction.class,
            TypeInstruction.class,
            VarInstruction.class
        };

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

    public int getBackwardInstructionIndex(final BackwardInstructionIterator backwardInstructionIterator) {
        return this.index - 1;
    }

    public void writeOut(final DataOutput out) throws IOException {
        // write out type of the instruction
        boolean typeFound = false;
        for (int i = 0; i < instructions.length; ++i) {
            if (instructions[i].equals(getClass())) {
                typeFound = true;
                out.writeByte(i);
                break;
            }
        }
        if (!typeFound)
            throw new RuntimeException("Class " + getClass() + " has no index to export");
        out.writeInt(this.index);
        out.writeInt(this.lineNumber);
        out.writeInt(this.opcode);
    }

    public static AbstractInstruction readFrom(final DataInput in, final MethodReadInformation methodInfo) throws IOException {
        // first determine the type
        final byte type = in.readByte();
        if (type < 0 || type >= instructions.length)
            throw new IOException("corrupted data");
        final int index = in.readInt();
        final int lineNumber = in.readInt();
        final int opcode = in.readInt();

        final Class<?> instrClass = instructions[type];
        try {
            final Method readFromMethod = instrClass.getMethod("readFrom",
                    DataInput.class, MethodReadInformation.class, int.class, int.class, int.class);
            final Object o = readFromMethod.invoke(null, in, methodInfo, opcode, index, lineNumber);
            if (o instanceof AbstractInstruction)
                return (AbstractInstruction)o;
            throw new RuntimeException("readFrom does not return AbstractInstruction");
        } catch (final SecurityException e) {
            throw new RuntimeException(e);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException("Internal error: class " + instrClass + " does not implement readFrom", e);
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
    public Instance getNextInstance(final BackwardInstructionIterator backwardInstructionIterator) throws TracerException, EOFException {
        return new AbstractInstance(this, backwardInstructionIterator.getNextInstructionOccurenceNumber(this.index), backwardInstructionIterator.getStackDepth());
    }

    public Instruction getPrevious() {
        assert getIndex() >= getMethod().getInstructionNumberStart()
            && getIndex() <= getMethod().getInstructionNumberEnd();
        if (getIndex() == getMethod().getInstructionNumberStart())
            return null;
        final Instruction previous = getMethod().getInstructions().get(
                getIndex()-1-getMethod().getInstructionNumberStart());
        assert previous.getIndex() == getIndex()-1;
        return previous;
    }

    public Instruction getNext() {
        assert getIndex() >= getMethod().getInstructionNumberStart()
            && getIndex() <= getMethod().getInstructionNumberEnd();
        if (getIndex() + 1 == getMethod().getInstructionNumberEnd())
            return null;
        final Instruction next = getMethod().getInstructions().get(
                getIndex()+1-getMethod().getInstructionNumberStart());
        assert next.getIndex() == getIndex()+1;
        return next;
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

        @Override
        public Instruction getInstruction() {
            return this.wrappedInstruction;
        }

        @Override
        public int getStackDepth() {
            return this.stackDepth;
        }

    }

}
