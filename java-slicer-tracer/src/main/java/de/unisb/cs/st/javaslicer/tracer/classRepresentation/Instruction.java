package de.unisb.cs.st.javaslicer.tracer.classRepresentation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Instruction {

    private static int nextIndex = 0;

    private static final Class<?>[] instructions =
        new Class<?>[] {
            ArrayInstruction.class, FieldInstruction.class, IIncInstruction.class,
            IntPush.class, JumpInstruction.class, LabelMarker.class,
            LdcInstruction.class, MethodInvocationInstruction.class,
            NewArrayInstruction.class, SimpleInstruction.class
        };

    private final int index;
    protected final ReadMethod method;
    private final int opcode;
    private final int lineNumber;

    public Instruction(final ReadMethod readMethod, final int opcode, final int lineNumber) {
        this(readMethod, opcode, lineNumber, nextIndex++);
        if (nextIndex < 0)
            throw new RuntimeException("Integer overflow in instruction index");
    }

    protected Instruction(final ReadMethod readMethod, final int opcode, final int lineNumber, final int index) {
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

    public int getBackwardInstructionIndex() {
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

    public static Instruction readFrom(final DataInput in, final ReadMethod readMethod) throws IOException {
        // first determine the type
        final byte type = in.readByte();
        if (type < 0 || type >= instructions.length)
            throw new RuntimeException("Illegal instruction class index: " + type);
        final int index = in.readInt();
        final int lineNumber = in.readInt();
        final int opcode = in.readInt();

        final Class<?> instrClass = instructions[type];
        try {
            final Method readFromMethod = instrClass.getMethod("readFrom",
                    ObjectInputStream.class, ReadMethod.class, int.class, int.class, int.class);
            final Object o = readFromMethod.invoke(null, in, readMethod, opcode, index, lineNumber);
            if (o instanceof Instruction)
                return (Instruction)o;
            throw new RuntimeException("readFrom does not return Instruction");
        } catch (final SecurityException e) {
            throw new RuntimeException(e);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException("Internal error: class " + instrClass + " does not implement readFrom", e);
        } catch (final IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (final InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

}
