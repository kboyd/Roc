/*
 * Copyright (c) 2013 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/** Tests {@link Curve}. */
public class CurveTest {

    public static final double TOLERANCE = 1.0e-10;

    // Test data

    static final int[] labelsWorst = {0, 0, 1, 1, 1, 1, 1};
    static final int[] labelsWorst_posCounts = {0, 0, 0, 1, 2, 3, 4, 5};
    static final int[] labelsWorst_negCounts = {0, 1, 2, 2, 2, 2, 2, 2};

    static final int[] labelsAverage = {1, 0, 1, 1, 0, 0, 1, 0, 0, 1};
    static final int[] labelsAverage_posCounts = {0, 1, 1, 2, 3, 3, 3, 4, 4, 4, 5};
    static final int[] labelsAverage_negCounts = {0, 0, 1, 1, 1, 2, 3, 3, 4, 5, 5};

    static final int[] labelsBest = {1, 1, 0, 0, 0};
    static final int[] labelsBest_posCounts = {0, 1, 2, 2, 2, 2};
    static final int[] labelsBest_negCounts = {0, 0, 0, 1, 2, 3};

    static final int[] staircaseLabels = {1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0};

    /* Random positive and negative counts for testing curves that may
     * have irregular count increments (not the standard where only the
     * positive or the negative count increments by one for each
     * increase in rank).
     *
     * Python code:
     *
     * import random
     * incs = [0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 3, 3, 4]
     * ps = [0] * 15
     * ns = [0] * 15
     * for i in xrange(1, len(ps)):
     *     ps[i] = ps[i - 1] + random.choice(incs)
     * for i in xrange(1, len(ns)):
     *     ns[i] = ns[i - 1] + random.choice(incs)
     */
    static final int[] random_posCounts = {0, 1, 4, 5, 5, 5, 7, 8, 10, 12, 12, 13, 13, 15, 16};
    static final int[] random_negCounts = {0, 0, 1, 2, 6, 9, 10, 10, 13, 13, 16, 18, 18, 20, 20};

    Curve curve;
    Curve randCurve;
    Curve staircaseCurve;
    @Before public void setUp() {
        curve = new Curve(labelsAverage);
        randCurve = new Curve(random_posCounts, random_negCounts);
        staircaseCurve = new Curve(staircaseLabels);
    }

    /**
     * Tests {@link Curve.buildCounts(int[])} and {@link
     * Curve.buildCounts(int[], int)}.
     */
    @Test public void testBuildCountsFromHardLabels() {
        Curve curve = new Curve(labelsWorst);
        assertArrayEquals(labelsWorst_posCounts, curve.truePositiveCounts);
        assertArrayEquals(labelsWorst_negCounts, curve.falsePositiveCounts);
        assertEquals(5, curve.totalPositives);
        assertEquals(2, curve.totalNegatives);

        curve = new Curve(labelsWorst, 0);  // Non-default positive label
        assertArrayEquals(labelsWorst_negCounts, curve.truePositiveCounts);
        assertArrayEquals(labelsWorst_posCounts, curve.falsePositiveCounts);
        assertEquals(2, curve.totalPositives);
        assertEquals(5, curve.totalNegatives);

        curve = new Curve(labelsAverage);
        assertArrayEquals(labelsAverage_posCounts, curve.truePositiveCounts);
        assertArrayEquals(labelsAverage_negCounts, curve.falsePositiveCounts);
        assertEquals(5, curve.totalPositives);
        assertEquals(5, curve.totalNegatives);

        curve = new Curve(labelsAverage, 0);  // Non-default positive label
        assertArrayEquals(labelsAverage_negCounts, curve.truePositiveCounts);
        assertArrayEquals(labelsAverage_posCounts, curve.falsePositiveCounts);
        assertEquals(5, curve.totalPositives);
        assertEquals(5, curve.totalNegatives);

        curve = new Curve(labelsBest);
        assertArrayEquals(labelsBest_posCounts, curve.truePositiveCounts);
        assertArrayEquals(labelsBest_negCounts, curve.falsePositiveCounts);
        assertEquals(2, curve.totalPositives);
        assertEquals(3, curve.totalNegatives);

        curve = new Curve(labelsBest, 0);  // Non-default positive label
        assertArrayEquals(labelsBest_negCounts, curve.truePositiveCounts);
        assertArrayEquals(labelsBest_posCounts, curve.falsePositiveCounts);
        assertEquals(3, curve.totalPositives);
        assertEquals(2, curve.totalNegatives);
    }

