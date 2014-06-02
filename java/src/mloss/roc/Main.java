/*
 * Copyright (c) 2014 Roc Project.  This is free software.  See
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mloss.roc.util.CsvProcessing;
import mloss.roc.util.NaiveCsvReader;


// General idea is to read columns (scores or labels) from files and
// then send them for appropriate processing (joining, sorting, curve
// generation).

// TODO options: --report <report> --to <file>
// TODO option for specifying positive label


/**
 * <p>Main reads the results of a binary classification and generates a
 * report of the ROC and PR results.  The inputs are the scores given to
 * the examples and the associated ground truth labels.  The output is a
 * customizable report including ROC and/or PR curves and areas.</p>
 *
 * <p>The inputs can be specified with or without the examples in one or
 * more CSV (or other delimited text) inputs.  The general aim is to
 * take in a variety of inputs so that little or no processing needs to
 * be done to the output of a classification algorithm and so that
 * generating ROC/PR analyses are easy.  Indeed, this main will even
 * join scores and labels together database-style.</p>
 */
public class Main {

    public static final String aboutMessage =
        "Roc 0.1.0.  ROC and PR curves.\n" +
        "Copyright (c) 2014 Roc Project.  This is free software; see the license.\n" +
        "https://github.com/kboyd/Roc";

    // TODO replace license message with dumping license file from JAR
    // (or similar "resource" access)
    public static final String licenseMessage =
        "Roc is free, open source software licensed under the BSD 2-clause (FreeBSD) license.";

    public static final String helpOptName = "--help";
    public static final String versionOptName = "--version";
    public static final String aboutOptName = "--about";
    public static final String licenseOptName = "--license";
    public static final String debugOptName = "--debug";
    public static final String scoresOptName = "--scores";
    public static final String labelsOptName = "--labels";
    public static final String scoresLabelsOptName = "--scores-labels";
    public static final String scoresKeyOptName = "--scores-key";
    public static final String scoresColumnOptName = "--scores-column";
    public static final String labelsKeyOptName = "--labels-key";
    public static final String labelsColumnOptName = "--labels-column";

    public static final String stdioFileName = "-";

    private static final String indent = "        ";

    // TODO load help from a file, or have it compiled in, or build it with an options package?
    // file pros: easy to write, could work with multiple languages
    // file cons: how load reliably? (resources loader?), synchronization of code and help
    // here pros: code and help automatically synchronized
    // here cons: complex to read/write, hard to format
    // opts pros: might provide best-of-both-worlds of above
    // opts cons: third-party, syntax/verbosity

    // Manually wrap the help text to 80 characters (column 89 for
    // non-indented, column 81 for indented)
    public static final String help =
        "                      Roc.  Everything ROC and PR curves.\n\n" +

        "SYNOPSIS\n\n" +

        "java mloss.roc.Main [OPTION [ARGUMENT]]...\n\n" +

        "DESCRIPTION\n\n" +

        "Reads the scores and labels of a binary classification result and prints ROC and\n" +
        "PR analysis reports.  There are three basic modes of input:\n" +
        "  1. a single file with columns for both scores and labels,\n" +
        "  2. columns of scores and labels in separate files to be joined together,\n" +
        "  3. a single file with a column of labels that are already in ranked order.\n" +
        "The default behavior when no options are given is to treat standard input as\n" +
        "mode 1, equivalent to '--scores-labels -'.  Using '-' as a file name indicates\n" +
        "standard input.\n" +
        "\n" +

        "OPTIONS\n\n" +

        helpOptName + "\n" + indent + "Display this help.\n" +
        versionOptName + " | " + aboutOptName + "\n" + indent +
        "Display the version and other information about this software.\n" +
        licenseOptName + "\n" + indent +
        "Display a summary of the license for this software.\n" +
        debugOptName + "\n" + indent + "Print stack traces, etc.\n" +

        scoresOptName + " FILE\n" + indent +
        "File containing scores, one per line, in CSV format.  Must be specified\n" + indent +
        "in combination with --labels.  The scores are matched to the labels by\n" + indent +
        "the specified keys or otherwise by line number.  No default.\n" +
        labelsOptName + " FILE\n" + indent +
        "File containing labels, one per line, in CSV format.  Labels specified\n" + indent +
        "by themselves are treated as already ranked from most positive to most\n" + indent +
        "negative.  No default.\n" +
        scoresLabelsOptName + " FILE\n" + indent +
        "File containing scores and labels, in CSV format.  No default.\n" +

