/*
 * Copyright (c) 2014 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc.util;


import java.util.Iterator;


/**
 * Makes object arrays into iterables so as to work alongside
 * collections and the like.  Wraps an array in a read-only iterable.
 *
 * @param <E> Type of array element
 * @see ArrayIterator
 */
public class IterableArray<E> implements Iterable<E> {

    /** Array to iterate over. */
    private E[] array;

    /**
     * <p>Constructs an iterable version of the given array so it can be
     * used as a collection.  Just wraps the given array; does not
     * allocate memory.</p>
     *
     * <p>Use {@link ArrayUtils#intArrayToIntegerArray(int[])} and
     * {@link ArrayUtils#doubleArrayToDoubleArray(double[])} to first
     * convert arrays of primitives to arrays of objects if needed. For
     * example:</p>
     *
     * <pre><code>
     * int[] intArray = ...;
     * ... = new IterableArray(ArrayUtils.intArrayToIntegerArray(intArray));
     * </code></pre>
     *
     * @param array Some array of objects.
     */
    public IterableArray(E[] array) {
        this.array = array;
    }

    /**
     * @return A read-only iterator.
     */
    public Iterator<E> iterator() {
        return new ArrayIterator<E>(array);
    }
}
