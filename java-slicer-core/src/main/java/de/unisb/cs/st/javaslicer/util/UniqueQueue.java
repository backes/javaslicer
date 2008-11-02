package de.unisb.cs.st.javaslicer.util;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * A Queue in which every element is only inserted once. Further insertions have
 * no effekt.
 *
 * @author Clemens Hammacher
 */
public class UniqueQueue<E> extends ArrayDeque<E> {

    private static final long serialVersionUID = 388606802602789126L;

    private final Set<E> seen;

    public UniqueQueue() {
        super();
        this.seen = new HashSet<E>();
    }

    public UniqueQueue(final Collection<? extends E> c) {
        super(c);
        this.seen = new HashSet<E>(c);
    }

    public UniqueQueue(final int numElements) {
        super(numElements);
        this.seen = new HashSet<E>(numElements);
    }

    public Set<E> getSeen() {
        return Collections.unmodifiableSet(this.seen);
    }

    @Override
    public void addFirst(final E e) {
        if (this.seen.add(e))
            super.addFirst(e);
    }

    @Override
    public void addLast(final E e) {
        if (this.seen.add(e))
            super.addLast(e);
    }

    @Override
    public boolean add(final E e) {
        if (!this.seen.add(e))
            return false;
        super.addLast(e);
        return true;
    }

    @Override
    public boolean offer(final E e) {
        if (!this.seen.add(e))
            return false;
        super.addLast(e);
        return true;
    }

    @Override
    public boolean offerFirst(final E e) {
        if (!this.seen.add(e))
            return false;
        super.addFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(final E e) {
        if (!this.seen.add(e))
            return false;
        super.addLast(e);
        return true;
    }

    /**
     * Resets the set of seen elements.
     * After this operation, every elements is again accepted exactly once.
     */
    public void clearSeen() {
        this.seen.clear();
    }

}
