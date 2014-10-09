/*
 * Copyright (c) 2013 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc;


import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import static mloss.roc.util.Assert.*;
import mloss.roc.util.IterableArray;


/** Tests {@link Curve.Builder}. */
public class CurveBuilderTest {

    // Ranked labels in various forms
    public static final String[] rankedLabelsArray = {
        "1", "2", "3", "1", "2", "2", "1", "1", "3", "1", "1"
    };
    public static final Iterable<String> rankedLabelsSequence =
        new IterableArray<String>(rankedLabelsArray);
    public static final List<String> rankedLabelsList =
        Collections.unmodifiableList(Arrays.asList(rankedLabelsArray));

    // Predicteds and actuals in various forms
    public static final Double[] predictedsArray = {
        0.47341896085274016, 0.018859285548017635, 0.9315716230462066,
        0.9335304877912131, 0.7290329817040795, 0.9838470565991855,
        0.9819062711720467, 0.5422121795480975, 0.5023282102530746,
        0.13125000483509008, 0.04172635518302947
    };
    // ranks: {8, 11, 4, 3, 5, 1, 2, 6, 7, 9, 10}
    public static final String[] actualsArray = {
        "1", "1", "1", "3", "2", "1", "2", "2", "1", "3", "1"
    };
    public static final Iterable<Double> predictedsSequence =
        new IterableArray<Double>(predictedsArray);
    public static final Iterable<String> actualsSequence =
        new IterableArray<String>(actualsArray);
    public static final List<Double> predictedsList =
        Collections.unmodifiableList(Arrays.asList(predictedsArray));
    public static final List<String> actualsList =
        Collections.unmodifiableList(Arrays.asList(actualsArray));

    // An object different than any of the labels but that will compare equal
    public static final String positiveLabel = new String("1");

    // Correct counts based on the above data
    public static final int[] posCounts = {0, 1, 1, 1, 2, 2, 2, 3, 4, 4, 5, 6};
    public static final int[] negCounts = {0, 0, 1, 2, 2, 3, 4, 4, 4, 5, 5, 5};

    // Object under test
    Curve.Builder<Double, String> builder;

    @Before public void setUp() {
        builder = new Curve.Builder<Double, String>();
    }

    /** Tests {@link Curve.Builder.rankedLabels(Iterable)}. */
    @Test public void testBuildWithRankedLabels() {
        // Check sequence instantiation
        builder.rankedLabels(rankedLabelsSequence);
        assertNotSame(rankedLabelsSequence, builder.rankedLabels);
        assertIterablesEqual(rankedLabelsSequence, builder.rankedLabels);

        // Check list reuse
        builder.rankedLabels(rankedLabelsList);
        assertSame(rankedLabelsList, builder.rankedLabels);

        // Check build
        Curve curve = builder.positiveLabel(positiveLabel).build();
        assertArrayEquals(posCounts, curve.truePositiveCounts);
        assertArrayEquals(negCounts, curve.falsePositiveCounts);
    }

    //@Test public void testBuildWithRankedLabelsWeights() {
    //}

    /**
     * Tests {@link Curve.Builder.predicteds(Iterable)} and {@link
     * Curve.Builder.predicteds(Iterable)}.
     */
    @Test public void testBuildWithPredictedsActuals() {
        // Check sequence instantiation
        builder.predicteds(predictedsSequence).actuals(actualsSequence);
        assertNotSame(predictedsSequence, builder.predicteds);
        assertIterablesEqual(predictedsSequence, builder.predicteds);
        assertNotSame(actualsSequence, builder.actuals);
        assertIterablesEqual(actualsSequence, builder.actuals);

        // Check list reuse
        builder.predicteds(predictedsList).actuals(actualsList);
        assertSame(predictedsList, builder.predicteds);
        assertSame(actualsList, builder.actuals);

        // Check build
        Curve curve = builder.positiveLabel(positiveLabel).build();
        assertArrayEquals(posCounts, curve.truePositiveCounts);
        assertArrayEquals(negCounts, curve.falsePositiveCounts);
    }

    //@Test public void testBuildWithPredictedsActualsWeights() {
    //}

