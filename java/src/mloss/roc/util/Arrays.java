/*
 * Copyright (c) 2013 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc.util;


/**
 * <p>All the array utilities we wished Java included.</p>
 *
 * <p>So far this includes functions for converting arrays of primitive
 * numbers to arrays of number objects.</p>
 */
public class Arrays {

    /**
     * Unconstructable (no-op).  (A private constructor is necessary
     * because otherwise Java inserts a default public constructor.)
     */
    private Arrays() {}

    /**
     * Converts an int array to an Integer array.  Creates an Integer
     * array that is a copy of the values in the given int array.
     *
     * @param array Any int array.
     */
    public static Integer[] intArrayToIntegerArray(int[] array) {
        Integer[] objectArray = new Integer[array.length];
        for (int index = 0; index < array.length; ++index) {
            objectArray[index] = Integer.valueOf(array[index]);
        }
        return objectArray;
    }

    /**
     * Converts a double array to a Double array.  Creates a Double
     * array that is a copy of the values in the given double array.
     *
     * @param array Any double array.
     */
    public static Double[] doubleArrayToDoubleArray(double[] array) {
        Double[] objectArray = new Double[array.length];
        for (int index = 0; index < array.length; ++index) {
            objectArray[index] = Double.valueOf(array[index]);
        }
        return objectArray;
    }
}
