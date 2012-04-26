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

    /** Direct constructor. */
    CurveData(int[] truePositiveCounts, int[] falsePositiveCounts) {
	this.truePositiveCounts = truePositiveCounts;
	this.falsePositiveCounts = falsePositiveCounts;
	totalPositives = truePositiveCounts[truePositiveCounts.length - 1];
	totalNegatives = falsePositiveCounts[falsePositiveCounts.length - 1];
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
        buildCounts(rankedLabels, positiveLabel);
    }

    /**
     * Calls {@link #CurveData(int[], int)} with positiveLabel=1 (the
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
     * @param positiveLabel See {@link #CurveData(int[], int)}.
     */
    void buildCounts(int[] rankedLabels, int positiveLabel) {
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
    public double rocArea() {
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
    public double prArea(double minimumRecall, double maximumRecall) {
	throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Generate (x,y) points for a ROC curve.  Interpolation in ROC
     * space is linear so the ROC curve can be plotted by simply
     * connecting these points with lines.
     *
     * @return An array of two-element arrays.  Each two-element array
     * is a (x,y) point.  Points are sorted by ascending x value with
     * ties broken by ascending y value.
     */
    public double[][] rocPoints() {
	double[][] points = new double[truePositiveCounts.length][2];
	double totPos = (double) totalPositives;
	double totNeg = (double) totalNegatives;
	for (int pointIndex = 0; pointIndex < points.length; pointIndex++) {
	    points[pointIndex][0] = (double) falsePositiveCounts[pointIndex] / totNeg;  // FPR on x-axis
	    points[pointIndex][1] = (double) truePositiveCounts[pointIndex] / totPos;  // TPR on y-axis
	}
	return points;
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
    public double[][] prPoints(int numberOfSamples) {
	throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Directly from
     * http://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain.
     *
     * Computes the cross product of vectors OA and OB, that is, the
     * z-component of their three-dimensional cross product.  (A and B
     * are two-dimensional vectors and O is their common origin.)  The
     * cross product is positive if vector OAB turns counter-clockwise,
     * negative if clockwise, and zero if collinear.
     *
     * This can also be more intuitively understood in the planar case
     * as comparing the slopes/directions of the two vectors.  It
     * essentially computes (direction(OB) - direction(OA)).  This
     * understanding is derived as follows:
     * <pre>
     * direction(OB) = (B.y - O.y) / (B.x - O.x)
     * direction(OA) = (A.y - O.y) / (A.x - O.x)
     * d(OB) - d(OA) = ((B.y - O.y) / (B.x - O.x)) - ((A.y - O.y) / (A.x - O.x))
     *               = ((B.y - O.y) * (A.x - O.x) - (A.y - O.y) * (B.x - O.x)) / ((B.x - O.x) * (A.x - O.x))
     * </pre>
     * Then throw away the denominator because it doesn't affect the
     * comparison:
     * <pre>
     * (B.y - O.y) * (A.x - O.x) - (A.y - O.y) * (B.x - O.x)
     * </pre>
     *
     * We only need integer vectors for ROC/PR curves so keep everything
     * integer calculations.
     *
     * @param ox The x component of vector O.
     * @param oy The y component of vector O.
     * @param ax The x component of vector A.
     * @param ay The y component of vector A.
     * @param bx The x component of vector B.
     * @param by The y component of vector B.
     * @return The cross product of the given vectors, OAxOB.
     */
    static int vectorCrossProduct(int ox, int oy, int ax, int ay, int bx, int by) {
	return (ax - ox) * (by - oy) - (ay - oy) * (bx - ox);
    }

    /**
     * Finds the convex hull of a list of integer points.  The points
     * should already be sorted in increasing x order (ties broken by
     * increasing y order) which is already the case for curve data
     * counts.  Keeping separate arrays of x and y coordinates allows
     * for easy conversion to/from negative and positive counts.
     *
     * Use Andrew's monotone chain convex hull algorithm (except run
     * clockwise and only compute the upper hull).  The points are
     * already sorted and we already know the most extreme
     * lower-left point so the time complexity is O(n).
     *
     * http://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain.
     *
     * @param xCoords The x coordinates of the points.
     * @param yCoords The y coordinates of the points.
     * @return A two-element array where the first element is an array
     * of the x coordinates of the convex hull points and the second
     * element is an array of the y coordinates of the convex hull
     * points.  The convex hull points are an order-preserved sublist of
     * the given points.
     */
    static int[][] convexHullPoints(int[] xCoords, int[] yCoords) {
	// Point O (origin) is the second-to-last point in the hull.
	// Point A is the last point in the hull.  Point B is the
	// current point from the curve under consideration.

	int[] hullXCoords = new int[xCoords.length];
	int[] hullYCoords = new int[xCoords.length];
	int numberHullPoints = 0;
	for (int pointIndex = 0; pointIndex < xCoords.length; pointIndex++) {
	    // Remove points (A) from the hull that lie under vector OB.
	    // When OAxOB >= 0, OAB makes a left (counter-clockwise)
	    // turn so drop A.  This is opposite the (OAxOB <= 0) stated
	    // in the algorithm because here it is running clockwise
	    // rather than counter-clockwise.  The equals causes
	    // collinear points to be dropped as well.
	    while (numberHullPoints >= 2 &&
		   CurveData.vectorCrossProduct(
						hullXCoords[numberHullPoints - 2],
						hullYCoords[numberHullPoints - 2],
						hullXCoords[numberHullPoints - 1],
						hullYCoords[numberHullPoints - 1],
						xCoords[pointIndex],
						yCoords[pointIndex])
		   >= 0) {
		numberHullPoints--;
	    }
	    // OAB is now convex, so add B to the hull
	    hullXCoords[numberHullPoints] = xCoords[pointIndex];
	    hullYCoords[numberHullPoints] = yCoords[pointIndex];
	    numberHullPoints++;
	}
	// Downsize arrays
	int[] newHullXCoords = new int[numberHullPoints];
	int[] newHullYCoords = new int[numberHullPoints];
	System.arraycopy(hullXCoords, 0, newHullXCoords, 0, numberHullPoints);
	System.arraycopy(hullYCoords, 0, newHullYCoords, 0, numberHullPoints);
	return new int[][]{newHullXCoords, newHullYCoords};
    }

    /**
     * Creates a new curve containing only the convex hull of this
     * curve.
     *
     * @return A new curve, the convex hull of this curve.
     */
    public CurveData convexHull() {
	// Calculate the convex hull from the points defined by the
	// counts.  The convex hull points are also in terms of counts.
	// These are the new counts.
	int[][] hullPoints = convexHullPoints(falsePositiveCounts,  // FPR on x-axis
					      truePositiveCounts);  // TPR on y-axis
	return new CurveData(hullPoints[1], hullPoints[0]);
    }
}
