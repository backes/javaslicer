package de.unisb.cs.st.javaslicer.tracer.util;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class IntegerMap<V> implements Map<Integer, V>, Cloneable {

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

    static final float DEFAULT_SWITCH_TO_MAP_RATIO = 0.2f;

    static final float DEFAULT_SWITCH_TO_LIST_RATIO = 0.5f;

    /**
     * Will switch back (from list to map) when the ratio (size/highest_int) is below this threshold.
     */
    private final float switchToMapRatio;

    /**
     * Will switch from map to list when the ratio (size/highest_int) is above this threshold.
     */
    private final float switchToListRatio;

    /**
     * The table, resized as necessary. Length MUST Always be a power of two.
     */
    Entry<V>[] mapTable;

    V[] list;

    // maintained when the map is used to notice when we can switch to list
    private int minIndex = Integer.MAX_VALUE;

    private int maxIndex = Integer.MIN_VALUE;

    /**
     * The number of key-value mappings contained in this map.
     */
    int size;

    /**
     * The next size value at which to resize (capacity * load factor).
     *
     * @serial
     */
    private int mapThreshold;

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
    public IntegerMap(final int initialCapacity, final float loadFactor, final float switchToMapRatio,
            final float switchToListRatio) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        final int initCapacity = initialCapacity > MAXIMUM_CAPACITY ? MAXIMUM_CAPACITY : initialCapacity;
        if (loadFactor <= 0 || (loadFactor != loadFactor)) // check for negative value or NaN
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initCapacity)
            capacity <<= 1;

        this.loadFactor = loadFactor;
        this.mapThreshold = (int) (capacity * loadFactor);
        this.mapTable = new Entry[capacity];
        this.switchToMapRatio = switchToMapRatio;
        this.switchToListRatio = switchToListRatio;
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial capacity and the default load factor (0.75).
     *
     * @param initialMapCapacity
     *            the initial capacity.
     * @throws IllegalArgumentException
     *             if the initial capacity is negative.
     */
    public IntegerMap(final int initialMapCapacity) {
        this(initialMapCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_SWITCH_TO_MAP_RATIO, DEFAULT_SWITCH_TO_LIST_RATIO);
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity (16) and the default load factor (0.75).
     */
    public IntegerMap() {
        this(DEFAULT_INITIAL_CAPACITY);
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
        if (key instanceof Integer)
            return get(((Integer) key).intValue());
        return null;
    }

    public V get(final int key) {
        if (this.list != null) {
            if (key >= 0 && key < this.list.length)
                return this.list[key];
            return null;
        }
        final int index = key & (this.mapTable.length - 1);
        for (Entry<V> e = this.mapTable[index]; e != null; e = e.next)
            if (key == e.key)
                return e.value;
        return null;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified key.
     *
     * @param key
     *            The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified key.
     */
    public boolean containsKey(final Object key) {
        return key instanceof Integer ? containsKey(((Integer)key).intValue()) : false;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified key.
     *
     * @param key
     *            The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified key.
     */
    public boolean containsKey(final int key) {
        if (this.list != null)
            return key >= 0 && key < this.list.length && this.list[key] != null;

        final int index = key & (this.mapTable.length - 1);
        for (Entry<V> e = this.mapTable[index]; e != null; e = e.next)
            if (e.key == key)
                return true;
        return false;
    }

    /**
     * Returns the entry associated with the specified key in the HashMap. Returns null if the HashMap contains no
     * mapping for the key.
     */
    final Entry<V> getEntry(final Object key) {
        if (key instanceof Integer)
            return getEntry(((Integer) key).intValue());
        return null;
    }

    final Entry<V> getEntry(final int key) {
        if (this.list != null) {
            if (key >= 0 && key < this.list.length && this.list[key] != null)
                return new Entry<V>(key, this.list[key], null);
            return null;
        }
        final int index = key & (this.mapTable.length - 1);
        for (Entry<V> e = this.mapTable[index]; e != null; e = e.next)
            if (e.key == key)
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
    public V put(final Integer key, final V value) {
        return put(key.intValue(), value);
    }

    @SuppressWarnings("unchecked")
    public V put(final int key, final V value) {
        if (value == null)
            throw new NullPointerException();
        if (this.list != null) {
            if (key >= 0 && key < this.list.length) {
                final V old = this.list[key];
                this.list[key] = value;
                if (old == null)
                    ++this.size;
                return old;
            }
            final boolean switchToMap = key < 0 || this.size < this.switchToMapRatio * Math.max(key, key+1);
            if (switchToMap) {
                switchToMap();
                // and continue with the map code below...
            } else {
                final int newSize = 3 * key / 2 + 1;
                final V[] oldList = this.list;
                this.list = (V[]) new Object[newSize];
                System.arraycopy(oldList, 0, this.list, 0, oldList.length);
                this.list[key] = value;
                this.size++;
                return null;
            }
        }
        final int index = key & (this.mapTable.length - 1);
        for (Entry<V> e = this.mapTable[index]; e != null; e = e.next) {
            if (e.key == key) {
                final V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        this.modCount++;
        addEntry(key, value, index);
        return null;
    }

    @SuppressWarnings("unchecked")
    private void switchToMap() {
        this.modCount++;
        final int minTableSize = (int) (1.1 * this.list.length / this.loadFactor);
        int mapTableSize = 1;
        while (mapTableSize < minTableSize)
            mapTableSize <<= 1;

        this.mapTable = new Entry[mapTableSize];
        boolean minSet = false;
        for (int key = 0; key < this.list.length; ++key) {
            final V value = this.list[key];
            if (value == null)
                continue;
            if (!minSet) {
                minSet = true;
                this.minIndex = key;
            }
            this.maxIndex = key;
            final int index = key & (mapTableSize - 1);
            this.mapTable[index] = new Entry<V>(key, value, this.mapTable[index]);
        }
        this.list = null;
        this.modCount++;
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
    void resizeMap(final int newCapacity) {
        final Entry<V>[] oldTable = this.mapTable;
        final int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            this.mapThreshold = Integer.MAX_VALUE;
            return;
        }

        final Entry<V>[] newTable = new Entry[newCapacity];
        transferMap(newTable);
        this.mapTable = newTable;
        this.mapThreshold = (int) (newCapacity * this.loadFactor);
    }

    /**
     * Transfers all entries from current table to newTable.
     */
    private void transferMap(final Entry<V>[] newTable) {
        final Entry<V>[] src = this.mapTable;
        final int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++) {
            Entry<V> e = src[j];
            if (e != null) {
                src[j] = null;
                do {
                    final Entry<V> next = e.next;
                    final int newIndex = e.key & (newCapacity - 1);
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
    public void putAll(final Map<? extends Integer, ? extends V> m) {
        for (final Map.Entry<? extends Integer, ? extends V> e : m.entrySet())
            put(e.getKey(), e.getValue());
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
        if (key instanceof Integer)
            return remove(((Integer) key).intValue());
        return null;
    }

    public V remove(final int key) {
        if (this.list != null) {
            if (key < 0 || key >= this.list.length)
                return null;
            final V old = this.list[key];
            this.list[key] = null;
            if (old != null)
                this.size--;
            return old;
        }

        final int index = key & (this.mapTable.length - 1);
        Entry<V> prev = this.mapTable[index];
        Entry<V> e = prev;

        while (e != null) {
            final Entry<V> next = e.next;
            if (e.key == key) {
                this.modCount++;
                this.size--;
                if (prev == e)
                    this.mapTable[index] = next;
                else
                    prev.next = next;
                if (e.key == this.minIndex || e.key == this.maxIndex) {
                    recomputeMinMaxIndexes();
                }
                return e.value;
            }
            prev = e;
            e = next;
        }

        return null;
    }

    private void recomputeMinMaxIndexes() {
        this.minIndex = Integer.MAX_VALUE;
        this.maxIndex = Integer.MIN_VALUE;
        for (Entry<V> e : this.mapTable) {
            while (e != null) {
                if (e.key < this.minIndex)
                    this.minIndex = e.key;
                if (e.key > this.maxIndex)
                    this.maxIndex = e.key;
                e = e.next;
            }
        }
    }

    /**
     * Removes all of the mappings from this map. The map will be empty after this call returns.
     */
    @SuppressWarnings("unchecked")
    public void clear() {
        this.modCount++;
        this.size = 0;
        if (this.list != null) {
            this.list = (V[]) new Object[this.list.length];
        } else {
            this.mapTable = new Entry[this.mapTable.length];
            this.minIndex = Integer.MAX_VALUE;
            this.maxIndex = Integer.MIN_VALUE;
        }
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified value.
     *
     * @param value
     *            value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the specified value
     */
    public boolean containsValue(final Object value) {
        if (value == null)
            return false;

        if (this.list != null) {
            for (final V val : this.list)
                if (val != null && val.equals(value))
                    return true;
            return false;
        }

        final Entry<V>[] tab = this.mapTable;
        for (int i = 0; i < tab.length; i++)
            for (Entry<V> e = tab[i]; e != null; e = e.next)
                if (value.equals(e.value))
                    return true;
        return false;
    }

    private static final class Entry<V> implements Map.Entry<Integer, V> {

        final int key;

        V value;

        Entry<V> next;

        /**
         * Creates new entry.
         */
        Entry(final int key, final V value, final Entry<V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final Integer getKey() {
            return this.key;
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
            final Integer k1 = getKey();
            final Object k2 = e.getKey();
            if (k1 == null ? k2 == null : k1.equals(k2)) {
                final Object v1 = getValue();
                final Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2)))
                    return true;
            }
            return false;
        }

        @Override
        public final int hashCode() {
            return this.key ^ (this.value == null ? 0 : this.value.hashCode());
        }

        @Override
        public final String toString() {
            return this.key + "=" + getValue();
        }

    }

    /**
     * Adds a new entry with the specified key, value and hash code to the specified bucket. It is the responsibility of
     * this method to resize the table if appropriate.
     */
    private void addEntry(final int key, final V value, final int index) {
        this.mapTable[index] = new Entry<V>(key, value, this.mapTable[index]);
        this.size++;
        if (key < this.minIndex)
            this.minIndex = key;
        if (key > this.maxIndex)
            this.maxIndex = key;
        if (checkSwitchToList())
            return;
        if (this.size >= this.mapThreshold)
            resizeMap(2 * this.mapTable.length);
    }

    private boolean checkSwitchToList() {
        if (this.minIndex >= 0 && this.size > 3 && this.size > this.switchToListRatio * (this.maxIndex+1f)) {
            switchToList();
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void switchToList() {
        this.modCount++;
        final int minListSize = (int) (1.1 * this.maxIndex + 1);
        int listSize = 1;
        while (listSize < minListSize)
            listSize <<= 1;

        this.list = (V[]) new Object[listSize];
        for (Entry<V> e : this.mapTable) {
            while (e != null) {
                if (e.key < this.minIndex || e.key > this.maxIndex)
                    throw new ConcurrentModificationException();
                this.list[e.key] = e.value;
                e = e.next;
            }
        }
        this.mapTable = null;
        this.minIndex = Integer.MAX_VALUE;
        this.maxIndex = Integer.MIN_VALUE;
        this.modCount++;
    }

    private class MapIterator implements Iterator<Map.Entry<Integer, V>> {
        Entry<V> next; // next entry to return

        int expectedModCount; // For fast-fail

        int index; // current slot

        Entry<V> current; // current entry

        MapIterator() {
            this.expectedModCount = IntegerMap.this.modCount;
            if (IntegerMap.this.size > 0) { // advance to first entry
                final Entry<V>[] t = IntegerMap.this.mapTable;
                while (this.index < t.length && (this.next = t[this.index++]) == null) {
                    continue;
                }
            }
        }

        public Entry<V> next() {
            if (IntegerMap.this.modCount != this.expectedModCount)
                throw new ConcurrentModificationException();
            final Entry<V> e = this.next;
            if (e == null)
                throw new NoSuchElementException();

            if ((this.next = e.next) == null) {
                final Entry<V>[] t = IntegerMap.this.mapTable;
                while (this.index < t.length && (this.next = t[this.index++]) == null)
                    continue;
            }
            this.current = e;
            return e;
        }

        public final boolean hasNext() {
            return this.next != null;
        }

        public void remove() {
            if (this.current == null)
                throw new IllegalStateException();
            if (IntegerMap.this.modCount != this.expectedModCount)
                throw new ConcurrentModificationException();
            final int k = this.current.key;
            this.current = null;
            IntegerMap.this.remove(k);
            this.expectedModCount = IntegerMap.this.modCount;
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
    public Set<Integer> keySet() {
        return new AbstractSet<Integer>() {
            @Override
            public Iterator<Integer> iterator() {
                if (IntegerMap.this.list != null) {
                    return new Iterator<Integer>() {

                        int nextCursor = getNextCursor(0);

                        int expectedModCount = IntegerMap.this.modCount;

                        int lastKey = -1;

                        private int getNextCursor(final int i) {
                            int next = i;
                            while (next < IntegerMap.this.list.length && IntegerMap.this.list[next] == null)
                                ++next;
                            return next;
                        }

                        public boolean hasNext() {
                            if (this.expectedModCount != IntegerMap.this.modCount)
                                throw new ConcurrentModificationException();
                            return this.nextCursor < IntegerMap.this.list.length;
                        }

                        public Integer next() {
                            if (this.expectedModCount != IntegerMap.this.modCount)
                                throw new ConcurrentModificationException();
                            if (!hasNext())
                                throw new NoSuchElementException();
                            this.lastKey = this.nextCursor;
                            this.nextCursor = getNextCursor(this.nextCursor + 1);
                            return this.lastKey;
                        }

                        public void remove() {
                            if (this.expectedModCount != IntegerMap.this.modCount)
                                throw new ConcurrentModificationException();
                            if (this.lastKey == -1)
                                throw new IllegalStateException();
                            IntegerMap.this.remove(this.lastKey);
                        }

                    };
                }
                // else:

                return new Iterator<Integer>() {
                    private final Iterator<Map.Entry<Integer, V>> i = new MapIterator();

                    public boolean hasNext() {
                        return this.i.hasNext();
                    }

                    public Integer next() {
                        return this.i.next().getKey();
                    }

                    public void remove() {
                        this.i.remove();
                    }
                };
            }

            @Override
            public int size() {
                return IntegerMap.this.size();
            }

            @Override
            public boolean contains(final Object k) {
                return IntegerMap.this.containsKey(k);
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
                if (IntegerMap.this.list != null) {
                    return new Iterator<V>() {

                        int nextCursor = getNextCursor(0);

                        int expectedModCount = IntegerMap.this.modCount;

                        int lastKey = -1;

                        private int getNextCursor(final int i) {
                            int next = i;
                            while (next < IntegerMap.this.list.length && IntegerMap.this.list[next] == null)
                                ++next;
                            return next;
                        }

                        public boolean hasNext() {
                            if (this.expectedModCount != IntegerMap.this.modCount)
                                throw new ConcurrentModificationException();
                            return this.nextCursor < IntegerMap.this.list.length;
                        }

                        public V next() {
                            if (this.expectedModCount != IntegerMap.this.modCount)
                                throw new ConcurrentModificationException();
                            if (!hasNext())
                                throw new NoSuchElementException();
                            this.lastKey = this.nextCursor;
                            this.nextCursor = getNextCursor(this.nextCursor + 1);
                            return IntegerMap.this.list[this.lastKey];
                        }

                        public void remove() {
                            if (this.expectedModCount != IntegerMap.this.modCount)
                                throw new ConcurrentModificationException();
                            if (this.lastKey == -1)
                                throw new IllegalStateException();
                            IntegerMap.this.remove(this.lastKey);
                        }

                    };
                }
                // else:

                return new Iterator<V>() {
                    private final Iterator<Map.Entry<Integer, V>> i = new MapIterator();

                    public boolean hasNext() {
                        return this.i.hasNext();
                    }

                    public V next() {
                        return this.i.next().getValue();
                    }

                    public void remove() {
                        this.i.remove();
                    }
                };
            }

            @Override
            public int size() {
                return IntegerMap.this.size();
            }

            @Override
            public boolean contains(final Object v) {
                return IntegerMap.this.containsValue(v);
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
    public Set<Map.Entry<Integer, V>> entrySet() {
        return new EntrySet();
    }

    final class EntrySet implements Set<Map.Entry<Integer, V>> {

        public Iterator<Map.Entry<Integer, V>> iterator() {
            if (IntegerMap.this.list != null) {
                return new Iterator<Map.Entry<Integer, V>>() {

                    int nextCursor = getNextCursor(0);

                    int expectedModCount = IntegerMap.this.modCount;

                    int lastKey = -1;

                    private int getNextCursor(final int i) {
                        int next = i;
                        while (next < IntegerMap.this.list.length && IntegerMap.this.list[next] == null)
                            ++next;
                        return next;
                    }

                    public boolean hasNext() {
                        if (this.expectedModCount != IntegerMap.this.modCount)
                            throw new ConcurrentModificationException();
                        return this.nextCursor < IntegerMap.this.list.length;
                    }

                    public Entry<V> next() {
                        if (this.expectedModCount != IntegerMap.this.modCount)
                            throw new ConcurrentModificationException();
                        if (!hasNext())
                            throw new NoSuchElementException();
                        this.lastKey = this.nextCursor;
                        this.nextCursor = getNextCursor(this.nextCursor + 1);
                        return new Entry<V>(this.lastKey, IntegerMap.this.list[this.lastKey], null);
                    }

                    public void remove() {
                        if (this.expectedModCount != IntegerMap.this.modCount)
                            throw new ConcurrentModificationException();
                        if (this.lastKey == -1)
                            throw new IllegalStateException();
                        IntegerMap.this.remove(this.lastKey);
                    }

                };
            }
            // else:

            return new MapIterator();
        }

        @SuppressWarnings("unchecked")
        public boolean contains(final Object o) {
            if (!(o instanceof Entry))
                return false;
            final Entry<V> e = (Entry<V>) o;
            final Entry<V> candidate = getEntry(e.key);
            return candidate != null && candidate.equals(e);
        }

        @SuppressWarnings("unchecked")
        public boolean remove(final Object o) {
            if (o instanceof Entry)
                return IntegerMap.this.remove(((Entry) o).key) != null;
            return false;
        }

        public int size() {
            return IntegerMap.this.size;
        }

        public void clear() {
            IntegerMap.this.clear();
        }

        public boolean add(final java.util.Map.Entry<Integer, V> e) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(final Collection<? extends Map.Entry<Integer, V>> c) {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(final Collection<?> c) {
            for (final Object o : c)
                if (!contains(o))
                    return false;
            return true;
        }

        public boolean isEmpty() {
            return IntegerMap.this.isEmpty();
        }

        public boolean removeAll(final Collection<?> c) {
            boolean changed = false;
            for (final Object o : c)
                if (remove(o))
                    changed = true;
            return changed;
        }

        public boolean retainAll(final Collection<?> c) {
            final Iterator<Map.Entry<Integer, V>> e = iterator();
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
            final Iterator<Map.Entry<Integer, V>> it = iterator();
            for (int i = 0; i < r.length; i++) {
                if (!it.hasNext()) { // fewer elements than expected
                    final Object[] r2 = new Object[i];
                    System.arraycopy(r, 0, r2, 0, i);
                    return r2;
                }
                r[i] = it.next();
            }
            return it.hasNext() ? finishToArray(r, it) : r;
        }

        @SuppressWarnings("unchecked")
        public <T> T[] toArray(final T[] a) {
            final T[] r = a.length >= size() ? a : (T[]) java.lang.reflect.Array.newInstance(a.getClass()
                    .getComponentType(), size());
            final Iterator<Map.Entry<Integer, V>> it = iterator();

            for (int i = 0; i < r.length; i++) {
                if (!it.hasNext()) { // fewer elements than expected
                    if (a != r) {
                        final T[] r2 = (T[]) java.lang.reflect.Array.newInstance(a.getClass()
                            .getComponentType(), i);
                        System.arraycopy(r, 0, r2, 0, i);
                        return r2;
                    }
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
                    final T[] old = a;
                    a = (T[]) java.lang.reflect.Array.newInstance(a.getClass()
                        .getComponentType(), newCap);
                    System.arraycopy(old, 0, a, 0, newCap);
                }
                a[i++] = (T) it.next();
            }
            // trim if overallocated
            if (i == a.length)
                return a;
            final T[] newA = (T[]) java.lang.reflect.Array.newInstance(a.getClass()
                .getComponentType(), i);
            System.arraycopy(a, 0, newA, 0, i);
            return newA;
        }

    }

    @Override
    public String toString() {
        final Iterator<Map.Entry<Integer, V>> i = entrySet().iterator();
        if (!i.hasNext())
            return "{}";

        final StringBuilder sb = new StringBuilder();
        sb.append('{');
        while (true) {
            final Map.Entry<Integer, V> e = i.next();
            final Integer key = e.getKey();
            final V value = e.getValue();
            sb.append(key).append('=').append(value == this ? "(this Map)" : value);
            if (!i.hasNext())
                return sb.append('}').toString();
            sb.append(", ");
        }
    }

    @Override
    public int hashCode() {
        int h = 0;
        final Iterator<Map.Entry<Integer, V>> i = entrySet().iterator();
        while (i.hasNext())
            h += i.next().hashCode();
        return h;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Map))
            return false;
        final Map<Integer, V> m = (Map<Integer, V>) o;
        if (m.size() != size())
            return false;

        try {
            final Iterator<Map.Entry<Integer, V>> i = entrySet().iterator();
            while (i.hasNext()) {
                final Map.Entry<Integer, V> e = i.next();
                final Integer key = e.getKey();
                final V value = e.getValue();
                if (!value.equals(m.get(key)))
                    return false;
            }
        } catch (final ClassCastException unused) {
            return false;
        } catch (final NullPointerException unused) {
            return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IntegerMap<V> clone() {
        IntegerMap<V> clone;
        try {
            clone = (IntegerMap<V>) super.clone();
        } catch (final CloneNotSupportedException e) {
            // this should never occur since we are cloneable!!
            throw new RuntimeException(e);
        }
        if (this.list != null) {
            clone.list = (V[]) new Object[this.list.length];
            System.arraycopy(this.list, 0, clone.list, 0, this.list.length);
        }
        if (this.mapTable != null) {
            final Entry<V>[] newTable = new Entry[this.mapTable.length];
            for (int j = 0; j < this.mapTable.length; ++j) {
                Entry<V> e = this.mapTable[j];
                while (e != null) {
                    newTable[j] = new Entry<V>(e.key, e.value, newTable[j]);
                    e = e.next;
                }
            }
            clone.mapTable = newTable;
        }
        return clone;
    }

}
