/*
 * Copyright (c) 2013 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc.util;


import java.util.Iterator;


/**
 * An iterator for arrays so that arrays can be used as iterables
 * alongside collections and the like.  This is a read-only iterator.
 *
 * @param <E> Type of array element
 * @see IterableArray
 */
public class ArrayIterator<E> implements Iterator<E> {

    /** Array to iterate over. */
    private E[] array;

    /** The current position in the array. */
    private int index;

    /**
     * Constructs an iterator for the given array.
     *
     * @param array Some array of objects.
     */
    public ArrayIterator(E[] array) {
        this.array = array;
        index = 0;
    }

    public boolean hasNext() {
        return index < array.length;
    }

    public E next() {
        return array[index++];
    }

    /**
     * Unsupported.
     *
     * @throws UnsupportedOperationException
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
