/*
 * Copyright (c) 2012 Roc Project.  See LICENSE.txt for details.
 */

package mloss.roc;


import java.util.List;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;


public class CurveDataPrimitivesBuilderTest {

    // Sorted labels based on predicteds and actuals below
    public static final int[] rankedLabels = {
        1, 1, 1, 3, 2, 3, 2, 2, 2, 3, 2, 3, 1, 3, 3, 1, 1
    };

    // [random.uniform(-100, 100) for i in xrange(17)]
    public static final double[] predicteds = {
        -18.95840085578395, 93.4748011165401, -75.0369638305275,
        95.62739569795454, 96.41839667685787, 37.65397831676424,
        -71.21440043496705, -14.789818865549293, -46.052787553164464,
        -34.01160708843891, -64.97691643233858, -19.27039969775535,
        60.96527431783812, 25.8607377777939, -32.51746953761858,
        42.236700131527414, -78.93297842093148
    };

    // [random.choice((1, 2, 3)) for i in xrange(17)]
    public static final int[] actuals = {
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
    CurveData.PrimitivesBuilder builder;

    @Before public void setUp() {
        builder = new CurveData.PrimitivesBuilder();
    }

    /**
     * Tests {@link CurveData.PrimitivesBuilder.rankedLabels(int[])} and
     * {@link CurveData.PrimitivesBuilder.positiveLabel(int)}.
     */
    @Test public void testBuildWithRankedLabels() {
        CurveData curve = builder.rankedLabels(rankedLabels).build();
        assertArrayEquals(posCounts, curve.truePositiveCounts);
        assertArrayEquals(negCounts, curve.falsePositiveCounts);
        curve = builder.positiveLabel(3).build();
        assertArrayEquals(posCountsLabel3, curve.truePositiveCounts);
        assertArrayEquals(negCountsLabel3, curve.falsePositiveCounts);
    }

    //@Test public void testBuildWithRankedLabelsWeights() {
    //}

    /**
     * Tests {@link CurveData.PrimitivesBuilder.predicteds(double[])}
     * and {@link CurveData.PrimitivesBuilder.actuals(int[])} and {@link
     * CurveData.PrimitivesBuilder.positiveLabel(int)}.
     */
    @Test public void testBuildWithPredictedsActuals() {
        CurveData curve = builder.predicteds(predicteds).actuals(actuals)
            .build();
        assertArrayEquals(posCounts, curve.truePositiveCounts);
        assertArrayEquals(negCounts, curve.falsePositiveCounts);
        curve = builder.positiveLabel(3).build();
        assertArrayEquals(posCountsLabel3, curve.truePositiveCounts);
        assertArrayEquals(negCountsLabel3, curve.falsePositiveCounts);
    }

    //@Test public void testBuildWithPredictedsActualsWeights() {
    //}

    /**
     * Tests {@link
     * CurveData.PrimitivesBuilder.primitiveArrayToList(int[])}.
     */
    @Test public void testPrimitiveArrayToListInteger() {
        List<Integer> list = CurveData.PrimitivesBuilder
            .primitiveArrayToList(new int[] {});
        assertEquals(0, list.size());
        list = CurveData.PrimitivesBuilder.primitiveArrayToList(actuals);
        assertEquals(17, list.size());
        CurveDataBuilderTest.ArrayIterable<Integer> iterable =
            new CurveDataBuilderTest
            .ArrayIterable<Integer>(intArrayToIntegerArray(actuals));
        CurveDataBuilderTest.assertIterablesEqual(iterable, list);
    }

    /**
     * Tests {@link
     * CurveData.PrimitivesBuilder.primitiveArrayToList(double[])}.
     */
    @Test public void testPrimitiveArrayToListDouble() {
        List<Double> list = CurveData.PrimitivesBuilder
            .primitiveArrayToList(new double[] {});
        assertEquals(0, list.size());
        list = CurveData.PrimitivesBuilder.primitiveArrayToList(predicteds);
        assertEquals(17, list.size());
        CurveDataBuilderTest.ArrayIterable<Double> iterable =
            new CurveDataBuilderTest
            .ArrayIterable<Double>(doubleArrayToDoubleArray(predicteds));
        CurveDataBuilderTest.assertIterablesEqual(iterable, list);
    }


    ////////////////////////////////////////


    public static Integer[] intArrayToIntegerArray(int[] array) {
        Integer[] objectArray = new Integer[array.length];
        for (int index = 0; index < array.length; ++index) {
            objectArray[index] = Integer.valueOf(array[index]);
        }
        return objectArray;
    }

    public static Double[] doubleArrayToDoubleArray(double[] array) {
        Double[] objectArray = new Double[array.length];
        for (int index = 0; index < array.length; ++index) {
            objectArray[index] = Double.valueOf(array[index]);
        }
        return objectArray;
    }
}
