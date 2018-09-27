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


package com.ca.apm.commons.common;

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
import org.testng.Assert;

import com.ca.apm.automation.action.test.ClwRunner;

public class CLWCommons {

    String str = "";
	String result="";
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
     * Gets all the historic metric values of the requested agents and metrics 
     * for the past specified time        
     * 
     * @param user
     * @param password
     * @param agentExpression
     * @param metricExpression
     * @param host
     * @param port
     * @param emLibDir
     * @param minutes     
     * @return 
     *        Returns a list which has all the historic metric values from the CLW command for 
     *        past specified minutes
     */
    public List<String> getHistoricMetricValuesForTimeInMinutes(String user, String password, String agentExpression,
    		String metricExpression, String host, int port, String emLibDir, int minutes) {         
            
    		List<String> returnValue = new ArrayList<String>();  
    		String returnsplitValue[] = null;    
    		List<String> output = getMetricValueForTimeInMinutes(user, password, agentExpression, metricExpression, host, port, emLibDir, minutes);
          
    		if (output.size() >= 3) 
    		for (int i=2;i<output.size();i++) {
            	returnsplitValue = verifyValue(output.get(i)).split(":::");
                LOGGER.info("After Split" +returnsplitValue[1]);
                returnValue.add(returnsplitValue[1]);
            }            	
            	
            return returnValue;  
            
    }  
    
