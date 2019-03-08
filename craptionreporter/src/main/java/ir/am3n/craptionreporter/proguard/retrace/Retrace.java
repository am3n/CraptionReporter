/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2017 Eric Lafortune @ GuardSquare
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package ir.am3n.craptionreporter.proguard.retrace;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import ir.am3n.craptionreporter.proguard.obfuscate.MappingReader;

public class Retrace {

    public static final String STACK_TRACE_EXPRESSION = "(?:.*?\\bat\\s+%c\\.%m\\s*\\(%s(?::%l)?\\)\\s*(?:~\\[.*\\])?)|(?:(?:.*?[:\"]\\s+)?%c(?::.*)?)";
    private File mappingFile;
    private String stackTrace;
    private String regularExpression = STACK_TRACE_EXPRESSION;
    private boolean verbose = false;

    /**
     * Creates a new ReTrace_ForJava instance.
     * @param regularExpression the regular expression for parsing the lines in
     *                          the stack trace.
     * @param verbose           specifies whether the de-obfuscated stack trace
     */
    public Retrace(File mappingFile, String stackTrace, String regularExpression, boolean verbose) {
        this.mappingFile = mappingFile;
        this.stackTrace = stackTrace;
        this.regularExpression = regularExpression;
        this.verbose = verbose;
    }

    public String retrace() {

        try {
            // Open the input stack trace. We're always using the UTF-8
            // character encoding, even for reading from the standard
            // input.

            InputStream inputStream = new ByteArrayInputStream(stackTrace.getBytes("UTF-8"));
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            LineNumberReader stackTraceReader = new LineNumberReader(new BufferedReader(inputStreamReader));

            // Open the output stack trace, again using UTF-8 encoding.
            StringBuilder stackTraceWriter = new StringBuilder();

            try {

                // Create a pattern for stack frames.
                FramePattern pattern = new FramePattern(regularExpression, verbose);

                // Create a remapper.
                FrameRemapper mapper = new FrameRemapper();

                // Read the mapping file.
                MappingReader mappingReader = new MappingReader(mappingFile);
                mappingReader.pump(mapper);

                // Read and process the lines of the stack trace.
                while (true) {
                    // Read a line.
                    String obfuscatedLine = stackTraceReader.readLine();
                    if (obfuscatedLine == null) {
                        break;
                    }

                    // Try to match it against the regular expression.
                    FrameInfo obfuscatedFrame = pattern.parse(obfuscatedLine);
                    if (obfuscatedFrame != null) {
                        // Transform the obfuscated frame back to one or more
                        // original frames.
                        Iterator<FrameInfo> retracedFrames = mapper.transform(obfuscatedFrame).iterator();

                        String previousLine = null;

                        while (retracedFrames.hasNext()) {
                            // Retrieve the next retraced frame.
                            FrameInfo retracedFrame = retracedFrames.next();

                            // Format the retraced line.
                            String retracedLine = pattern.format(obfuscatedLine, retracedFrame);

                            // Clear the common first part of ambiguous alternative
                            // retraced lines, to present a cleaner list of
                            // alternatives.
                            String trimmedLine = previousLine != null && obfuscatedFrame.getLineNumber() == 0
                                    ? trim(retracedLine, previousLine)
                                    : retracedLine;

                            // Print out the retraced line.
                            if (trimmedLine != null) {
                                stackTraceWriter.append(trimmedLine).append("\r\n");
                            }

                            previousLine = retracedLine;
                        }
                    } else {
                        // Print out the original line.
                        stackTraceWriter.append(obfuscatedLine).append("\r\n");
                    }
                }

                return stackTraceWriter.toString();

            } finally {
                stackTraceReader.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "";
    }


    /**
     * Returns the first given string, with any leading characters that it has
     * in common with the second string replaced by spaces.
     */
    private String trim(String string1, String string2) {
        StringBuilder line = new StringBuilder(string1);

        // Find the common part.
        int trimEnd = firstNonCommonIndex(string1, string2);
        if (trimEnd == string1.length()) {
            return null;
        }

        // Don't clear the last identifier characters.
        trimEnd = lastNonIdentifierIndex(string1, trimEnd) + 1;

        // Clear the common characters.
        for (int index = 0; index < trimEnd; index++) {
            if (!Character.isWhitespace(string1.charAt(index))) {
                line.setCharAt(index, ' ');
            }
        }

        return line.toString();
    }


    /**
     * Returns the index of the first character that is not the same in both
     * given strings.
     */
    private int firstNonCommonIndex(String string1, String string2) {
        int index = 0;
        while (index < string1.length() &&
                index < string2.length() &&
                string1.charAt(index) == string2.charAt(index)) {
            index++;
        }

        return index;
    }


    /**
     * Returns the index of the last character that is not an identifier
     * character in the given string, at or before the given index.
     */
    private int lastNonIdentifierIndex(String line, int index) {
        while (index >= 0 &&
                Character.isJavaIdentifierPart(line.charAt(index))) {
            index--;
        }

        return index;
    }

}