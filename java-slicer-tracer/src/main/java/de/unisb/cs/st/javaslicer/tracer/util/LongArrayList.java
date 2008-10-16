package de.unisb.cs.st.javaslicer.tracer.util;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

public class LongArrayList<T> extends AbstractList<T> implements RandomAccess, Cloneable, java.io.Serializable {

    private static final long serialVersionUID = 4585266173348382453L;

    private transient Object[][] elements;

    private long size;

    private static final int maxElementsPerArray = 1<<30;
    private static final int lowerArrayMask = maxElementsPerArray-1;

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity
     *            the initial capacity of the list
     * @exception IllegalArgumentException
     *                if the specified initial capacity is negative
     */
    public LongArrayList(final long initialCapacity) {
        super();
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        if (initialCapacity > maxElementsPerArray) {
            this.elements = new Object[(int) ((initialCapacity >>> 34)+1)][];
            for (int i = 0; i < this.elements.length-1; ++i)
                this.elements[i] = new Object[maxElementsPerArray];
            this.elements[this.elements.length-1] = new Object[(int)initialCapacity & lowerArrayMask];
        } else
            this.elements = new Object[1][(int) initialCapacity];
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public LongArrayList() {
        this(10);
    }

    /**
     * Constructs a list containing the elements of the specified collection, in the order they are returned by the
     * collection's iterator.
     *
     * @param c
     *            the collection whose elements are to be placed into this list
     * @throws NullPointerException
     *             if the specified collection is null
     */
    public LongArrayList(final Collection<? extends T> c) {
        final int cSize = c.size();
        if (cSize < maxElementsPerArray) {
            this.elements = new Object[1][];
            this.elements[0] = c.toArray();
            this.size = cSize;
            // c.toArray might (incorrectly) not return Object[] (see 6260652)
            if (this.elements[0].getClass() != Object[].class)
                this.elements[0] = arrayCopy(this.elements[0], this.elements[0].length);
        } else {
            this.elements = new Object[1][maxElementsPerArray];
            addAll(c);
        }
    }

    /**
     * Trims the capacity of this <tt>LongArrayList</tt> instance to be the list's current size. An application can use this
     * operation to minimize the storage of an <tt>LongArrayList</tt> instance.
     */
    public void trimToSize() {
        this.modCount++;
        final int neededCap1 = (int) (this.size >>> 34) + 1;
        final int neededCap2 = ((int)this.size) & lowerArrayMask;
        if (neededCap1 < this.elements.length)
            this.elements = arrayCopy(this.elements, neededCap1);
        if (neededCap2 < this.elements[neededCap1-1].length)
            this.elements[neededCap1-1] = arrayCopy(this.elements[neededCap1-1], neededCap2);
    }

    /**
     * Increases the capacity of this <tt>ArrayList</tt> instance, if necessary, to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity
     *            the desired minimum capacity
     */
    public void ensureCapacity(final long minCapacity) {
        this.modCount++;
        int neededCap1 = (int) (minCapacity >>> 34) + 1;
        int neededCap2 = ((int)minCapacity) & lowerArrayMask;
        if (neededCap1 > this.elements.length) {
            final long newCapacity = Math.max(this.size*4/3+1, minCapacity);
            neededCap1 = (int) (newCapacity >>> 34) + 1;
            neededCap2 = ((int)newCapacity) & lowerArrayMask;
            final Object[][] newElements = new Object[neededCap1][];
            System.arraycopy(this.elements, 0, newElements, 0, this.elements.length-1);
            if (this.elements[this.elements.length-1].length == maxElementsPerArray)
                newElements[this.elements.length-1] = this.elements[this.elements.length-1];
            else
                newElements[this.elements.length-1] = arrayCopy(this.elements[this.elements.length-1], maxElementsPerArray);
            for (int i = this.elements.length; i < newElements.length; ++i) {
                newElements[i] = new Object[i == newElements.length -1 ? neededCap2 : maxElementsPerArray];
            }
            this.elements = newElements;
        } else if (neededCap1 == this.elements.length && neededCap2 > this.elements[neededCap1-1].length) {
            final long newCapacity = Math.max(this.size*4/3+1, minCapacity);
            if ((newCapacity >>> 34) + 1 == neededCap1)
                neededCap2 = ((int)newCapacity) & lowerArrayMask;
            else
                neededCap2 = maxElementsPerArray;
            this.elements[neededCap1-1] = arrayCopy(this.elements[neededCap1-1], neededCap2);
        }
    }

    /**
     * Returns the number of elements in this list, or Integer.MAX_VALUE if
     * more than Integer.MAX_VALUE elements are in this list.
     *
     * @return min(number of elements in this list, Integer.MAX_VALUE)
     */
    @Override
    public int size() {
        if (this.size >= Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        return (int) this.size;
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public long longSize() {
        return this.size;
    }

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     *
     * @return <tt>true</tt> if this list contains no elements
     */
    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element. More formally, returns <tt>true</tt> if and
     * only if this list contains at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o
     *            element whose presence in this list is to be tested
     * @return <tt>true</tt> if this list contains the specified element
     */
    @Override
    public boolean contains(final Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * Returns the index of the first occurrence of the specified element in the first segment (1<<30) ofthis list,
     * or -1 if this segment does not contain the element. More formally, returns the lowest index <tt>i</tt> in
     * <tt>[0, 1<<30]</tt> such that <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     */
    @Override
    public int indexOf(final Object o) {
        return (int)longIndexOf(o, Integer.MAX_VALUE);
    }

    /**
     * Returns the index of the first occurrence of the specified element in this list, or -1 if this list does not
     * contain the element. More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>, or -1 if there is no such index.
     */
    public long longIndexOf(final Object o) {
        return longIndexOf(o, this.size);
    }

    private long longIndexOf(final Object o, final long searchSize) {
        int pos1 = 0, pos2 = 0;
        Object[] lowArr = this.elements[0];
        if (o == null) {
            for (long l = 0; l < searchSize; ++l) {
                if (pos2 == maxElementsPerArray) {
                    lowArr = this.elements[++pos1];
                    pos2 = 0;
                }
                if (lowArr[pos2++] == null)
                    return l;
            }
        } else {
            for (long l = 0; l < searchSize; ++l) {
                if (pos2 == maxElementsPerArray) {
                    lowArr = this.elements[++pos1];
                    pos2 = 0;
                }
                if (o.equals(lowArr[pos2++]))
                    return l;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified element in the first segment (1<<30) ofthis list,
     * or -1 if this segment does not contain the element. More formally, returns the highest index <tt>i</tt> in
     * <tt>[0, 1<<30]</tt> such that <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     */
    @Override
    public int lastIndexOf(final Object o) {
        return (int)longLastIndexOf(o, Integer.MAX_VALUE);
    }

    /**
     * Returns the index of the last occurrence of the specified element in this list, or -1 if this list does not
     * contain the element. More formally, returns the highest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>, or -1 if there is no such index.
     */
    public long longLastIndexOf(final Object o) {
        return longLastIndexOf(o, this.size);
    }

    private long longLastIndexOf(final Object o, final long searchSize) {
        int pos1 = (int) (searchSize >>> 34);
        int pos2 = ((int)searchSize) & lowerArrayMask;
        Object[] lowArr = this.elements[pos1];
        if (o == null) {
            for (long l = searchSize; l >= 0; --l) {
                if (pos2-- == 0) {
                    lowArr = this.elements[--pos1];
                    pos2 = maxElementsPerArray;
                }
                if (lowArr[pos2] == null)
                    return l;
            }
        } else {
            for (long l = searchSize; l >= 0; --l) {
                if (pos2-- == 0) {
                    lowArr = this.elements[--pos1];
                    pos2 = maxElementsPerArray;
                }
                if (o.equals(lowArr[pos2]))
                    return l;
            }
        }
        return -1;
    }

    /**
     * Returns a shallow copy of this <tt>LongArrayList</tt> instance. (The elements themselves are not copied.)
     *
     * @return a clone of this <tt>LongArrayList</tt> instance
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        try {
            final LongArrayList<T> v = (LongArrayList<T>) super.clone();
            v.elements = new Object[this.elements.length][];
            for (int i = 0; i < this.elements.length; ++i)
                v.elements[i] = arrayCopy(this.elements[i], maxElementsPerArray);
            return v;
        } catch (final CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    /**
     * Returns an array containing the first 1<<30 elements in this list in proper sequence (from first to last element).
     *
     * <p>
     * The returned array will be "safe" in that no references to it are maintained by this list. (In other words, this
     * method must allocate a new array). The caller is thus free to modify the returned array.
     *
     * <p>
     * This method acts as bridge between array-based and collection-based APIs.
     *
     * @return an array containing the first 1<<30 elements in this list in proper sequence
     */
    @Override
    public Object[] toArray() {
        return arrayCopy(this.elements[0], (int)Math.min(this.size, maxElementsPerArray));
    }

    /**
     * Returns an array containing the first 1<<30 elements in this list in proper sequence (from first to last element); the
     * runtime type of the returned array is that of the specified array. If the list fits in the specified array, it is
     * returned therein. Otherwise, a new array is allocated with the runtime type of the specified array and the size
     * of this list.
     *
     * <p>
     * If the list fits in the specified array with room to spare (i.e., the array has more elements than the list), the
     * element in the array immediately following the end of the collection is set to <tt>null</tt>. (This is useful in
     * determining the length of the list <i>only</i> if the caller knows that the list does not contain any null
     * elements.)
     *
     * @param a
     *            the array into which the elements of the list are to be stored, if it is big enough; otherwise, a new
     *            array of the same runtime type is allocated for this purpose.
     * @return an array containing the first 1<<30 elements of the list
     * @throws ArrayStoreException
     *             if the runtime type of the specified array is not a supertype of the runtime type of every element in
     *             this list
     * @throws NullPointerException
     *             if the specified array is null
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T2> T2[] toArray(final T2[] a) {
        final int length = (int) Math.min(this.size, maxElementsPerArray);
        if (a.length < length) {
            // Make a new array of a's runtime type, but my contents:
            return (T2[]) arrayCopy(this.elements[0], length, a.getClass());
        }
        System.arraycopy(this.elements[0], 0, a, 0, length);
        if (a.length > length)
            a[length+1] = null;
        return a;
    }

    // Positional Access Operations

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index
     *            index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public T get(final int index) {
        if (index < maxElementsPerArray) {
            rangeCheck(index);
            return (T) this.elements[0][index];
        }
        return get((long) index);
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index
     *            index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public T get(final long index) {
        rangeCheck(index);
        final int pos1 = (int) (index >>> 34);
        final int pos2 = ((int)index) & lowerArrayMask;
        return (T) this.elements[pos1][pos2];
    }

    /**
     * Replaces the element at the specified position in this list with the specified element.
     *
     * @param index
     *            index of the element to replace
     * @param element
     *            element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public T set(final int index, final T element) {
        if (index < maxElementsPerArray) {
            rangeCheck(index);
            final T oldValue = (T) this.elements[0][index];
            this.elements[0][index] = element;
            return oldValue;
        }
        return set((long) index, element);
    }

    /**
     * Replaces the element at the specified position in this list with the specified element.
     *
     * @param index
     *            index of the element to replace
     * @param element
     *            element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public T set(final long index, final T element) {
        rangeCheck(index);

        final int pos1 = (int) (index >>> 34);
        final int pos2 = ((int)index) & lowerArrayMask;
        final T oldValue = (T) this.elements[pos1][pos2];
        this.elements[pos1][pos2] = element;
        return oldValue;
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param e
     *            element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    @Override
    public boolean add(final T e) {
        ensureCapacity(this.size + 1); // Increments modCount!!
        final int pos1 = (int) (this.size >>> 34);
        final int pos2 = ((int)this.size) & lowerArrayMask;
        this.elements[pos1][pos2] = e;
        ++this.size;
        return true;
    }

    /**
     * Inserts the specified element at the specified position in this list. Shifts the element currently at that
     * position (if any) and any subsequent elements to the right (adds one to their indices).
     *
     * @param index
     *            index at which the specified element is to be inserted
     * @param element
     *            element to be inserted
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     */
    @Override
    public void add(final int index, final T element) {
        if (this.size < maxElementsPerArray) {
            if (index > (int)this.size || index < 0)
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);

            ensureCapacity(this.size+1);
            System.arraycopy(this.elements[0], index, this.elements[0], index + 1, (int) (this.size - index));
            this.elements[0][index] = element;
            ++this.size;
        } else {
            add((long)index, element);
        }
    }

    /**
     * Inserts the specified element at the specified position in this list. Shifts the element currently at that
     * position (if any) and any subsequent elements to the right (adds one to their indices).
     *
     * @param index
     *            index at which the specified element is to be inserted
     * @param element
     *            element to be inserted
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     */
    public void add(final long index, final T element) {
        if (index > this.size || index < 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);

        moveForward(index, 1); // Increments modCount!!
        final int pos1 = (int) (index >>> 34);
        final int pos2 = ((int)index) & lowerArrayMask;
        this.elements[pos1][pos2] = element;
        ++this.size;
    }

    private void moveForward(final long index, final int amount) {
        ensureCapacity(this.size + amount); // Increments modCount!!
        for (long pos = this.size; pos > index; ) {
            final int newPos1 = (int) ((pos+amount) >>> 34);
            final int newPos2 = ((int)(pos+amount)) & lowerArrayMask;
            final int oldPos1 = (int) (pos >>> 34);
            final int oldPos2 = (int)pos & lowerArrayMask;
            int copy = Math.min(oldPos2, newPos2);
            if (pos - index < copy)
                copy = (int) (pos - index);
            System.arraycopy(this.elements[oldPos1], oldPos2-copy, this.elements[newPos1], newPos2-copy, copy);
            pos -= copy;
        }
    }

    private void moveBackward(final long index, final long amount) {
        ++this.modCount;
        for (long pos = index; pos < this.size; ) {
            final int newPos1 = (int) ((pos-amount) >>> 34);
            final int newPos2 = ((int)(pos-amount)) & lowerArrayMask;
            final int oldPos1 = (int) (pos >>> 34);
            final int oldPos2 = (int)pos & lowerArrayMask;
            int copy = maxElementsPerArray - Math.max(oldPos2, newPos2);
            if (this.size - pos < copy)
                copy = (int) (this.size - pos);
            System.arraycopy(this.elements[oldPos1], oldPos2, this.elements[newPos1], newPos2, copy);
            Arrays.fill(this.elements[oldPos1], oldPos2, oldPos2 + copy, null);
            pos += copy;
        }
    }

    /**
     * Removes the element at the specified position in this list. Shifts any subsequent elements to the left (subtracts
     * one from their indices).
     *
     * @param index
     *            the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public T remove(final int index) {
        if (this.size <= maxElementsPerArray) {
            rangeCheck(index);
            ++this.modCount;
            final T oldValue = (T) this.elements[0][index];
            final int numMoved = (int)this.size - index - 1;
            if (numMoved > 0)
                System.arraycopy(this.elements[0], index + 1, this.elements[0], index, numMoved);
            this.elements[0][(int)--this.size] = null;
            --this.size;
            return oldValue;
        }

        return remove((long)index);
    }

    /**
     * Removes the element at the specified position in this list. Shifts any subsequent elements to the left (subtracts
     * one from their indices).
     *
     * @param index
     *            the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public T remove(final long index) {
        rangeCheck(index);

        final int pos1 = (int) (index >>> 34);
        final int pos2 = ((int)index) & lowerArrayMask;
        final T oldElement = (T) this.elements[pos1][pos2];
        moveBackward(index, 1); // increments modCount
        --this.size;
        return oldElement;
    }

    /**
     * Removes the first occurrence of the specified element from this list, if it is present. If the list does not
     * contain the element, it is unchanged. More formally, removes the element with the lowest index <tt>i</tt> such
     * that <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt> (if such an element exists).
     * Returns <tt>true</tt> if this list contained the specified element (or equivalently, if this list changed as a
     * result of the call).
     *
     * @param o
     *            element to be removed from this list, if present
     * @return <tt>true</tt> if this list contained the specified element
     */
    @Override
    public boolean remove(final Object o) {
        final int index = indexOf(o);
        if (index == -1)
            return false;
        remove(index);
        return true;
    }

    /**
     * Removes all of the elements from this list. The list will be empty after this call returns.
     */
    @Override
    public void clear() {
        this.modCount++;
        this.elements = new Object[1][10];
        this.size = 0;
    }

    /**
     * Appends all of the elements in the specified collection to the end of this list, in the order that they are
     * returned by the specified collection's Iterator. The behavior of this operation is undefined if the specified
     * collection is modified while the operation is in progress. (This implies that the behavior of this call is
     * undefined if the specified collection is this list, and this list is nonempty.)
     *
     * @param c
     *            collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException
     *             if the specified collection is null
     */
    @Override
    public boolean addAll(final Collection<? extends T> c) {
        if (this.size + c.size() <= maxElementsPerArray) {
            final Object[] a = c.toArray();
            final int numNew = a.length;
            if (this.size + a.length <= maxElementsPerArray) {
                ensureCapacity(this.size + numNew); // Increments modCount
                System.arraycopy(a, 0, this.elements[0], (int)this.size, numNew);
                this.size += numNew;
                return numNew != 0;
            }
        }

        return super.addAll(c);
    }

    /**
     * Inserts all of the elements in the specified collection into this list, starting at the specified position.
     * Shifts the element currently at that position (if any) and any subsequent elements to the right (increases their
     * indices). The new elements will appear in the list in the order that they are returned by the specified
     * collection's iterator.
     *
     * @param index
     *            index at which to insert the first element from the specified collection
     * @param c
     *            collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     * @throws NullPointerException
     *             if the specified collection is null
     */
    @Override
    public boolean addAll(final int index, final Collection<? extends T> c) {
        if (this.size + c.size() <= maxElementsPerArray) {
            if (index > (int)this.size || index < 0)
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
            final Object[] a = c.toArray();
            final int numNew = a.length;
            if (this.size + a.length <= maxElementsPerArray) {
                ensureCapacity(this.size + numNew); // Increments modCount
                System.arraycopy(this.elements[0], index, this.elements[0], index + numNew, (int) (this.size - index));
                System.arraycopy(a, 0, this.elements[0], index, numNew);
                this.size += numNew;
                return numNew != 0;
            }
        }

        return addAll((long)index, c);
    }

    /**
     * Inserts all of the elements in the specified collection into this list, starting at the specified position.
     * Shifts the element currently at that position (if any) and any subsequent elements to the right (increases their
     * indices). The new elements will appear in the list in the order that they are returned by the specified
     * collection's iterator.
     *
     * @param index
     *            index at which to insert the first element from the specified collection
     * @param c
     *            collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     * @throws NullPointerException
     *             if the specified collection is null
     */
    public boolean addAll(final long index, final Collection<? extends T> c) {
        if (index > this.size || index < 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);

        if (c.size() < Integer.MAX_VALUE) {
            final Object[] a = c.toArray();
            final int numNew = a.length;
            if (a.length < Integer.MAX_VALUE) {
                moveForward(index, numNew); // increments modCount
                int pos1 = (int) (index >>> 34);
                int pos2 = ((int)index) & lowerArrayMask;
                Object[] lowArr = this.elements[pos1];
                for (int i = 0; i < numNew; ++i) {
                    if (pos2 == maxElementsPerArray) {
                        lowArr = this.elements[++pos1];
                        pos2 = 0;
                    }
                    lowArr[pos2++] = a[i];
                }
                this.size += numNew;
                return numNew != 0;
            }
        }

        long i = index;
        for (final T t: c) {
            add(i++, t);
        }
        return i != index;
    }

    /**
     * Removes from this list all of the elements whose index is between <tt>fromIndex</tt>, inclusive, and
     * <tt>toIndex</tt>, exclusive. Shifts any succeeding elements to the left (reduces their index). This call shortens
     * the list by <tt>(toIndex - fromIndex)</tt> elements. (If <tt>toIndex==fromIndex</tt>, this operation has no
     * effect.)
     *
     * @param fromIndex
     *            index of first element to be removed
     * @param toIndex
     *            index after last element to be removed
     * @throws IndexOutOfBoundsException
     *             if fromIndex or toIndex out of range (fromIndex &lt; 0 || fromIndex &gt;= size() || toIndex &gt;
     *             size() || toIndex &lt; fromIndex)
     */
    @Override
    protected void removeRange(final int fromIndex, final int toIndex) {
        if (this.size <= maxElementsPerArray) {
            rangeCheck(fromIndex);
            rangeCheck(toIndex);
            ++this.modCount;
            final int numMoved = (int)this.size - toIndex;
            if (numMoved > 0)
                System.arraycopy(this.elements[0], toIndex, this.elements[0], fromIndex, numMoved);
            Arrays.fill(this.elements[0], fromIndex+numMoved, (int)this.size, null);
            this.size -= toIndex - fromIndex;
        } else {
            removeRange((long)fromIndex, (long)toIndex);
        }
    }

    /**
     * Removes from this list all of the elements whose index is between <tt>fromIndex</tt>, inclusive, and
     * <tt>toIndex</tt>, exclusive. Shifts any succeeding elements to the left (reduces their index). This call shortens
     * the list by <tt>(toIndex - fromIndex)</tt> elements. (If <tt>toIndex==fromIndex</tt>, this operation has no
     * effect.)
     *
     * @param fromIndex
     *            index of first element to be removed
     * @param toIndex
     *            index after last element to be removed
     * @throws IndexOutOfBoundsException
     *             if fromIndex or toIndex out of range (fromIndex &lt; 0 || fromIndex &gt;= size() || toIndex &gt;
     *             size() || toIndex &lt; fromIndex)
     */
    protected void removeRange(final long fromIndex, final long toIndex) {
        moveBackward(fromIndex, this.size - toIndex);
    }

    /**
     * Checks if the given index is in range. If not, throws an appropriate runtime exception. This method does *not*
     * check if the index is negative: It is always used immediately prior to an array access, which throws an
     * ArrayIndexOutOfBoundsException if index is negative.
     */
    private void rangeCheck(final long index) {
        if (index >= this.size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
    }

    /**
     * Save the state of the <tt>LongArrayList</tt> instance to a stream (that is, serialize it).
     *
     * @serialData The length of the array backing the <tt>LongArrayList</tt> instance is emitted (int), followed by all of
     *             its elements (each an <tt>Object</tt>) in the proper order.
     */
    private void writeObject(final java.io.ObjectOutputStream s) throws java.io.IOException {
        // Write out element count, and any hidden stuff
        final int expectedModCount = this.modCount;
        s.defaultWriteObject();

        // Write out array length
        s.writeInt(this.elements.length);
        s.writeInt(this.elements[this.elements.length-1].length);

        // Write out all elements in the proper order.
        int pos1 = 0, pos2 = 0;
        Object[] lowArr = this.elements[0];
        for (long l = 0; l < this.size; ++l) {
            if (++pos2 == maxElementsPerArray) {
                lowArr = this.elements[++pos1];
                pos2 = 0;
            }
            s.writeObject(lowArr[pos2]);
        }

        if (this.modCount != expectedModCount)
            throw new ConcurrentModificationException();
    }

    /**
     * Reconstitute the <tt>ArrayList</tt> instance from a stream (that is, deserialize it).
     */
    private void readObject(final java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        // Read in size, and any hidden stuff
        s.defaultReadObject();

        // Read in array length and allocate array
        final int arrayLength1 = s.readInt();
        final int arrayLength2 = s.readInt();
        this.elements = new Object[arrayLength1][];
        for (int i = 0; i < arrayLength1-1; ++i)
            this.elements[i] = new Object[maxElementsPerArray];
        this.elements[arrayLength1-1] = new Object[arrayLength2];

        // Read in all elements in the proper order.
        int pos1 = 0, pos2 = 0;
        Object[] lowArr = this.elements[0];
        for (long l = 0; l < this.size; ++l) {
            if (++pos2 == maxElementsPerArray) {
                lowArr = this.elements[++pos1];
                pos2 = 0;
            }
            lowArr[pos2] = s.readObject();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] arrayCopy(final T[] original, final int newLength, final Class<? extends T> newType) {
        final T[] copy = newType == Object.class ? (T[]) new Object[newLength]
            : (T[]) Array.newInstance(newType, newLength);
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] arrayCopy(final T[] original, final int newLength) {
        return arrayCopy(original, newLength, (Class<? extends T>)original.getClass().getComponentType());
    }

    // Iterators

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * <p>
     * This implementation returns a straightforward implementation of the iterator interface, relying on the backing
     * list's {@code size()}, {@code get(int)}, and {@code remove(int)} methods.
     *
     * <p>
     * Note that the iterator returned by this method will throw an {@code UnsupportedOperationException} in response to
     * its {@code remove} method unless the list's {@code remove(int)} method is overridden.
     *
     * <p>
     * This implementation can be made to throw runtime exceptions in the face of concurrent modification, as described
     * in the specification for the (protected) {@code modCount} field.
     *
     * @return an iterator over the elements in this list in proper sequence
     *
     * @see #modCount
     */
    @Override
    public Iterator<T> iterator() {
        return new Itr();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation returns {@code listIterator(0)}.
     *
     * @see #listIterator(int)
     */
    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation returns a straightforward implementation of the {@code ListIterator} interface that extends
     * the implementation of the {@code Iterator} interface returned by the {@code iterator()} method. The {@code
     * ListIterator} implementation relies on the backing list's {@code get(int)}, {@code set(int, E)}, {@code add(int,
     * E)} and {@code remove(int)} methods.
     *
     * <p>
     * Note that the list iterator returned by this implementation will throw an {@code UnsupportedOperationException}
     * in response to its {@code remove}, {@code set} and {@code add} methods unless the list's {@code remove(int)},
     * {@code set(int, E)}, and {@code add(int, E)} methods are overridden.
     *
     * <p>
     * This implementation can be made to throw runtime exceptions in the face of concurrent modification, as described
     * in the specification for the (protected) {@code modCount} field.
     *
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     *
     * @see #modCount
     */
    @Override
    public ListIterator<T> listIterator(final int index) {
        if (index < 0 || index > size())
            throw new IndexOutOfBoundsException("Index: " + index);

        return new ListItr(index);
    }

    protected int getModCount() {
        return this.modCount;
    }

    private class Itr implements Iterator<T> {

        protected Itr() {
            // nop
        }

        /**
         * Index of element to be returned by subsequent call to next.
         */
        long cursor = 0;

        /**
         * Index of element returned by most recent call to next or previous. Reset to -1 if this element is deleted by
         * a call to remove.
         */
        long lastRet = -1;

        /**
         * The modCount value that the iterator believes that the backing List should have. If this expectation is
         * violated, the iterator has detected concurrent modification.
         */
        int expectedModCount = getModCount();

        public boolean hasNext() {
            return this.cursor != size();
        }

        public T next() {
            checkForComodification();
            try {
                final T next = get(this.cursor);
                this.lastRet = this.cursor++;
                return next;
            } catch (final IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            if (this.lastRet == -1)
                throw new IllegalStateException();
            checkForComodification();

            try {
                LongArrayList.this.remove(this.lastRet);
                if (this.lastRet < this.cursor)
                    this.cursor--;
                this.lastRet = -1;
                this.expectedModCount = getModCount();
            } catch (final IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        final void checkForComodification() {
            if (getModCount() != this.expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    private class ListItr extends Itr implements ListIterator<T> {

        protected ListItr(final long index) {
            this.cursor = index;
        }

        public boolean hasPrevious() {
            return this.cursor != 0;
        }

        public T previous() {
            checkForComodification();
            try {
                final long i = this.cursor - 1;
                final T previous = get(i);
                this.lastRet = this.cursor = i;
                return previous;
            } catch (final IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        public int nextIndex() {
            if (this.cursor >= Integer.MAX_VALUE)
                return Integer.MAX_VALUE;
            return (int) this.cursor;
        }

        public int previousIndex() {
            if (this.cursor > Integer.MAX_VALUE)
                return Integer.MAX_VALUE;
            return (int) (this.cursor-1);
        }

        public void set(final T e) {
            if (this.lastRet == -1)
                throw new IllegalStateException();
            checkForComodification();

            try {
                LongArrayList.this.set(this.lastRet, e);
                this.expectedModCount = getModCount();
            } catch (final IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(final T e) {
            checkForComodification();

            try {
                LongArrayList.this.add(this.cursor++, e);
                this.lastRet = -1;
                this.expectedModCount = getModCount();
            } catch (final IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

}
