/*
 * Copyright (c) 2014 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


/**
 * Basic utilities for working with tabular text data (sequences of
 * homogeneous rows).
 */
public class CsvProcessing {

    /**
     * Comparator for CSV rows (and similar) that uses certain fields as
     * (compound) keys for comparison.  Can be used to sort a single set
     * of rows like in sorting or compare two sets of rows like in
     * joining.
     */
    public static class StringArrayComparator implements Comparator<String[]> {
        int[] keyFields1;
        int[] keyFields2;

        /**
         * Creates a comparator for CSV rows that compares rows
         * according to the given (possibly compound) keys.  The fields
         * are matched in the given order.  For example, {4, 1, 2} and
         * {3, 0, 1} specify the equality constraint: csv1.4 == csv2.3
         * and csv1.1 == csv2.0 and csv1.2 == csv2.1.  To create a
         * comparator to order a single set of rows make {@code
         * keyFields1 == keyFields2} as the two CSV rows will be from
         * the same source.
         *
         * @param keyFields1 The indices of the fields to use as keys
         * from the first CSV input
         * @param keyFields2 The indices of the fields to use as keys
         * from the second CSV input
         */
        public StringArrayComparator(int[] keyFields1, int[] keyFields2) {
            // Check the arrays are of the same length
            if (keyFields1.length != keyFields2.length) {
                new IllegalArgumentException("Arrays of key fields must be the same length.");
            }
            this.keyFields1 = keyFields1;
            this.keyFields2 = keyFields2;
        }

        public int compare(String[] row1, String[] row2) {
            // Compare strings field-by-field for the key fields
            String field1;
            String field2;
            int cmp;
            for (int index = 0; index < keyFields1.length; index++) {
                field1 = row1[keyFields1[index]];
                field2 = row2[keyFields2[index]];
                cmp = field1.compareTo(field2);
                if (cmp != 0) {
                    return cmp;
                }
            }
            return 0;
        }
    }

    /**
     * Projects the fields from the given inputs into a single tuple.
     *
     * @param fields The selected fields.  The outer array is a list of
     * fields to include.  Each inner array is a pair {@code
     * {inputIndex, fieldIndex}} accessing the field {@code
     * tuples[inputIndex][fieldIndex]}.
     * @param tuples The inputs to project.
     */
    public static String[] project(int[][] fields, String[]... tuples) {
        int length = fields.length;
        String[] result = new String[length];
        int[] field;
        for (int fieldIndex = 0; fieldIndex < length; fieldIndex++) {
            field = fields[fieldIndex];
            result[fieldIndex] = tuples[field[0]][field[1]];
        }
        return result;
    }

    // TODO projectAll?

    /**
     * Performs an inner join of the given CSV inputs using the merge
     * join algorithm.  Sorts the given inputs by their (compound) keys
     * and then merge joins them, yielding the desired projection of
     * fields.
     *
     * @param keys The keys (and, hence, equality constraints) for the
     * join.  The outer array is a list of equality constraints.  Each
     * inner array specifies an equality constraint by listing the keys
     * that should be equal.  Each element of an inner array is a field
     * index for the corresponding CSV input.  Their order and number
     * matches that of the inputs.  For example, {@code {{3, 2}, {0,
     * 1}}} sets up the following compound equality constraint for the
     * join: csv1.3 == csv2.2 and csv1.0 == csv2.1.
     * @param fields The fields to include in each result tuple, the
     * desired projection.  The outer array lists the fields.  Each
     * inner array is a pair {@code {inputIndex, fieldIndex}} that
     * specifies a field to include in the output.  The fields are
     * output in the given order.  For example, {{1, 3}, {1, 2}, {0, 0},
     * {0, 5}} yields tuples consisting of the fields (csv2.3, csv2.2,
     * csv1.0, csv1.5).
     * @param csv1 The first CSV input
     * @param csv2 The second CSV input
     */
    public static List<String[]> mergeJoin(
            int[][] keys,
            int[][] fields,
            Iterable<String[]> csv1,
            Iterable<String[]> csv2) {

        // Instantiate each input as necessary to enable sorting
        List<String[]> sorted1;
        if (csv1 instanceof Collection) {
            sorted1 = new ArrayList<String[]>((Collection<String[]>) csv1);
        } else {
            sorted1 = new ArrayList<String[]>(100);
            for (String[] row : csv1) {
                sorted1.add(row);
            }
        }
        List<String[]> sorted2;
        if (csv2 instanceof Collection) {
            sorted2 = new ArrayList<String[]>((Collection<String[]>) csv2);
        } else {
            sorted2 = new ArrayList<String[]>(100);
            for (String[] row : csv2) {
                sorted2.add(row);
            }
        }

        // Unzip the keys from equality pairs into keys for each CSV
        // individually (transpose the matrix)
        int[] keys1 = new int[keys.length];
        for (int index = 0; index < keys.length; index++) {
            keys1[index] = keys[index][0];
        }
        int[] keys2 = new int[keys.length];
        for (int index = 0; index < keys.length; index++) {
            keys2[index] = keys[index][1];
        }

        // Create comparators for sorting and merging
        StringArrayComparator cmp1 = new StringArrayComparator(keys1, keys1);
        StringArrayComparator cmp2 = new StringArrayComparator(keys2, keys2);
        StringArrayComparator mergeCmp = new StringArrayComparator(keys1, keys2);

        // Sort each input
        Collections.sort(sorted1, cmp1);
        Collections.sort(sorted2, cmp2);

        // Merge the two CSVs together
        List<String[]> merged = new LinkedList<String[]>();
        int size1 = sorted1.size();
        int size2 = sorted2.size();
        int index1 = 0;
        int range2Lo = 0;
        int range2Hi = 0;
        String[] row1;
        String[] row2;
        String[] projected;
        int cmp;
        while (index1 < size1 && range2Lo < size2) {
            row1 = sorted1.get(index1);
            row2 = sorted2.get(range2Lo);
            cmp = mergeCmp.compare(row1, row2);
            if (cmp < 0) {
                // No match, advance 1
                index1++;
            } else if (cmp > 0) {
                // No match, advance 2.  If there was just an equal
                // block in input 2, skip the block.  Otherwise just
                // advance normally.
                if (range2Lo < range2Hi) {
                    range2Lo = range2Hi;
                } else {
                    range2Lo++;
                }
            } else {
                // Join all equal rows of 2 with current element of 1
                range2Hi = range2Lo;
                do {
                    // Combine the rows from the two inputs
                    projected = project(fields, row1, row2);
                    merged.add(projected);
                    // Advance 2
                    range2Hi++;
                    // Get and compare the next row, if any
                    if (range2Hi < size2) {
                        row2 = sorted2.get(range2Hi);
                        cmp = mergeCmp.compare(row1, row2);
                    } else {
                        cmp = -1;  // Input 1 is "before" end
                    }
                } while (cmp == 0);  // range2Hi checked above
                // Equal block done.  Advance 1.
                index1++;
            }
        }
        return merged;
    }
}
