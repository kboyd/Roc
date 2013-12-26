/*
 * Copyright (c) 2013 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import mloss.roc.util.NaiveCsvReader;


// General idea is to read columns (scores or labels) from files and
// then send them for appropriate processing (sorting, curve
// generation).

// TODO specification of columns in CSV files. bracket notation after
// filename on command line?

/**
 * <p>Main reads the results of a binary classification and generates a
 * report of the ROC and PR results.  The inputs are the scores given to
 * the examples and the associated groud truth labels.  The output is a
 * customizable report including ROC and/or PR curves and areas.</p>
 *
 * <p>The inputs can be specified with or without the examples in one or
 * more CSV (or other delimited text) inputs.  The general aim is to
 * take in a variety of inputs so that little or no processing needs to
 * be done to the output of a classification algorithm and so that
 * generating ROC/PR analyses are easy.  Indeed, this main will even
 * join scores and labels together database-style.</p>
 *
 * <p></p>
 */
public class Main {

    public static final String aboutMessage =
        "Roc 0.1.0.  ROC and PR curves.\n" +
        "Copyright (c) 2013 Roc Project.  This is free software; see the license.\n" +
        "https://github.com/kboyd/Roc";

    // TODO replace license message with dumping license file from JAR
    public static final String licenseMessage =
        "Roc is free, open source software licensed under the BSD 2-clause (FreeBSD) license.";

    public static final String helpKey = "--help";
    public static final String versionKey = "--version";
    public static final String aboutKey = "--about";
    public static final String licenseKey = "--license";
    public static final String scoresKey = "--scores";
    public static final String labelsKey = "--labels";
    public static final String scoresLabelsKey = "--scores-labels";

    public static final String stdioFileName = "-";

    public static final String help =
        "TODO!  No help yet.  Sorry about that.\n" +
        helpKey + "\n" +
        versionKey + " | " + aboutKey + "\n" +
        licenseKey + "\n" +
        "\n" +
        scoresKey + " FILE[:OPTS]\n" +
        labelsKey + " FILE[:OPTS]\n" +
        scoresLabelsKey + " FILE[:OPTS]\n" +
        "";

    /** -h, -?, --help, help, etc. */
    private static final Pattern helpPattern = Pattern.compile("(--?(h(elp)?|\\?))|help");

