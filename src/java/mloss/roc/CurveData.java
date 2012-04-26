/*
 * Copyright (c) 2012 Roc Project.  See LICENSE.txt for details.
 */

package mloss.roc;


/**
 * This class is a binary classification result analysis suitable for
 * producing ROC and PR curves.
 *
 * A binary (positive-negative) classifier assigns some number (score or
 * probability) to each data point (example) in a data set.  The
 * assigned number reflects the classifier's belief that the example is
 * truly a positive example (as opposed to a negative example).  Each
 * example also has a true label, its class.  The scores from the
 * classifier and the true labels only become significant for producing
 * ROC and PR curves once they are ranked from most-believed positive to
 * most-believed negative.  It is then the list of true labels in this
 * ranked order that is analyzed to produce ROC and PR curves.
 *
 * <h3>Technical Details</h3>
 *
 * Conceptually a ROC or PR curve is a list of confusion matrices, one
 * for each element of a ranking plus a zero one to start.  This
 * representation contains all the information needed to compute
 * everything about ROC and PR curves.  If you have n points in your
 * ranking, you need n + 1 confusion matrices, one in response to each
 * element of the ranking and a "null" (zero) one to start.  A confusion
 * matrix is a 2-by-2 table describing the classification result that
 * would occur if one chose the classification threshold after the
 * current element of the ranking.  A confusion matrix contains four
 * numbers: the number of true positives, the number of false positives,
 * the number of false negatives, and the number of true negatives.  See
 * the following table.
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

    // TODO javadocs on these
    protected int[] truePositiveCounts;
    protected int[] falsePositiveCounts;
    protected int totalPositives;
    protected int totalNegatives;
    protected int positiveLabel;

    /** Direct constructor. */
    CurveData(int[] truePositiveCounts, int[] falsePositiveCounts, int positiveLabel) {
	this.truePositiveCounts = truePositiveCounts;
	this.falsePositiveCounts = falsePositiveCounts;
	totalPositives = truePositiveCounts[truePositiveCounts.length - 1];
	totalNegatives = falsePositiveCounts[falsePositiveCounts.length - 1];
	this.positiveLabel = positiveLabel;
    }

    /**
     * Creates a classification result analysis suitable for producing
     * ROC and PR curves.
     *
     * @param rankedLabels A list containing the true label for each
     * example in the order of classified most likely positive to
     * classified most likely negative.  (The numbers used to rank the
     * labels are not part of the ranked labels.)
     * @param positiveLabel The label that will be considered positive.
     * All other labels are considered negative.  This allows for
     * handling multiple classes without having to rewrite all the
     * labels into some prespecified positive and negative signifiers.
     */
    public CurveData(int[] rankedLabels, int positiveLabel) {
	this.positiveLabel = positiveLabel;
        buildCounts(rankedLabels);
    }

    /**
     * Calls {@link CurveData(int[], int)} with positiveLabel=1 (the
     * default positive label).
     */
    public CurveData(int[] rankedLabels) {
	this(rankedLabels, 1);
    }

    /**
     * Counts and stores the numbers of correctly-classified positives
     * and negatives at each threshold level.
     *
     * @param rankedLabels A list containing the true label for each
     * example.  The labels are ordered (ranked) from most likely
     * positive to most likely negative.
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
            if (rankedLabels[labelIndex] == positiveLabel) {
                totalPositives++;
            } else {
                totalNegatives++;
	    }
            truePositiveCounts[labelIndex + 1] = totalPositives;
            falsePositiveCounts[labelIndex + 1] = totalNegatives;
        }
    }

    /**
     * Computes the confusion matrix at a particular classification
     * threshold.
     *
     * @param rankNumber The number of elements in the ranking to treat
     * as positive.  (The classification threshold is between the
     * rankNumber-th and the (rankNumber + 1)-th label in the ranking.)
     * @return A four-element array containing the numbers of true
     * positives, false positives, false negatives, and true negatives
     * ([TP, FP, FN, TN]).
     */
    public int[] confusionMatrix(int rankNumber) {
        int truePositives = truePositiveCounts[rankNumber];
        int falsePositives = falsePositiveCounts[rankNumber];
        int falseNegatives = totalPositives - truePositives;
        int trueNegatives = totalNegatives - falsePositives;
        return new int[] {truePositives, falsePositives, falseNegatives, trueNegatives};
    }

    /**
     * Computes the point on the ROC curve that corresponds to a
     * particular classification threshold.
     *
     * @param rankNumber The number of elements in the ranking to treat
     * as positive.
     * @return A two-element array containing the false positive rate
     * (x-axis) and the true positive rate (y-axis) ([FPR, TPR]).
     */
    public double[] rocPoint(int rankNumber) {
	double falsePositiveRatio = (double) falsePositiveCounts[rankNumber] / (double) totalNegatives;
	double truePositiveRatio = (double) truePositiveCounts[rankNumber] / (double) totalPositives;
        return new double[] {falsePositiveRatio, truePositiveRatio};
    }

    /**
     * @return The area under the ROC curve.
     */
    public double calculateRocArea() {
	// Calculate the area of each trapezoid formed by two successive
	// (non-vertical) ROC points and sum them all up.  Non-vertical:
	// there is only more area when the x-value (FPR) changes.

	// Formula:
	//   area += base * (height1 + height2) / 2.0
	// where
	//   base = (fp[i] - fp[i-1]) / totN
	//   height1 = tp[i-1] / totP
	//   height2 = tp[i] / totP
	// Combine and simplify
	//   area += ((fp[i] - fp[i-1]) * (tp[i-1] + tp[i])) / (2.0 * totP * totN)
	// then save denominator for last (move outside sum)
	//   area = (sum_i ((fp[i] - fp[i-1]) * (tp[i-1] + tp[i]))) / (2.0 * totP * totN)

	int countsArea = 0;
	for (int countIndex = 1; countIndex < truePositiveCounts.length; countIndex++) {
	    // Successive counts can never be less
	    if (falsePositiveCounts[countIndex] > falsePositiveCounts[countIndex - 1]) {
		countsArea += (falsePositiveCounts[countIndex] - falsePositiveCounts[countIndex - 1])
		    * (truePositiveCounts[countIndex - 1] + truePositiveCounts[countIndex]);
	    }
	}
        return (double) countsArea / (double) (2 * totalPositives * totalNegatives);
    }

    /**
     * Calculate area under PR curve for recall between minimum and
     * maximum recall. Uses interpolation from Davis and Goadrich
     * 2006 (and Goadrich and Shavlik 2005?).
     *
     * @param minimumRecall lowest recall that counts towards area
     * @param maximumRecall highest recall that counts towards area
     * @return area under PR curve
     */
    public double calculatePrArea(double minimumRecall, double maximumRecall) {
	throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Generate (x,y) points for ROC curve. Linear interpolation
     * between ROC points so simply draw lines between the points.
     *
     * TODO - should we return an object, CurvePoints or something?
     *
     * @return [i][0] is x-value (fpr) of ith point, [i][1] is y-value
     * (tpr) of ith point, points are sorted by ascending x-value
     */
    public double[][] plotRoc() {
	throw new UnsupportedOperationException("Not yet implemented");
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
	throw new UnsupportedOperationException("Not yet implemented");
    }
}
