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

package com.ca.apm.automation.action.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a heterogeneous set of metrics.
 */
public class MetricSet {
    final String agentName;
    Map<String, List<Metric<Double>>> numberMetrics = new HashMap<>();
    Map<String, List<Metric<String>>> textMetrics = new HashMap<>();
    Map<String, List<Metric<Date>>> dateMetrics = new HashMap<>();
    Set<String> uniquePaths = new HashSet<>();

    /**
     * Factory method used to generate a set of metrics from a single agent in a given time frame.
     * Uses {@link Clw} to query EM.
     *
     * @param clw Clw instance used to query EM.
     * @param agentName Agent name.
     * @param start Start of the queried time frame.
     * @param end End of the queried time frame.
     * @return MetricSet instance.
     */
    @NotNull
    public static MetricSet fromAgentMetrics(@NotNull Clw clw, @NotNull String agentName,
        @NotNull Calendar start, @NotNull Calendar end) {

        Args.notNull(clw, "clw");
        Args.notBlank(agentName, "agentName");
        Args.notNull(start, "start");
        Args.notNull(end, "end");

        MetricSet ms = new MetricSet(agentName);

        // Example: Tue Jul 21 15:17:00 CEST 2015
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy");
        for (Map<String, String> record : clw.getUniqueMetricsFromAgents(".*" + agentName, start, end)) {

            final String timestampString = record.get("Actual Start Timestamp");
            try {
                final String rsrc = record.get("Resource");
                final String path = rsrc + (rsrc.isEmpty() ? "" : "|") + record.get("MetricName");
                final String valueType = record.get("Value Type");
                final Date timestamp = formatter.parse(timestampString);
                switch (valueType) {
                    case "Long":
                    case "Integer":
                        ms.addNumberMetric(timestamp, path, Long.valueOf(record.get("Integer Value")));
                        break;

                    case "Float":
                        ms.addNumberMetric(timestamp, path, Double.valueOf(record.get("Float Value")));
                        break;

                    case "String":
                        ms.addTextMetric(timestamp, path, record.get("String Value"));
                        break;

                    case "Date":
                        final String dateValue = record.get("Date Value");
                        try {
                            ms.addDateMetric(timestamp, path, formatter.parse(dateValue));
                        } catch (ParseException e) {
                            throw new IllegalStateException("Failed to parse Date metric value: "
                                + dateValue, e);
                        }
                        break;

                    default:
                        throw new IllegalStateException("Unknown metric value type: " + valueType);
                }
            } catch (ParseException e) {
                throw new IllegalStateException("Failed to parse Start Timestamp metric value: "
                    + timestampString, e);
            }
        }

        return ms;
    }

    protected MetricSet(String agentName) {
        this.agentName = agentName;
    }

    /**
     * Returns a set of unique paths held by this instance.
     *
     * @return Set of unique metric paths.
     */
    @Nullable
    public synchronized Set<String> getUniquePaths() {
        return uniquePaths;
    }

    /**
     * Identifies whether a metric path is held by the instance and whether its type is Number.
     *
     * @param path Metric path.
     * @return true if included and its type is Number.
     */
    public boolean isNumber(String path) {
        return numberMetrics.containsKey(path);
    }

    /**
     * Identifies whether a metric path is held by the instance and whether its type is Text.
     *
     * @param path Metric path.
     * @return true if included and its type is Text.
     */
    public boolean isText(String path) {
        return textMetrics.containsKey(path);
    }

    /**
     * Identifies whether a metric path is held by the instance and whether its type is Date.
     *
     * @param path Metric path.
     * @return true if included and its type is Date.
     */
    public boolean isDate(String path) {
        return dateMetrics.containsKey(path);
    }

    /**
     * Returns a set of metric paths for each metric of type Number.
     *
     * @return Set of metric paths.
     */
    @Nullable
    public synchronized Set<String> getNumberMetrics() {
        return numberMetrics.keySet();
    }

    /**
     * Returns a set of metric paths for each metric of type Text.
     *
     * @return Set of metric paths.
     */
    @Nullable
    public synchronized Set<String> getTextMetrics() {
        return textMetrics.keySet();
    }

    /**
     * Returns a set of metric paths for each metric of type Date.
     *
     * @return Set of metric paths.
     */
    @Nullable
    public synchronized Set<String> getDateMetrics() {
        return dateMetrics.keySet();
    }

