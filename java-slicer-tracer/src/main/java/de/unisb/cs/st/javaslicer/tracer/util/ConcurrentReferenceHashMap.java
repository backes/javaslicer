package de.unisb.cs.st.javaslicer.tracer.util;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An advanced hash table supporting configurable garbage collection semantics
 * of keys and values, optional referential-equality, full concurrency of
 * retrievals, and adjustable expected concurrency for updates.
 *
 * This table is designed around specific advanced use-cases. If there is any
 * doubt whether this table is for you, you most likely should be using
 * {@link java.util.concurrent.ConcurrentHashMap} instead.
 *
 * This table supports strong, weak, and soft keys and values. By default keys
 * are weak, and values are strong. Such a configuration offers similar behavior
 * to {@link java.util.WeakHashMap}, entries of this table are periodically
 * removed once their corresponding keys are no longer referenced outside of
 * this table. In other words, this table will not prevent a key from being
 * discarded by the garbage collector. Once a key has been discarded by the
 * collector, the corresponding entry is no longer visible to this table;
 * however, the entry may occupy space until a future table operation decides to
 * reclaim it. For this reason, summary functions such as <tt>size</tt> and
 * <tt>isEmpty</tt> might return a value greater than the observed number of
 * entries. In order to support a high level of concurrency, stale entries are
 * only reclaimed during blocking (usually mutating) operations.
 *
 * Enabling soft keys allows entries in this table to remain until their space
 * is absolutely needed by the garbage collector. This is unlike weak keys which
 * can be reclaimed as soon as they are no longer referenced by a normal strong
 * reference. The primary use case for soft keys is a cache, which ideally
 * occupies memory that is not in use for as long as possible.
 *
 * By default, values are held using a normal strong reference. This provides
 * the commonly desired guarantee that a value will always have at least the
 * same life-span as it's key. For this reason, care should be taken to ensure
 * that a value never refers, either directly or indirectly, to its key, thereby
 * preventing reclamation. If this is unavoidable, then it is recommended to use
 * the same reference type in use for the key. However, it should be noted that
 * non-strong values may disappear before their corresponding key.
 *
 * While this table does allow the use of both strong keys and values, it is
 * recommended to use {@link java.util.concurrent.ConcurrentHashMap} for such a
 * configuration, since it is optimized for that case.
 *
 * Just like {@link java.util.concurrent.ConcurrentHashMap}, this class obeys
 * the same functional specification as {@link java.util.Hashtable}, and
 * includes versions of methods corresponding to each method of
 * <tt>Hashtable</tt>. However, even though all operations are thread-safe,
 * retrieval operations do <em>not</em> entail locking, and there is
 * <em>not</em> any support for locking the entire table in a way that
 * prevents all access. This class is fully interoperable with
 * <tt>Hashtable</tt> in programs that rely on its thread safety but not on
 * its synchronization details.
 *
 * <p>
 * Retrieval operations (including <tt>get</tt>) generally do not block, so
 * may overlap with update operations (including <tt>put</tt> and
 * <tt>remove</tt>). Retrievals reflect the results of the most recently
 * <em>completed</em> update operations holding upon their onset. For
 * aggregate operations such as <tt>putAll</tt> and <tt>clear</tt>,
 * concurrent retrievals may reflect insertion or removal of only some entries.
 * Similarly, Iterators and Enumerations return elements reflecting the state of
 * the hash table at some point at or since the creation of the
 * iterator/enumeration. They do <em>not</em> throw
 * {@link ConcurrentModificationException}. However, iterators are designed to
 * be used by only one thread at a time.
 *
 * <p>
 * The allowed concurrency among update operations is guided by the optional
 * <tt>concurrencyLevel</tt> constructor argument (default <tt>16</tt>),
 * which is used as a hint for internal sizing. The table is internally
 * partitioned to try to permit the indicated number of concurrent updates
 * without contention. Because placement in hash tables is essentially random,
 * the actual concurrency will vary. Ideally, you should choose a value to
 * accommodate as many threads as will ever concurrently modify the table. Using
 * a significantly higher value than you need can waste space and time, and a
 * significantly lower value can lead to thread contention. But overestimates
 * and underestimates within an order of magnitude do not usually have much
 * noticeable impact. A value of one is appropriate when it is known that only
 * one thread will modify and all others will only read. Also, resizing this or
 * any other kind of hash table is a relatively slow operation, so, when
 * possible, it is a good idea to provide estimates of expected table sizes in
 * constructors.
 *
 * <p>
 * This class and its views and iterators implement all of the <em>optional</em>
 * methods of the {@link Map} and {@link Iterator} interfaces.
 *
 * <p>
 * Like {@link Hashtable} but unlike {@link HashMap}, this class does
 * <em>not</em> allow <tt>null</tt> to be used as a key or value.
 *
 * <p>
 * This class is a member of the <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author Doug Lea
 * @author Jason T. Greene
 * @param <K>
 *            the type of keys maintained by this map
 * @param <V>
 *            the type of mapped values
 */
