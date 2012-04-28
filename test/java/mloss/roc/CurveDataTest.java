/*
 * Copyright (c) 2012 Roc Project.  See LICENSE.txt for details.
 */

package mloss.roc;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/** Tests {@link CurveData}. */
public class CurveDataTest {

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

    CurveData curve;
    CurveData randCurve;

    @Before public void setUp() {
        curve = new CurveData(labelsAverage);
	randCurve = new CurveData(random_posCounts, random_negCounts);
    }

    /** Tests {@link CurveData.buildCounts(int[])} and {@link
     * CurveData.buildCounts(int[], int)}.
     */
    @Test public void testBuildCountsFromHardLabels() {
        CurveData curve = new CurveData(labelsWorst);
        assertArrayEquals(labelsWorst_posCounts, curve.truePositiveCounts);
        assertArrayEquals(labelsWorst_negCounts, curve.falsePositiveCounts);
        assertEquals(5, curve.totalPositives);
        assertEquals(2, curve.totalNegatives);

        curve = new CurveData(labelsWorst, 0);  // Non-default positive label
        assertArrayEquals(labelsWorst_negCounts, curve.truePositiveCounts);
        assertArrayEquals(labelsWorst_posCounts, curve.falsePositiveCounts);
        assertEquals(2, curve.totalPositives);
        assertEquals(5, curve.totalNegatives);

        curve = new CurveData(labelsAverage);
        assertArrayEquals(labelsAverage_posCounts, curve.truePositiveCounts);
        assertArrayEquals(labelsAverage_negCounts, curve.falsePositiveCounts);
        assertEquals(5, curve.totalPositives);
        assertEquals(5, curve.totalNegatives);

        curve = new CurveData(labelsAverage, 0);  // Non-default positive label
        assertArrayEquals(labelsAverage_negCounts, curve.truePositiveCounts);
        assertArrayEquals(labelsAverage_posCounts, curve.falsePositiveCounts);
        assertEquals(5, curve.totalPositives);
        assertEquals(5, curve.totalNegatives);

        curve = new CurveData(labelsBest);
        assertArrayEquals(labelsBest_posCounts, curve.truePositiveCounts);
        assertArrayEquals(labelsBest_negCounts, curve.falsePositiveCounts);
        assertEquals(2, curve.totalPositives);
        assertEquals(3, curve.totalNegatives);

        curve = new CurveData(labelsBest, 0);  // Non-default positive label
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

    /** Tests {@link CurveData.confusionMatrix(int)}. */
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

    /** Tests {@link CurveData.rocPoint(int)}. */
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

    /** Tests {@link CurveData.rocPoints()}. */
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

    /** Tests {@link CurveData.rocArea()}. */
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
	// Point, Increment Type (Curve Line), Area Calculation
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
	curve = new CurveData(labelsWorst_posCounts, labelsWorst_negCounts);
        assertEquals(0.0, curve.rocArea(), TOLERANCE);

	// Half area
	int[] posCounts_half = {0, 1};
	int[] negCounts_half = {0, 1};
	curve = new CurveData(posCounts_half, negCounts_half);
        assertEquals(0.5, curve.rocArea(), TOLERANCE);

	// Full area
	curve = new CurveData(labelsBest_posCounts, labelsBest_negCounts);
        assertEquals(1.0, curve.rocArea(), TOLERANCE);
    }

    // x-axis: recall = tp / (tp + fn) = tp / #p
    // y-axis: precision = tp / (tp + fp)