    public int[][] createConfusionMatrices(int[] posCounts, int[] negCounts) {
        int[][] matrices = new int[posCounts.length][4];
        int totPos = posCounts[posCounts.length - 1];
        int totNeg = negCounts[negCounts.length - 1];
        for (int matrixIndex = 0; matrixIndex < matrices.length; matrixIndex++) {
            matrices[matrixIndex][0] = posCounts[matrixIndex];
            matrices[matrixIndex][1] = negCounts[matrixIndex];
            matrices[matrixIndex][2] = totPos - posCounts[matrixIndex];
            matrices[matrixIndex][3] = totNeg - negCounts[matrixIndex];
        }
        return matrices;
    }

    /** Tests {@link Curve.confusionMatrix(int)}. */
    @Test public void testConfusionMatrix() {
        // Normal
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

        // Random
        expected = createConfusionMatrices(random_posCounts, random_negCounts);
        for (int expectedIndex = 0; expectedIndex < expected.length; expectedIndex++) {
            assertArrayEquals(expected[expectedIndex], randCurve.confusionMatrix(expectedIndex));
        }
    }

    public double[][] createRocPoints(int[] posCounts, int[] negCounts) {
        double[][] points = new double[posCounts.length][2];
        double totPos = (double) posCounts[posCounts.length - 1];
        double totNeg = (double) negCounts[negCounts.length - 1];
        for (int pointIndex = 0; pointIndex < points.length; pointIndex++) {
            points[pointIndex][0] = (double) negCounts[pointIndex] / totNeg;  // FPR on X
            points[pointIndex][1] = (double) posCounts[pointIndex] / totPos;  // TPR on Y
        }
        return points;
    }

    /** Tests {@link Curve.rocPoint(int)}. */
    @Test public void testRocPoint() {
        // Normal
        // Remember FPR horizontal, TPR vertical
        // Do these in fractions for human readability and floating-point precision
        double[][] expected = {
            {0.0/5.0, 0.0/5.0},  // 0
            {0.0/5.0, 1.0/5.0},  // 1
            {1.0/5.0, 1.0/5.0},  // 2
            {1.0/5.0, 2.0/5.0},  // 3
            {1.0/5.0, 3.0/5.0},  // 4
            {2.0/5.0, 3.0/5.0},  // 5
            {3.0/5.0, 3.0/5.0},  // 6
            {3.0/5.0, 4.0/5.0},  // 7
            {4.0/5.0, 4.0/5.0},  // 8
            {5.0/5.0, 4.0/5.0},  // 9
            {5.0/5.0, 5.0/5.0}   // 10
        };
        for (int expectedIndex = 0; expectedIndex < expected.length; expectedIndex++) {
            assertArrayEquals(expected[expectedIndex], curve.rocPoint(expectedIndex), TOLERANCE);
        }

        // Random
        expected = createRocPoints(random_posCounts, random_negCounts);
        for (int expectedIndex = 0; expectedIndex < expected.length; expectedIndex++) {
            assertArrayEquals(expected[expectedIndex], randCurve.rocPoint(expectedIndex), TOLERANCE);
        }
    }

