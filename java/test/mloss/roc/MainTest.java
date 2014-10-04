/*
 * Copyright (c) 2014 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;


/** Tests {@link Main}. */
public class MainTest {

    Main main;
    BufferedReader input;
    StringWriter outputString;
    PrintWriter output;
    StringWriter errorString;
    PrintWriter error;

    public void makeMain(String inputText) {
        input = new BufferedReader(new StringReader(inputText));
        outputString = new StringWriter();
        output = new PrintWriter(outputString, true);
        errorString = new StringWriter();
        error = new PrintWriter(errorString, true);
        main = new Main(input, output, error);
    }

    @Test
    public void run_help()
        throws Main.Exception, FileNotFoundException, IOException {

        // Matcher for help output.  Just checks the headers.
        Matcher<String> helpMatcher = allOf(
            containsString("Roc"),
            containsString("Everything ROC and PR curves."),
            containsString("SYNOPSIS"),
            containsString("DESCRIPTION"),
            containsString("OPTIONS"),
            containsString("EXAMPLES"));

        // Check a few variations of getting help
        String[][] helpCmds = {
            {"-h"}, {"-H"}, {"-?"},
            {"help"}, {"-help"}, {"--help"}, {"HELP"},
            // Help overrides all other options
            {"--debug", "--about", "--license", "--scores", "--labels", "help"},
        };
        for (String[] cryHelp : helpCmds) {
            makeMain("");
            main.run(cryHelp);
            // Help output goes to regular output
            assertThat(outputString.toString(), helpMatcher);
            assertEquals("", errorString.toString());
        }
    }

    @Test
    public void run_about()
        throws Main.Exception, FileNotFoundException, IOException {

        // Matcher for about output
        @SuppressWarnings("unchecked") // Type inference shortcoming?
        Matcher<String> matcher = allOf(
            containsString(MetaInfo.NAME),
            containsString(MetaInfo.VERSION_STRING),
            containsString(MetaInfo.TAGLINE),
            containsString(MetaInfo.URL),
            containsString(MetaInfo.COPYRIGHT_NOTICE),
            containsString("free, open source software"),
            containsString(MetaInfo.LICENSE_NAME));

        String[] cmd = {"--about"};
        makeMain("");
        main.run(cmd);
        // Informational output goes to regular output
        assertThat(outputString.toString(), matcher);
        assertEquals("", errorString.toString());
    }

    @Test
    public void run_version()
        throws Main.Exception, FileNotFoundException, IOException {

        String[] cmd = {"--version"};
        makeMain("");
        main.run(cmd);
        // Informational output goes to regular output
        assertEquals(MetaInfo.NAME + " " + MetaInfo.VERSION_STRING + "\n", outputString.toString());
        assertEquals("", errorString.toString());
    }

    // Scores and labels together
    public static final String scrsLblsCsv =
        "0.54803549918305260,1\n" +
        "0.03331158540273116,0\n" +
        "0.99309114770899720,0\n" +
        "0.44406440779686673,0\n" +
        "0.96616658885080790,1\n" +
        "0.73243831826076260,1\n" +
        "0.92764889482674350,0\n" +
        "0.20053631827008822,1\n" +
        "0.96599717171711140,0\n" +
        "0.51474884028057590,1\n";

    // Scores only.  Key is row index.
    public static final String scrsCsv =
        "0.54803549918305260\n" +
        "0.03331158540273116\n" +
        "0.99309114770899720\n" +
        "0.44406440779686673\n" +
        "0.96616658885080790\n" +
        "0.73243831826076260\n" +
        "0.92764889482674350\n" +
        "0.20053631827008822\n" +
        "0.96599717171711140\n" +
        "0.51474884028057590\n";

    // Labels only.  Key is row index.
    public static final String lblsCsv =
        "1\n" +
        "0\n" +
        "0\n" +
        "0\n" +
        "1\n" +
        "1\n" +
        "0\n" +
        "1\n" +
        "0\n" +
        "1\n";

    // Compound keys and scores.  Key is columns 3 and 1.  Score is
    // column 2.
    public static final String keysScrsCsv =
        "z,0.54803549918305260,b\n" +
        "y,0.03331158540273116,b\n" +
        "c,0.99309114770899720,b\n" +
        "w,0.44406440779686673,c\n" +
        "r,0.96616658885080790,b\n" +
        "q,0.73243831826076260,c\n" +
        "s,0.92764889482674350,a\n" +
        "e,0.20053631827008822,a\n" +
        "b,0.96599717171711140,a\n" +
        "h,0.51474884028057590,a\n";

    // Compound keys and labels.  Key is columns 2 and 3.  Label is
    // column 1.
    public static final String keysLblsCsv =
        "1,b,z\n" +
        "0,b,y\n" +
        "0,b,c\n" +
        "0,c,w\n" +
        "1,b,r\n" +
        "1,c,q\n" +
        "0,a,s\n" +
        "1,a,e\n" +
        "0,a,b\n" +
        "1,a,h\n";

