/*
 *  @(#)HashMap.java    1.73 07/03/13
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * THIS COPY IS HERE FOR NOT BEING INSTRUMENTED!
 *
 */

package de.unisb.cs.st.javaslicer.tracer.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class WeakIdentityHashMap<K, V> implements Map<K, V> {

    /**
     * The default initial capacity - MUST be a power of two.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified by either of the constructors with
     * arguments. MUST be a power of two <= 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    final ReferenceQueue<K> queue = new ReferenceQueue<K>();

    /**
     * The table, resized as necessary. Length MUST Always be a power of two.
     */
    Entry<K, V>[] table;

    /**
     * The number of key-value mappings contained in this map.
     */
    int size;

    /**
     * The next size value at which to resize (capacity * load factor).
     *
     * @serial
     */
    private int threshold;

    /**
     * The load factor for the hash table.
     *
     * @serial
     */
    private final float loadFactor;

    /**
     * The number of times this HashMap has been structurally modified Structural modifications are those that change
     * the number of mappings in the HashMap or otherwise modify its internal structure (e.g., rehash). This field is
     * used to make iterators on Collection-views of the HashMap fail-fast. (See ConcurrentModificationException).
     */
    volatile int modCount;

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial capacity and load factor.
     *
     * @param initialCapacity
     *            the initial capacity
     * @param loadFactor
     *            the load factor
     * @throws IllegalArgumentException
     *             if the initial capacity is negative or the load factor is nonpositive
     */
    @SuppressWarnings("unchecked")
    public WeakIdentityHashMap(final int initialCapacity, final float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        final int initCapacity = initialCapacity > MAXIMUM_CAPACITY ? MAXIMUM_CAPACITY : initialCapacity;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initCapacity)
            capacity <<= 1;

        this.loadFactor = loadFactor;
        this.threshold = (int) (capacity * loadFactor);
        this.table = new Entry[capacity];
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial capacity and the default load factor (0.75).
     *
     * @param initialCapacity
     *            the initial capacity.
     * @throws IllegalArgumentException
     *             if the initial capacity is negative.
     */
    public WeakIdentityHashMap(final int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity (16) and the default load factor (0.75).
     */
    @SuppressWarnings("unchecked")
    public WeakIdentityHashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        this.threshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
        this.table = new Entry[DEFAULT_INITIAL_CAPACITY];
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    public int size() {
        return this.size;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    public boolean isEmpty() {
        return this.size == 0;
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the
     * key.
     *
     * <p>
     * More formally, if this map contains a mapping from a key {@code k} to a value {@code v} such that
     * {@code (key==null ? k==null : key.equals(k))}, then this method returns {@code v}; otherwise it returns
     * {@code null}. (There can be at most one such mapping.)
     *
     * <p>
     * A return value of {@code null} does not <i>necessarily</i> indicate that the map contains no mapping for the
     * key; it's also possible that the map explicitly maps the key to {@code null}. The
     * {@link #containsKey containsKey} operation may be used to distinguish these two cases.
     *
     * @see #put(Object, Object)
     */
    public V get(final Object key) {
        final int index = hash(key) & (this.table.length - 1);
        for (Entry<K, V> e = this.table[index]; e != null; e = e.next)
            if (key == e.getKey())
                return e.value;
        return null;
    }

    protected int hash(final Object key) {
        return System.identityHashCode(key);
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified key.
     *
     * @param key
     *            The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified key.
     */
    public boolean containsKey(final Object key) {
        return getEntry(key) != null;
    }

    /**
     * Returns the entry associated with the specified key in the HashMap. Returns null if the HashMap contains no
     * mapping for the key.
     */
    final Entry<K, V> getEntry(final Object key) {
        final int index = hash(key) & (this.table.length - 1);
        for (Entry<K, V> e = this.table[index]; e != null; e = e.next)
            if (e.getKey() == key)
                return e;
        return null;
    }

    /**
     * Associates the specified value with the specified key in this map. If the map previously contained a mapping for
     * the key, the old value is replaced.
     *
     * @param key
     *            key with which the specified value is to be associated
     * @param value
     *            value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping for
     *         <tt>key</tt>.
     */
    public V put(final K key, final V value) {
        expungeStaleEntries();
        final int hash = hash(key);
        final int index = hash & (this.table.length - 1);
        for (Entry<K, V> e = this.table[index]; e != null; e = e.next) {
            if (e.getKey() == key) {
                final V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        this.modCount++;
        addEntry(key, value, hash, index);
        return null;
    }

    /**
     * Rehashes the contents of this map into a new array with a larger capacity. This method is called automatically
     * when the number of keys in this map reaches its threshold.
     *
     * If current capacity is MAXIMUM_CAPACITY, this method does not resize the map, but sets threshold to
     * Integer.MAX_VALUE. This has the effect of preventing future calls.
     *
     * @param newCapacity
     *            the new capacity, MUST be a power of two; must be greater than current capacity unless current
     *            capacity is MAXIMUM_CAPACITY (in which case value is irrelevant).
     */
    @SuppressWarnings("unchecked")
    void resize(final int newCapacity) {
        final Entry<K, V>[] oldTable = this.table;
        final int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            this.threshold = Integer.MAX_VALUE;
            return;
        }

        final Entry<K, V>[] newTable = new Entry[newCapacity];
        transfer(newTable);
        this.table = newTable;
        this.threshold = (int) (newCapacity * this.loadFactor);
    }

    /**
     * Transfers all entries from current table to newTable.
     */
    private void transfer(final Entry<K, V>[] newTable) {
        final Entry<K, V>[] src = this.table;
        final int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++) {
            Entry<K, V> e = src[j];
            if (e != null) {
                src[j] = null;
                do {
                    final Entry<K, V> next = e.next;
                    final int newIndex = e.hash & (newCapacity - 1);
                    e.next = newTable[newIndex];
                    newTable[newIndex] = e;
                    e = next;
                } while (e != null);
            }
        }
    }

    /**
     * Copies all of the mappings from the specified map to this map. These mappings will replace any mappings that this
     * map had for any of the keys currently in the specified map.
     *
     * @param m
     *            mappings to be stored in this map
     * @throws NullPointerException
     *             if the specified map is null
     */
    public void putAll(final Map<? extends K, ? extends V> m) {
        final int numKeysToBeAdded = m.size();
        if (numKeysToBeAdded == 0)
            return;

        /*
         * Expand the map if the map if the number of mappings to be added
         * is greater than or equal to threshold.  This is conservative; the
         * obvious condition is (m.size() + size) >= threshold, but this
         * condition could result in a map with twice the appropriate capacity,
         * if the keys to be added overlap with the keys already in this map.
         * By using the conservative calculation, we subject ourself
         * to at most one extra resize.
         */
        if (numKeysToBeAdded > this.threshold) {
            int targetCapacity = (int) (numKeysToBeAdded / this.loadFactor + 1);
            if (targetCapacity > MAXIMUM_CAPACITY)
                targetCapacity = MAXIMUM_CAPACITY;
            int newCapacity = this.table.length;
            while (newCapacity < targetCapacity)
                newCapacity <<= 1;
            if (newCapacity > this.table.length)
                resize(newCapacity);
        }

        for (final Iterator<? extends Map.Entry<? extends K, ? extends V>> i = m.entrySet().iterator(); i.hasNext();) {
            final Map.Entry<? extends K, ? extends V> e = i.next();
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param key
     *            key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping for
     *         <tt>key</tt>. (A <tt>null</tt> return can also indicate that the map previously associated
     *         <tt>null</tt> with <tt>key</tt>.)
     */
    public V remove(final Object key) {
        final Entry<K, V> e = removeEntryForKey(key);
        return (e == null ? null : e.value);
    }

    /**
     * Removes and returns the entry associated with the specified key in the HashMap. Returns null if the HashMap
     * contains no mapping for this key.
     */
    final Entry<K, V> removeEntryForKey(final Object key) {
        final int index = hash(key) & (this.table.length - 1);
        Entry<K, V> prev = this.table[index];
        Entry<K, V> e = prev;

        while (e != null) {
            final Entry<K, V> next = e.next;
            final K k = e.getKey();
            if (k == key) {
                this.modCount++;
                this.size--;
                if (prev == e)
                    this.table[index] = next;
                else
                    prev.next = next;
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
     * Special version of remove for EntrySet.
     */
    final Entry<K, V> removeMapping(final Entry<K, V> entry) {
        final int index = entry.hash & (this.table.length - 1);
        Entry<K, V> prev = this.table[index];
        Entry<K, V> e = prev;

        final K key = entry.getKey();
        while (e != null) {
            final Entry<K, V> next = e.next;
            if (e.getKey() == key) {
                this.modCount++;
                this.size--;
                if (prev == e)
                    this.table[index] = next;
                else
                    prev.next = next;
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
     * Removes all of the mappings from this map. The map will be empty after this call returns.
     */
    public void clear() {
        expungeStaleEntries();

        this.modCount++;
        final Entry<K, V>[] tab = this.table;
        for (int i = 0; i < tab.length; i++)
            tab[i] = null;
        this.size = 0;
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified value.
     *
     * @param value
     *            value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the specified value
     */
    public boolean containsValue(final Object value) {
        final Entry<K, V>[] tab = this.table;
        for (int i = 0; i < tab.length; i++)
            for (Entry<K, V> e = tab[i]; e != null; e = e.next)
                if (value.equals(e.value))
                    return true;
        return false;
    }

    private static final class Entry<K, V> extends WeakReference<K> implements Map.Entry<K, V> {

        final int hash;

        V value;

        Entry<K, V> next;

        /**
         * Creates new entry.
         */
        Entry(final K key, final int hash, final V value, final Entry<K, V> next, final ReferenceQueue<K> queue) {
            super(key, queue);
            this.hash = hash;
            this.value = value;
            this.next = next;
        }

        public final K getKey() {
            return get();
        }

        public final V getValue() {
            return this.value;
        }

        public final V setValue(final V newValue) {
            final V oldValue = this.value;
            this.value = newValue;
            return oldValue;
        }

        @Override
        public final boolean equals(final Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            final Object k1 = getKey();
            final Object k2 = e.getKey();
            if (k1 == k2) {
                final Object v1 = getValue();
                final Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2)))
                    return true;
            }
            return false;
        }

        @Override
        public final int hashCode() {
            final K t = getKey();
            return (t == null ? 0 : t.hashCode()) ^ (this.value == null ? 0 : this.value.hashCode());
        }

        @Override
        public final String toString() {
            return getKey() + "=" + getValue();
        }

    }

    /**
     * Adds a new entry with the specified key, value and hash code to the specified bucket. It is the responsibility of
     * this method to resize the table if appropriate.
     *
     * Subclass overrides this to alter the behavior of put method.
     */
    void addEntry(final K key, final V value, final int hash, final int bucketIndex) {
        this.table[bucketIndex] = new Entry<K, V>(key, hash, value, this.table[bucketIndex], this.queue);
        if (this.size++ >= this.threshold)
            resize(2 * this.table.length);
    }

    /**
     * Expunges stale entries from the table.
     */
    @SuppressWarnings("unchecked")
    private void expungeStaleEntries() {
        Entry<K, V> e;
        while ((e = (Entry<K, V>) this.queue.poll()) != null) {
            removing(e.value);
            final int index = e.hash & (this.table.length-1);
            Entry<K, V> prev = this.table[index];
            Entry<K, V> p = prev;
            while (p != null) {
                final Entry<K, V> next = p.next;
                if (p == e) {
                    if (prev == e)
                        this.table[index] = next;
                    else
                        prev.next = next;
                    e.next = null; // Help GC
                    e.value = null; // " "
                    this.size--;
                    break;
                }
                prev = p;
                p = next;
            }
        }
    }

    // hook for subclasses
    protected void removing(final V value) {
        // nothing
    }

    private abstract class HashIterator<E> implements Iterator<E> {
        Entry<K, V> next; // next entry to return

        int expectedModCount; // For fast-fail

        int index; // current slot

        Entry<K, V> current; // current entry

        HashIterator() {
            this.expectedModCount = WeakIdentityHashMap.this.modCount;
            if (WeakIdentityHashMap.this.size > 0) { // advance to first entry
                final Entry<K, V>[] t = WeakIdentityHashMap.this.table;
                while (this.index < t.length && (this.next = t[this.index++]) == null) {
                    continue;
                }
            }
        }

        public final boolean hasNext() {
            return this.next != null;
        }

        final Entry<K, V> nextEntry() {
            if (WeakIdentityHashMap.this.modCount != this.expectedModCount)
                throw new ConcurrentModificationException();
            final Entry<K, V> e = this.next;
            if (e == null)
                throw new NoSuchElementException();

            if ((this.next = e.next) == null) {
                final Entry<K, V>[] t = WeakIdentityHashMap.this.table;
                while (this.index < t.length && (this.next = t[this.index++]) == null)
                    continue;
            }
            this.current = e;
            return e;
        }

        public void remove() {
            if (this.current == null)
                throw new IllegalStateException();
            if (WeakIdentityHashMap.this.modCount != this.expectedModCount)
                throw new ConcurrentModificationException();
            final Object k = this.current.getKey();
            this.current = null;
            WeakIdentityHashMap.this.removeEntryForKey(k);
            this.expectedModCount = WeakIdentityHashMap.this.modCount;
        }

    }

    final class ValueIterator extends HashIterator<V> {
        public V next() {
            return nextEntry().value;
        }
    }

    final class KeyIterator extends HashIterator<K> {
        public K next() {
            return nextEntry().getKey();
        }
    }

    final class EntryIterator extends HashIterator<Map.Entry<K, V>> {
        public Map.Entry<K, V> next() {
            return nextEntry();
        }
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map. The set is backed by the map, so changes to the map
     * are reflected in the set, and vice-versa. If the map is modified while an iteration over the set is in progress
     * (except through the iterator's own <tt>remove</tt> operation), the results of the iteration are undefined. The
     * set supports element removal, which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
     * <tt>clear</tt> operations. It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     */
    public Set<K> keySet() {
        return new AbstractSet<K>() {
            @Override
            public Iterator<K> iterator() {
                return new KeyIterator();
            }

            @Override
            public int size() {
                return WeakIdentityHashMap.this.size();
            }

            @Override
            public boolean contains(final Object k) {
                return WeakIdentityHashMap.this.containsKey(k);
            }
        };
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map. The collection is backed by the map, so
     * changes to the map are reflected in the collection, and vice-versa. If the map is modified while an iteration
     * over the collection is in progress (except through the iterator's own <tt>remove</tt> operation), the results
     * of the iteration are undefined. The collection supports element removal, which removes the corresponding mapping
     * from the map, via the <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations. It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     */
    public Collection<V> values() {
        return new AbstractCollection<V>() {
            @Override
            public Iterator<V> iterator() {
                return new ValueIterator();
            }

            @Override
            public int size() {
                return WeakIdentityHashMap.this.size();
            }

            @Override
            public boolean contains(final Object v) {
                return WeakIdentityHashMap.this.containsValue(v);
            }
        };
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map. The set is backed by the map, so changes to the
     * map are reflected in the set, and vice-versa. If the map is modified while an iteration over the set is in
     * progress (except through the iterator's own <tt>remove</tt> operation, or through the <tt>setValue</tt>
     * operation on a map entry returned by the iterator) the results of the iteration are undefined. The set supports
     * element removal, which removes the corresponding mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and <tt>clear</tt> operations. It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map
     */
    public Set<Map.Entry<K, V>> entrySet() {
        return new EntrySet();
    }

    final class EntrySet implements Set<Map.Entry<K, V>> {

        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @SuppressWarnings("unchecked")
        public boolean contains(final Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            final Map.Entry<K, V> e = (Map.Entry<K, V>) o;
            final Entry<K, V> candidate = getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
        }

        @SuppressWarnings("unchecked")
        public boolean remove(final Object o) {
            if (o instanceof Entry)
                return removeMapping((Entry<K, V>) o) != null;
            return false;
        }

        public int size() {
            return WeakIdentityHashMap.this.size;
        }

        public void clear() {
            WeakIdentityHashMap.this.clear();
        }

        public boolean add(final java.util.Map.Entry<K, V> e) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(final Collection<? extends java.util.Map.Entry<K, V>> c) {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(final Collection<?> c) {
            for (final Object o : c)
                if (!contains(o))
                    return false;
            return true;
        }

        public boolean isEmpty() {
            return WeakIdentityHashMap.this.isEmpty();
        }

        public boolean removeAll(final Collection<?> c) {
            boolean changed = false;
            for (final Object o : c)
                if (remove(o))
                    changed = true;
            return changed;
        }

        public boolean retainAll(final Collection<?> c) {
            final Iterator<Map.Entry<K, V>> e = iterator();
            boolean changed = false;
            while (e.hasNext())
                if (!c.contains(e.next())) {
                    e.remove();
                    changed = true;
                }
            return changed;
        }

        public Object[] toArray() {
            final Object[] r = new Object[size()];
            final Iterator<Map.Entry<K, V>> it = iterator();
            for (int i = 0; i < r.length; i++) {
                if (!it.hasNext()) // fewer elements than expected
                    return Arrays.copyOf(r, i);
                r[i] = it.next();
            }
            return it.hasNext() ? finishToArray(r, it) : r;
        }

        @SuppressWarnings("unchecked")
        public <T> T[] toArray(final T[] a) {
            final T[] r = a.length >= size() ? a : (T[]) java.lang.reflect.Array.newInstance(a.getClass()
                    .getComponentType(), size());
            final Iterator<Map.Entry<K, V>> it = iterator();

            for (int i = 0; i < r.length; i++) {
                if (!it.hasNext()) { // fewer elements than expected
                    if (a != r)
                        return Arrays.copyOf(r, i);
                    r[i] = null; // null-terminate
                    return r;
                }
                r[i] = (T) it.next();
            }
            return it.hasNext() ? finishToArray(r, it) : r;
        }

        /**
         * Reallocates the array being used within toArray when the iterator returned more elements than expected, and
         * finishes filling it from the iterator.
         *
         * @param r
         *            the array, replete with previously stored elements
         * @param it
         *            the in-progress iterator over this collection
         * @return array containing the elements in the given array, plus any further elements returned by the iterator,
         *         trimmed to size
         */
        @SuppressWarnings("unchecked")
        private <T> T[] finishToArray(final T[] r, final Iterator<?> it) {
            T[] a = r;
            int i = a.length;
            while (it.hasNext()) {
                final int cap = a.length;
                if (i == cap) {
                    int newCap = ((cap / 2) + 1) * 3;
                    if (newCap <= cap) { // integer overflow
                        if (cap == Integer.MAX_VALUE)
                            throw new OutOfMemoryError("Required array size too large");
                        newCap = Integer.MAX_VALUE;
                    }
                    a = Arrays.copyOf(a, newCap);
                }
                a[i++] = (T) it.next();
            }
            // trim if overallocated
            return (i == a.length) ? a : Arrays.copyOf(a, i);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Map))
            return false;
        final Map<K, V> m = (Map<K, V>) o;
        if (m.size() != size())
            return false;

        try {
            final Iterator<Map.Entry<K, V>> i = entrySet().iterator();
            while (i.hasNext()) {
                final Map.Entry<K, V> e = i.next();
                final K key = e.getKey();
                final V value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(m.get(key)))
                        return false;
                }
            }
        } catch (final ClassCastException unused) {
            return false;
        } catch (final NullPointerException unused) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int h = 0;
        final Iterator<Map.Entry<K, V>> i = entrySet().iterator();
        while (i.hasNext())
            h += i.next().hashCode();
        return h;
    }

    @Override
    public String toString() {
        final Iterator<Map.Entry<K, V>> i = entrySet().iterator();
        if (!i.hasNext())
            return "{}";

        final StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            final Map.Entry<K, V> e = i.next();
            final K key = e.getKey();
            final V value = e.getValue();
            sb.append(key == this ? "(this Map)" : key);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);
            if (!i.hasNext())
                return sb.append('}').toString();
            sb.append(", ");
        }
    }

}
