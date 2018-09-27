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
 * Author : BALRA06/KETSW01
 */
package com.ca.apm.tests.agentcontrollability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.commons.coda.common.XMLUtil;

public class AgentControllabilityTests extends AgentControllabilityCommons {
    
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AgentControllabilityTests.class);

    public AgentControllabilityTests() {}

    @BeforeClass(alwaysRun = true)
    public void ACCInitialize() {
        super.ACCInitialize();
    }
    
    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_450351_Agent_redirection_is_not_handled_when_lb_has_incorrect_collector()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "col3", "tomcat"};
        setuptestcase("450351",serversList);
        startTomcatAgent(tomcatRoleId);
        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);
        startEM(collector3RoleId);

        clw.setAllowedAgentWithCollector(user, password, agent_collector_name,
                tomcatAgentExpression, "abcd", collector1Port, momHost,
                momPort, momLibDir); 
        harvestWait(300);

        String msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        
        isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg);
    
    } 

    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_440535_Test_affinity_functionality_of_lb_file()
            throws Exception {

        serversList = new String[]{"mom", "col1", "col2", "tomcat", "jboss"};
        setuptestcase("440535",serversList);
        String findStr = "AffinityAgent";
        replaceProp("introscope.agent.agentName=Tomcat Agent",
                "introscope.agent.agentName=AffinityAgent", tomcatMachineId,
                tomcatAgentProfile);
        replaceProp("introscope.agent.agentName=JBoss Agent",
                "introscope.agent.agentName=AffinityAgent", jbossMachineId,
                jbossAgentProfile);

        xmlutil.addlatchedEntryInLoadBalXML(loadBalanceFile, "Test-affinity",
                ".*\\|.*\\|.*AffinityAgent.*", collector1Host + ":"
                        + collector1Port,
                collector2Host + ":" + collector2Port, "1:true");

        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);
        startTomcatAgent(tomcatRoleId);
        startJBossAgent(jbossRoleId);
        harvestWait(60);

        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        int count = StringUtils.countMatches(tempResult1, findStr);
        Assert.assertEquals(
                "AffinityAgent is not connected to Collector1 though affinitiy is set for collector1 ",
                2, count);

        xmlutil.changelatchedEntryInLoadBalXML(loadBalanceFile,
                "Test-affinity", collector2Host + ":" + collector2Port,
                collector1Host + ":" + collector1Port);
        harvestWait(240);

        tempResult1 = clw.getNodeList(user, password, ".*", collector2Host,
                collector2Port, momLibDir).toString();
        count = StringUtils.countMatches(tempResult1, findStr);
        Assert.assertEquals(
                "AffinityAgent is not connected to Collector2 though affinitiy is set for collector2 ",
                2, count);

        stopCollectorEM(momRoleId,collector2RoleId);
        harvestWait(120);

        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        count = StringUtils.countMatches(tempResult1, findStr);
        Assert.assertEquals(
                "AffinityAgent is not connected to Collector1 though collector2 is shutdown ",
                2, count);

        startEM(collector2RoleId);
        harvestWait(240);

        tempResult1 = clw.getNodeList(user, password, ".*", collector2Host,
                collector2Port, momLibDir).toString();
        count = StringUtils.countMatches(tempResult1, findStr);
        Assert.assertEquals(
                "AffinityAgent is not connected back to Collector2 though it is up ",
                2, count);
    }

    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_268922_Overriden_Collector_settings_by_MOM_with_Agent()
            throws Exception {
            
        serversList = new String[]{"mom", "col1", "tomcat", "jboss"};       
        setuptestcase("268922",serversList);
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false",
                collector1MachineId, col1ConfigFile);
        startEM(momRoleId);
        startEM(collector1RoleId);
        startTomcatAgent(tomcatRoleId);
        harvestWait(60);

        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        Assert.assertTrue("Tomcat Agent is not connected to the Collector",
                tempResult1.contains("Tomcat"));

        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false",
                momMachineId, momConfigFile);
        harvestWait(120);

        startJBossAgent(jbossRoleId);
        harvestWait(60);

        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        Assert.assertTrue(
                "Both the agents are not connected to the appropriate MOM or Collector",
                tempResult1.contains("Tomcat")
                        && !tempResult1.contains("JBoss"));

        String msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        
        checkLogForMsg(envProperties, jbossMachineId, jbossLogFile, msg);
        
    }

    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_440678_Affinity_for_agents_connected_directly_to_collector()
            throws Exception {
            
        serversList = new String[]{"mom", "col1", "col2", "col3", "tomcat"};
        setuptestcase("440678",serversList);
        xmlutil.addlatchedEntryInLoadBalXML(loadBalanceFile, "Test-affinity",
                ".*\\|.*\\|.*Tomcat.*", collector1Host + ":" + collector1Port,
                collector2Host + ":" + collector2Port, "2:true");

        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);
        startEM(collector3RoleId);

        replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
                "agentManager.url.1=" + collector1Host + ":" + collector1Port,
                tomcatMachineId, tomcatAgentProfile);

        startTomcatAgent(tomcatRoleId);
        harvestWait(120);

        tempResult1 = clw.getNodeList(user, password, ".*", collector2Host,
                collector2Port, momLibDir).toString();
        Assert.assertTrue("Tomcat Agent is not connected to the Collector2",
                tempResult1.contains("Tomcat"));        

    }

    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_268901_UAC_permission_ON_for_ACC() throws Exception {

        serversList = new String[]{"mom", "col1", "col2"};
        setuptestcase("268901",serversList);
        XMLUtil.createUserInUsersXML(momConfigdir + "/users.xml", "ACC", "");
        Map<String, String> attributeMap = new HashMap<String, String>();
        attributeMap.put("user", "ACC");
        attributeMap.put("permission", "agent_control");
        XMLUtil.createElement(momConfigdir + "/server.xml", "grant", "",
                "server", "version", "0.2", attributeMap);

        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);

        tempResult1 = clw.getLoadBalancingXmlLastUpdatedTime("ACC", "",
                momHost, momPort, momLibDir);
        Assert.assertTrue(
                "CLW Query to get lastmodified timestamp has failed",
                tempResult1.contains("Sun") || tempResult1.contains("Mon") || tempResult1.contains("Tue") || tempResult1.contains("Wed") 
                || tempResult1.contains("Thu") || tempResult1.contains("Fri") || tempResult1.contains("Sat"));

        int i = xmlutil.addlatchedEntryInLoadBalXML(loadBalanceFile,
                "Test-affinity", ".*\\|.*\\|.*Tomcat.*", collector1Host + ":"
                        + collector1Port,
                collector2Host + ":" + collector2Port, "1:true");
        Assert.assertTrue(
                "Tomcat agent specifier entry is not added successfully to the loadbalancing file",
                i == 1);
        harvestWait(120);

        tempResult1 = clw.removeAgentExpression("ACC", "",
                ".*\\|.*\\|.*Tomcat.*", momHost, momPort, momLibDir).toString();
        Assert.assertTrue(
                "CLW command for removeAgentExpression failed to run successfully",
                tempResult1.contains("Command executed successfully"));     

    }

    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_296852_UAC_permission_ON_for_ACC_group()
            throws Exception {

        serversList = new String[]{"mom", "col1", "col2"};
        setuptestcase("296852",serversList);
        XMLUtil.createUserInUsersXML(momConfigdir + "/users.xml", "ACC1", "");
        XMLUtil.createUserInUsersXML(momConfigdir + "/users.xml", "ACC2", "");
        XMLUtil.createGroupAddMultipleUsersInUsersXML(momConfigdir
                + "/users.xml", "ACCgroup", "ACCgroup", "ACC1,ACC2");
        XMLUtil.createGroupGrantForElement(momConfigdir + "/server.xml",
                "server", "ACCgroup", "agent_control");

        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);

        tempResult1 = clw.getLoadBalancingXmlLastUpdatedTime("ACC1", "",
                momHost, momPort, momLibDir);
        Assert.assertTrue(
                "CLW Query to get lastmodified timestamp has failed",
                tempResult1.contains("Sun") || tempResult1.contains("Mon") || tempResult1.contains("Tue") || tempResult1.contains("Wed") 
                || tempResult1.contains("Thu") || tempResult1.contains("Fri") || tempResult1.contains("Sat"));

        int i = xmlutil.addlatchedEntryInLoadBalXML(loadBalanceFile,
                "Test-affinity", ".*\\|.*\\|.*Tomcat.*", collector1Host + ":"
                        + collector1Port,
                collector2Host + ":" + collector2Port, "1:true");
        Assert.assertTrue(
                "Tomcat agent specifier entry is not added successfully to the loadbalancing file",
                i == 1);
        harvestWait(120);
        tempResult1 = clw.removeAgentExpression("ACC2", "",
                ".*\\|.*\\|.*Tomcat.*", momHost, momPort, momLibDir).toString();
        Assert.assertTrue(
                "CLW command for removeAgentExpression failed to run successfully",
                tempResult1.contains("Command executed successfully")); 

    }

    @Test(groups = { "BAT" }, enabled = true)
    public void verify_ALM_268891_Verify_default_agent_connection_mode_property_in_Collector()
            throws Exception {
        
        serversList = new String[]{"empty"};
        setuptestcase("268891",serversList);

        try {
            isKeywordInFile(envProperties, collector1MachineId, col1ConfigFile,
                    "introscope.apm.agentcontrol.agent.allowed=true");
            isKeywordInFile(envProperties, collector1MachineId, col1ConfigFile,
                    "introscope.apm.agentcontrol.agent.emlistlookup.enable=true");
            isKeywordInFile(envProperties, collector1MachineId, col1ConfigFile,
                    "introscope.enterprisemanager.agent.disallowed.connection.limit=0");
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.assertTrue("Log check failed because of the Exception : "
                    + e, false);
        }
    }

    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_268890_Verify_default_agent_connection_mode_property_in_MOM()
            throws Exception {
        
        serversList = new String[]{"empty"};
        setuptestcase("268890",serversList);

        try {
            isKeywordInFile(envProperties, momMachineId, momConfigFile,
                    "introscope.apm.agentcontrol.agent.allowed=true");
            isKeywordInFile(envProperties, momMachineId, momConfigFile,
                    "introscope.apm.agentcontrol.agent.emlistlookup.enable=true");
            isKeywordInFile(envProperties, momMachineId, momConfigFile,
                    "introscope.enterprisemanager.agent.disallowed.connection.limit=0");
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.assertTrue("Log check failed because of the Exception : "
                    + e, false);
        }

    }

    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_295431_no_ACC_for_direct_collector_connection_global_allow()
            throws Exception {

        serversList = new String[]{"mom", "col1", "col2", "tomcat"};
        setuptestcase("295431",serversList);
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false",
                collector1MachineId, col1ConfigFile);
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false",
                collector2MachineId, col2ConfigFile);
        replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
                "agentManager.url.1=" + collector1Host + ":" + collector1Port,
                tomcatMachineId, tomcatAgentProfile);

        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);
        startTomcatAgent(tomcatRoleId);
        harvestWait(60);

        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        Assert.assertTrue("Tomcat Agent is not connected to the Collector1",
                tempResult1.contains("Tomcat"));

        stopEM(momRoleId);
        stopCollectorEM(momRoleId,collector1RoleId);
        stopCollectorEM(momRoleId,collector2RoleId);
        startEM(collector1RoleId);      

        String msg = "Connected to "
                + collector1Host
                + ":"
                + collector1Port
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
            
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);

    }

    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_450352_Enhancement_to_add_agent_name_in_collector_rejection_log_msgs_on_MOM_backlog()
            throws Exception {

        serversList = new String[]{"mom", "col1", "col2", "tomcat"};
        setuptestcase("450352",serversList);
        replaceProp("log4j.logger.Manager=INFO, console, logfile",
                "log4j.logger.Manager=DEBUG, console, logfile", momMachineId,
                momConfigFile);

        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);

        clw.setDisAllowedAgentWithCollector(user, password,
                agent_collector_name, tomcatAgentExpression, collector1Host,
                collector1Port, momHost, momPort, momLibDir);
        clw.setDisAllowedAgentWithCollector(user, password,
                agent_collector_name, tomcatAgentExpression, collector2Host,
                collector2Port, momHost, momPort, momLibDir);
        harvestWait(120);

        startTomcatAgent(tomcatRoleId);     

        String msg1 = "Reject " + collector1Host + "@" + collector1Port
                + " because agent is excluded";     
        checkLogForMsg(envProperties, momMachineId, momLogFile, msg1);

        msg1 = "Reject " + collector2Host + "@" + collector2Port
                + " because agent is excluded";     
        checkLogForMsg(envProperties, momMachineId, momLogFile, msg1);

    }

    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_268902_UAC_OFF_for_ACC_Only_on_Cluster()
            throws Exception {

        serversList = new String[]{"mom", "col1", "col2"};
        setuptestcase("268902",serversList);        
        String accuser = "ACC";
        XMLUtil.createUserInUsersXML(momConfigdir + "/users.xml", accuser, "");

        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);

        tempResult1 = clw.getLoadBalancingXmlLastUpdatedTime("ACC", "",
                momHost, momPort, momLibDir);
        Assert.assertTrue(
                "Expected output from CLW is not found",
                tempResult1
                        .contains("com.wily.introscope.permission.PermissionException: User "
                                + accuser
                                + " does not have sufficient permissions in domain Server Resource"));

    }

    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_303682_Agent_does_not_iterate_through_EM_list_when_disallowed()
            throws Exception {

        serversList = new String[]{"col1", "col2", "col3", "tomcat"};
        setuptestcase("303682",serversList);
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false",
                collector1MachineId, col1ConfigFile);
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false",
                collector2MachineId, col2ConfigFile);
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false",
                collector3MachineId, col3ConfigFile);
        replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
                "agentManager.url.1=" + collector1Host + ":" + collector1Port,
                tomcatMachineId, tomcatAgentProfile);
        List<String> appendproplist = new ArrayList<String>();
        appendproplist.add("agentManager.url.2=" + collector2Host + ":"
                + collector2Port);
        appendproplist.add("agentManager.url.3=" + collector3Host + ":"
                + collector3Port);
        appendProp(appendproplist, tomcatMachineId, tomcatAgentProfile);

        startEM(collector1RoleId);
        startEM(collector2RoleId);
        startEM(collector3RoleId);
        startTomcatAgent(tomcatRoleId);     

        String msg1 = "Lost contact with the Introscope Enterprise Manager at "
                + collector1Host + ":" + collector1Port;        
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);

        msg1 = "Lost contact with the Introscope Enterprise Manager at "
                + collector2Host + ":" + collector2Port;        
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);

        msg1 = "Lost contact with the Introscope Enterprise Manager at "
                + collector3Host + ":" + collector3Port;        
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);

        msg1 = "Connected to "
                + collector3Host
                + ":"
                + collector3Port
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);
        
    }

    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_269835_Agent_ALLOW_DISALLOW_REDIRECT_one_MOM_three_Collectors()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "col3", "tomcat", "jboss"};
        setuptestcase("269835",serversList);
        replaceProp("introscope.agent.agentName=Tomcat Agent",
                "introscope.agent.agentName=AffinityAgent", tomcatMachineId,
                tomcatAgentProfile);
        replaceProp("introscope.agent.agentName=JBoss Agent",
                "introscope.agent.agentName=AffinityAgent", jbossMachineId,
                jbossAgentProfile);

        startEM(momRoleId);
        clw.setAllowedAgentWithCollector(user, password, agent_collector_name,
                ".*\\|.*\\|.*AffinityAgent.*", collector1Host, collector1Port,
                momHost, momPort, momLibDir);
        XMLUtil.changeAttributeValue(loadBalanceFile, "collector", "latched",
                "false", "true");
        harvestWait(60);

        startEM(collector1RoleId);
        startEM(collector2RoleId);
        startEM(collector3RoleId);
        startTomcatAgent(tomcatRoleId);
        startJBossAgent(jbossRoleId);
        harvestWait(60);

        String findStr = "AffinityAgent";
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        int count1 = StringUtils.countMatches(tempResult1, findStr);
        Assert.assertEquals(
                "AffinityAgent is not connected to Collector1 though it is included in loadbal file ",
                2, count1);

        clw.setAllowedAgentWithCollector(user, password, agent_collector_name,
                ".*\\|.*\\|.*AffinityAgent.*", collector2Host, collector2Port,
                momHost, momPort, momLibDir);
        XMLUtil.changeAttributeValue(loadBalanceFile, "collector", "latched",
                "false", "true");
        harvestWait(240);

        stopEM(momRoleId);
        stopCollectorEM(momRoleId,collector1RoleId);
        harvestWait(60);

        String msg = "Connected to " + collector2Host;      
        checkLogForMsg(envProperties, jbossMachineId, jbossLogFile, msg);       
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);
        startEM(momRoleId);
        clw.setDisAllowedAgentWithCollector(user, password,
                agent_collector_name, ".*\\|.*\\|.*AffinityAgent.*",
                collector1Host, collector1Port, momHost, momPort, momLibDir);
        clw.setDisAllowedAgentWithCollector(user, password,
                agent_collector_name, ".*\\|.*\\|.*AffinityAgent.*",
                collector2Host, collector2Port, momHost, momPort, momLibDir);
        harvestWait(180);

        tempResult1 = clw.getNodeList(user, password, ".*", collector3Host,
                collector3Port, momLibDir).toString();
        count1 = StringUtils.countMatches(tempResult1, findStr);
        Assert.assertEquals(
                "AffinityAgent is not connected to Collector3 though remaining 2 collectors are excluded ",
                2, count1);     

    }

    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_440677_Custom_agents_disallowed_messages_in_Verbose_level()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat"};
        setuptestcase("440677",serversList);
        replaceProp("log4j.logger.Manager=INFO, console, logfile",
                "log4j.logger.Manager=DEBUG, console, logfile", momMachineId,
                momConfigFile);
        replaceProp("log4j.logger.Manager=INFO, console, logfile",
                "log4j.logger.Manager=DEBUG, console, logfile",
                collector1MachineId, col1ConfigFile);
        replaceProp("log4j.logger.Manager=INFO, console, logfile",
                "log4j.logger.Manager=DEBUG, console, logfile",
                collector2MachineId, col2ConfigFile);

        startEM(momRoleId);
        clw.setDisAllowedAgentWithCollector(user, password,
                agent_collector_name, tomcatAgentExpression, collector1Host,
                collector1Port, momHost, momPort, momLibDir);
        harvestWait(120);

        startEM(collector1RoleId);
        startEM(collector2RoleId);
        startTomcatAgent(tomcatRoleId);

        String msg1 = "[VERBOSE] [PO:main Mailman 5] [Manager.LoadBalancer] Disconnected SuperDomain|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Business Application Agent (Virtual)";
        String msg2 = "[VERBOSE] [PO:main Mailman 7] [Manager.LoadBalancer] Disconnected SuperDomain|Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Metric Agent (Virtual)";

        try {
            isKeywordInFile(envProperties, collector1MachineId, col1LogFile, msg1);
            isKeywordInFile(envProperties, collector1MachineId, col1LogFile, msg2);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
            LOGGER.info("Disconnect messages are not seen in verbose level in the collector log");
        }   

    }

    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_268894_default_agent_connection_mode_with_default_option_True()
            throws Exception {

        serversList = new String[]{"mom", "tomcat"};
        setuptestcase("268894",serversList);
        replaceProp("introscope.enterprisemanager.clustering.mode=MOM",
                "introscope.enterprisemanager.clustering.mode=StandAlone",
                momMachineId, momConfigFile);
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true", "#",
                momMachineId, momConfigFile);

        startEM(momRoleId);
        startTomcatAgent(tomcatRoleId);
        harvestWait(60);

        tempResult1 = clw.getNodeList(user, password, ".*", momHost, momPort,
                momLibDir).toString();
        Assert.assertTrue("Tomcat Agent is not connected to the standalone EM",
                tempResult1.contains("Tomcat"));        

    }

    /* There is an open Defect DE175425 , will be in commented state until the
       defect is fixed
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_268892_default_agent_connection_mode_with_False()
            throws Exception {
        
        serversList = new String[]{"mom", "jboss"};
        setuptestcase("268892",serversList);
        replaceProp("introscope.enterprisemanager.clustering.mode=MOM",
                "introscope.enterprisemanager.clustering.mode=StandAlone",
                momMachineId, momConfigFile);
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false",
                momMachineId, momConfigFile);

        startEM(momRoleId);
        startJBossAgent(jbossRoleId);       

        String msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        checkLogForMsg(envProperties, jbossMachineId, jbossLogFile, msg);

        tempResult1 = clw.getNodeList(user, password, ".*", momHost, momPort,
                momLibDir).toString();
        Assert.assertTrue(
                "JBoss Agent is not connected to the standalone EM in disallowed mode",
                !tempResult1.contains("JBoss"));
        tempResult1 = clw.getLatestMetricValue(user, password,
                "(.*)\\|JBoss\\|JBoss Agent", "GC Heap:Bytes In Use", momHost,
                momPort, momLibDir);
        Assert.assertTrue(
                "Metrics are getting reported though Jboss set to connect in disallowed mode",
                tempResult1.equals("-1"));

        stopEM(momRoleId);
        replaceProp("introscope.apm.agentcontrol.agent.allowed=false",
                "introscope.apm.agentcontrol.agent.allowed=true", momMachineId,
                momConfigFile);
        startEM(momRoleId);
        harvestWait(120);

        tempResult1 = clw.getNodeList(user, password, ".*", momHost, momPort,
                momLibDir).toString();
        Assert.assertTrue(
                "JBoss Agent is not connected to the standalone EM in allowed mode",
                tempResult1.contains("JBoss"));
        tempResult1 = clw.getLatestMetricValue(user, password,
                "(.*)\\|JBoss\\|JBoss Agent", "GC Heap:Bytes In Use", momHost,
                momPort, momLibDir);
        Assert.assertTrue("Metrics are not getting reported from Jboss",
                !tempResult1.equals(-1));       

    } */

    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_268889_Verify_default_agent_connection_mode_property_in_EM_properties_file_Standalone()
            throws Exception {      
        
        serversList = new String[]{"empty"};
        setuptestcase("268889",serversList);
        
        try {
            isKeywordInFile(envProperties, standaloneMachineId, standaloneConfigFile,
                    "introscope.apm.agentcontrol.agent.allowed=true");
            isKeywordInFile(envProperties, standaloneMachineId, standaloneConfigFile,
                    "introscope.apm.agentcontrol.agent.emlistlookup.enable=true");
            isKeywordInFile(envProperties, standaloneMachineId, standaloneConfigFile,
                    "introscope.enterprisemanager.agent.disallowed.connection.limit=0");
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.assertTrue("Log check failed because of the Exception : "
                    + e, false);
        }
    }

    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_268897_Agent_Controllability_at_MOM_ON_and_Collector_ON()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat"};
        setuptestcase("268897",serversList);
        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);

        startTomcatAgent(tomcatRoleId);     

        String msg1 = "Connected controllable Agent to the Introscope Enterprise Manager at "
                + momHost;      
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);
        
        msg1 = "Lost contact with the Introscope Enterprise Manager at "
                + momHost + ":" + momPort;      
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        tempResult2 = clw.getNodeList(user, password, ".*", collector2Host,
                collector2Port, momLibDir).toString();
        Assert.assertTrue(
                "Tomcat Agent is not connected to any of the Collector",
                tempResult1.contains("Tomcat")
                        || tempResult2.contains("Tomcat"));     

    }

    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_303711_introscope_apm_agentcontrol_agent_reconnect_wait()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat", "jboss"};
        setuptestcase("303711",serversList);
        String logfile = "null";
        String machineId = "null";
        replaceProp(
                "introscope.enterprisemanager.agent.disallowed.connection.limit=0",
                "introscope.enterprisemanager.agent.disallowed.connection.limit=1",
                momMachineId, momConfigFile);
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false",
                momMachineId, momConfigFile);
        replaceProp("log4j.logger.IntroscopeAgent=INFO,logfile",
                "log4j.logger.IntroscopeAgent=DEBUG,logfile", tomcatMachineId,
                tomcatAgentProfile);
        replaceProp("log4j.logger.IntroscopeAgent=INFO,logfile",
                "log4j.logger.IntroscopeAgent=DEBUG,logfile", jbossMachineId,
                jbossAgentProfile);

        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);
        startJBossAgent(jbossRoleId);
        startTomcatAgent(tomcatRoleId);
        harvestWait(180);

        tempResult1 = clw.getCurrentAgentsDisAllowedList(user, password,
                momHost, momHost, momPort, momLibDir).toString();

        if (tempResult1.contains("Tomcat")) {
            logfile = jbossLogFile;
            machineId = jbossMachineId;
        }

        else if (tempResult1.contains("JBoss")) {
            logfile = tomcatLogFile;
            machineId = tomcatMachineId;
        } else
            Assert.assertTrue(
                    "No agents are connected in diallowed mode to the MOM",
                    false);

        String msg = "Waiting 45000 milliseconds for Introscope Enterprise Manager "
                + momHost;  
        harvestWait(600);
        checkLogForMsg(envProperties, machineId, logfile, msg);
        
        replaceProp("introscope.apm.agentcontrol.agent.reconnect.wait=45",
                "introscope.apm.agentcontrol.agent.reconnect.wait=10",
                momMachineId, momConfigFile);   
        harvestWait(120);
        
        msg = "Hot config property introscope.apm.agentcontrol.agent.reconnect.wait changed from 45 to 10";
        checkLogForMsg(envProperties, momMachineId, momLogFile, msg);
        harvestWait(120);
        msg = "Waiting 75000 milliseconds for Introscope Enterprise Manager "
                + momHost;
        checkLogForMsg(envProperties, machineId, logfile, msg);
        
        replaceProp("introscope.apm.agentcontrol.agent.reconnect.wait=10",
                "introscope.apm.agentcontrol.agent.reconnect.wait=-1",
                momMachineId, momConfigFile);
        harvestWait(120);
                
        msg = "[WARN] [PO Async Executor] [Manager.LoadBalancer] introscope.apm.agentcontrol.agent.reconnect.wait is negative: -1";
        checkLogForMsg(envProperties, momMachineId, momLogFile, msg);
        msg = "Using default value for introscope.apm.agentcontrol.agent.reconnect.wait: 45";
        checkLogForMsg(envProperties, momMachineId, momLogFile, msg);
        msg = "Hot config property introscope.apm.agentcontrol.agent.reconnect.wait changed from 10 to 45";
        checkLogForMsg(envProperties, momMachineId, momLogFile, msg);
        
        replaceProp("introscope.apm.agentcontrol.agent.reconnect.wait=-1",
                "introscope.apm.agentcontrol.agent.reconnect.wait=60",
                momMachineId, momConfigFile);
        harvestWait(120);
                
        msg = "Hot config property introscope.apm.agentcontrol.agent.reconnect.wait changed from 45 to 60";
        checkLogForMsg(envProperties, momMachineId, momLogFile, msg);
        harvestWait(600);
        msg = "Waiting 90000 milliseconds for Introscope Enterprise Manager "
                + momHost;
        checkLogForMsg(envProperties, machineId, logfile, msg);
        
        replaceProp("introscope.apm.agentcontrol.agent.reconnect.wait=60",
                "introscope.apm.agentcontrol.agent.reconnect.wait= ",
                momMachineId, momConfigFile);
        harvestWait(120);
                
        msg = "[WARN] [PO Async Executor] [Manager.LoadBalancer] introscope.apm.agentcontrol.agent.reconnect.wait is not an integer:";
        checkLogForMsg(envProperties, momMachineId, momLogFile, msg);
        msg = "Using default value for introscope.apm.agentcontrol.agent.reconnect.wait: 45";
        checkLogForMsg(envProperties, momMachineId, momLogFile, msg);
        msg = "Hot config property introscope.apm.agentcontrol.agent.reconnect.wait changed from 60 to 45";
        checkLogForMsg(envProperties, momMachineId, momLogFile, msg);
        msg = "Value for the property introscope.apm.agentcontrol.agent.reconnect.wait is not an integer, setting it to default value of:45";
        checkLogForMsg(envProperties, momMachineId, momLogFile, msg);

    } 

    @Test(groups = { "Deep" }, enabled = true)
    public void verify_ALM_268893_default_agent_connection_mode_with_TRUE()
            throws Exception {
        
        serversList = new String[]{"mom", "jboss"};
        setuptestcase("268893",serversList);
        replaceProp("introscope.enterprisemanager.clustering.mode=MOM",
                "introscope.enterprisemanager.clustering.mode=StandAlone",
                momMachineId, momConfigFile);

        startEM(momRoleId);
        startJBossAgent(jbossRoleId);
        harvestWait(60);

        tempResult1 = clw.getNodeList(user, password, ".*", momHost, momPort,
                momLibDir).toString();
        Assert.assertTrue(
                "JBoss Agent is not connected to the standalone EM in allowed mode",
                tempResult1.contains("JBoss"));
        tempResult1 = clw.getLatestMetricValue(user, password,
                "(.*)\\|JBoss\\|JBoss Agent", "GC Heap:Bytes In Use", momHost,
                momPort, momLibDir);
        Assert.assertTrue("Metrics are not getting reported from Jboss",
                !tempResult1.equals(-1));

        stopEM(momRoleId);
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false",
                momMachineId, momConfigFile);
        startEM(momRoleId);
        
        String msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        checkLogForMsg(envProperties, jbossMachineId, jbossLogFile, msg);

        tempResult1 = clw.getNodeList(user, password, ".*", momHost, momPort,
                momLibDir).toString();
        Assert.assertTrue(
                "JBoss Agent is not connected to the standalone EM in disallowed mode",
                !tempResult1.contains("JBoss"));
        tempResult1 = clw.getLatestMetricValue(user, password,
                "(.*)\\|JBoss\\|JBoss Agent", "GC Heap:Bytes In Use", momHost,
                momPort, momLibDir);
        Assert.assertTrue(
                "Metrics are getting reported though Jboss set to connect in disallowed mode",
                tempResult1.equals("-1"));

    } 

    @Test(groups = { "Deep" }, enabled = true)
    public void verify_ALM_268908_CLW_Get_List_of_disallowed_Agents_on_particular_EM_Collector()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat"};
        setuptestcase("268908",serversList);
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false",
                momMachineId, momConfigFile);
        replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
                "agentManager.url.1=" + collector1Host + ":" + collector1Port,
                tomcatMachineId, tomcatAgentProfile);

        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);
        startTomcatAgent(tomcatRoleId);
        harvestWait(60);

        tempResult1 = clw.getCurrentAgentsDisAllowedList(user, password,
                collector1Host, momHost, momPort, momLibDir).toString();

        Assert.assertTrue(
                "Tomcat is not found in the disallowed agents list of collector1",
                tempResult1.contains("Tomcat"));        

    }

    @Test(groups = { "Deep" }, enabled = true)
    public void verify_ALM_295426_default_agent_connection_mode_with_TRUE_Cluster_Collector_mode()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "tomcat"};
        setuptestcase("295426",serversList);
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false",
                collector1MachineId, col1ConfigFile);
        startEM(momRoleId);
        startEM(collector1RoleId);
        startTomcatAgent(tomcatRoleId);
        harvestWait(60);

        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        Assert.assertTrue("Tomcat Agent is not connected to the Collector",
                tempResult1.contains("Tomcat"));

        stopCollectorEM(momRoleId,collector1RoleId);
        
        String msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);

        startEM(collector1RoleId);
        harvestWait(240);

        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        Assert.assertTrue(
                "Tomcat Agent is not connected back to the Collector after restart",
                tempResult1.contains("Tomcat"));        

    }
    
    @Test(groups = { "Deep" }, enabled = true)
    public void verify_ALM_295425_default_agent_connection_mode_with_TRUE_Cluster_MOM_mode()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "jboss"};
        setuptestcase("295425",serversList);        
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false",
                collector1MachineId, col1ConfigFile);
        
        startEM(momRoleId);
        startEM(collector1RoleId);
        startJBossAgent(jbossRoleId);
        harvestWait(120);
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        Assert.assertTrue(
                "JBoss Agent is not redirected to the collector",
                 tempResult1.contains("JBoss"));
        
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false",
                momMachineId, momConfigFile);
        harvestWait(240);       
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        Assert.assertTrue(
                "JBoss Agent is not redirected to the collector",
                 tempResult1.contains("JBoss"));        
        tempResult1 = clw.getLatestMetricValue(user, password,
                "(.*)\\|JBoss\\|JBoss Agent", "GC Heap:Bytes In Use", collector1Host,
                collector1Port, momLibDir);
        Assert.assertTrue(
                "Metrics from JBoss are not getting reported after the agentcontrol property change",
                !tempResult1.equals("-1"));     
        
    }   
    
   //TestCase fails currently because of the open defect DE186816
    @Test(groups = { "FULL" }, enabled = true) 
    public void verify_ALM_450350_TT74470_Consistent_Recurrence_Of_Agent_Transaction_Trace_UserID_Not_Configured_Warning()
            throws Exception  {
        	
        	serversList = new String[]{"mom", "tomcat"};
        	setuptestcase("450350",serversList);
            replaceProp("introscope.enterprisemanager.clustering.mode=MOM",
                        "introscope.enterprisemanager.clustering.mode=StandAlone",
                        momMachineId, momConfigFile);
            String msg = "[WARN] [IntroscopeAgent.Agent] Agent_Transaction_Trace_UserID_Not_Configured";
            startEM(momRoleId);
            startTomcatAgent(tomcatRoleId);
            harvestWait(60);

            tempResult1 = clw.getNodeList(user, password, ".*", momHost, momPort,
                                          momLibDir).toString();
            Assert.assertTrue("Tomcat Agent is not connected to the standalone EM",
                              tempResult1.contains("Tomcat"));

            try
            {
            	isKeywordInFileOneTimeCounter(envProperties, tomcatMachineId, tomcatLogFile, msg);
                Assert.assertTrue(true);
            } catch (Exception e)
            {
                Assert.assertTrue(false);
                LOGGER.info("Message:::"+ msg + "::::Appear either 0 time or more than one time");
            } 

        }
    
    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_268909_CLW_To_add_an_allow_Agent()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat"};
        setuptestcase("268909",serversList);        
        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);      

        clw.setAllowedAgentWithCollector(user, password, agent_collector_name,
                tomcatAgentExpression, collector2Host, collector2Port, momHost,
                momPort, momLibDir);
        XMLUtil.changeAttributeValue(loadBalanceFile, "collector", "latched",
                "false", "true");
        harvestWait(120);
        
        startTomcatAgent(tomcatRoleId);
        harvestWait(60);
        tempResult1 = clw.getNodeList(user, password, ".*", collector2Host,
                collector2Port, momLibDir).toString();
        Assert.assertTrue("Tomcat Agent is not connected to the Collector",
                tempResult1.contains("Tomcat"));        
        
    }
    
    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_268910_CLW_To_add_an_disallow_Agent()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat"};
        setuptestcase("268910",serversList);        
        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);      
        
        clw.setDisAllowedAgentWithCollector(user, password,
                agent_collector_name, tomcatAgentExpression, collector2Host,
                collector2Port, momHost, momPort, momLibDir);
        harvestWait(120);
        
        startTomcatAgent(tomcatRoleId);
        harvestWait(60);
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        Assert.assertTrue("Tomcat Agent is not connected to the Collector1 though it has been set to be disallowed by collector2",
                tempResult1.contains("Tomcat"));
                
    }
    
    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_430029_Agent_doesnt_redirect_when_execludeinclude_tags_are_alternatively_used_in_lbxml()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat"};
        setuptestcase("430029",serversList);        
        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);  
        startTomcatAgent(tomcatRoleId);     
        
        clw.setDisAllowedAgentWithCollector(user, password,
                agent_collector_name, tomcatAgentExpression, collector2Host,
                collector2Port, momHost, momPort, momLibDir);
        harvestWait(180);
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        Assert.assertTrue("Tomcat Agent is not connected to the Collector1 though it has been set to be disallowed by collector2",
                tempResult1.contains("Tomcat"));
        
        revertFile(loadBalanceFile, loadBalanceFile_copy, momMachineId);        
        clw.setAllowedAgentWithCollector(user, password, agent_collector_name,
                tomcatAgentExpression, collector2Host, collector2Port, momHost,
                momPort, momLibDir);
        XMLUtil.changeAttributeValue(loadBalanceFile, "collector", "latched",
                "false", "true");
        harvestWait(180);   
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector2Host,
                collector2Port, momLibDir).toString();
        Assert.assertTrue("Tomcat Agent is not connected to the Collector2 after rebalance",
                tempResult1.contains("Tomcat"));
                
        
        revertFile(loadBalanceFile, loadBalanceFile_copy, momMachineId);
        clw.setDisAllowedAgentWithCollector(user, password,
                agent_collector_name, tomcatAgentExpression, collector1Host,
                collector1Port, momHost, momPort, momLibDir);
        clw.setDisAllowedAgentWithCollector(user, password,
                agent_collector_name, tomcatAgentExpression, collector2Host,
                collector2Port, momHost, momPort, momLibDir);
                
        String msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);
        
    }
    
    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_269832_Agent_ALLOW_DISALLOW_REDIRECT_on_MOM_Collector()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "tomcat"};
        setuptestcase("269832",serversList);        
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false",
                collector1MachineId, col1ConfigFile);
        
        startEM(momRoleId);
        startEM(collector1RoleId);
        startTomcatAgent(tomcatRoleId);
        harvestWait(120);

        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        Assert.assertTrue("Tomcat Agent is not connected to the Collector",
                tempResult1.contains("Tomcat"));        
                
        clw.setDisAllowedAgentWithCollector(user, password,
                agent_collector_name, tomcatAgentExpression, collector1Host,
                collector1Port, momHost, momPort, momLibDir);
                
        String msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);
        
        String agentExpression =
                "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)";
        String disallowedAgentsCountMetric =
                "Enterprise Manager\\|Connections:Number of Disallowed Agents";
        String actualMetricValue =
                clw.getLatestMetricValue("admin", "", agentExpression,
                               disallowedAgentsCountMetric,momHost, momPort, momLibDir);
        LOGGER.info("actualMetricValue is"+actualMetricValue);
        Assert.assertTrue("The metric Number of Disallowed Agents does not report the correct value",
                actualMetricValue.equals("Integer:::1"));
     
    }
    
    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_269833_Agent_ALLOW_DISALLOW_REDIRECT_one_MOM_two_Collectors()
            throws Exception {      
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat"};
        setuptestcase("269833",serversList);
        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);  
        startTomcatAgent(tomcatRoleId);     
        harvestWait(60);
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        tempResult2 = clw.getNodeList(user, password, ".*", collector2Host,
                collector2Port, momLibDir).toString();
        Assert.assertTrue(
                "Tomcat Agent is not connected to any of the Collector",
                tempResult1.contains("Tomcat")
                        || tempResult2.contains("Tomcat"));
        
        clw.setAllowedAgentWithCollector(user, password, agent_collector_name,
                tomcatAgentExpression, collector2Host, collector2Port, momHost,
                momPort, momLibDir);
        XMLUtil.changeAttributeValue(loadBalanceFile, "collector", "latched",
                "false", "true");
        harvestWait(180);
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector2Host,
                collector2Port, momLibDir).toString();
        Assert.assertTrue("Tomcat Agent is not connected to Collector2 after rebalance",
                tempResult1.contains("Tomcat"));
        
        stopCollectorEM(momRoleId,collector2RoleId);
                
        String msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);
        
    }
    
    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_362050_Older_agent_redirecting_to_Collectors_after_setting_agent_allowed_to_true_on_MOM()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "tomcat", "jboss"};
        setuptestcase("362050",serversList);        
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false",
                momMachineId, momConfigFile);
        
        startEM(momRoleId);
        startEM(collector1RoleId);
        startTomcatAgent(tomcatRoleId);
                
        String msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);
        
        replaceProp("introscope.apm.agentcontrol.agent.allowed=false",
                "introscope.apm.agentcontrol.agent.allowed=true",
                momMachineId, momConfigFile);
        harvestWait(120);
        
        startJBossAgent(jbossRoleId);
        harvestWait(60);
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();      
        Assert.assertTrue(
                "New Agent - JBoss Agent is not connected to the Collector",
                tempResult1.contains("JBoss"));         
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();      
        Assert.assertTrue(
                "Older Agent - Tomcat Agent is not connected to the Collector after the agent control is made to true",
                tempResult1.contains("Tomcat"));        
        
    }
    
    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_295761_CLW_To_remove_entry_in_loadbalancingxml()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat"};
        setuptestcase("295761",serversList);        
        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);

        clw.setDisAllowedAgentWithCollector(user, password,
                agent_collector_name, tomcatAgentExpression, collector1Host,
                collector1Port, momHost, momPort, momLibDir);
        clw.setDisAllowedAgentWithCollector(user, password,
                agent_collector_name, tomcatAgentExpression, collector2Host,
                collector2Port, momHost, momPort, momLibDir);
        harvestWait(120);
        
        startTomcatAgent(tomcatRoleId);
        harvestWait(240);
        
        String msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg);
        
        clw.removeAgentExpression(user, password, tomcatAgentExpression, momHost, momPort, momLibDir);
        harvestWait(240);
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        tempResult2 = clw.getNodeList(user, password, ".*", collector2Host,
                collector2Port, momLibDir).toString();
        Assert.assertTrue(
                "Tomcat Agent is not connected to any of the Collector",
                tempResult1.contains("Tomcat")
                        || tempResult2.contains("Tomcat"));     
    }
        
    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_268921_Self_lookup_MOM_and_connected_collector_down()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat"};
        setuptestcase("268921",serversList);            
        String colRoleId = "null";
        String othercolHost = "null";       
        List<String> selflookupprop = new ArrayList<String>();
        selflookupprop.add("introscope.apm.agentcontrol.agent.selflookup.enable=true");
        appendProp(selflookupprop, momMachineId, momConfigFile);
        
        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);  
        startTomcatAgent(tomcatRoleId);     
        harvestWait(60);
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        tempResult2 = clw.getNodeList(user, password, ".*", collector2Host,
                collector2Port, momLibDir).toString();
        
        if (tempResult1.contains("Tomcat"))
        {
            colRoleId = collector1RoleId;
            othercolHost = collector2Host;              
        }
        else if (tempResult2.contains("Tomcat"))
        {
            colRoleId = collector2RoleId;
            othercolHost = collector1Host;              
        }
        else
            Assert.assertTrue(
                    "Tomcat Agent is not connected to any of the Collector",false);         
        
        stopEM(momRoleId);
        stopCollectorEM(momRoleId,colRoleId);
                
        String msg = "Connected to " + othercolHost;        
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);
        
        try
        {
        isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, "[ERROR] [IntroscopeAgent.ConnectionThread]");
        Assert.assertTrue(false);
        } catch (Exception e) {
        Assert.assertTrue(true);
        LOGGER.info("Errors seen in AGent logs");
        }   
    
    }
    
    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_295423_Self_lookup_MOM_and_collectors()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "col3", "tomcat"};
        setuptestcase("295423",serversList);
        String colRoleId = "null";      
        String othercol1host = "null";      
        String othercol2host = "null";      
        String othercol1RoleId = "null";
        String othercol2RoleId = "null";        
        String othercolhost = "null";       
        List<String> selflookupprop = new ArrayList<String>();
        selflookupprop.add("introscope.apm.agentcontrol.agent.selflookup.enable=true");
        appendProp(selflookupprop, momMachineId, momConfigFile);
        
        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);  
        startEM(collector3RoleId);
        startTomcatAgent(tomcatRoleId);     
        harvestWait(60);
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        tempResult2 = clw.getNodeList(user, password, ".*", collector2Host,
                collector2Port, momLibDir).toString();
        tempResult3 = clw.getNodeList(user, password, ".*", collector3Host,
                collector3Port, momLibDir).toString();
        
        if (tempResult1.contains("Tomcat"))
        {
            colRoleId = collector1RoleId;           
            othercol1host = collector2Host;         
            othercol1RoleId = collector2RoleId;
            othercol2host = collector3Host;         
            othercol2RoleId = collector3RoleId;
        }
        else if (tempResult2.contains("Tomcat"))
        {
            colRoleId = collector2RoleId;           
            othercol1host = collector1Host;         
            othercol1RoleId = collector1RoleId;
            othercol2host = collector3Host;             
            othercol2RoleId = collector3RoleId;
        }
        else if (tempResult3.contains("Tomcat"))
        {
            colRoleId = collector3RoleId;           
            othercol1host = collector1Host;         
            othercol1RoleId = collector1RoleId;
            othercol2host = collector2Host;         
            othercol2RoleId = collector2RoleId;
        }
        else
            Assert.assertTrue(
                    "Tomcat Agent is not connected to any of the Collector",false);         
        
        stopEM(momRoleId);
        stopCollectorEM(momRoleId,colRoleId);
        harvestWait(240);
        
        String msg1 = "Connected to " + othercol1host;
        String msg2 = "Connected to " + othercol2host;      
        
        try
        {
            isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg1);
            colRoleId = othercol1RoleId;            
            othercolhost = othercol2host;       
            Assert.assertTrue(true);
        }
        catch (Exception e1)
        {
            Assert.assertTrue(true);
                try
                {
                    isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg2);
                    colRoleId = othercol2RoleId;                    
                    othercolhost = othercol1host;           
                    Assert.assertTrue(true);                    
                } catch (Exception e2) {                    
                    Assert.assertTrue(
                            "Tomcat Agent is not connected to any of the other 2 Collectors available",false);                  
                }           
        }
        
        stopCollectorEM(momRoleId,colRoleId);       
        String msg4 = "Connected to " + othercolhost;       
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg4);
        
        try
        {
        isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, "[ERROR] [IntroscopeAgent.ConnectionThread]");
        Assert.assertTrue(false);
        } catch (Exception e) {
        Assert.assertTrue(true);
        LOGGER.info("Errors seen in AGent logs");
        }
    
    }
    
    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_295422_Self_lookup_restart_agent_after_self_lookup()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat"};
        setuptestcase("295422",serversList);
        String colRoleId = "null";      
        String othercolHost = "null";
        int othercolPort = 0;       
        List<String> selflookupprop = new ArrayList<String>();
        selflookupprop.add("introscope.apm.agentcontrol.agent.selflookup.enable=true");
        appendProp(selflookupprop, momMachineId, momConfigFile);        
                
        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);  
        startTomcatAgent(tomcatRoleId);     
        harvestWait(60);
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        tempResult2 = clw.getNodeList(user, password, ".*", collector2Host,
                collector2Port, momLibDir).toString();
        
        if (tempResult1.contains("Tomcat"))
        {
            colRoleId = collector1RoleId;           
            othercolHost = collector2Host;
            othercolPort = collector2Port;
        }
        else if (tempResult2.contains("Tomcat"))
        {
            colRoleId = collector2RoleId;           
            othercolHost = collector1Host;
            othercolPort = collector1Port;
        }
        else
            Assert.assertTrue(
                    "Tomcat Agent is not connected to any of the Collector",false);     
        
        stopEM(momRoleId);
        stopCollectorEM(momRoleId,colRoleId);
                
        String msg1 = "Connected to " + othercolHost;       
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);
        
        try
        {
        isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, "[ERROR] [IntroscopeAgent.ConnectionThread]");
        Assert.assertTrue(false);
        } catch (Exception e) {
        Assert.assertTrue(true);
        LOGGER.info("Errors seen in AGent logs");
        }
        
        stopTomcatAgent(tomcatRoleId);
        startTomcatAgent(tomcatRoleId);
        harvestWait(120);
        
        String msg2 = "[WARN] [IntroscopeAgent.ConnectionThread] Failed to connect to the Introscope Enterprise Manager at " + momHost;
        String msg3 = "NullPointerException";
        isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg2);
        try
        {
        isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg3);
        Assert.assertTrue(false);
        } catch (Exception e) {
        Assert.assertTrue(true);
        LOGGER.info("NullPointerException seen in AGent logs");
        }
        
        startEM(momRoleId);
        harvestWait(240);
        
        tempResult3 = clw.getNodeList(user, password, ".*", othercolHost,
                othercolPort, momLibDir).toString();
        Assert.assertTrue(
                "Tomcat Agent is not connected to the available Collector",
                tempResult3.contains("Tomcat"));    
        
    }
    
    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_430034_Add_examples_in_loadbalancing_xml_file_for_SSLHTTPS_agent_redirection()
            throws Exception {
        
        serversList = new String[]{"empty"};
        setuptestcase("430034",serversList);
        String msg1 = "Example 8: Redirect Default/SSL/HTTP/HTTPS agents to EMs within the cluster";
        String msg2 = "If agents with the mentioned specifiers connect via HTTP/HTTPS/SSL ports, use only the default communication port of EM";
        String msg3 ="Example 9: Redirect EPAgents to EMs outside the cluster";
        String msg4 = "If agents with the mentioned specifiers connect via HTTP/HTTPS/SSL ports, use the respective communication port of the EM";
        String msg5 = "<collector host=\"host01\" port=\"5443\"/>";
        String msg6 = "<collector host=\"host01\" port=\"8081\"/>";
        String msg7 = "<collector host=\"host01\" port=\"8444\"/>";
        
        try {
            isKeywordInFile(envProperties, momMachineId, loadBalanceFile, msg1);
            isKeywordInFile(envProperties, momMachineId, loadBalanceFile, msg2);
            isKeywordInFile(envProperties, momMachineId, loadBalanceFile, msg3);
            isKeywordInFile(envProperties, momMachineId, loadBalanceFile, msg4);
            isKeywordInFile(envProperties, momMachineId, loadBalanceFile, msg5);
            isKeywordInFile(envProperties, momMachineId, loadBalanceFile, msg6);
            isKeywordInFile(envProperties, momMachineId, loadBalanceFile, msg7);
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.assertTrue("Log check failed because of the Exception : "
                    + e, false);
        }
        
    }
    
    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_295763_CLW_To_get_last_modified_timestamp()
            throws Exception {
        
        serversList = new String[]{"mom"};
        setuptestcase("295763",serversList);        
        startEM(momRoleId);
        clw.setDisAllowedAgentWithCollector(user, password,
                agent_collector_name, tomcatAgentExpression, collector1Host,
                collector1Port, momHost, momPort, momLibDir);
        tempResult1 = clw.getLoadBalancingXmlLastUpdatedTime(user, password,
                momHost, momPort, momLibDir);
        LOGGER.info("clw output is " + tempResult1);
        Assert.assertTrue(
                "CLW Query to get lastmodified timestamp has failed",
                tempResult1.contains("Sun") || tempResult1.contains("Mon") || tempResult1.contains("Tue") || tempResult1.contains("Wed") 
                || tempResult1.contains("Thu") || tempResult1.contains("Fri") || tempResult1.contains("Sat"));
        
    }
    
    @Test(groups = {"Deep"}, enabled = true)
    public void verify_ALM_295430_Proper_log_messages()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat"};
        setuptestcase("295430",serversList);        
        String othercolHost = "null";
        String colHost = "null";
        String othercolroleId = "null";
        String colRoleId = "null";
        String msg ;
        
        replaceProp("log4j.logger.IntroscopeAgent=INFO,logfile",
                "log4j.logger.IntroscopeAgent=DEBUG,logfile", tomcatMachineId,
                tomcatAgentProfile);        
        
        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);  
        startTomcatAgent(tomcatRoleId);
                
        msg = "[INFO] [IntroscopeAgent.IsengardServerConnectionManager] Connected controllable Agent to the Introscope Enterprise Manager at " + momHost;
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);
        
        msg = "[WARN] [IntroscopeAgent.IsengardServerConnectionManager] Lost contact with the Introscope Enterprise Manager at " + momHost;      
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        tempResult2 = clw.getNodeList(user, password, ".*", collector2Host,
                collector2Port, momLibDir).toString();
        
        if (tempResult1.contains("Tomcat"))
        {
            colRoleId = collector1RoleId;
            colHost = collector1Host;
            othercolroleId = collector2RoleId;
            othercolHost = collector2Host;          
        }
        else if (tempResult2.contains("Tomcat"))
        {
            colRoleId = collector2RoleId;
            colHost = collector2Host;
            othercolroleId = collector1RoleId;
            othercolHost = collector1Host;          
        }
        else
            Assert.assertTrue(
                    "Tomcat Agent is not connected to any of the Collector",false); 
        
        msg = "[INFO] [IntroscopeAgent.ConnectionThread] Connected to " + colHost;
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);
        
        stopCollectorEM(momRoleId,colRoleId);
                
        msg = "[WARN] [IntroscopeAgent.ConnectionThread] Failed to re-connect to the Introscope Enterprise Manager at " + colHost;
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);
        
        msg = "[INFO] [IntroscopeAgent.ConnectionThread] Connected to " + othercolHost;
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);
        
        stopCollectorEM(momRoleId,othercolroleId);
                
        msg = "[WARN] [IntroscopeAgent.ConnectionThread] Failed to re-connect to the Introscope Enterprise Manager at " + othercolHost;
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);
        
        msg = "[INFO] [IntroscopeAgent.ConnectionThread] Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);
        
    }
    
    @Test(groups = { "Deep" }, enabled = true)
    public void verify_ALM_295653_no_ACC_for_direct_collector_connection()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat"};
        setuptestcase("295653",serversList);      
        ACCInitialize();
        revertConfigFiles();
        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);
        
        replaceProp("agentManager.url.1=" + momHost + ":" + momPort, "agentManager.url.1="
            + collector1Host + ":" + collector1Port, tomcatMachineId,
            tomcatAgentProfile);
        
        startTomcatAgent(tomcatRoleId);
        harvestWait(60);
        
        String msg1 = "Connected controllable Agent to the Introscope Enterprise Manager at "
            + collector1Host;
        isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg1);
        
        stopCollectorEM(momRoleId, collector1RoleId);
        harvestWait(120);  

        String msg2 = "Lost contact with the Introscope Enterprise Manager at "
            + momHost;
        isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg2);
        
        String msg3 = "Connected controllable Agent to the Introscope Enterprise Manager at "
            + collector2Host;
        isKeywordInFile(envProperties, tomcatMachineId, tomcatLogFile, msg3);
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector2Host,
                collector2Port, momLibDir).toString();
        Assert.assertTrue(
                "Error -> Tomcat Agent is not redirected to the collector2",
                 tempResult1.contains("Tomcat"));     
        
        startEM(collector1RoleId);
        harvestWait(60);
        tempResult2 = clw.getNodeList(user, password, ".*", collector1Host,
            collector1Port, momLibDir).toString();
        Assert.assertFalse(
            "Error -> Tomcat Agent is redirected back to collector1",
             tempResult2.contains("Tomcat"));     
        
    }
    
    
    @Test(groups = { "Deep" }, enabled = true)
    public void verify_ALM_296899_restart_collector_within_two_rebalancing_periods()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat"};
        setuptestcase("296899",serversList);        
        String agentExpression =
            "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)";
        String disallowedAgentsCountMetric =
            "Enterprise Manager\\|Connections:Number of Disallowed Agents";
     
        startTomcatAgent(tomcatRoleId);
        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);
        clw.setAllowedAgentWithCollector(user, password, agent_collector_name,
            tomcatAgentExpression, collector2Host, collector2Port, momHost,
            momPort, momLibDir);
        harvestWait(150);
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector2Host,
            collector2Port, momLibDir).toString();
        Assert.assertTrue(
            "Error -> Tomcat Agent is not redirected to the included collector2",
             tempResult1.contains("Tomcat"));
       
        stopCollectorEM(momRoleId, collector2RoleId);
        harvestWait(60);
        
        String actualMetricValue =
            clw.getLatestMetricValue("admin", "", agentExpression,
                           disallowedAgentsCountMetric, momHost, momPort, momLibDir);
        System.out.println("Disallowed agents metric value: " +actualMetricValue);
        Assert.assertTrue(actualMetricValue.contains("1"));
        
        startEM(collector2RoleId);
        harvestWait(360);
        tempResult2 = clw.getNodeList(user, password, ".*", collector2Host,
            collector2Port, momLibDir).toString();
        Assert.assertTrue(
            "Error -> Tomcat Agent is not redirected to the included collector2",
             tempResult2.contains("Tomcat"));
    
    }   
    
    @Test(groups = { "Deep" }, enabled = true)
    public void verify_ALM_269836_Agent_ALLOW_DISALLOW_REDIRECT_with_2_IP_addresses_in_Agent_Profile()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "standalone", "tomcat", "jboss"};
        setuptestcase("269836",serversList);
        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);
        startEM(standaloneRoleId);
        
        String findStr = "AffinityAgent";
        replaceProp("introscope.agent.agentName=Tomcat Agent",
                "introscope.agent.agentName=AffinityAgent", tomcatMachineId,
                tomcatAgentProfile);
        replaceProp("introscope.agent.agentName=JBoss Agent",
                "introscope.agent.agentName=AffinityAgent", jbossMachineId,
                jbossAgentProfile);
        
        List<String> appendagentmgrprop = new ArrayList<String>();
        appendagentmgrprop.add("agentManager.url.2=" + standaloneHost + ":" + standalonePort);
        appendProp(appendagentmgrprop, tomcatMachineId, tomcatAgentProfile);
        
        clw.setAllowedAgentWithCollector(user, password, agent_collector_name,
                ".*\\|.*\\|.*AffinityAgent.*", collector1Host, collector1Port,
                momHost, momPort, momLibDir);
        XMLUtil.changeAttributeValue(loadBalanceFile, "collector", "latched",
                "false", "true");
        harvestWait(120);
        
        startTomcatAgent(tomcatRoleId);
        startJBossAgent(jbossRoleId);
        harvestWait(60);
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        int count = StringUtils.countMatches(tempResult1, findStr);
        Assert.assertEquals(
                "AffinityAgent is not connected to Collector1 though it is in the include list ",
                2, count);
        
        clw.setAllowedAgentWithCollector(user, password, agent_collector_name,
                ".*\\|.*\\|.*AffinityAgent.*", collector2Host, collector2Port,
                momHost, momPort, momLibDir);
        XMLUtil.changeAttributeValue(loadBalanceFile, "collector", "latched",
                "false", "true");        
        harvestWait(120);
        
        stopCollectorEM(momRoleId,collector1RoleId); 
        stopCollectorEM(momRoleId, collector2RoleId);      
        harvestWait(120);
         
        tempResult1 = clw.getNodeList(user, password, ".*", standaloneHost,
                    standalonePort, standaloneLibDir).toString();
        count = StringUtils.countMatches(tempResult1, findStr);
        Assert.assertEquals(
                "AffinityAgent is not connected to the standalone EM ",
                1, count);
        String msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        checkLogForMsg(envProperties, jbossMachineId, jbossLogFile, msg);
      
    }
        
    @Test(groups = { "Full" }, enabled = true)
    public void verify_ALM_298780_Check_the_metric_EM_Port_for_multiple_agents_connected_to_the_same_EM()
            throws Exception {
        
        serversList = new String[]{"standalone", "tomcat", "jboss"};
        setuptestcase("298780",serversList);
        replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
                "agentManager.url.1=" + standaloneHost + ":" + standalonePort,
                tomcatMachineId, tomcatAgentProfile);
        
        replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
                "agentManager.url.1=" + standaloneHost + ":" + standalonePort,
                jbossMachineId, jbossAgentProfile);
        
        startEM(standaloneRoleId);
        startTomcatAgent(tomcatRoleId);
        startJBossAgent(jbossRoleId);
        harvestWait(60);
        
        tempResult1 = clw.getLatestMetricValue(user, password,
                "(.*)\\|JBoss\\|JBoss Agent", "EM Port", standaloneHost,
                standalonePort, standaloneLibDir);
        
        tempResult2 = clw.getLatestMetricValue(user, password,
                "(.*)\\|Tomcat\\|Tomcat Agent", "EM Port", standaloneHost,
                standalonePort, standaloneLibDir);      
        
        Assert.assertTrue("Tomcat or JBoss is not reporting the Correct EM Port value",
                tempResult1.equals("String:::"+standalonePort) && tempResult2.equals("String:::"+standalonePort));      
                
    }   
    
    @Test(groups = { "Full" }, enabled = true)
    public void verify_ALM_450364_Waiting_to_connect_to_Introscope_Enterprise_Manager_in_Agent_logs_INFO_msgs()
            throws Exception {
                    
        serversList = new String[]{"mom", "tomcat", "jboss"};
        setuptestcase("450364",serversList);
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
                "introscope.apm.agentcontrol.agent.allowed=false",
                momMachineId, momConfigFile);
        
        replaceProp("introscope.enterprisemanager.agent.disallowed.connection.limit=0",
                "introscope.enterprisemanager.agent.disallowed.connection.limit=1", 
                momMachineId, momConfigFile);           
        
        startEM(momRoleId);
        startTomcatAgent(tomcatRoleId);
                
        String msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);
        
        startJBossAgent(jbossRoleId);
        harvestWait(120);       
        msg = "[INFO] [IntroscopeAgent.IsengardServerConnectionManager] Waiting 45000 milliseconds for Introscope Enterprise Manager "
                + momHost;      
        checkLogForMsg(envProperties, jbossMachineId, jbossLogFile, msg);
        
        String agentExpression =
                "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)";
        String disallowedAgentsCountMetric =
                "Enterprise Manager\\|Connections:Number of Disallowed Agents";
        String disallowedAgentsClampedMetric =
                "Enterprise Manager\\|Connections:Disallowed Agents Clamped";
        String actualMetricValue1 =
                clw.getLatestMetricValue("admin", "", agentExpression,
                               disallowedAgentsCountMetric,momHost, momPort, momLibDir); 
        String actualMetricValue2 =
                clw.getLatestMetricValue("admin", "", agentExpression,
                        disallowedAgentsClampedMetric,momHost, momPort, momLibDir);
        
        Assert.assertTrue("The metric Number of Disallowed Agents does not report the correct value",
                actualMetricValue1.equals("Integer:::1"));
        Assert.assertTrue("The metric Disallowed Agents Clamped does not report the correct value",
                actualMetricValue2.equals("Integer:::1"));    
       
    } 
    
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_447461_Verify_agent_should_not_be_connected_to_EM_if_agent_connection_clamp_got_hit()
            throws Exception {      
            
        serversList = new String[]{"standalone", "tomcat", "jboss", "tomcat1"};
        setuptestcase("447461",serversList);        
        replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
                "agentManager.url.1=" + standaloneHost + ":" + standalonePort,
                tomcatMachineId, tomcatAgentProfile);
        replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
                "agentManager.url.1=" + standaloneHost + ":" + standalonePort,
                jbossMachineId, jbossAgentProfile);
        replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
                "agentManager.url.1=" + standaloneHost + ":" + standalonePort,
                tomcat1MachineId, tomcat1AgentProfile);     
        
        setattributeinapmthresholdXML(standaloneMachineId, standaloneapmthresholdxmlpath, agentconnlimitxpath, agentevntthresholdattribute, "2");
        
        startEM(standaloneRoleId);
        startTomcatAgent(tomcatRoleId);
        startJBossAgent(jbossRoleId);
        harvestWait(60);
        
        tempResult1 = clw.getNodeList(user, password, ".*", standaloneHost, standalonePort,
                standaloneLibDir).toString();
        Assert.assertTrue("Tomcat or JBoss Agent is not connected to the standalone EM",
                tempResult1.contains("Tomcat") && tempResult1.contains("JBoss"));
        
        startTomcatAgent(tomcat1RoleId);
        harvestWait(60);
        
        tempResult1 = clw.getNodeList(user, password, ".*", standaloneHost, standalonePort,
                standaloneLibDir).toString();
        Assert.assertTrue("Tomcat1 Agent is connected to the standalone EM even after agentconnection limit is reached",
                !tempResult1.contains("Tomcat1"));          
        
    }   
    
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_447470_Verify_agent_connected_to_MOM_in_disallowed_mode_when_agent_limit_clamp_is_hit_at_collector()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "tomcat", "jboss", "tomcat1"};
        setuptestcase("447470",serversList);        
        setattributeinapmthresholdXML(collector1MachineId, col1apmthresholdxmlpath, agentconnlimitxpath, agentevntthresholdattribute, "2");
        
        startEM(momRoleId);
        startEM(collector1RoleId);
        
        startTomcatAgent(tomcatRoleId);
        startJBossAgent(jbossRoleId);       
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host, collector1Port,
                        momLibDir).toString();
        Assert.assertTrue("Tomcat or JBoss Agent is not connected to the Collector",
                tempResult1.contains("Tomcat") && tempResult1.contains("JBoss"));
        
        startTomcatAgent(tomcat1RoleId);
        harvestWait(120);
                
        String msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        checkLogForMsg(envProperties, standaloneMachineId, tomcat1LogFile, msg);
        
        setattributeinapmthresholdXML(collector1MachineId, col1apmthresholdxmlpath, agentconnlimitxpath, agentevntthresholdattribute, "4");
        harvestWait(120);
        msg = "[Manager.Agent] [Clamp : introscope.enterprisemanager.agent.connection.limit, Value : 4]";
        checkLogForMsg(envProperties, collector1MachineId, col1LogFile, msg);
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host, collector1Port,
                momLibDir).toString();
        Assert.assertTrue("Tomcat1 Agent is not connected to Collector1",
                      tempResult1.contains("Tomcat1"));     
        
    }
    
    @Test(groups = { "Full" }, enabled = true)
    public void verify_ALM_447472_Verify_agent_connected_directly_to_collector_and_redirected_to_MOM_if_agent_limit_clamp_hit_on_collector()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "tomcat", "jboss", "tomcat1"};
        setuptestcase("447472",serversList);        
        replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
                "agentManager.url.1=" + collector1Host + ":" + collector1Port,
                tomcatMachineId, tomcatAgentProfile);       
        replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
                "agentManager.url.1=" + collector1Host + ":" + collector1Port,
                tomcat1MachineId, tomcat1AgentProfile);
        setattributeinapmthresholdXML(collector1MachineId, col1apmthresholdxmlpath, agentconnlimitxpath, agentevntthresholdattribute, "2");
        
        startEM(momRoleId);
        startEM(collector1RoleId);
        
        startTomcatAgent(tomcatRoleId);
        startJBossAgent(jbossRoleId);       
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host, collector1Port,
                        momLibDir).toString();
        Assert.assertTrue("Tomcat or JBoss Agent is not connected to the Collector",
                tempResult1.contains("Tomcat") && tempResult1.contains("JBoss"));
        
        startTomcatAgent(tomcat1RoleId);
                
        String msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        checkLogForMsg(envProperties, standaloneMachineId, tomcat1LogFile, msg);
        
    } 
    
     /* This test fails currently due to open defect - DE137790 */
    @Test(groups = { "Full" }, enabled = true)
    public void verify_ALM_447473_Verify_agent_connected_to_MOM_in_disallowed_mode_when_agent_is_excluded_in_loadbalancingxml()
            throws Exception {      
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat", "jboss", "tomcat1"};
        setuptestcase("447473",serversList);        
        replaceProp("introscope.agent.agentName=Tomcat Agent",
                "introscope.agent.agentName=AffinityAgent", tomcatMachineId,
                tomcatAgentProfile);
        replaceProp("introscope.agent.agentName=JBoss Agent",
                "introscope.agent.agentName=AffinityAgent", jbossMachineId,
                jbossAgentProfile);
        replaceProp("introscope.agent.agentName=Tomcat Agent",
                "introscope.agent.agentName=AffinityAgent", tomcat1MachineId,
                tomcat1AgentProfile);
        
        setattributeinapmthresholdXML(collector1MachineId, col1apmthresholdxmlpath, agentconnlimitxpath, agentevntthresholdattribute, "2");
        
        startEM(momRoleId);
        clw.setAllowedAgentWithCollector(user, password, agent_collector_name,
                ".*\\|.*\\|.*AffinityAgent.*", collector1Host, collector1Port,
                momHost, momPort, momLibDir);
        XMLUtil.changeAttributeValue(loadBalanceFile, "collector", "latched",
                "false", "true");
        
        harvestWait(120);       
        startEM(collector1RoleId);
        startEM(collector2RoleId);
        
        startTomcatAgent(tomcatRoleId);
        startJBossAgent(jbossRoleId);
        
        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        int count = StringUtils.countMatches(tempResult1, "AffinityAgent");
        Assert.assertEquals( "Agents started are not connected to Collector1 ",
                2, count);
        
        startTomcatAgent(tomcat1RoleId);
                
        String msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        checkLogForMsg(envProperties, standaloneMachineId, tomcat1LogFile, msg);
        
        msg = "[Manager] Reporting Clamp hit for MaxAgentConnections limit to MOM";
        checkLogForMsg(envProperties, collector1MachineId, col1LogFile, msg);
        
        msg = "[Manager] Collector " + collector1Host + "@" + collector1Port +" reported Clamp hit for MaxAgentConnections limit";
        checkLogForMsg(envProperties, momMachineId, momLogFile, msg);
        
    }   
    
    /* This test fails currently due to open defect - DE137790 */
    @Test(groups = {"Full"}, enabled = true)
    public void verify_ALM_362049_tt79897_Agent_should_be_disallowed_in_MOM_when_collectors_within_cluster_are_down()
        throws Exception {
       
        serversList = new String[]{"mom", "col1", "tomcat"};
        setuptestcase("362049",serversList);
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
            "introscope.apm.agentcontrol.agent.allowed=false", momMachineId, momConfigFile);
        try {
            xmlutil.addCollectorsEntryInLoadbalanceXML(loadBalanceFile, agent_collector_name,
                ".*\\|.*\\|.*Tomcat.*", collector2Host + ":" + collector2Port, collector3Host + ":"
                    + collector3Port, "include");
        } catch (Exception e) {
            e.printStackTrace();
        }
        replaceProp("agentManager.url.1=" + momHost + ":" + momPort, "agentManager.url.1="
            + collector1Host + ":" + collector1Port, tomcatMachineId, tomcatAgentProfile);

        startEM(collector1RoleId);
        startTomcatAgent(tomcatRoleId);
        harvestWait(60);
        String msg1 =
            "Connected controllable Agent to the Introscope Enterprise Manager at "
                + collector1Host;
       
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);           

        startEM(momRoleId);
        String msg2 =
            "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
      
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg2);
       
    } 


    @Test(groups = {"Full"}, enabled = true)
    public void verify_ALM_449090_Agents_with_same_name_running_on_a_single_machine_are_not_listed_properly_in_denied_agent_list()
        throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "col3", "tomcat", "jboss"};
        setuptestcase("449090",serversList);
        replaceProp("introscope.agent.customProcessName=JBoss",
            "introscope.agent.customProcessName=Tomcat", jbossMachineId, jbossAgentProfile);
        replaceProp("introscope.agent.agentName=JBoss Agent",
            "introscope.agent.agentName=Tomcat Agent", jbossMachineId, jbossAgentProfile);
        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);
        startEM(collector3RoleId);
        startTomcatAgent(tomcatRoleId);
        startJBossAgent(jbossRoleId);        
        harvestWait(120);
        
        tempResult1 = clw.getNodeList(user, password, ".*", momHost, momPort, momLibDir).toString();
        Assert.assertTrue("Tomcat Agent is not connected to the Collector",
            tempResult1.contains("Tomcat Agent") && tempResult1.contains("Tomcat Agent%1"));

        xmlutil.addEmptyCollectorEntryInLoadbalanceXML(loadBalanceFile, agent_collector_name,
            ".*\\|.*\\|.*", "include");
        harvestWait(120);
        
        String msg =
            "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
             
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);        
        checkLogForMsg(envProperties, jbossMachineId, jbossLogFile, msg);
        
    }
    
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_447471_Verify_agent_connected_to_MOM_in_disallowed_mode_when_agent_limit_clamp_hit_on_more_than_1_collector()
            throws Exception {      
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat", "jboss", "tomcat1", "jboss1"};
        setuptestcase("447471",serversList);        
        
        setattributeinapmthresholdXML(collector1MachineId, col1apmthresholdxmlpath, agentconnlimitxpath, agentevntthresholdattribute, "2");
        setattributeinapmthresholdXML(collector2MachineId, col2apmthresholdxmlpath, agentconnlimitxpath, agentevntthresholdattribute, "1");
        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);
        
        startTomcatAgent(tomcatRoleId);
        startJBossAgent(jbossRoleId);
        startTomcatAgent(tomcat1RoleId);
        harvestWait(120);
        
        String agentExpression =
                "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)(.*)";
        String numberofAgentsMetric =
                "Enterprise Manager\\|Connections:Number of Agents";
        String col1MetricValue =
                clw.getLatestMetricValue("admin", "", agentExpression,
                        numberofAgentsMetric,collector1Host, collector1Port, momLibDir);
        String col2MetricValue =
                clw.getLatestMetricValue("admin", "", agentExpression,
                        numberofAgentsMetric,collector2Host, collector2Port, momLibDir);
        startJBossAgent(jboss1RoleId);
        
        String msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        checkLogForMsg(envProperties, standaloneMachineId, jboss1LogFile, msg);
        
        if (col1MetricValue.contains("2")) {    
        
            msg = "[Manager] Reporting Clamp hit for MaxAgentConnections limit to MOM";
            checkLogForMsg(envProperties, collector2MachineId, col2LogFile, msg);
            msg = "[Manager] Collector " + collector2Host + "@" + collector2Port +" reported Clamp hit for MaxAgentConnections limit";
            checkLogForMsg(envProperties, momMachineId, momLogFile, msg);
         
        } else if (col2MetricValue.contains("2")) {
            msg = "[Manager] Reporting Clamp hit for MaxAgentConnections limit to MOM";
            checkLogForMsg(envProperties, collector1MachineId, col1LogFile, msg);
            msg = "[Manager] Collector " + collector1Host + "@" + collector1Port +" reported Clamp hit for MaxAgentConnections limit";
            checkLogForMsg(envProperties, momMachineId, momLogFile, msg);
        }
        else
            Assert.assertTrue("Agents are not connected to the respective collectors",false);   
        
    }
    
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_448521_Verify_agent_connected_to_StandAlone_EM_when_metric_limit_clamp_got_hit()
            throws Exception {
        
        serversList = new String[]{"standalone", "tomcat", "jboss"};
        setuptestcase("448521",serversList);
        replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
                "agentManager.url.1=" + standaloneHost + ":" + standalonePort,
                tomcatMachineId, tomcatAgentProfile);
        replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
                "agentManager.url.1=" + standaloneHost + ":" + standalonePort,
                jbossMachineId, jbossAgentProfile);     
        
        startEM(standaloneRoleId);
        startTomcatAgent(tomcatRoleId);
        harvestWait(180);       
        
        String agentExpression =
                "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)(.*)";
        String numberofMetrics =
                "Enterprise Manager\\|Connections:Number of Metrics";
        String numberofMetricsValue =
                clw.getLatestMetricValue("admin", "", agentExpression,
                        numberofMetrics,standaloneHost, standalonePort, standaloneLibDir);      
        
        String[] value = numberofMetricsValue.split(":::");         
        String metricslivelimit = Integer.toString(Integer.parseInt(value[1])+5);   
        
        setattributeinapmthresholdXML(standaloneMachineId, standaloneapmthresholdxmlpath, agentmetricslivelimitxpath, agentevntthresholdattribute, metricslivelimit);
        harvestWait(120);
        
        startJBossAgent(jbossRoleId);
        
        String msg = "[Manager.Agent] The EM has too many live metrics reporting from Agents  and will stop accepting new metrics from Agents";
        checkLogForMsg(envProperties, standaloneMachineId, standaloneLogFile, msg);                 
        
        setattributeinapmthresholdXML(standaloneMachineId, standaloneapmthresholdxmlpath, agentmetricslivelimitxpath, agentevntthresholdattribute, "50000");
        
        msg = "[Manager.Agent] The EM is now accepting new live metrics from Agents after clamp reset";
        checkLogForMsg(envProperties, standaloneMachineId, standaloneLogFile, msg);         
        harvestWait(60);
        
        numberofMetricsValue =
                clw.getLatestMetricValue("admin", "", agentExpression,
                        numberofMetrics,standaloneHost, standalonePort, standaloneLibDir);
        value = numberofMetricsValue.split(":::");
        
        Assert.assertTrue("Number of Metrics is not increasing event after the metric live limit clamp is increased",
                          Integer.parseInt(value[1]) > Integer.parseInt(metricslivelimit));     
        
    } 
    
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_448546_Verify_agent_connected_to_MOM_when_metric_limit_clamp_got_hit()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "tomcat", "jboss"};
        setuptestcase("448546",serversList);
        startEM(momRoleId);
        startEM(collector1RoleId);      
        startTomcatAgent(tomcatRoleId);
        harvestWait(180);
        
        String agentExpression =
                "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)(.*)";
        String numberofMetrics =
                "Enterprise Manager\\|Connections:Number of Metrics";
        String numberofMetricsValue =
                clw.getLatestMetricValue("admin", "", agentExpression,
                        numberofMetrics,collector1Host, collector1Port, momLibDir);     
        
        String[] value = numberofMetricsValue.split(":::");         
        String metricslivelimit = Integer.toString(Integer.parseInt(value[1])+5);   
        
        setattributeinapmthresholdXML(collector1MachineId, col1apmthresholdxmlpath, agentmetricslivelimitxpath, agentevntthresholdattribute, metricslivelimit);
        String msg = "[Manager.Agent] [Clamp : introscope.enterprisemanager.metrics.live.limit, Value : " + metricslivelimit +"]";
        checkLogForMsg(envProperties, collector1MachineId, col1LogFile, msg);               
        
        startJBossAgent(jbossRoleId);
        
        msg = "[Manager.Agent] The EM has too many live metrics reporting from Agents  and will stop accepting new metrics from Agents";
        checkLogForMsg(envProperties, collector1MachineId, col1LogFile, msg);
        
        msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        
        checkLogForMsg(envProperties, jbossMachineId, jbossLogFile, msg);
        
        setattributeinapmthresholdXML(collector1MachineId, col1apmthresholdxmlpath, agentmetricslivelimitxpath, agentevntthresholdattribute, "50000");
        
        msg = "[Manager.Agent] The EM is now accepting new live metrics from Agents after clamp reset";
        checkLogForMsg(envProperties, collector1MachineId, col1LogFile, msg);
        harvestWait(60);
        
        numberofMetricsValue =
                clw.getLatestMetricValue("admin", "", agentExpression,
                        numberofMetrics,collector1Host, collector1Port, momLibDir);     
        
        value = numberofMetricsValue.split(":::"); 
        
        Assert.assertTrue("Number of Metrics is not increasing in col1 event after the metric live limit clamp is increased",
                  Integer.parseInt(value[1]) > Integer.parseInt(metricslivelimit));     
        
        msg = "Connected to " + collector1Host;
        checkLogForMsg(envProperties, jbossMachineId, jbossLogFile, msg);           
        
    } 
    
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_448547_Verify_agent_connected_directly_to_collector_when_metric_limit_clamp_is_hit()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "tomcat", "jboss"};
        setuptestcase("448547",serversList);
        
        replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
                "agentManager.url.1=" + collector1Host + ":" + collector1Port,
                tomcatMachineId, tomcatAgentProfile);
        replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
                "agentManager.url.1=" + collector1Host + ":" + collector1Port,
                jbossMachineId, jbossAgentProfile);
        
        startEM(momRoleId);
        startEM(collector1RoleId);      
        startTomcatAgent(tomcatRoleId);
        harvestWait(180);
        
        String agentExpression =
                "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)(.*)";
        String numberofMetrics =
                "Enterprise Manager\\|Connections:Number of Metrics";
        String numberofMetricsValue =
                clw.getLatestMetricValue("admin", "", agentExpression,
                        numberofMetrics,collector1Host, collector1Port, momLibDir);     
        
        String[] value = numberofMetricsValue.split(":::");         
        String metricslivelimit = Integer.toString(Integer.parseInt(value[1])+5);   
        
        setattributeinapmthresholdXML(collector1MachineId, col1apmthresholdxmlpath, agentmetricslivelimitxpath, agentevntthresholdattribute, metricslivelimit);
        String msg = "[Manager.Agent] [Clamp : introscope.enterprisemanager.metrics.live.limit, Value : " + metricslivelimit +"]";
        checkLogForMsg(envProperties, collector1MachineId, col1LogFile, msg);               
        
        startJBossAgent(jbossRoleId);
        
        msg = "[Manager.Agent] The EM has too many live metrics reporting from Agents  and will stop accepting new metrics from Agents";
        checkLogForMsg(envProperties, collector1MachineId, col1LogFile, msg);
        
        msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        
        checkLogForMsg(envProperties, jbossMachineId, jbossLogFile, msg);
        
    } 
    
    @Test(groups = { "Smoke" }, enabled = true)
    public void verify_ALM_448548_Verify_agent_connection_to_MOM_with_loadbalancing_and_agent_allowed_false()
            throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat", "jboss"};
        setuptestcase("448548",serversList);        
        
        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);      
        
        String agentExpression =
                "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)(.*)";
        String numberofMetrics =
                "Enterprise Manager\\|Connections:Number of Metrics";
        String numberofMetricsValue =
                clw.getLatestMetricValue("admin", "", agentExpression,
                        numberofMetrics,collector1Host, collector1Port, momLibDir);     
        
        String[] value = numberofMetricsValue.split(":::");         
        String metricslivelimit = Integer.toString(Integer.parseInt(value[1])+5);   
        
        setattributeinapmthresholdXML(collector1MachineId, col1apmthresholdxmlpath, agentmetricslivelimitxpath, agentevntthresholdattribute, metricslivelimit);
        String msg = "[Manager.Agent] [Clamp : introscope.enterprisemanager.metrics.live.limit, Value : " + metricslivelimit +"]";
        checkLogForMsg(envProperties, collector1MachineId, col1LogFile, msg);
        
        clw.setAllowedAgentWithCollector(user, password, agent_collector_name,
                ".*\\|.*\\|.*Tomcat.*", collector1Host, collector1Port, momHost,
                momPort, momLibDir);
        XMLUtil.changeAttributeValue(loadBalanceFile, "collector", "latched",
                "false", "true");       
        clw.setDisAllowedAgentWithCollector(user, password,
                agent_collector_name, ".*\\|.*\\|.*JBoss.*", collector2Host,
                collector2Port, momHost, momPort, momLibDir);
        harvestWait(120);
        
        startTomcatAgent(tomcatRoleId);
        startJBossAgent(jbossRoleId);
        
        msg = "[Manager.Agent] The EM has too many live metrics reporting from Agents  and will stop accepting new metrics from Agents";
        checkLogForMsg(envProperties, collector1MachineId, col1LogFile, msg);
        
        msg = "Connected to "
                + momHost
                + ":"
                + momPort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);
        checkLogForMsg(envProperties, jbossMachineId, jbossLogFile, msg);
        
        setattributeinapmthresholdXML(collector1MachineId, col1apmthresholdxmlpath, agentmetricslivelimitxpath, agentevntthresholdattribute, "50000");
        msg = "[Manager.Agent] [Clamp : introscope.enterprisemanager.metrics.live.limit, Value : 50,000]";
        checkLogForMsg(envProperties, collector1MachineId, col1LogFile, msg);
        
        msg = "[Manager.Agent] The EM is now accepting new live metrics from Agents after clamp reset";
        checkLogForMsg(envProperties, collector1MachineId, col1LogFile, msg);
        
        msg = "Connected to " + collector1Host;
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);
        checkLogForMsg(envProperties, jbossMachineId, jbossLogFile, msg); 
        
    }   
    
    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_268918_verify_agent_disallowed_clamp() throws InterruptedException {
       
        serversList = new String[]{"mom", "col1", "tomcat", "jboss"};
        setuptestcase("268918",serversList);
        
        String agentExpression =
                "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)";
        String disallowedAgentsCountMetric =
                "Enterprise Manager\\|Connections:Number of Disallowed Agents";
                                         
        //Set introscope.apm.agentcontrol.agent.allowed to "FALSE" in EM Properties in MOM
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true",
            "introscope.apm.agentcontrol.agent.allowed=false", momMachineId,
            momConfigFile);
        
        startEM(momRoleId);
        startEM(collector1RoleId);
        startTomcatAgent(tomcatRoleId);
        startJBossAgent(jbossRoleId);
     
        String msg = "Connected to "
            + momHost
            + ":"
            + momPort
            + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);
        checkLogForMsg(envProperties, jbossMachineId, jbossLogFile, msg);
        
        String actualMetricValue =
            clw.getLatestMetricValue(user, password, agentExpression,
                           disallowedAgentsCountMetric,momHost, momPort, momLibDir);              
        Assert.assertTrue(actualMetricValue.equals("Integer:::2"));
    }
    
    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_268895_verify_agent_controllability_ON() {
        serversList = new String[]{"standalone","tomcat"};
        setuptestcase("268895",serversList); 
        
        replaceProp("agentManager.url.1=" + momHost + ":" + momPort,
            "agentManager.url.1=" + standaloneHost + ":" + standalonePort,
            tomcatMachineId, tomcatAgentProfile);
        
        startEM(standaloneRoleId);
        startTomcatAgent(tomcatRoleId);    
        
        String msg =
            "Connected to "
                + standaloneHost
                + ":"
                + standalonePort
                + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in allowed mode.";
       
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg);         
    }
    
    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_268936_verify_latch_functionality_in_loadbalancing_file() throws Exception {
        
        serversList = new String[]{"mom", "col1", "col2", "tomcat", "jboss"};
        setuptestcase("268936",serversList); 
                
        String findStr = "AffinityAgent";
        replaceProp("introscope.agent.agentName=Tomcat Agent",
                "introscope.agent.agentName=AffinityAgent", tomcatMachineId,
                tomcatAgentProfile);
        replaceProp("introscope.agent.agentName=JBoss Agent",
                "introscope.agent.agentName=AffinityAgent", jbossMachineId,
                jbossAgentProfile);

        xmlutil.addlatchedEntryInLoadBalXML(loadBalanceFile, "Test-affinity",
                ".*\\|.*\\|.*AffinityAgent.*", collector1Host + ":"
                        + collector1Port,
                collector2Host + ":" + collector2Port, "1:true");

        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);
        startTomcatAgent(tomcatRoleId);
        startJBossAgent(jbossRoleId);
        harvestWait(60);

        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        int count = StringUtils.countMatches(tempResult1, findStr);
        Assert.assertEquals(
                "AffinityAgent is not connected to Collector1 though affinitiy is set for collector1 ",
                2, count);

        xmlutil.changelatchedEntryInLoadBalXML(loadBalanceFile,
                "Test-affinity", collector2Host + ":" + collector2Port,
                collector1Host + ":" + collector1Port);
        harvestWait(240);

        tempResult1 = clw.getNodeList(user, password, ".*", collector2Host,
                collector2Port, momLibDir).toString();
        count = StringUtils.countMatches(tempResult1, findStr);
        Assert.assertEquals(
                "AffinityAgent is not connected to Collector2 though affinitiy is set for collector2 ",
                2, count);

        stopCollectorEM(momRoleId,collector2RoleId);
        harvestWait(120);

        tempResult1 = clw.getNodeList(user, password, ".*", collector1Host,
                collector1Port, momLibDir).toString();
        count = StringUtils.countMatches(tempResult1, findStr);
        Assert.assertEquals(
                "AffinityAgent is not connected to Collector1 though collector2 is shutdown ",
                2, count);

        startEM(collector2RoleId);
        harvestWait(240);

        tempResult1 = clw.getNodeList(user, password, ".*", collector2Host,
                collector2Port, momLibDir).toString();
        count = StringUtils.countMatches(tempResult1, findStr);
        Assert.assertEquals(
                "AffinityAgent is not connected back to Collector2 though it is up ",
                2, count);
    }
    
    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_430033_verify_82188_HostName_in_loadbalancing_agentSpecifier_regex_is_not_caseInsensitive()
        throws InterruptedException {
        String agentExpression = "";
        serversList = new String[] {"mom", "col1", "col2", "tomcat", "tomcat1"};
        setuptestcase("430033", serversList);
        replaceProp("introscope.agent.agentName=Tomcat Agent",
                "introscope.agent.agentName=TomcatAgent", tomcatMachineId,
                tomcatAgentProfile);
        replaceProp("introscope.agent.agentName=Tomcat1 Agent",
                "introscope.agent.agentName=Tomcat1Agent", tomcat1MachineId,
                tomcatAgentProfile);

        startEM(momRoleId);
        startEM(collector1RoleId);
        startEM(collector2RoleId);
        startTomcatAgent(tomcatRoleId);
        startTomcatAgent(tomcat1RoleId);

        waitForAgentNodes(".*\\|.*\\|.*TomcatAgent", momHost, momPort, momLibDir);
        waitForAgentNodes(".*\\|.*\\|.*Tomcat1Agent", momHost, momPort, momLibDir);
        tempResult = clw.getNodeList(user, password, ".*", momHost, momPort, momLibDir);
        for (String s : tempResult) {
            if (s.contains("Tomcat1Agent")) agentExpression = s;

        }
        agentExpression = agentExpression.replace("tas", "TAS");
        LOGGER.info("casesensitive agentexpression is "+agentExpression);
        clw.setAllowedAgentWithCollector(user, password, agent_collector_name,
            ".*\\|.*\\|.*TomcatAgent", collector1Host, collector1Port, momHost, momPort, momLibDir);
        clw.setAllowedAgentWithCollector(user, password, agent_collector_name, agentExpression,
            collector2Host, collector2Port, momHost, momPort, momLibDir);

        waitForAgentNodes(".*\\|.*\\|.*Tomcat.*", momHost, momPort, momLibDir);
        harvestWait(240);

        tempResult1 =
            clw.getNodeList(user, password, ".*", collector1Host, collector1Port, momLibDir)
                .toString();
        Assert.assertTrue("Tomcat Agent1 is not connected to the Collector1 after rebalance",
            tempResult1.contains("TomcatAgent"));

        tempResult2 =
            clw.getNodeList(user, password, ".*", collector2Host, collector2Port, momLibDir)
                .toString();
        Assert.assertTrue("Tomcat Agent2 is not connected to the Collector2 after rebalance",
            tempResult2.contains("Tomcat1Agent"));
    }
    
    @Test(groups = {"BAT"}, enabled = true)
    public void verify_ALM_430036_verify_81615_Collector_not_able_to_add_to_MOM_after_hotconfig() throws InterruptedException {
        serversList = new String[]{"mom", "col1", "tomcat"};
        setuptestcase("430036",serversList); 

        replaceProp("introscope.enterprisemanager.clustering.login.em1.host="+collector1Host,
                "introscope.enterprisemanager.clustering.login.em1.host=xxx", momMachineId, momConfigFile);
        
        startEM(momRoleId);
        startEM(collector1RoleId);
        startTomcatAgent(tomcatRoleId);
        
        replaceProp("introscope.enterprisemanager.clustering.login.em1.host=xxx",
            "introscope.enterprisemanager.clustering.login.em1.host="+collector1Host, momMachineId, momConfigFile);
        harvestWait(120);      
        LOGGER.info("Checking if collector1 is connected to the MOM");
        String msg1 = "Connected to the Introscope Enterprise Manager at " + collector1Host;
        checkLogForMsg(envProperties, momMachineId, momLogFile, msg1);
        waitForAgentNodes(tomcatAgentExpression, collector1Host, collector1Port, momLibDir);
        msg1 =
            "Connected controllable Agent to the Introscope Enterprise Manager at "
                + collector1Host;
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);
        
    }
    
    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_269830_verify_Agent_ALLOW_DISALLOW_on_Collector_with_agent() throws InterruptedException {
        serversList = new String[]{"col1", "tomcat"};
        setuptestcase("269830",serversList); 
        
        // Point agent to connect to Collector1 directly
        replaceProp(momHost, collector1Host, tomcatMachineId, tomcatAgentProfile);
        startEM(collector1RoleId);
        startTomcatAgent(tomcatRoleId);
               
        String msg1 =
            "Connected controllable Agent to the Introscope Enterprise Manager at "
                + collector1Host;
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);
        tempResult1 =
            clw.getNodeList(user, password, ".*", collector1Host, collector1Port, momLibDir)
                .toString();
        Assert.assertTrue(tempResult1.contains("Tomcat Agent"));
        LOGGER.info("Test1 Passed - Agent direct connection to collector1 successful");
        stopCollectorEM(momRoleId,collector1RoleId);
        //configure agent allowed property to false in collector1
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true", "introscope.apm.agentcontrol.agent.allowed=false", collector1MachineId, col1ConfigFile);   
        startEM(collector1RoleId);
        harvestWait(60);
        String msg2 = "Connected to "
            + collector1Host
            + ":"
            + collector1Port
            + ",com.wily.isengard.postofficehub.link.net.DefaultSocketFactory in disallowed mode.";
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg2);
        LOGGER.info("Test2 Passed - Agent connected to collector1 in disallowed mode");
    }
    
    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_323220_verify_71054_Agent_to_connect_to_last_known_connected_Collector_when_all_other_Collectors_not_reachable() throws InterruptedException {
       
        serversList = new String[]{"mom", "col1", "tomcat"};
        setuptestcase("323220",serversList);
                                   
        //Modify Tomcat Agent profile file to connect directly to Collector 1
        replaceProp(momHost, collector1Host, tomcatMachineId, tomcatAgentProfile);
        startEM(collector1RoleId);
        startTomcatAgent(tomcatRoleId);
        String msg1 =
            "Connected controllable Agent to the Introscope Enterprise Manager at "
                + collector1Host;
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg1);
        
        stopCollectorEM(momRoleId, collector1RoleId);
        stopTomcatAgent(tomcatRoleId);
        renameFile(tomcatLogFile, tomcatLogFile+"1_323220", tomcatMachineId);
        startEM(momRoleId);
        startEM(collector1RoleId);
        checkLogForMsg(envProperties, collector1MachineId, col1LogFile, "MOM Introscope Enterprise Manager connected");
        startTomcatAgent(tomcatRoleId);
        waitForAgentNodes(tomcatAgentExpression, collector1Host, collector1Port, momLibDir);
        harvestWait(60);
        // Allow collector 2 and 3 to connect to Tomcat Agent
        LOGGER.info("Configuring tomcat agent to connect on collector2 and collector3");
        clw.setAllowedAgentWithCollector(user, password, agent_collector_name,
            tomcatAgentExpression, collector2Host, collector2Port, momHost, momPort, momLibDir);
        clw.setAllowedAgentWithCollector(user, password, agent_collector_name,
            tomcatAgentExpression, collector3Host, collector3Port, momHost, momPort, momLibDir);
        harvestWait(180);

        String msg2 = "Lost contact with the Introscope Enterprise Manager at " + collector1Host + ":"
                + collector1Port;
        checkLogForMsg(envProperties, tomcatMachineId, tomcatLogFile, msg2);
        waitForAgentNodes(tomcatAgentExpression, momHost, momPort, momLibDir);
        tempResult1 =
            clw.getNodeList(user, password, ".*", collector1Host, collector1Port, momLibDir)
                .toString();
        Assert.assertTrue("Tomcat Agent is not connected to the Collector1- the last known collector",
            tempResult1.contains("Tomcat Agent"));      
     }
    
    @Test(groups = {"SMOKE"}, enabled = true)
    public void verify_ALM_268919_verify_number_of_disallowed_agents_clamped() throws InterruptedException {
        serversList = new String[]{"mom", "col1", "tomcat", "tomcat1", "jboss"};
        setuptestcase("268919",serversList);

        int numberOfAgents = 3;
        int disallowedMetricLimit = 2;
        int numberOfDisallowedAgentsClamped = (numberOfAgents-disallowedMetricLimit);
        
        String agentExpression =
                "(.*)\\|Custom Metric Process \\(Virtual\\)\\|Custom Metric Agent \\(Virtual\\)";
        String disallowedAgentsClampedMetric =
                "Enterprise Manager\\|Connections:Disallowed Agents Clamped";
                    
        replaceProp("introscope.apm.agentcontrol.agent.allowed=true", "introscope.apm.agentcontrol.agent.allowed=false", momMachineId, momConfigFile);
        replaceProp("introscope.enterprisemanager.agent.disallowed.connection.limit=0", "introscope.enterprisemanager.agent.disallowed.connection.limit=2", momMachineId, momConfigFile);
        startEM(momRoleId);
        startEM(collector1RoleId);
        startTomcatAgent(tomcatRoleId);
        startJBossAgent(jbossRoleId);
        startTomcatAgent(tomcat1RoleId);
        harvestWait(60);
        String actualMetricValue =
            clw.getLatestMetricValue("admin", "", agentExpression,
                disallowedAgentsClampedMetric,momHost, momPort, momLibDir);
        System.out.println("Disallowed agents clamped: " +actualMetricValue);
        
        Assert.assertTrue(actualMetricValue.contains(String.valueOf(numberOfDisallowedAgentsClamped)));
    }
    
    @AfterMethod(alwaysRun = true)
    public void stopservicesandrevertchanges() {
        
        stopEMServices();
        stopAllAgents();
        revertConfigFiles();
        renamelogfiles();
    }

}
