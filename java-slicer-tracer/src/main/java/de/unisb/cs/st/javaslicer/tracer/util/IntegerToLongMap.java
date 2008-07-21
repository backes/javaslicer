package de.unisb.cs.st.javaslicer.tracer.util;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class IntegerToLongMap implements Map<Integer, Long>, Cloneable {

    /**
     * The default initial capacity - MUST be a power of two.
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified by either of the constructors with
     * arguments. MUST be a power of two <= 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     */
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;

    public static final float DEFAULT_SWITCH_TO_MAP_RATIO = 0.15f;

    public static final float DEFAULT_SWITCH_TO_LIST_RATIO = 0.25f;

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
    Entry[] mapTable;

    long[] list = null;

    boolean[] listEntriesWithZeroValue = null;

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

    protected final long defaultValue;

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
    public IntegerToLongMap(final int initialCapacity, final float loadFactor, final float switchToMapRatio,
            final float switchToListRatio, final long defaultValue) {
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
        this.defaultValue = defaultValue;
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial capacity and the default load factor (0.75).
     *
     * @param initialMapCapacity
     *            the initial capacity.
     * @throws IllegalArgumentException
     *             if the initial capacity is negative.
     */
    public IntegerToLongMap(final int initialMapCapacity) {
        this(initialMapCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_SWITCH_TO_MAP_RATIO, DEFAULT_SWITCH_TO_LIST_RATIO, Integer.MIN_VALUE);
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity (16) and the default load factor (0.75).
     */
    public IntegerToLongMap() {
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
     * Returns the value to which the specified key is mapped, or defaultValue() if this map contains no
     * mapping for the key.
     * Returns <code>null</code> if the key is not of type Integer;
     */
    public Long get(final Object key) {
        if (key instanceof Integer)
            return getLong(((Integer) key).intValue());
        return null;
    }

    public long getLong(final int key) {
        if (this.list != null) {
            if (key >= 0 && key < this.list.length) {
                final long val = this.list[key];
                if (val == 0 && (this.listEntriesWithZeroValue == null || !this.listEntriesWithZeroValue[key]))
                    return this.defaultValue;
                return val;
            }
            return this.defaultValue;
        }
        final int index = key & (this.mapTable.length - 1);
        for (Entry e = this.mapTable[index]; e != null; e = e.next)
            if (key == e.key)
                return e.value;
        return this.defaultValue;
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
    final protected Entry getEntry(final Object key) {
        if (key instanceof Integer)
            return getEntry(((Integer) key).intValue());
        return null;
    }

    final protected Entry getEntry(final int key) {
        if (this.list != null) {
            if (key >= 0 && key < this.list.length) {
                final long val = this.list[key];
                if (val == 0 && (this.listEntriesWithZeroValue == null || !this.listEntriesWithZeroValue[key]))
                    return null;
                return new Entry(key, val, null);
            }
            return null;
        }
        final int index = key & (this.mapTable.length - 1);
        for (Entry e = this.mapTable[index]; e != null; e = e.next)
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
     * @return the previous value associated with <tt>key</tt>, or <tt>defaultValue</tt> if there was no mapping for
     *         <tt>key</tt>.
     */
    public Long put(final Integer key, final Long value) {
        return put(key.intValue(), value.longValue());
    }

    public long put(final int key, final long value) {
        if (this.list != null) {
            if (key >= 0 && key < this.list.length) {
                long old = this.list[key];
                this.list[key] = value;
                if (old == 0) {
                    if (this.listEntriesWithZeroValue == null || !this.listEntriesWithZeroValue[key]) {
                        ++this.size;
                        old = this.defaultValue;
                    } else if (value != 0) {
                        this.listEntriesWithZeroValue[key] = false;
                    }
                }
                if (value == 0) {
                    if (this.listEntriesWithZeroValue == null)
                        this.listEntriesWithZeroValue = new boolean[this.list.length];
                    this.listEntriesWithZeroValue[key] = true;
                }
                return old;
            }
            final boolean switchToMap = key < 0 || this.size < this.switchToMapRatio * (key+1);
            if (switchToMap) {
                switchToMap();
                // and continue with the map code below...
            } else {
                final int newSize = 3 * key / 2 + 1;
                final long[] oldList = this.list;
                this.list = new long[newSize];
                System.arraycopy(oldList, 0, this.list, 0, oldList.length);
                if (this.listEntriesWithZeroValue != null) {
                    final boolean[] oldListEntries = this.listEntriesWithZeroValue;
                    this.listEntriesWithZeroValue = new boolean[newSize];
                    System.arraycopy(oldListEntries, 0, this.listEntriesWithZeroValue, 0, oldListEntries.length);
                }
                this.list[key] = value;
                this.size++;
                if (value == 0) {
                    if (this.listEntriesWithZeroValue == null)
                        this.listEntriesWithZeroValue = new boolean[this.list.length];
                    this.listEntriesWithZeroValue[key] = true;
                }
                return this.defaultValue;
            }
        }
        final int index = key & (this.mapTable.length - 1);
        for (Entry e = this.mapTable[index]; e != null; e = e.next) {
            if (e.key == key) {
                final long oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        this.modCount++;
        addEntry(key, value, index);
        return this.defaultValue;
    }

    private void switchToMap() {
        this.modCount++;
        final int minTableSize = (int) (1.1 * this.list.length / this.loadFactor);
        int mapTableSize = 1;
        while (mapTableSize < minTableSize)
            mapTableSize <<= 1;

        this.mapTable = new Entry[mapTableSize];
        boolean minSet = false;
        for (int key = 0; key < this.list.length; ++key) {
            final long value = this.list[key];
            if (value == 0 && (
                    this.listEntriesWithZeroValue == null
                    || !this.listEntriesWithZeroValue[key]))
                continue;
            if (!minSet) {
                minSet = true;
                this.minIndex = key;
            }
            this.maxIndex = key;
            final int index = key & (mapTableSize - 1);
            this.mapTable[index] = new Entry(key, value, this.mapTable[index]);
        }
        this.list = null;
        this.listEntriesWithZeroValue = null;
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
    void resizeMap(final int newCapacity) {
        final Entry[] oldTable = this.mapTable;
        final int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            this.mapThreshold = Integer.MAX_VALUE;
            return;
        }

        final Entry[] newTable = new Entry[newCapacity];
        transferMap(newTable);
        this.mapTable = newTable;
        this.mapThreshold = (int) (newCapacity * this.loadFactor);
    }

    /**
     * Transfers all entries from current table to newTable.
     */
    private void transferMap(final Entry[] newTable) {
        final Entry[] src = this.mapTable;
        final int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++) {
            Entry e = src[j];
            if (e != null) {
                src[j] = null;
                do {
                    final Entry next = e.next;
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
    public void putAll(final Map<? extends Integer, ? extends Long> m) {
        for (final Map.Entry<? extends Integer, ? extends Long> e : m.entrySet())
            put(e.getKey(), e.getValue());
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param key
     *            key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or <tt>defaultValue</tt> if there was no mapping for
     *         <tt>key</tt>. A <tt>null</tt> is returned if the key is not of type Integer.
     */
    public Long remove(final Object key) {
        if (key instanceof Integer)
            return remove(((Integer) key).intValue());
        return null;
    }

    public long remove(final int key) {
        if (this.list != null) {
            if (key < 0 || key >= this.list.length)
                return this.defaultValue;
            final long old = this.list[key];
            if (old != 0 || (this.listEntriesWithZeroValue != null && this.listEntriesWithZeroValue[key])) {
                this.size--;
            }
            if (this.listEntriesWithZeroValue != null)
                this.listEntriesWithZeroValue[key] = false;
            this.list[key] = 0;
            return old;
        }

        final int index = key & (this.mapTable.length - 1);
        Entry prev = this.mapTable[index];
        Entry e = prev;

        while (e != null) {
            final Entry next = e.next;
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

        return this.defaultValue;
    }

    private void recomputeMinMaxIndexes() {
        this.minIndex = Integer.MAX_VALUE;
        this.maxIndex = Integer.MIN_VALUE;
        for (Entry e : this.mapTable) {
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
    public void clear() {
        this.modCount++;
        this.size = 0;
        if (this.list != null) {
            this.list = new long[this.list.length];
            this.listEntriesWithZeroValue = null;
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
        if (value instanceof Integer)
            return containsValue(((Integer)value).intValue());
        return false;
    }

    public boolean containsValue(final int value) {

        if (this.list != null) {
            if (value == 0) {
                if (this.listEntriesWithZeroValue != null)
                    for (final boolean b: this.listEntriesWithZeroValue)
                        if (b)
                            return true;
                return false;
            }
            for (final long val : this.list)
                if (val == value)
                    return true;
            return false;
        }

        final Entry[] tab = this.mapTable;
        for (int i = 0; i < tab.length; i++)
            for (Entry e = tab[i]; e != null; e = e.next)
                if (value == e.value)
                    return true;
        return false;
    }

    public void increment(final int key) {
        if (this.list != null) {
            if (key >= 0 && key < this.list.length) {
                long val = this.list[key];
                if (val == 0) {
                    if (this.listEntriesWithZeroValue != null && this.listEntriesWithZeroValue[key]) {
                        this.listEntriesWithZeroValue[key] = false;
                        this.list[key] = 1;
                        return;
                    }
                    // otherwise go to the put below
                } else {
                    ++val;
                    if (val == 0) {
                        if (this.listEntriesWithZeroValue == null)
                            this.listEntriesWithZeroValue = new boolean[this.list.length];
                        this.listEntriesWithZeroValue[key] = true;
                    }
                    this.list[key] = val;
                    return;
                }
            }

        } else {

            final int index = key & (this.mapTable.length - 1);
            for (Entry e = this.mapTable[index]; e != null; e = e.next) {
                if (e.key == key) {
                    e.value++;
                    return;
                }
            }
        }
        put(key, this.defaultValue + 1);
    }

    public long incrementAndGet(final int key, final long addValue) {
        if (this.list != null) {
            if (key >= 0 && key < this.list.length) {
                final long val = this.list[key];
                if (val == 0) {
                    if (this.listEntriesWithZeroValue != null && this.listEntriesWithZeroValue[key]) {
                        this.listEntriesWithZeroValue[key] = false;
                        this.list[key] = addValue;
                        return addValue;
                    }
                    // otherwise go to the put below
                } else {
                    final long newVal = val + addValue;
                    if (newVal == 0) {
                        if (this.listEntriesWithZeroValue == null)
                            this.listEntriesWithZeroValue = new boolean[this.list.length];
                        this.listEntriesWithZeroValue[key] = true;
                    }
                    this.list[key] = newVal;
                    return newVal;
                }
            }

        } else {

            final int index = key & (this.mapTable.length - 1);
            for (Entry e = this.mapTable[index]; e != null; e = e.next) {
                if (e.key == key) {
                    e.value += addValue;
                    return e.value;
                }
            }
        }
        put(key, this.defaultValue + addValue);
        return addValue;
    }

    private static final class Entry implements Map.Entry<Integer, Long> {

        final int key;

        long value;

        Entry next;

        /**
         * Creates new entry.
         */
        Entry(final int key, final long value, final Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final Integer getKey() {
            return this.key;
        }

        public final Long getValue() {
            return this.value;
        }

        public final Long setValue(final Long newValue) {
            final long oldValue = this.value;
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
            return this.key ^ (int)(this.value ^ (this.value >>> 32));
        }

        @Override
        public final String toString() {
            return this.key + "=" + this.value;
        }

    }

    /**
     * Adds a new entry with the specified key, value and hash code to the specified bucket. It is the responsibility of
     * this method to resize the table if appropriate.
     */
    private void addEntry(final int key, final long value, final int index) {
        this.mapTable[index] = new Entry(key, value, this.mapTable[index]);
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

    private void switchToList() {
        this.modCount++;
        final int minListSize = (int) (1.1 * this.maxIndex + 1);
        int listSize = 1;
        while (listSize < minListSize)
            listSize <<= 1;

        this.list = new long[listSize];
        for (Entry e : this.mapTable) {
            while (e != null) {
                if (e.key < this.minIndex || e.key > this.maxIndex)
                    throw new ConcurrentModificationException();
                if (e.value == 0) {
                    if (this.listEntriesWithZeroValue == null)
                        this.listEntriesWithZeroValue = new boolean[listSize];
                    this.listEntriesWithZeroValue[e.key] = true;
                } else
                    this.list[e.key] = e.value;
                e = e.next;
            }
        }
        this.mapTable = null;
        this.minIndex = Integer.MAX_VALUE;
        this.maxIndex = Integer.MIN_VALUE;
        this.modCount++;
    }

    private class MapIterator implements Iterator<Map.Entry<Integer, Long>> {
        Entry next; // next entry to return

        int expectedModCount; // For fast-fail

        int index; // current slot

        Entry current; // current entry

        protected MapIterator() {
            this.expectedModCount = IntegerToLongMap.this.modCount;
            if (IntegerToLongMap.this.size > 0) { // advance to first entry
                final Entry[] t = IntegerToLongMap.this.mapTable;
                while (this.index < t.length && (this.next = t[this.index++]) == null) {
                    continue;
                }
            }
        }

        public Entry next() {
            if (IntegerToLongMap.this.modCount != this.expectedModCount)
                throw new ConcurrentModificationException();
            final Entry e = this.next;
            if (e == null)
                throw new NoSuchElementException();

            if ((this.next = e.next) == null) {
                final Entry[] t = IntegerToLongMap.this.mapTable;
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
            if (IntegerToLongMap.this.modCount != this.expectedModCount)
                throw new ConcurrentModificationException();
            final int k = this.current.key;
            this.current = null;
            IntegerToLongMap.this.remove(k);
            this.expectedModCount = IntegerToLongMap.this.modCount;
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
                if (IntegerToLongMap.this.list != null) {
                    return new Iterator<Integer>() {

                        int nextCursor = getNextCursor(0);

                        int expectedModCount = IntegerToLongMap.this.modCount;

                        int lastKey = -1;

                        private int getNextCursor(final int i) {
                            int next = i;
                            while (next < IntegerToLongMap.this.list.length
                                    && IntegerToLongMap.this.list[next] == 0
                                    && (IntegerToLongMap.this.listEntriesWithZeroValue == null
                                        || !IntegerToLongMap.this.listEntriesWithZeroValue[next]))
                                ++next;
                            return next;
                        }

                        public boolean hasNext() {
                            if (this.expectedModCount != IntegerToLongMap.this.modCount)
                                throw new ConcurrentModificationException();
                            return this.nextCursor < IntegerToLongMap.this.list.length;
                        }

                        public Integer next() {
                            if (this.expectedModCount != IntegerToLongMap.this.modCount)
                                throw new ConcurrentModificationException();
                            if (!hasNext())
                                throw new NoSuchElementException();
                            this.lastKey = this.nextCursor;
                            this.nextCursor = getNextCursor(this.nextCursor + 1);
                            return this.lastKey;
                        }

                        public void remove() {
                            if (this.expectedModCount != IntegerToLongMap.this.modCount)
                                throw new ConcurrentModificationException();
                            if (this.lastKey == -1)
                                throw new IllegalStateException();
                            IntegerToLongMap.this.remove(this.lastKey);
                        }

                    };
                }
                // else:

                return new Iterator<Integer>() {
                    private final Iterator<Map.Entry<Integer, Long>> i = new MapIterator();

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
                return IntegerToLongMap.this.size();
            }

            @Override
            public boolean contains(final Object k) {
                return IntegerToLongMap.this.containsKey(k);
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
    public Collection<Long> values() {
        return new AbstractCollection<Long>() {
            @Override
            public Iterator<Long> iterator() {
                if (IntegerToLongMap.this.list != null) {
                    return new Iterator<Long>() {

                        int nextCursor = getNextCursor(0);

                        int expectedModCount = IntegerToLongMap.this.modCount;

                        int lastKey = -1;

                        private int getNextCursor(final int i) {
                            int next = i;
                            while (next < IntegerToLongMap.this.list.length
                                    && IntegerToLongMap.this.list[next] == 0
                                    && (IntegerToLongMap.this.listEntriesWithZeroValue == null
                                        || !IntegerToLongMap.this.listEntriesWithZeroValue[next]))
                                ++next;
                            return next;
                        }

                        public boolean hasNext() {
                            if (this.expectedModCount != IntegerToLongMap.this.modCount)
                                throw new ConcurrentModificationException();
                            return this.nextCursor < IntegerToLongMap.this.list.length;
                        }

                        public Long next() {
                            if (this.expectedModCount != IntegerToLongMap.this.modCount)
                                throw new ConcurrentModificationException();
                            if (!hasNext())
                                throw new NoSuchElementException();
                            this.lastKey = this.nextCursor;
                            this.nextCursor = getNextCursor(this.nextCursor + 1);
                            return IntegerToLongMap.this.list[this.lastKey];
                        }

                        public void remove() {
                            if (this.expectedModCount != IntegerToLongMap.this.modCount)
                                throw new ConcurrentModificationException();
                            if (this.lastKey == -1)
                                throw new IllegalStateException();
                            IntegerToLongMap.this.remove(this.lastKey);
                        }

                    };
                }
                // else:

                return new Iterator<Long>() {
                    private final Iterator<Map.Entry<Integer, Long>> i = new MapIterator();

                    public boolean hasNext() {
                        return this.i.hasNext();
                    }

                    public Long next() {
                        return this.i.next().getValue();
                    }

                    public void remove() {
                        this.i.remove();
                    }
                };
            }

            @Override
            public int size() {
                return IntegerToLongMap.this.size();
            }

            @Override
            public boolean contains(final Object v) {
                return IntegerToLongMap.this.containsValue(v);
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
    public Set<Map.Entry<Integer, Long>> entrySet() {
        return new EntrySet();
    }

    final class EntrySet implements Set<Map.Entry<Integer, Long>> {

        public Iterator<Map.Entry<Integer, Long>> iterator() {
            if (IntegerToLongMap.this.list != null) {
                return new Iterator<Map.Entry<Integer, Long>>() {

                    int nextCursor = getNextCursor(0);

                    int expectedModCount = IntegerToLongMap.this.modCount;

                    int lastKey = -1;

                    private int getNextCursor(final int i) {
                        int next = i;
                        while (next < IntegerToLongMap.this.list.length
                                && IntegerToLongMap.this.list[next] == 0
                                && (IntegerToLongMap.this.listEntriesWithZeroValue == null
                                    || !IntegerToLongMap.this.listEntriesWithZeroValue[next]))
                            ++next;
                        return next;
                    }

                    public boolean hasNext() {
                        if (this.expectedModCount != IntegerToLongMap.this.modCount)
                            throw new ConcurrentModificationException();
                        return this.nextCursor < IntegerToLongMap.this.list.length;
                    }

                    public Entry next() {
                        if (this.expectedModCount != IntegerToLongMap.this.modCount)
                            throw new ConcurrentModificationException();
                        if (!hasNext())
                            throw new NoSuchElementException();
                        this.lastKey = this.nextCursor;
                        this.nextCursor = getNextCursor(this.nextCursor + 1);
                        return new Entry(this.lastKey, IntegerToLongMap.this.list[this.lastKey], null);
                    }

                    public void remove() {
                        if (this.expectedModCount != IntegerToLongMap.this.modCount)
                            throw new ConcurrentModificationException();
                        if (this.lastKey == -1)
                            throw new IllegalStateException();
                        IntegerToLongMap.this.remove(this.lastKey);
                    }

                };
            }
            // else:

            return new MapIterator();
        }

        public boolean contains(final Object o) {
            if (!(o instanceof Entry))
                return false;
            final Entry e = (Entry) o;
            final Entry candidate = getEntry(e.key);
            return candidate != null && candidate.equals(e);
        }

        public boolean remove(final Object o) {
            if (o instanceof Entry) {
                final int key = ((Entry)o).key;
                return IntegerToLongMap.this.containsKey(key)
                    && IntegerToLongMap.this.remove(((Entry) o).key) == IntegerToLongMap.this.defaultValue;
            }
            return false;
        }

        public int size() {
            return IntegerToLongMap.this.size;
        }

        public void clear() {
            IntegerToLongMap.this.clear();
        }

        public boolean add(final java.util.Map.Entry<Integer, Long> e) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(final Collection<? extends Map.Entry<Integer, Long>> c) {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(final Collection<?> c) {
            for (final Object o : c)
                if (!contains(o))
                    return false;
            return true;
        }

        public boolean isEmpty() {
            return IntegerToLongMap.this.isEmpty();
        }

        public boolean removeAll(final Collection<?> c) {
            boolean changed = false;
            for (final Object o : c)
                if (remove(o))
                    changed = true;
            return changed;
        }

        public boolean retainAll(final Collection<?> c) {
            final Iterator<Map.Entry<Integer, Long>> e = iterator();
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
            final Iterator<Map.Entry<Integer, Long>> it = iterator();
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
            final Iterator<Map.Entry<Integer, Long>> it = iterator();

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
        final Iterator<Map.Entry<Integer, Long>> i = entrySet().iterator();
        if (!i.hasNext())
            return "{}";

        final StringBuilder sb = new StringBuilder();
        sb.append('{');
        while (true) {
            final Map.Entry<Integer, Long> e = i.next();
            final Integer key = e.getKey();
            final Long value = e.getValue();
            sb.append(key).append('=').append(value);
            if (!i.hasNext())
                return sb.append('}').toString();
            sb.append(", ");
        }
    }

    @Override
    public int hashCode() {
        int h = 0;
        final Iterator<Map.Entry<Integer, Long>> i = entrySet().iterator();
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
        final Map<Integer, Integer> m = (Map<Integer, Integer>) o;
        if (m.size() != size())
            return false;

        try {
            final Iterator<Map.Entry<Integer, Long>> i = entrySet().iterator();
            while (i.hasNext()) {
                final Map.Entry<Integer, Long> e = i.next();
                final Integer key = e.getKey();
                final Long value = e.getValue();
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

    @Override
    public IntegerToLongMap clone() {
        IntegerToLongMap clone;
        try {
            clone = (IntegerToLongMap) super.clone();
        } catch (final CloneNotSupportedException e) {
            // this should never occur since we are cloneable!!
            throw new RuntimeException(e);
        }
        if (this.list != null) {
            clone.list = new long[this.list.length];
            System.arraycopy(this.list, 0, clone.list, 0, this.list.length);
        }
        if (this.listEntriesWithZeroValue != null) {
            clone.listEntriesWithZeroValue = new boolean[this.listEntriesWithZeroValue.length];
            System.arraycopy(this.listEntriesWithZeroValue, 0, clone.listEntriesWithZeroValue, 0, this.listEntriesWithZeroValue.length);
        }
        if (this.mapTable != null) {
            final Entry[] newTable = new Entry[this.mapTable.length];
            for (int j = 0; j < this.mapTable.length; ++j) {
                Entry e = this.mapTable[j];
                while (e != null) {
                    newTable[j] = new Entry(e.key, e.value, newTable[j]);
                    e = e.next;
                }
            }
            clone.mapTable = newTable;
        }
        return clone;
    }

}
