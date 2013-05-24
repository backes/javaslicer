/** License information:
 *    Component: javaslicer-common
 *    Package:   de.unisb.cs.st.javaslicer.common.util
 *    Class:     UntracedArrayList
 *    Filename:  javaslicer-common/src/main/java/de/unisb/cs/st/javaslicer/common/util/UntracedArrayList.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * @(#)ArrayList.java   1.56 06/04/21
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * THIS COPY IS HERE FOR NOT BEING INSTRUMENTED!
 *
 */

package de.unisb.cs.st.javaslicer.common.util;

import java.util.AbstractList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

public class UntracedArrayList<E> implements List<E> {

    /**
     * The array buffer into which the elements of the ArrayList are stored. The capacity of the ArrayList is the length
     * of this array buffer.
     */
    private Object[] elementData;

    /**
     * The size of the ArrayList (the number of elements it contains).
     */
    protected int size;

    protected int modCount;

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity
     *            the initial capacity of the list
     * @exception IllegalArgumentException
     *                if the specified initial capacity is negative
     */
    public UntracedArrayList(final int initialCapacity) {
        super();
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        this.elementData = new Object[initialCapacity];
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public UntracedArrayList() {
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
    public UntracedArrayList(final Collection<? extends E> c) {
        this.elementData = c.toArray();
        this.size = this.elementData.length;
        // c.toArray might (incorrectly) not return Object[] (see 6260652)
        if (this.elementData.getClass() != Object[].class) {
            final Object[] old = this.elementData;
            this.elementData = new Object[this.size];
            System.arraycopy(old, 0, this.elementData, 0, this.size);
        }
    }

    /**
     * Trims the capacity of this <tt>ArrayList</tt> instance to be the list's current size. An application can use
     * this operation to minimize the storage of an <tt>ArrayList</tt> instance.
     */
    public void trimToSize() {
        this.modCount++;
        final int oldCapacity = this.elementData.length;
        if (this.size < oldCapacity) {
            final Object[] old = this.elementData;
            this.elementData = new Object[this.size];
            System.arraycopy(old, 0, this.elementData, 0, this.size);
        }
    }

    /**
     * Increases the capacity of this <tt>ArrayList</tt> instance, if necessary, to ensure that it can hold at least
     * the number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity
     *            the desired minimum capacity
     */
    public void ensureCapacity(final int minCapacity) {
        this.modCount++;
        final int oldCapacity = this.elementData.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            // minCapacity is usually close to size, so this is a win:
            final Object[] old = this.elementData;
            this.elementData = new Object[newCapacity];
            System.arraycopy(old, 0, this.elementData, 0, oldCapacity);
        }
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    @Override
	public int size() {
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
     * Returns <tt>true</tt> if this list contains the specified element. More formally, returns <tt>true</tt> if
     * and only if this list contains at least one element <tt>e</tt> such that
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
     * Returns the index of the first occurrence of the specified element in this list, or -1 if this list does not
     * contain the element. More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>, or -1 if there is no such index.
     */
    @Override
	public int indexOf(final Object o) {
        if (o == null) {
            for (int i = 0; i < this.size; i++)
                if (this.elementData[i] == null)
                    return i;
        } else {
            for (int i = 0; i < this.size; i++)
                if (o.equals(this.elementData[i]))
                    return i;
        }
        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified element in this list, or -1 if this list does not
     * contain the element. More formally, returns the highest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>, or -1 if there is no such index.
     */
    @Override
	public int lastIndexOf(final Object o) {
        if (o == null) {
            for (int i = this.size - 1; i >= 0; i--)
                if (this.elementData[i] == null)
                    return i;
        } else {
            for (int i = this.size - 1; i >= 0; i--)
                if (o.equals(this.elementData[i]))
                    return i;
        }
        return -1;
    }

    /**
     * Returns an array containing all of the elements in this list in proper sequence (from first to last element).
     *
     * <p>
     * The returned array will be "safe" in that no references to it are maintained by this list. (In other words, this
     * method must allocate a new array). The caller is thus free to modify the returned array.
     *
     * <p>
     * This method acts as bridge between array-based and collection-based APIs.
     *
     * @return an array containing all of the elements in this list in proper sequence
     */
    @Override
	public Object[] toArray() {
        final Object[] a = new Object[this.size];
        System.arraycopy(this.elementData, 0, a, 0, this.size);
        return a;
    }

    /**
     * Returns an array containing all of the elements in this list in proper sequence (from first to last element); the
     * runtime type of the returned array is that of the specified array. If the list fits in the specified array, it is
     * returned therein. Otherwise, a new array is allocated with the runtime type of the specified array and the size
     * of this list.
     *
     * <p>
     * If the list fits in the specified array with room to spare (i.e., the array has more elements than the list), the
     * element in the array immediately following the end of the collection is set to <tt>null</tt>. (This is useful
     * in determining the length of the list <i>only</i> if the caller knows that the list does not contain any null
     * elements.)
     *
     * @param a
     *            the array into which the elements of the list are to be stored, if it is big enough; otherwise, a new
     *            array of the same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list
     * @throws ArrayStoreException
     *             if the runtime type of the specified array is not a supertype of the runtime type of every element in
     *             this list
     * @throws NullPointerException
     *             if the specified array is null
     */
    @Override
	@SuppressWarnings("unchecked")
    public <T> T[] toArray(final T[] a) {
        if (a.length < this.size) {
            // Make a new array of a's runtime type, but my contents:
            final T[] newA = (T[]) java.lang.reflect.Array.newInstance(a.getClass()
                .getComponentType(), this.size);
            System.arraycopy(this.elementData, 0, newA, 0, this.size);
            return newA;
        }
        System.arraycopy(this.elementData, 0, a, 0, this.size);
        if (a.length > this.size)
            a[this.size] = null;
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
    @Override
	@SuppressWarnings("unchecked")
    public E get(final int index) {
        rangeCheck(index);

        return (E) this.elementData[index];
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
    @Override
	@SuppressWarnings("unchecked")
    public E set(final int index, final E element) {
        rangeCheck(index);

        final E oldValue = (E) this.elementData[index];
        this.elementData[index] = element;
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
	public boolean add(final E e) {
        ensureCapacity(this.size + 1); // Increments modCount!!
        this.elementData[this.size++] = e;
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
	public void add(final int index, final E element) {
        if (index > this.size || index < 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);

        ensureCapacity(this.size + 1); // Increments modCount!!
        System.arraycopy(this.elementData, index, this.elementData, index + 1, this.size - index);
        this.elementData[index] = element;
        this.size++;
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
    @Override
	@SuppressWarnings("unchecked")
    public E remove(final int index) {
        rangeCheck(index);

        this.modCount++;
        final E oldValue = (E) this.elementData[index];

        final int numMoved = this.size - index - 1;
        if (numMoved > 0)
            System.arraycopy(this.elementData, index + 1, this.elementData, index, numMoved);
        this.elementData[--this.size] = null; // Let gc do its work

        return oldValue;
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
        if (o == null) {
            for (int index = 0; index < this.size; index++)
                if (this.elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
        } else {
            for (int index = 0; index < this.size; index++)
                if (o.equals(this.elementData[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }

    /*
     * Private remove method that skips bounds checking and does not
     * return the value removed.
     */
    private void fastRemove(final int index) {
        this.modCount++;
        final int numMoved = this.size - index - 1;
        if (numMoved > 0)
            System.arraycopy(this.elementData, index + 1, this.elementData, index, numMoved);
        this.elementData[--this.size] = null; // Let gc do its work
    }

    /**
     * Removes all of the elements from this list. The list will be empty after this call returns.
     */
    @Override
	public void clear() {
        this.modCount++;

        // Let gc do its work
        for (int i = 0; i < this.size; i++)
            this.elementData[i] = null;

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
	public boolean addAll(final Collection<? extends E> c) {
        final Object[] a = c.toArray();
        final int numNew = a.length;
        ensureCapacity(this.size + numNew); // Increments modCount
        System.arraycopy(a, 0, this.elementData, this.size, numNew);
        this.size += numNew;
        return numNew != 0;
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
	public boolean addAll(final int index, final Collection<? extends E> c) {
        if (index > this.size || index < 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);

        final Object[] a = c.toArray();
        final int numNew = a.length;
        ensureCapacity(this.size + numNew); // Increments modCount

        final int numMoved = this.size - index;
        if (numMoved > 0)
            System.arraycopy(this.elementData, index, this.elementData, index + numNew, numMoved);

        System.arraycopy(a, 0, this.elementData, index, numNew);
        this.size += numNew;
        return numNew != 0;
    }

    /**
     * Checks if the given index is in range. If not, throws an appropriate runtime exception. This method does *not*
     * check if the index is negative: It is always used immediately prior to an array access, which throws an
     * ArrayIndexOutOfBoundsException if index is negative.
     */
    private void rangeCheck(final int index) {
        if (index >= this.size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
    }

    @Override
	public boolean containsAll(final Collection<?> c) {
        for (final Object o : c)
            if (!contains(o))
                return false;
        return true;
    }

    @Override
	public Iterator<E> iterator() {
        return new Itr();
    }

    @Override
	public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    @Override
	public ListIterator<E> listIterator(final int index) {
        return new ListItr(index);
    }

    @Override
	public boolean removeAll(final Collection<?> c) {
        boolean modified = false;
        final Iterator<?> e = iterator();
        while (e.hasNext()) {
            if (c.contains(e.next())) {
                e.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
	public boolean retainAll(final Collection<?> c) {
        boolean modified = false;
        final Iterator<E> e = iterator();
        while (e.hasNext()) {
            if (!c.contains(e.next())) {
                e.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
	public List<E> subList(final int fromIndex, final int toIndex) {
        return (this instanceof RandomAccess ? new RandomAccessSubList(fromIndex, toIndex) : new SubList(fromIndex,
                toIndex));
    }

    protected class Itr implements Iterator<E> {
        /**
         * Index of element to be returned by subsequent call to next.
         */
        int cursor = 0;

        /**
         * Index of element returned by most recent call to next or previous. Reset to -1 if this element is deleted by
         * a call to remove.
         */
        int lastRet = -1;

        /**
         * The modCount value that the iterator believes that the backing List should have. If this expectation is
         * violated, the iterator has detected concurrent modification.
         */
        int expectedModCount = UntracedArrayList.this.modCount;

        @Override
		public boolean hasNext() {
            return this.cursor != size();
        }

        @Override
		public E next() {
            checkForComodification();
            try {
                final E next = get(this.cursor);
                this.lastRet = this.cursor++;
                return next;
            } catch (final IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        @Override
		public void remove() {
            if (this.lastRet == -1)
                throw new IllegalStateException();
            checkForComodification();

            try {
                UntracedArrayList.this.remove(this.lastRet);
                if (this.lastRet < this.cursor)
                    this.cursor--;
                this.lastRet = -1;
                this.expectedModCount = UntracedArrayList.this.modCount;
            } catch (final IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        final void checkForComodification() {
            if (UntracedArrayList.this.modCount != this.expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(final int index) {
            this.cursor = index;
        }

        @Override
		public boolean hasPrevious() {
            return this.cursor != 0;
        }

        @Override
		public E previous() {
            checkForComodification();
            try {
                final int i = this.cursor - 1;
                final E previous = get(i);
                this.lastRet = this.cursor = i;
                return previous;
            } catch (final IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        @Override
		public int nextIndex() {
            return this.cursor;
        }

        @Override
		public int previousIndex() {
            return this.cursor - 1;
        }

        @Override
		public void set(final E e) {
            if (this.lastRet == -1)
                throw new IllegalStateException();
            checkForComodification();

            try {
                UntracedArrayList.this.set(this.lastRet, e);
                this.expectedModCount = UntracedArrayList.this.modCount;
            } catch (final IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
		public void add(final E e) {
            checkForComodification();

            try {
                UntracedArrayList.this.add(this.cursor++, e);
                this.lastRet = -1;
                this.expectedModCount = UntracedArrayList.this.modCount;
            } catch (final IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private class SubList extends AbstractList<E> {

        protected final int offset;

        protected int sublistSize;

        protected int expectedModCount;

        SubList(final int fromIndex, final int toIndex) {
            if (fromIndex < 0)
                throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
            if (toIndex > UntracedArrayList.this.size())
                throw new IndexOutOfBoundsException("toIndex = " + toIndex);
            if (fromIndex > toIndex)
                throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
            this.offset = fromIndex;
            this.sublistSize = toIndex - fromIndex;
            this.expectedModCount = UntracedArrayList.this.modCount;
        }

        @Override
        public E set(final int index, final E element) {
            rangeCheck(index);
            checkForComodification();
            return UntracedArrayList.this.set(index + this.offset, element);
        }

        @Override
        public E get(final int index) {
            rangeCheck(index);
            checkForComodification();
            return UntracedArrayList.this.get(index + this.offset);
        }

        @Override
        public int size() {
            checkForComodification();
            return this.sublistSize;
        }

        @Override
        public void add(final int index, final E element) {
            if (index < 0 || index > this.sublistSize)
                throw new IndexOutOfBoundsException();
            checkForComodification();
            UntracedArrayList.this.add(index + this.offset, element);
            this.expectedModCount = UntracedArrayList.this.modCount;
            this.sublistSize++;
            UntracedArrayList.this.modCount++;
        }

        @Override
        public E remove(final int index) {
            rangeCheck(index);
            checkForComodification();
            final E result = UntracedArrayList.this.remove(index + this.offset);
            this.expectedModCount = UntracedArrayList.this.modCount;
            this.sublistSize--;
            UntracedArrayList.this.modCount++;
            return result;
        }

        @Override
        public boolean addAll(final Collection<? extends E> c) {
            return addAll(this.sublistSize, c);
        }

        @Override
        public boolean addAll(final int index, final Collection<? extends E> c) {
            if (index < 0 || index > this.sublistSize)
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.sublistSize);
            final int cSize = c.size();
            if (cSize == 0)
                return false;

            checkForComodification();
            UntracedArrayList.this.addAll(this.offset + index, c);
            this.expectedModCount = UntracedArrayList.this.modCount;
            this.sublistSize += cSize;
            UntracedArrayList.this.modCount++;
            return true;
        }

        @Override
        public Iterator<E> iterator() {
            return listIterator(0);
        }

        @Override
        public ListIterator<E> listIterator(final int index) {
            checkForComodification();
            if (index < 0 || index > this.sublistSize)
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.sublistSize);

            return new ListIterator<E>() {
                private final ListIterator<E> i = UntracedArrayList.this.listIterator(index
                        + UntracedArrayList.SubList.this.offset);

                @Override
				public boolean hasNext() {
                    return nextIndex() < SubList.this.sublistSize;
                }

                @Override
				public E next() {
                    if (!hasNext())
                        throw new NoSuchElementException();
                    return this.i.next();
                }

                @Override
				public boolean hasPrevious() {
                    return previousIndex() >= 0;
                }

                @Override
				public E previous() {
                    if (!hasPrevious())
                        throw new NoSuchElementException();
                    return this.i.previous();
                }

                @Override
				public int nextIndex() {
                    return this.i.nextIndex() - SubList.this.offset;
                }

                @Override
				public int previousIndex() {
                    return this.i.previousIndex() - SubList.this.offset;
                }

                @Override
				public void remove() {
                    this.i.remove();
                    SubList.this.expectedModCount = UntracedArrayList.this.modCount;
                    SubList.this.sublistSize--;
                    UntracedArrayList.this.modCount++;
                }

                @Override
				public void set(final E e) {
                    this.i.set(e);
                }

                @Override
				public void add(final E e) {
                    this.i.add(e);
                    SubList.this.expectedModCount = UntracedArrayList.this.modCount;
                    SubList.this.sublistSize++;
                    UntracedArrayList.this.modCount++;
                }
            };
        }

        @Override
        public List<E> subList(final int fromIndex, final int toIndex) {
            return new SubList(fromIndex + this.offset, toIndex + this.offset);
        }

        private void rangeCheck(final int index) {
            if (index < 0 || index >= this.sublistSize)
                throw new IndexOutOfBoundsException("Index: " + index + ",Size: " + this.sublistSize);
        }

        private void checkForComodification() {
            if (UntracedArrayList.this.modCount != this.expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    private class RandomAccessSubList extends SubList implements RandomAccess {
        RandomAccessSubList(final int fromIndex, final int toIndex) {
            super(fromIndex, toIndex);
        }

        @Override
        public List<E> subList(final int fromIndex, final int toIndex) {
            return new RandomAccessSubList(fromIndex + this.offset, toIndex + this.offset);
        }
    }

}
