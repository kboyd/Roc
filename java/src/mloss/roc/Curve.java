/*
 * Copyright (c) 2013 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


// TODO: handle case where classifier output is a weak ranking, e.g. class labels

/**
 * <p>This class is a binary classification result analysis suitable for
 * producing ROC and PR curves.  It represents a ranking from which ROC
 * and PR curves are computed.</p>
 *
 * <h3>Brief Background: Classification</h3>
 *
 * <p>A binary (positive-negative) classifier assigns some number (a
 * score or probability or class label) to each data point (example) in
 * a data set.  The assigned number reflects the classifier's belief
 * that the example is truly a positive example (as opposed to a
 * negative example).  Each example also has a true label, its class.
 * Therefore, to test a classifier, one uses it to classify unseen
 * examples (a test set) and compares those classification results with
 * the true labels.</p>
 *
 * <p>To classify new examples, one must first choose a threshold.
 * Then, all the examples with scores higher than the threshold are
 * classified positive and all other examples are classified negative.
 * In practice, considering only a single threshold is restrictive.  ROC
 * and PR curves consider performance at all possible thresholds.  To do
 * that, the scores are ranked from most-believed positive to
 * most-believed negative and then the list of true labels in the ranked
 * order is analyzed to produce ROC and PR curves.</p>
 *
 * <p>For more information consult the Wikipedia pages on <a
 * href="http://en.wikipedia.org/wiki/Binary_classification">binary
 * classification</a>, <a
 * href="http://en.wikipedia.org/wiki/Receiver_operating_characteristic">ROC
 * analysis</a>, <a
 * href="http://en.wikipedia.org/wiki/Precision_and_recall">PR
 * analysis</a>, and your favorite book on machine learning.</p>
 *
 * <h3>Usage</h3>
 *
 * <p>This is the central class for creating and working with ROC and PR
 * curves.</p>
 *
 * <p>To create a ROC or PR curve, one only needs the ranking of true
 * labels from most positive to most negative.  This is because the
 * scores become immaterial after ranking: the number of labels assigned
 * correctly can be counted for any threshold in the ranking, regardless
 * of any scores.  (After choosing a threshold between two labels, all
 * examples above the threshold are classified positive and all others
 * below are negative.)  Once the ranking is obtained, one can use it to
 * compute curves, areas, statistics, etc.  That is what this class
 * does.  It holds a ranking and uses it to compute curves and other
 * information.</p>
 *
 * <p>One can construct this class directly from a ranking of labels,
 * but it is more convenient to use a builder to compute the ranking and
 * construct this class.  There are two builders.  The regular {@link
 * Curve.Builder} accepts collections of objects and the {@link
 * Curve.PrimitivesBuilder} accepts arrays of primitive numbers.  (You
 * can use the class {@link mloss.roc.util.IterableArray} to treat
 * arrays of objects as iterable collections suitable for the regular
 * builder.)  You give a builder a list of scores (from your classifier)
 * and a corresponding list of labels and the builder does the rest.
 * See the <a href="{@docRoot}/overview-summary.html">overview</a> for a
 * code example.</p>
 *
 * <h3>Technical Details</h3>
 *
 * <p>Conceptually a ROC or PR curve is a list of confusion matrices,
 * one for each possible threshold of a ranking.  This representation
 * contains all the information needed to compute everything about ROC
 * and PR curves.  If you have n points in your ranking, you need n + 1
 * confusion matrices, one between each element of the ranking and one
 * on each of the ends.  A confusion matrix is a 2-by-2 table describing
 * the classification result that would occur if one chose the
 * classification threshold after the current element of the ranking.  A
 * confusion matrix contains four numbers: the number of true positives,
 * the number of false positives, the number of false negatives, and the
 * number of true negatives.  See the following table.</p>
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
 * <p>Although each confusion matrix involves four numbers, each can
 * actually be represented by two numbers plus two totals (shared among
 * all matrices): the number of true positives so far in the ranking,
 * the number of false positives (negatives) so far in the ranking, the
 * total number of positives in the ranking, and the total number of
 * negatives in the ranking.  This is possible because the following
 * identities allow one to recover each confusion matrix.</p>
 *
 * <pre><code>
 * true positives = (true positive counts list)[ranking index]
 * false positives = (false positive counts list)[ranking index]
 * false negatives = (total positives) - (true positives)
 * true negatives = (total negatives) - (false positives)
 * </code></pre>
 */
