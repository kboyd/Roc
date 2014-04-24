/*
 * Copyright (c) 2014 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc.util;

import java.util.Arrays;
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

    public static class StringArrayComparator implements Comparator<String[]> {
        int[] keyFields1;
        int[] keyFields2;

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