    // Scores and labels together.  Score is column 2.  Label is column
    // 5.  Key is columns 1 and 3 but unnecessary.
    public static final String keysScrsLblsCsv =
        "z,0.54803549918305260,b,,1,y,\n" +
        "y,0.03331158540273116,b,,0,n,\n" +
        "c,0.99309114770899720,b,,0,n,\n" +
        "w,0.44406440779686673,c,,0,n,\n" +
        "r,0.96616658885080790,b,,1,y,\n" +
        "q,0.73243831826076260,c,,1,y,\n" +
        "s,0.92764889482674350,a,,0,n,\n" +
        "e,0.20053631827008822,a,,1,y,\n" +
        "b,0.96599717171711140,a,,0,n,\n" +
        "h,0.51474884028057590,a,,1,y,\n";

    public static File makeTempFile()
        throws IOException {

        File tmp = File.createTempFile("rocJunitTmp", "");
        tmp.deleteOnExit();
        return tmp;
    }

    public static File makeTempFileWithContents(String content)
        throws IOException {

        // Create a temporary file and put the contents in it
        File tmp = makeTempFile();
        PrintWriter output = new PrintWriter(tmp);
        output.write(content);
        output.close();
        return tmp;
    }

    public static String readFileAsString(File file)
        throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder builder = new StringBuilder(1000);
        String line = reader.readLine(); // Strips EOLs
        while (line != null) {
            builder.append(line);
            builder.append("\n");
            line = reader.readLine();
        }
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public static final Matcher<String> yamlMatcher = allOf(
        containsString("%YAML 1.1"),
        containsString("---"),
        containsString("ROC area: 0.4"),
        containsString("ROC points count: 11"),
        containsString("ROC points:"),
        containsString("PR area: 0."),
        containsString("PR points count: 11"),
        containsString("PR points:"),
        containsString("..."));

