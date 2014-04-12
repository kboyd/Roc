/*
 * Copyright (c) 2014 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc.util;

import java.util.List;

import static org.junit.Assert.*;
import org.junit.Test;


/** Tests (@link CsvProcessing}. */
public class CsvProcessingTest {

    public static String[][] csv1 = {
        // key1, key2, val1, val2, val3
        // (random.randint(0, 5), random.choice('abc'), random.choice('defghi'), random.randint(0, 9), random.choice('ny'))
        {"2", "c", "h", "5", "n"}, // 0
        {"4", "a", "f", "7", "y"},
        {"0", "c", "e", "5", "n"},
        {"0", "c", "f", "3", "n"},
        {"3", "c", "i", "1", "y"},
        {"1", "a", "f", "1", "y"}, // 5
        {"0", "a", "d", "1", "y"},
        {"1", "a", "g", "7", "y"},
        {"5", "a", "f", "5", "n"},
        {"3", "c", "i", "2", "n"},
        {"5", "a", "g", "4", "n"}, // 10
    };

    public static String[][] csv2 = {
        // key1, val1, key2, val2, key3, val3, val4
        // (random.randint(2, 9), random.choice('nmyx'), random.choice('abcd'), random.choice('defghi'), random.randint(0, 1), random.randint(0, 9), random.choice(string.lowercase))
        {"6", "n", "d", "f", "0", "4", "z"}, // 0
        {"7", "x", "c", "g", "1", "8", "q"},
        {"4", "m", "a", "h", "1", "6", "g"},
        {"6", "y", "c", "g", "1", "8", "h"},
        {"5", "y", "b", "g", "1", "2", "f"},
        {"7", "m", "b", "h", "1", "6", "s"}, // 5
        {"2", "y", "a", "i", "0", "9", "v"},
        {"8", "y", "b", "f", "1", "7", "b"},
        {"6", "n", "d", "i", "1", "3", "m"},
        {"6", "x", "a", "h", "0", "2", "p"},
        {"2", "m", "c", "d", "1", "3", "l"}, // 10
        {"9", "n", "c", "h", "0", "7", "a"},
        {"4", "y", "a", "i", "1", "3", "u"},
        {"7", "m", "c", "h", "0", "2", "d"},
        {"4", "y", "d", "i", "0", "3", "y"},
        {"7", "m", "c", "d", "1", "1", "f"}, // 15
        {"2", "n", "b", "i", "0", "9", "x"},
        {"5", "y", "a", "g", "0", "6", "j"},
        {"3", "n", "c", "h", "0", "7", "d"},
        {"8", "m", "d", "d", "1", "6", "o"},
        {"4", "x", "b", "e", "0", "7", "w"}, // 20
        {"6", "x", "b", "e", "0", "7", "s"},
        {"8", "x", "a", "i", "0", "8", "v"},
    };

    @Test public void testProject() {
        int[][] fields;
        String[] expected;
        String[] actual;

        // 0 rows, 0 fields
        fields = new int[][] {};
        expected = new String[] {};
        actual = CsvProcessing.project(fields);
        assertArrayEquals(expected, actual);

        // 1 row, 0 fields
        fields = new int[][] {};
        expected = new String[] {};
        actual = CsvProcessing.project(fields, csv1[2]);
        assertArrayEquals(expected, actual);

        // 1 row, 1 field
        fields = new int[][] {{0, 4}};
        expected = new String[] {"n"};
        actual = CsvProcessing.project(fields, csv1[10]);
        assertArrayEquals(expected, actual);

        // 1 row, 3 fields
        fields = new int[][] {{0, 0}, {0, 3}, {0, 2}};
        expected = new String[] {"5", "5", "f"};
        actual = CsvProcessing.project(fields, csv1[8]);
        assertArrayEquals(expected, actual);

        // 1 row, 3 repeated fields
        fields = new int[][] {{0, 3}, {0, 3}, {0, 3}};
        expected = new String[] {"1", "1", "1"};
        actual = CsvProcessing.project(fields, csv1[5]);
        assertArrayEquals(expected, actual);

        // 2 rows, 0 fields
        fields = new int[][] {};
        expected = new String[] {};
        actual = CsvProcessing.project(fields, csv1[4], csv2[1]);
        assertArrayEquals(expected, actual);

        // 2 rows, 1 field
        fields = new int[][] {{1, 3}};
        expected = new String[] {"i"};
        actual = CsvProcessing.project(fields, csv1[3], csv2[22]);
        assertArrayEquals(expected, actual);

        // 2 rows, 2 fields
        fields = new int[][] {{1, 3}, {1, 1}};
        expected = new String[] {"i", "y"};
        actual = CsvProcessing.project(fields, csv1[10], csv2[14]);
        assertArrayEquals(expected, actual);

        // 2 rows, 5 fields
        fields = new int[][] {{0, 2}, {0, 3}, {1, 4}, {0, 0}, {1, 3}};
        expected = new String[] {"f", "1", "1", "1", "g"};
        actual = CsvProcessing.project(fields, csv1[5], csv2[3]);
        assertArrayEquals(expected, actual);

        // 2 rows, 10 fields
        fields = new int[][] {
            {0, 2}, {0, 4}, {0, 1}, {0, 3}, {0, 1},
            {0, 4}, {1, 5}, {0, 3}, {1, 2}, {0, 3}};
        expected = new String[] {"f", "y", "a", "1", "a", "y", "3", "1", "c", "1"};
        actual = CsvProcessing.project(fields, csv1[5], csv2[10]);
        assertArrayEquals(expected, actual);

        // 3 rows, 3 fields
        fields = new int[][] {{2, 0}, {1, 2}, {0, 2}};
        expected = new String[] {"3", "b", "g"};
        actual = CsvProcessing.project(fields, csv1[10], csv2[5], csv1[4]);
        assertArrayEquals(expected, actual);

        // 3 rows, 7 fields
        fields = new int[][] {{2, 3}, {1, 1}, {1, 6}, {1, 5}, {1, 1}, {1, 2}, {0, 4}};
        expected = new String[] {"5", "n", "x", "9", "n", "b", "y"};
        actual = CsvProcessing.project(fields, csv1[1], csv2[16], csv1[2]);
        assertArrayEquals(expected, actual);
    }

