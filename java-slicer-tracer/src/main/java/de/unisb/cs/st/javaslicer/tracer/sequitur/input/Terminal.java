package de.unisb.cs.st.javaslicer.tracer.sequitur.input;

import java.io.IOException;
import java.io.ObjectInputStream;


// package-private
class Terminal<T> extends Symbol<T> {

    private final T value;

    public Terminal(final T value, final int count) {
        super(count);
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    @Override
    public long getLength(final boolean ignoreCount) {
        return ignoreCount ? 1 : this.count;
    }

    @Override
    public String toString() {
        if (this.count == 1)
            return String.valueOf(this.value);

        return new StringBuilder().append(this.value)
            .append('^').append(this.count).toString();
    }

    @SuppressWarnings("unchecked")
    public static <T> Terminal<T> readFrom(final ObjectInputStream objIn, final boolean counted,
            final ObjectReader<? extends T> objectReader, final Class<? extends T> checkInstance) throws IOException, ClassNotFoundException {
        final int count = counted ? DataInput.readInt(objIn) : 1;
        if (objectReader == null) {
            final Object value = objIn.readObject();
            if (checkInstance != null && !checkInstance.isInstance(value))
                throw new ClassCastException(value.getClass().getName()+" not assignment-compatible with "+checkInstance.getName());
            return new Terminal<T>((T) value, count);
        }
        final T value = objectReader.readObject(objIn);
        return new Terminal<T>(value, count);
    }

}
