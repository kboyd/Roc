/*
 * Copyright (c) 2012 Roc Project.  See LICENSE.txt for details.
 */

package mloss.roc.util;


/**
 * All the array utilities we wished Java included.
 */
public class Arrays {

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
     * Converts a double array to a Double array.  Creates an Double
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