    /** Tests {@link Curve.rocPoints()}. */
    @Test public void testRocPoints() {
        // Normal
        double[][] expected = createRocPoints(labelsAverage_posCounts, labelsAverage_negCounts);
        double[][] actual = curve.rocPoints();
        assertEquals(expected.length, actual.length);
        for (int expectedIndex = 0; expectedIndex < expected.length; expectedIndex++) {
            assertArrayEquals(expected[expectedIndex], actual[expectedIndex], TOLERANCE);
        }

        // Random
        expected = createRocPoints(random_posCounts, random_negCounts);
        actual = randCurve.rocPoints();
        assertEquals(expected.length, actual.length);
        for (int expectedIndex = 0; expectedIndex < expected.length; expectedIndex++) {
            assertArrayEquals(expected[expectedIndex], actual[expectedIndex], TOLERANCE);
        }
    }

    /** Tests {@link Curve.rocArea()}. */
    @Test public void testRocArea() {
        // Normal
        // Points: Rectangles (w * h = area):
        // 1-2: 0.2 * 0.2 = 1.0 / 25.0
        // 4-5: 0.2 * 0.6 = 3.0 / 25.0
        // 5-6: 0.2 * 0.6 = 3.0 / 25.0
        // 7-8: 0.2 * 0.8 = 4.0 / 25.0
        // 8-9: 0.2 * 0.8 = 4.0 / 25.0
        // ---------------------------
        // Sum: 15.0 / 25.0 = 0.6
        double expected = 15.0 / 25.0;
        assertEquals(expected, curve.rocArea(), TOLERANCE);

        // Random
        // Fields: point, increment type, area calculation
        // Think of the increment type as ASCII art for a ROC plot
        // rotated 90 degrees clockwise, which corresponds to the TPR
        // points along the left, as they are below.
        // ( 0/20,  0/16), .,
        // ( 0/20,  1/16), -,
        // ( 1/20,  4/16), \, 1/20 * (1/16 + 4/16)/2 = 5/640
        // ( 2/20,  5/16), \, 1/20 * (4/16 + 5/16)/2 = 9/640
        // ( 6/20,  5/16), |,
        // ( 9/20,  5/16), |, 7/20 * 5/16 = 35/320 = 70/640
        // (10/20,  7/16), \, 1/20 * (5/16 + 7/16)/2 = 6/320 = 12/640
        // (10/20,  8/16), -,
        // (13/20, 10/16), \, 3/20 * (8/16 + 10/16)/2 = 27/320 = 54/640
        // (13/20, 12/16), -,
        // (16/20, 12/16), |, 3/20 * 12/16 = 36/320 = 72/640
        // (18/20, 13/16), \, 2/20 * (12/16 + 13/16)/2 = 50/640
        // (18/20, 13/16), .,
        // (20/20, 15/16), \, 2/20 * (13/16 + 15/16)/2 = 28/320 = 56/640
        // (20/20, 16/16), -,
        // Total: 328/640 = 41/80 = 0.5125
        expected = 41.0 / 80.0;
        assertEquals(expected, randCurve.rocArea(), TOLERANCE);

        // Test a few possibly pathological cases

        // No area
        curve = new Curve(labelsWorst_posCounts, labelsWorst_negCounts);
        assertEquals(0.0, curve.rocArea(), TOLERANCE);

        // Half area
        int[] posCounts_half = {0, 1};
        int[] negCounts_half = {0, 1};
        curve = new Curve(posCounts_half, negCounts_half);
        assertEquals(0.5, curve.rocArea(), TOLERANCE);

        // Full area
        curve = new Curve(labelsBest_posCounts, labelsBest_negCounts);
        assertEquals(1.0, curve.rocArea(), TOLERANCE);
    }

    // x-axis: recall = tp / (tp + fn) = tp / #p
    // y-axis: precision = tp / (tp + fp)

