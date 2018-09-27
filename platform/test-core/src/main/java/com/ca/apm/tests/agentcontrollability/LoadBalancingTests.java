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
 * Date : 11/03/2016
 */
package com.ca.apm.tests.agentcontrollability;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.tests.testbed.AgentLoadBalancingHVRLinuxClusterTestbed;
import com.ca.apm.tests.testbed.AgentLoadBalancingLinuxTestbed;


public class LoadBalancingTests extends LoadBalancingCommons {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancingTests.class);


    public LoadBalancingTests() {}
    
    /**
     * Author: JAMSA07
     * Verify the config changes are HOT (means do not need a restart)
     */
    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_298385_HotConfig() {

        backupPropAndLoadBalancingFiles();
        setIntroscopeEMProperties();
        harvestWait(120);

        startEMServices();
        waitForAgentNodes(".*", momhost, Integer.parseInt(momPort), emLibDir);

        try {
            xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "Include-HotConfig",
                ".*\\|.*\\|.*Tomcat.*", collector1Host + ":" + c1Port, "include");
        } catch (Exception e) {
            e.printStackTrace();
        }

        detectHotChanges("Detected change to loadbalancing.xml");

        replaceProp("introscope.enterprisemanager.clustering.login.em1.weight=1",
            "introscope.enterprisemanager.clustering.login.em1.weight=2",
            AgentLoadBalancingHVRLinuxClusterTestbed.MOM_MACHINE_ID, configFile);

        detectHotChanges("weight=2");
        detectHotChanges("Rebalancing");

        stopEMServices();
        revertPropAndLoadBalancingFiles("298385");
    }

    /**
     * Author: JAMSA07
     * Verify if the loadbalancing file changes are taken even if it is added after EM service is up
     * and running.
     * 
     */

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_298395_MissingFile() {

        backupPropAndLoadBalancingFiles();
        setIntroscopeEMProperties();

        try {
            xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "Missing-File",
                ".*\\|.*\\|.*Tomcat.*", collector1Host + ":" + c1Port, "include");

        } catch (Exception e) {
            e.printStackTrace();
        }

        renameFile(loadBalanceFile, loadBalanceFile_Copy,
            AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID);

        startEMServices();
        waitForAgentNodes(".*", momhost, Integer.parseInt(momPort), emLibDir);

        renameFile(loadBalanceFile_Copy, loadBalanceFile,
            AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID);
        detectHotChanges("Detected change to loadbalancing.xml");

        List<String> list =
            checkForCollectorAgents(".*Tomcat.*", MetricExpression, collector1Host,
                Integer.parseInt(c1Port), emLibDir);
        Iterator<String> i = list.iterator();
        boolean found = false;
        while (i.hasNext()) {
            if (i.next().toString().trim().toLowerCase().contains("tomcat")) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);

        stopEMServices();
        revertPropAndLoadBalancingFiles("298395");
    }

    /**
     * Author: JAMSA07
     * Verify the scenario where the exclude option in loadbalancing is empty (No collectors added)
     */

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_298391_EmptyExclude() {

        backupPropAndLoadBalancingFiles();
        setIntroscopeEMProperties();

        startEMServices();
        waitForAgentNodes(".*", momhost, Integer.parseInt(momPort), emLibDir);

        try {
            xmlUtil.addEmptyCollectorEntryInLoadbalanceXML(loadBalanceFile, "Empty-Exclude",
                ".*\\|.*\\|.*", "exclude");

        } catch (Exception e) {
            e.printStackTrace();
        }
        detectHotChanges("Detected change to loadbalancing.xml");
        verifyAllCollectors();

        stopEMServices();
        revertPropAndLoadBalancingFiles("298391");
    }

    /**
     * Author: JAMSA07
     * Verify the scenario where the include option in loadbalancing is empty (No collectors added)
     */

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_280472_EmptyInclude() {

        backupPropAndLoadBalancingFiles();
        setIntroscopeEMProperties();

        startEMServices();
        waitForAgentNodes(".*", momhost, Integer.parseInt(momPort), emLibDir);

        try {
            xmlUtil.addEmptyCollectorEntryInLoadbalanceXML(loadBalanceFile, "Empty-Include",
                ".*\\|.*\\|.*", "include");

        } catch (Exception e) {
            e.printStackTrace();
        }
        detectHotChanges("Detected change to loadbalancing.xml");
        verifyAllCollectors();

        stopEMServices();
        revertPropAndLoadBalancingFiles("280472");
    }

    /**
     * Author: JAMSA07
     * Verify the scenario where the include option in loadbalancing is invalid (No valid collector
     * name added)
     */

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_298397_InvalidInclude() {

        backupPropAndLoadBalancingFiles();
        setIntroscopeEMProperties();

        startEMServices();
        waitForAgentNodes(".*", momhost, Integer.parseInt(momPort), emLibDir);

        try {
            xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "Invalid-Include",
                ".*\\|.*\\|.*", "jamsa" + ":" + c1Port, "include");
        } catch (Exception e) {
            e.printStackTrace();
        }

        detectHotChanges("Detected change to loadbalancing.xml");

        verifyAllCollectors();

        stopEMServices();
        revertPropAndLoadBalancingFiles("298397");
    }

    /**
     * Author: JAMSA07
     * Verify the scenario where the include option in loadbalancing has 2 entries
     * 
     */

    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_298396_TwoInclude() {

        backupPropAndLoadBalancingFiles();
        setIntroscopeEMProperties();

        startEMServices();
        waitForAgentNodes(".*", momhost, Integer.parseInt(momPort), emLibDir);

        try {
            xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "TwoInclude-Agent1",
                ".*\\|.*\\|.*Tomcat.*", collector1Host + ":" + c1Port, "include");
            xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "TwoInclude-Agent2",
                ".*\\|.*\\|.*JBoss.*",  collector2Host + ":" + c2Port, "include");
        } catch (Exception e) {
            e.printStackTrace();
        }

        detectHotChanges("Detected change to loadbalancing.xml");
        List<String> list =
            checkForCollectorAgents(".*Tomcat.*", MetricExpression, collector1Host,
                Integer.parseInt(c1Port), emLibDir);
        Iterator<String> i = list.iterator();

        boolean found = false;
        while (i.hasNext()) {
            if (i.next().toString().trim().toLowerCase().contains("tomcat")) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);

        list.clear();
        list =
            checkForCollectorAgents(".*JBoss.*", MetricExpression, collector2Host,
                Integer.parseInt(c2Port), emLibDir);
        i = list.iterator();

        found = false;
        while (i.hasNext()) {
            if (i.next().toString().trim().toLowerCase().contains("jboss")) {
                found = true;
                break;
            }
        }

        Assert.assertTrue(found);

        stopEMServices();
        revertPropAndLoadBalancingFiles("298396");
    }

    /**
     * Author: JAMSA07
     * Verify the scenario where the exclude option in loadbalancing has 2 entries
     * 
     */

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_298389_TwoExclude() {

        backupPropAndLoadBalancingFiles();
        setIntroscopeEMProperties();

        startEMServices();
        waitForAgentNodes(".*", momhost, Integer.parseInt(momPort), emLibDir);

        try {
            xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "Exclude-Agent1",
                ".*\\|.*\\|.*Tomcat.*", collector1Host + ":" + c1Port, "exclude");
            xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "Exclude-Agent2",
                ".*\\|.*\\|.*JBoss.*",  collector2Host + ":" + c2Port, "exclude");

        } catch (Exception e) {
            e.printStackTrace();
        }
        detectHotChanges("Detected change to loadbalancing.xml");
        List<String> list =
            checkForCollectorAgents(".*Tomcat.*", MetricExpression, collector1Host,
                Integer.parseInt(c1Port), emLibDir);
        Iterator<String> i = list.iterator();

        boolean found = false;
        while (i.hasNext()) {
            if (i.next().toString().trim().toLowerCase().contains("tomcat")) {
                found = true;
                break;
            }
        }
        Assert.assertFalse(found);

        list =
            checkForCollectorAgents(".*JBoss.*", MetricExpression, collector2Host,
                Integer.parseInt(c2Port), emLibDir);
        i = list.iterator();


        found = false;
        while (i.hasNext()) {
            if (i.next().toString().trim().toLowerCase().contains("jboss")) {
                found = true;
                break;
            }
        }
        Assert.assertFalse(found);

        stopEMServices();
        revertPropAndLoadBalancingFiles("298389");

    }

    /**
     * AUTHOR: DWIBA01
     * Verify in Load balancing Agent connected to particular Agent . And  if particular collector
     * down it will connect MOM in disallowed state
     */

    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_205199_Loadbalancing_Dot_Xml_Include_Single_Collector() {

        backupPropAndLoadBalancingFiles();
        setIntroscopeEMProperties();

        startEMServices();
        waitForAgentNodes(".*", momhost, Integer.parseInt(momPort), emLibDir);

        try {
            xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "TwoInclude-Agent1",
                ".*\\|.*\\|.*Tomcat.*", collector1Host + ":" + c1Port, "include");
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> list =
            checkForCollectorAgents(".*Tomcat.*", MetricExpression, collector1Host,
                Integer.parseInt(c1Port), emLibDir);
        Iterator<String> i = list.iterator();

        boolean found = false;
        while (i.hasNext()) {
            if (i.next().toString().trim().toLowerCase().contains("tomcat")) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);

        stopCollectorEM(momRoleId, col1RoleID);
        stopEMServiceFlowExecutor(AgentLoadBalancingLinuxTestbed.COLLECTOR1_MACHINE_ID);

        detectHotChanges("Removed Collector:");
        detectHotChanges("No eligible collector for:");

        stopEMServices();
        revertPropAndLoadBalancingFiles("205199");
    }

    /**
     * AUTHOR: DWIBA01
     * Verify the loadbalancing changes are HOT (means do not need a restart)
     * 
     */

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_205217_Loadbalancing_Dot_Xml_Hot_Config() {

        backupPropAndLoadBalancingFiles();
        setIntroscopeEMProperties();

        startEMServices();
        waitForAgentNodes(".*", momhost, Integer.parseInt(momPort), emLibDir);

        List<String> list =
            checkForCollectorAgents(".*Tomcat.*", MetricExpression, collector1Host,
                Integer.parseInt(c1Port), emLibDir);
        Iterator<String> i = list.iterator();

        boolean found = false;

        try {
            xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "Include-HotConfig",
                ".*\\|.*\\|.*Tomcat.*", collector2Host + ":" + c2Port, "include");
        } catch (Exception e) {
            e.printStackTrace();
        }

        list.clear();
        list =
            checkForCollectorAgents(".*Tomcat.*", MetricExpression, collector2Host,
                Integer.parseInt(c2Port), emLibDir);
        i = list.iterator();

        found = false;
        while (i.hasNext()) {
            if (i.next().toString().trim().toLowerCase().contains("tomcat")) {
                found = true;
                break;
            }
        }
        Assert.assertTrue(found);

        stopEMServices();
        revertPropAndLoadBalancingFiles("205217");
    }

    /**
     * AUTHOR: DWIBA01
     * Verify in Load balancing Agent connected to Collector First and then  Mom if particular
     * collector not present 
     * 
     */

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_298382_Sticky_Agent() {

        LOGGER.info("verify_ALM_298382_Sticky_Agent Start ");
        
        backupPropAndLoadBalancingFiles();
        setIntroscopeEMProperties();
        LOGGER.info("verify_ALM_298382_Sticky_Agent completed backup ");
        stopTomcatAgent(tomcatRoleId);
        stopTomcatServiceFlowExecutor(AgentLoadBalancingLinuxTestbed.AGENT_MACHINE_ID);
        LOGGER.info("verify_ALM_298382_Sticky_Agent stop tomcat Agent ");
        
        backupFile(agentProfile, agentProfile+"_backup", AgentLoadBalancingLinuxTestbed.AGENT_MACHINE_ID);
        setAgentProperties();
        
        startEMServices();
        harvestWait(180);
        startTomcatAgent(tomcatRoleId);
        LOGGER.info("verify_ALM_298382_Sticky_Agent start tomcat Agent ");
        
        harvestWait(180);
        waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
        LOGGER.info("verify_ALM_298382_Sticky_Agent start All EM and collector completed ");
       
        Assert.assertTrue(verifyColTomcatAgent(collector1Host, c1Port));

        LOGGER.info("verify_ALM_298382_Sticky_Agent start Validate Agent connected to Collector  ");

        stopCollectorEM(momRoleId, col1RoleID);
        stopEMServiceFlowExecutor(AgentLoadBalancingLinuxTestbed.COLLECTOR1_MACHINE_ID);
        
        LOGGER.info("verify_ALM_298382_Sticky_Agent stop collector completed ");
        harvestWait(180);
        boolean found = false;
        found = verifyColTomcatAgent(collector2Host, c2Port);
        if (found)
            Assert.assertTrue(found);
        else {
            LOGGER.info("verify_ALM_298382_Sticky_Agent tomcact not found in collector 2 - Initiate validating collector 3 for Tomcat ");
            found = verifyColTomcatAgent(collector3Host, c3Port);
            Assert.assertTrue(found);
        }
        LOGGER.info("verify_ALM_298382_Sticky_Agent Agent redireect to other collector ");

        startEM(col1RoleID);
        harvestWait(180);
        LOGGER.info("verify_ALM_298382_Sticky_Agent collector1 started ");
        stopTomcatAgent(tomcatRoleId);
        stopTomcatServiceFlowExecutor(AgentLoadBalancingLinuxTestbed.AGENT_MACHINE_ID);
        waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
        LOGGER.info("verify_ALM_298382_Sticky_Agent Agent stop completed  ");
        startTomcatAgent(tomcatRoleId);
        waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir); 
        
        Assert.assertTrue(verifyColTomcatAgent(collector1Host, c1Port));
        LOGGER.info("verify_ALM_298382_Sticky_Agent Agent connected to collector 1  ");

        stopCollectorEM(momRoleId, col1RoleID);
        stopEMServiceFlowExecutor(AgentLoadBalancingLinuxTestbed.COLLECTOR1_MACHINE_ID);

        stopCollectorEM(momRoleId, col2RoleID);
        stopEMServiceFlowExecutor(AgentLoadBalancingLinuxTestbed.COLLECTOR2_MACHINE_ID);
        LOGGER.info("verify_ALM_298382_Sticky_Agent collector 1 & 2 stop completed  ");
        harvestWait(180);
        Assert.assertTrue(verifyColTomcatAgent(collector3Host, c3Port));
        LOGGER.info("verify_ALM_298382_Sticky_Agent Agnet connected to  collector 3  ");
        stopEMServices();
        revertPropAndLoadBalancingFiles("298382");
        LOGGER.info("verify_ALM_298382_Sticky_Agent test case End  ");
        
        stopTomcatAgent(tomcatRoleId);
        stopTomcatServiceFlowExecutor(AgentLoadBalancingLinuxTestbed.AGENT_MACHINE_ID);

        deleteFile(agentProfile, AgentLoadBalancingLinuxTestbed.AGENT_MACHINE_ID);
        moveFile(agentProfile+"_backup", agentProfile, AgentLoadBalancingLinuxTestbed.AGENT_MACHINE_ID);
        
        startTomcatAgent(tomcatRoleId);
    }

    /**
     * Author: JAMSA07
     * Verify the wait time period HOT config changes
     */

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_298398_InitialWaitTimePeriod() {

        backupPropAndLoadBalancingFiles();
        setIntroscopeEMProperties();

        startEMServices();
        waitForAgentNodes(".*", momhost, Integer.parseInt(momPort), emLibDir);

        verifyAllCollectors();

        stopEMServices();
        revertPropAndLoadBalancingFiles("298398");
    }

    /**
     * Author: JAMSA07
     * Verify if the MOM changes the agent connectivity from Collector to collector appropriately
     * based on the operations.
     */

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_298384_HTTPTunnelling() {

        backupPropAndLoadBalancingFiles();
        setIntroscopeEMProperties();
        stopAllAgents();

        String host = "";

        /**
         * Add the property
         * transport.enable.isengard.http.tunneling=false
         * 
         */

        backupFile(configFile, configFile + "_backup",
            AgentLoadBalancingLinuxTestbed.COLLECTOR1_MACHINE_ID);

        List<String> colWeights = new ArrayList<String>();
        colWeights.add("transport.enable.isengard.http.tunneling=false");
        appendProp(colWeights, AgentLoadBalancingLinuxTestbed.COLLECTOR1_MACHINE_ID, configFile);

        /**
         * Agent property modification
         **/

        backupFile(agentProfile, agentProfile + "_backup",
            AgentLoadBalancingLinuxTestbed.AGENT_MACHINE_ID);

        replaceProp("agentManager.url.1=" + momhost + momPort, "agentManager.url.1=http://"
            + momhost + momWebPort, AgentLoadBalancingLinuxTestbed.AGENT_MACHINE_ID, agentProfile);

        startTomcatAgent(tomcatRoleId);
        startJBossAgent(jbossRoleId);

        startEMServices();
        waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
        List<String> nodeList = null;

        boolean found = false;
        int i = 0;
        do {
            nodeList =
                clw.getMetricValueForTimeInMinutes(user, password, ".*Tomcat.*", MetricExpression,
                    collector1Host, Integer.parseInt(c1Port), emLibDir, 1);
            LOGGER.debug("The list size is : " + nodeList.size());
            i++;
            if (nodeList.size() > 3) harvestWait(5);
            if (i >= 10) {
                found = true;
                break;
            }
        } while (nodeList.size() > 2);

        Assert.assertFalse(found);

        List<String> list;
        list =
            getConnectedAgentNamesToCollector(collector1Host, Integer.parseInt(c1Port),
                ".*Tomcat.*");
        if (list.isEmpty()) {
            LOGGER.info("COL1 has no tomcat");
            list =
                getConnectedAgentNamesToCollector(collector2Host, Integer.parseInt(c2Port),
                    ".*Tomcat.*");
            if (list.isEmpty()) {
                LOGGER.info("COL2 has no tomcat");
                list =
                    getConnectedAgentNamesToCollector(collector3Host, Integer.parseInt(c3Port),
                        ".*Tomcat.*");
                if (list.isEmpty()) {
                    LOGGER.info("COL3 has no tomcat");
                    LOGGER.info("This is ridicuous");
                    Assert.assertTrue(false);
                }

                else
                    host = list.get(0);
            } else
                host = list.get(0);
        } else
            host = list.get(0);

        LOGGER.info("Tomcat is connected to " + host);

        String emToCheck = "";
        int port = 0;
        if (collector1Host.trim().equalsIgnoreCase(host.trim()))
            Assert.assertTrue(false);
        else if (collector2Host.trim().equalsIgnoreCase(host.trim())) {
            LOGGER.info("Collector 2 : " + collector2Host);
            stopCollectorEM(momRoleId, col2RoleID);
            emToCheck = collector3Host;
            port = Integer.parseInt(c3Port);
        }

        else if (collector3Host.trim().equalsIgnoreCase(host.trim())) {
            LOGGER.info("Collector 3 : " + collector3Host);
            stopCollectorEM(momRoleId, col3RoleID);
            emToCheck = collector2Host;
            port = Integer.parseInt(c2Port);
        } else
            Assert.assertTrue(false);

        harvestWait(180);

        if (emToCheck != "" && port != 0)
            list = getConnectedAgentNamesToCollector(emToCheck, port, ".*Tomcat.*");
        else
            LOGGER.info("Somethng is wrong");

        if (list.isEmpty()) Assert.assertTrue(false);

        LOGGER.info("" + list);
        stopEMServices();
        revertPropAndLoadBalancingFiles("298384");
        deleteFile(configFile, AgentLoadBalancingLinuxTestbed.COLLECTOR1_MACHINE_ID);
        moveFile(configFile + "_backup", configFile,
            AgentLoadBalancingLinuxTestbed.COLLECTOR1_MACHINE_ID);

        stopAllAgents();

        deleteFile(agentProfile, AgentLoadBalancingLinuxTestbed.AGENT_MACHINE_ID);
        moveFile(agentProfile + "_backup", agentProfile,
            AgentLoadBalancingLinuxTestbed.AGENT_MACHINE_ID);
        harvestWait(30);

        startJBossAgent(jbossRoleId);
        startTomcatAgent(tomcatRoleId);
    }

    /**
     * Author: JAMSA07
     * Verify the INVALID entry on the fly for LOADBALANCING.XML file does not throw a NULL POINTER
     * EXCEPTION
     * 
     */

    @Test(groups = {"DEEP"}, enabled = true)
    public void verify_ALM_419916_MomNPE() {

        backupPropAndLoadBalancingFiles();
        setIntroscopeEMProperties();

        startEMServices();
        waitForAgentNodes(".*", momhost, Integer.parseInt(momPort), emLibDir);

        try {
            xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "TwoInclude-Agent1",
                ".*\\|.*\\|.*Tomcat.*", collector1Host + ":" + c1Port, "include");
            xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "Two-Include-Agent2",
                ".*\\|.*\\|.*JBoss.*", collector2Host + ":" + c2Port, "include");
        } catch (Exception e) {
            e.printStackTrace();
        }

        detectHotChanges("Detected change to loadbalancing.xml");

        deleteFile(loadBalanceFile, AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID);
        moveFile(loadBalanceFile + "_backup", loadBalanceFile,
            AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID);

        backupFile(loadBalanceFile, loadBalanceFile + "_backup",
            AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID);

        try {
            xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "TwoInclude-Agent1",
                ".*\\|.*\\|.*Tomcat.*", collector1Host + ":" + c1Port, "include");
            xmlUtil.addCollectorEntryInLoadbalanceXML(loadBalanceFile, "Two-Include-Agent2",
                ".*\\|.*\\|.*JBoss.*", "WrongInvalidEntry" + ":" + c2Port, "include");
        } catch (Exception e) {
            e.printStackTrace();
        }

        DontDetectTheseHotChanges("Exception: java.lang.NullPointerException");

        stopEMServices();
        revertPropAndLoadBalancingFiles("419916");
    }
    
    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_298400_AgentHistory() {
        
        backupPropAndLoadBalancingFiles();
        setIntroscopeEMProperties();
        startEMServices();
        waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
        
        String host="";
        List<String> list;
        list =
            getConnectedAgentNamesToCollector(collector1Host, Integer.parseInt(c1Port),
                ".*Tomcat.*");
        if (list.isEmpty()) {
            LOGGER.info("COL1 has no tomcat");
            list =
                getConnectedAgentNamesToCollector(collector2Host, Integer.parseInt(c2Port),
                    ".*Tomcat.*");
            if (list.isEmpty()) {
                LOGGER.info("COL2 has no tomcat");
                list =
                    getConnectedAgentNamesToCollector(collector3Host, Integer.parseInt(c3Port),
                        ".*Tomcat.*");
                if (list.isEmpty()) {
                    LOGGER.info("COL3 has no tomcat");
                    LOGGER.info("This is ridicuous");
                    Assert.assertTrue(false);
                }

                else
                    host = list.get(0);
            } else
                host = list.get(0);
        } else
            host = list.get(0);

        LOGGER.info("Tomcat is connected to " + host);

        stopAllAgents();
        harvestWait(30);
        startJBossAgent(jbossRoleId);
        startTomcatAgent(tomcatRoleId);

        waitForAgentNodes(".*Tomcat.*", momhost, Integer.parseInt(momPort), emLibDir);
        
        /**
         * Initializing port to the MOM port 
         */
        int port=Integer.parseInt(momPort);
        if(host.contains(collector1Host))
            port=Integer.parseInt(c1Port);
        else if(host.contains(collector1Host))
            port=Integer.parseInt(c1Port);
        else if(host.contains(collector1Host))
            port=Integer.parseInt(c1Port);
        
        /**
         * Testing to see if tomcat still connects to the same collector
         */
        
        list.clear();
        list = getConnectedAgentNamesToCollector(host, port, ".*Tomcat.*");
        
        Assert.assertTrue(!list.isEmpty());
    }

}