    /**
     *
     */
    public static void apiMain(String[] args, BufferedReader input,
                               PrintWriter output, PrintWriter error
                               ) throws MainException, FileNotFoundException, IOException {
        // Environment.  For now this is a naive environment that maps
        // keys (command line options) to unparsed values (or null).
        // Booleans are represented by key existence and file names are
        // strings.  It would be better to map the keys to appropriate
        // objects that can handle the various types of environment
        // values.
        String defaultOperation = scoresLabelsKey;
        String defaultFileName = stdioFileName;
        String defaultDelimiter = ",";
        Map<String, String> env = new TreeMap<String, String>();

        // Search the command line arguments for requested help.  Help
        // overrides all other operations and must be processed before
        // any possible exceptions.  Therefore it must be treated
        // specially.
        for (String arg : args) {
            if (helpPattern.matcher(arg.toLowerCase()).matches()) {
                error.println(help);
                return;
            }
        }

        // Parse the command line arguments
        for (int argIndex = 0; argIndex < args.length; argIndex++) {
            String arg = args[argIndex].toLowerCase();
            if (arg.equals(helpKey) ||
                arg.equals(versionKey) ||
                arg.equals(aboutKey) ||
                arg.equals(licenseKey)
                ) {
                env.put(arg, null);
            } else if (arg.equals(scoresKey) ||
                       arg.equals(labelsKey) ||
                       arg.equals(scoresLabelsKey)
                       ) {
                // Check next argument exists
                if (argIndex + 1 < args.length) {
                    String fileName = args[argIndex + 1];
                    env.put(arg, fileName);
                    argIndex++;
                } else {
                    throw new MainException(String.format("Argument missing for option: %s", arg), ExitStatus.ERROR_USAGE);
                }
            } else {
                throw new MainException(String.format("Unrecognized option: %s", arg), ExitStatus.ERROR_USAGE);
            }
        }

        // Enforce logical constraints
        if (env.containsKey(scoresKey) && !env.containsKey(labelsKey)) {
            throw new MainException(String.format("Option '%s' must be accompanied by option '%s'.", scoresKey, labelsKey), ExitStatus.ERROR_USAGE);
        }

        // Check files exist up front before processing anything
        for (String value : env.values()) {
            if (value == null || value.equals(defaultFileName))
                continue;
            File file = new File(value);
            if (!file.exists() || !file.isFile() || !file.canRead()) {
                throw new MainException(String.format("Not a readable file: %s", value), ExitStatus.ERROR_FILE);
            }
        }

        // If no operations, do the default
        if (env.size() == 0) {
            env.put(defaultOperation, defaultFileName);
        }

        // Process the operations

        // Print about
        if (env.containsKey(versionKey) || env.containsKey(aboutKey)) {
            error.println(aboutMessage);
        }
        // Print license
        if (env.containsKey(licenseKey)) {
            error.println(licenseMessage);
        }
        // Process inputs.  Providing scores and labels together in one
        // input overrides providing them individually.
        if (env.containsKey(scoresLabelsKey)) {
            // Input is scores and labels together
            //output.println(scoresLabelsKey);
            //output.println(env.get(scoresLabelsKey));
            // TODO assume columns or what for multiple columns in single file?
            // Get scores and labels CSV input from a file or stdin as specified
            BufferedReader scoresLabelsInput = openFileOrDefault(env.get(scoresLabelsKey), stdioFileName, input);
            // Generate curve and output
            processScoresLabelsOneInput(scoresLabelsInput, defaultDelimiter, 0, 1, output);
        } else if (env.containsKey(labelsKey)) {
            // Input is scores and labels separately or labels only
            BufferedReader scoresInput = null;
            BufferedReader labelsInput = null;
            // Get scores input only if given
            if (env.containsKey(scoresKey)) {
                //output.println(scoresKey);
                //output.println(env.get(scoresKey));
                // Get scores CSV input from a file or stdin as specified
                scoresInput = openFileOrDefault(env.get(scoresKey), stdioFileName, input);
            }
            // Get labels CSV input from a file or stdin as specified
            labelsInput = openFileOrDefault(env.get(labelsKey), stdioFileName, input);
            // Generate curve and output
            processScoresLabelsTwoInputs(scoresInput, labelsInput, defaultDelimiter, output);
        }
    }

    /**
     *
     */
    public static void apiMain(String[] args)
        throws MainException, FileNotFoundException, IOException {
        apiMain(args,
                new BufferedReader(new InputStreamReader(System.in)),
                new PrintWriter(System.out, true),
                new PrintWriter(System.err, true));
    }

    /**
     *
     */
    public enum ExitStatus {
        OK,
        ERROR_USAGE,
        ERROR_INPUT,
        ERROR_FILE,
        ERROR_INTERNAL,
    }

    /**
     *
     */
    public static class MainException extends Exception {
        private static final long serialVersionUID = 1L;

        public ExitStatus exitStatus;

        public MainException(String message, ExitStatus exitStatus) {
            super(message);
            this.exitStatus = exitStatus;
        }
    }

    /**
     * Calls {@link #apiMain(String[])}, handles its exceptions, and
     * exits.
     */
    public static void main(String[] args) {
        try {
            apiMain(args);
        } catch (Exception e) {
            //System.err.println(String.format("roc: Error: %s: %s", e.getClass().getName(), e.getMessage()));
            System.err.println(String.format("roc: Error: %s", e.getMessage()));
            int exitStatus = 1;
            if (e instanceof MainException) {
                exitStatus = ((MainException) e).exitStatus.ordinal();
            }
            System.exit(exitStatus);
        }
    }