    static final double[][] expectedRawPrPoints_curve = {
        {0.0/5.0, 1.0/1.0},  // 0  // This is undefined, so we choose to extend the first point to the left.  (Copy the vertical value of the first point.)
        {1.0/5.0, 1.0/1.0},  // 1
        {1.0/5.0, 1.0/2.0},  // 2
        {2.0/5.0, 2.0/3.0},  // 3
        {3.0/5.0, 3.0/4.0},  // 4
        {3.0/5.0, 3.0/5.0},  // 5
        {3.0/5.0, 3.0/6.0},  // 6
        {4.0/5.0, 4.0/7.0},  // 7
        {4.0/5.0, 4.0/8.0},  // 8
        {4.0/5.0, 4.0/9.0},  // 9
        {5.0/5.0, 5.0/10.0}  // 10
    };

    static final double[][] expectedPrPoints_curve = expectedRawPrPoints_curve;

    static final double[][] expectedRawPrPoints_randCurve = {
        { 0.0/16.0,   1.0/1.0},
        { 1.0/16.0,   1.0/1.0},
        { 4.0/16.0,   4.0/5.0},
        { 5.0/16.0,   5.0/7.0},
        { 5.0/16.0,  5.0/11.0},
        { 5.0/16.0,  5.0/14.0},
        { 7.0/16.0,  7.0/17.0},
        { 8.0/16.0,  8.0/18.0},
        {10.0/16.0, 10.0/23.0},
        {12.0/16.0, 12.0/25.0},
        {12.0/16.0, 12.0/28.0},
        {13.0/16.0, 13.0/31.0},
        {13.0/16.0, 13.0/31.0},
        {15.0/16.0, 15.0/35.0},
        {16.0/16.0, 16.0/36.0}
    };

    static final double[][] expectedPrPoints_randCurve = {
        { 0.0/16.0,   1.0/1.0},
        { 1.0/16.0,   1.0/1.0},
        { 1.0/16.0,   4.0/5.0}, // "lower-left" addition
        { 4.0/16.0,   4.0/5.0},
        { 4.0/16.0,   5.0/7.0}, // "lower-left" addition
        { 5.0/16.0,   5.0/7.0},
        { 5.0/16.0,  5.0/11.0},
        { 5.0/16.0,  5.0/14.0},
        { 5.0/16.0,  7.0/17.0}, // "lower-left" addition
        { 7.0/16.0,  7.0/17.0},
        { 8.0/16.0,  8.0/18.0},
        { 8.0/16.0, 10.0/23.0}, // "lower-left" addition
        {10.0/16.0, 10.0/23.0},
        {12.0/16.0, 12.0/25.0},
        {12.0/16.0, 12.0/28.0},
        {12.0/16.0, 13.0/31.0}, // "lower-left" addition
        {13.0/16.0, 13.0/31.0},
        {13.0/16.0, 13.0/31.0},
        {13.0/16.0, 15.0/35.0}, // "lower-left" addition
        {15.0/16.0, 15.0/35.0},
        {16.0/16.0, 16.0/36.0}
    };

    /** Tests {@link Curve.prPoint(int)}. */
    @Test public void testPrPoint() {
        // Normal
        for (int expectedIndex = 0;
             expectedIndex < expectedRawPrPoints_curve.length;
             expectedIndex++) {
            assertArrayEquals(expectedRawPrPoints_curve[expectedIndex],
                              curve.prPoint(expectedIndex), TOLERANCE);
        }

        // Random
        for (int expectedIndex = 0;
             expectedIndex < expectedRawPrPoints_randCurve.length;
             expectedIndex++) {
            assertArrayEquals(expectedRawPrPoints_randCurve[expectedIndex],
                              randCurve.prPoint(expectedIndex), TOLERANCE);
        }
    }

