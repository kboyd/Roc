/*
 * Copyright (c) 2012 Roc Project.  See LICENSE.txt for details.
 */

package mloss.roc;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * Builds CurveData objects in multiple, flexible ways.  To build a
 * CurveData from primitives see {@link CurveBuilderPrimitives}.
 *
 * Fundamentally, a curve is built from a ranking of labels and a label
 * to consider positive (see {@link CurveData#CurveData(List<T>, T)}).
 * However, curves are often constructed from lists of predictions
 * (scores) and the corresponding actual labels.  In this case, the
 * actual labels are ranked by sorting the scores in descending order.
 *
 * Therefore, in order to build a curve one must provide (1) a ranking
 * of labels or (2) corresponding lists of predictions and actual
 * labels.  A positive label must also be provided.  A list of weights
 * may be given.  The list must correspond to the lists of predicteds
 * and actuals (if given) or correspond to the list of ranked labels.  A
 * comparator may be given which will be used for sorting the scores.
 * Otherwise the natural ordering of the scores will be used.
 *
 * <ul>
 * <li>All inputs will be left unmodified.</li>
 * <li>Iterables will be instantiated as lists unless they are already
 * lists.</li>
 * <li>If providing a comparator, provide one for ascending order as all
 * comparators are reversed internally.</li>
 * <li>This builder works for multiple creations.  Just keep calling
 * build.</li>
 * </ul>
 *
 * @param <TScore> Type of score/prediction
 * @param <TLabel> Type of label
 */
public class CurveBuilder<TScore extends Comparable<? super TScore>, TLabel> {
    /* This is a fluent interface
     * (en.wikipedia.org/wiki/Fluent_interface) for building curves.
     * The point is to support multiple and flexible ways of
     * constructing curves without writing dozens of methods to handle
     * all the combinations of parameters.  See "Effective Java" items
     * 1, 2 (and the rest of Chapter 2).
     *
     * This implementation uses Lists instead of Collections because
     * collections are not necessarily ordered and ranking requires
     * order.
     */

    // TODO accept a comparator for ranking

    /** Basic container to hold a score, label, and weight. */
    private class Tuple {
        /* While you generally want a static inner class, this one can't
         * be static because it needs to "inherit" the type parameters
         * from the outer class.
         */

        public TScore score;
        public TLabel label;
        public Double weight;

        public Tuple(TScore score, TLabel label, Double weight) {
            this.score = score;
            this.label = label;
            this.weight = weight;
        }

        public Tuple(TScore score, TLabel label) {
            this.score = score;
            this.label = label;
        }
    }

    // The following are default access to allow subclassing within this
    // package.  All the lists should be the same length and kept in the
    // same order.
    List<TLabel> rankedLabels = null;
    List<TScore> predicteds = null;
    List<TLabel> actuals = null;
    List<Double> weights = null;
    TLabel positiveLabel = null;

    /** No-op constructor. */
    public CurveBuilder() {}

    /**
     * Specifies a sequence of labels that have already been ranked from
     * most-believed positive to most-believed negative (the
     * belief/score, having already been used, is not given).  Unless
     * labels are integers, a positive label must also be specified.
     * The iterable is instantiated as a list if necessary.
     *
     * @param labels A sequence of labels ranked from most positive to
     * most negative.
     * @return This builder
     */
    public CurveBuilder rankedLabels(Iterable<TLabel> labels) {
        if (labels instanceof List) {
            rankedLabels = (List<TLabel>) labels;
        } else {
            rankedLabels = CurveBuilder.instantiateSequence(labels);
        }
        return this;
    }

    /**
     * Specifies a sequence of scores or predictions that reflect
     * beliefs in how likely corresponding labels are positive.  Must be
     * specified in combination with actual labels.  The scores will be
     * used to rank the actual labels.  The iterable is instantiated as
     * a list if necessary.
     *
     * @param predicteds A sequence of scores
     * @return This builder
     */
    public CurveBuilder predicteds(Iterable<TScore> predicteds) {
        if (predicteds instanceof List) {
            this.predicteds = (List<TScore>) predicteds;
        } else {
            this.predicteds = CurveBuilder.instantiateSequence(predicteds);
        }
        return this;
    }

    /**
     * Specifies a sequence of actual labels that correspond to the
     * predictions.  Must be specified in combination with predictions.
     * The iterable is instantiated as a list if necessary.
     *
     * @param actuals A sequence of labels
     * @return This builder
     */
    public CurveBuilder actuals(Iterable<TLabel> actuals) {
        if (actuals instanceof List) {
            this.actuals = (List<TLabel>) actuals;
        } else {
            this.actuals = CurveBuilder.instantiateSequence(actuals);
        }
        return this;
    }

    /**
     * Specifies a sequence of weights that correspond to the actual
     * labels.  Completely optional.  The iterable is instantiated as a
     * list if necessary.
     *
     * @param weights A sequence of doubles
     * @return This builder
     */
    public CurveBuilder weights(Iterable<Double> weights) {
        if (weights instanceof List) {
            this.weights = (List<Double>) weights;
        } else {
            this.weights = CurveBuilder.instantiateSequence(weights);
        }
        return this;
    }

    /**
     * Specifies the object to use as a positive label.
     *
     * @param label The positive label
     * @return This builder
     */
    public CurveBuilder positiveLabel(TLabel label) {
        positiveLabel = label;
        return this;
    }

    /**
     * Does the work of checking for valid builder state.  To be called
     * by {@link #build()} before building.
     */
    private void checkValidBuilderState() {
        // Check for correct builder state
        if (positiveLabel == null) {
            throw new IllegalStateException("A positive label must be specified.");
        }
        if (rankedLabels == null) {
            if (predicteds == null || actuals == null) {
                throw new IllegalStateException("Both 'predicteds' and 'actuals' must be specified unless 'rankedLabels' is specified.");
            }
            if (predicteds.size() != actuals.size()) {
                throw new IllegalStateException("The sizes of 'predicteds' and 'actuals' must agree.");
            }
            if (weights != null && weights.size() != predicteds.size()) {
                throw new IllegalStateException("The size of 'weights' must agree with those of 'predicteds' and 'actuals'.");
            }
        }
        // Builder state OK
    }

    /**
     * Builds a curve from parameters that have been specified up to
     * this point.  Ranks the labels by the scores if necessary.
     *
     * @return A new curve
     * @throws IllegalStateException if the builder is not in a valid
     * state to construct a curve
     */
    public CurveData build() {
        // Check if it is OK to proceed (throws exception if not)
        checkValidBuilderState();

        // Create a list of ranked labels if not already given
        if (rankedLabels == null) {
            // Rank actuals by predicteds using a stable sort.  First
            // populate a sortable list.
            List<Tuple> sorted = new ArrayList<Tuple>(predicteds.size());
            Iterator<TScore> iterPredicteds = predicteds.iterator();
            Iterator<TLabel> iterActuals = actuals.iterator();
            // Include the weights in the sort if not null (else branch)
            if (weights == null) {
                while (iterPredicteds.hasNext() && iterActuals.hasNext()) {
                    sorted.add(new Tuple(iterPredicteds.next(), iterActuals.next()));
                }
            } else {
                Iterator<Double> iterWeights = weights.iterator();
                while (iterPredicteds.hasNext() && iterActuals.hasNext() && iterWeights.hasNext()) {
                    sorted.add(new Tuple(iterPredicteds.next(), iterActuals.next(), iterWeights.next()));
                }
            }
            // Sort in reverse order to make a ranking
            Collections.sort(sorted, null);  // TODO need comparator
            rankedLabels = new ArrayList<TLabel>(sorted.size());
            for (Tuple tuple : sorted) {
                rankedLabels.add(tuple.label);
            }
            // Make sure the weights are in the ranked order too
            if (weights != null) {
                // Create a new list so that the original one is left unmodified
                weights = new ArrayList<Double>(sorted.size());
                for (Tuple tuple : sorted) {
                    weights.add(tuple.weight);
                }
            }
        }

        // TODO handle weights, comparator
        return new CurveData(rankedLabels, positiveLabel);
    }

    /**
     * Instantiates a sequence as a list.
     *
     * @param sequence Any sequence
     * @return A list containing the same elements as the sequence in
     * the same order.
     */
    public static <E> List<E> instantiateSequence(Iterable<E> sequence) {
        List<E> list = new LinkedList<E>();
        for (E element : sequence) {
            list.add(element);
        }
        return list;
    }
}