    /** Tests {@link Curve.Builder.comparator(Comparator)}. */
    @Test public void testComparator() {
        builder.comparator(new Comparator<Number>() {
                // Compare only tenths (and larger) digits
                public int compare(Number n1, Number n2) {
                    return ((int) (10.0 * n1.doubleValue()))
                        - ((int) (10.0 * n2.doubleValue()));
                }
            });
        /* The above comparator results in the following (effective)
         * predicteds and actuals:
         *
         * [4, 0, 9, 9, 7, 9, 9, 5, 5, 1, 0]
         * [1, 1, 1, 3, 2, 1, 2, 2, 1, 3, 1]
         *
         * The same lists after stable sorting by descending score are:
         *
         * [9, 9, 9, 9, 7, 5, 5, 4, 1, 0, 0]
         * [1, 3, 1, 2, 2, 2, 1, 1, 3, 1, 1]
         *
         * This produces the following pos and neg counts (with positive
         * label = 1):
         */
        int[] posCounts = {0, 1, 1, 2, 2, 2, 2, 3, 4, 4, 5, 6};
        int[] negCounts = {0, 0, 1, 1, 2, 3, 4, 4, 4, 5, 5, 5};
        Curve curve = builder.predicteds(predictedsList).actuals(actualsList)
            .positiveLabel(positiveLabel).build();
        assertArrayEquals(posCounts, curve.truePositiveCounts);
        assertArrayEquals(negCounts, curve.falsePositiveCounts);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidState_empty() {
        builder.build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidState_noLabelPa() {
        builder
            .predicteds(predictedsList)
            .actuals(actualsList)
            .build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidState_noLabelRl() {
        builder
            .rankedLabels(rankedLabelsList)
            .build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidState_noActuals() {
        builder
            .predicteds(predictedsList)
            .positiveLabel(positiveLabel)
            .build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidState_noPredicteds() {
        builder
            .actuals(actualsList)
            .positiveLabel(positiveLabel)
            .build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidState_emptyRankedLabels() {
        List<String> emptyLabels = Collections.emptyList();
        builder
            .rankedLabels(emptyLabels)
            .positiveLabel(positiveLabel)
            .build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidState_emptyPredicteds() {
        List<Double> emptyPredicteds = Collections.emptyList();
        builder
            .predicteds(emptyPredicteds)
            .actuals(actualsList)
            .positiveLabel(positiveLabel)
            .build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidState_emptyActuals() {
        List<String> emptyActuals = Collections.emptyList();
        builder
            .predicteds(predictedsList)
            .actuals(emptyActuals)
            .positiveLabel(positiveLabel)
            .build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidState_paSize() {
        List<Double> predicteds = new LinkedList<Double>(predictedsList);
        predicteds.add(6.28);
        builder
            .predicteds(predicteds)
            .actuals(actualsList)
            .positiveLabel(positiveLabel)
            .build();
    }

//    @Test(expected=IllegalArgumentException.class)
//    public void testInvalidStatePAWSize() {
//        List<Double> weights = Collections.emptyList();
//        builder.predicteds(predictedsList).actuals(actualsList).weights(weights)
//            .positiveLabel(positiveLabel).build();
//    }

//    @Test(expected=IllegalArgumentException.class)
//    public void testInvalidStateRLWSize() {
//        List<Double> weights = Collections.emptyList();
//        builder.rankedLabels(rankedLabelsList).weights(weights)
//            .positiveLabel(positiveLabel).build();
//    }

    /** Tests {@link Curve.Builder.instantiateSequence(Iterable)}. */
    @Test public void instantiateSequence() {
        Random random = new Random();
        int sequenceSize = random.nextInt(31);
        Long[] array = new Long[sequenceSize];
        for (int sequenceIndex = 0; sequenceIndex < sequenceSize; sequenceIndex++) {
            array[sequenceIndex] = Long.valueOf(random.nextLong());
        }
        Iterable<Long> sequence = new IterableArray<Long>(array);
        List<Long> list = Curve.Builder.instantiateSequence(sequence);
        assertNotSame(sequence, list);
        assertIterablesEqual(sequence, list);
    }

    /**
     * Tests {@link Curve.Builder.TupleScoreReverseComparator} with
     * natural orderings.
     */
    @Test public void testTupleScoreReverseComparatorNaturalOrdering() {
        Curve.Builder<Double, String>.TupleScoreReverseComparator comparator =
            builder.new TupleScoreReverseComparator();
        Curve.Builder<Double, String>.Tuple tuple1 = builder.new Tuple(3.33, "pos");
        Curve.Builder<Double, String>.Tuple tuple2 = builder.new Tuple(3.14, "neg");
        Curve.Builder<Double, String>.Tuple tuple3 =
            builder.new Tuple(new Double(3.33), "neg");  // Avoid identity equality
        // Test basic ranking orderings
        assertEquals(-1, comparator.compare(tuple1, tuple2));
        assertEquals(1, comparator.compare(tuple2, tuple1));
        // Test value equality and ignoring of labels
        assertEquals(0, comparator.compare(tuple1, tuple3));
    }

    /**
     * Tests {@link Curve.Builder.TupleScoreReverseComparator} with
     * an explicit, given comparator.
     */
    @Test public void testTupleScoreReverseComparatorExplicitComparator() {
        // Create a builder for a score type with richer comparison possibilities
        Curve.Builder<String, String> builder = new Curve.Builder<String, String>();
        /* Create a non-standard comparator that compares strings by
         * length.  Have the comparator work for objects to test
         * Comparator<? super TScore>.
         */
        Curve.Builder<String, String>.TupleScoreReverseComparator comparator =
            builder.new TupleScoreReverseComparator(new Comparator<Object>() {
                    public int compare(Object o1, Object o2) {
                        if (o1 instanceof String && o2 instanceof String) {
                            return ((String) o1).length() - ((String) o2).length();
                        } else {
                            return System.identityHashCode(o1) - System.identityHashCode(o2);
                        }
                    }
                });
        Curve.Builder<String, String>.Tuple tuple1 = builder.new Tuple("22", "pos");
        Curve.Builder<String, String>.Tuple tuple2 = builder.new Tuple("333", "neg");
        Curve.Builder<String, String>.Tuple tuple3 = builder.new Tuple("two", "neg");
        // Test basic ranking orderings
        assertEquals(1, comparator.compare(tuple1, tuple2));
        assertEquals(-1, comparator.compare(tuple2, tuple1));
        assertEquals(0, comparator.compare(tuple2, tuple3));
    }
}