    /** Tests {@link Curve.prPoints()}. */
    @Test public void testPrPoints() {
        // Normal
        double[][] actual = curve.prPoints();
        assertEquals(expectedPrPoints_curve.length, actual.length);
        for (int expectedIndex = 0;
             expectedIndex < expectedPrPoints_curve.length;
             expectedIndex++) {
            assertArrayEquals(expectedPrPoints_curve[expectedIndex],
                              actual[expectedIndex], TOLERANCE);
            //System.out.println(java.util.Arrays.toString(actual[expectedIndex]) + ",");
        }

        // Random
        actual = randCurve.prPoints();
        assertEquals(expectedPrPoints_randCurve.length, actual.length);
        for (int expectedIndex = 0;
             expectedIndex < expectedPrPoints_randCurve.length;
             expectedIndex++) {
            assertArrayEquals(expectedPrPoints_randCurve[expectedIndex],
                              actual[expectedIndex], TOLERANCE);
            //System.out.println(java.util.Arrays.toString(actual[expectedIndex]) + ",");
        }
    }

    /** Tests {@link Curve.prArea()}. */
    @Test public void testPrArea() {
        // Normal
        // Fields: point, increment type, area calculation
        // Think of the increment type as ASCII art for the line
        // connecting the previous and current point in a PR plot
        // (0/5,  1/1), .,
        // (1/5,  1/1), -, 1/5 * 1 = 1/5
        // (1/5,  1/2), |,
        // (2/5,  2/3), /, 1/5 * (1/2 + 2/3)/2 = 7/60
        // (3/5,  3/4), /, 1/5 * (2/3 + 3/4)/2 = 17/120
        // (3/5,  3/5), |,
        // (3/5,  3/6), |,
        // (4/5,  4/7), /, 1/5 * (3/6 + 4/7)/2 = 1/5 * 45/84 = 3/28
        // (4/5,  4/8), |,
        // (4/5,  4/9), |,
        // (5/5, 5/10), /, 1/5 * (4/9 + 5/10)/2 = 1/5 * 85/180 = 17/180
        // Total: 1663/2520 = 0.6599
        double expected = 1663.0 / 2520.0;
        assertEquals(expected, curve.prArea(), TOLERANCE);

        // Random
        // ( 0/16,   1/1}, .,
        // ( 1/16,   1/1}, -, 1/16 * 1 = 1/16
        // ( 1/16,   4/5}, |,
        // ( 4/16,   4/5}, -, 3/16 * 4/5 = 12/80 = 3/20
        // ( 4/16,   5/7}, |,
        // ( 5/16,   5/7}, -, 1/16 * 5/7 = 5/112
        // ( 5/16,  5/11}, |,
        // ( 5/16,  5/14}, |,
        // ( 5/16,  7/17}, |,
        // ( 7/16,  7/17}, -, 2/16 * 7/17 = 14/272 = 7/136
        // ( 8/16,  8/18}, /, 1/16 * (7/17 + 8/18)/2 = 1/16 * 131/306 = 131/4896
        // ( 8/16, 10/23}, |,
        // (10/16, 10/23}, -, 2/16 * 10/23 = 10/184 = 5/92
        // (12/16, 12/25}, /, 2/16 * (10/23 + 12/25)/2 = 1/8 * 263/575 = 263/4600
        // (12/16, 12/28}, |,
        // (12/16, 13/31}, |,
        // (13/16, 13/31}, -, 1/16 * 13/31 = 13/496
        // (13/16, 13/31}, .,
        // (13/16, 15/35}, |,
        // (15/16, 15/35}, -, 2/16 * 15/35 = 15/280 = 3/56
        // (16/16, 16/36}, /, 1/16 * (15/35 + 16/36)/2 = 1/16 * 55/126 = 55/2016
        // Total (Thanks Maxima!): 169204981/305449200 = 0.553954572478828
        expected = 169204981.0 / 305449200.0;
        assertEquals(expected, randCurve.prArea(), TOLERANCE);
    }