    /**
     * sends the corresponding file from EM to the matching agents config directory
     * 
     * @param user
     * @param password
     * @param agentExpression
     * @param filetosend
     * @param destinationDir
     * 
     *         
     */
    public List<String> sendConfigFiletoAgents(String user, String password, String agentExpression,
    		String filetosend, String host, int port, String emLibDir) {

        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();
       
        String command = "send config file " + "\"" + filetosend + "\"" + " to agents matching \"" + agentExpression 
        		+ "\" to destination dir ";

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
    
    /**
     * get the list of nodes matching the expression 
     * 
     * @param user
     * @param password
     * @param expression
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    
    public List<String> getNodeList(String user, String password,
        String expression,  String host, int port, String emLibDir) {

        LOGGER.info("Inside getNodeList");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();
        final String command =
            "list agents matching "+expression;

        LOGGER.info(command);
        return clwRunner.runClw(command);
    }
    
    /**
     * gets the transaction traces
     * 
     * @param user
     * @param password
     * @param expression
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    
    public List<String> getTranscationTraces(String user, String password,
        String expression,  String host, int port, String emLibDir) {

        LOGGER.info("Inside getTranscationTraces");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();
        
        final String command = (new StringBuilder(
            "trace transactions exceeding 1 ms in agents matching (.*) for 60 seconds"))
            .toString();
        
        LOGGER.info(command);
        return clwRunner.runClw(command);
    }

    /**
     * gets the loadbalancing.xml last updated time
     * 
     * @param user
     * @param password
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    public String getLoadBalancingXmlLastUpdatedTime(String user, String password, String host,
        int port, String emLibDir) {

        LOGGER.info("Inside getLoadBalancingXmlLastUpdatedTime");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        final String command = "get loadbalancer lastmodified timestamp";

        LOGGER.info(command);
        result = clwRunner.runClw(command).toString().replaceAll("\\[|\\]", "");
        return result;


    }

    /**
     * gets the loadbalancing.xml information along with last updated time
     * 
     * @param user
     * @param password
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    public List<String> getLoadBalancingXmlDetails(String user, String password, String host,
        int port, String emLibDir) {

        LOGGER.info("Inside getLoadBalancingXmlDetails");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        final String command = "get loadbalancer details";

        LOGGER.info(command);
        return clwRunner.runClw(command);


    }

    /**
     * gets the allowed Agents List
     * 
     * @param user
     * @param password
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    public List<String> getAllowedAgentsList(String user, String password, String host, int port,
        String emLibDir) {

        LOGGER.info("Inside getAllowedAgentsList");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        final StringBuilder command = new StringBuilder();
        command.append("get allowed agents");
        command.append(" ");
        command.append(host);
        command.append(" ");
        command.append(port);

        LOGGER.info(command.toString());
        return clwRunner.runClw(command.toString());


    }


    /**
     * gets the Disallowed Agents List
     * 
     * @param user
     * @param password
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    public List<String> getDisAllowedAgentsList(String user, String password, String host,
        int port, String emLibDir) {

        LOGGER.info("Inside getDisAllowedAgentsList");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        final StringBuilder command = new StringBuilder();
        command.append("get disallowed agents");
        command.append(" ");
        command.append(host);
        command.append(" ");
        command.append(port);

        LOGGER.info(command.toString());
        return clwRunner.runClw(command.toString());


    }


    /**
     * gets the Current allowed Agents List dynamically/runtime for the given collector
     * 
     * @param user
     * @param password
     * @param collectorHost
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    public List<String> getCurrentAgentsAllowedList(String user, String password,
        String collectorHost, String host, int port, String emLibDir) {

        LOGGER.info("Inside getCurrentAgentsAllowedList");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        final StringBuilder command = new StringBuilder();
        command.append("get current allowed agents");
        command.append(" ");
        command.append(collectorHost);
        command.append(" ");
        command.append(port);

        LOGGER.info(command.toString());
        return clwRunner.runClw(command.toString());


    }


    /**
     * gets the Current disallowed Agents List dynamically/runtime for the given collector
     * 
     * @param user
     * @param password
     * @param collectorHost
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    public List<String> getCurrentAgentsDisAllowedList(String user, String password,
        String collectorHost, String host, int port, String emLibDir) {

        LOGGER.info("Inside getCurrentAgentsDisAllowedList");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        final StringBuilder command = new StringBuilder();
        command.append("get current disallowed agents");
        command.append(" ");
        command.append(collectorHost);
        command.append(" ");
        command.append(port);

        LOGGER.info(command.toString());
        return clwRunner.runClw(command.toString());


    }


    /**
     * sets the given Agent expression to the given collector in loadbalancing.xml file in allowed
     * mode
     * 
     * @param user
     * @param password
     * @param collectorHost
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    public List<String> setAllowedAgentWithCollector(String user, String password,
        String agent_controller_name, String agentExpression, String collectorHost,
        int collcotrPort, String host, int port, String emLibDir) {

        LOGGER.info("Inside setAllowedAgentWithCollector");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        final StringBuilder command = new StringBuilder();
        command.append("allow agent");
        command.append(" ");
        command.append(agent_controller_name);
        command.append(" ");
        command.append(agentExpression);
        command.append(" ");
        command.append(collectorHost);
        command.append(" ");
        command.append(collcotrPort);
        command.append(" ");
        command.append(getLoadBalancingXmlLastUpdatedTime(user, password, host, port, emLibDir));


        LOGGER.info(command.toString());
        return clwRunner.runClw(command.toString());


    }


    /**
     * sets the given Agent expression to the given collector in loadbalancing.xml file in
     * disallowed mode
     * 
     * @param user
     * @param password
     * @param collectorHost
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    public List<String> setDisAllowedAgentWithCollector(String user, String password,
        String agent_controller_name, String agentExpression, String collectorHost,
        int collectorPort, String host, int port, String emLibDir) {

        LOGGER.info("Inside setDisAllowedAgentWithCollector");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        final StringBuilder command = new StringBuilder();
        command.append("disallow agent");
        command.append(" ");
        command.append(agent_controller_name);
        command.append(" ");
        command.append(agentExpression);
        command.append(" ");
        command.append(collectorHost);
        command.append(" ");
        command.append(collectorPort);
        command.append(" ");
        command.append(getLoadBalancingXmlLastUpdatedTime(user, password, host, port, emLibDir));


        LOGGER.info(command.toString());
        return clwRunner.runClw(command.toString());
    }


    /**
     * sets the given Agent expression in loadbalancing.xml file in allowed mode
     * 
     * @param user
     * @param password
     * @param collectorHost
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    public List<String> setAllowedAgent(String user, String password, String agent_controller_name,
        String agentExpression, String host, int port, String emLibDir) {

        LOGGER.info("Inside setAllowedAgent");

        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        final StringBuilder command = new StringBuilder();
        command.append("allow agent");
        command.append(" ");
        command.append(agent_controller_name);
        command.append(" ");
        command.append(agentExpression);
        command.append(" ");
        command.append(getLoadBalancingXmlLastUpdatedTime(user, password, host, port, emLibDir));


        LOGGER.info(command.toString());
        return clwRunner.runClw(command.toString());


    }

    /**
     * sets the given Agent expression to the given collector in loadbalancing.xml file in
     * disallowed mode
     * 
     * @param user
     * @param password
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    public List<String> setDisAllowedAgent(String user, String password,
        String agent_controller_name, String agentExpression, String host, int port, String emLibDir) {

        LOGGER.info("Inside setDisAllowedAgent");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        final StringBuilder command = new StringBuilder();
        command.append("disallow agent");
        command.append(" ");
        command.append(agent_controller_name);
        command.append(" ");
        command.append(agentExpression);
        command.append(" ");
        command.append(getLoadBalancingXmlLastUpdatedTime(user, password, host, port, emLibDir));


        LOGGER.info(command.toString());
        return clwRunner.runClw(command.toString());
    }


    /**
     * removes the given agent expression from the loadbalancing.xml file
     * 
     * @param user
     * @param password
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    public List<String> removeAgentExpression(String user, String password, String agentExpression,
        String host, int port, String emLibDir) {

        LOGGER.info("Inside removeAgentExpression");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        final StringBuilder command = new StringBuilder();
        command.append("remove agent");
        command.append(" ");
        command.append(agentExpression);
        command.append(" ");
        command.append(getLoadBalancingXmlLastUpdatedTime(user, password, host, port, emLibDir));


        LOGGER.info(command.toString());
        return clwRunner.runClw(command.toString());
    }


    /**
     * gets the included collectors List for the given agent expression from the loadbalancing.xml
     * file
     * 
     * @param user
     * @param password
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    public List<String> getIncludedCollectorsList(String user, String password,
        String agentExpression, String host, int port, String emLibDir) {

        LOGGER.info("Inside getIncludedCollectorsList");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        final StringBuilder command = new StringBuilder();
        command.append("get included collectors");
        command.append(" ");
        command.append(agentExpression);
        command.append(" ");


        LOGGER.info(command.toString());
        return clwRunner.runClw(command.toString());
    }


    /**
     * gets the excludeded collectors List for the given agent expression from the loadbalancing.xml
     * file
     * 
     * @param user
     * @param password
     * @param host
     * @param port
     * @param emLibDir
     * @return
     */
    public List<String> getExcludedCollectorsList(String user, String password,
        String agentExpression, String host, int port, String emLibDir) {

        LOGGER.info("Inside getExcludedCollectorsList");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        final StringBuilder command = new StringBuilder();
        command.append("get excluded collectors");
        command.append(" ");
        command.append(agentExpression);
        command.append(" ");

        LOGGER.info(command.toString());
        return clwRunner.runClw(command.toString());
    }

    
    
    public List<String> turnOffMetrics(String user, String password, String expression,
        String host, int port, String emLibDir) {

        LOGGER.info("Inside turnOffMetrics");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();
        final String command = "turn off exact metric " + expression;
        LOGGER.info(command);
        return clwRunner.runClw(command);
    }

    public List<String> turnOnMetrics(String user, String password, String expression,
        String host, int port, String emLibDir) {

        LOGGER.info("Inside turnONMetrics");
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();
        final String command = "turn on exact metric " + expression;
        LOGGER.info(command);
        return clwRunner.runClw(command);
    }
    
    public List<String> turnOffAgents(String user, String password, String expression,
            String host, int port, String emLibDir) {

            LOGGER.info("Inside turnOffAgents");
            ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
            ClwRunner clwRunner =
                clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                    .password(password).build();
            final String command = "turn off agents matching " + expression;
            LOGGER.info(command);
            return clwRunner.runClw(command);
        }

        public List<String> turnOnAgents(String user, String password, String expression,
            String host, int port, String emLibDir) {

            LOGGER.info("Inside turnONAgents");
            ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
            ClwRunner clwRunner =
                clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                    .password(password).build();
            final String command = "turn on agents matching " + expression;
            LOGGER.info(command);
            return clwRunner.runClw(command);
        }

    /**
     * Check the connectivity between MOM and the specified COLLECTOR
     *
     * @param agentExpression
     * @param metricExpression
     * @param emhost
     * @param emPort
     * @param emLibDir
     */

    public void checkCollectorToMOMConnectivity(String agentExpression,
        String metricExpression, String emhost, String emPort, String emLibDir) {
        List<String> list =
            getAgentMetricsForEMHost(agentExpression, metricExpression, emhost,
                Integer.parseInt(emPort), emLibDir);
        Assert.assertTrue(list.size()>2);
    }

    /**
     * Get the agent metrics for a specified host
     * waits a maximum time of 1200 seconds for the
     * value to be obtained and checks every 15 seconds for the result.
     * 
     * @param agentExpression
     * @param metricExpression
     * @param emHost
     * @param emPort
     * @param emLibDir
     * @return
     */

    public List<String> getAgentMetricsForEMHost(String agentExpression, String metricExpression,
        String emHost, int emPort, String emLibDir) {
        List<String> nodeList = null;
        String user="Admin";
        String password="";
        int i = 0;
        for (i = 0; i < 80; i++) {
            nodeList =
                getMetricValueForTimeInMinutes(user, password, agentExpression,
                    metricExpression, emHost, emPort, emLibDir, 1);
            LOGGER.debug("The list size is : " + nodeList.size());
            i++;
            if (nodeList.size() < 3)
                harvestWait(15);
            else
                break;
        }
        LOGGER.info("It has iterated "+(i+1)+" iterations taking "+(i+1)*15+" seconds");
        return nodeList;
    }

    /**
     * Get the name of the collector for the given agent
     * 
     * @param collectorNames
     * @param collectorPorts
     * @param collector_RoleIDs
     * @param agentName
     * @param roleOrHost
     * @param emLibDir
     * @return
     */

    public String getAgentConnectedCollectorName(List<String> collectorNames,
        List<Integer> collectorPorts, List<String> collector_RoleIDs, String agentName,
        String roleOrHost, String emLibDir) {
        String agentFoundAtCollector = "";
        boolean found = false;
        for (int i = 0; i < collectorNames.size(); i++) {
            if (getConnectedAgentNamesToEMHost(collectorNames.get(i), collectorPorts.get(i),
                agentName, emLibDir).size() > 0) {
                LOGGER.info("The Agent Host Name is "
                    + getConnectedAgentNamesToEMHost(collectorNames.get(i), collectorPorts.get(i),
                        agentName, emLibDir).get(0));
                found = true;
            }
            if (found) {
                agentFoundAtCollector =
                    roleOrHost.contains("Host") ? collectorNames.get(i) : collector_RoleIDs.get(i);
                LOGGER.info("The Resulted Collector is " + agentFoundAtCollector + "That is "
                    + collectorNames.get(i));
                break;
            }
        }
        return agentFoundAtCollector;
    }

    /**
     * Get the agent expression for the given EMHost, connected to that agent.
     * 
     * @param emHost
     * @param emPort
     * @param expression
     * @param emLibDir
     * @return
     */


    public List<String> getConnectedAgentsExpressionToEMHost(String emHost, int emPort,
        String expression, String emLibDir) {
        String user="Admin";
        String password="";

        int i = 0;
        List<String> nodeList = null;
        for (i = 0; i < 80; i++) {
            nodeList = getNodeList(user,password, expression, emHost, emPort, emLibDir);
            if (nodeList.size() >= 1)
                break;
            else
                harvestWait(15);
        }
        LOGGER.info("Agent connected to EM after "+i+" iterations taking "+i*15+" seconds");
        return nodeList;
    }

    /**
     * Get the agent name for the given EMHost, connected to that agent.
     * 
     * @param emHost
     * @param emPort
     * @param expression
     * @param emLibDir
     * @return
     */

    public List<String> getConnectedAgentNamesToEMHost(String emHost, int emPort,
        String expression, String emLibDir) {

        List<String> list =
            getConnectedAgentsExpressionToEMHost(emHost, emPort, expression, emLibDir);
        List<String> names = new ArrayList<String>();

        if (list != null) {
            int k = 0;
            while (k < list.size()) {
                names.add(list.get(k++).split("\\|")[0]);
            }
        }
        return names;
    }


    /**
     * Get the count of disallowed agents
     * Hover in a loop until disallowed count comes up as they might not show up in the first
     * instance.
     * 
     * @param emHost
     * @param emPort
     * @param emLibPath
     * @return
     */

    public int getNumberOfDisallowedAgents(String emHost, int emPort, String emLibPath) {
        int disallowedAgentCount = 0;
        for (int k = 0; k < 80; k++) {
            List<String> list =
                getConnectedAgentMetricForEMHost(
                    "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)",
                    "Enterprise Manager\\|Connections:Number of Disallowed Agents", emHost, emPort,
                    emLibPath);
            if (list.size() >= 3) {
                for (int i = 2; i < list.size(); i++) {
                    String value = list.get(i).split(",")[13];
                    LOGGER.debug("The value is " + value);
                    disallowedAgentCount = Integer.parseInt(value);
                }
            }
            if (list.size() < 3 || disallowedAgentCount < 1)
                harvestWait(15);
            else
                break;

        }
        return disallowedAgentCount;
    }

    /**
     * Get the count of disallowed agents at Collector
     * Hover in a loop until disallowed count comes up as they might not show up in the first
     * instance.
     * 
     * @param emHost
     * @param emPort
     * @param emLibPath
     * @return
     *         Ex:*SuperDomain*|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom
     *         Metric Agent (Virtual) (tas-itc-n9@5001)|Enterprise Manager|Connections:Number of
     *         Disallowed Agents
     */

    public int getNumberOfDisallowedAgentsAtCollector(String collectorHost, int collectorPort,
        String emLibPath) {
        int disallowedAgentCount = 0;
        for (int k = 0; k < 20; k++) {
            List<String> list =
                getConnectedAgentMetricForEMHost(
                    "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\) \\("
                        + collectorHost + "@" + collectorPort + "\\)",
                    "Enterprise Manager\\|Connections:Number of Disallowed Agents", collectorHost,
                    collectorPort, emLibPath);

            if (list.size() >= 3) {
                for (int i = 2; i < list.size(); i++) {
                    String value = list.get(i).split(",")[13];
                    LOGGER.debug("The value is " + value);
                    disallowedAgentCount = Integer.parseInt(value);
                }
            }
            if (list.size() < 3 || disallowedAgentCount < 1)
                harvestWait(15);
            else
                break;

        }
        LOGGER.info("Disallowed agent count at Collector is "+disallowedAgentCount);
        return disallowedAgentCount;
    }

    
    /**
     * Validate count of disallowed agents
     * Hover in a loop until disallowed count comes up as they might not show up in the first
     * instance.
     * 
     * @param emHost
     * @param emPort
     * @param emLibPath
     * @param count
     * @return
     */

    public int validateNumberOfDisallowedAgents(String emHost, int emPort, String emLibPath,int count) {
        int disallowedAgentCount = 0;
        for (int k = 0; k < 20; k++) {
            List<String> list =
                getConnectedAgentMetricForEMHost(
                    "(.*)\\|Custom Metric Host \\(Virtual\\)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)",
                    "Enterprise Manager\\|Connections:Number of Disallowed Agents", emHost, emPort,
                    emLibPath);
            if (list.size() >= 3) {
                for (int i = 2; i < list.size(); i++) {
                    String value = list.get(i).split(",")[13];
                    LOGGER.debug("The value is " + value);
                    disallowedAgentCount = Integer.parseInt(value);
                }
            }
            if (list.size() < 3 || disallowedAgentCount < count)
                harvestWait(15);
            else
                break;

        }
        LOGGER.info("Disallowed agent count is "+disallowedAgentCount);
        return disallowedAgentCount;
    }



    /**
     * Get disallowed agents metrics
     * Hover in a loop until disallowed count comes up as they might not show up in the first
     * instance.
     * 
     * @param emHost
     * @param emPort
     * @param emLibPath
     * @param agentExpression
     * @param metricExpression
     * @return
     */

    public int getDisallowedAgentMetrics(String emHost, int emPort, String emLibPath,
        String agentExpression, String metricExpression) {
        int metricValue = 0;
        for (int k = 0; k < 80; k++) {
            List<String> list =
                getConnectedAgentMetricForEMHost(agentExpression, metricExpression, emHost, emPort,
                    emLibPath);
            if (list.size() >= 3) {
                for (int i = 2; i < list.size(); i++) {
                    String value = list.get(i).split(",")[13];
                    LOGGER.debug("The value is " + value);
                    metricValue = Integer.parseInt(value);
                }
            }
            if (list.size() < 3 || metricValue < 1)
                harvestWait(15);
            else
                break;

        }
        return metricValue;
    }


    /**
     * validate disallowed agents metrics
     * Hover in a loop until disallowed count comes up as they might not show up in the first
     * instance.
     * 
     * @param emHost
     * @param emPort
     * @param emLibPath
     * @param agentExpression
     * @param metricExpression
     * @return
     */

    public int validateDisallowedAgentMetricsByCount(String emHost, int emPort, String emLibPath,
        String agentExpression, String metricExpression, int count) {
        int metricValue = 0;
        boolean flag=false;
        for (int k = 0; k < 80; k++) {
            List<String> list =
                getConnectedAgentMetricForEMHost(agentExpression, metricExpression, emHost, emPort,
                    emLibPath);
            if (list.size() >= 3) {
                for (int i = 2; i < list.size(); i++) {
                    String value = list.get(i).split(",")[13];
                    LOGGER.debug("The value is " + value);
                    metricValue = Integer.parseInt(value);
                    if(metricValue == count)
                    {
                        flag=true;
                        break;
                    }
                }
            }
            if(flag)
                break;
            else
                harvestWait(15);
        }
        return metricValue;
    }

   
    /**
     * Get the list of metrics for the agent which is connected to the EM Host
     *
     * @param agentExpression
     * @param metricExpression
     * @param emHost
     * @param emPort
     * @param emLibDir
     * @return
     */

    public List<String> getConnectedAgentMetricForEMHost(String agentExpression,
        String metricExpression, String emHost, int emPort, String emLibDir) {
        List<String> nodeList = null;
        String user="admin";
        String password="";
        try {
            nodeList =
                getMetricValueForTimeInMinutes(user, password, agentExpression, metricExpression,
                    emHost, emPort, emLibDir, 1);
            LOGGER.debug("The list size is : " + nodeList.size());
        } catch (Exception e) {
            LOGGER.error("Please check the command properly");
            LOGGER.error("It could be that emhost is not just up for running the CLW command");
        }
        return nodeList;
    }

    public void waitForAgentNodes(String expression, String emHost, int emPort, String emLibDir) {
        String user="Admin";
        String password="";
        int i = 0;
        List<String> nodeList;
        for (i = 0; i < 80; i++) {
            nodeList = getNodeList(user, password, expression, emHost, emPort, emLibDir);
            if (nodeList.size() >= 1)
                break;
            else
                harvestWait(15);
        }
        if (i == 80)
            Assert.assertTrue(false);
        else
            LOGGER.info("Agent connected to EM after " + i + " iterations taking " + i * 15
                + " seconds");
    }
    
    public void harvestWait(int seconds) {
        try {
            LOGGER.info("Harvesting crops for " + String.valueOf(seconds) + " seconds");
            Thread.sleep(seconds * 1000);
            LOGGER.info("Crops harvested.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
    /**
     * Get the list of agents and return true if given agent/text found
     *
     * @param user
     * @param password
     * @param expression
     * @param emHost
     * @param emPort
     * @param emLibDir
     * @param textToFind
     * @return boolean
     */

    public boolean isAgentPresent(String user, String password, String expression, String emHost, int emPort, String emLibDir,String textToFind) {

        boolean result=false;
        int i = 0;
        List<String> nodeList;
        for (i = 0; i < 80; i++) {
            nodeList = getNodeList(user, password, expression, emHost, emPort, emLibDir);
            if (nodeList.size() >= 1){
                 if(nodeList.toString().contains(textToFind)){
                     result= true;
                     break;
                 }
            }
            else
                harvestWait(15);
        }
        if (i == 80)
            result=false;
        else
            LOGGER.info("Agent connected to EM after " + i + " iterations taking " + i * 15
                + " seconds");
        return result;
    }
    
    /**
     * Method to shutdown the local EM, returns true if EM Shutdown is successful
     *
     * @param user
     * @param password
     * @param emHost
     * @param emPort
     * @param emLibDir
     * @return boolean
     */
    public boolean shutDownLocalEM(String user, String password, String host, String port,
        String emLibDir) {
        boolean isEMshut = true;
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(Integer.parseInt(port)).clwWorkStationDir(emLibDir)
                .user(user).password(password).build();

        String command ="shutdown";
        LOGGER.info("The command is..." + command);
        List<String> result = clwRunner.runClw(command);

        for (int i = 0; i < result.size(); i++) {
            if (result.get(i).contains("Unable to connect to the Enterprise Manager") || result.get(i).contains("Unable to login to the Enterprise Manager"))
                isEMshut = false;
        }
        LOGGER.info("The result is...." + isEMshut);
        return isEMshut;

    }

    /**
     * Method to rename the dashboard 
     * 
     * @param user
     * @param password
     * @param host
     * @param port
     * @param emLibDir
     * @param dashboardName
     * @param managmentmodule name
     * @param newDashboardName
     * @return boolean
     */
    public boolean renameDashboard(String user,String password,String host,String port,String emLibDir,String dashboardName,String managementModuleName,String newDashboardName)
    {
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(Integer.parseInt(port)).clwWorkStationDir(emLibDir)
                .user(user).password(password).build();
        
        String reNamecommand ="rename dashboard named \"(.*"+dashboardName+".*)\" in management module named \"(.*"+managementModuleName+".*)\" to  \""+newDashboardName+"\"";
        clwRunner.runClw(reNamecommand);
        harvestWait(30);
        String verifyRenamedDb="list dashboards matching \"(.*"+newDashboardName+".*)\" in management modules matching \"(.*"+managementModuleName+".*)\"";
        List<String> resultdbResult=clwRunner.runClw(verifyRenamedDb);
        for(String dbName:resultdbResult)
        {
            LOGGER.info("dashboard Name "+dbName);
            if(dbName.contains(newDashboardName))
                return true;
        }
        
        LOGGER.info("Unable to rename dashboard....");
        return false;
    }

    
    /**
     * Method to delete the dashboard 
     * 
     * @param user
     * @param password
     * @param host
     * @param port
     * @param emLibDir
     * @param dashboardName
     * @param managmentmodule name
     * @param newDashboardName
     * @return boolean
     */
    public boolean deleteDashboard(String user,String password,String host,String port,String emLibDir,String dashboardName,String managementModuleName)
    {
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(Integer.parseInt(port)).clwWorkStationDir(emLibDir)
                .user(user).password(password).build();
        
        String reNamecommand ="delete dashboards matching \"(.*"+dashboardName+".*)\" in management module matching \"(.*"+managementModuleName+".*)\"";
        clwRunner.runClw(reNamecommand);
        String verifyRenamedDb="list dashboards matching \"(.*"+dashboardName+".*)\" in management modules matching \"(.*"+managementModuleName+".*)\"";
        List<String> resultdbResult=clwRunner.runClw(verifyRenamedDb);
        for(String dbName:resultdbResult)
        {
            LOGGER.info("dashboard Name "+dbName);
            if(!dbName.contains(dashboardName))
                return true;
        }
        
        LOGGER.info("Unable to delete dashboard....");
        return false;
    }
    
    
    /**
     * Method to verify transaction trace 
     * 
     * @param user
     * @param password
     * @param expression
     * @param host
     * @param port
     * @param emLibDir
     * @param logMessage
     * @return
     */

    public boolean verifyTransactionTrace(String user, String password, String agentExpr,
        String host, int port, String emLibDir) {

        String logMessage = "Writing Transaction Trace Data to";
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        final String command =
            (new StringBuilder("trace transactions exceeding 1 ms in agents matching \"("
                + agentExpr + ")\" for 60 seconds")).toString();

        LOGGER.info(command);
        List<String> transactions = clwRunner.runClw(command);

        String str;
        boolean flag = false;

        Iterator<String> it = transactions.iterator();

        while (it.hasNext()) {
            str = it.next();
            if (str.contains(logMessage)) {
                flag = true;
                break;
            }
        }

        return flag;
    }
    
    /**
     * Method to verify transaction trace without generating traces
     * 
     * @param user
     * @param password
     * @param expression
     * @param host
     * @param port
     * @param emLibDir
     * @param logMessage
     * @return
     */

    public boolean verifyTransactionTracewithnoTraces(String user, String password, String agentExpr,
        String host, int port, String emLibDir) {

        String logMessage = "No transaction traces collected...";
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(port).clwWorkStationDir(emLibDir).user(user)
                .password(password).build();

        final String command =
            (new StringBuilder("trace transactions exceeding 1 ms in agents matching \"("
                + agentExpr + ")\" for 60 seconds")).toString();

        LOGGER.info(command);
        List<String> transactions = clwRunner.runClw(command);

        String str;
        boolean flag = false;

        Iterator<String> it = transactions.iterator();

        while (it.hasNext()) {
            str = it.next();
            if (str.equals(logMessage)) {
                flag = true;
                break;
            }
        }

        return flag;
    }
    
    /**
     * Method to activate Management Module 
     * 
     * @param user
     * @param password
     * @param host
     * @param port
     * @param emLibDir    
     * @param managmentmodule name    
     * @return boolean
     */
    public boolean activateManamementModule(String user,String password,String host,String port,String emLibDir,String managementModuleName)
    {
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(Integer.parseInt(port)).clwWorkStationDir(emLibDir)
                .user(user).password(password).build();
        
        String activateMMCommand = "activate management modules matching " + managementModuleName;
        List<String> activateMMCommandList = clwRunner.runClw(activateMMCommand);
        for(String activateMMResult:activateMMCommandList)
        {
            LOGGER.info("activateMM " + activateMMResult);
            if(activateMMResult.contains(managementModuleName + ":Active"))
                return true;
        }
        
        LOGGER.info("Unable to activate Management Module....");
        return false;
    }

    /**
     * Method to deactivate Management Module 
     * 
     * @param user
     * @param password
     * @param host
     * @param port
     * @param emLibDir    
     * @param managmentmodule name    
     * @return boolean
     */
    public boolean deactivateManamementModule(String user,String password,String host,String port,String emLibDir,String managementModuleName)
    {
        ClwRunner.Builder clwBuilder = new ClwRunner.Builder();
        ClwRunner clwRunner =
            clwBuilder.host(host).port(Integer.parseInt(port)).clwWorkStationDir(emLibDir)
                .user(user).password(password).build();
        
        String deactivateMMCommand = "deactivate management modules matching " + managementModuleName;
        List<String> deactivateMMCommandList = clwRunner.runClw(deactivateMMCommand);
        for(String deactivateMMResult:deactivateMMCommandList)
        {
            LOGGER.info("deactivateMM " + deactivateMMResult);
            if(deactivateMMResult.contains(managementModuleName + ":Inactive"))
                return true;
        }
        
        LOGGER.info("Unable to deactivate Management Module....");
        return false;
    }

}
