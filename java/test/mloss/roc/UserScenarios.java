/*
 * Copyright (c) 2013 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc;

import static org.junit.Assert.*;
import org.junit.Test;


/**
 * User scenarios are examples and acceptance tests, functional tests
 * that validate the features and user stories.
 */
public class UserScenarios {

    /**
     * A basic scenario where the user has two arrays, the actual labels
     * extracted from their dataset and the predicted labels from their
     * classifier, and just calculates the area for the ROC curve and
     * the area and the points for the achievable curve.
     */
    @Test
    public void minimalRocAreaScenario() {
        // Imagine the following two arrays come from a dataset and a classifier
        int[] actualLabels = {1, 1, 0, 0, 0, 0, 1,
                              1, 0, 1, 1, 1, 0};
        double[] predictedLabels = {0.86, 0.54, 0.15, 0.69, 0.51, 0.35, 0.78,
                                    0.06, 0.70, 0.71, 0.80, 0.84, 0.67};
        // Sorted (Python dict syntax):
        // {0.86:1, 0.84:1, 0.80:1, 0.78:1, 0.71:1, 0.70:0, 0.69:0,
        //  0.67:0, 0.54:1, 0.51:0, 0.35:0, 0.15:0, 0.06:1}
        // This creates a curve that is (minimally) the following points:
        // [(0, 0), (0, 5/7), (3/6, 5/7), (3/6, 6/7), (6/6, 6/7), (6/6, 7/7)]
        // The convex hull is the following points:
        double[][] maxPoints = {{0.0, 0.0}, {0.0, 5.0 / 7.0}, {1.0, 1.0}};

        // Create the ROC analysis (The ranking is computed automatically.)
        Curve rocAnalysis = new Curve.PrimitivesBuilder()
            .predicteds(predictedLabels)
            .actuals(actualLabels)
            .build();
        // Get the convex hull
        Curve convexHull = rocAnalysis.convexHull();
        // Calculate the AUC ROCs
        double area = rocAnalysis.rocArea();
        double maxArea = convexHull.rocArea();
        // Get the points for later plotting
        double[][] rocPoints = convexHull.rocPoints();

        // Check the results
        // 2 rectangles: 3/6 * 5/7 + 3/6 * 6/7 = 11/14
        assertEquals(11.0 / 14.0, area, CurveTest.TOLERANCE);
        // 1 trapezoid: (5 / 7 + 1) * 1 / 2 = 6/7
        assertEquals(6.0 / 7.0, maxArea, CurveTest.TOLERANCE);
        assertEquals(maxPoints.length, rocPoints.length);
        for (int pointIndex = 0; pointIndex < maxPoints.length; pointIndex++) {
            assertArrayEquals(maxPoints[pointIndex], rocPoints[pointIndex], CurveTest.TOLERANCE);
        }
    }

    // TODO read actual and predicted labels from file
    // TODO write points to file suitable for e.g. gnuplot
    // TODO one-liner
}
