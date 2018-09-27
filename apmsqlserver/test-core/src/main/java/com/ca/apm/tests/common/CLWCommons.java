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
 * 
 * Author : JAMSA07/ SANTOSH JAMMI
 * Author : GAMSA03/ SANTOSH GAMPA
 * Date : 20/11/2015
 */


package com.ca.apm.tests.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.test.ClwRunner;

public class CLWCommons {

    String str = "";
    private static final Logger LOGGER = LoggerFactory.getLogger(CLWCommons.class);


    /**
     * Gets the metric value for the requested agents and metrics
     * 
     * @param user
     * @param password
     * @param agentExpression
     * @param metricExpression
     * @return
     *         Only returns the latest value
     */
    public String getLatestMetricValue(String user, String password, String agentExpression,
        String metricExpression, String host, int port, String emLibDir) {

        int minutes = 1;
        String returnValue = "";


        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        String command =
            "get historical data from agents matching \"" + agentExpression
                + "\" and metrics matching \"" + metricExpression + "\" for past " + minutes
                + " minutes";

        List<String> output = clwRunner.runClw(command);

        Iterator<String> out = output.iterator();
        if (output.size() >= 3) while (out.hasNext())
            returnValue = verifyValue(out.next());

        if (returnValue != "")
            return returnValue;

        else
            return "-1";
    }



    /**
     * Gets the metric value for the requested agents and metrics
     * 
     * @param user
     * @param password
     * @param agentExpression
     * @param metricExpression
     * @return
     * 
     *         Returns a list which has all the results for the CLW command for the past specified
     *         minutes
     */
    public List<String> getMetricValueForTimeInMinutes(String user, String password,
        String agentExpression, String metricExpression, String host, int port, String emLibDir,
        int minutes) {

        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        String command =
            "get historical data from agents matching \"" + agentExpression
                + "\" and metrics matching \"" + metricExpression + "\" for past " + minutes
                + " minutes";

        return clwRunner.runClw(command);
    }


    /**
     * Gets the metric value for the requested agents and metrics
     * 
     * Dates are calendar objects
     * 
     * @param user
     * @param password
     * @param agentExpression
     * @param metricExpression
     * @param host
     * @param port
     * @param emLibDir
     * @param start
     * @param end
     * @return
     * 
     *         Returns a list which has all the results for the CLW command in date/time range
     */

    public List<String> getMetricValueInTimeRange(String user, String password,
        String agentExpression, String metricExpression, String host, int port, String emLibDir,
        Calendar start, Calendar end) {

        LOGGER.info("Inside getMetricValueInTimeRange");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String startFormatted = formatter.format(start.getTime());
        String endFormatted = formatter.format(end.getTime());

        final String command =
            "get historical data from agents matching \"" + agentExpression
                + "\" and metrics matching \"" + metricExpression + "\" between " + startFormatted
                + " and " + endFormatted;

        LOGGER.info(command);
        return clwRunner.runClw(command);
    }



    /**
     * Gets the metric value for the requested agents and metrics
     * 
     * Dates are Strings
     * 
     * @param user
     * @param password
     * @param agentExpression
     * @param metricExpression
     * @param host
     * @param port
     * @param emLibDir
     * @param start
     * @param end
     * @return
     * 
     *         Returns a list which has all the results for the CLW command in date/time range
     */

    public List<String> getMetricValueInTimeRange(String user, String password,
        String agentExpression, String metricExpression, String host, int port, String emLibDir,
        String start, String end) {

        LOGGER.info("Inside getMetricValueInTimeRange");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();
        final String command =
            "get historical data from agents matching \"" + agentExpression
                + "\" and metrics matching \"" + metricExpression + "\" between " + start + " and "
                + end;

        LOGGER.info(command);
        return clwRunner.runClw(command);
    }

