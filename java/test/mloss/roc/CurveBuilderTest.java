/*
 * Copyright (c) 2015 Roc Project.  This is free software.  See
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

    // Scores and labels in various forms
    public static final Double[] scoresArray = {
        0.47341896085274016, 0.018859285548017635, 0.9315716230462066,
        0.9335304877912131, 0.7290329817040795, 0.9838470565991855,
        0.9819062711720467, 0.5422121795480975, 0.5023282102530746,
        0.13125000483509008, 0.04172635518302947
    };
    // ranks: {8, 11, 4, 3, 5, 1, 2, 6, 7, 9, 10}
    public static final String[] labelsArray = {
        "1", "1", "1", "3", "2", "1", "2", "2", "1", "3", "1"
    };
    public static final Iterable<Double> scoresSequence =
        new IterableArray<Double>(scoresArray);
    public static final Iterable<String> labelsSequence =
        new IterableArray<String>(labelsArray);
    public static final List<Double> scoresList =
        Collections.unmodifiableList(Arrays.asList(scoresArray));
    public static final List<String> labelsList =
        Collections.unmodifiableList(Arrays.asList(labelsArray));

    // An object different than any of the labels but that will compare equal
    public static final String positiveLabel = new String("1");

    // Correct counts based on the above data
    public static final int[] posCounts = {0, 1, 1, 1, 2, 2, 2, 3, 4, 4, 5, 6};
    public static final int[] negCounts = {0, 0, 1, 2, 2, 3, 4, 4, 4, 5, 5, 5};

    // Tied scores and labels
    public static final Integer[] tiedScoresArray = { // 5 levels
        3, 1, 5, 1, 4, 2, 1, 3, 3, 1, 1, 1, 2,
    };
    public static final Integer[] tiedLabelsArray = { // 3 classes
        3, 3, 3, 1, 2, 1, 2, 1, 1, 1, 2, 2, 1,
    };
    public static final Integer tiedPositiveLabel = new Integer(1);
    public static final int[] tiedPosCounts = {0, 0, 0, 2, 4, 6};
    public static final int[] tiedNegCounts = {0, 1, 2, 3, 3, 7};
    // Collections forms of above
    public static final Iterable<Integer> tiedScoresSequence =
        new IterableArray<Integer>(tiedScoresArray);
    public static final Iterable<Integer> tiedLabelsSequence =
        new IterableArray<Integer>(tiedLabelsArray);

    // Object under test
    Curve.Builder<Double, String> builder;

    @Before
    public void setUp() {
        builder = new Curve.Builder<Double, String>();
    }

    /** Tests {@link Curve.Builder.rankedLabels(Iterable)}. */
    @Test
    public void testBuildWithRankedLabels() {
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
     * Tests {@link Curve.Builder.scores(Iterable)} and {@link
     * Curve.Builder.labels(Iterable)}.
     */
    @Test
    public void testBuildWithScoresLabels() {
        // Check sequence instantiation
        builder.scores(scoresSequence).labels(labelsSequence);
        assertNotSame(scoresSequence, builder.scores);
        assertIterablesEqual(scoresSequence, builder.scores);
        assertNotSame(labelsSequence, builder.labels);
        assertIterablesEqual(labelsSequence, builder.labels);

        // Check list reuse
        builder.scores(scoresList).labels(labelsList);
        assertSame(scoresList, builder.scores);
        assertSame(labelsList, builder.labels);

        // Check build
        Curve curve = builder.positiveLabel(positiveLabel).build();
        assertArrayEquals(posCounts, curve.truePositiveCounts);
        assertArrayEquals(negCounts, curve.falsePositiveCounts);
    }

    @Test
    public void testBuildWithTiedScoresLabels() {
        Curve.Builder<Integer, Integer> builder =
            new Curve.Builder<Integer, Integer>();
        Curve curve = builder.
            scores(tiedScoresSequence).
            labels(tiedLabelsSequence).
            positiveLabel(tiedPositiveLabel).
            build();
        assertArrayEquals(tiedPosCounts, curve.truePositiveCounts);
        assertArrayEquals(tiedNegCounts, curve.falsePositiveCounts);
    }

    //@Test public void testBuildWithScoresLabelsWeights() {
    //}

    /** Tests {@link Curve.Builder.comparator(Comparator)}. */
    @Test
    public void testComparator() {
        builder.comparator(new Comparator<Number>() {
                // Compare only tenths (and larger) digits
                public int compare(Number n1, Number n2) {
                    return ((int) (10.0 * n1.doubleValue()))
                        - ((int) (10.0 * n2.doubleValue()));
                }
            });
        /* The above comparator results in the following (effective)
         * scores and labels:
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
        Curve curve = builder.scores(scoresList).labels(labelsList)
            .positiveLabel(positiveLabel).build();
        assertArrayEquals(posCounts, curve.truePositiveCounts);
        assertArrayEquals(negCounts, curve.falsePositiveCounts);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidState_empty() {
        builder.build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidState_noPositiveLabelSl() {
        builder
            .scores(scoresList)
            .labels(labelsList)
            .build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidState_noPositiveLabelRl() {
        builder
            .rankedLabels(rankedLabelsList)
            .build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidState_noLabels() {
        builder
            .scores(scoresList)
            .positiveLabel(positiveLabel)
            .build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidState_noScores() {
        builder
            .labels(labelsList)
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
    public void testInvalidState_emptyScores() {
        List<Double> emptyScores = Collections.emptyList();
        builder
            .scores(emptyScores)
            .labels(labelsList)
            .positiveLabel(positiveLabel)
            .build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidState_emptyLabels() {
        List<String> emptyLabels = Collections.emptyList();
        builder
            .scores(scoresList)
            .labels(emptyLabels)
            .positiveLabel(positiveLabel)
            .build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidState_paSize() {
        List<Double> scores = new LinkedList<Double>(scoresList);
        scores.add(6.28);
        builder
            .scores(scores)
            .labels(labelsList)
            .positiveLabel(positiveLabel)
            .build();
    }

//    @Test(expected=IllegalArgumentException.class)
//    public void testInvalidStatePAWSize() {
//        List<Double> weights = Collections.emptyList();
//        builder.scores(scoresList).labels(labelsList).weights(weights)
//            .positiveLabel(positiveLabel).build();
//    }

//    @Test(expected=IllegalArgumentException.class)
//    public void testInvalidStateRLWSize() {
//        List<Double> weights = Collections.emptyList();
//        builder.rankedLabels(rankedLabelsList).weights(weights)
//            .positiveLabel(positiveLabel).build();
//    }

    /** Tests {@link Curve.Builder.instantiateSequence(Iterable)}. */
    @Test
    public void instantiateSequence() {
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
    @Test
    public void testTupleScoreReverseComparatorNaturalOrdering() {
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
    @Test
    public void testTupleScoreReverseComparatorExplicitComparator() {
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