    static final int[] convexHull_randomXs = {0, 1, 1, 2, 2, 4, 4, 5, 5, 5, 5, 5, 6, 6, 6, 7, 7, 8, 9, 9};
    static final int[] convexHull_randomYs = {2, 3, 5, 2, 5, 5, 7, 2, 4, 6, 7, 9, 0, 7, 7, 4, 5, 0, 2, 4};
    static final int[] convexHull_negCounts = {0, 0, 1, 3, 5, 5, 6, 7, 9, 11, 12};
    static final int[] convexHull_posCounts = {0, 1, 1, 1, 2, 4, 6, 8, 10, 10, 12};
    static final int[] expectedNegCounts_convexHull = {0, 0, 9, 12};
    static final int[] expectedPosCounts_convexHull = {0, 1, 10, 12};

    /** Tests {@link Curve.convexHullPoints(int[], int[])}. */
    @Test public void testConvexHullPoints() {
        // Full area, convex hull is left and top of rectangle
        int[][] points = Curve.convexHullPoints(labelsBest_negCounts, labelsBest_posCounts);
        int[] xs = {0, 0, 3};
        int[] ys = {0, 2, 2};
        assertArrayEquals(xs, points[0]);
        assertArrayEquals(ys, points[1]);

        // No area, convex hull is positive slope diagonal
        points = Curve.convexHullPoints(labelsBest_posCounts, labelsBest_negCounts);
        xs = new int[]{0, 2};
        ys = new int[]{0, 3};
        assertArrayEquals(xs, points[0]);
        assertArrayEquals(ys, points[1]);

        // Convex hull with (sorted) random scatter
        points = Curve.convexHullPoints(convexHull_randomXs, convexHull_randomYs);
        xs = new int[]{0, 1, 5, 9};
        ys = new int[]{2, 5, 9, 4};
        assertArrayEquals(xs, points[0]);
        assertArrayEquals(ys, points[1]);

        // Convex hull with interesting but realistic counts
        points = Curve.convexHullPoints(convexHull_negCounts, convexHull_posCounts);
        assertArrayEquals(expectedNegCounts_convexHull, points[0]);
        assertArrayEquals(expectedPosCounts_convexHull, points[1]);
    }

    /** Tests {@link Curve.convexHull()}. */
    @Test public void testConvexHull() {
        // Normal
        Curve hull = curve.convexHull();
        int[] expectedPosCounts_curve = {0, 1, 3, 5};
        int[] expectedNegCounts_curve = {0, 0, 1, 5};
        assertArrayEquals(expectedPosCounts_curve, hull.truePositiveCounts);
        assertArrayEquals(expectedNegCounts_curve, hull.falsePositiveCounts);
        assertTrue(hull.rocArea() >= curve.rocArea());

        // Random
        hull = randCurve.convexHull();
        int[] expectedPosCounts_random = {0, 1, 4, 5, 12, 16};
        int[] expectedNegCounts_random = {0, 0, 1, 2, 13, 20};
        assertArrayEquals(expectedPosCounts_random, hull.truePositiveCounts);
        assertArrayEquals(expectedNegCounts_random, hull.falsePositiveCounts);
        assertTrue(hull.rocArea() >= randCurve.rocArea());

        // Interesting but realistic
        curve = new Curve(convexHull_posCounts, convexHull_negCounts);
        hull = curve.convexHull();
        assertArrayEquals(expectedPosCounts_convexHull, hull.truePositiveCounts);
        assertArrayEquals(expectedNegCounts_convexHull, hull.falsePositiveCounts);
        assertTrue(hull.rocArea() >= curve.rocArea());
    }