    public static BufferedReader openFileOrDefault(String fileName, String defaultName, BufferedReader defaultInput) throws IOException {
        if (fileName.equals(defaultName)) {
            return defaultInput;
        } else {
            return new BufferedReader(new FileReader(fileName));
        }
    }

    private static void processScoresLabelsOneInput(BufferedReader scoresLabelsInput, String delimiter, int scoresColumn, int labelsColumn, PrintWriter output) throws IOException {
        // Read the CSV input
        List<String[]> csvRows = new NaiveCsvReader(scoresLabelsInput, delimiter).readAll();
        //for (String[] row : csvRows) {
        //    output.println(Arrays.toString(row));
        //}
        // Create iterators for appropriate columns
        Iterator<Double> scoresIterator = new StringToDoubleConversionIterator(new ProjectionIterator<String>(csvRows, scoresColumn));
        Iterator<String> labelsIterator = new ProjectionIterator<String>(csvRows, labelsColumn);
        // Call curve generation and output
        printReport(buildCurve(scoresIterator, labelsIterator), output);
    }

    private static void processScoresLabelsTwoInputs(BufferedReader scoresInput, BufferedReader labelsInput, String delimiter, PrintWriter output) throws IOException {
        List<String[]> csvRows;
        Iterator<Double> scoresIterator = null;
        // Process scores input only if given
        if (scoresInput != null) {
            // Read CSV input
            csvRows = new NaiveCsvReader(scoresInput, delimiter).readAll();
            //for (String[] row : csvRows) {
            //    output.println(Arrays.toString(row));
            //}
            // Create scores iterator
            scoresIterator = new StringToDoubleConversionIterator(new ProjectionIterator<String>(csvRows, 0));
        }
        // Process labels input
        csvRows = new NaiveCsvReader(labelsInput, delimiter).readAll();
        //for (String[] row : csvRows) {
        //    output.println(Arrays.toString(row));
        //}
        // Create labels iterator
        Iterator<String> labelsIterator = new ProjectionIterator<String>(csvRows, 0);
        // Call curve generation and output
        printReport(buildCurve(scoresIterator, labelsIterator), output);
    }

    private static Curve buildCurve(Iterator<Double> scoresIterator, Iterator<String> labelsIterator) {
        return new Curve.Builder<Double,String>()
            .predicteds(new IteratorIterable<Double>(scoresIterator))
            .actuals(new IteratorIterable<String>(labelsIterator))
            .positiveLabel("1")
            .build();
    }

    public static class ProjectionIterator<E> implements Iterator<E> {
        private Iterator<E[]> iterator;
        private int column;

        public ProjectionIterator(List<E[]> table, int column) {
            iterator = table.iterator();
            this.column = column;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public E next() {
            E[] next = iterator.next();
            if (next.length > column) {
                return next[column];
            } else {
                return null;
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static class StringToDoubleConversionIterator implements Iterator<Double> {

        private Iterator<String> iterator;

        public StringToDoubleConversionIterator(Iterator<String> iterator) {
            this.iterator = iterator;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Double next() {
            String next = iterator.next();
            try {
                return Double.parseDouble(next);
            } catch (NumberFormatException e) {
                // Fix exception message so that it is independent of the exception name
                throw new NumberFormatException(String.format("Not a floating-point number: \"%s\"", next));
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static class IteratorIterable<T> implements Iterable<T> {
        private Iterator<T> iterator;

        public IteratorIterable(Iterator<T> iterator) {
            this.iterator = iterator;
        }

        public Iterator<T> iterator() {
            return iterator;
        }
    }

    public static void printReport(Curve curve, PrintWriter output /* TODO what to include and what format */) {
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
        output.println("ROC points Gnuplot text: |");
        for (double[] point : rocPoints) {
            // Just format the floating point numbers to string for now
            // because I can't find a floating point format that works
            // like the string formatting and chops off the trailing
            // zeros.
            output.println(String.format("%s %s", point[0], point[1]));
        }
        output.println("# End ROC points Gnuplot text");
        output.println("...");
    }
}
