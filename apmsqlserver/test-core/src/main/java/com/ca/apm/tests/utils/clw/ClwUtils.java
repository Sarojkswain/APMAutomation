/*
 * Copyright (c) 2014 CA. All rights reserved.
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

package com.ca.apm.tests.utils.clw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains utility methods for use with CLWorkstation.jar
 */
public class ClwUtils {

    private static final Logger log = LoggerFactory.getLogger(ClwUtils.class);

    private String emHost = "localhost";

    private String user = "Admin";

    private String password = "";

    private int port = 5001;

    private String clWorkstationJarFileLocation = "./lib/CLWorkstation.jar";

    private String javaPath = "java";

    public void setEmHost(String emHost) {
        this.emHost = emHost;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    /**
     * @param clWorkstationJarFileLocation
     *        the clWorkstationJarFileLocation to set
     */
    public void setClWorkstationJarFileLocation(String clWorkstationJarFileLocation) {
        this.clWorkstationJarFileLocation = clWorkstationJarFileLocation;
    }

    public String getMetricFromAgent(String agentRegularExpression, String metricRegularExpression)
        throws Exception {
        final ClwRunner clw = setUpClwRunner();
        final MetricUtil metricutil = new MetricUtil(clw);
        final String ret =
            metricutil.getLastHistoricalDataFromAgents(agentRegularExpression,
                metricRegularExpression);
        log.info("result == " + ret);
        return ret;
    }

    /**
     * Method extract maximal value of metric in given time range for agent and metric specified by
     * given regular expressions.
     * 
     * @param start - starting timestamp
     * @param end - ending timestamp
     * @return maximal metrics value in given period
     */
    public Long getMaxMetricsValueFromAgent(String agentRegularExpression,
        String metricRegularExpression, Calendar start, Calendar end) throws Exception {
        final ClwRunner clw = setUpClwRunner();
        final MetricUtil metricutil = new MetricUtil(clw);
        final Long ret =
            metricutil.getMaxHistoricalDataFromAgents(agentRegularExpression,
                metricRegularExpression, start, end);
        log.info("result == " + ret);
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
        final ClwRunner clw = setUpClwRunner();
        final MetricUtil metricutil = new MetricUtil(clw);
        return metricutil.getCollectorHostnameForAgent(agentRegularExpression, hostname);
    }

    /**
     * This method checks if all collectors of given list are configure on MOM or on CDV.
     * Method checks it according to hostname.
     * 
     * @param hostname - hostname of cluster (CDV or MOM)
     * @param collectors - list of configured collectors' hostnames and ports in format
     *        'hostname@port'
     * 
     */
    public void checkClusterConfiguration(String hostname, String[] collectors) throws Exception {
        final ClwRunner clw = setUpClwRunner();
        final String command = "get cluster configuration";
        final String clwOutputStrings[] = clw.runCLW(command);
        if (null == clwOutputStrings || clwOutputStrings.length == 0) {
            throw new RuntimeException("CLW didn't return any data");
        }
        if (!clwOutputStrings[1].contains(hostname)){
            throw new Exception("Cluster is not right configured. Expected " + hostname
                    + " but found " + clwOutputStrings[1]);
        }

        HashSet<String> clusterCollectors = extractSequenceOfRows(clwOutputStrings, "Collectors");
        ArrayList<String> clustersArrayList = new ArrayList<String>(Arrays.asList(collectors));
        if (!(clusterCollectors.containsAll(clustersArrayList) && clusterCollectors.size() == collectors.length)) {
            throw new Exception("Cluster is not right configured. Expected "
                + clustersArrayList.toString() + " but found " + clusterCollectors.toString());
        }

    }


    /**
     * Iterate string filed with output rows of Clw command and returns set of rows founded from row
     * which contains specified key-string.
     * 
     * @param clwOutputStrings - result of clw.runCLW(command)
     * @param keyString - string which indicates finding sequence of rows
     * @return
     */
    private HashSet<String> extractSequenceOfRows(String[] clwOutputStrings, String keyString) {
        HashSet<String> resultSet = new HashSet<String>();
        int i = 0;
        for (String row : clwOutputStrings) {
            if (row.contains(keyString)) {
                i++;
                String collector = clwOutputStrings[i];
                while (!collector.isEmpty()) {
                    resultSet.add(collector);
                    i++;
                    collector = i < clwOutputStrings.length ? clwOutputStrings[i] : "";
                }
                return resultSet;
            }
            i++;

        }
        return null;
    }


    private ClwRunner setUpClwRunner() {
        final ClwRunner clw = new ClwRunner();
        clw.setClWorkstationJarFileLocation(clWorkstationJarFileLocation);
        clw.setJavaPath(javaPath);
        clw.setEmHost(emHost);
        clw.setPort(port);
        clw.setUser(user);
        clw.setPassword(password);
        return clw;
    }

    /**
     * gets the number of active or passive agents at the given EM.
     * 
     * @param collectorHost host of the Collector
     *        NULL for MOM
     * @param type
     *        :(active/passive)
     * @return Returns number of passive/active agents
     */
    public int getAgents(String collectorHost, String type) throws Exception {
        log.info("Retrieving number of '" + type + "' agents for collector '" + collectorHost
            + "' from " + emHost + ":" + port);
        ClwRunner clw = setUpClwRunner();

        // build proper metrics string
        String metricpath;
        if (type.equalsIgnoreCase("Active")) {
            metricpath = "Number of Agents";
        } else if (type.equalsIgnoreCase("Disallowed")) {
            metricpath = "Number of Disallowed Agents";
        } else {
            throw new RuntimeException("Unknown Agent type : '" + type
                + "'. Only 'Active' or 'Disallowed' are allowed.");
        }

        final String hostAndPort;
        if (collectorHost == null) {
            hostAndPort = "";
        } else {
            hostAndPort = " (" + collectorHost + "@.*)";
        };
        final String agentRegularExpression =
            "Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)"
                + hostAndPort;
        final String metricRegularExpression = "Enterprise Manager|Connections:" + metricpath;

        // invoke CLW and get metrics
        final MetricUtil metricutil = new MetricUtil(clw);
        String ret =
            metricutil.getLastHistoricalDataFromAgents(agentRegularExpression,
                metricRegularExpression);

        log.info("result == " + ret);
        if (ret == null) {
            ret = "0";
        }

        return Integer.parseInt(ret);
    }

    /**
     * Executes CLW command as is
     * 
     * @param clwCommand - string to pass on to CLWorkstation
     * @return CLW output
     */
    public List<String> runClw(String clwCommand) throws Exception {
        log.info("Running CLW command '" + clwCommand + "' on " + emHost + ":" + port);
        final ClwRunner clw = setUpClwRunner();

        final String output[] = clw.runCLW(clwCommand);

        final List<String> ret = Arrays.asList(output);

        log.info("CLW Return value == " + ret);
        return ret;
    }
}
