package de.unisb.cs.st.javaslicer.tracer.util.sequitur.input;

// package-private
abstract class Symbol<T> {

    protected final int count;

    protected Symbol(final int count) {
        assert count > 0;
        this.count = count;
    }

    public abstract long getLength(boolean ignoreCount);

}