public class Curve {
    /* This class is implemented in terms of arrays of primitives.
     * Avoid converting to/from arrays of primitives and lists of
     * objects if possible.  Internally, this should not be a problem as
     * any anticipated conversions only concern construction and, hence,
     * are in the purvey of the builders, leaving this class as simple
     * as possible.  (There is just one case of equivalent code paths
     * for supporting arrays and collections which seems like a decent
     * trade-off for unnecessary overhead.)
     */

    /** The number of true positives at an index in the ranking. */
    int[] truePositiveCounts;

    /** The number of false positives at an index in the ranking. */
    int[] falsePositiveCounts;

    /** The total number of positive labels/examples. */
    int totalPositives;

    /** The total number of negative labels/examples. */
    int totalNegatives;

    /**
     * Initializes the fields of this class.
     *
     * @param rankingSize The length of the ranking of labels.
     */
    private void initFields(int rankingSize) {
        // Allocate space for n + 1 points.  There is one point after
        // each element in the ranking and a zero one to start.
        truePositiveCounts = new int[rankingSize + 1];
        falsePositiveCounts = new int[rankingSize + 1];

        // Initial values
        truePositiveCounts[0] = 0;
        falsePositiveCounts[0] = 0;
        totalPositives = 0;
        totalNegatives = 0;
    }

