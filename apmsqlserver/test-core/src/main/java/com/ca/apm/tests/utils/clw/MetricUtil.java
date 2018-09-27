package com.ca.apm.tests.utils.clw;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to retrieve metrics by invoking CLW. Adapted from CODA and
 * simplified. Extended with new methods as needed.
 * 
 */
public class MetricUtil {

    private static final Logger log = LoggerFactory.getLogger(MetricUtil.class);

    private ClwRunner clw;

    private final static Integer MIN = 1;

    private final static Integer MAX = 2;

    private final static Integer VALUE = 3;

    private final static Integer COUNT = 4;

    private final static Integer STRING = 19;

    private final static String[] ESCAPE_SYMBOLS = {"(", ")", ":", "?", "|"};

    public MetricUtil(ClwRunner clw) {
        this.clw = clw;
    }

    /**
     * This methods extracts the latest value of the metric just before the
     * execution of the command.
     * 
     * @return a string value of the last metric value
     */
    public String getLastHistoricalDataFromAgents(String agentRegularExpression,
        String metricRegularExpression) throws Exception {
        final List<String> temp =
            this.getHistoricalDataFromAgents(agentRegularExpression, metricRegularExpression, 1);
        String ret = null;
        if (temp.size() > 0) {
            ret = temp.get(0);
        }
        return ret;
    }

    /**
     * Method extracts maximal value of metric in given time range.
     * 
     * @param start - starting timestamp
     * @param end - ending timestamp
     * @return maximal metric value
     */
    public Long getMaxHistoricalDataFromAgents(String agentRegularExpression,
        String metricRegularExpression, Calendar start, Calendar end) throws Exception {
        final List<String> temp =
            this.getHistoricalDataFromAgents(agentRegularExpression, metricRegularExpression,
                start, end);

        HashSet<Long> values = new HashSet<Long>();
        if (temp.size() > 0) {
            for (String value : temp) {
                value = value.trim();
                Long intVal = Long.valueOf(value);
                values.add(intVal);
            }
        }
        Long maxValue = 0L;
        for (Long value : values) {
            if (value > maxValue) maxValue = value;
        }
        return maxValue;
    }

    /**
     * This methods extracts historical data for a specified number of minutes
     * prior to execution of the command and at a frequency of 15 seconds
     * 
     * @param n
     *        the number of last n minutes of metric data needed
     * @return An array of strings which are ordered so that, most recent values
     *         are first and oldest values are last in the array
     */
    private List<String> getHistoricalDataFromAgents(String agentRegularExpression,
        String metricRegularExpression, int minutes) throws Exception {
        agentRegularExpression = escapeSymbols(agentRegularExpression);
        metricRegularExpression = escapeSymbols(metricRegularExpression);

        final String command =
            "get historical data from agents matching \"" + agentRegularExpression
                + "\" and metrics matching \"" + metricRegularExpression + "\" for past " + minutes
                + " minutes with frequency of 15 seconds";
        final String clwOutputStrings[] = clw.runCLW(command);

        if (null == clwOutputStrings || clwOutputStrings.length == 0) {
            throw new RuntimeException("CLW didn't return any data");
        }

        final List<String> ret = getMetricDataFromResultString(clwOutputStrings, VALUE);

        log.info("CLW Return value == " + ret);
        return ret;
    }

    /**
     * Method extract historical data in given time period for specific agent and metric.
     * 
     * @param agentRegularExpression
     * @param metricRegularExpression
     * @param start - starting timestamp
     * @param end - ending timestamp
     * @return list of metric values
     */
    private List<String> getHistoricalDataFromAgents(String agentRegularExpression,
        String metricRegularExpression, Calendar start, Calendar end) throws Exception {

        agentRegularExpression = escapeSymbols(agentRegularExpression);
        metricRegularExpression = escapeSymbols(metricRegularExpression);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());

        final String command =
            "get historical data from agents matching \"" + agentRegularExpression
                + "\" and metrics matching \"" + metricRegularExpression + "\" between "
                + startFormated + " and " + endFormated;
        final String clwOutputStrings[] = clw.runCLW(command);

        if (null == clwOutputStrings || clwOutputStrings.length == 0) {
            throw new RuntimeException("CLW didn't return any data");
        }

        final List<String> ret = getMetricDataFromResultString(clwOutputStrings, VALUE);