    static final double[][] expectedRawPrPoints_curve = {
	{0.0/5.0,     0.0},  // 0  // TODO decide what to do about this case: 0.0 / (0.0 + 0.0)
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

    static final double[][] expectedRawPrPoints_randCurve = {
	{ 0.0/16.0,       0.0},
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

    /** Tests {@link CurveData.prPoint(int)}. */
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

    /** Tests {@link CurveData.rawPrPoints()}. */
    @Test public void testRawPrPoints() {
	// Normal
	double[][] actual = curve.rawPrPoints();
	assertEquals(expectedRawPrPoints_curve.length, actual.length);
	for (int expectedIndex = 0;
	     expectedIndex < expectedRawPrPoints_curve.length;
	     expectedIndex++) {
	    assertArrayEquals(expectedRawPrPoints_curve[expectedIndex],
			      actual[expectedIndex], TOLERANCE);
	}

	// Random
	actual = randCurve.rawPrPoints();
	assertEquals(expectedRawPrPoints_randCurve.length, actual.length);
	for (int expectedIndex = 0;
	     expectedIndex < expectedRawPrPoints_randCurve.length;
	     expectedIndex++) {
	    assertArrayEquals(expectedRawPrPoints_randCurve[expectedIndex],
			      actual[expectedIndex], TOLERANCE);
	}
    }

    static final double[][] expectedPrPoints_curve = {
	{1.0/5.0, 1.0/1.0},  //
	{1.0/5.0, 1.0/2.0},
	{1.0/5.0, 1.0/2.0},  //
	{2.0/5.0, 1.0/2.0},
	{2.0/5.0, 2.0/3.0},  //
	{3.0/5.0, 2.0/3.0},
	{3.0/5.0, 3.0/4.0},  //
	{3.0/5.0, 3.0/5.0},
	{3.0/5.0, 3.0/5.0},  //
	{3.0/5.0, 3.0/6.0},
	{3.0/5.0, 3.0/6.0},  //
	{4.0/5.0, 3.0/6.0},
	{4.0/5.0, 4.0/7.0},  //
	{4.0/5.0, 4.0/8.0},
	{4.0/5.0, 4.0/8.0},  //
	{4.0/5.0, 4.0/9.0},
	{4.0/5.0, 4.0/9.0},  //
	{5.0/5.0, 4.0/9.0},
	{5.0/5.0, 5.0/10.0}  //
    };

    static final double[][] expectedPrPoints_randCurve = {
	{ 1.0/16.0,   1.0/1.0},  //
	{ 1.0/16.0,   4.0/5.0},
	{ 4.0/16.0,   4.0/5.0},  //
	{ 4.0/16.0,   5.0/7.0},
	{ 5.0/16.0,   5.0/7.0},  //
	{ 5.0/16.0,  5.0/11.0},
	{ 5.0/16.0,  5.0/11.0},  //
	{ 5.0/16.0,  5.0/14.0},
	{ 5.0/16.0,  5.0/14.0},  //
	{ 7.0/16.0,  5.0/14.0},
	{ 7.0/16.0,  7.0/17.0},  //
	{ 8.0/16.0,  7.0/17.0},
	{ 8.0/16.0,  8.0/18.0},  //
	{ 8.0/16.0, 10.0/23.0},
	{10.0/16.0, 10.0/23.0},  //
	{12.0/16.0, 10.0/23.0},
	{12.0/16.0, 12.0/25.0},  //
	{12.0/16.0, 12.0/28.0},
	{12.0/16.0, 12.0/28.0},  //
	{12.0/16.0, 13.0/31.0},
	{13.0/16.0, 13.0/31.0},  //
	{13.0/16.0, 13.0/31.0},
	{13.0/16.0, 13.0/31.0},  //
	{15.0/16.0, 13.0/31.0},
	{15.0/16.0, 15.0/35.0},  //
	{16.0/16.0, 15.0/35.0},
	{16.0/16.0, 16.0/36.0}   //
    };

    /** Tests {@link CurveData.prPoints()}. */
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

    static final int[] convexHull_randomXs = {0, 1, 1, 2, 2, 4, 4, 5, 5, 5, 5, 5, 6, 6, 6, 7, 7, 8, 9, 9};
    static final int[] convexHull_randomYs = {2, 3, 5, 2, 5, 5, 7, 2, 4, 6, 7, 9, 0, 7, 7, 4, 5, 0, 2, 4};
    static final int[] convexHull_negCounts = {0, 0, 1, 3, 5, 5, 6, 7, 9, 11, 12};
    static final int[] convexHull_posCounts = {0, 1, 1, 1, 2, 4, 6, 8, 10, 10, 12};
    static final int[] expectedNegCounts_convexHull = {0, 0, 9, 12};
    static final int[] expectedPosCounts_convexHull = {0, 1, 10, 12};

    /** Tests {@link CurveData.convexHullPoints(int[], int[])}. */
    @Test public void testConvexHullPoints() {
	// Full area, convex hull is left and top of rectangle
	int[][] points = CurveData.convexHullPoints(labelsBest_negCounts, labelsBest_posCounts);
	int[] xs = {0, 0, 3};
	int[] ys = {0, 2, 2};
	assertArrayEquals(xs, points[0]);
	assertArrayEquals(ys, points[1]);

	// No area, convex hull is positive slope diagonal
	points = CurveData.convexHullPoints(labelsBest_posCounts, labelsBest_negCounts);
	xs = new int[]{0, 2};
	ys = new int[]{0, 3};
	assertArrayEquals(xs, points[0]);
	assertArrayEquals(ys, points[1]);

	// Convex hull with (sorted) random scatter
	points = CurveData.convexHullPoints(convexHull_randomXs, convexHull_randomYs);
	xs = new int[]{0, 1, 5, 9};
	ys = new int[]{2, 5, 9, 4};
	assertArrayEquals(xs, points[0]);
	assertArrayEquals(ys, points[1]);

	// Convex hull with interesting but realistic counts
	points = CurveData.convexHullPoints(convexHull_negCounts, convexHull_posCounts);
	assertArrayEquals(expectedNegCounts_convexHull, points[0]);
	assertArrayEquals(expectedPosCounts_convexHull, points[1]);
    }

    /** Tests {@link CurveData.convexHull()}. */
    @Test public void testConvexHull() {
	// Normal
	CurveData hull = curve.convexHull();
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
	curve = new CurveData(convexHull_posCounts, convexHull_negCounts);
	hull = curve.convexHull();
	assertArrayEquals(expectedPosCounts_convexHull, hull.truePositiveCounts);
	assertArrayEquals(expectedNegCounts_convexHull, hull.falsePositiveCounts);
	assertTrue(hull.rocArea() >= curve.rocArea());
    }
}
