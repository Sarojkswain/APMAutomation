/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.automation.action.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class to retrieve metrics by invoking CLW. Adapted from CODA and simplified. Extended with new methods as needed.
 */
public class MetricUtils2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricUtils2.class);

    private final ClwRunner2 clw;

    private static final Integer MIN = 1;

    private static final Integer MAX = 2;

    private static final Integer VALUE = 3;

    private static final Integer COUNT = 4;

    private static final Integer STRING = 19;

    private static final String[] ESCAPE_SYMBOLS = {"(", ")", ":", "?", "|"};

    public MetricUtils2(ClwRunner2 clw) {
        this.clw = clw;
    }

    /**
     * This methods extracts the latest value of the metric just before the execution of the command.
     *
     * @return a string value of the last metric value
     */
    public String getLastHistoricalDataFromAgents(String agentRegularExpression, String metricRegularExpression) {
        final List<String> temp = getHistoricalDataFromAgents(agentRegularExpression, metricRegularExpression, 1);
        String ret = "";
        if (!temp.isEmpty()) {
            ret = temp.get(0);
        }
        return ret;
    }

    /**
     * Method extracts maximal value of metric in given time range.
     *
     * @param start - starting timestamp
     * @param end   - ending timestamp
     * @return maximal metric value
     */
    public long getMaxHistoricalDataFromAgents(String agentRegularExpression, String metricRegularExpression, Calendar start,
                                               Calendar end) {
        final List<String> temp =
            getHistoricalDataFromAgents(agentRegularExpression, metricRegularExpression, start, end);

        Set<Long> values = new HashSet<>();
        if (!temp.isEmpty()) {
            for (String value : temp) {
                value = value.trim();
                Long intVal = Long.valueOf(value);
                values.add(intVal);
            }
        }
        long maxValue = 0L;
        for (Long value : values) {
            if (value > maxValue) {
                maxValue = value;
            }
        }
        return maxValue;
    }

    /**
     * This methods extracts historical data for a specified number of minutes prior to execution of the command and at a frequency of 15
     * seconds
     *
     * @param minutes the number of last n minutes of metric data needed
     * @return An array of strings which are ordered so that, most recent values are first and oldest values are last in the array
     */
    public List<String> getHistoricalDataFromAgents(String agentRegularExpression, String metricRegularExpression, int minutes) {
        agentRegularExpression = escapeSymbols(agentRegularExpression);
        metricRegularExpression = escapeSymbols(metricRegularExpression);

        final String command =
            "get historical data from agents matching \"" + agentRegularExpression
            + "\" and metrics matching \"" + metricRegularExpression + "\" for past " + minutes
            + " minutes with frequency of 15 seconds";
        final List<String> clwOutputStrings = clw.runClw(command);

        if (clwOutputStrings.isEmpty()) {
            throw new RuntimeException("CLW didn't return any data");
        }

        final List<String> ret = getMetricDataFromResultString(clwOutputStrings, VALUE);

        LOGGER.info("CLW Return value == " + ret);
        return ret;
    }

    /**
     * Method extract historical data in given time period for specific agent and metric.
     *
     * @param start - starting timestamp
     * @param end   - ending timestamp
     * @return list of metric values
     */
    public List<String> getHistoricalDataFromAgents(String agentRegularExpression, String metricRegularExpression, Calendar start,
                                                     Calendar end) {

        agentRegularExpression = escapeSymbols(agentRegularExpression);
        metricRegularExpression = escapeSymbols(metricRegularExpression);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());

        final String command =
            "get historical data from agents matching \"" + agentRegularExpression
            + "\" and metrics matching \"" + metricRegularExpression + "\" between "
            + startFormated + " and " + endFormated;
        final List<String> clwOutputStrings = clw.runClw(command);

        if (clwOutputStrings.isEmpty()) {
            throw new RuntimeException("CLW didn't return any data");
        }

        final List<String> ret = getMetricDataFromResultString(clwOutputStrings, VALUE);

        LOGGER.info("CLW Return value == " + ret);
        return ret;
    }


    /**
     * Returns EM host for the agent which is running at given hostname.
     *
     * @param hostname - host of machine where agent run
     * @return EM hostname string
     * @throws IllegalStateException - in case given hostname wasn't found in results so we can't decide
     */
    public String getCollectorHostnameForAgent(String agentRegularExpression, String hostname) {

        agentRegularExpression = escapeSymbols(agentRegularExpression);
        hostname = escapeSymbols(hostname);
        final String command =
            "get historical data from agents matching \"" + agentRegularExpression
            + "\" and metrics matching \"EM Host\" for past 1 minute";
        final List<String> clwOutputStrings = clw.runClw(command);
        if (clwOutputStrings.isEmpty()) {
            throw new IllegalStateException("CLW didn't return any data");
        }
        int i = 1;
        while (i > 0) {
            i++;
            if (i == clwOutputStrings.size()) {
                throw new IllegalStateException("Hostname wasn't found in results");
            }
            final String[] values = clwOutputStrings.get(i).split(",");
            if (values[1].equals(hostname)) {
                return values[19];
            }
        }
        throw new IllegalStateException("Hostname wasn't found in results");
    }


    /**
     * This method returns the column # of value type column. The variable temp should always be first row of the CLW output of metric data,
     * i.e. row containing column headers
     *
     * @param temp Array of comma separated values. i.e. row containing column headers
     * @return The "Value Type" Column number. Index starting with 0.
     * @see CLWBean
     */
    private int getValueTypeColumn(String temp) {
        String[] values = temp.split(",");
        for (int i = 0; i < values.length; i++) {
            if ("Value Type".equals(values[i])) {
                return i;
            }
        }
        return 12;// default is 12
    }

    private int getMetricValueColumn(String[] tmpArray, int valueTypeColumn) {
        if ("Integer".equals(tmpArray[valueTypeColumn])) {
            return 13;
        }
        if ("Long".equals(tmpArray[valueTypeColumn])) {
            return 13;
        }
        if ("Float".equals(tmpArray[valueTypeColumn])) {
            return 16;
        }
        if ("String".equals(tmpArray[valueTypeColumn])) {
            return 19;
        }
        if ("Date".equals(tmpArray[valueTypeColumn])) {
            return 20;
        }

        return 13;// default we consider it as integer value
    }

    private int getMetricMaxValueColumn(String[] tmpArray, int valueTypeColumn) {
        if ("Integer".equals(tmpArray[valueTypeColumn])) {
            return 15;
        }
        if ("Long".equals(tmpArray[valueTypeColumn])) {
            return 15;
        }
        if ("Float".equals(tmpArray[valueTypeColumn])) {
            return 18;
        }
        if ("String".equals(tmpArray[valueTypeColumn])) {
            return 19;
        }
        if ("Date".equals(tmpArray[valueTypeColumn])) {
            return 20;
        }

        return 13;// default we consider it as integer value
    }

    private int getMetricMinValueColumn(String[] tmpArray, int valueTypeColumn) {
        if ("Integer".equals(tmpArray[valueTypeColumn])) {
            return 14;
        }
        if ("Long".equals(tmpArray[valueTypeColumn])) {
            return 14;
        }
        if ("Float".equals(tmpArray[valueTypeColumn])) {
            return 17;
        }
        if ("String".equals(tmpArray[valueTypeColumn])) {
            return 19;
        }
        if ("Date".equals(tmpArray[valueTypeColumn])) {
            return 20;
        }

        return 13;// default we consider it as integer value
    }

    private List<String> getMetricDataFromResultString(List<String> temp, int type) {
        int valueTypeColumn = getValueTypeColumn(temp.get(0));
        final List<String> ret = new ArrayList<>();
        for (int i = 1; i < temp.size(); i++) {// start with int i=1 as first
            // column is only column headers
            if (!temp.get(i).startsWith("Domain, Host, Process, AgentName, Resource, MetricName, Record Type,")) {

                // split only if there is 0 or even number of quotes ahead of
                // comma
                String[] tmpArray = temp.get(i).split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                int metricValueColumn = -1;

                // this if condition is another check for eliminating column
                // headers. In 9.0.5 we get a "?" for first line
                if (type == MIN) {
                    metricValueColumn = getMetricMinValueColumn(tmpArray, valueTypeColumn);
                } else if (type == MAX) {
                    metricValueColumn = getMetricMaxValueColumn(tmpArray, valueTypeColumn);
                } else if (type == COUNT) {
                    metricValueColumn = 11;
                } else if (type == STRING) {
                    metricValueColumn = STRING;
                } else {
                    metricValueColumn = getMetricValueColumn(tmpArray, valueTypeColumn);
                }

                if ((tmpArray.length > 21) && (metricValueColumn == 19)) {// String Value contains ','.
                    // As there is only one
                    // possibility to get ',' in
                    // String
                    // value.
                    ret.add(temp.get(i).substring(nthIndexOf(temp.get(i), ',', 19) + 1,
                                                  temp.get(i).lastIndexOf(",")));
                } else if (tmpArray.length > metricValueColumn) {
                    ret.add(tmpArray[metricValueColumn]);
                }
            }
        }
        Collections.reverse(ret);
        return ret;
    }

    private int nthIndexOf(String str, char delimiter, int n) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == delimiter) {
                n--;
                if (n == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private String escapeSymbols(String source) {
        if ((source == null) || source.isEmpty()) {
            return source;
        }
        for (String str : ESCAPE_SYMBOLS) {
            source = source.replace(str, "\\" + str);
        }

        return source;
    }
}