        log.info("CLW Return value == " + ret);
        return ret;
    }



    /**
     * Returns EM host for the agent which is running at given hostname.
     * 
     * @param agentRegularExpression
     * @param hostname - host of machine where agent run
     * @return EM hostname string
     * @throws Exception - in case given hostname wasn't found in results so we can't decide
     */
    public String getCollectorHostnameForAgent(String agentRegularExpression, String hostname)
        throws Exception {

        agentRegularExpression = escapeSymbols(agentRegularExpression);
        hostname = escapeSymbols(hostname);
        final String command =
            "get historical data from agents matching \"" + agentRegularExpression
                + "\" and metrics matching \"EM Host\" for past 1 minute";
        final String clwOutputStrings[] = clw.runCLW(command);
        if (null == clwOutputStrings || clwOutputStrings.length == 0) {
            throw new RuntimeException("CLW didn't return any data");
        }
        int i = 1;
        while (i > 0) {
            i++;
            if (i == clwOutputStrings.length)
                throw new Exception("Hostname wasn't found in results");
            final String values[] = clwOutputStrings[i].split(",");
            if (values[1].equals(hostname)) {
                return values[19];
            }
        }
        throw new Exception("Hostname wasn't found in results");
    }


    /**
     * This method returns the column # of value type column. The variable temp
     * should always be first row of the CLW output of metric data, i.e. row
     * containing column headers
     * 
     * @param temp
     *        Array of comma separated values. i.e. row containing column
     *        headers
     * @return The "Value Type" Column number. Index starting with 0.
     * @see CLWBean
     * @version 1.0
     */
    private static int getValueTypeColumn(String temp) {
        String values[] = temp.split(",");
        for (int i = 0; i < values.length; i++)
            if (values[i].equals("Value Type")) return i;
        return 12;// default is 12
    }

    private static int getMetricValueColumn(String[] tmpArray, int valueTypeColumn) {
        if (tmpArray[valueTypeColumn].equals("Integer")) return 13;
        if (tmpArray[valueTypeColumn].equals("Long")) return 13;
        if (tmpArray[valueTypeColumn].equals("Float")) return 16;
        if (tmpArray[valueTypeColumn].equals("String")) return 19;
        if (tmpArray[valueTypeColumn].equals("Date")) return 20;

        return 13;// default we consider it as integer value
    }

    private static int getMetricMaxValueColumn(String[] tmpArray, int valueTypeColumn) {
        if (tmpArray[valueTypeColumn].equals("Integer")) return 15;
        if (tmpArray[valueTypeColumn].equals("Long")) return 15;
        if (tmpArray[valueTypeColumn].equals("Float")) return 18;
        if (tmpArray[valueTypeColumn].equals("String")) return 19;
        if (tmpArray[valueTypeColumn].equals("Date")) return 20;

        return 13;// default we consider it as integer value
    }

    private static int getMetricMinValueColumn(String[] tmpArray, int valueTypeColumn) {
        if (tmpArray[valueTypeColumn].equals("Integer")) return 14;
        if (tmpArray[valueTypeColumn].equals("Long")) return 14;
        if (tmpArray[valueTypeColumn].equals("Float")) return 17;
        if (tmpArray[valueTypeColumn].equals("String")) return 19;
        if (tmpArray[valueTypeColumn].equals("Date")) return 20;

        return 13;// default we consider it as integer value
    }

    private static List<String> getMetricDataFromResultString(String temp[], int type) {
        int valueTypeColumn = getValueTypeColumn(temp[0]);
        final List<String> ret = new ArrayList<String>();
        for (int i = 1; i < temp.length; i++) {// start with int i=1 as first
                                               // column is only column headers
            if (!temp[i]
                .startsWith("Domain, Host, Process, AgentName, Resource, MetricName, Record Type,")) {

                // split only if there is 0 or even number of quotes ahead of
                // comma
                String[] tmpArray = temp[i].split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
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

                if (tmpArray.length > 21 && metricValueColumn == 19) {// String Value contains ','.
                                                                      // As there is only one
                                                                      // possibility to get ',' in
                                                                      // String
                                                                      // value.
                    ret.add(temp[i].substring(nthIndexOf(temp[i], ',', 19) + 1,
                        temp[i].lastIndexOf(",")));
                } else if (tmpArray.length > metricValueColumn) {
                    ret.add(tmpArray[metricValueColumn]);
                }
            }
        }
        Collections.reverse(ret);
        return ret;
    }

    private static int nthIndexOf(String str, char delimiter, int n) {
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

    private static String escapeSymbols(String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        for (String str : ESCAPE_SYMBOLS) {
            source = source.replace(str, "\\" + str);
        }

        return source;
    }
}
