package de.unisb.cs.st.javaslicer.util;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A non-synchronized Class holding one integer value.
 *
 * Usable just like {@link AtomicInteger}, but without a volatile (so not threadsafe).
 *
 * @author Clemens Hammacher
 */
public class IntHolder extends Number implements Serializable {

    private static final long serialVersionUID = 9105103358098386487L;

    private int value;

    /**
     * Creates a new IntHolder with the given initial value.
     *
     * @param initialValue the initial value
     */
    public IntHolder(final int initialValue) {
        this.value = initialValue;
    }

    /**
     * Creates a new IntHolder with initial value {@code 0}.
     */
    public IntHolder() {
        this(0);
    }

    /**
     * Gets the current value.
     *
     * @return the current value
     */
    public final int get() {
        return this.value;
    }

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
    public final void set(final int newValue) {
        this.value = newValue;
    }

    /**
     * Sets to the given value and returns the old value.
     *
     * @param newValue the new value
     * @return the previous value
     */
    public final int getAndSet(final int newValue) {
        final int val = this.value;
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
    public final boolean compareAndSet(final int expect, final int update) {
        int val = this.value;
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
    public final int getAndIncrement() {
        return this.value++;
    }

    /**
     * Decrements by one the current value.
     *
     * @return the previous value
     */
    public final int getAndDecrement() {
        return this.value--;
    }

    /**
     * Adds the given value to the current value.
     *
     * @param delta the value to add
     * @return the previous value
     */
    public final int getAndAdd(final int delta) {
        final int val = this.value;
        this.value += delta;
        return val;
    }

    /**
     * Increments by one the current value.
     *
     * @return the updated value
     */
    public final int incrementAndGet() {
        return ++this.value;
    }

    /**
     * Decrements by one the current value.
     *
     * @return the updated value
     */
    public final int decrementAndGet() {
        return --this.value;
    }

    /**
     * Adds the given value to the current value.
     *
     * @param delta the value to add
     * @return the updated value
     */
    public final int addAndGet(final int delta) {
        return this.value += delta;
    }

    /**
     * Returns the String representation of the current value.
     * @return the String representation of the current value.
     */
    @Override
    public String toString() {
        return Integer.toString(get());
    }

    @Override
    public int intValue() {
        return get();
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
