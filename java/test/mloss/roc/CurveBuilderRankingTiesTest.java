package mloss.roc;

import static org.junit.Assert.*;
import org.junit.Test;

import mloss.roc.util.IterableArray;


/** Tests {@link Curve.Builder}. */
public class CurveBuilderRankingTiesTest {

    /** Tests {@link Curve.Builder} when predicteds contains ties. */
    @Test public void testRankingTiesCollection() {
        /*
          Predicted Actual
          3         1
          3         1
          3         1
          3         0
          2         1
          2         0
          1         1
          1         0
        */
        // Predicted and actuals with ties in predicteds
        Integer[] predictedsArray = {
            3, 1, 1, 2, 3, 3, 2, 3
        };
        Integer[] actualsArray = {
            0, 1, 0, 1, 1, 1, 0, 1
        };
        Iterable<Integer> predictedsSequence =
            new IterableArray<Integer>(predictedsArray);
        Iterable<Integer> actualsSequence =
            new IterableArray<Integer>(actualsArray);

        // Positive label
        Integer positiveLabel = new Integer(1);

        // Correct counts based on above data
        int[] posCounts = {0, 3, 4, 5 };
        int[] negCounts = {0, 1, 2, 3 };

        // Object under test
        Curve.Builder<Integer, Integer> builder =
            new Curve.Builder<Integer, Integer>();

        // Construct curve
        Curve curve = builder.predicteds(predictedsSequence).actuals(actualsSequence)
            .positiveLabel(positiveLabel).build();

        assertArrayEquals(posCounts, curve.truePositiveCounts);
        assertArrayEquals(negCounts, curve.falsePositiveCounts);
    }

    /** Tests {@link Curve.PrimitiveBuilder} when predicteds contains
     * ties.
     */
    @Test public void testRankingTiesPrimitive() {
        // Use predicteds that are exactly representable in doubles to
        // avoid floating point comparisons for this test.
        /*
          Predicted Actual
          3.0       1
          3.0       1
          3.0       1
          3.0       0
          2.0       1
          2.0       0
          1.0       1
          1.0       0
        */

        // Predicted and actuals with ties
        double predicteds[] = { 3.0, 1.0, 1.0, 2.0, 3.0, 3.0, 2.0, 3.0 };
        int actuals[] = { 0, 1, 0, 1, 1, 1, 0, 1 };

        int positiveLabel = 1;

        // Correct counts based on above data
        int[] posCounts = {0, 3, 4, 5 };
        int[] negCounts = {0, 1, 2, 3 };

        Curve.PrimitivesBuilder builder = new Curve.PrimitivesBuilder();

        // Construct curve
        Curve curve = builder.predicteds(predicteds).actuals(actuals)
            .positiveLabel(positiveLabel).build();

        assertArrayEquals(posCounts, curve.truePositiveCounts);
        assertArrayEquals(negCounts, curve.falsePositiveCounts);
    }
}
