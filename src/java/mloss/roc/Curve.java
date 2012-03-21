/*
 * Copyright (c) 2012 Roc Project.  See LICENSE.txt for details.
 */

package mloss.roc;


/**
 * Conceptually a curve is a list of confusion matrices, one for each
 * element of a ranking plus a zero one to start.  This representation
 * contains all the information needed to compute everything about ROC
 * and PR curves.  If you have n points in your ranking, you need n + 1
 * confusion matrices, one in response to each element of the ranking
 * and a "null" (zero) one to start.  A confusion matrix is a 2-by-2
 * table describing the classification result that would occur if one
 * chose the classification threshold "below" the current element of the
 * ranking.  A confusion matrix contains four numbers: the number of
 * true positives, the number of false positives, the number of false
 * negatives, and the number of true negatives.  See the following
 * table.
 *
 * <pre>
 *            |              Actual               |
 * Classified | Positive        | Negative        |
 * ------------------------------------------------
 * Positive   | True Positive   | False Positive  |
 * Negative   | False Negative  | True Negative   |
 * ------------------------------------------------
 *            | Total Positives | Total Negatives |
 * </pre>
 *
 * Although each confusion matrix involves four numbers, each can
 * actually be represented by two numbers plus two totals (shared among
 * all matrices): the number of true positives so far in the ranking,
 * the number of false positives so far in the ranking,
 * the total number of positives in the ranking, and the total
 * number of negatives in the ranking.  This is possible
 * because the following identities allow one to recover each confusion
 * matrix.
 *
 * <code>
 * true positives = (true positive counts list)[ranking index]
 * false positives = (false positive counts list)[ranking index]
 * false negatives = (total positives) - (true positives)
 * true negatives = (total negatives) - (false positives)
 * </code>
 */
class Curve {

    protected int[] truePositiveCounts;
    protected int[] falsePositiveCounts;
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
        // Allocate space for n + 1 points.  There is one point after
        // each element in the ranking and a zero one to start.
        truePositiveCounts = new int[rankedLabels.length + 1];
        falsePositiveCounts = new int[rankedLabels.length + 1];

        // Initial values
        truePositiveCounts[0] = 0;
        falsePositiveCounts[0] = 0;
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
            falsePositiveCounts[labelIndex + 1] = totalNegatives;
        }
    }

    /**
     * TODO
     */
    public int[] confusionMatrix(int rankNumber) {
        int truePositives = truePositiveCounts[rankNumber];
        int falsePositives = falsePositiveCounts[rankNumber];
        int falseNegatives = totalPositives - truePositives;
        int trueNegatives = totalNegatives - falsePositives;
        return new int[] {truePositives, falsePositives, falseNegatives, trueNegatives};
    }

    public double[] rocPoint(int rankNumber) {
	double falsePositiveRatio = (double) falsePositiveCounts[rankNumber] / (double) totalNegatives;
	double truePositiveRatio = (double) truePositiveCounts[rankNumber] / (double) totalPositives;
        return new double[] {falsePositiveRatio, truePositiveRatio};
    }

    /**
     * TODO
     */
    public double rocArea() {
	// TODO make work for trapezoids and convex hull
	// There is only a new rectangle when the x-value (FPR) changes
	double area = 0.0;
	for (int countIndex = 1; countIndex < truePositiveCounts.length; countIndex++) {
	    if (falsePositiveCounts[countIndex] > falsePositiveCounts[countIndex - 1]) {
		double base = (double) (falsePositiveCounts[countIndex] - falsePositiveCounts[countIndex - 1]) / (double) totalNegatives;
		double height = (double) truePositiveCounts[countIndex] / (double) totalPositives;
		area += base * height;
	    }
	}
        return area;
    }
}
