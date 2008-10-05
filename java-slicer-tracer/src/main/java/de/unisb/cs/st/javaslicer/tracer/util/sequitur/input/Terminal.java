package de.unisb.cs.st.javaslicer.tracer.util.sequitur.input;


public class Terminal<T> extends Symbol<T> {

    private final T value;
    private int count = 1;

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