public class ConcurrentReferenceHashMap<K, V> extends AbstractMap<K, V>
        implements java.util.concurrent.ConcurrentMap<K, V>, Serializable {

    private static final long serialVersionUID = 7249069246763182397L;

    /*
     * The basic strategy is to subdivide the table among Segments,
     * each of which itself is a concurrently readable hash table.
     */

    /**
     * An option specifying which Java reference type should be used to refer to
     * a key and/or value.
     */
    public static enum ReferenceType {
        /** Indicates a normal Java strong reference should be used */
        STRONG,
        /** Indicates a {@link WeakReference} should be used */
        WEAK,
        /** Indicates a {@link SoftReference} should be used */
        SOFT
    }


    public static enum Option {
        /**
         * Indicates that referential-equality (== instead of .equals()) should
         * be used when locating keys. This offers similar behavior to
         * {@link IdentityHashMap}
         */
        IDENTITY_COMPARISONS
    }

    /* ---------------- Constants -------------- */

    private static final ReferenceType DEFAULT_KEY_TYPE = ReferenceType.WEAK;

    private static final ReferenceType DEFAULT_VALUE_TYPE = ReferenceType.STRONG;


    /**
     * The default initial capacity for this table, used when not otherwise
     * specified in a constructor.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 32;

    /**
     * The default load factor for this table, used when not otherwise specified
     * in a constructor.
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The default concurrency level for this table, used when not otherwise
     * specified in a constructor.
     */
    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified by
     * either of the constructors with arguments. MUST be a power of two <= 1<<30
     * to ensure that entries are indexable using ints.
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The maximum number of segments to allow; used to bound constructor
     * arguments.
     */
    private static final int MAX_SEGMENTS = 1 << 16; // slightly conservative

    /**
     * Number of unsynchronized retries in size and containsValue methods before
     * resorting to locking. This is used to avoid unbounded retries if tables
     * undergo continuous modification which would make it impossible to obtain
     * an accurate result.
     */
    private static final int RETRIES_BEFORE_LOCK = 2;

    /* ---------------- Fields -------------- */

    /**
     * Mask value for indexing into segments. The upper bits of a key's hash
     * code are used to choose the segment.
     */
    private final int segmentMask;

    /**
     * Shift value for indexing within segments.
     */
    private final int segmentShift;

    /**
     * The segments, each of which is a specialized hash table
     */
    protected final Segment<K, V>[] segments;

    private boolean identityComparisons;

    private transient Set<K> keySet;
    private transient Set<Map.Entry<K, V>> entrySet;
    private transient Collection<V> values;

    private final ListenerList<RemoveStaleListener<? super V>> removeStaleListeners
        = new ListenerList<RemoveStaleListener<? super V>>();

    /* ---------------- Small Utilities -------------- */

    /**
     * Applies a supplemental hash function to a given hashCode, which defends
     * against poor quality hash functions. This is critical because
     * ConcurrentReferenceHashMap uses power-of-two length hash tables, that
     * otherwise encounter collisions for hashCodes that do not differ in lower
     * or upper bits.
     */
    private static int hash(final int h) {
        // Spread bits to regularize both segment and index locations,
        // using variant of single-word Wang/Jenkins hash.
        int hash = h + (h << 15) ^ 0xffffcd7d;
        hash ^= (hash >>> 10);
        hash += (hash << 3);
        hash ^= (hash >>> 6);
        hash += (hash << 2) + (hash << 14);
        return hash ^ (hash >>> 16);
    }

    /**
     * Returns the segment that should be used for key with given hash
     *
     * @param hash
     *            the hash code for the key
     * @return the segment
     */
    final Segment<K, V> segmentFor(final int hash) {
        return this.segments[(hash >>> this.segmentShift) & this.segmentMask];
    }

    private int hashOf(final Object key) {
        return hash(this.identityComparisons ? System.identityHashCode(key) : key
            .hashCode());
    }

    /* ---------------- Inner Classes -------------- */

    private static interface KeyReference {

        int keyHash();
    }

    /**
     * A weak-key reference which stores the key hash needed for reclamation.
     */
    private static final class WeakKeyReference<K> extends WeakReference<K> implements
            KeyReference {

        final int hash;

        WeakKeyReference(final K key, final int hash, final ReferenceQueue<K> refQueue) {
            super(key, refQueue);
            this.hash = hash;
        }

        public final int keyHash() {
            return this.hash;
        }
    }

    /**
     * A soft-key reference which stores the key hash needed for reclamation.
     */
    private static final class SoftKeyReference<K> extends SoftReference<K> implements
            KeyReference {

        final int hash;

        SoftKeyReference(final K key, final int hash, final ReferenceQueue<K> refQueue) {
            super(key, refQueue);
            this.hash = hash;
        }

        public final int keyHash() {
            return this.hash;
        }
    }

    /**
     * ConcurrentReferenceHashMap list entry. Note that this is never exported
     * out as a user-visible Map.Entry.
     *
     * Because the value field is volatile, not final, it is legal wrt the Java
     * Memory Model for an unsynchronized reader to see null instead of initial
     * value when read via a data race. Although a reordering leading to this is
     * not likely to ever actually occur, the Segment.readValueUnderLock method
     * is used as a backup in case a null (pre-initialized) value is ever seen
     * in an unsynchronized access method.
     */
    private static final class HashEntry<K, V> {

        protected final Object keyRef;
        protected final int hash;
        protected volatile Object valueRef;
        protected final HashEntry<K, V> next;

        protected HashEntry(final K key, final int hash, final HashEntry<K, V> next, final V value,
                final ReferenceType keyType, final ReferenceType valueType,
                final ReferenceQueue<K> refQueue) {
            this.keyRef = newKeyReference(key, keyType, hash, refQueue);
            this.hash = hash;
            this.next = next;
            this.valueRef = newValueReference(value, valueType);
        }

        private final Object newKeyReference(final K key, final ReferenceType keyType, final int hash1,
                final ReferenceQueue<K> refQueue) {
            if (keyType == ReferenceType.WEAK)
                return new WeakKeyReference<K>(key, hash1, refQueue);
            if (keyType == ReferenceType.SOFT)
                return new SoftKeyReference<K>(key, hash1, refQueue);

            return key;
        }

        final Object newValueReference(final V value, final ReferenceType valueType) {
            if (valueType == ReferenceType.WEAK)
                return new WeakReference<V>(value);
            if (valueType == ReferenceType.SOFT)
                return new SoftReference<V>(value);

            return value;
        }

        @SuppressWarnings("unchecked")
        final K key() {
            if (this.keyRef instanceof Reference)
                return ((Reference<K>) this.keyRef).get();

            return (K) this.keyRef;
        }

        final V value() {
            return dereferenceValue(this.valueRef);
        }

        @SuppressWarnings("unchecked")
        final V dereferenceValue(final Object value) {
            if (value instanceof Reference)
                return ((Reference<V>) value).get();

            return (V) value;
        }

        final void setValue(final V value, final ReferenceType valueType) {
            this.valueRef = newValueReference(value, valueType);
        }

        @SuppressWarnings("unchecked")
        static final <K, V> HashEntry<K, V>[] newArray(final int i) {
            return new HashEntry[i];
        }
    }

    /**
     * Segments are specialized versions of hash tables. This subclasses from
     * ReentrantLock opportunistically, just to simplify some locking and avoid
     * separate construction.
     */
    private static final class Segment<K, V> implements
            Serializable {

        /*
         * Segments maintain a table of entry lists that are ALWAYS
         * kept in a consistent state, so can be read without locking.
         * Next fields of nodes are immutable (final).  All list
         * additions are performed at the front of each bin. This
         * makes it easy to check changes, and also fast to traverse.
         * When nodes would otherwise be changed, new nodes are
         * created to replace them. This works well for hash tables
         * since the bin lists tend to be short. (The average length
         * is less than two for the default load factor threshold.)
         *
         * Read operations can thus proceed without locking, but rely
         * on selected uses of volatiles to ensure that completed
         * write operations performed by other threads are
         * noticed. For most purposes, the "count" field, tracking the
         * number of elements, serves as that volatile variable
         * ensuring visibility.  This is convenient because this field
         * needs to be read in many read operations anyway:
         *
         *   - All (unsynchronized) read operations must first read the
         *     "count" field, and should not look at table entries if
         *     it is 0.
         *
         *   - All (synchronized) write operations should write to
         *     the "count" field after structurally changing any bin.
         *     The operations must not take any action that could even
         *     momentarily cause a concurrent read operation to see
         *     inconsistent data. This is made easier by the nature of
         *     the read operations in Map. For example, no operation
         *     can reveal that the table has grown but the threshold
         *     has not yet been updated, so there are no atomicity
         *     requirements for this with respect to reads.
         *
         * As a guide, all critical volatile reads and writes to the
         * count field are marked in code comments.
         */

        private static final long serialVersionUID = 2249069246763182397L;

        /**
         * The number of elements in this segment's region.
         */
        transient volatile int count;

        /**
         * Number of updates that alter the size of the table. This is used
         * during bulk-read methods to make sure they see a consistent snapshot:
         * If modCounts change during a traversal of segments computing size or
         * checking containsValue, then we might have an inconsistent view of
         * state so (usually) must retry.
         */
        transient int modCount;

        /**
         * The table is rehashed when its size exceeds this threshold. (The
         * value of this field is always <tt>(int)(capacity *
         * loadFactor)</tt>.)
         */
        private transient int threshold;

        /**
         * The per-segment table.
         */
        transient volatile HashEntry<K, V>[] table;

        /**
         * The load factor for the hash table. Even though this value is same
         * for all segments, it is replicated to avoid needing links to outer
         * object.
         *
         * @serial
         */
        private final float loadFactor;

        /**
         * The collected weak-key reference queue for this segment. This should
         * be (re)initialized whenever table is assigned,
         */
        private transient volatile ReferenceQueue<K> refQueue;

        private final ReferenceType keyType;

        private final ReferenceType valueType;

        private final boolean identityComparisons;

        private final ListenerList<RemoveStaleListener<? super V>> removeStaleListeners;

        Segment(final int initialCapacity, final float lf, final ReferenceType keyType,
                final ReferenceType valueType, final boolean identityComparisons,
                final ListenerList<RemoveStaleListener<? super V>> removeStaleListeners) {
            this.loadFactor = lf;
            this.keyType = keyType;
            this.valueType = valueType;
            this.identityComparisons = identityComparisons;
            this.removeStaleListeners = removeStaleListeners;
            setTable(HashEntry.<K, V> newArray(initialCapacity));
        }

        @SuppressWarnings("unchecked")
        static final <K, V> Segment<K, V>[] newArray(final int i) {
            return new Segment[i];
        }

        private boolean keyEq(final Object src, final Object dest) {
            return this.identityComparisons ? src == dest : src.equals(dest);
        }

        /**
         * Sets table to new HashEntry array. Call only while holding lock or in
         * constructor.
         */
        void setTable(final HashEntry<K, V>[] newTable) {
            this.threshold = (int) (newTable.length * this.loadFactor);
            this.table = newTable;
            this.refQueue = new ReferenceQueue<K>();
        }

        /**
         * Returns properly casted first entry of bin for given hash.
         */
        HashEntry<K, V> getFirst(final int hash) {
            final HashEntry<K, V>[] tab = this.table;
            return tab[hash & (tab.length - 1)];
        }

        HashEntry<K, V> newHashEntry(final K key, final int hash, final HashEntry<K, V> next,
                final V value) {
            return new HashEntry<K, V>(key, hash, next, value, this.keyType,
                    this.valueType, this.refQueue);
        }

        /**
         * Reads value field of an entry under lock. Called if value field ever
         * appears to be null. This is possible only if a compiler happens to
         * reorder a HashEntry initialization with its table assignment, which
         * is legal under memory model but is not known to ever occur.
         */
        synchronized V readValueUnderLock(final HashEntry<K, V> e) {
            removeStale();
            return e.value();
        }

        /* Specialized implementations of map methods */

        V get(final Object key, final int hash) {
            if (this.count != 0) { // read-volatile
                HashEntry<K, V> e = getFirst(hash);
                while (e != null) {
                    if (e.hash == hash && keyEq(key, e.key())) {
                        final Object opaque = e.valueRef;
                        if (opaque != null)
                            return e.dereferenceValue(opaque);

                        return readValueUnderLock(e); // recheck
                    }
                    e = e.next;
                }
            }
            return null;
        }

        boolean containsKey(final Object key, final int hash) {
            if (this.count != 0) { // read-volatile
                HashEntry<K, V> e = getFirst(hash);
                while (e != null) {
                    if (e.hash == hash && keyEq(key, e.key()))
                        return true;
                    e = e.next;
                }
            }
            return false;
        }

        boolean containsValue(final Object value) {
            if (this.count != 0) { // read-volatile
                final HashEntry<K, V>[] tab = this.table;
                final int len = tab.length;
                for (int i = 0; i < len; i++) {
                    for (HashEntry<K, V> e = tab[i]; e != null; e = e.next) {
                        final Object opaque = e.valueRef;

                        final V v;
                        if (opaque == null)
                            v = readValueUnderLock(e); // recheck
                        else
                            v = e.dereferenceValue(opaque);

                        if (value.equals(v))
                            return true;
                    }
                }
            }
            return false;
        }

        synchronized boolean replace(final K key, final int hash, final V oldValue, final V newValue) {
            removeStale();
            HashEntry<K, V> e = getFirst(hash);
            while (e != null && (e.hash != hash || !keyEq(key, e.key())))
                e = e.next;

            boolean replaced = false;
            if (e != null && oldValue.equals(e.value())) {
                replaced = true;
                e.setValue(newValue, this.valueType);
            }
            return replaced;
        }

        synchronized V replace(final K key, final int hash, final V newValue) {
            removeStale();
            HashEntry<K, V> e = getFirst(hash);
            while (e != null && (e.hash != hash || !keyEq(key, e.key())))
                e = e.next;

            V oldValue = null;
            if (e != null) {
                oldValue = e.value();
                e.setValue(newValue, this.valueType);
            }
            return oldValue;
        }


        synchronized V put(final K key, final int hash, final V value, final boolean onlyIfAbsent) {
            removeStale();
            int c = this.count;
            if (c++ > this.threshold) {// ensure capacity
                final int reduced = rehash();
                if (reduced > 0) // adjust from possible weak cleanups
                    this.count = (c -= reduced) - 1; // write-volatile
            }

            final HashEntry<K, V>[] tab = this.table;
            final int index = hash & (tab.length - 1);
            final HashEntry<K, V> first = tab[index];
            HashEntry<K, V> e = first;
            while (e != null && (e.hash != hash || !keyEq(key, e.key())))
                e = e.next;

            V oldValue;
            if (e != null) {
                oldValue = e.value();
                if (!onlyIfAbsent)
                    e.setValue(value, this.valueType);
            } else {
                oldValue = null;
                ++this.modCount;
                tab[index] = newHashEntry(key, hash, first, value);
                this.count = c; // write-volatile
            }
            return oldValue;
        }

        int rehash() {
            final HashEntry<K, V>[] oldTable = this.table;
            final int oldCapacity = oldTable.length;
            if (oldCapacity >= MAXIMUM_CAPACITY)
                return 0;

            /*
             * Reclassify nodes in each list to new Map.  Because we are
             * using power-of-two expansion, the elements from each bin
             * must either stay at same index, or move with a power of two
             * offset. We eliminate unnecessary node creation by catching
             * cases where old nodes can be reused because their next
             * fields won't change. Statistically, at the default
             * threshold, only about one-sixth of them need cloning when
             * a table doubles. The nodes they replace will be garbage
             * collectable as soon as they are no longer referenced by any
             * reader thread that may be in the midst of traversing table
             * right now.
             */

            final HashEntry<K, V>[] newTable = HashEntry.newArray(oldCapacity << 1);
            this.threshold = (int) (newTable.length * this.loadFactor);
            final int sizeMask = newTable.length - 1;
            int reduce = 0;
            for (int i = 0; i < oldCapacity; i++) {
                // We need to guarantee that any existing reads of old Map can
                // proceed. So we cannot yet null out each bin.
                final HashEntry<K, V> e = oldTable[i];

                if (e != null) {
                    final HashEntry<K, V> next = e.next;
                    final int idx = e.hash & sizeMask;

                    // Single node on list
                    if (next == null)
                        newTable[idx] = e;

                    else {
                        // Reuse trailing consecutive sequence at same slot
                        HashEntry<K, V> lastRun = e;
                        int lastIdx = idx;
                        for (HashEntry<K, V> last = next; last != null; last =
                                last.next) {
                            final int k = last.hash & sizeMask;
                            if (k != lastIdx) {
                                lastIdx = k;
                                lastRun = last;
                            }
                        }
                        newTable[lastIdx] = lastRun;
                        // Clone all remaining nodes
                        for (HashEntry<K, V> p = e; p != lastRun; p = p.next) {
                            final K key = p.key();
                            // only skip GC'd weak refs if there are no listeners
                            if (this.removeStaleListeners.size() == 0 && key == null) {
                                reduce++;
                                continue;
                            }
                            final int k = p.hash & sizeMask;
                            final HashEntry<K, V> n = newTable[k];
                            newTable[k] =
                                    newHashEntry(key, p.hash, n, p.value());
                        }
                    }
                }
            }
            this.table = newTable;
            return reduce;
        }

        /**
         * Remove; match on key only if value null, else match both.
         */
        synchronized V remove(final Object key, final int hash, final Object value, final boolean weakRemove) {
            if (!weakRemove)
                removeStale();
            int c = this.count - 1;
            final HashEntry<K, V>[] tab = this.table;
            final int index = hash & (tab.length - 1);
            final HashEntry<K, V> first = tab[index];
            HashEntry<K, V> e = first;
            // a weak remove operation compares the WeakReference instance
            while (e != null && (!weakRemove || key != e.keyRef)
                    && (e.hash != hash || !keyEq(key, e.key())))
                e = e.next;

            V oldValue = null;
            if (e != null) {
                final V v = e.value();
                if (value == null || value.equals(v)) {
                    oldValue = v;
                    // All entries following removed node can stay
                    // in list, but all preceding ones need to be
                    // cloned.
                    ++this.modCount;
                    HashEntry<K, V> newFirst = e.next;
                    for (HashEntry<K, V> p = first; p != e; p = p.next) {
                        final K pKey = p.key();
                        // Skip GC'd keys only if no listeners are present
                        if (this.removeStaleListeners.size() == 0 && pKey == null) {
                            c--;
                            continue;
                        }

                        newFirst =
                                newHashEntry(pKey, p.hash, newFirst, p
                                    .value());
                    }
                    tab[index] = newFirst;
                    this.count = c; // write-volatile
                }
            }
            return oldValue;
        }

        final void removeStale() {
            if (this.keyType == ReferenceType.STRONG)
                return;

            KeyReference ref;
            while ((ref = (KeyReference) this.refQueue.poll()) != null) {
                final V removedValue = remove(ref, ref.keyHash(), null, true);
                if (removedValue != null)
                    for (final RemoveStaleListener<? super V> rl: this.removeStaleListeners) {
                        rl.removed(removedValue);
                    }
            }
        }

        synchronized void clear() {
            if (this.count != 0) {
                final HashEntry<K, V>[] tab = this.table;
                for (int i = 0; i < tab.length; i++)
                    tab[i] = null;
                ++this.modCount;
                // replace the reference queue to avoid unnecessary stale
                // cleanups
                this.refQueue = new ReferenceQueue<K>();
                this.count = 0; // write-volatile
            }
        }

        static void executeUnderLock(final Segment<?, ?>[] segments, final Runnable runnable) {
            if (segments.length == 0)
                runnable.run();
            else
                segments[0].executeUnderLock(segments, 1, runnable);
        }

        private synchronized void executeUnderLock(final Segment<?, ?>[] segments,
                final int nextIndex, final Runnable runnable) {
            if (nextIndex < segments.length)
                segments[nextIndex].executeUnderLock(segments, nextIndex + 1, runnable);
            else
                runnable.run();
        }
    }

    public static class ListenerList<V> implements Iterable<V> {
        private Object[] listeners = new Object[0];

        public synchronized void add(final V obj) {
            final Object[] newListeners = new Object[this.listeners.length+1];
            System.arraycopy(this.listeners, 0, newListeners, 0, this.listeners.length);
            newListeners[this.listeners.length] = obj;
            this.listeners = newListeners;
        }

        public int size() {
            return this.listeners.length;
        }

        public synchronized void remove(final V obj) {
            for (int i = 0; i < this.listeners.length; ++i) {
                if (this.listeners[i].equals(obj)) {
                    final Object[] newListeners = new Object[this.listeners.length-1];
                    System.arraycopy(this.listeners, 0, newListeners, 0, i);
                    if (i+1 < this.listeners.length) {
                        System.arraycopy(this.listeners, i+1, newListeners, i, this.listeners.length-i-1);
                    }
                    this.listeners = newListeners;
                }
            }
        }

        public Iterator<V> iterator() {
            return new ListenerListItr<V>(this.listeners);
        }

        private static class ListenerListItr<V> implements Iterator<V> {

            private final Object[] listeners;
            private int index = 0;

            public ListenerListItr(final Object[] listeners) {
                this.listeners = listeners;
            }

            public boolean hasNext() {
                return this.index < this.listeners.length;
            }

            @SuppressWarnings("unchecked")
            public V next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return (V) this.listeners[this.index++];
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

        }

    }

    /**
     * A Listener to be notified whenever an weak element of this map is automatically
     * removed. It will not be notified when the user calls the remove method!
     */
    public static interface RemoveStaleListener<V> {
        void removed(V removedValue);
    }

    /* ---------------- Public operations -------------- */

    /**
     * Creates a new, empty map with the specified initial capacity, reference
     * types, load factor and concurrency level.
     *
     * Behavioral changing options such as {@link Option#IDENTITY_COMPARISONS}
     * can also be specified.
     *
     * @param initialCapacity
     *            the initial capacity. The implementation performs internal
     *            sizing to accommodate this many elements.
     * @param loadFactor
     *            the load factor threshold, used to control resizing. Resizing
     *            may be performed when the average number of elements per bin
     *            exceeds this threshold.
     * @param concurrencyLevel
     *            the estimated number of concurrently updating threads. The
     *            implementation performs internal sizing to try to accommodate
     *            this many threads.
     * @param keyType
     *            the reference type to use for keys
     * @param valueType
     *            the reference type to use for values
     * @param options
     *            the behavioral options
     * @throws IllegalArgumentException
     *             if the initial capacity is negative or the load factor or
     *             concurrencyLevel are nonpositive.
     */
    public ConcurrentReferenceHashMap(final int initialCapacity, final float loadFactor,
            final int concurrencyLevel, final ReferenceType keyType,
            final ReferenceType valueType, final EnumSet<Option> options) {
        if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0)
            throw new IllegalArgumentException();

        final int chosenConcurrencyLevel = Math.min(concurrencyLevel, MAX_SEGMENTS);

        // Find power-of-two sizes best matching arguments
        int sshift = 0;
        int ssize = 1;
        while (ssize < chosenConcurrencyLevel) {
            ++sshift;
            ssize <<= 1;
        }
        this.segmentShift = 32 - sshift;
        this.segmentMask = ssize - 1;
        this.segments = Segment.newArray(ssize);

        final int chosenInitialCapacity = Math.min(initialCapacity, MAXIMUM_CAPACITY);
        int c = chosenInitialCapacity / ssize;
        if (c * ssize < chosenInitialCapacity)
            ++c;
        int cap = 1;
        while (cap < c)
            cap <<= 1;

        this.identityComparisons =
                options != null
                        && options.contains(Option.IDENTITY_COMPARISONS);

        for (int i = 0; i < this.segments.length; ++i)
            this.segments[i] =
                    new Segment<K, V>(cap, loadFactor, keyType, valueType,
                            this.identityComparisons, this.removeStaleListeners);
    }

    /**
     * Creates a new, empty map with the specified initial capacity, load factor
     * and concurrency level.
     *
     * @param initialCapacity
     *            the initial capacity. The implementation performs internal
     *            sizing to accommodate this many elements.
     * @param loadFactor
     *            the load factor threshold, used to control resizing. Resizing
     *            may be performed when the average number of elements per bin
     *            exceeds this threshold.
     * @param concurrencyLevel
     *            the estimated number of concurrently updating threads. The
     *            implementation performs internal sizing to try to accommodate
     *            this many threads.
     * @throws IllegalArgumentException
     *             if the initial capacity is negative or the load factor or
     *             concurrencyLevel are nonpositive.
     */
    public ConcurrentReferenceHashMap(final int initialCapacity, final float loadFactor,
            final int concurrencyLevel) {
        this(initialCapacity, loadFactor, concurrencyLevel, DEFAULT_KEY_TYPE,
                DEFAULT_VALUE_TYPE, null);
    }

    /**
     * Creates a new, empty map with the specified initial capacity and load
     * factor and with the default reference types (weak keys, strong values),
     * and concurrencyLevel (16).
     *
     * @param initialCapacity
     *            The implementation performs internal sizing to accommodate
     *            this many elements.
     * @param loadFactor
     *            the load factor threshold, used to control resizing. Resizing
     *            may be performed when the average number of elements per bin
     *            exceeds this threshold.
     * @throws IllegalArgumentException
     *             if the initial capacity of elements is negative or the load
     *             factor is nonpositive
     *
     * @since 1.6
     */
    public ConcurrentReferenceHashMap(final int initialCapacity, final float loadFactor) {
        this(initialCapacity, loadFactor, DEFAULT_CONCURRENCY_LEVEL);
    }


    /**
     * Creates a new, empty map with the specified initial capacity, reference
     * types and with default load factor (0.75) and concurrencyLevel (16).
     *
     * @param initialCapacity
     *            the initial capacity. The implementation performs internal
     *            sizing to accommodate this many elements.
     * @param keyType
     *            the reference type to use for keys
     * @param valueType
     *            the reference type to use for values
     * @throws IllegalArgumentException
     *             if the initial capacity of elements is negative.
     */
    public ConcurrentReferenceHashMap(final int initialCapacity,
            final ReferenceType keyType, final ReferenceType valueType) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL,
                keyType, valueType, null);
    }

    /**
     * Creates a new, empty map with the specified initial capacity, and with
     * default reference types (weak keys, strong values), load factor (0.75)
     * and concurrencyLevel (16).
     *
     * @param initialCapacity
     *            the initial capacity. The implementation performs internal
     *            sizing to accommodate this many elements.
     * @throws IllegalArgumentException
     *             if the initial capacity of elements is negative.
     */
    public ConcurrentReferenceHashMap(final int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
    }

    /**
     * Creates a new, empty map with a default initial capacity (16), reference
     * types (weak keys, strong values), default load factor (0.75) and
     * concurrencyLevel (16).
     */
    public ConcurrentReferenceHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR,
                DEFAULT_CONCURRENCY_LEVEL);
    }

    /**
     * Creates a new map with the same mappings as the given map. The map is
     * created with a capacity of 1.5 times the number of mappings in the given
     * map or 16 (whichever is greater), and a default load factor (0.75) and
     * concurrencyLevel (16).
     *
     * @param m
     *            the map
     */
    public ConcurrentReferenceHashMap(final Map<? extends K, ? extends V> m) {
        this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1,
            DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR,
                DEFAULT_CONCURRENCY_LEVEL);
        putAll(m);
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    @Override
    public boolean isEmpty() {
        final Segment<K, V>[] segments1 = this.segments;
        /*
         * We keep track of per-segment modCounts to avoid ABA
         * problems in which an element in one segment was added and
         * in another removed during traversal, in which case the
         * table was never actually empty at any point. Note the
         * similar use of modCounts in the size() and containsValue()
         * methods, which are the only other methods also susceptible
         * to ABA problems.
         */
        final int[] mc = new int[segments1.length];
        int mcsum = 0;
        for (int i = 0; i < segments1.length; ++i) {
            if (segments1[i].count != 0)
                return false;
            mcsum += mc[i] = segments1[i].modCount;
        }
        // If mcsum happens to be zero, then we know we got a snapshot
        // before any modifications at all were made. This is
        // probably common enough to bother tracking.
        if (mcsum != 0) {
            for (int i = 0; i < segments1.length; ++i) {
                if (segments1[i].count != 0 || mc[i] != segments1[i].modCount)
                    return false;
            }
        }
        return true;
    }

    /**
     * Returns the number of key-value mappings in this map. If the map contains
     * more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of key-value mappings in this map
     */
    @Override
    public int size() {
        final Segment<K, V>[] segments1 = this.segments;
        long sum = 0;
        long check = 0;
        final int[] mc = new int[segments1.length];
        // Try a few times to get accurate count. On failure due to
        // continuous async changes in table, resort to locking.
        for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
            check = 0;
            sum = 0;
            int mcsum = 0;
            for (int i = 0; i < segments1.length; ++i) {
                sum += segments1[i].count;
                mcsum += mc[i] = segments1[i].modCount;
            }
            if (mcsum != 0) {
                for (int i = 0; i < segments1.length; ++i) {
                    check += segments1[i].count;
                    if (mc[i] != segments1[i].modCount) {
                        check = -1; // force retry
                        break;
                    }
                }
            }
            if (check == sum)
                break;
        }
        if (check != sum) { // Resort to locking all segments
            final long[] sumArr = new long[1];
            Segment.executeUnderLock(this.segments, new Runnable() {
                public void run() {
                    for (final Segment<K, V> seg: ConcurrentReferenceHashMap.this.segments)
                        sumArr[0] += seg.count;
                }
            });
            sum = sumArr[0];
        }
        if (sum > Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        return (int) sum;
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null}
     * if this map contains no mapping for the key.
     *
     * <p>
     * More formally, if this map contains a mapping from a key {@code k} to a
     * value {@code v} such that {@code key.equals(k)}, then this method
     * returns {@code v}; otherwise it returns {@code null}. (There can be at
     * most one such mapping.)
     *
     * @throws NullPointerException
     *             if the specified key is null
     */
    @Override
    public V get(final Object key) {
        final int hash = hashOf(key);
        return segmentFor(hash).get(key, hash);
    }

    /**
     * Tests if the specified object is a key in this table.
     *
     * @param key
     *            possible key
     * @return <tt>true</tt> if and only if the specified object is a key in
     *         this table, as determined by the <tt>equals</tt> method;
     *         <tt>false</tt> otherwise.
     * @throws NullPointerException
     *             if the specified key is null
     */
    @Override
    public boolean containsKey(final Object key) {
        final int hash = hashOf(key);
        return segmentFor(hash).containsKey(key, hash);
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value. Note: This method requires a full internal traversal of
     * the hash table, and so is much slower than method <tt>containsKey</tt>.
     *
     * @param value
     *            value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value
     * @throws NullPointerException
     *             if the specified value is null
     */
    @Override
    public boolean containsValue(final Object value) {
        if (value == null)
            throw new NullPointerException();

        // See explanation of modCount use above

        final Segment<K, V>[] segments1 = this.segments;
        final int[] mc = new int[segments1.length];

        // unused, just for doing volatile-reads
        int tmp = 0;

        // Try a few times without locking
        for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
            int mcsum = 0;
            for (int i = 0; i < segments1.length; ++i) {
                // volatile-read
                tmp += segments1[i].count;
                mcsum += mc[i] = segments1[i].modCount;
                if (segments1[i].containsValue(value))
                    return true;
            }
            boolean cleanSweep = true;
            if (mcsum != 0) {
                for (int i = 0; i < segments1.length; ++i) {
                    // volatile-read
                    tmp += segments1[i].count;
                    if (mc[i] != segments1[i].modCount) {
                        cleanSweep = false;
                        break;
                    }
                }
            }
            if (cleanSweep)
                return false;
        }
        final boolean[] found = new boolean[1];
        Segment.executeUnderLock(this.segments, new Runnable() {
            public void run() {
               for (int i = 0; i < segments1.length; ++i) {
                   if (segments1[i].containsValue(value)) {
                       found[0] = true;
                       break;
                   }
               }
            }
        });
        return found[0];
    }

    /**
     * Legacy method testing if some key maps into the specified value in this
     * table. This method is identical in functionality to
     * {@link #containsValue}, and exists solely to ensure full compatibility
     * with class {@link java.util.Hashtable}, which supported this method
     * prior to introduction of the Java Collections framework.
     *
     * @param value
     *            a value to search for
     * @return <tt>true</tt> if and only if some key maps to the
     *         <tt>value</tt> argument in this table as determined by the
     *         <tt>equals</tt> method; <tt>false</tt> otherwise
     * @throws NullPointerException
     *             if the specified value is null
     */
    public boolean contains(final Object value) {
        return containsValue(value);
    }

    /**
     * Maps the specified key to the specified value in this table. Neither the
     * key nor the value can be null.
     *
     * <p>
     * The value can be retrieved by calling the <tt>get</tt> method with a
     * key that is equal to the original key.
     *
     * @param key
     *            key with which the specified value is to be associated
     * @param value
     *            value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>
     * @throws NullPointerException
     *             if the specified key or value is null
     */
    @Override
    public V put(final K key, final V value) {
        if (value == null)
            throw new NullPointerException();
        final int hash = hashOf(key);
        return segmentFor(hash).put(key, hash, value, false);
    }

    /**
     * {@inheritDoc}
     *
     * @return the previous value associated with the specified key, or
     *         <tt>null</tt> if there was no mapping for the key
     * @throws NullPointerException
     *             if the specified key or value is null
     */
    public V putIfAbsent(final K key, final V value) {
        if (value == null)
            throw new NullPointerException();
        final int hash = hashOf(key);
        return segmentFor(hash).put(key, hash, value, true);
    }

    /**
     * Copies all of the mappings from the specified map to this one. These
     * mappings replace any mappings that this map had for any of the keys
     * currently in the specified map.
     *
     * @param m
     *            mappings to be stored in this map
     */
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        for (final Map.Entry<? extends K, ? extends V> e: m.entrySet())
            put(e.getKey(), e.getValue());
    }

    /**
     * Removes the key (and its corresponding value) from this map. This method
     * does nothing if the key is not in the map.
     *
     * @param key
     *            the key that needs to be removed
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>
     * @throws NullPointerException
     *             if the specified key is null
     */
    @Override
    public V remove(final Object key) {
        final int hash = hashOf(key);
        return segmentFor(hash).remove(key, hash, null, false);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException
     *             if the specified key is null
     */
    public boolean remove(final Object key, final Object value) {
        final int hash = hashOf(key);
        if (value == null)
            return false;
        return segmentFor(hash).remove(key, hash, value, false) != null;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException
     *             if any of the arguments are null
     */
    public boolean replace(final K key, final V oldValue, final V newValue) {
        if (oldValue == null || newValue == null)
            throw new NullPointerException();
        final int hash = hashOf(key);
        return segmentFor(hash).replace(key, hash, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     *
     * @return the previous value associated with the specified key, or
     *         <tt>null</tt> if there was no mapping for the key
     * @throws NullPointerException
     *             if the specified key or value is null
     */
    public V replace(final K key, final V value) {
        if (value == null)
            throw new NullPointerException();
        final int hash = hashOf(key);
        return segmentFor(hash).replace(key, hash, value);
    }

    /**
     * Removes all of the mappings from this map.
     */
    @Override
    public void clear() {
        for (final Segment<K, V> s: this.segments)
            s.clear();
    }

    /**
     * Removes any stale entries whose keys have been finalized. Use of this
     * method is normally not necessary since stale entries are automatically
     * removed lazily, when blocking operations are required. However, there are
     * some cases where this operation should be performed eagerly, such as
     * cleaning up old references to a ClassLoader in a multi-classloader
     * environment.
     */
    public void purgeStaleEntries() {
        for (final Segment<K, V> s: this.segments)
            s.removeStale();
    }


    /**
     * Returns a {@link Set} view of the keys contained in this map. The set is
     * backed by the map, so changes to the map are reflected in the set, and
     * vice-versa. The set supports element removal, which removes the
     * corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
     * <tt>clear</tt> operations. It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * <p>
     * The view's <tt>iterator</tt> is a "weakly consistent" iterator that
     * will never throw {@link ConcurrentModificationException}, and guarantees
     * to traverse elements as they existed upon construction of the iterator,
     * and may (but is not guaranteed to) reflect any modifications subsequent
     * to construction.
     */
    @Override
    public Set<K> keySet() {
        final Set<K> ks = this.keySet;
        return (ks != null) ? ks : (this.keySet = new KeySet());
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are reflected
     * in the collection, and vice-versa. The collection supports element
     * removal, which removes the corresponding mapping from this map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations. It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     *
     * <p>
     * The view's <tt>iterator</tt> is a "weakly consistent" iterator that
     * will never throw {@link ConcurrentModificationException}, and guarantees
     * to traverse elements as they existed upon construction of the iterator,
     * and may (but is not guaranteed to) reflect any modifications subsequent
     * to construction.
     */
    @Override
    public Collection<V> values() {
        final Collection<V> vs = this.values;
        return (vs != null) ? vs : (this.values = new Values());
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map. The set
     * is backed by the map, so changes to the map are reflected in the set, and
     * vice-versa. The set supports element removal, which removes the
     * corresponding mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
     * <tt>clear</tt> operations. It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * <p>
     * The view's <tt>iterator</tt> is a "weakly consistent" iterator that
     * will never throw {@link ConcurrentModificationException}, and guarantees
     * to traverse elements as they existed upon construction of the iterator,
     * and may (but is not guaranteed to) reflect any modifications subsequent
     * to construction.
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        final Set<Map.Entry<K, V>> es = this.entrySet;
        return (es != null) ? es : (this.entrySet = new EntrySet());
    }

    /**
     * Returns an enumeration of the keys in this table.
     *
     * @return an enumeration of the keys in this table
     * @see #keySet()
     */
    public Enumeration<K> keys() {
        return new KeyIterator();
    }

    /**
     * Returns an enumeration of the values in this table.
     *
     * @return an enumeration of the values in this table
     * @see #values()
     */
    public Enumeration<V> elements() {
        return new ValueIterator();
    }

    /* ---------------- Iterator Support -------------- */

    private abstract class HashIterator {

        int nextSegmentIndex;
        int nextTableIndex;
        HashEntry<K, V>[] currentTable;
        HashEntry<K, V> nextEntry;
        HashEntry<K, V> lastReturned;
        K currentKey; // Strong reference to weak key (prevents gc)

        HashIterator() {
            this.nextSegmentIndex = ConcurrentReferenceHashMap.this.segments.length - 1;
            this.nextTableIndex = -1;
            advance();
        }

        public boolean hasMoreElements() {
            return hasNext();
        }

        final void advance() {
            if (this.nextEntry != null && (this.nextEntry = this.nextEntry.next) != null)
                return;

            while (this.nextTableIndex >= 0) {
                if ((this.nextEntry = this.currentTable[this.nextTableIndex--]) != null)
                    return;
            }

            while (this.nextSegmentIndex >= 0) {
                final Segment<K, V> seg = ConcurrentReferenceHashMap.this.segments[this.nextSegmentIndex--];
                if (seg.count != 0) {
                    this.currentTable = seg.table;
                    for (int j = this.currentTable.length - 1; j >= 0; --j) {
                        if ((this.nextEntry = this.currentTable[j]) != null) {
                            this.nextTableIndex = j - 1;
                            return;
                        }
                    }
                }
            }
        }

        public boolean hasNext() {
            while (this.nextEntry != null) {
                if (this.nextEntry.key() != null)
                    return true;
                advance();
            }

            return false;
        }

        HashEntry<K, V> nextEntry() {
            do {
                if (this.nextEntry == null)
                    throw new NoSuchElementException();

                this.lastReturned = this.nextEntry;
                this.currentKey = this.lastReturned.key();
                advance();
            } while (this.currentKey == null); // Skip GC'd keys

            return this.lastReturned;
        }

        public void remove() {
            if (this.lastReturned == null)
                throw new IllegalStateException();
            ConcurrentReferenceHashMap.this.remove(this.currentKey);
            this.lastReturned = null;
        }
    }

    private final class KeyIterator extends HashIterator implements Iterator<K>,
            Enumeration<K> {

        protected KeyIterator() {
            super();
        }

        public K next() {
            return super.nextEntry().key();
        }

        public K nextElement() {
            return super.nextEntry().key();
        }
    }

    private final class ValueIterator extends HashIterator implements Iterator<V>,
            Enumeration<V> {

        protected ValueIterator() {
            super();
        }

        public V next() {
            return super.nextEntry().value();
        }

        public V nextElement() {
            return super.nextEntry().value();
        }
    }

    /*
     * This class is needed for JDK5 compatibility.
     */
    private static class SimpleEntry<K, V> implements Entry<K, V>, java.io.Serializable {

        private static final long serialVersionUID = -8499721149061103585L;

        private final K key;
        private V value;

        public SimpleEntry(final K key, final V value) {
            this.key = key;
            this.value = value;
        }

        public SimpleEntry(final Entry<? extends K, ? extends V> entry) {
            this.key = entry.getKey();
            this.value = entry.getValue();
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }

        public V setValue(final V value) {
            final V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return eq(this.key, e.getKey()) && eq(this.value, e.getValue());
        }

        @Override
        public int hashCode() {
            return (this.key == null ? 0 : this.key.hashCode())
                    ^ (this.value == null ? 0 : this.value.hashCode());
        }

        @Override
        public String toString() {
            return this.key + "=" + this.value;
        }

        private static boolean eq(final Object o1, final Object o2) {
            return o1 == null ? o2 == null : o1.equals(o2);
        }
    }


    /**
     * Custom Entry class used by EntryIterator.next(), that relays setValue
     * changes to the underlying map.
     */
    private final class WriteThroughEntry extends SimpleEntry<K, V> {

        private static final long serialVersionUID = -7900634345345313646L;

        WriteThroughEntry(final K k, final V v) {
            super(k, v);
        }

        /**
         * Set our entry's value and write through to the map. The value to
         * return is somewhat arbitrary here. Since a WriteThroughEntry does not
         * necessarily track asynchronous changes, the most recent "previous"
         * value could be different from what we return (or could even have been
         * removed in which case the put will re-establish). We do not and
         * cannot guarantee more.
         */
        @Override
        public V setValue(final V value) {
            if (value == null)
                throw new NullPointerException();
            final V v = super.setValue(value);
            ConcurrentReferenceHashMap.this.put(getKey(), value);
            return v;
        }
    }

    private final class EntryIterator extends HashIterator implements
            Iterator<Entry<K, V>> {

        protected EntryIterator() {
            super();
        }

        public Map.Entry<K, V> next() {
            final HashEntry<K, V> e = super.nextEntry();
            return new WriteThroughEntry(e.key(), e.value());
        }
    }

    private final class KeySet extends AbstractSet<K> {

        protected KeySet() {
            super();
        }

        @Override
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        @Override
        public int size() {
            return ConcurrentReferenceHashMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return ConcurrentReferenceHashMap.this.isEmpty();
        }

        @Override
        public boolean contains(final Object o) {
            return ConcurrentReferenceHashMap.this.containsKey(o);
        }

        @Override
        public boolean remove(final Object o) {
            return ConcurrentReferenceHashMap.this.remove(o) != null;
        }

        @Override
        public void clear() {
            ConcurrentReferenceHashMap.this.clear();
        }
    }

    private final class Values extends AbstractCollection<V> {

        protected Values() {
            super();
        }

        @Override
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        @Override
        public int size() {
            return ConcurrentReferenceHashMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return ConcurrentReferenceHashMap.this.isEmpty();
        }

        @Override
        public boolean contains(final Object o) {
            return ConcurrentReferenceHashMap.this.containsValue(o);
        }

        @Override
        public void clear() {
            ConcurrentReferenceHashMap.this.clear();
        }
    }

    private final class EntrySet extends AbstractSet<Map.Entry<K, V>> {

        protected EntrySet() {
            super();
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public boolean contains(final Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            final V v = ConcurrentReferenceHashMap.this.get(e.getKey());
            return v != null && v.equals(e.getValue());
        }

        @Override
        public boolean remove(final Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return ConcurrentReferenceHashMap.this.remove(e.getKey(), e
                .getValue());
        }

        @Override
        public int size() {
            return ConcurrentReferenceHashMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return ConcurrentReferenceHashMap.this.isEmpty();
        }

        @Override
        public void clear() {
            ConcurrentReferenceHashMap.this.clear();
        }
    }

    /* ---------------- Serialization Support -------------- */

    /**
     * Save the state of the <tt>ConcurrentReferenceHashMap</tt> instance to a
     * stream (i.e., serialize it).
     *
     * @param s
     *            the stream
     * @serialData the key (Object) and value (Object) for each key-value
     *             mapping, followed by a null pair. The key-value mappings are
     *             emitted in no particular order.
     */
    private void writeObject(final java.io.ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();

        for (int k = 0; k < this.segments.length; ++k) {
            final Segment<K, V> seg = this.segments[k];
            synchronized (seg) {
                final HashEntry<K, V>[] tab = seg.table;
                for (int i = 0; i < tab.length; ++i) {
                    for (HashEntry<K, V> e = tab[i]; e != null; e = e.next) {
                        final K key = e.key();
                        if (key == null) // Skip GC'd keys
                            continue;

                        s.writeObject(key);
                        s.writeObject(e.value());
                    }
                }
            }
        }
        s.writeObject(null);
        s.writeObject(null);
    }

    /**
     * Reconstitute the <tt>ConcurrentReferenceHashMap</tt> instance from a
     * stream (i.e., deserialize it).
     *
     * @param s
     *            the stream
     */
    @SuppressWarnings("unchecked")
    private void readObject(final java.io.ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        s.defaultReadObject();

        // Initialize each segment to be minimally sized, and let grow.
        for (int i = 0; i < this.segments.length; ++i) {
            this.segments[i].setTable(new HashEntry[1]);
        }

        // Read the keys and values, and put the mappings in the table
        for (;;) {
            final K key = (K) s.readObject();
            final V value = (V) s.readObject();
            if (key == null)
                break;
            put(key, value);
        }
    }

    public void addRemoveStaleListener(final RemoveStaleListener<? super V> listener) {
        this.removeStaleListeners.add(listener);
    }

    public void removeRemoveStaleListener(final RemoveStaleListener<? super V> listener) {
        this.removeStaleListeners.remove(listener);
    }

}