        scoresColumnOptName + " COLUMN\n" + indent +
        "Column containing the scores in the scores file or the scores-labels\n" + indent +
        "file.  1-based number.  Default is 1 for separate scores and labels\n" + indent +
        "files or otherwise the smallest number not specified by --labels-column.\n" +
        labelsColumnOptName + " COLUMN\n" + indent +
        "Column containing the labels in the labels file or the scores-labels\n" + indent +
        "file.  1-based number.  Default is 1 for separate scores and labels\n" + indent+
        "files or otherwise the smallest number not specified by --scores-column.\n" +
        scoresKeyOptName + " COLUMN_LIST\n" + indent +
        "Column or list of columns containing the (compound) keys for the scores\n" + indent +
        "file to use when joining the scores to the labels from the labels file.\n" + indent +
        "Comma-separated list of 1-based numbers as a single token (no spaces).\n" + indent +
        "Default is to join the files by line number.  Only applies to separate\n" + indent +
        "scores and labels files.\n" +
        labelsKeyOptName + " COLUMN_LIST\n" + indent +
        "Column or list of columns containing the (compound) keys for the labels\n" + indent +
        "file to use when joining the labels to the scores from the scores file.\n" + indent +
        "Comma-separated list of 1-based numbers as a single token (no spaces).\n" + indent +
        "Default is to join the files by line number.  Only applies to separate\n" + indent +
        "scores and labels files.\n" +
        "\n" +

        "EXAMPLES\n\n" +

        "Join results with ground truth:\n" +
        "    ...Main --scores results.csv --scores-key 1 --scores-column 5\n" +
        "            --labels truth.csv   --labels-key 3 --labels-column 4\n" +

        "Join using compound key (whose fields are reversed in the truth):\n" +
        "    ...Main --scores results.csv --scores-key 1,2 --scores-column 3\n" +
        "            --labels truth.csv   --labels-key 2,1 --labels-column 3\n" +

        "Specify score and label columns:\n" +
        "    ...Main --scores-labels scrsLbls.csv --labels-column 3 --scores-column 4\n" +

        "Processing standard input with default options (which could be omitted):\n" +
        "    <classifier> | java...Main --scores-labels - --scores-column 1\n" +
        "        --labels-column 2\n" +

        "";  // This is here to make inserting/reordering lines easier

    /**
     * Pattern to recognize various requests for help: -h, -?, --help,
     * help, etc.
     */
    private static final Pattern helpPattern =
        Pattern.compile("(--?(h(elp)?|\\?))|help");

    /** Pattern to match positive integers. */
    private static final Pattern integerPattern =
        Pattern.compile("\\d+");

    /** Pattern to match a list of positive integers. */
    private static final Pattern integerListPattern =
        Pattern.compile("\\d+(,\\d+)*");

    /** Pattern to split by commas. */
    private static final Pattern commaSplitPattern =
        Pattern.compile("\\s*,\\s*");

    // IO members
    private BufferedReader input;
    private PrintWriter output;
    private PrintWriter error;

    /** Constructs a main with the given IO. */
    public Main(BufferedReader input,
                PrintWriter output,
                PrintWriter error) {
        this.input = input;
        this.output = output;
        this.error = error;
    }

    /** Constructs a main that uses all the standard IO. */
    public Main() {
        this(new BufferedReader(new InputStreamReader(System.in)),
             new PrintWriter(System.out, true), // auto-flush
             new PrintWriter(System.err, true));
    }

