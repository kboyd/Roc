/*
 * Copyright (c) 2014 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc;


import java.util.List;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import static mloss.roc.util.Assert.*;
import mloss.roc.util.Arrays;
import mloss.roc.util.IterableArray;


public class CurvePrimitivesBuilderTest {

    // Sorted labels based on scores and labels below
    public static final int[] rankedLabels = {
        1, 1, 1, 3, 2, 3, 2, 2, 2, 3, 2, 3, 1, 3, 3, 1, 1
    };

    // [random.uniform(-100, 100) for i in xrange(17)]
    public static final double[] scores = {
        -18.95840085578395, 93.4748011165401, -75.0369638305275,
        95.62739569795454, 96.41839667685787, 37.65397831676424,
        -71.21440043496705, -14.789818865549293, -46.052787553164464,
        -34.01160708843891, -64.97691643233858, -19.27039969775535,
        60.96527431783812, 25.8607377777939, -32.51746953761858,
        42.236700131527414, -78.93297842093148
    };

    // [random.choice((1, 2, 3)) for i in xrange(17)]
    public static final int[] labels = {
        2, 1, 1,
        1, 1, 3,
        3, 2, 1,
        3, 3, 3,
        3, 2, 2,
        2, 1
    };

    // Correct counts based on the above data (positive label = 1)
    public static final int[] posCounts = {
        0, 1, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 5, 6
    };
    public static final int[] negCounts = {
        0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 10, 11, 11, 11
    };

    // Correct counts based on the above data (positive label = 3)
    public static final int[] posCountsLabel3 = {
        0, 0, 0, 0, 1, 1, 2, 2, 2, 2, 3, 3, 4, 4, 5, 6, 6, 6
    };
    public static final int[] negCountsLabel3 = {
        0, 1, 2, 3, 3, 4, 4, 5, 6, 7, 7, 8, 8, 9, 9, 9, 10, 11
    };

    // Object under test
    Curve.PrimitivesBuilder builder;

    @Before
    public void setUp() {
        builder = new Curve.PrimitivesBuilder();
    }

    /**
     * Tests {@link Curve.PrimitivesBuilder.rankedLabels(int[])} and
     * {@link Curve.PrimitivesBuilder.positiveLabel(int)}.
     */
    @Test
    public void testBuildWithRankedLabels() {
        Curve curve = builder.rankedLabels(rankedLabels).build();
        assertArrayEquals(posCounts, curve.truePositiveCounts);
        assertArrayEquals(negCounts, curve.falsePositiveCounts);
        curve = builder.positiveLabel(3).build();
        assertArrayEquals(posCountsLabel3, curve.truePositiveCounts);
        assertArrayEquals(negCountsLabel3, curve.falsePositiveCounts);
    }

    //@Test public void testBuildWithRankedLabelsWeights() {
    //}

    /**
     * Tests {@link Curve.PrimitivesBuilder.scores(double[])} and
     * {@link Curve.PrimitivesBuilder.labels(int[])} and {@link
     * Curve.PrimitivesBuilder.positiveLabel(int)}.
     */
    @Test
    public void testBuildWithScoresLabels() {
        Curve curve = builder.scores(scores).labels(labels)
            .build();
        assertArrayEquals(posCounts, curve.truePositiveCounts);
        assertArrayEquals(negCounts, curve.falsePositiveCounts);
        curve = builder.positiveLabel(3).build();
        assertArrayEquals(posCountsLabel3, curve.truePositiveCounts);
        assertArrayEquals(negCountsLabel3, curve.falsePositiveCounts);
    }

    //@Test public void testBuildWithScoresLabelsWeights() {
    //}

    /**
     * Tests {@link
     * Curve.PrimitivesBuilder.primitiveArrayToList(int[])}.
     */
    @Test
    public void testPrimitiveArrayToListInteger() {
        List<Integer> list = Curve.PrimitivesBuilder
            .primitiveArrayToList(new int[] {});
        assertEquals(0, list.size());
        list = Curve.PrimitivesBuilder.primitiveArrayToList(labels);
        assertEquals(17, list.size());
        IterableArray<Integer> iterable = new IterableArray<Integer>
            (Arrays.intArrayToIntegerArray(labels));
        assertIterablesEqual(iterable, list);
    }

    /**
     * Tests {@link
     * Curve.PrimitivesBuilder.primitiveArrayToList(double[])}.
     */
    @Test
    public void testPrimitiveArrayToListDouble() {
        List<Double> list = Curve.PrimitivesBuilder
            .primitiveArrayToList(new double[] {});
        assertEquals(0, list.size());
        list = Curve.PrimitivesBuilder.primitiveArrayToList(scores);
        assertEquals(17, list.size());
        IterableArray<Double> iterable = new IterableArray<Double>
            (Arrays.doubleArrayToDoubleArray(scores));
        assertIterablesEqual(iterable, list);
    }
}