    /**
     * 
     * @param value
     * @return
     * 
     *         validates the String based on value type and returns a concatenated String with its
     *         value type and value.
     */
    private String verifyValue(String value) {
        LOGGER.info(value);
        LOGGER.info("Next Entry");
        String returnValue = "";

        if (!value.equalsIgnoreCase("?")) {
            String[] values = value.split(",");
            if (values.length > 1) {
                if (values[12].equalsIgnoreCase("Integer")) {
                    returnValue = values[12] + ":::" + values[13];
                    LOGGER.info("This is an integer value" + returnValue);
                } else if (values[12].equalsIgnoreCase("Float")) {
                    returnValue = values[12] + ":::" + values[16];
                    LOGGER.info("This is an float value" + returnValue);
                } else if (values[12].equalsIgnoreCase("String")) {
                    returnValue = values[12] + ":::" + values[19];
                    LOGGER.info("This is an String value" + returnValue);
                } else if (values[12].equalsIgnoreCase("Date")) {
                    returnValue = values[12] + ":::" + values[20];
                    LOGGER.info("This is an Date value" + returnValue);
                } else if (values[12].equalsIgnoreCase("long")) {
                    returnValue = values[12] + ":::" + values[13];
                    LOGGER.info("This is an Long value" + returnValue);
                } else if (values[12].equalsIgnoreCase("double")) {
                    returnValue = values[12] + ":::" + values[13];
                    LOGGER.info("This is an Double value" + returnValue);
                }
            }
        }
        return returnValue;
    }
    
    
    /**
     * Gets the Set of unique metric paths for the specified agents and metric expressions
     * 
     * @param user
     * @param password
     * @param agentExpression
     * @param metricExpression
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    public Set<String> getuniqueMetricPaths(String user, String password,
        String agentExpression, String metricExpression, String host, int port, String emLibDir) {

        List<String> clwOut = new ArrayList<String>();
        List<String> metricResources = new ArrayList<String>();
        Set<String> uniqueMetricPaths = Collections.emptySet();

        // Fire the clw command with the given parameters
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        String command =
            "get historical data from agents matching \"" + agentExpression
                + "\" and metrics matching \"" + metricExpression + "\" for past 1 minutes";

        clwOut = clwRunner.runClw(command);

        // Proceed if at least one data point exists with the iterator staring at the first data
        // point
        Iterator<String> clwOutItr = clwOut.listIterator(2);
        if (clwOut.size() >= 3) {

            while (clwOutItr.hasNext()) {
                String datapoint = clwOutItr.next();
                String[] values = datapoint.split(",");
                String metricPath = values[4] + ":" + values[5];
                metricResources.add(metricPath);
            }
            uniqueMetricPaths = new HashSet<String>(metricResources);
             System.out.println(metricResources);
            LOGGER.info("The unique Metric Paths for agent expression " + agentExpression + 
            		" and metric expression " + metricExpression + " are");
            System.out.println(uniqueMetricPaths);
        } else {
            LOGGER.info("No data exists for the given clw query");
        }

        return uniqueMetricPaths;
    }
        
    /**
     * Gets the Set of unique metric resources for the specified agents and metric expressions
     * 
     * @param user
     * @param password
     * @param agentExpression
     * @param metricExpression
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    public Set<String> getuniqueMetricResources(String user, String password,
        String agentExpression, String metricExpression, String host, int port, String emLibDir) {

        List<String> clwOut = new ArrayList<String>();
        List<String> metricResources = new ArrayList<String>();
        Set<String> uniqueMetricResources = Collections.emptySet();

        // Fire the clw command with the given parameters
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        String command =
            "get historical data from agents matching \"" + agentExpression
                + "\" and metrics matching \"" + metricExpression + "\" for past 1 minutes";

        clwOut = clwRunner.runClw(command);

        // Proceed if at least one data point exists with the iterator staring at the first data
        // point
        Iterator<String> clwOutItr = clwOut.listIterator(2);
        if (clwOut.size() >= 3) {

            while (clwOutItr.hasNext()) {
                String datapoint = clwOutItr.next();
                String[] values = datapoint.split(",");
                metricResources.add(values[4]);
            }
            uniqueMetricResources = new HashSet<String>(metricResources);
            // System.out.println(metricResources);
            LOGGER.info("The unique Metric Paths for agent expression " + agentExpression + 
            		" and metric expression " + metricExpression + " are");
            System.out.println(uniqueMetricResources);
        } else {
            LOGGER.info("No data exists for the given clw query");
        }

        return uniqueMetricResources;
    }
    
    
    public List<String> getNodeList(String user, String password,
        String expression,  String host, int port, String emLibDir) {

        LOGGER.info("Inside getMetricValueInTimeRange");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();
        final String command =
            "list agents matching "+expression;

        LOGGER.info(command);
        return clwRunner.runClw(command);
    }
    
    

    public void shutOffMetrics() {
        // TODO:
    }

    public void turnOnMetrics() {
        // TODO:
    }


}
