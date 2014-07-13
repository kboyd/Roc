/*
 * Copyright (c) 2014 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc;


import java.io.PrintWriter;
import java.util.Arrays;


// Alternative is an object that has a format and a list of what to
// include in the report
public class Reports {

    public static final String[] names = {
        "all",
        "prArea",
        "prPts",
        "rocArea",
        "rocPts",
    };

    public static final String namesString = Arrays.toString(names);

    public static void report(String reportName, Curve curve, PrintWriter output) {
        String reportNameLower = reportName.toLowerCase();
        if (reportNameLower.equals(names[0].toLowerCase())) {
            yaml(curve, output);
        } else if (reportNameLower.equals(names[1].toLowerCase())) {
            prArea(curve, output);
        } else if (reportNameLower.equals(names[2].toLowerCase())) {
            prPts(curve, output);
        } else if (reportNameLower.equals(names[3].toLowerCase())) {
            rocArea(curve, output);
        } else if (reportNameLower.equals(names[4].toLowerCase())) {
            rocPts(curve, output);
        } else {
            throw new IllegalArgumentException(String.format("Report name '%s' is not one of %s.", reportName, namesString));
        }
    }

    public static void prArea(Curve curve, PrintWriter output) {
        output.println(String.format("%s", curve.prArea()));
    }

    public static void prPts(Curve curve, PrintWriter output) {
        double[][] prPoints = curve.prPoints();
        for (double[] point : prPoints) {
            output.println(String.format("%s %s", point[0], point[1]));
        }
    }

    public static void rocArea(Curve curve, PrintWriter output) {
        output.println(String.format("%s", curve.rocArea()));
    }

    public static void rocPts(Curve curve, PrintWriter output) {
        double[][] rocPoints = curve.rocPoints();
        for (double[] point : rocPoints) {
            output.println(String.format("%s %s", point[0], point[1]));
        }
    }

    public static void yaml(Curve curve, PrintWriter output) {
        output.println("%YAML 1.1");
        output.println("---");
        output.println("ROC area: " + curve.rocArea());
        double[][] rocPoints = curve.rocPoints();
        output.println(String.format("ROC points count: %d", rocPoints.length));
        output.println("ROC points:");
        for (double[] point : rocPoints) {
            // Just format the floating point numbers to string for now
            // because I can't find a floating point format that works
            // like the string formatting and chops off the trailing
            // zeros.
            output.println(String.format("  - [%s, %s]", point[0], point[1]));
        }
        output.println("PR area: " + curve.prArea());
        double[][] prPoints = curve.prPoints();
        output.println(String.format("PR points count: %d", prPoints.length));
        output.println("PR points:");
        for (double[] point : prPoints) {
            output.println(String.format("  - [%s, %s]", point[0], point[1]));
        }
        output.println("...");
    }

}
