/*
 * Copyright (c) 2012 Roc Project.  See LICENSE.txt for details.
 */

package mloss.roc.util;


import java.util.Iterator;


/**
 * Makes object arrays into iterables so as to work alongside
 * collections and the like.
 *
 * @param <E> Type of array element
 * @see ArrayIterator
 */
public class IterableArray<E> implements Iterable<E> {

    /** Array to iterate over. */
    E[] array;

    /**
     * Constructs an iterable version of the given array.
     *
     * @param array Some array of objects.
     */
    public IterableArray(E[] array) {
        this.array = array;
    }

    public Iterator<E> iterator() {
        return new ArrayIterator<E>(array);
    }
}