    /** Tests {@link Curve.mannWhitneyU()}. */
    @Test public void testMannWhitneyU() {
        /* There are 2 samples; rank them all together.  For each
         * sample, r is the sum of ranks for that sample, n is the
         * sample size, u is the statistic.
         *
         * Formula:
         * u = r - (n * (n + 1)) / 2
         * Identities:
         * r1 + r0 = N * (N + 1) / 2,  N = n1 + n0
         * u1 + u0 = n1 * n0
         */

        // Expected arrays are {posU, negU}

        // u1 = (1+3+4+7+10) - 5 * 6 / 2 = 10
        // u0 = (2+5+6+8+9) - 5 * 6 / 2 = 15
        double[] expectedUsCurve = {10.0, 15.0};  // sum: 25 (= 5 * 5)
        assertArrayEquals(expectedUsCurve, curve.mannWhitneyU(), TOLERANCE);
        // u1 = (1+3+5+7+9+11+13) - 7 * 8 / 2 = 21
        // u0 = (2+4+6+8+10+12+14) - 7 * 8 / 2 = 28
        double[] expectedUsStair = {21.0, 28.0};  // sum: 49 (= 7 * 7)
        assertArrayEquals(expectedUsStair, staircaseCurve.mannWhitneyU(), TOLERANCE);
        // u1 = (1*1+3*3.5+1*6.5+0*9.5+0*13+2*16+1*18+2*21+2*24.5+0*27+1*30+2*33.5+1*36) - 16 * 17 / 2 = 156
        // u0 = (0*1+1*3.5+1*6.5+4*9.5+3*13+1*16+0*18+3*21+0*24.5+3*27+2*30+2*33.5+0*36) - 20 * 21 / 2 = 164
        double[] expectedUsRandom = {156.0, 164.0};  // sum: 320 (= 16 * 20)
        assertArrayEquals(expectedUsRandom, randCurve.mannWhitneyU(), TOLERANCE);

        // The individual U statistics can come out non-integer
        int[] posCounts = {0, 1, 3, 5, 6};
        int[] negCounts = {0, 0, 1, 3, 4};
        curve = new Curve(posCounts, negCounts);
        // u1 = (1*1+2*3+2*6.5+1*9.5) - 6 * 7 / 2 = 8.5
        // u0 = (0*1+1*3+2*6.5+1*9.5) - 4 * 5 / 2 = 15.5
        double[] expectedUsNonInt = {8.5, 15.5};  // sum: 24 (= 6 * 4)
        assertArrayEquals(expectedUsNonInt, curve.mannWhitneyU(), TOLERANCE);
    }

    /**
     * Tests for integer overflow bug in {@link Curve.rocArea()} and
     * {@link Curve.mannWhitneyU()} that produces incorrect (and
     * negative) ROC areas when totalPositives and totalNegatives are
     * greater than 2^16.  https://github.com/kboyd/Roc/issues/15
     */
    @Test public void testRocAreaIntegerOverflow() {
	// Use 2^17 positives and negatives.
	int n = 1<<17;
	// Check when in perfect order with correct rocArea of 1.
	int[] labels = new int[n+n];
	// Positives (1) come first.
	Arrays.fill(labels,0,n,1);
	// Negatives (0) come after.
	Arrays.fill(labels,n,n+n,0);
	Curve hugeCurve = new Curve(labels);
	double expectedRocArea = 1.0;
	assertEquals(expectedRocArea, hugeCurve.rocArea(),TOLERANCE);
	double[] expectedMannWhitneyU = {0.0,(double) n * (double)n};
	assertArrayEquals(expectedMannWhitneyU,hugeCurve.mannWhitneyU(),TOLERANCE);
	
	// Check when in worst order with correct rocArea of 0.
	// Negatives (0) come first.
	Arrays.fill(labels,0,n,0);
	// Positives (1) come after.
	Arrays.fill(labels,n,n+n,1);
	hugeCurve = new Curve(labels);
	expectedRocArea = 0.0;
	assertEquals(expectedRocArea, hugeCurve.rocArea(),TOLERANCE);
	expectedMannWhitneyU = new double[]{(double)n * (double) n,0.0};
	assertArrayEquals(expectedMannWhitneyU,hugeCurve.mannWhitneyU(),TOLERANCE);
    }
}
