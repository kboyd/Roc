/*
 * Copyright (c) 2012 Roc Project.  See LICENSE.txt for details.
 */

package mloss.roc;


import java.util.ArrayList;
import java.util.List;


/**
 * The same as {@link CurveBuilder} except takes arrays of primitives as
 * input.  Scores must be doubles and labels must be integers.  (Labels
 * must compare exactly.)  All arrays of primitives are converted to
 * lists of number objects.
 */
public class CurveBuilderPrimitives extends CurveBuilder<Double, Integer> {

    /** No-op constructor. */
    public CurveBuilderPrimitives() {}

    /**
     * @param labels A sequence of labels ranked from most positive to
     * most negative.
     * @return This builder
     * @see CurveBuilder#rankedLabels(Iterable)
     */
    public CurveBuilderPrimitives rankedLabels(int[] labels) {
        rankedLabels = CurveBuilderPrimitives.primitiveArrayToList(labels);
        return this;
    }

    /**
     * @param predicteds A sequence of scores
     * @return This builder
     * @see CurveBuilder#predicteds(Iterable)
     */
    public CurveBuilderPrimitives predicteds(double[] predicteds) {
        this.predicteds = CurveBuilderPrimitives.primitiveArrayToList(predicteds);
        return this;
    }

    /**
     * @param actuals A sequence of labels
     * @return This builder
     * @see CurveBuilder#actuals(Iterable)
     */
    public CurveBuilderPrimitives actuals(int[] actuals) {
        this.actuals = CurveBuilderPrimitives.primitiveArrayToList(actuals);
        return this;
    }

    /**
     * @param weights A sequence of doubles
     * @return This builder
     * @see CurveBuilder#weights(Iterable)
     */
    public CurveBuilderPrimitives weights(double[] weights) {
        this.weights = CurveBuilderPrimitives.primitiveArrayToList(weights);
        return this;
    }

    /**
     * @param label The positive label
     * @return This builder
     * @see CurveBuilder#positiveLabel(Object)
     */
    public CurveBuilderPrimitives positiveLabel(int label) {
        positiveLabel = Integer.valueOf(label);
        return this;
    }

    /**
     * Converts a primitive array of doubles to a list of Doubles.
     *
     * @param array An array of doubles
     * @return A list of Doubles
     */
    public static List<Double> primitiveArrayToList(double[] array) {
        List<Double> list = new ArrayList<Double>(array.length);
        for (double element : array) {
            list.add(Double.valueOf(element));
        }
        return list;
    }

    /**
     * Converts a primitive array of integers to a list of Integers.
     *
     * @param array An array of ints
     * @return A list of Integers
     */
    public static List<Integer> primitiveArrayToList(int[] array) {
        List<Integer> list = new ArrayList<Integer>(array.length);
        for (int element : array) {
            list.add(Integer.valueOf(element));
        }
        return list;
    }
}
