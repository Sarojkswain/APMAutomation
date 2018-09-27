/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.automation.utils.mainframe.sysview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an easy way of interacting with information from SYSVIEW panels that use the tabular
 * format for its data.
 */
public class TabularData {
    private static final Logger logger = LoggerFactory.getLogger(Sysview.class);

    private final Set<String> columns;
    private List<Map<String, String>> rows = new ArrayList<>();
    private List<String> messages = null;

    /**
     * Constructor.
     * Uses an empty column set.
     */
    protected TabularData() {
        this(Collections.<String>emptySet());
    }

    /**
     * Constructor.
     *
     * @param columns Set of columns.
     */
    protected TabularData(Set<String> columns) {
        this.columns = columns;
    }

    /**
     * Adds a row of data.
     *
     * @param row Row data.
     */
    protected synchronized void addRow(Map<String, String> row) {
        assert row.size() == columns.size();

        rows.add(row);
    }

    /**
     * Returns a set of all the column names contained in the data.
     *
     * @return Column names.
     */
    public Set<String> getColumns() {
        return columns;
    }

    /**
     * Returns the first row where the value in the specified column matches the specified value.
     * The match is not case sensitive.
     *
     * @param column Column to match.
     * @param value Column value to match.
     * @return Data row if one is matched, null otherwise.
     */
    public synchronized Map<String, String> getFirstRowMatching(String column, String value) {
        Args.check(columns.contains(column), "Unknown column '" + column + "'");

        for (Map<String, String> row : rows) {
            if (row.get(column).compareToIgnoreCase(value) == 0) {
                return row;
            }
        }

        return null;
    }

    public synchronized List<Map<String, String>> getAllRows() {
        return rows;
    }

    /**
     * Returns all the rows where the value in the specified column matches the specified value.
     * The match is not case sensitive.
     *
     * @param column Column to match.
     * @param value Column value to match.
     * @return List of matching data rows.
     */
    public synchronized List<Map<String, String>> getAllRowsMatching(String column, String value) {
        Args.check(columns.contains(column), "Unknown column '" + column + "'");

        List<Map<String, String>> matchingRows = new ArrayList<>();

        for (Map<String, String> row : rows) {
            if (row.get(column).compareToIgnoreCase(value) == 0) {
                matchingRows.add(row);
            }
        }

        return matchingRows;
    }

    /**
     * Returns all the rows matching the specified include and exclude filters.
     * Each filter consists of a column name (key), and a regular expression pattern (value) that is
     * used to match values in the corresponding column for inclusion or exclusion.
     *
     * If at least one inclusion filter is specified then only rows that match an inclusion filter
     * and do not match any exclusion filters are returned. If no inclusion filter is specified then
     * all rows that do not match any exclusion filters are returned.
     *
     * @param include Inclusion filters.
     * @param exclude Exclusion filters.
     * @return Rows matching the specified filtering criteria.
     */
    public synchronized List<Map<String, String>> getAllRowsMatching(Map<String, Pattern> include, Map<String, Pattern> exclude) {
        List<Map<String, String>> matchingRows = new ArrayList<>();

        for (Map<String, String> row : rows) {
            boolean add = (include == null || include.isEmpty());

            if (include != null && !include.isEmpty()) {
                for (Map.Entry<String, Pattern> filter : include.entrySet()) {
                    final String column = filter.getKey();
                    final Pattern pattern = filter.getValue();

                    if (!columns.contains(column)) {
                        logger.warn("Ignoring unknown column in filter: {}", column);
                        continue;
                    }

                    if (pattern.matcher(row.get(column)).matches()) {
                        add = true;
                        break;
                    }
                }
            }

            if (add && exclude != null && !exclude.isEmpty()) {
                for (Map.Entry<String, Pattern> filter : exclude.entrySet()) {
                    final String column = filter.getKey();
                    final Pattern pattern = filter.getValue();

                    if (!columns.contains(column)) {
                        logger.warn("Ignoring unknown column in filter: {}", column);
                        continue;
                    }

                    if (pattern.matcher(row.get(column)).matches()) {
                        add = false;
                        break;
                    }
                }
            }

            if (add) {
                matchingRows.add(row);
            }
        }

        return matchingRows;
    }

    /**
     * Returns all of the messages included in the output by SYSVIEW.
     *
     * @return Messages.
     */
    public List<String> getMessages() {
        return messages;
    }

