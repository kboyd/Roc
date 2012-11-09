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


public class CurveBuilderPrimitives extends CurveBuilder<Double, Integer> {

    public CurveBuilderPrimitives() {}

    public CurveBuilderPrimitives rankedLabels(int[] labels) {
        rankedLabels = CurveBuilderPrimitives.primitiveArrayToList(labels);
        return this;
    }

    public CurveBuilderPrimitives predicteds(double[] predicteds) {
        this.predicteds = CurveBuilderPrimitives.primitiveArrayToList(predicteds);
        return this;
    }

    public CurveBuilderPrimitives actuals(int[] actuals) {
        this.actuals = CurveBuilderPrimitives.primitiveArrayToList(actuals);
        return this;
    }

    public CurveBuilderPrimitives weights(double[] weights) {
        this.weights = CurveBuilderPrimitives.primitiveArrayToList(weights);
        return this;
    }

    public CurveBuilderPrimitives positiveLabel(int label) {
        positiveLabel = Integer.valueOf(label);
        return this;
    }

    public static List<Double> primitiveArrayToList(double[] array) {
        List<Double> list = new ArrayList<Double>(array.length);
        for (double element : array) {
            list.add(Double.valueOf(element));
        }
        return list;
    }

    public static List<Integer> primitiveArrayToList(int[] array) {
        List<Integer> list = new ArrayList<Integer>(array.length);
        for (int element : array) {
            list.add(Integer.valueOf(element));
        }
        return list;
    }
}
