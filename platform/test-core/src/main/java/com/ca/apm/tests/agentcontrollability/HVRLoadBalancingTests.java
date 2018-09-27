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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
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
import com.ca.apm.tests.testbed.AgentLoadBalancingHVRLinuxClusterTestbed;


public class HVRLoadBalancingTests extends BaseAgentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HVRLoadBalancingTests.class);
    TestUtils utility = new TestUtils();
    CLWCommons clw = new CLWCommons();
    BaseAgentTest bt = new BaseAgentTest();
    EmUtils emUtils = utilities.createEmUtils();

    protected final String momhost;
    protected final String momRoleId;
    protected final String col1RoleID;
    protected final String col2RoleID;
    protected final String col3RoleID;
    protected final String emLibDir;
    protected final String configFile;
    protected final String user;
    protected final String password;
    protected final String AgentExpression;
    protected final String MetricExpression;
    protected final String collector1Host;
    protected final String loadBalanceFile;
    protected final String EMlogFile;
    protected final String loadBalanceFile_Copy;
    protected final String c1Port;
    protected final String c2Port;
    protected final String c3Port;
    protected final String collector2Host;
    protected final String collector3Host;
    protected final String hvrAgentHost;
    protected final String clwJarFileLoc;

    CLWBean clwbean;
    protected final String momPort;
    protected String oldProp;
    protected String newProp;

    XMLUtil xmlUtil = new XMLUtil();
    XMLFileUtil xmlFileUtil = new XMLFileUtil();

    public HVRLoadBalancingTests() {
        momRoleId = AgentLoadBalancingHVRLinuxClusterTestbed.MOM_ROLE_ID;
        col1RoleID = AgentLoadBalancingHVRLinuxClusterTestbed.COLLECTOR1_ROLE_ID;
        col2RoleID = AgentLoadBalancingHVRLinuxClusterTestbed.COLLECTOR2_ROLE_ID;
        col3RoleID = AgentLoadBalancingHVRLinuxClusterTestbed.COLLECTOR3_ROLE_ID;

        AgentExpression = "\".*\\|.*\\|.*\"";
        MetricExpression = ".*CPU.*";
        momPort = envProperties.getRolePropertiesById(momRoleId).getProperty("emPort");
        c1Port = envProperties.getRolePropertiesById(col1RoleID).getProperty("emPort");
        c2Port = envProperties.getRolePropertiesById(col2RoleID).getProperty("emPort");
        c3Port = envProperties.getRolePropertiesById(col3RoleID).getProperty("emPort");

        collector1Host = envProperties.getMachineHostnameByRoleId(col1RoleID);
        collector2Host = envProperties.getMachineHostnameByRoleId(col2RoleID);
        collector3Host = envProperties.getMachineHostnameByRoleId(col3RoleID);

        hvrAgentHost =
            envProperties
                .getMachineHostnameByRoleId(AgentLoadBalancingHVRLinuxClusterTestbed.HVRAGENT_ROLE);

        loadBalanceFile =
            envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing.xml";
        loadBalanceFile_Copy =
            envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR)
                + "/loadbalancing_1.xml";
        momhost =
            envProperties
                .getMachineHostnameByRoleId(AgentLoadBalancingHVRLinuxClusterTestbed.MOM_ROLE_ID);
        emLibDir = envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_LIB_DIR);

        EMlogFile =
            envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_LOG_FILE);

        configFile =
            envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_CONFIG_FILE);
        clwJarFileLoc = emLibDir + "CLWorkstation.jar";

        user = "Admin";
        password = "";
        clwbean = new CLWBean(momhost, user, password, Integer.parseInt(momPort), clwJarFileLoc);

    }
    

    @BeforeTest(alwaysRun=true)
    public void init() {
        List<String> machines = new ArrayList<String>();
        machines.add(AgentLoadBalancingHVRLinuxClusterTestbed.MOM_MACHINE_ID);
        machines.add(AgentLoadBalancingHVRLinuxClusterTestbed.COLLECTOR1_MACHINE_ID);
        machines.add(AgentLoadBalancingHVRLinuxClusterTestbed.COLLECTOR2_MACHINE_ID);
        machines.add(AgentLoadBalancingHVRLinuxClusterTestbed.COLLECTOR3_MACHINE_ID);
        machines.add(AgentLoadBalancingHVRLinuxClusterTestbed.HVRAGENT_MACHINE_ID);
        syncTimeOnMachines(machines);
        setIntroscopeEMProperties();
        startHVRAgent();
        startEMServices();
        
        
    }


    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_359274_CollectorGap() {

        waitForAgentNodes(".*PerfLoad1_Agent1", momhost, Integer.parseInt(momPort), emLibDir);
        List<String> nodeList;
        nodeList = clw.getNodeList("Admin", "", ".*PerfLoad1_Agent1",momhost, Integer.parseInt(momPort), emLibDir);
        String expression ="";
        String phrase = "";
        phrase = "weight=2";
        detectHotChanges(phrase);

        phrase = "weight=3";
        detectHotChanges(phrase);

        phrase = "weight=5";
        detectHotChanges(phrase);

        phrase = "Average collector weight=3.0";
        detectHotChanges(phrase);
        if(nodeList.size()>0)
            expression = nodeList.get(0);
        else
            expression = ".*PerfLoad1_Agent.*";
        expression = expression.replaceAll("\\|", "\\\\|");
        LOGGER.info("The expression is "+expression);
        
        int i=10;
        harvestWait(i*60);
        List<String> l = clw.getMetricValueForTimeInMinutes(user,password, expression, ".*CPU.*" , momhost, Integer.parseInt(momPort), emLibDir,i);
        LOGGER.debug(""+l);
        if(l.size()>i*4)
            Assert.assertTrue(true);
        else
            Assert.assertTrue(false);
        
    }

    @Test(groups = {"FULL"}, enabled = true)
    public void verify_ALM_449079_LoadBalanceThresholdTest() {

        waitForAgentNodes(".*PerfLoad1_Agent.*", momhost, Integer.parseInt(momPort), emLibDir);
        
        String phrase = "";
        phrase = "weight=2";
        detectHotChanges(phrase);

        phrase = "weight=3";
        detectHotChanges(phrase);

        phrase = "weight=5";
        detectHotChanges(phrase);

        phrase = "Average collector weight=3.0";
        detectHotChanges(phrase);
        List<String> list1, list2, list3, list4, list5, list6;

        list1 = getConnectedAgentNamesToCollector(collector1Host, Integer.parseInt(c1Port),".*PerfLoad1_Agent.*");
        list2 = getConnectedAgentNamesToCollector(collector2Host, Integer.parseInt(c2Port),".*PerfLoad1_Agent.*");
        list3 = getConnectedAgentNamesToCollector(collector3Host, Integer.parseInt(c3Port),".*PerfLoad1_Agent.*");
        
        harvestWait(30);
        
        replaceProp("introscope.enterprisemanager.clustering.login.em1.weight=2",
            "introscope.enterprisemanager.clustering.login.em1.weight=1",
            AgentLoadBalancingHVRLinuxClusterTestbed.MOM_MACHINE_ID, configFile);

        replaceProp("introscope.enterprisemanager.clustering.login.em2.weight=3",
            "introscope.enterprisemanager.clustering.login.em2.weight=4",
            AgentLoadBalancingHVRLinuxClusterTestbed.MOM_MACHINE_ID, configFile);
        
        harvestWait(30);
        
        list4 = getConnectedAgentNamesToCollector(collector1Host, Integer.parseInt(c1Port),".*PerfLoad1_Agent.*");
        list5 = getConnectedAgentNamesToCollector(collector2Host, Integer.parseInt(c2Port),".*PerfLoad1_Agent.*");
        list6 = getConnectedAgentNamesToCollector(collector3Host, Integer.parseInt(c3Port),".*PerfLoad1_Agent.*");
        
        if(list1.size() == list4.size())
            LOGGER.debug("It Works for list1!!!");
        if(list2.size() == list5.size())
            LOGGER.debug("It Works for list2!!!");
        if(list3.size() == list6.size())
            LOGGER.debug("It Works for list3!!!");
        
}

    public void stopHVRAgent() {
        LOGGER.info("Stopping HVR Agent");
        stopHVRAgent(momhost, Integer.parseInt(momPort),
            AgentLoadBalancingHVRLinuxClusterTestbed.HVRAGENT_INSTALL_DIRECTORY,
            AgentLoadBalancingHVRLinuxClusterTestbed.HVRAGENT_MACHINE_ID, "extract", "2", "2", "15");

        stopHVRServiceFlowExecutor(AgentLoadBalancingHVRLinuxClusterTestbed.HVRAGENT_MACHINE_ID);
    }

    public void startHVRAgent() {

        LOGGER.info("Starting HVR Agent");
        startHVRAgent(momhost, Integer.parseInt(momPort),
            AgentLoadBalancingHVRLinuxClusterTestbed.HVRAGENT_INSTALL_DIRECTORY,
            AgentLoadBalancingHVRLinuxClusterTestbed.HVRAGENT_MACHINE_ID, "extract", "1", "1", "15");
    }



    public void setIntroscopeEMProperties() {

        List<String> colWeights = new ArrayList<String>();
        colWeights.add("introscope.apm.agentcontrol.clw.enable=true");
        colWeights.add("introscope.enterprisemanager.clustering.login.em1.weight=2");
        colWeights.add("introscope.enterprisemanager.clustering.login.em2.weight=3");
        colWeights.add("introscope.enterprisemanager.clustering.login.em3.weight=5");
        colWeights.add("log4j.logger.Manager.LoadBalancer=DEBUG");
        colWeights.add("log4j.logger.additivity.Manager.LoadBalancer=false");
        appendProp(colWeights, AgentLoadBalancingHVRLinuxClusterTestbed.MOM_MACHINE_ID, configFile);
        replaceProp("introscope.enterprisemanager.loadbalancing.interval=600",
            "introscope.enterprisemanager.loadbalancing.interval=120",
            AgentLoadBalancingHVRLinuxClusterTestbed.MOM_MACHINE_ID, configFile);
        replaceProp("introscope.enterprisemanager.loadbalancing.threshold=20000",
            "introscope.enterprisemanager.loadbalancing.threshold=5000",
            AgentLoadBalancingHVRLinuxClusterTestbed.MOM_MACHINE_ID, configFile);
    }


    public void stopEMServices() {
        stopCollectorEM(momRoleId, col1RoleID);
        stopEMServiceFlowExecutor(AgentLoadBalancingHVRLinuxClusterTestbed.COLLECTOR1_MACHINE_ID);
        stopCollectorEM(momRoleId, col2RoleID);
        stopEMServiceFlowExecutor(AgentLoadBalancingHVRLinuxClusterTestbed.COLLECTOR2_MACHINE_ID);
        stopCollectorEM(momRoleId, col3RoleID);
        stopEMServiceFlowExecutor(AgentLoadBalancingHVRLinuxClusterTestbed.COLLECTOR3_MACHINE_ID);
        stopEM(momRoleId);
        stopEMServiceFlowExecutor(AgentLoadBalancingHVRLinuxClusterTestbed.MOM_MACHINE_ID);
        harvestWait(10);
    }

    public void startEMServices() {
        startEM(col1RoleID);
        startEM(col2RoleID);
        startEM(col3RoleID);
        startEM(momRoleId);
    }


     public void stopEMServiceFlowExecutor(String machineId) {

        StopServiceFlowContext.Builder stopServiceFlowContextBuilder =
            new StopServiceFlowContext.Builder();
        stopServiceFlowContextBuilder.processToKill("Introscope_Enterprise_Manager.lax").build();
        StopServiceFlowContext ssf = new StopServiceFlowContext(stopServiceFlowContextBuilder);

        runFlowByMachineId(machineId, StopServiceFlow.class, ssf);
    }

       public void stopHVRServiceFlowExecutor(String machineId) {

        StopServiceFlowContext.Builder stopServiceFlowContextBuilder =
            new StopServiceFlowContext.Builder();
        stopServiceFlowContextBuilder.processToKill("hvr*").build();
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