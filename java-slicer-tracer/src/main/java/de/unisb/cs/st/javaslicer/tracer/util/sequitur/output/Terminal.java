package de.unisb.cs.st.javaslicer.tracer.util.sequitur.output;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.LinkedList;


public class Terminal<T> extends Symbol<T> {

    private final T value;
    protected int count = 1;

    public Terminal(final T value) {
        this.value = value;
    }

    @Override
    public boolean meltDigram() {
        if (this.next.getClass() != this.getClass())
            return false;

        final Terminal<?> otherT = (Terminal<?>) this.next;
        if (this.value == null ? otherT.value == null : this.value.equals(otherT.value)) {
            this.count += otherT.count;
            otherT.remove();
            return true;
        }
        return false;
    }

    @Override
    public int getHeader() {
        assert this.count >= 1;
        return this.count == 1 ? 2 : 3;
    }

    @Override
    public void writeOut(final ObjectOutputStream objOut, final Grammar<T> grammar,
            final ObjectWriter<? super T> objectWriter,
            final LinkedList<Rule<T>> queue) throws IOException {
        assert this.count >= 1;
        if (this.count != 1) {
            DataOutput.writeLong(objOut, this.count);
        }
        objectWriter.writeObject(this.value, objOut);
    }

    @Override
    protected boolean singleEquals(final Symbol<?> obj) {
        if (obj.getClass() != this.getClass())
            return false;
        final Terminal<?> other = (Terminal<?>) obj;
        return this.count == other.count
            && this.value == null ? other.value == null : this.value.equals(other.value);
    }

    @Override
    protected int singleHashcode() {
        return (this.value == null ? 0 : this.value.hashCode()) + 31*this.count;
    }

    @Override
    public String toString() {
        assert this.count >= 1;
        if (this.count == 1)
            return String.valueOf(this.value);

        return new StringBuilder().append('R').append(this.value)
            .append('^').append(this.count).toString();
    }

}
