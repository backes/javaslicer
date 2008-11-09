package de.unisb.cs.st.javaslicer.tracer.util.sequitur.output;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Queue;

import de.unisb.cs.st.javaslicer.tracer.util.sequitur.output.Rule.Dummy;

// package-private
class Terminal<T> extends Symbol<T> {

    protected final T value;

    public Terminal(final T value) {
        this(value, 1);
    }

    public Terminal(final T value, final int count) {
        super(count);
        this.value = value;
    }

    @Override
    public boolean meltDigram(final Grammar<T> grammar) {
        if (this.next.getClass() != this.getClass())
            return false;

        final Terminal<T> otherT = (Terminal<T>) this.next;
        if (this.value == null ? otherT.value == null : this.value.equals(otherT.value)) {
            final boolean hasPrev = !(this.prev instanceof Dummy<?>);
            final boolean hasNextNext = !(otherT.next instanceof Dummy<?>);
            if (hasPrev)
                grammar.removeDigram(this.prev);
            if (hasNextNext)
                grammar.removeDigram(otherT);
            this.count += otherT.count;
            otherT.remove();
            if (hasPrev)
                grammar.checkDigram(this.prev);
            if (hasNextNext)
                grammar.checkDigram(this);
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
            final Queue<Rule<T>> queue) throws IOException {
        assert this.count >= 1;
        if (this.count != 1) {
            DataOutput.writeInt(objOut, this.count);
        }
        if (objectWriter == null)
            objOut.writeObject(this.value);
        else
            objectWriter.writeObject(this.value, objOut);
    }

    @Override
    protected boolean singleEquals(final Symbol<?> obj) {
        if (obj.getClass() != this.getClass())
            return false;
        final Terminal<?> other = (Terminal<?>) obj;
        return this.count == other.count
            && (this.value == null ? other.value == null : this.value.equals(other.value));
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

        return new StringBuilder().append(this.value)
            .append('^').append(this.count).toString();
    }

}
