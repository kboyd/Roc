/*
 * Copyright (c) 2012 Roc Project.  See LICENSE.txt for details.
 */

package mloss.roc;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import mloss.roc.CurveData;


/*
 * This is a fluent interface (en.wikipedia.org/wiki/Fluent_interface)
 * for building curves.  The point is to support multiple and flexible
 * ways of constructing curves without writing dozens of methods to
 * handle all the combinations of parameters.  See "Effective Java"
 * items 1, 2 (and the rest of Chapter 2).
 */


/**
 * requires ordered collections (lists), so must instantiate iterables
 * labels will need to be ranked (sorted in descending order) by predicteds
 * aim: work for multiple creations
 * all inputs should be left unmodified
 */
public class CurveBuilder<TScore extends Comparable<? super TScore>, TLabel> {
    // TODO accept a comparator for ranking

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

    // Default access to allow subclassing within this package
    List<TLabel> rankedLabels = null;
    List<TScore> predicteds = null;
    List<TLabel> actuals = null;
    List<Double> weights = null;
    TLabel positiveLabel = null;

    public CurveBuilder() {}

    public CurveBuilder rankedLabels(Iterable<TLabel> labels) {
        if (labels instanceof List) {
            rankedLabels = (List<TLabel>) labels;
        } else {
            rankedLabels = CurveBuilder.instantiateSequence(labels);
        }
        return this;
    }

    public CurveBuilder predicteds(Iterable<TScore> predicteds) {
        if (predicteds instanceof List) {
            this.predicteds = (List<TScore>) predicteds;
        } else {
            this.predicteds = CurveBuilder.instantiateSequence(predicteds);
        }
        return this;
    }

    public CurveBuilder actuals(Iterable<TLabel> actuals) {
        if (actuals instanceof List) {
            this.actuals = (List<TLabel>) actuals;
        } else {
            this.actuals = CurveBuilder.instantiateSequence(actuals);
        }
        return this;
    }

    public CurveBuilder weights(Iterable<Double> weights) {
        if (weights instanceof List) {
            this.weights = (List<Double>) weights;
        } else {
            this.weights = CurveBuilder.instantiateSequence(weights);
        }
        return this;
    }

    public CurveBuilder positiveLabel(TLabel label) {
        positiveLabel = label;
        return this;
    }

    public CurveData build() {
        // TODO handle label
        // TODO consolidate checking
        // Create a list of ranked labels if not already given
        if (rankedLabels == null) {
            // Check for correct builder state
            if (predicteds == null || actuals == null) {
                throw new IllegalStateException("Both 'predicteds' and 'actuals' must be specified unless 'rankedLabels' is specified.");
            }
            if (predicteds.size() != actuals.size()) {
                throw new IllegalStateException("The sizes of 'predicteds' and 'actuals' must agree.");
            }
            if (weights != null && weights.size() != predicteds.size()) {
                throw new IllegalStateException("The size of 'weights' must agree with those of 'predicteds' and 'actuals'.");
            }
            // Builder state OK

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

        // Call the appropriate curve constructor and return a new object
        // TODO handle positive label
        return new CurveData(rankedLabels, positiveLabel);
    }

    public static <E> List<E> instantiateSequence(Iterable<E> sequence) {
        List<E> collection = new LinkedList<E>();
        for (E element : sequence) {
            collection.add(element);
        }
        return collection;
    }
}
