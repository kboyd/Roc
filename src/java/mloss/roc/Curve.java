/*
 * Copyright (c) 2012 Roc Project.  See LICENSE.txt for details.
 */

package mloss.roc;


/**
 * Conceptually a curve is an ordered set of confusion matrices, one for
 * each element of a ranking plus a zero one to start.
 * TODO
 */
class Curve {

    protected int[] truePositiveCounts;
    protected int totalPositives;
    protected int totalNegatives;

    /**
     * TODO
     */
    public Curve(int[] rankedLabels) {
        buildCounts(rankedLabels);
    }

    /**
     * TODO
     */
    void buildCounts(int[] rankedLabels) {
        // Allocate space for n + 1 points.  There is one point in
        // between each ranked label (n - 1) and two points for start
        // and end.
        truePositiveCounts = new int[rankedLabels.length + 1];

        // Initial values
        truePositiveCounts[0] = 0;
        totalPositives = 0;
        totalNegatives = 0;

        // Calculate the individual confusion matrices
        for (int labelIndex = 0; labelIndex < rankedLabels.length; labelIndex++) {
            if (rankedLabels[labelIndex] == 1) {
                totalPositives++;
            } else {
                totalNegatives++;
            }
            truePositiveCounts[labelIndex + 1] = totalPositives;
        }
    }

    /**
     * TODO
     */
    int[] confusionMatrix(int rankNumber) {
        int truePositives = truePositiveCounts[rankNumber];
        int falsePositives = rankNumber - truePositives;
        int falseNegatives = totalPositives - truePositives;
        int trueNegatives = totalNegatives - falsePositives;
        return new int[] {truePositives, falsePositives, falseNegatives, trueNegatives};
    }

    double[] point(int rankNumber) {
        // TODO
        return new double[2];
    }

    /**
     * TODO
     */
    double area() {
        // TODO
        return 0.0;
    }
}