    /** Direct constructor, mainly for testing and internal use. */
    Curve(int[] truePositiveCounts, int[] falsePositiveCounts) {
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
    public Curve(int[] rankedLabels, int positiveLabel) {
        initFields(rankedLabels.length);
        buildCounts(rankedLabels, positiveLabel);
    }

    /**
     * Calls {@link #Curve(int[], int)} with positiveLabel=1 (the
     * default positive label for integers).
     */
    public Curve(int[] rankedLabels) {
        this(rankedLabels, 1);
    }

    /**
     * Creates a classification result analysis suitable for producing
     * ROC and PR curves.  This is the version to use for collections of
     * number objects.
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
    public <T> Curve(List<T> rankedLabels, T positiveLabel) {
        initFields(rankedLabels.size());
        buildCounts(rankedLabels, positiveLabel);
    }

    /**
     * Counts and stores the numbers of correctly-classified positives
     * and negatives at each threshold level.
     *
     * @param rankedLabels A list containing the true label for each
     * example.  The labels must already be ordered (ranked) from most
     * likely positive to most likely negative.
     * @param positiveLabel See {@link #Curve(int[], int)}.
     */
    void buildCounts(int[] rankedLabels, int positiveLabel) {
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
     * Generic collections version of {@link #buildCounts(int[], int)}.
     */
    <T> void buildCounts(Iterable<T> rankedLabels, T positiveLabel) {
        // Calculate the individual confusion matrices
        int labelIndex = 0;
        for (T label : rankedLabels) {
            if (label.equals(positiveLabel)) {
                totalPositives++;
            } else {
                totalNegatives++;
            }
            truePositiveCounts[labelIndex + 1] = totalPositives;
            falsePositiveCounts[labelIndex + 1] = totalNegatives;
            labelIndex++;
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
     * Generates (x,y)=(FPR,TPR) points for plotting a ROC curve with
     * connecting lines.  Interpolation in ROC space is linear so
     * connecting consecutive points with lines gives the exact ROC
     * curve.
     *
     * @return An array of two-element arrays.  Each two-element array
     * is a (x,y) point in ROC space.  Points are provided in plotting
     * order.
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
     * Computes the area under the ROC curve.  (The curve is defined by
     * {@link #rocPoints()}.)
     *
     * @return Area under the ROC curve
     */
    public double rocArea() {
        /* This implementation is in terms of the Mann-Whitney U
         * statistic rather than trapezoids.  Formula: auc-roc = u0 /
         * (n1 * n0).  The statistic for the negatives is the one that
         * corresponds to the area under the curve.  (I'm not sure why.)
         */
        double[] uStatistics = mannWhitneyU();
        return uStatistics[1] / (double) (totalPositives * totalNegatives);
    }

    /**
     * Computes recall at a particular classification threshold.
     *
     * @param rankNumber The number of elements in the ranking to treat
     * as positive.
     * @return Recall at the given threshold.
     */
    public double recall(int rankNumber) {
        // recall = tp / (tp + fn) = tp / #p
        return (double) truePositiveCounts[rankNumber] /
            (double) totalPositives;
    }

    /**
     * Computes precision at a particular classification threshold.
     *
     * @param rankNumber The number of elements in the ranking to treat
     * as positive.
     * @return Precision at the given threshold.
     */
    public double precision(int rankNumber) {
        // precision = tp / (tp + fp)
        // Precision uses the value of rank 1 as the value for rank 0
        if (rankNumber == 0) {
            rankNumber++;
        }
        return (double) truePositiveCounts[rankNumber] /
            (double) (truePositiveCounts[rankNumber] +
                      falsePositiveCounts[rankNumber]);
    }

    /**
     * Computes the point on the PR curve that corresponds to a
     * particular classification threshold.
     *
     * The precision is undefined (due to division by zero) for the
     * zeroth point.  We choose to handle this as a special case and
     * define the zeroth point as (0, y1) where y1 is the vertical value
     * (precision) of the point with rank 1.  That is, the zeroth point
     * extends the curve horizontally left from the first defined point
     * (rank 1) to the vertical axis.
     *
     * @param rankNumber The number of elements in the ranking to treat
     * as positive.
     * @return A two-element array containing the recall (x-axis) and
     * the precision (y-axis) ([recall, precision]).
     */
    public double[] prPoint(int rankNumber) {
        // (x/recall, y/precision)
        return new double[] {recall(rankNumber), precision(rankNumber)};
    }

    /**
     * Generates (x,y)=(recall,precision) points for plotting a PR curve
     * with connecting lines.  Interpolation in PR space is generally
     * not linear, so the returned points are the closest linear
     * underestimate of the actual PR curve.  Plot the PR curve by
     * connecting consecutive points with lines.
     *
     * Technically, a closer linear underestimate could be achieved by
     * introducing intermediate points (samples), but this method
     * achieves the closest underestimate without samples.
     *
     * See TODO:REFERENCE for more information.
     *
     * @return An array of two-element arrays.  Each two-element array
     * is a (x,y) point in PR space.  Points are provided in plotting
     * order.
     */
    public double[][] prPoints() {
        // As long as there are no ties (no thresholds where both true
        // and false positives increase), the raw PR points suffice and
        // can be linearly interpolated for an underestimate of the
        // actual curve.  In the case of a tie, linear interpolation
        // overestimates, which this method handles by creating a
        // lower-left bounding "box" that underestimates.  Each tie
        // requires introducing a single point, the lower-left corner of
        // the bounding "box".

        // Count the number of ties to know the number of extra points.
        int ties = 0;
        int prevIndex;
        for (int pointIndex = 1; pointIndex < truePositiveCounts.length; pointIndex++) {
            // A tie is when both counts increase between thresholds
            prevIndex = pointIndex - 1;
            if (truePositiveCounts[pointIndex] > truePositiveCounts[prevIndex] &&
                falsePositiveCounts[pointIndex] > falsePositiveCounts[prevIndex]) {
                ties++;
            }
        }

        // Calculate the PR points adding "lower-left" points for ties
        double[][] points = new double[truePositiveCounts.length + ties][2];
        double[] point = prPoint(0);
        // Add the zeroth point by hand
        points[0][0] = point[0];
        points[0][1] = point[1];
        // Add the remaining points
        int pointIndex = 1;
        for (int countIndex = 1; countIndex < truePositiveCounts.length; countIndex++) {
            // Get the current PR point
            point = prPoint(countIndex);
            // If this is a "tie" point, add a point to make a
            // lower-left bounding "box".  A tie is when both counts
            // increase between thresholds.
            prevIndex = countIndex - 1;
            if (truePositiveCounts[countIndex] > truePositiveCounts[prevIndex] &&
                falsePositiveCounts[countIndex] > falsePositiveCounts[prevIndex]) {
                // Add a "lower-left" point which has x/recall from the
                // previous point and y/precision from the current
                // point.
                points[pointIndex][0] = points[pointIndex - 1][0];
                points[pointIndex][1] = point[1];
                pointIndex++;
            }
            // Add in the current PR point
            points[pointIndex][0] = point[0];
            points[pointIndex][1] = point[1];
            pointIndex++;
        }
        return points;
    }

    /**
     * Calculates an estimate of the area under the PR curve.  (The
     * curve is defined by {@link #prPoints()}.)  The area is an
     * underestimate of the area under the actual PR curve.
     *
     * @return Area under the PR curve
     */
    public double prArea() {
        double area = 0.0;
        int posCount, prevPosCount;
        double base, height, prevHeight;
        for (int countIndex = 1; countIndex < truePositiveCounts.length; countIndex++) {
            // There are 3 cases:
            // 1. Positive count increased: trapezoid between current
            //    height and previous height.  (This case handles the
            //    first point and it just so happens both heights are
            //    the same.)
            // 2. Negative count increased: height decreased and there
            //    is no area
            // 3. Positive and negative counts increased: rectangle at
            //    current height (to lower-bound curve)

            // Only calculate an area if the positives have increased
            posCount = truePositiveCounts[countIndex];
            prevPosCount = truePositiveCounts[countIndex - 1];
            if (posCount > prevPosCount) {
                base = (double) (posCount - prevPosCount);
                height = precision(countIndex);
                if (falsePositiveCounts[countIndex] > // Neg counts
                    falsePositiveCounts[countIndex - 1]) {
                    // Rectangle
                    area += base * height;
                } else {
                    // Trapezoid
                    prevHeight = precision(countIndex - 1);
                    area += base * (prevHeight + height) / 2.0;
                }
            }
        }
        // The number of positives was factored out of all the base
        // computations.  Put it back in and return.
        return area / (double) totalPositives;
    }

    /**
     * <p>Computes the cross product of vectors OA and OB, that is, the
     * z-component of their three-dimensional cross product.  (A and B
     * are two-dimensional vectors and O is their common origin.)  The
     * cross product is positive if vector OAB turns counter-clockwise,
     * negative if clockwise, and zero if collinear.<p>
     *
     * <p>This can also be more intuitively understood in the planar
     * case as comparing the slopes/directions of the two vectors.  It
     * essentially computes (direction(OB) - direction(OA)).  This
     * understanding is derived as follows:<p>
     *
     * <pre><code>
     * direction(OB) = (B.y - O.y) / (B.x - O.x)
     * direction(OA) = (A.y - O.y) / (A.x - O.x)
     * d(OB) - d(OA) = ((B.y - O.y) / (B.x - O.x)) - ((A.y - O.y) / (A.x - O.x))
     *               = ((B.y - O.y) * (A.x - O.x) - (A.y - O.y) * (B.x - O.x)) / ((B.x - O.x) * (A.x - O.x))
     * </code></pre>
     *
     * <p>Then throw away the denominator because it doesn't affect the
     * comparison:<p>
     *
     * <pre>
     * (B.y - O.y) * (A.x - O.x) - (A.y - O.y) * (B.x - O.x)
     * </pre>
     *
     * <p>We only need integer vectors for ROC/PR curves so keep
     * everything integer calculations.<p>
     *
     * <p>Directly from the <a
     * href="http://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain">monotone
     * chain computational geometry</a> section from the Wikibook on
     * Algorithm Implementation.<p>
     *
     * @param ox The x component of vector O.
     * @param oy The y component of vector O.
     * @param ax The x component of vector A.
     * @param ay The y component of vector A.
     * @param bx The x component of vector B.
     * @param by The y component of vector B.
     * @return The cross product of the given vectors, OAxOB.
     * @see #convexHullPoints(int[], int[])
     */
    static int vectorCrossProduct(int ox, int oy, int ax, int ay, int bx, int by) {
        return (ax - ox) * (by - oy) - (ay - oy) * (bx - ox);
    }

    /**
     * <p>Finds the convex hull of a list of integer points.  The points
     * should already be sorted in increasing x order (ties broken by
     * increasing y order) which is already the case for curve data
     * counts.  Keeping separate arrays of x and y coordinates allows
     * for easy conversion to/from negative and positive counts.<p>
     *
     * <p>This implementation uses <a
     * href="http://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain">Andrew's
     * monotone chain convex hull algorithm</a> (except it runs
     * clockwise and only computes the upper hull).  The points are
     * already sorted and we already know the most extreme lower-left
     * point so the time complexity is O(n).<p>
     *
     * <p>Collinear points are redundant and so are dropped.<p>
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
                   Curve.vectorCrossProduct(
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
    public Curve convexHull() {
        // Calculate the convex hull from the points defined by the
        // counts.  The convex hull points are also in terms of counts.
        // These are the new counts.
        int[][] hullPoints = convexHullPoints(falsePositiveCounts,  // FPR on x-axis
                                              truePositiveCounts);  // TPR on y-axis
        return new Curve(hullPoints[1], hullPoints[0]);
    }

    /**
     * Computes the Mann-Whitney(-Wilcoxon) U statistics for the ranking
     * of positives and negatives.  The canonical U statistic is the
     * lesser of the two values.
     *
     * @return A two-element array containing the U statistic for the
     * positives and the U statistic for the negatives.
     */
    public double[] mannWhitneyU() {
        /* We don't know which U statistic will be less apriori so we
         * might as well compute and report both (although for
         * efficiency one could just do subtraction, but that doesn't
         * matter much here).  The other reason for reporting both
         * statistics is that ROC area can be calculated using the U
         * statistic for the negative examples.  Thus we need to know
         * both statistics, or at least which is which, not just the
         * minimum one.
         *
         * The individual U statistics are not necessarily integers, so
         * we need to return a pair of doubles.
         *
         * The ranking of the labels has to be reconstructed from the
         * counts so that the U statistics can be calculated.  The
         * difference of confusion matrices gives the numbers of
         * positives and negatives between thresholds.  The sum of the
         * true/false positives in a confusion matrix gives the total
         * number of labels above a particular threshold which is also
         * the maximum rank for those labels.  In general, we need to
         * sum up the ranks for the positive and negative labels, but
         * tied labels get a fractional, mean rank.  The mean rank is
         * just the mean of all the "raw" ranks of the labels that are
         * tied.  So if labels with "raw" ranks of 2, 3, 4, 5 are tied
         * they each get a fractional rank of 3.5.  In order to
         * calculate the rank each label gets, we need to know the
         * minimum and maximum "raw" ranks of the group, or,
         * alternatively, the maximum "raw" rank and the numbers of
         * positives and negatives in the group.  (All labels in a group
         * are tied and correspond to a single confusion matrix.)  The
         * mean rank is calculated as (((min-raw-rank) + (max-raw-rank))
         * / 2).  The quantity (min-raw-rank) is the same as the
         * ((max-raw-rank) + 1) from the previous group/confusion matrix
         * and can therefore be calculated as ((max-raw-rank) -
         * (pos-count) - (neg-count) + 1).
         *
         * The formula for an individual U statistic is
         *
         * u = r - n * (n + 1) / 2
         *
         * where r is the sum of fractional ranks for the sample and n
         * is the sample size.  Relationships between the U statistics
         * of the two samples:
         *
         * N = n1 + n2
         * r1 + r2 = N * (N + 1) / 2
         * u1 + u2 = n1 * n2
         */

        int posCount;
        int negCount;
        int maxRawRank;
        double rank;
        double sumPosRanks = 0.0;
        double sumNegRanks = 0.0;
        // Start with the first non-zero counts (index 1)
        for (int countsIndex = 1; countsIndex < truePositiveCounts.length; countsIndex++) {
            // Get the numbers of positives and negatives for this group
            posCount = truePositiveCounts[countsIndex] - truePositiveCounts[countsIndex - 1];
            negCount = falsePositiveCounts[countsIndex] - falsePositiveCounts[countsIndex - 1];
            // Get the max raw rank
            maxRawRank = truePositiveCounts[countsIndex] + falsePositiveCounts[countsIndex];
            // Calculate the (fractional) rank each label in this group gets
            rank = (double) (maxRawRank - posCount - negCount + 1 + maxRawRank) / 2.0;
            // Add the rank of each label to the sum
            sumPosRanks += rank * (double) posCount;
            sumNegRanks += rank * (double) negCount;
        }
        double uPos = sumPosRanks - (double) (totalPositives * totalPositives + totalPositives) / 2.0;
        double uNeg = sumNegRanks - (double) (totalNegatives * totalNegatives + totalNegatives) / 2.0;
        return new double[] {uPos, uNeg};
    }


    ////////////////////////////////////////


    /**
     * <p>Builds {@code Curve} objects in multiple, flexible ways.  The
     * methods in this class independently specify each piece of
     * requisite information and can be chained together into a single,
     * self-documenting expression.  (This class is a <a
     * href="http://en.wikipedia.org/wiki/Fluent_interface">fluent
     * interface</a> <a
     * href="http://en.wikipedia.org/wiki/Builder_pattern">builder
     * pattern</a>.)</p>
     *
     * <p>This builder works with iterable sequences of objects.  To
     * build a Curve from arrays of primitive numbers see {@link
     * PrimitivesBuilder}.<p>
     *
     * <p>Fundamentally, a curve is built from a ranking of labels and a
     * label to consider positive.  This approach is "pure" but often
     * inconvenient (see {@link Curve#Curve(List, Object)}).  Therefore,
     * curves are often constructed from lists of predictions (scores)
     * and the corresponding actual labels, which are typically
     * conveniently available.  In this case, the actual labels are
     * ranked by sorting the scores in descending order.<p>
     *
     * <p>Thus, in order to build a curve one must provide (1) a ranking
     * of labels or (2) corresponding lists of predictions and actual
     * labels.  A positive label must also be provided in either case.
     * (This class allows arbitrary labels for flexibility, such as
     * easily handling multiclass tasks, but then the user must say what
     * label is positive in a given situation.)</p>
     *
     * <p>There are also additional options.  A list of weights may be
     * given.  The list of weights must correspond to the lists of
     * predicteds and actuals (if given) or correspond to the list of
     * ranked labels.  A comparator may be given which will be used for
     * sorting the scores.  Otherwise the natural ordering of the scores
     * will be used.<p>
     *
     * <h3>Notes</h3>
     *
     * <ul>
     * <li>All inputs will be left unmodified.</li>
     * <li>Iterables will be instantiated as lists unless they are
     * already lists.</li>
     * <li>If providing a comparator, provide one for ascending order as
     * all comparators are reversed internally.</li>
     * <li>This builder works for multiple creations.  Just keep
     * updating parameters and calling build.</li>
     * </ul>
     *
     * @param <TScore> Type of score/prediction
     * @param <TLabel> Type of label
     */
    public static class Builder<TScore extends Comparable<? super TScore>, TLabel> {
        /* This is a fluent interface
         * (en.wikipedia.org/wiki/Fluent_interface) for building curves.
         * The point is to support multiple and flexible ways of
         * constructing curves without writing dozens of methods to
         * handle all the combinations of parameters.  See "Effective
         * Java" items 1, 2 (and the rest of Chapter 2).
         *
         * This implementation uses Lists instead of Collections because
         * collections are not necessarily ordered and ranking requires
         * order.
         */

        // The following are default access to allow subclassing within
        // this package.  All the lists should be the same length and
        // kept in the same order.
        List<TLabel> rankedLabels;
        List<TScore> predicteds;
        List<TLabel> actuals;
        List<Double> weights;
        TLabel positiveLabel;
        Comparator<? super TScore> comparator;

        /** No-op constructor. */
        public Builder() {}

        /**
         * Specifies a sequence of labels that have already been ranked
         * from most-believed positive to most-believed negative (the
         * belief/score, having already been used, is not given).
         * Unless labels are integers, a positive label must also be
         * specified.  The iterable is instantiated as a list if
         * necessary.
         *
         * @param labels A sequence of labels ranked from most positive
         * to most negative.
         * @return This builder
         */
        public Builder<TScore, TLabel> rankedLabels(Iterable<TLabel> labels) {
            if (labels instanceof List) {
                rankedLabels = (List<TLabel>) labels;
            } else {
                rankedLabels = Builder.instantiateSequence(labels);
            }
            return this;
        }

        /**
         * Specifies a sequence of scores or predictions that reflect
         * beliefs in how likely corresponding labels are positive.
         * Must be specified in combination with actual labels.  The
         * scores will be used to rank the actual labels.  The iterable
         * is instantiated as a list if necessary.
         *
         * @param predicteds A sequence of scores
         * @return This builder
         */
        public Builder<TScore, TLabel> predicteds(Iterable<TScore> predicteds) {
            if (predicteds instanceof List) {
                this.predicteds = (List<TScore>) predicteds;
            } else {
                this.predicteds = Builder.instantiateSequence(predicteds);
            }
            return this;
        }

        /**
         * Specifies a sequence of actual labels that correspond to the
         * predictions.  Must be specified in combination with
         * predictions.  The iterable is instantiated as a list if
         * necessary.
         *
         * @param actuals A sequence of labels
         * @return This builder
         */
        public Builder<TScore, TLabel> actuals(Iterable<TLabel> actuals) {
            if (actuals instanceof List) {
                this.actuals = (List<TLabel>) actuals;
            } else {
                this.actuals = Builder.instantiateSequence(actuals);
            }
            return this;
        }

//        /**
//         * Specifies a sequence of weights that correspond to the actual
//         * labels.  Completely optional.  The iterable is instantiated
//         * as a list if necessary.
//         *
//         * @param weights A sequence of doubles
//         * @return This builder
//         */
//        public Builder<TScore, TLabel> weights(Iterable<Double> weights) {
//            if (weights instanceof List) {
//                this.weights = (List<Double>) weights;
//            } else {
//                this.weights = Builder.instantiateSequence(weights);
//            }
//            return this;
//        }

        /**
         * Specifies the object to use as a positive label.
         *
         * @param label The positive label
         * @return This builder
         */
        public Builder<TScore, TLabel> positiveLabel(TLabel label) {
            positiveLabel = label;
            return this;
        }

        /**
         * Specifies a comparator to use for sorting scores.  The
         * comparator should sort scores in ascending order; it will be
         * reversed internally to produce a ranking.  If a comparator is
         * not specified (or it is specified as null), the natural
         * ordering will be used (in which case the scores must be
         * {@link Comparable}).
         *
         * @param comparator A comparator for scores
         */
        public Builder<TScore, TLabel> comparator(Comparator<? super TScore> comparator) {
            this.comparator = comparator;
            return this;
        }

        /**
         * Does the work of checking for valid builder state.  To be
         * called by {@link #build()} before building.
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
            } else {
                if (weights != null && weights.size() != rankedLabels.size()) {
                    throw new IllegalStateException("The sizes of 'rankedLabels' and 'weights' must agree.");
                }
            }
            // Builder state OK
        }

        /**
         * Builds a curve from parameters that have been specified up to
         * this point.  Ranks the labels by the scores if necessary.
         *
         * @return A new curve
         * @throws IllegalStateException if the builder is not in a
         * valid state to construct a curve
         */
        public Curve build() {
            // Check if it is OK to proceed (throws exception if not)
            checkValidBuilderState();

            // Create a list of ranked labels if not already given
            if (rankedLabels == null) {
                // Rank actuals by predicteds using a stable sort.
                // First populate a sortable list.
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
                Collections.sort(sorted, new TupleScoreReverseComparator(comparator));
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

            // TODO pass weights if specified
            return new Curve(rankedLabels, positiveLabel);
        }

        /**
         * Instantiates a sequence as a list.
         *
         * @param sequence Any sequence
         * @return A list containing the same elements as the sequence
         * in the same order.
         */
        public static <E> List<E> instantiateSequence(Iterable<E> sequence) {
            List<E> list = new LinkedList<E>();
            for (E element : sequence) {
                list.add(element);
            }
            return list;
        }

        /** Basic container to hold a score, label, and weight. */
        class Tuple {
            /* While you generally want a static inner class, this one
             * can't be static because it needs to "inherit" the type
             * parameters from the outer class.
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

        /**
         * Comparator for tuples that orders tuples in reverse order by
         * their score.
         */
        class TupleScoreReverseComparator implements Comparator<Tuple> {
            /* Non-static due to type parameters. */

            private Comparator<? super TScore> scoreComparator;

            public TupleScoreReverseComparator() {}

            public TupleScoreReverseComparator(Comparator<? super TScore> scoreComparator) {
                this.scoreComparator = scoreComparator;
            }

            public int compare(Tuple tuple1, Tuple tuple2) {
                // Reverse the ordering of the given comparator.  Use
                // the "natural ordering" (interface Comparable) if
                // comparator not specified.
                if (scoreComparator == null) {
                    return -tuple1.score.compareTo(tuple2.score);
                } else {
                    return -scoreComparator.compare(tuple1.score, tuple2.score);
                }
            }
        }
    }


    ////////////////////////////////////////


    /**
     * <p>This class is the same as {@link Builder} except this takes
     * arrays of primitives as input (instead of sequences of objects).
     * Scores must be doubles and labels must be integers.  (Labels must
     * compare exactly.)  The default positive label is 1.</p>
     *
     * <p>All arrays of primitives are converted to lists of number
     * objects.</p>
     */
    public static class PrimitivesBuilder extends Builder<Double, Integer> {

        /** Creates a builder with a default positive label of 1. */
        public PrimitivesBuilder() {
            positiveLabel = Integer.valueOf(1);
        }

        /**
         * @param labels A sequence of labels ranked from most positive
         * to most negative.
         * @return This builder
         * @see Builder#rankedLabels(Iterable)
         */
        public PrimitivesBuilder rankedLabels(int[] labels) {
            rankedLabels = PrimitivesBuilder.primitiveArrayToList(labels);
            return this;
        }

        /**
         * @param predicteds A sequence of scores
         * @return This builder
         * @see Builder#predicteds(Iterable)
         */
        public PrimitivesBuilder predicteds(double[] predicteds) {
            this.predicteds = PrimitivesBuilder.primitiveArrayToList(predicteds);
            return this;
        }

        /**
         * @param actuals A sequence of labels
         * @return This builder
         * @see Builder#actuals(Iterable)
         */
        public PrimitivesBuilder actuals(int[] actuals) {
            this.actuals = PrimitivesBuilder.primitiveArrayToList(actuals);
            return this;
        }

//        /**
//         * @param weights A sequence of doubles
//         * @return This builder
//         * @see Builder#weights(Iterable)
//         */
//        public PrimitivesBuilder weights(double[] weights) {
//            this.weights = PrimitivesBuilder.primitiveArrayToList(weights);
//            return this;
//        }

        /**
         * @param label The positive label
         * @return This builder
         * @see Builder#positiveLabel(Object)
         */
        public PrimitivesBuilder positiveLabel(int label) {
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
}
