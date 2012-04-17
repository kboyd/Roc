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
public class CurveData {

    protected int[] truePositiveCounts;
    protected int[] falsePositiveCounts;
    protected int totalPositives;
    protected int totalNegatives;

    /**
     * Value for negative label.
     */
    public static final int NEG = 0;
    
    /**
     * Value for positive label.
     */
    public static final int POS = 1;

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
            if (rankedLabels[labelIndex] == POS) {
                totalPositives++;
            } else if (rankedLabels[labelIndex] == NEG) {
                totalNegatives++;
            } else {
		throw new IllegalArgumentException("Invalid label, neither negative (" + NEG + ") nor positive (" + POS + ")");
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
     * Calculate area under ROC curve.
     * @return area under ROC curve
     */
    public double calculateRocArea() {
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

    /**
     * Calculate area under PR curve for recall between minimum and
     * maximum recall. Uses interpolation from Davis and Goadrich
     * 2006.
     *
     * @param minimumRecall lowest recall that counts towards area
     * @param maximumRecall highest recall that counts towards area
     * @return area under PR curve
     */
    public double calculatePrArea(double minimumRecall, double maximumRecall) {
	throw new NotImplementedException();
    }


    /**
     * Generate (x,y) points for ROC curve. Linear interpolation
     * between ROC points so simply draw lines between the points.
     *
     * TODO - return double[][] or an object, CurvePoints or something?
     *
     * @return [i][0] is x-value (fpr) of ith point, [i][1] is y-value
     * (tpr) of ith point, points are sorted by ascending x-value
     */
    public double[][] plotRoc() {
	throw new NotImplementedException();
    }

    /**
     * Generate (x,y) points for PR curve. Linear interpolation is NOT
     * correct for PR curves, instead interpolation is done by
     * FORMULA.
     * 
     * TODO - probably need a plotPr() that doesn't require specifying
     * the numberOfSamples, what should be the default though? 100?
     * 1000?
     *
     * @param numberOfSamples number of evenly spaced samples to take
     * of the PR curve, with a sufficiently large value using a line
     * between points is a reasonable approximation of the correct
     * interpolation
     * @return [i][0] is the x-value (recall) of the ith point, [i][1]
     * is the y-value (precision) of the ith points, points are sorted
     * by ascending x-value
     */
    public double[][] plotPr(int numberOfSamples) {
	throw new NotImplementedException();
    }
}
