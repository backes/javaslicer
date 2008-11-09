package de.unisb.cs.st.javaslicer.tracer.util;

import java.util.concurrent.atomic.AtomicLong;


/**
 * A non-synchronized Class holding one long value.
 *
 * Usable just like {@link AtomicLong}, but without a volatile (so not threadsafe).
 *
 * @author Clemens Hammacher
 */
public class LongHolder extends Number {

    private static final long serialVersionUID = 7236014395270337184L;

    private long value;

    /**
     * Creates a new IntHolder with the given initial value.
     *
     * @param initialValue the initial value
     */
    public LongHolder(final long initialValue) {
        this.value = initialValue;
    }

    /**
     * Creates a new IntHolder with initial value {@code 0}.
     */
    public LongHolder() {
        this(0);
    }

    /**
     * Gets the current value.
     *
     * @return the current value
     */
    public final long get() {
        return this.value;
    }

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
    public final void set(final long newValue) {
        this.value = newValue;
    }

    /**
     * Sets to the given value and returns the old value.
     *
     * @param newValue the new value
     * @return the previous value
     */
    public final long getAndSet(final long newValue) {
        final long val = this.value;
        this.value = newValue;
        return val;
    }

    /**
     * Sets the value to the given updated value
     * if the current value {@code ==} the expected value.
     *
     * @param expect the expected value
     * @param update the new value
     * @return true if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final boolean compareAndSet(final long expect, final long update) {
        long val = this.value;
        if (val == expect) {
            val = update;
            return true;
        }
        return false;
    }

    /**
     * Increments by one the current value.
     *
     * @return the previous value
     */
    public final long getAndIncrement() {
        return this.value++;
    }

    /**
     * Decrements by one the current value.
     *
     * @return the previous value
     */
    public final long getAndDecrement() {
        return this.value--;
    }

    /**
     * Adds the given value to the current value.
     *
     * @param delta the value to add
     * @return the previous value
     */
    public final long getAndAdd(final long delta) {
        final long val = this.value;
        this.value += delta;
        return val;
    }

    /**
     * Increments by one the current value.
     *
     * @return the updated value
     */
    public final long incrementAndGet() {
        return ++this.value;
    }

    /**
     * Decrements by one the current value.
     *
     * @return the updated value
     */
    public final long decrementAndGet() {
        return --this.value;
    }

    /**
     * Adds the given value to the current value.
     *
     * @param delta the value to add
     * @return the updated value
     */
    public final long addAndGet(final long delta) {
        return this.value += delta;
    }

    /**
     * Returns the String representation of the current value.
     * @return the String representation of the current value.
     */
    @Override
    public String toString() {
        return Long.toString(get());
    }

    @Override
    public int intValue() {
        return (int) get();
    }

    @Override
    public long longValue() {
        return get();
    }

    @Override
    public float floatValue() {
        return get();
    }

    @Override
    public double doubleValue() {
        return get();
    }

}
