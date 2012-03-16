/*
 * Copyright (c) 2012 Roc Project.  See LICENSE.txt for details.
 */

package mloss.roc;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/** Tests {@link Curve}. */
public class CurveTest {

    public static final double TOLERANCE = 1.0e-10;

    // Test data

    static final int[] labelsWorst = {0, 0, 1, 1, 1, 1, 1};
    static final int[] labelsWorst_counts = {0, 0, 0, 1, 2, 3, 4, 5};

    static final int[] labelsAverage = {1, 0, 1, 1, 0, 0, 1, 0, 0, 1};
    static final int[] labelsAverage_counts = {0, 1, 1, 2, 3, 3, 3, 4, 4, 4, 5};

    static final int[] labelsBest = {1, 1, 0, 0, 0};
    static final int[] labelsBest_counts = {0, 1, 2, 2, 2, 2};

    Curve curve;

    @Before public void setUp() {
        curve = new Curve(labelsAverage);
    }

    /** Tests {@link Curve.buildCounts(int[])}. */
    @Test public void testBuildCountsFromHardLabels() {
        Curve curve = new Curve(labelsWorst);
        assertArrayEquals(labelsWorst_counts, curve.truePositiveCounts);
        assertEquals(5, curve.totalPositives);
        assertEquals(2, curve.totalNegatives);

        curve = new Curve(labelsAverage);
        assertArrayEquals(labelsAverage_counts, curve.truePositiveCounts);
        assertEquals(5, curve.totalPositives);
        assertEquals(5, curve.totalNegatives);

        curve = new Curve(labelsBest);
        assertArrayEquals(labelsBest_counts, curve.truePositiveCounts);
        assertEquals(2, curve.totalPositives);
        assertEquals(3, curve.totalNegatives);
    }

    /** Tests {@link Curve.confusionMatrix(int)}. */
    @Test public void testConfusionMatrix() {
        int[][] expected = {
            {0, 0, 5, 5},
            {1, 0, 4, 5},
            {1, 1, 4, 4},
            {2, 1, 3, 4},
            {3, 1, 2, 4},
            {3, 2, 2, 3},
            {3, 3, 2, 2},
            {4, 3, 1, 2},
            {4, 4, 1, 1},
            {4, 5, 1, 0},
            {5, 5, 0, 0}
        };
        for (int expectedIndex = 0; expectedIndex < expected.length; expectedIndex++) {
            assertArrayEquals(expected[expectedIndex], curve.confusionMatrix(expectedIndex));
        }
    }

    /** Tests {@link Curve.point(int)}. */
    @Test public void testPoint() {
        // Remember FPR horizontal, TPR vertical
        // Do these in fractions for human readability and floating-point precision
        double[][] expected = {
            {0.0, 0.0},  // 0
            {0.0, 1.0 / 5.0},  // 1
            {1.0 / 5.0, 1.0 / 5.0},  // 2
            {1.0 / 5.0, 2.0 / 5.0},  // 3
            {1.0 / 5.0, 3.0 / 5.0},  // 4
            {2.0 / 5.0, 3.0 / 5.0},  // 5
            {3.0 / 5.0, 3.0 / 5.0},  // 6
            {3.0 / 5.0, 4.0 / 5.0},  // 7
            {4.0 / 5.0, 4.0 / 5.0},  // 8
            {1.0, 4.0 / 5.0},  // 9
            {1.0, 1.0}  // 10
        };
        for (int expectedIndex = 0; expectedIndex < expected.length; expectedIndex++) {
            assertArrayEquals(expected[expectedIndex], curve.point(expectedIndex), TOLERANCE);
        }
    }

    /** Tests {@link Curve.area()}. */
    @Test public void testArea() {
        // Points: Rectangles (w * h = area):
        // 1-2: 0.2 * 0.2 = 1.0 / 25.0
        // 4-5: 0.2 * 0.6 = 3.0 / 25.0
        // 5-6: 0.2 * 0.6 = 3.0 / 25.0
        // 7-8: 0.2 * 0.8 = 4.0 / 25.0
        // 8-9: 0.2 * 0.8 = 4.0 / 25.0
        // ---------------------------
        // Sum: 15.0 / 25.0 = 0.6
        double expected = 15.0 / 25.0;
        assertEquals(expected, curve.area(), TOLERANCE);
    }
}