    @Test
    public void run_rankedLabels()
        throws Main.Exception, FileNotFoundException, IOException {

        File labelsFile = makeTempFileWithContents(lblsCsv);
        String[] cmd = {
            "--labels", labelsFile.getAbsolutePath(),
        };
        makeMain("");
        main.run(cmd);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), yamlMatcher);
    }

    @Test
    public void run_scoresLabelsTogether()
        throws Main.Exception, FileNotFoundException, IOException {

        File scoreslabelsFile = makeTempFileWithContents(scrsLblsCsv);
        String[] cmd = {
            "--scores-labels", scoreslabelsFile.getAbsolutePath(),
        };
        makeMain("");
        main.run(cmd);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), yamlMatcher);
    }

    @Test
    public void run_scoresLabelsTogetherWithColumns()
        throws Main.Exception, FileNotFoundException, IOException {

        File scoreslabelsFile = makeTempFileWithContents(keysScrsLblsCsv);
        String[] cmd = {
            "--scores-labels", scoreslabelsFile.getAbsolutePath(),
            "--labels-column", "5",
            "--scores-column", "2",
        };
        makeMain("");
        main.run(cmd);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), yamlMatcher);
    }

    @Test
    public void run_separateScoresLabelsInOrder()
        throws Main.Exception, FileNotFoundException, IOException {

        File scoresFile = makeTempFileWithContents(scrsCsv);
        File labelsFile = makeTempFileWithContents(lblsCsv);
        String[] cmd = {
            "--scores", scoresFile.getAbsolutePath(),
            "--labels", labelsFile.getAbsolutePath(),
        };
        makeMain("");
        main.run(cmd);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), yamlMatcher);
    }

    @Test
    public void run_separateScoresLabelsInOrderWithColumns()
        throws Main.Exception, FileNotFoundException, IOException {

        File scoresFile = makeTempFileWithContents(keysScrsCsv);
        File labelsFile = makeTempFileWithContents(keysLblsCsv);
        String[] cmd = {
            "--scores", scoresFile.getAbsolutePath(),
            "--scores-column", "2",
            "--labels", labelsFile.getAbsolutePath(),
            "--labels-column", "1",
        };
        makeMain("");
        main.run(cmd);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), yamlMatcher);
    }

    @Test
    public void run_joinedScoresLabels()
        throws Main.Exception, FileNotFoundException, IOException {

        File scoresFile = makeTempFileWithContents(keysScrsCsv);
        File labelsFile = makeTempFileWithContents(keysLblsCsv);
        String[] cmd = {
            "--scores", scoresFile.getAbsolutePath(),
            "--scores-key", "3,1",
            "--scores-column", "2",
            "--labels", labelsFile.getAbsolutePath(),
            "--labels-key", "2,3",
            "--labels-column", "1",
        };
        makeMain("");
        main.run(cmd);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), yamlMatcher);
    }

    @Test
    public void run_noArgs()
        throws Main.Exception, FileNotFoundException, IOException {

        String[] cmd = {};
        makeMain(scrsLblsCsv);
        main.run(cmd);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), yamlMatcher);
    }

    @Test
    public void run_emptyInput()
        throws Main.Exception, FileNotFoundException, IOException {

        String[] cmd = {};
        makeMain("");
        try {
            main.run(cmd);
            fail("Exception not thrown for empty input");
        } catch (Main.Exception e) {
            assertThat(e.getMessage(), containsString("Empty input"));
        }
        assertEquals("", outputString.toString());
        assertEquals("", errorString.toString());
    }

    @Test
    public void run_positiveLabel()
        throws Main.Exception, FileNotFoundException, IOException {

        String[] cmd = {
            "--scores-labels", "-",
            "--positive", "y",
            "--labels-column", "6",
            "--scores-column", "2",
        };
        makeMain(keysScrsLblsCsv);
        main.run(cmd);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), yamlMatcher);
    }

    public static final Matcher<String> rocPtsMatcher = allOf(
        // Just pick some basic/easy points to check
        containsString("0.0 0.0"),
        containsString("0.2 0.2"),
        containsString("0.6 0.6"),
        containsString("0.8 0.8"),
        containsString("1.0 1.0"));

    public static final Matcher<String> rocAreaMatcher =
        containsString("0.44");

    public static final Matcher<String> prAreaMatcher =
        containsString("0.417");

    public static final Matcher<String> rocPrAreasMatcher =
        allOf(rocAreaMatcher, prAreaMatcher);

    @Test
    public void run_reportRocPoints()
        throws Main.Exception, FileNotFoundException, IOException {

        String[] cmd = {
            "--scores-labels", "-",
            "--report", "rocpts",
            "--to", "-",
            "--scores-column", "2",
            "--labels-column", "5",
        };
        makeMain(keysScrsLblsCsv);
        main.run(cmd);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), rocPtsMatcher);
    }

    @Test
    public void run_singleReport()
        throws Main.Exception, FileNotFoundException, IOException {

        String[] cmd = {
            "--scores-labels", "-",
            "--report", "rocArea",
            "--to", "-",
        };
        makeMain(scrsLblsCsv);
        main.run(cmd);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), rocAreaMatcher);
    }

    @Test
    public void run_reportDefaultOutput()
        throws Main.Exception, FileNotFoundException, IOException {

        String[] cmd = {
            "--scores-labels", "-",
            "--report", "rocArea",
        };
        makeMain(scrsLblsCsv);
        main.run(cmd);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), rocAreaMatcher);
    }

    @Ignore
    @Test
    public void run_reportDefaultInput()
        throws Main.Exception, FileNotFoundException, IOException {

        String[] cmd = {
            "--report", "rocArea",
            "--to", "-",
        };
        makeMain(scrsLblsCsv);
        main.run(cmd);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), rocAreaMatcher);
    }

    @Test
    public void run_multipleReportsMultipleOptions()
        throws Main.Exception, FileNotFoundException, IOException {

        String[] cmd = {
            "--scores-labels", "-",
            "--report", "rocArea",
            "--report", "prArea",
            "--to", "-",
        };
        makeMain(scrsLblsCsv);
        main.run(cmd);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), rocPrAreasMatcher);
    }

    @Test
    public void run_multipleReportsSingleOption()
        throws Main.Exception, FileNotFoundException, IOException {

        String[] cmd = {
            "--scores-labels", "-",
            "--report", "rocArea,prArea",
            "--to", "-",
        };
        makeMain(scrsLblsCsv);
        main.run(cmd);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), rocPrAreasMatcher);
    }

    @Test
    public void run_reportsToMultipleFiles()
        throws Main.Exception, FileNotFoundException, IOException {

        File rocAreaFile = makeTempFile();
        File prAreaFile = makeTempFile();
        File areasFile = makeTempFile();
        String[] cmd = {
            "--scores-labels", "-",
            "--report", "rocArea",
            "--to", rocAreaFile.getAbsolutePath(),
            "--report", "prArea",
            "--to", prAreaFile.getAbsolutePath(),
            "--report", "rocArea,prArea",
            "--to", areasFile.getAbsolutePath(),
        };
        makeMain(scrsLblsCsv);
        main.run(cmd);
        assertEquals("", errorString.toString());
        assertEquals("", outputString.toString());
        assertThat(readFileAsString(rocAreaFile), rocAreaMatcher);
        assertThat(readFileAsString(prAreaFile), prAreaMatcher);
        assertThat(readFileAsString(areasFile), rocPrAreasMatcher);
    }

    @Test
    public void run_toFileWithoutReport()
        throws Main.Exception, FileNotFoundException, IOException {

        String[] cmd = {
            "--scores-labels", "-",
            "--to", "-",
            "--report", "all",
        };
        makeMain(scrsLblsCsv);
        try {
            main.run(cmd);
            fail("Exception not thrown for no '--report' before '--to'");
        } catch (Main.Exception e) {
            assertThat(e.getMessage(), containsString("No report options precede output file option"));
        }
        assertEquals("", errorString.toString());
        assertEquals("", outputString.toString());
    }

    // TODO test output file given multiple times

    // TODO test delimiter option
}