    /**
     * Returns a set of metric data points for a Number metric with the specified path.
     *
     * @param path Metric path.
     * @return Set of metric data points.
     */
    @Nullable
    public synchronized List<Metric<Double>> getNumberMetric(String path) {
        return numberMetrics.get(path);
    }

    /**
     * Returns a set of metric data points for a Text metric with the specified path.
     *
     * @param path Metric path.
     * @return Set of metric data points.
     */
    @Nullable
    public synchronized List<Metric<String>> getTextMetric(String path) {
        return textMetrics.get(path);
    }

    /**
     * Returns a set of metric data points for a Date metric with the specified path.
     *
     * @param path Metric path.
     * @return Set of metric data points.
     */
    @Nullable
    public synchronized List<Metric<Date>> getDateMetric(String path) {
        return dateMetrics.get(path);
    }

    /**
     * Adds a Number metric data point.
     *
     * @param timestamp Date and time of the data point.
     * @param path Metric path.
     * @param value Metric value.
     */
    public synchronized void addNumberMetric(Date timestamp, String path, double value) {
        if (isText(path) || isDate(path)) {
            throw new IllegalStateException("Inconsistent value type for metric path: " + path);
        }

        if (!isNumber(path)) {
            numberMetrics.put(path, new ArrayList<Metric<Double>>());
        }

        numberMetrics.get(path).add(new Metric<Double>(timestamp, path, value));
        uniquePaths.add(path);
    }

    /**
     * Adds a Text metric data point.
     *
     * @param timestamp Date and time of the data point.
     * @param path Metric path.
     * @param value Metric value.
     */
    public synchronized void addTextMetric(Date timestamp, String path, String value) {
        if (isNumber(path) || isDate(path)) {
            throw new IllegalStateException("Inconsistent value type for metric path: " + path);
        }

        if (!isText(path)) {
            textMetrics.put(path, new ArrayList<Metric<String>>());
        }

        textMetrics.get(path).add(new Metric<String>(timestamp, path, value));
        uniquePaths.add(path);
    }

    /**
     * Adds a Date metric data point.
     *
     * @param timestamp Date and time of the data point.
     * @param path Metric path.
     * @param value Metric value.
     */
    public synchronized void addDateMetric(Date timestamp, String path, Date value) {
        if (isNumber(path) || isText(path)) {
            throw new IllegalStateException("Inconsistent value type for metric path: " + path);
        }

        if (!isDate(path)) {
            dateMetrics.put(path, new ArrayList<Metric<Date>>());
        }

        dateMetrics.get(path).add(new Metric<Date>(timestamp, path, value));
        uniquePaths.add(path);
    }

    /**
     * Replaces any metric paths matching the provided pattern with the provided replacement string.
     * Capture groups can be used in the pattern and replacement the same way they can be used in
     * {@link Matcher#replaceAll(String)}.
     *
     * @param match Pattern to match.
     * @param replacement Replacement path.
     */
    public synchronized void updatePaths(Pattern match, String replacement) {
        Set<String> toUpdate = new HashSet<String>();
        for (String path : getUniquePaths()) {
            Matcher matcher = match.matcher(path);
            if (matcher.matches()) {
                toUpdate.add(path);
            }
        }

        for (String path : toUpdate) {
            Matcher matcher = match.matcher(path);
            String replaced = matcher.replaceAll(replacement);

            uniquePaths.add(replaced);
            uniquePaths.remove(path);

            if (isNumber(path)) {
                numberMetrics.put(replaced, getNumberMetric(path));
            } else if (isText(path)) {
                textMetrics.put(replaced, getTextMetric(path));
            } else if (isDate(path)) {
                dateMetrics.put(replaced, getDateMetric(path));
            } else {
                assert false;
            }

            removePath(path);
        }
    }

    /**
     * Removes a metric with the specified path from the instance.
     *
     * @param path Metric path.
     */
    public synchronized void removePath(String path) {
        uniquePaths.remove(path);
        numberMetrics.remove(path);
        textMetrics.remove(path);
        dateMetrics.remove(path);
    }

    /**
     * Removes all metric with paths matching those in the provided collection.
     *
     * @param paths Collection of metric paths.
     */
    public synchronized void removePaths(Collection<String> paths) {
        for (String path : paths) {
            removePath(path);
        }
    }
}
