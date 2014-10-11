/*
 * Copyright (c) 2014 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc.util;


/**
 * <p>All the array utilities we wished Java included.</p>
 */
public class ArrayUtils {

    /**
     * Unconstructable (no-op).  (A private constructor is necessary
     * because otherwise Java inserts a default public constructor.)
     */
    private ArrayUtils() {}

    /**
     * Converts an array of ints to an array of Integers.
     */
    public static Integer[] intsToIntegers(int... ints) {
        Integer[] integers = new Integer[ints.length];
        for (int index = 0; index < ints.length; ++index) {
            integers[index] = Integer.valueOf(ints[index]);
        }
        return integers;
    }

    /**
     * Converts an array of doubles to an array of Doubles.
     */
    public static Double[] dblsToDoubles(double... dbls) {
        Double[] doubles = new Double[dbls.length];
        for (int index = 0; index < dbls.length; ++index) {
            doubles[index] = Double.valueOf(dbls[index]);
        }
        return doubles;
    }

    /**
     * Adds a constant to each of the given integers.  Returns a new
     * array.
     */
    public static int[] add(int addend, int... ints) {
        int[] result = new int[ints.length];
        for (int index = 0; index < ints.length; index++) {
            result[index] = ints[index] + addend;
        }
        return result;
    }

    /**
     * Finds the first occurrence of the target among the given
     * integers.  Returns the index of the target or -1 if the target
     * was not found.
     */
    public static int indexOf(int target, int... ints) {
        for (int index = 0; index < ints.length; index++) {
            if (ints[index] == target) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Stand-in for {@link
     * java.lang.String.join(CharSequence,CharSequence)} in Java 1.8.
     */
    public static String join(String delimiter, String... pieces) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < pieces.length; index++) {
            builder.append(pieces[index]);
            if (index < (pieces.length - 1)) {
                builder.append(delimiter);
            }
        }
        return builder.toString();
    }

    /**
     * Converts each of the given strings to integers (using {@link
     * Integer.parseInt(String)}).
     */
    public static int[] parseInts(String... strings) {
        int[] ints = new int[strings.length];
        for (int index = 0; index < strings.length; index++) {
            ints[index] = Integer.parseInt(strings[index]);
        }
        return ints;
    }

    /**
     * Converts each of the given integers to strings (using {@link
     * Object.toString()}).
     */
    public static String[] toStrings(int... ints) {
        String[] strings = new String[ints.length];
        for (int index = 0; index < ints.length; index++) {
            strings[index] = Integer.toString(ints[index]);
        }
        return strings;
    }
}
