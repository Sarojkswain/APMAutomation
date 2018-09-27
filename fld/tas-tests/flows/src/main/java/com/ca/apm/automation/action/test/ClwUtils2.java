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

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Contains utility methods for use with CLWorkstation.jar
 */
public class ClwUtils2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClwUtils2.class);
    private final ClwRunner2 clwRunner;
    private final MetricUtils2 metricUtils;
    private final TransactionTraceUtils transactionTraceUtils;
    
    public ClwUtils2(ClwRunner2 clwRunner) {
        this.clwRunner = clwRunner;
        metricUtils = new MetricUtils2(clwRunner);
        transactionTraceUtils = new TransactionTraceUtils(clwRunner);
    }

    @NotNull
    public ClwRunner2 getClwRunner() {
        return clwRunner;
    }

    @NotNull
    public MetricUtils2 getMetricUtils() {
        return metricUtils;
    }
    
    @NotNull
    public TransactionTraceUtils getTransactionTraceUtils() {
        return transactionTraceUtils;
    }

    /**
     * Trace transactions for the <code>forSec</code> seconds given the agent name regular expression <code>agentNameRegex</code> and 
     * transaction minimal length in milliseconds <code>exceedsMillis</code>.
     * 
     * <p/>
     * This methods corresponds to CLWorkstation.jar's "trace transactions" command.
     * 
     * @param agentNameRegex  agent name regular expression
     * @param exceedsMillis   minimal transaction length in milliseconds   
     * @param forSecs         tracing session length in seconds
     * @return                CLWorkstation's output
     */
    public List<String> traceTransactions(String agentNameRegex, int exceedsMillis, int forSecs) {
        LOGGER.info("Starting tracing transactions exceeding {} ms for agents which names match regex='{}' for {} s", exceedsMillis, agentNameRegex, forSecs);
        List<String> outputRows = transactionTraceUtils.traceTransactions(agentNameRegex, exceedsMillis, forSecs);
        for (String outputRow : outputRows) {
            LOGGER.info(outputRow);
        }
        return outputRows;
    }
    
    public String getMetricFromAgent(String agentRegularExpression, String metricRegularExpression) throws Exception {
        final String ret =
            metricUtils.getLastHistoricalDataFromAgents(agentRegularExpression, metricRegularExpression);
        LOGGER.info("result == {}", ret);
        return ret;
    }

    /**
     * Method extract maximal value of metric in given time range for agent and metric specified by given regular expressions.
     *
     * @param start - starting timestamp
     * @param end   - ending timestamp
     * @return maximal metrics value in given period
     */
    public Long getMaxMetricsValueFromAgent(String agentRegularExpression, String metricRegularExpression, Calendar start, Calendar end) {
        final Long ret =
            metricUtils.getMaxHistoricalDataFromAgents(agentRegularExpression, metricRegularExpression, start, end);
        LOGGER.info("result == " + ret);
        return ret;
    }


    /**
     * Returns EM host for the agent which is running at given hostname.
     *
     * @param hostname - host of machine where agent run
     * @return EM hostname string
     * @throws IllegalStateException - in case given hostname wasn't found in results so we can't decide
     */
    public String getCollectorHostnameForAgent(String agentRegularExpression, String hostname)  {
        return metricUtils.getCollectorHostnameForAgent(agentRegularExpression, hostname);
    }

    /**
     * This method checks if all collectors of given list are configure on MOM or on CDV. Method checks it according to hostname.
     *
     * @param hostname   - hostname of cluster (CDV or MOM)
     * @param collectors - list of configured collectors' hostnames and ports in format 'hostname@port'
     */
    public void checkClusterConfiguration(String hostname, String[] collectors) {
        final String command = "get cluster configuration";
        final List<String> clwOutputStrings = clwRunner.runClw(command);
        if (clwOutputStrings.isEmpty()) {
            throw new RuntimeException("CLW didn't return any data");
        }
        if (!clwOutputStrings.get(1).contains(hostname)) {
            throw new IllegalStateException("Cluster is not right configured. Expected " + hostname
                                            + " but found " + clwOutputStrings.get(1));
        }

        Set<String> clusterCollectors = extractSequenceOfRows(clwOutputStrings, "Collectors");
        List<String> clustersArrayList = new ArrayList<>(Arrays.asList(collectors));
        if (!(clusterCollectors.containsAll(clustersArrayList) && (clusterCollectors.size() == collectors.length))) {
            throw new IllegalStateException(
                "Cluster is not right configured. Expected " + clustersArrayList + " but found " + clusterCollectors);
        }
    }

    /**
     * Iterate string filed with output rows of Clw command and returns set of rows founded from row which contains specified key-string.
     *
     * @param clwOutputStrings - result of clw.runCLW(command)
     * @param keyString        - string which indicates finding sequence of rows
     */
    @NotNull
    private Set<String> extractSequenceOfRows(List<String> clwOutputStrings, String keyString) {
        Set<String> resultSet = new HashSet<>();
        int i = 0;
        for (String row : clwOutputStrings) {
            if (row.contains(keyString)) {
                i++;
                String collector = clwOutputStrings.get(i);
                while (!collector.isEmpty()) {
                    resultSet.add(collector);
                    i++;
                    collector = (i < clwOutputStrings.size()) ? clwOutputStrings.get(i) : "";
                }
                return resultSet;
            }
            i++;

        }
        return resultSet;
    }

    /**
     * gets the number of active or passive agents at the given EM.
     *
     * @param collectorHost host of the Collector NULL for MOM
     * @param type          :(active/passive)
     * @return Returns number of passive/active agents
     */
    public int getAgents(String collectorHost, String type) {

        LOGGER.info("Retrieving number of '{}' agents for collector '{}' from {}:{}", type, collectorHost, clwRunner.getEmHost(),
                    clwRunner.getPort());
        // build proper metrics string
        String metricpath;
        if ("Active".equalsIgnoreCase(type)) {
            metricpath = "Number of Agents";
        } else if ("Disallowed".equalsIgnoreCase(type)) {
            metricpath = "Number of Disallowed Agents";
        } else {
            throw new RuntimeException("Unknown Agent type : '" + type + "'. Only 'Active' or 'Disallowed' are allowed.");
        }

        final String hostAndPort;
        if (collectorHost == null) {
            hostAndPort = "";
        } else {
            hostAndPort = " (" + collectorHost + "@.*)";
        }

        final String agentRegularExpression =
            "Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)"
            + hostAndPort;
        final String metricRegularExpression = "Enterprise Manager|Connections:" + metricpath;

        // invoke CLW and get metrics

        String ret =
            metricUtils.getLastHistoricalDataFromAgents(agentRegularExpression, metricRegularExpression);

        LOGGER.info("result == {}", ret);
        if (ret == null) {
            ret = "0";
        }

        return Integer.parseInt(ret);
    }
}