    public void testMergeJoin(
            int[][] keys, int[][] fields,
            String[][] csv1, String[][] csv2,
            String[][] expectedMergeResult) {
        Iterable<String[]> input1 = new IterableArray<String[]>(csv1);
        Iterable<String[]> input2 = new IterableArray<String[]>(csv2);
        List<String[]> result = CsvProcessing.mergeJoin(keys, fields, input1, input2);
        String[][] actual = new String[result.size()][];
        actual = result.toArray(actual);
        assertArrayEquals(expectedMergeResult, actual);
    }

    public static String[][] mergeResult1Key2Vals = {
        // 0.0=1.0, 0.2, 1.6
        {"2", "h", "v"},
        {"2", "h", "l"},
        {"2", "h", "x"},
        {"3", "i", "d"},
        {"3", "i", "d"},
        {"4", "f", "g"},
        {"4", "f", "u"},
        {"4", "f", "y"},
        {"4", "f", "w"},
        {"5", "f", "f"},
        {"5", "f", "j"},
        {"5", "g", "f"},
        {"5", "g", "j"},
    };

    @Test public void testMergeJoin1Key2Vals() {
        int[][] keys = {{0, 0}};
        int[][] fields = {{0, 0}, {0, 2}, {1, 6}};
        testMergeJoin(keys, fields, csv1, csv2, mergeResult1Key2Vals);
        // Test CSVs swapped.  This requires swapping elements 10 and 11
        // of the result.
        String[] swap = mergeResult1Key2Vals[10];
        mergeResult1Key2Vals[10] = mergeResult1Key2Vals[11];
        mergeResult1Key2Vals[11] = swap;
        fields = new int[][] {{0, 0}, {1, 2}, {0, 6}};
        testMergeJoin(keys, fields, csv2, csv1, mergeResult1Key2Vals);
    }

    public static String[][] mergeResult1Key5Vals = {
        // 1.5=0.3, 1.6, 1.3, 0.2, 0.4, 1.4
        {"1", "f", "d", "i", "y", "1"},
        {"1", "f", "d", "f", "y", "1"},
        {"1", "f", "d", "d", "y", "1"},
        {"2", "f", "g", "i", "n", "1"},
        {"2", "p", "h", "i", "n", "0"},
        {"2", "d", "h", "i", "n", "0"},
        {"3", "m", "i", "f", "n", "1"},
        {"3", "l", "d", "f", "n", "1"},
        {"3", "u", "i", "f", "n", "1"},
        {"3", "y", "i", "f", "n", "0"},
        {"4", "z", "f", "g", "n", "0"},
        {"7", "b", "f", "f", "y", "1"},
        {"7", "b", "f", "g", "y", "1"},
        {"7", "a", "h", "f", "y", "0"},
        {"7", "a", "h", "g", "y", "0"},
        {"7", "d", "h", "f", "y", "0"},
        {"7", "d", "h", "g", "y", "0"},
        {"7", "w", "e", "f", "y", "0"},
        {"7", "w", "e", "g", "y", "0"},
        {"7", "s", "e", "f", "y", "0"},
        {"7", "s", "e", "g", "y", "0"},
    };

    @Test public void testMergeJoin1Key5Vals() {
        int[][] keys = {{5, 3}};
        int[][] fields = {{0, 5}, {0, 6}, {0, 3}, {1, 2}, {1, 4}, {0, 4}};
        testMergeJoin(keys, fields, csv2, csv1, mergeResult1Key5Vals);
    }

    public static String[][] mergeResult2Keys2Vals = {
        // 0.0=1.0, 0.1=1.2, 0.2, 1.1
        {"2", "c", "h", "m"},
        {"3", "c", "i", "n"},
        {"3", "c", "i", "n"},
        {"4", "a", "f", "m"},
        {"4", "a", "f", "y"},
        {"5", "a", "f", "y"},
        {"5", "a", "g", "y"},
    };

    @Test public void testMergeJoin2Keys2Vals() {
        int[][] keys = {{0, 0}, {1, 2}};
        int[][] fields = {{0, 0}, {1, 2}, {0, 2}, {1, 1}};
        testMergeJoin(keys, fields, csv1, csv2, mergeResult2Keys2Vals);
    }
}
