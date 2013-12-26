/*
 * Copyright (c) 2013 Roc Project.  This is free software.  See
 * LICENSE.txt for details.
 */

package mloss.roc.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * TODO To be replaced by a proper CSV reader at some point.
 */
public class NaiveCsvReader {
    private BufferedReader input;
    private String delimiter;

    public NaiveCsvReader(BufferedReader input, String delimiter) {
        this.input = input;
        this.delimiter = delimiter;
    }

    public NaiveCsvReader(BufferedReader input) {
        this(input, ",");
    }

    public List<String[]> readAll() throws IOException {
        String line;
        List<String[]> lines = new LinkedList<String[]>();
        Pattern splitPattern = Pattern.compile("\\Q" + delimiter + "\\E");
        while ((line = input.readLine()) != null) {
            // -1: include trailing empty fields
            String[] fields = splitPattern.split(line, -1);
            lines.add(fields);
        }
        return lines;
    }
}
