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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.LogCheckFlow;
import com.ca.apm.automation.action.flow.utility.LogCheckFlowContext;
import com.ca.apm.automation.action.flow.webapp.DeployTomcatFlowContext;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.commons.coda.common.ApmbaseUtil;
import com.ca.apm.commons.coda.common.XMLUtil;
import com.ca.apm.commons.common.CLWCommons;
import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.common.XMLFileUtil;
import com.ca.apm.commons.flow.StopServiceFlow;
import com.ca.apm.commons.flow.StopServiceFlowContext;
import com.ca.apm.commons.tests.BaseAgentTest;
import com.ca.apm.tests.common.introscope.util.CLWBean;
import com.ca.apm.tests.common.introscope.util.MetricUtil;
import com.ca.apm.tests.testbed.AgentLoadBalancingLinuxTestbed;


public class LoadBalancingCommons extends BaseAgentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancingTests.class);
    TestUtils utility = new TestUtils();
    CLWCommons clw = new CLWCommons();
    BaseAgentTest bt = new BaseAgentTest();
    EmUtils emUtils = utilities.createEmUtils();

    protected final String momhost;
    protected final String momRoleId;
    protected final String col1RoleID;
    protected final String col2RoleID;
    protected final String col3RoleID;
    protected final String tomcatMachineID;
    protected final String emLibDir;
    protected final String configFile;
    protected final String tomcatRoleId;
    protected final String jbossRoleId;
    protected final String user;
    protected final String password;
    protected final String AgentExpression;
    protected final String tomcatAgentExpression;
    protected final String MetricExpression;
    protected final String collector1Host;
    protected final String loadBalanceFile;
    protected final String EMlogFile;
    protected final String loadBalanceFile_Copy;
    protected final String jBossAgentExpression;
    protected final String c1Port;
    protected final String c2Port;
    protected final String c3Port;
    protected final String collector2Host;
    protected final String collector3Host;
    protected final String agentHost;
    protected final String clwJarFileLoc;
    public String metric = "Enterprise Manager:Host";
    protected String tomcatAgentLogFile;
    protected final String momWebPort;
    CLWBean clwbean;
    protected final String momPort;
    protected final String agentProfile;
    protected String oldProp;
    protected String newProp;
    private MetricUtil metricUtil = null;

    XMLUtil xmlUtil = new XMLUtil();
    XMLFileUtil xmlFileUtil = new XMLFileUtil();

    public LoadBalancingCommons() {
        momRoleId = AgentLoadBalancingLinuxTestbed.MOM_ROLE_ID;
        col1RoleID = AgentLoadBalancingLinuxTestbed.COLLECTOR1_ROLE_ID;
        col2RoleID = AgentLoadBalancingLinuxTestbed.COLLECTOR2_ROLE_ID;
        col3RoleID = AgentLoadBalancingLinuxTestbed.COLLECTOR3_ROLE_ID;

        tomcatRoleId = AgentLoadBalancingLinuxTestbed.TOMCAT_ROLE_ID;
        jbossRoleId = AgentLoadBalancingLinuxTestbed.JBOSS_ROLE_ID;
        AgentExpression = "\".*\\|.*\\|.*\"";
        tomcatAgentExpression = "(.*)\\|Tomcat\\|Tomcat Agent";
        MetricExpression = ".*CPU.*";
        jBossAgentExpression = "\".*\\|.*\\|JBoss.*\"";
        momPort = envProperties.getRolePropertiesById(momRoleId).getProperty("emPort");
        c1Port = envProperties.getRolePropertiesById(col1RoleID).getProperty("emPort");
        c2Port = envProperties.getRolePropertiesById(col2RoleID).getProperty("emPort");
        c3Port = envProperties.getRolePropertiesById(col3RoleID).getProperty("emPort");
        momWebPort = envProperties.getRolePropertiesById(momRoleId).getProperty("emWebPort");

        collector1Host = envProperties.getMachineHostnameByRoleId(col1RoleID);
        collector2Host = envProperties.getMachineHostnameByRoleId(col2RoleID);
        collector3Host = envProperties.getMachineHostnameByRoleId(col3RoleID);

        agentHost = envProperties.getMachineHostnameByRoleId(tomcatRoleId);

        loadBalanceFile =
            envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing.xml";
        loadBalanceFile_Copy =
            envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing_1.xml";
        momhost =
            envProperties.getMachineHostnameByRoleId(AgentLoadBalancingLinuxTestbed.MOM_ROLE_ID);
        emLibDir = envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_LIB_DIR);

        EMlogFile =
            envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_LOG_FILE);

        configFile =
            envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        agentProfile =
            envProperties.getRolePropertyById(tomcatRoleId,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR)
                + "/wily/core/config/IntroscopeAgent.profile";
        tomcatMachineID = AgentLoadBalancingLinuxTestbed.AGENT_MACHINE_ID;
        clwJarFileLoc = emLibDir + "CLWorkstation.jar";

        user = "Admin";
        password = "";
        clwbean = new CLWBean(momhost, user, password, Integer.parseInt(momPort), clwJarFileLoc);
        tomcatAgentLogFile =
            envProperties.getRolePropertyById(tomcatRoleId,
                DeployTomcatFlowContext.ENV_TOMCAT_INSTALL_DIR) + "/wily/logs/IntroscopeAgent.log";
    }

    @BeforeTest(alwaysRun = true)
    public void initialize() {

        List<String> machines = new ArrayList<String>();
        machines.add(AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID);
        machines.add(AgentLoadBalancingLinuxTestbed.COLLECTOR1_MACHINE_ID);
        machines.add(AgentLoadBalancingLinuxTestbed.COLLECTOR2_MACHINE_ID);
        machines.add(AgentLoadBalancingLinuxTestbed.AGENT_MACHINE_ID);
        syncTimeOnMachines(machines);

        startTomcatAgent(tomcatRoleId);
        startJBossAgent(jbossRoleId);
    }

    public void waitforEMLog() {
        boolean flag = false;
        File EMLOG = new File(EMlogFile);
        for (int i = 0; i < 20; i++) {
            if (EMLOG.exists()) {
                flag = true;
                break;
            } else
                harvestWait(60);
        }
        Assert.assertTrue(flag);
        harvestWait(10);
    }

    public void setIntroscopeEMProperties() {

        List<String> colWeights = new ArrayList<String>();
        colWeights.add("introscope.apm.agentcontrol.clw.enable=true");
        colWeights.add("introscope.enterprisemanager.clustering.login.em1.weight=1");
        colWeights.add("introscope.enterprisemanager.clustering.login.em2.weight=1");
        colWeights.add("introscope.enterprisemanager.clustering.login.em3.weight=1");
        colWeights.add("log4j.logger.Manager.LoadBalancer=DEBUG");
        colWeights.add("log4j.logger.additivity.Manager.LoadBalancer=false");
        appendProp(colWeights, AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID, configFile);
        replaceProp("introscope.enterprisemanager.loadbalancing.interval=600",
            "introscope.enterprisemanager.loadbalancing.interval=120",
            AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID, configFile);
        replaceProp("introscope.enterprisemanager.loadbalancing.threshold=20000",
            "introscope.enterprisemanager.loadbalancing.threshold=1",
            AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID, configFile);
    }

    public void stopEMServices() {
        stopCollectorEM(momRoleId, col1RoleID);
        stopEMServiceFlowExecutor(AgentLoadBalancingLinuxTestbed.COLLECTOR1_MACHINE_ID);
        stopCollectorEM(momRoleId, col2RoleID);
        stopEMServiceFlowExecutor(AgentLoadBalancingLinuxTestbed.COLLECTOR2_MACHINE_ID);
        stopCollectorEM(momRoleId, col3RoleID);
        stopEMServiceFlowExecutor(AgentLoadBalancingLinuxTestbed.AGENT_MACHINE_ID);
        stopEM(momRoleId);
        stopEMServiceFlowExecutor(AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID);
        harvestWait(10);
    }

    public void startEMServices() {
        try{startEM(col1RoleID);}catch(Exception e){e.printStackTrace();}
        try{startEM(col2RoleID);}catch(Exception e){e.printStackTrace();}
        try{startEM(col3RoleID);}catch(Exception e){e.printStackTrace();}
        try{startEM(momRoleId);}catch(Exception e){e.printStackTrace();}
    }


    public void verifyAllCollectors() {
        int count1 = 0;
        int count2 = 0;
        int count3 = 0;
        int i = 0;
        boolean flag = false;
        for (i=0;i<20;i++)
        {
            count1 =
                verifyCollectorAgent(".*", MetricExpression, collector1Host,
                    Integer.parseInt(c1Port), emLibDir).size();
            count2 =
                verifyCollectorAgent(".*", MetricExpression, collector2Host,
                    Integer.parseInt(c2Port), emLibDir).size();
            count3 =
                verifyCollectorAgent(".*", MetricExpression, collector3Host,
                    Integer.parseInt(c3Port), emLibDir).size();
            i++;
            if (count1 >= 3 || count2 >= 3 || count3 >= 3)
            {
                flag=true;
                break;
            }
            else
                harvestWait(60);
        }
            if (i == 20) flag = false;
        Assert.assertTrue(flag);
    }

    public List<String> checkForCollectorAgents(String agentExpression, String metricExpression,
        String emHost, int emPort, String emLibDir) {
        List<String> nodeList = null;
        int i = 0;
        do {
            nodeList =
                clw.getMetricValueForTimeInMinutes(user, password, agentExpression,
                    metricExpression, emHost, emPort, emLibDir, 1);
            LOGGER.debug("The list size is : " + nodeList.size());
            i++;
            harvestWait(20);
        } while (nodeList.size() <= 2 && i < 20);
        return nodeList;
    }

    public List<String> verifyCollectorAgent(String agentExpression, String metricExpression,
        String emHost, int emPort, String emLibDir) {
        List<String> nodeList = null;
        nodeList =
            clw.getMetricValueForTimeInMinutes(user, password, agentExpression, metricExpression,
                emHost, emPort, emLibDir, 1);
        LOGGER.debug("The list size is : " + nodeList.size());
        return nodeList;
    }


    public void revertPropAndLoadBalancingFiles(String testCaseId) {
        deleteFile(loadBalanceFile, AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID);
        moveFile(loadBalanceFile + "_backup", loadBalanceFile,
            AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID);
        deleteFile(configFile, AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID);
        moveFile(configFile + "_backup", configFile,
            AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID);
        renameLogWithTestCaseID(testCaseId);
    }

    public void backupPropAndLoadBalancingFiles() {
        backupFile(configFile, configFile + "_backup",
            AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID);
        backupFile(loadBalanceFile, loadBalanceFile + "_backup",
            AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID);
    }

    public void stopAllAgents() {
        try{
        stopTomcatAgent(tomcatRoleId);}
        catch(Exception e)
        {
            LOGGER.error(e.toString());
        }
        stopTomcatServiceFlowExecutor(AgentLoadBalancingLinuxTestbed.AGENT_MACHINE_ID);
        try {
            stopJBossAgent(jbossRoleId);
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
        stopJBossServiceFlowExecutor(AgentLoadBalancingLinuxTestbed.AGENT_MACHINE_ID);

    }

    public void stopEMServiceFlowExecutor(String machineId) {

        StopServiceFlowContext.Builder stopServiceFlowContextBuilder =
            new StopServiceFlowContext.Builder();
        stopServiceFlowContextBuilder.processToKill("Introscope_Enterprise_Manager.lax").build();
        StopServiceFlowContext ssf = new StopServiceFlowContext(stopServiceFlowContextBuilder);

        runFlowByMachineId(machineId, StopServiceFlow.class, ssf);
    }

    public void stopTomcatServiceFlowExecutor(String machineId) {

        StopServiceFlowContext.Builder stopServiceFlowContextBuilder =
            new StopServiceFlowContext.Builder();
        stopServiceFlowContextBuilder.processToKill("Tomcat*").build();
        StopServiceFlowContext ssf = new StopServiceFlowContext(stopServiceFlowContextBuilder);

        runFlowByMachineId(machineId, StopServiceFlow.class, ssf);
    }

    public void stopJBossServiceFlowExecutor(String machineId) {

        StopServiceFlowContext.Builder stopServiceFlowContextBuilder =
            new StopServiceFlowContext.Builder();
        stopServiceFlowContextBuilder.processToKill("jboss").build();
        StopServiceFlowContext ssf = new StopServiceFlowContext(stopServiceFlowContextBuilder);

        runFlowByMachineId(machineId, StopServiceFlow.class, ssf);
    }

    public void detectHotChanges(String phrase) {
        int i = 0;
        boolean flag = false;
        do {
            flag = ApmbaseUtil.checklogMsg(EMlogFile, phrase);
            if (flag == true) break;
            i++;
            harvestWait(20);
        } while (i < 20);
        Assert.assertTrue(flag);
    }

    public void DontDetectTheseHotChanges(String phrase) {
        int i = 0;
        boolean flag = false;
        do {
            flag = ApmbaseUtil.checklogMsg(EMlogFile, phrase);
            if (flag == true) break;
            i++;
            harvestWait(20);
        } while (i < 20);
        Assert.assertFalse(flag);
    }

    public String getEMHostNameForAgent(String commonmetricExpr) {
        LOGGER.info(metric);

        metric = commonmetricExpr + "|" + metric;
        LOGGER.info("Metric to check:" + metric);

        metricUtil = new MetricUtil(metric, clwbean);
        int elapsedInterval = 0;
        int chkInterval = 5 * 60;
        String emHostName = metricUtil.getMetricValue();
        while (true) {
            harvestWait(30);
            elapsedInterval = elapsedInterval + 30;

            if (!emHostName.isEmpty()) break;
            if (elapsedInterval == chkInterval) break;
        }
        return emHostName;
    }

    public void renameLogWithTestCaseID(String testCaseId) {
        backupFile(EMlogFile, EMlogFile + "_" + testCaseId,
            AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID);
        deleteFile(EMlogFile, AgentLoadBalancingLinuxTestbed.MOM_MACHINE_ID);
    }


    protected boolean verifyColTomcatAgent(String collectorHost, String cPort) {
        List<String> list =
            checkForCollectorAgents(".*Tomcat.*", MetricExpression, collectorHost,
                Integer.parseInt(cPort), emLibDir);

        Iterator<String> i = list.iterator();

        boolean found = false;
        while (i.hasNext()) {
            if (i.next().toString().trim().toLowerCase().contains("tomcat")) {
                found = true;
                break;
            }
        }
        return found;

    }

    public void checkLog(String message, String logFile) {
        LogCheckFlowContext LCS = LogCheckFlowContext.createWithNoTimeout(logFile, message);
        runFlowByMachineId(tomcatMachineID, LogCheckFlow.class, LCS);
    }

    protected void setAgentProperties() {
        List<String> agentFailoverConnectionOrder = new ArrayList<String>();
        replaceProp("agentManager.url.1=" + momhost + ":" + momPort, "agentManager.url.1="
            + collector1Host + ":" + c1Port, tomcatMachineID, agentProfile);

        agentFailoverConnectionOrder.add("agentManager.url.2=" + momhost + ":" + momPort);
        agentFailoverConnectionOrder
            .add("introscope.agent.enterprisemanager.failbackRetryIntervalInSeconds=120");
        appendProp(agentFailoverConnectionOrder, tomcatMachineID, agentProfile);

    }

    protected List<String> getConnectedAgentsToCollector(String emHost, int emPort,
        String expression) {
        int i = 0;
        List<String> nodeList = null;
        for (i = 0; i < 20; i++) {
            nodeList = clw.getNodeList("Admin", "", expression, emHost, emPort, emLibDir);
            if (nodeList.size() >= 1)
                break;
            else
                harvestWait(3);
        }

        return nodeList;
    }

    protected List<String> getConnectedAgentNamesToCollector(String emHost, int emPort,
        String expression) {

        List<String> list = getConnectedAgentsToCollector(emHost, emPort, expression);
        List<String> names = new ArrayList<String>();

        if (list != null) {
            int k = 0;
            while (k < list.size()) {
                names.add(list.get(k++).split("\\|")[0]);
            }
        }
        return names;
    }

}
