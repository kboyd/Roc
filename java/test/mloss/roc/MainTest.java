/*
 * Copyright (c) 2014 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;


/** Tests {@link Main}. */
public class MainTest {

    BufferedReader input;
    StringWriter outputString;
    PrintWriter output;
    StringWriter errorString;
    PrintWriter error;

    public void setUpIo(String source) {
        input = new BufferedReader(new StringReader(source));
        outputString = new StringWriter();
        output = new PrintWriter(outputString, true);
        errorString = new StringWriter();
        error = new PrintWriter(errorString, true);
    }

    @Test
    public void apiMain_help()
        throws Main.MainException, FileNotFoundException, IOException {

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
            setUpIo("");
            Main.apiMain(cryHelp, input, output, error);
            // Help output goes to error so regular output should be empty
            assertEquals("", outputString.toString());
            assertThat(errorString.toString(), helpMatcher);
        }
    }

    @Test
    public void apiMain_aboutVersion()
        throws Main.MainException, FileNotFoundException, IOException {

        // Matcher for about/version output
        Matcher<String> matcher = allOf(
            containsString("Roc"),
            containsString("ROC and PR curves"),
            containsString("Copyright"),
            containsString("free software"),
            containsString("license"),
            containsString("github.com/kboyd/Roc"));

        // Check both about and version
        String[][] cmds = {{"--about"}, {"--version"}};
        for (String[] cmd : cmds) {
            setUpIo("");
            Main.apiMain(cmd, input, output, error);
            // Informational output goes to error so regular output should be empty
            assertEquals("", outputString.toString());
            assertThat(errorString.toString(), matcher);
        }
    }

    @Test
    public void apiMain_license()
        throws Main.MainException, FileNotFoundException, IOException {

        // Matcher for license output
        Matcher<String> matcher = allOf(
            containsString("Roc"),
            containsString("free"),
            containsString("open source"),
            containsString("BSD"),
            containsString("license"));

        // Check both about and version
        String[] cmd = {"--license"};
        setUpIo("");
        Main.apiMain(cmd, input, output, error);
        // Informational output goes to error so regular output should be empty
        assertEquals("", outputString.toString());
        assertThat(errorString.toString(), matcher);
    }

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

    public static final String keysScrsCsvOpts = "{k:[3,1],s:2}";
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

    public static final String keysLblsCsvOpts = "{l:1,k:[2,3]}";
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

    public static final String keysScrsLblsCsvOpts = "{l:5,s:2}";
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

    public static String makeTempFileWithContents(String content)
        throws FileNotFoundException, IOException {

        // Create temporary file
        File tmp = File.createTempFile("rocJunitTmp", "");
        tmp.deleteOnExit();
        // Put the contents in the temp file
        PrintWriter output = new PrintWriter(tmp);
        output.write(content);
        output.close();
        // Return the name of the temp file
        return tmp.getAbsolutePath();
    }

    @SuppressWarnings("unchecked")
    public static final Matcher<String> yamlMatcher = allOf(
        containsString("%YAML 1.1"),
        containsString("---"),
        containsString("ROC area: 0.4"),
        containsString("ROC points count: 11"),
        containsString("ROC points:"),
        containsString("ROC points Gnuplot text: |"),
        containsString("# End ROC points Gnuplot text"),
        containsString("..."));

    @Test
    public void apiMain_rankedLabels()
        throws Main.MainException, FileNotFoundException, IOException {

        String labelsFileName = makeTempFileWithContents(lblsCsv);
        String[] cmd = {"--labels", labelsFileName};
        setUpIo("");
        Main.apiMain(cmd, input, output, error);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), yamlMatcher);
    }

    @Test
    public void apiMain_scoresLabelsTogether()
        throws Main.MainException, FileNotFoundException, IOException {

        String scoreslabelsFileName = makeTempFileWithContents(scrsLblsCsv);
        String[] cmd = {"--scores-labels", scoreslabelsFileName};
        setUpIo("");
        Main.apiMain(cmd, input, output, error);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), yamlMatcher);
    }

    @Test
    public void apiMain_scoresLabelsTogetherWithColumns()
        throws Main.MainException, FileNotFoundException, IOException {

        String scoreslabelsFileName = makeTempFileWithContents(keysScrsLblsCsv);
        String[] cmd = {
            "--scores-labels",
            scoreslabelsFileName + ":" + keysScrsLblsCsvOpts,
        };
        setUpIo("");
        Main.apiMain(cmd, input, output, error);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), yamlMatcher);
    }

    @Test
    public void apiMain_separateScoresLabelsInOrder()
        throws Main.MainException, FileNotFoundException, IOException {

        String scoresFileName = makeTempFileWithContents(scrsCsv);
        String labelsFileName = makeTempFileWithContents(lblsCsv);
        String[] cmd = {"--scores", scoresFileName, "--labels", labelsFileName};
        setUpIo("");
        Main.apiMain(cmd, input, output, error);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), yamlMatcher);
    }

    @Test
    public void apiMain_separateScoresLabelsInOrderWithColumns()
        throws Main.MainException, FileNotFoundException, IOException {

        String scoresFileName = makeTempFileWithContents(keysScrsCsv);
        String labelsFileName = makeTempFileWithContents(keysLblsCsv);
        String[] cmd = {
            "--scores",
            scoresFileName + ":{s:2}",
            "--labels",
            labelsFileName + ":{l:1}",
        };
        setUpIo("");
        Main.apiMain(cmd, input, output, error);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), yamlMatcher);
    }

    @Test
    public void apiMain_joinedScoresLabels()
        throws Main.MainException, FileNotFoundException, IOException {

        String scoresFileName = makeTempFileWithContents(keysScrsCsv);
        String labelsFileName = makeTempFileWithContents(keysLblsCsv);
        String[] cmd = {
            "--scores",
            scoresFileName + ":" + keysScrsCsvOpts,
            "--labels",
            labelsFileName + ":" + keysLblsCsvOpts,
        };
        setUpIo("");
        Main.apiMain(cmd, input, output, error);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), yamlMatcher);
    }

    @Test
    public void apiMain_noArgs()
        throws Main.MainException, FileNotFoundException, IOException {

        String[] cmd = {};
        setUpIo(scrsLblsCsv);
        Main.apiMain(cmd, input, output, error);
        assertEquals("", errorString.toString());
        assertThat(outputString.toString(), yamlMatcher);
    }

    @Test
    public void apiMain_emptyInput()
        throws Main.MainException, FileNotFoundException, IOException {

        String[] cmd = {};
        setUpIo("");
        try {
            Main.apiMain(cmd, input, output, error);
            fail("Exception not thrown for empty input");
        } catch (Main.MainException e) {
            assertThat(e.getMessage(), containsString("Empty input"));
        }
        assertEquals("", outputString.toString());
        assertEquals("", errorString.toString());
    }
}
