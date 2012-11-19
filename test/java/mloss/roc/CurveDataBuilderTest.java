/*
 * Copyright (c) 2012 Roc Project.  See LICENSE.txt for details.
 */

package mloss.roc;


import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;


/** Tests {@link CurveData.Builder}. */
public class CurveDataBuilderTest {
    // Ranked labels in various forms
    public static final String[] rankedLabelsArray = {"1", "2", "3", "1", "2", "2", "1", "1", "3", "1", "1"};
    public static final Iterable<String> rankedLabelsSequence = new ArrayIterable<String>(rankedLabelsArray);
    public static final List<String> rankedLabelsList = Collections.unmodifiableList(Arrays.asList(rankedLabelsArray));

    // Predicteds and actuals in various forms
    public static final Double[] predictedsArray = {0.47341896085274016, 0.018859285548017635, 0.9315716230462066, 0.9335304877912131, 0.7290329817040795, 0.9838470565991855, 0.9819062711720467, 0.5422121795480975, 0.5023282102530746, 0.13125000483509008, 0.04172635518302947};
    // ranks: {8, 11, 4, 3, 5, 1, 2, 6, 7, 9, 10}
    public static final String[] actualsArray = {"1", "1", "1", "3", "2", "1", "2", "2", "1", "3", "1"};
    public static final Iterable<Double> predictedsSequence = new ArrayIterable<Double>(predictedsArray);
    public static final Iterable<String> actualsSequence = new ArrayIterable<String>(actualsArray);
    public static final List<Double> predictedsList = Collections.unmodifiableList(Arrays.asList(predictedsArray));
    public static final List<String> actualsList = Collections.unmodifiableList(Arrays.asList(actualsArray));

    // An object different than any of the labels but that will compare equal
    public static final String positiveLabel = new String("1");

    // Correct counts based on the above data
    public static final int[] posCounts = {0, 1, 1, 1, 2, 2, 2, 3, 4, 4, 5, 6};
    public static final int[] negCounts = {0, 0, 1, 2, 2, 3, 4, 4, 4, 5, 5, 5};

    // Object under test
    CurveData.Builder<Double, String> builder;

    @Before public void setUp() {
        builder = new CurveData.Builder<Double, String>();
    }

    /** Tests {@link CurveData.Builder.rankedLabels(Iterable)}. */
    @Test public void testBuildWithRankedLabels() {
        // Check sequence instantiation
        builder.rankedLabels(rankedLabelsSequence);
        assertNotSame(rankedLabelsSequence, builder.rankedLabels);
        assertIterablesEqual(rankedLabelsSequence, builder.rankedLabels);

        // Check list reuse
        builder.rankedLabels(rankedLabelsList);
        assertSame(rankedLabelsList, builder.rankedLabels);

        // Check build
        CurveData curve = builder.positiveLabel(positiveLabel).build();
        assertArrayEquals(posCounts, curve.truePositiveCounts);
        assertArrayEquals(negCounts, curve.falsePositiveCounts);
    }

    //@Test public void testBuildWithRankedLabelsWeights() {
    //}

    /**
     * Tests {@link CurveData.Builder.predicteds(Iterable)} and {@link
     * CurveData.Builder.predicteds(Iterable)}.
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
        CurveData curve = builder.positiveLabel(positiveLabel).build();
        assertArrayEquals(posCounts, curve.truePositiveCounts);
        assertArrayEquals(negCounts, curve.falsePositiveCounts);
    }

    //@Test public void testBuildWithPredictedsActualsWeights() {
    //}

    //@Test public void testComparator() {
    //}

    @Test(expected=IllegalStateException.class)
    public void testInvalidStateEmpty() {
        builder.build();
    }

    @Test(expected=IllegalStateException.class)
    public void testInvalidStateNoLabel() {
        builder.predicteds(predictedsList).actuals(actualsList).build();
    }

    @Test(expected=IllegalStateException.class)
    public void testInvalidStateNoActuals() {
        builder.predicteds(predictedsList).positiveLabel(positiveLabel).build();
    }

    @Test(expected=IllegalStateException.class)
    public void testInvalidStateNoPredicteds() {
        builder.actuals(actualsList).positiveLabel(positiveLabel).build();
    }

    @Test(expected=IllegalStateException.class)
    public void testInvalidStatePASize() {
        List<Double> predicteds = Collections.emptyList();
        builder.predicteds(predicteds).actuals(actualsList)
            .positiveLabel(positiveLabel).build();
    }

    @Test(expected=IllegalStateException.class)
    public void testInvalidStatePAWSize() {
        List<Double> weights = Collections.emptyList();
        builder.predicteds(predictedsList).actuals(actualsList).weights(weights)
            .positiveLabel(positiveLabel).build();
    }

    @Test(expected=IllegalStateException.class)
    public void testInvalidStateRLWSize() {
        List<Double> weights = Collections.emptyList();
        builder.rankedLabels(rankedLabelsList).weights(weights)
            .positiveLabel(positiveLabel).build();
    }

    /** Tests {@link CurveData.Builder.instantiateSequence(Iterable)}. */
    @Test public void instantiateSequence() {
        Random random = new Random();
        int sequenceSize = random.nextInt(31);
        Long[] array = new Long[sequenceSize];
        for (int sequenceIndex = 0; sequenceIndex < sequenceSize; sequenceIndex++) {
            array[sequenceIndex] = Long.valueOf(random.nextLong());
        }
        Iterable<Long> sequence = new ArrayIterable<Long>(array);
        List<Long> list = CurveData.Builder.instantiateSequence(sequence);
        assertNotSame(sequence, list);
        assertIterablesEqual(sequence, list);
    }

    //@Test public void testTupleScoreReverseComparator() {
    //}


    ////////////////////////////////////////


    public static class ArrayIterator<E> implements Iterator<E> {
        private E[] array;
        private int index;
        public ArrayIterator(E[] array) {
            this.array = array;
            index = 0;
        }
        public boolean hasNext() {
            return index < array.length;
        }
        public E next() {
            return array[index++];
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static class ArrayIterable<E> implements Iterable<E> {
        private E[] array;
        public ArrayIterable(E[] array) {
            this.array = array;
        }
        public Iterator<E> iterator() {
            return new ArrayIterator<E>(array);
        }
    }

    public static <E> void assertIterablesEqual(Iterable<E> expecteds, Iterable<E> actuals) {
        int index = 0;
        E expected;
        E actual;
        Iterator<E> iterExpecteds = expecteds.iterator();
        Iterator<E> iterActuals = actuals.iterator();
        while (iterExpecteds.hasNext() && iterActuals.hasNext()) {
            expected = iterExpecteds.next();
            actual = iterActuals.next();
            if ((expected == null && actual != null) || (expected != null && !expected.equals(actual))) {
                fail("Iterables first differ at index " + index + ": expected <" + expected + "> but was <" + actual + ">.");
            }
            index++;
        }
        if (iterExpecteds.hasNext() != iterActuals.hasNext()) {
            if (iterExpecteds.hasNext()) {
                fail("Iterable 'actuals' has fewer elements than expected.");
            } else {
                fail("Iterable 'actuals' has more elements than expected.");
            }
        }
   }
}