    /**
     * Runs the given command line.
     */
    public void run(String[] args)
        throws MainException, FileNotFoundException, IOException {

        // Environment.  For now this is a somewhat naive environment
        // that maps keys (command line options) to FileArguments or
        // unparsed values (or null).  Booleans are represented by key
        // existence.  It would be better to map the keys to appropriate
        // objects that can handle the various types of environment
        // values, like in a command line parsing library.
        String defaultOperation = scoresLabelsOptName;
        String defaultFileName = stdioFileName;
        String defaultDelimiter = ",";
        String positiveLabel = "1";
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

        // Parse the command line options and their arguments
        for (int argIndex = 0; argIndex < args.length; argIndex++) {
            String arg = args[argIndex].toLowerCase();

            // Commands
            if (arg.equals(helpOptName) ||
                arg.equals(versionOptName) ||
                arg.equals(aboutOptName) ||
                arg.equals(licenseOptName)) {
                // Just store the command
                env.put(arg, null);
            } 

            // Input files
            else if (arg.equals(scoresOptName) ||
                     arg.equals(labelsOptName) ||
                     arg.equals(scoresLabelsOptName)) {
                // Check filename argument given
                if (argIndex + 1 >= args.length) {
                    throw new MainException(String.format("File name missing after option: %s", arg), ExitStatus.ERROR_USAGE);
                }
                // Check file exists and is readable
                String fileName = args[argIndex + 1];
                File file = new File(fileName);
                if (!fileName.equals(defaultFileName) &&
                    (!file.exists() ||
                     !file.isFile() ||
                     !file.canRead())) {
                    throw new MainException(String.format("Not a readable file: %s", fileName), ExitStatus.ERROR_FILE);
                }
                // Store the file name
                env.put(arg, fileName);
                argIndex++;
            }

            // Integers
            else if (arg.equals(scoresColumnOptName) ||
                     arg.equals(labelsColumnOptName)) {
                // Check integer argument given
                if (argIndex + 1 >= args.length) {
                    throw new MainException(String.format("Integer missing after option: %s", arg), ExitStatus.ERROR_USAGE);
                }
                // Check the value is an integer
                String integerValue = args[argIndex + 1];
                if (!integerPattern.matcher(integerValue).matches()) {
                    throw new MainException(String.format("Not an integer: %s", integerValue), ExitStatus.ERROR_USAGE);
                }
                // Store the integer value
                env.put(arg, integerValue);
                argIndex++;
            }

            // (Lists of) integers
            else if (arg.equals(scoresKeyOptName) ||
                     arg.equals(labelsKeyOptName)) {
                // Check integer/list argument given
                if (argIndex + 1 >= args.length) {
                    throw new MainException(String.format("Integer or list of integers missing after option: %s", arg), ExitStatus.ERROR_USAGE);
                }
                // Check the value is an integer or a list of integers
                String integerListValue = args[argIndex + 1];
                if (!integerListPattern.matcher(integerListValue).matches()) {
                    throw new MainException(String.format("Not an integer or list of integers: %s", integerListValue), ExitStatus.ERROR_USAGE);
                }
                // Store the integer value
                env.put(arg, integerListValue);
                argIndex++;
            }

            // Ignored or non-functional arguments
            else if (arg.equals(debugOptName)) {
            }

            // Unrecognized
            else {
                throw new MainException(String.format("Unrecognized option: %s", arg), ExitStatus.ERROR_USAGE);
            }
        } // Done parsing command line

        // Enforce logical constraints between options
        if (env.containsKey(scoresOptName) && !env.containsKey(labelsOptName)) {
            throw new MainException(String.format("Option '%s' must be accompanied by option '%s'.", scoresOptName, labelsOptName), ExitStatus.ERROR_USAGE);
        }

        // If no operations, do the default
        if (env.size() == 0) {
            env.put(defaultOperation, defaultFileName);
        }

        // Process the operations

        // Print about
        if (env.containsKey(versionOptName) || env.containsKey(aboutOptName)) {
            error.println(aboutMessage);
        }

        // Print license
        if (env.containsKey(licenseOptName)) {
            error.println(licenseMessage);
        }

        // Process inputs to build the curve.  Providing scores and
        // labels together in one input overrides providing them
        // individually.
        Curve curve = null;

        // Input is scores and labels together in CSV format
        if (env.containsKey(scoresLabelsOptName)) {
            // Get the scores and labels columns
            int scoreCol = 0;
            int labelCol = 0;
            if (env.containsKey(scoresColumnOptName)) {
                String integerValue = env.get(scoresColumnOptName);
                scoreCol = Integer.parseInt(integerValue);
            }
            if (env.containsKey(labelsColumnOptName)) {
                String integerValue = env.get(labelsColumnOptName);
                labelCol = Integer.parseInt(integerValue);
            }
            // Check column numbers are different
            if (scoreCol == labelCol && scoreCol > 0) {
                throw new MainException("The scores and labels columns must not be the same.", ExitStatus.ERROR_USAGE);
            }
            // Default column values that were not specified
            if (scoreCol == 0 && labelCol == 0) {
                scoreCol = 1;
                labelCol = 2;
            } else if (scoreCol == 0) {
                if (labelCol == 1) {
                    scoreCol = 2;
                } else {
                    scoreCol = 1;
                }
            } else if (labelCol == 0) {
                if (scoreCol == 1) {
                    labelCol = 2;
                } else {
                    labelCol = 1;
                }
            }
            // Change column numbers to indices
            scoreCol--;
            labelCol--;

            // Read the CSV input from files or stdin as requested
            String slFileName = env.get(scoresLabelsOptName);
            List<String[]> scoresLabelsCsv =
                readCsv(slFileName, openFileOrInput(slFileName), defaultDelimiter);

            // Build curve
            curve = buildCurveFromScoresLabels(
                scoresLabelsCsv, scoreCol, labelCol, positiveLabel);
        }

        // Input is scores and labels separately in CSV format
        else if (env.containsKey(scoresOptName) && env.containsKey(labelsOptName)) {
            // Get the key columns
            int[] scoresKeyCols = {};
            int[] labelsKeyCols = {};
            if (env.containsKey(scoresKeyOptName)) {
                String integerListValue = env.get(scoresKeyOptName);
                scoresKeyCols = parseIntegerList(integerListValue);
            }
            if (env.containsKey(labelsKeyOptName)) {
                String integerListValue = env.get(labelsKeyOptName);
                labelsKeyCols = parseIntegerList(integerListValue);
            }
            // Check for key agreement (provided by both or neither)
            if ((scoresKeyCols.length > 0 || 
                 labelsKeyCols.length > 0) &&
                scoresKeyCols.length != labelsKeyCols.length) {
                throw new MainException("The same number of keys must be specified for both scores and labels.", ExitStatus.ERROR_USAGE);
            }

            // Get the scores and labels columns
            int scoreCol = 0;
            int labelCol = 0;
            if (env.containsKey(scoresColumnOptName)) {
                String integerValue = env.get(scoresColumnOptName);
                scoreCol = Integer.parseInt(integerValue) - 1;
            }
            if (env.containsKey(labelsColumnOptName)) {
                String integerValue = env.get(labelsColumnOptName);
                labelCol = Integer.parseInt(integerValue) - 1;
            }

            // Read the CSV input from files or stdin as requested
            String sFileName = env.get(scoresOptName);
            String lFileName = env.get(labelsOptName);
            List<String[]> scoresCsv =
                readCsv(sFileName, openFileOrInput(sFileName), defaultDelimiter);
            List<String[]> labelsCsv =
                readCsv(lFileName, openFileOrInput(lFileName), defaultDelimiter);

            // Build curve with the rows already in order or with a join
            if (scoresKeyCols.length == 0) {
                curve = buildCurveFromSeparateScoresLabels(
                    scoresCsv, scoreCol,
                    labelsCsv, labelCol,
                    positiveLabel);
            } else {
                curve = buildCurveFromJoinedScoresLabels(
                    scoresCsv, scoresKeyCols, scoreCol,
                    labelsCsv, labelsKeyCols, labelCol,
                    positiveLabel);
            }
        }

        // Input is ranked labels (only) in CSV format
        else if (env.containsKey(labelsOptName)) {
            // Get the label column
            int labelCol = 0;
            if (env.containsKey(labelsColumnOptName)) {
                String integerValue = env.get(labelsColumnOptName);
                labelCol = Integer.parseInt(integerValue) - 1;
            }

            // Read the CSV input from files or stdin as requested
            String lFileName = env.get(labelsOptName);
            List<String[]> labelsCsv =
                readCsv(lFileName, openFileOrInput(lFileName), defaultDelimiter);

            // Build curve
            curve = buildCurveFromRankedLabels(
                labelsCsv, labelCol, positiveLabel);
        }

        // Report on the curve (if one was constructed)
        if (curve != null) {
            printReport(curve, output);
        }
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
            new Main().run(args);
        } catch (Exception e) {
            System.err.println(String.format("roc: Error: %s", e.getMessage()));
            // Print a stack trace if in debug mode
            if (Arrays.asList(args).contains(debugOptName)) {
                e.printStackTrace();
            }
            // Determine and return exit status
            int exitStatus = ExitStatus.ERROR_INTERNAL.ordinal();
            if (e instanceof MainException) {
                exitStatus = ((MainException) e).exitStatus.ordinal();
            }
            System.exit(exitStatus);
        }
    }

    /**
     * Parse a comma-separated list of positive integers into an array
     * of integers, subtracting 1 along the way.
     */
    private static int[] parseIntegerList(String list) {
        String[] strs = commaSplitPattern.split(list);
        int[] ints = new int[strs.length];
        for (int i = 0; i < strs.length; i++) {
            ints[i] = Integer.parseInt(strs[i]) - 1;
        }
        return ints;
    }

    public BufferedReader openFileOrInput(String fileName)
        throws FileNotFoundException {

        // Use the given input if the file name signifies stdio.
        // Otherwise open the given file.
        if (fileName.equals(stdioFileName)) {
            return input;
        } else {
            return new BufferedReader(new FileReader(fileName));
        }
    }

    /** Reads the specified input as CSV data. */
    public static List<String[]> readCsv(
            String fileName,
            BufferedReader input,
            String delimiter)
        throws IOException, MainException {

        // Read the entire CSV
        List<String[]> csv = new NaiveCsvReader(input, delimiter).readAll();
        // Check for non-empty input
        if (csv.size() == 0) {
            throw new MainException(String.format("Empty input: %s", fileName),
                                    ExitStatus.ERROR_FILE);
        }
        return csv;
    }

    public static Curve buildCurveFromRankedLabels(
            Iterable<String[]> labelsCsv,
            int labelsColumn,
            String positiveLabel) {

        // Create an iterator for the input column of labels
        Iterable<String> labelsIterator =
            new ProjectionIterator<String>(labelsCsv, labelsColumn);

        // Build and return the curve
        return new Curve.Builder<Double,String>()
            .rankedLabels(labelsIterator)
            .positiveLabel(positiveLabel)
            .build();
    }

    public static Curve buildCurveFromScoresLabels(
            Iterable<String[]> scoresLabelsCsv,
            int scoresColumn,
            int labelsColumn,
            String positiveLabel) {

        // Project the scores and labels
        Iterable<Double> scoresIterator =
            new StringToDoubleConversionIterator(
                new ProjectionIterator<String>(scoresLabelsCsv,
                                               scoresColumn));
        Iterable<String> labelsIterator =
            new ProjectionIterator<String>(scoresLabelsCsv,
                                           labelsColumn);

        // Build and return the curve
        return new Curve.Builder<Double,String>()
            .predicteds(scoresIterator)
            .actuals(labelsIterator)
            .positiveLabel(positiveLabel)
            .build();
    }

    public static Curve buildCurveFromJoinedScoresLabels(
            Iterable<String[]> scoresCsv,
            int[] scoresKeyColumns,
            int scoresColumn,
            Iterable<String[]> labelsCsv,
            int[] labelsKeyColumns,
            int labelsColumn,
            String positiveLabel) {

        // Check arguments
        if (scoresKeyColumns.length != labelsKeyColumns.length) {
            throw new IllegalArgumentException("There must be the same number of key columns for both the scores and labels.");
        }

        // Set up the join
        int[][] keys = new int[scoresKeyColumns.length][2];
        int[][] fields = new int[scoresKeyColumns.length + 2][2];
        int keyIndex;
        for (keyIndex = 0; keyIndex < scoresKeyColumns.length; keyIndex++) {
            // Match keys
            keys[keyIndex][0] = scoresKeyColumns[keyIndex];
            keys[keyIndex][1] = labelsKeyColumns[keyIndex];
            // Assemble fields
            fields[keyIndex][0] = 0;
            fields[keyIndex][1] = scoresKeyColumns[keyIndex];
        }
        // Assemble remaining fields
        fields[keyIndex][0] = 0;
        fields[keyIndex][1] = scoresColumn;
        keyIndex++;
        fields[keyIndex][0] = 1;
        fields[keyIndex][1] = labelsColumn;

        // Do the join
        List<String[]> scoresLabelsRows =
            CsvProcessing.mergeJoin(keys, fields, scoresCsv, labelsCsv);

        // Call the separate, in-order curve builder
        return buildCurveFromSeparateScoresLabels(
            scoresLabelsRows, fields.length - 2,
            scoresLabelsRows, fields.length - 1,
            positiveLabel);
    }

    public static Curve buildCurveFromSeparateScoresLabels(
            Iterable<String[]> scoresCsv,
            int scoresColumn,
            Iterable<String[]> labelsCsv,
            int labelsColumn,
            String positiveLabel) {

        // Project the scores and labels
        Iterable<Double> scoresIterator =
            new StringToDoubleConversionIterator(
                new ProjectionIterator<String>(scoresCsv,
                                               scoresColumn));
        Iterable<String> labelsIterator =
            new ProjectionIterator<String>(labelsCsv,
                                           labelsColumn);

        // Build and return the curve
        return new Curve.Builder<Double, String>()
            .predicteds(scoresIterator)
            .actuals(labelsIterator)
            .positiveLabel(positiveLabel)
            .build();
    }

    public static class ProjectionIterator<E>
        implements Iterator<E>, Iterable<E> {

        private Iterator<E[]> iterator;
        private int column;

        public ProjectionIterator(Iterable<E[]> table, int column) {
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

        public Iterator<E> iterator() {
            return this;
        }
    }

    public static class StringToDoubleConversionIterator
        implements Iterator<Double>, Iterable<Double> {

        private Iterator<String> iterator;

        public StringToDoubleConversionIterator(Iterable<String> iterable) {
            iterator = iterable.iterator();
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

        public Iterator<Double> iterator() {
            return this;
        }
    }

    public static void printReport(Curve curve, PrintWriter output /* TODO make report object to handle what to include and what format */) {
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