    /**
     * Constructs an instance from the output of a SYSVIEW command executed through the REXX
     * interface.
     *
     * @param commandOutput Stream with output of a SYSVIEW command.
     * @return Tabular data.
     * @throws IOException If there is a problem when reading or parsing the command output stream.
     */
    static TabularData fromRexxCommandOutput(List<String> commandOutput) throws IOException {
        TabularData td = null;
        Map<String, int[]> format = new HashMap<>();
        List<String> messages = new ArrayList<>();
        char delimiter = ' ';
        boolean rawOutput = false;

        // Because some column values in the output can be empty we can't simply split lines
        // based on a delimiter, instead we have to parse the values based on the widths of each
        // columns header.
        for (String line : commandOutput) {
            if (line.isEmpty()) {
                continue;
            }

            // The general format of each output line is: <type> <line-data>.
            // Where <type> is a single character that indicates the type of data on the line.
            switch (line.charAt(0)) {
                case 'M': // Message
                    messages.add(line.substring(1));
                    break;

                case 'I': // Info
                    // We have no use for these at the moment.
                    break;

                case 'T': // Title
                    // The second character in the Title line contains the delimiter used to
                    // separate column values. Since we have to support situations where the
                    // delimiter is a white space (indistinguishable from column data) the
                    // parsing algorithm can't rely on the delimiters so we instead normalize
                    // the output by replacing the delimiter with a space.
                    delimiter = line.charAt(1);
                    break;

                case 'H': // Header
                    if (td != null) {
                        throw new IOException("Malformed data: received two header lines");
                    }
                    // Strip line-type prefix.
                    line = line.substring(1);

                    // Parse the header line saving the labels and the start/end indexes of the
                    // column which is later used when parsing data lines.
                    int start;
                    int end = 0;
                    String label;
                    while (end >= 0 && end < line.length()) {
                        end++; // Skip delimiter
                        start = end;

                        // Column label
                        while (end < line.length() && line.charAt(end) != delimiter) {
                            ++end;
                        }
                        label = line.substring(start, Math.min(end, line.length())).trim();

                        // The width of the last column cannot be used as information making the
                        // end index useless. Instead we use the special value -1 that indicates
                        // 'until the end' to the data-parsing algorithm.
                        if (end >= line.length()) {
                            end = -1;
                        }

                        format.put(label, new int[] {start, end});
                    }

                    if (format.isEmpty()) {
                        throw new IOException("Malformed data: data contains no columns");
                    }

                    td = new TabularData(format.keySet());
                    break;

                default: // Unknown - might be data
                    if (!rawOutput) {
                        logger.warn("Unknown line type: " + line);
                        break;
                    }

                    // In case of raw data there are situations where an output line has no line
                    // type prefix. For now we 'solve' this by faking the prefix and processing
                    // it like any other data line.
                    line = "D " + line;
                case 'D': // Data
                    if (td == null) {
                        // For some screens with raw data SYSVIEW doesn't give us the header in
                        // which case we put all the data under one field called 'data'.
                        format.put("data", new int[] {1, -1});
                        td = new TabularData(format.keySet());
                        rawOutput = true;
                    }

                    // Strip line-type prefix.
                    line = line.substring(1);

                    // Silently ignore empty lines
                    if (line.isEmpty()) {
                        break;
                    }

                    // Parse the data line according to the format of the header line.
                    Map<String, String> row = new HashMap<>();
                    for (Entry<String, int[]> column : format.entrySet()) {
                        int[] indexes = column.getValue();
                        String value;

                        try {
                            if (indexes[1] == -1 || indexes[1] >= line.length()) {
                                // Only the start index is in or this is the last column,
                                // substring until end of line
                                value = line.substring(indexes[0]).trim();
                            } else {
                                // Both indexes are in, simple substring
                                value = line.substring(indexes[0], indexes[1]).trim();
                            }

                            row.put(column.getKey(), value);
                        } catch (StringIndexOutOfBoundsException e) {
                            logger.warn("Ignoring column '{}' on line '{}'", column.getKey(), line);
                        }
                    }

                    if (!row.isEmpty()) {
                        td.addRow(row);
                    }
                    break;
            }
        }

        if (td == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No identifiable column/data found, raw output follows:");
                for (String line : commandOutput) {
                    logger.debug("{}", line);
                }
            }

            // In case we weren't able to get any column/row data from the output we create an
            // 'empty' instance so that we can still pass back any messages.
            td = new TabularData();
        }

        td.messages = messages;

        return td;
    }

    @Override
    public String toString() {
        return getColumns().toString() + "=>" + getAllRows().toString();
    }
}
