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

package com.ca.apm.tests.test;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.tests.testbed.OneMomOneCollectorAbstractTestbed;
import com.ca.apm.tests.testbed.OneMomOneCollectorLinuxTestbed;
import com.ca.apm.tests.testbed.OneMomOneCollectorThreeAgentsAbstractTestbed;
import com.ca.apm.tests.testbed.OneMomOneCollectorThreeAgentsLinuxTestbed;
import com.ca.apm.tests.testbed.OneMomTwoCollectorsAbstractTestbed;
import com.ca.apm.tests.testbed.OneMomTwoCollectorsLinuxTestbed;
import com.ca.apm.tests.utils.FlowUtils;
import com.ca.apm.tests.utils.agents.AgentLocalUtils;
import com.ca.apm.tests.utils.clw.ClwUtils;
import com.ca.apm.tests.utils.configutils.PropertiesUtility;
import com.ca.apm.tests.utils.configutils.TimeUtility;
import com.ca.apm.tests.utils.emutils.EmBatLocalUtils;
import com.ca.apm.tests.utils.emutils.EmConfiguration;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.envproperty.EnvironmentPropertyException;
import com.ca.tas.envproperty.MachineEnvironmentProperties;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * <h5>TESTS for HTTPS</h5> <br>
 * <h5>HTTPS configuration w/o validation</h5> <br>
 * <bl> <li>On EM:</li>
 * <ol>
 * <li>Go to IntroscopeEnterpriseManager.properties file and comment property
 * introscope.enterprisemanager.enabled.channels=channel1 and Uncomment the property
 * introscope.enterprisemanager.enabled.channels=channel1,channel2</li>
 * <li>uncomment the property
 * introscope.enterprisemanager.webserver.jetty.configurationFile=em-jetty-config.xml</li>
 * </ol>
 * <li>On Agent</li>
 * <ol>
 * <li>Go to IntroscopeAgent.profile and comment the <br>
 * introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT=machine name<br>
 * introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT=5001<br>
 * introscope.agent.enterprisemanager.transport.tcp.socketfactory.DEFAULT=com.wily.isengard.
 * postofficehub.link.net.DefaultSocketFactory</li>
 * <li>uncomment the<br>
 * introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT=machine name<br>
 * introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT=8444<br>
 * introscope.agent.enterprisemanager.transport.tcp.socketfactory.DEFAULT=com.wily.isengard.
 * postofficehub.link.net.HttpsTunnelingSocketFactory<br>
 * </li>
 * </ol>
 * 
 * 
 * 
 * 
 * 
 * @author sobar03
 *
 */
public class HttpsTests {

    private static final Logger log = LoggerFactory.getLogger(HttpsTests.class);
    private EnvironmentPropertyContext envProps;

    @BeforeTest
    public void setUp() throws EnvironmentPropertyException, IOException {
        envProps = new EnvironmentPropertyContextFactory().createFromSystemProperty();

    }



    /**
     * Tests if the settings on MOM overwrite settings on collector
     * 
     * EM BAT tests # 353260 <br>
     * Author : Artur Sobieski
     *
     * <h5>PRE-REQUISITES:</h5> <br>
     * <ol>
     * <li>Install EM as MOM & Collector with default options</li>
     * <li>Configure with HTTPS configuration.</li>
     * </ol>
     *
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * <li>Open IntroscopeEnterpriseManager.properties file in Collector and set
     * introscope.apm.agentcontrol.agent.allowed as FALSE</li>
     * <li>Open IntroscopeEnterpriseManager.properties file in MOM and set
     * introscope.apm.agentcontrol.agent.allowed as TRUE</li>
     * <li>Configure 9.1 Agent say Agent1 and Connect to MOM with HTTPS configuration</li>
     * <li>Based on Collector settings Agent1 should not connect to Collector, but as MOM overrides
     * the setting in Collector Agent should connect to Collector.</li>
     * <li>Open IntroscopeEnterpriseManager.properties file in MOM and set
     * introscope.apm.agentcontrol.agent.allowed as FALSE</li>
     * <li>Check if agent is still connected</li>
     * </ol>
     * </p>
     *
     * <h5>EXPECTED RESULTS</h5>
     * <p>
     * <ol>
     * 
     * <li>The property is applicable for the new agents coming in. In this case, the agent would be
     * still connected and be reporting metrics.</li>
     * </ol>
     * </p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * <ul>
     * <li>Possibility of properties not being overridden by MOM</li>
     * </ul>
     * </p>
     * 
     * @author sobar03
     *
     */


    @Tas(testBeds = {@TestBed(name = OneMomOneCollectorLinuxTestbed.class, executeOn = OneMomOneCollectorAbstractTestbed.MOM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "sobar03")
    @Test(groups = {"BAT", "HTTPS"})
    public void settingsOverrideTest() throws Exception {

        final String emInstallDir =
            (String) envProps.getMachineProperties()
                .get(OneMomOneCollectorAbstractTestbed.COLLECTOR_MACHINE_ID)
                .get(OneMomOneCollectorAbstractTestbed.KeyEmInstallDir);

        final String tomcatInstallDir =
            (String) envProps.getMachineProperties()
                .get(OneMomOneCollectorAbstractTestbed.COLLECTOR_MACHINE_ID)
                .get(OneMomOneCollectorAbstractTestbed.KeyTomcatInstallDir);

        final String clWorkstationDir =
            (String) envProps.getMachineProperties()
                .get(OneMomOneCollectorAbstractTestbed.COLLECTOR_MACHINE_ID)
                .get(OneMomOneCollectorAbstractTestbed.KeyClWorkstationJarFileLocation);

        final String colhostname =
            envProps.getMachinePropertyById(OneMomOneCollectorAbstractTestbed.COLLECTOR_MACHINE_ID,
                MachineEnvironmentProperties.HOSTNAME);

        TimeUtility.synchronizeTimeWithCAServer();

        FlowUtils.synchronizeTime(envProps, OneMomOneCollectorAbstractTestbed.COLLECTOR_MACHINE_ID);

        AgentLocalUtils agent = new AgentLocalUtils(tomcatInstallDir);

        ClwUtils cu = new ClwUtils();

        cu.setClWorkstationJarFileLocation(clWorkstationDir);
        AgentLocalUtils agentUtil = new AgentLocalUtils(tomcatInstallDir);
        EmConfiguration configuration = new EmConfiguration(emInstallDir, 5001);

        EmBatLocalUtils.setUpHttpsProperties(configuration.getPropertiesPath());

        FlowUtils.setUpEMHttpsProperties(envProps,
            OneMomOneCollectorAbstractTestbed.COLLECTOR_MACHINE_ID,
            configuration.getPropertiesPath());

        FlowUtils.setUpAgentHttpsProperties(envProps,
            OneMomOneCollectorAbstractTestbed.COLLECTOR_MACHINE_ID, agentUtil.getProfilePath());

        // Setting collector test properties
        FlowUtils
            .updateProperty(envProps, OneMomOneCollectorAbstractTestbed.COLLECTOR_MACHINE_ID,
                configuration.getPropertiesPath(), "introscope.apm.agentcontrol.agent.allowed",
                "false");

        FlowUtils.startEm(envProps, OneMomOneCollectorAbstractTestbed.COLLECTOR_MACHINE_ID,
            OneMomOneCollectorAbstractTestbed.COLLECTOR_ROLE_ID);


        EmBatLocalUtils.startLocalEm(configuration);

        FlowUtils.startTomcat(envProps, OneMomOneCollectorAbstractTestbed.COLLECTOR_MACHINE_ID,
            tomcatInstallDir);

        // Wait for connection
        Thread.sleep(180 * 1000L);

        FlowUtils.isKeywordInLog(envProps, OneMomOneCollectorAbstractTestbed.COLLECTOR_MACHINE_ID,
            agent.getLogPath(), "Connected controllable Agent");


        HashMap<String, String> newProps = new HashMap<String, String>();
        newProps.put("introscope.apm.agentcontrol.agent.allowed", "false");

        PropertiesUtility.saveProperties(configuration.getPropertiesPath(), newProps, false);

        Thread.sleep(60 * 1000);

        // should stay connected
        assertEquals(1, cu.getAgents(colhostname, "Active"));



    }

    /**
     * Author : Artur<br>
     * TEST# 353257 <h5>TEST ACTIVITY</h5> <br>
     * Testing if disallowing agents works correctly.<br>
     * <br>
     * 
     * 
     * <h5>PRE-REQUISITES:</h5> <br>
     * <ol>
     * <li>Install a cluster with 1 MOM , 1 Collector and 3 Agents
     * <li>Configure the MOM and collector with HTTPS configuration(without validation)
     * </ol>
     * 
     * <h5>TEST ACTIVITY:</h5><br>
     * <ol>
     * <li>Set introscope.apm.agentcontrol.agent.allowed to "FALSE" in EM Properties in MOM</li>
     * <li>Configure HTTPS configuration for 3 agents
     * <li>Start any 3 APM 9.1 agents</li>
     * </ol>
     * 
     *
     * <h5>EXPECTED RESULTS</h5>
     * <p>
     * <ol>
     * <li>All the 3 agents should connect in disallowed mode.</li>
     * </ol>
     * </p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * <ul>
     * <li>Possibility of agents connecting even though MOM is in disallowed mode.</li>
     * </ul>
     * </p>
     * 
     * @throws Exception
     */
    @Tas(testBeds = {@TestBed(name = OneMomOneCollectorThreeAgentsLinuxTestbed.class, executeOn = OneMomOneCollectorThreeAgentsAbstractTestbed.MOM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "sobar03")
    @Test(groups = {"BAT", "HTTPS"})
    public void agentDisallowedClampTest() throws Exception {

        final ClwUtils cu = new ClwUtils();


        final String emInstallDir =
            (String) envProps.getMachineProperties()
                .get(OneMomOneCollectorThreeAgentsAbstractTestbed.COLLECTOR_1_MACHINE_ID)
                .get(OneMomOneCollectorThreeAgentsAbstractTestbed.KeyEmInstallDir);

        final String tomcatInstallDir =
            (String) envProps.getMachineProperties()
                .get(OneMomOneCollectorThreeAgentsAbstractTestbed.COLLECTOR_1_MACHINE_ID)
                .get(OneMomOneCollectorThreeAgentsAbstractTestbed.KeyTomcatInstallDir);

        final String clWorkstationDir =
            (String) envProps.getMachineProperties()
                .get(OneMomOneCollectorThreeAgentsAbstractTestbed.COLLECTOR_1_MACHINE_ID)
                .get(OneMomOneCollectorThreeAgentsAbstractTestbed.KeyClWorkstationJarFileLocation);


        EmConfiguration configuration = new EmConfiguration(emInstallDir, 5001);

        TimeUtility.synchronizeTimeWithCAServer();

        FlowUtils.synchronizeTime(envProps,
            OneMomOneCollectorThreeAgentsAbstractTestbed.COLLECTOR_1_MACHINE_ID);

        FlowUtils.synchronizeTime(envProps,
            OneMomOneCollectorThreeAgentsAbstractTestbed.AGENT_2_MACHINE_ID);

        FlowUtils.synchronizeTime(envProps,
            OneMomOneCollectorThreeAgentsAbstractTestbed.AGENT_3_MACHINE_ID);

        EmBatLocalUtils.setUpHttpsProperties(configuration.getPropertiesPath());

        FlowUtils.setUpEMHttpsProperties(envProps,
            OneMomOneCollectorThreeAgentsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
            configuration.getPropertiesPath());

        AgentLocalUtils agent = new AgentLocalUtils(tomcatInstallDir);

        FlowUtils.setUpAgentHttpsProperties(envProps,
            OneMomOneCollectorThreeAgentsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
            agent.getProfilePath());

        FlowUtils
            .setUpAgentHttpsProperties(envProps,
                OneMomOneCollectorThreeAgentsAbstractTestbed.AGENT_2_MACHINE_ID,
                agent.getProfilePath());

        FlowUtils
            .setUpAgentHttpsProperties(envProps,
                OneMomOneCollectorThreeAgentsAbstractTestbed.AGENT_3_MACHINE_ID,
                agent.getProfilePath());


        HashMap<String, String> newProps = new HashMap<String, String>();
        newProps.put("introscope.apm.agentcontrol.agent.allowed", "false");

        PropertiesUtility.saveProperties(configuration.getPropertiesPath(), newProps, false);

        EmBatLocalUtils.startLocalEm(configuration);

        FlowUtils.startEm(envProps,
            OneMomOneCollectorThreeAgentsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
            OneMomOneCollectorThreeAgentsAbstractTestbed.COLLECTOR_1_ROLE_ID);



        FlowUtils.startTomcat(envProps,
            OneMomOneCollectorThreeAgentsAbstractTestbed.COLLECTOR_1_MACHINE_ID, tomcatInstallDir);
        FlowUtils.startTomcat(envProps,
            OneMomOneCollectorThreeAgentsAbstractTestbed.AGENT_2_MACHINE_ID, tomcatInstallDir);
        FlowUtils.startTomcat(envProps,
            OneMomOneCollectorThreeAgentsAbstractTestbed.AGENT_3_MACHINE_ID, tomcatInstallDir);

        Thread.sleep(240 * 1000L);

        cu.setClWorkstationJarFileLocation(clWorkstationDir);

        int disallowed = cu.getAgents(null, "Disallowed");

        Log.info("Number of Disallowed agents " + disallowed);

        assertEquals(disallowed, 3);

    }


    /**
     * Verifies HTTPS Connectivity of <b>Agents</b>.
     * 
     * ALM test # 351572
     * 
     * @author sobar03
     * 
     *         <h5>PRECONDITIONS</h5>
     *         <p>
     *         <ul>
     *         <li>Provisioned Testbed with One MOM + 2 Collectors + 2 Agents with default
     *         configuration</li>
     *         </ul>
     *         </p>
     *         <h5>TEST ACTIVITY</h5>
     *         <p>
     *         <ol>
     *         <li>Verify mode and collector hosts before starting EM</li>
     *         <li>Go to IntroscopeEnterpriseManager.properties file in EMHome/config folder in MOM
     *         and verify the following properties: <br>
     * 
     *         1) introscope.enterprisemanager.clustering.mode=MOM<br>
     *         2) introscope.enterprisemanager.clustering.login.em1.host=name of collector 1]<br>
     *         introscope.enterprisemanager.clustering.login.em1.port=[ port number of collector 1]<br>
     *         introscope.enterprisemanager.clustering.login.em2.host=[host name of collector 2]<br>
     *         introscope.enterprisemanager.clustering.login.em2.port=[ port number of collector 2]<br>
     *         </li>
     *         <li>Configure the MOM and collectors with HTTPS configuration</li>
     *         <li>Start MOM</li>
     *         <li>Start the Collectors</li>
     *         <li>Verify if collectors are connected to MOM.</li>
     *         <li>Configure agent to connect to MOM in agent profile</li>
     *         <li>Start agent with HTTPS configuration
     *         <li>Verify If MOM redirects the agent to collector in its cluster.(check in Collector
     *         logs)</li>
     *         </ol>
     *         </p>
     *
     *         <h5>EXPECTED RESULTS</h5>
     *         <p>
     *         <ol>
     *         <li>All the properties must be set as expected</li>
     *         <li>To verify EM has started look for message
     *         "Introscope Enterprise Manager started." in IntroscopeEnterpriseManager.log</li>
     *         <li>See collector logs for connection messages:<br>
     *         AT COLLECTOR : "MOM Introscope Enterprise Manager connected"</li>
     *         <li>Verify collector log for connection message <br>
     *         [Manager.Agent] Connected to Agent</li>
     *         </ol>
     *         </p>
     *         <h5>RISKS MITIGATED</h5>
     *         <p>
     *         <ul>
     *         <li>Possibility of agent not connecting to collectors with https settings</li>
     *         </ul>
     *         </p>
     * 
     */
    @Tas(testBeds = {@TestBed(name = OneMomTwoCollectorsLinuxTestbed.class, executeOn = OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "sobar03")
    @Test(groups = {"MOM", "Connectivity", "BAT"})
    public void agentHTTPSClusterTest() throws Exception {



        final String emInstallDir =
            (String) envProps.getMachineProperties()
                .get(OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)
                .get(OneMomTwoCollectorsAbstractTestbed.KeyEmInstallDir);

        final String tomcatDir =
            (String) envProps.getMachineProperties()
                .get(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID)
                .get(OneMomTwoCollectorsAbstractTestbed.KeyTomcatInstallDir);


        EmConfiguration configuration = new EmConfiguration(emInstallDir, 5001);

        TimeUtility.synchronizeTimeWithCAServer();

        FlowUtils.synchronizeTime(envProps,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID);
        FlowUtils.synchronizeTime(envProps,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID);


        EmBatLocalUtils.setUpHttpsProperties(configuration.getPropertiesPath());
        FlowUtils.setUpEMHttpsProperties(envProps,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
            configuration.getPropertiesPath());
        FlowUtils.setUpEMHttpsProperties(envProps,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
            configuration.getPropertiesPath());


        EmBatLocalUtils.startLocalEm(configuration);

        FlowUtils.startEm(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_ROLE_ID);
        FlowUtils.startEm(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_ROLE_ID);


        Thread.sleep(30 * 1000);
        // Verifying connection


        FlowUtils.isKeywordInLog(envProps,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID, configuration.getLogPath(),
            "MOM Introscope Enterprise Manager connected");
        FlowUtils.isKeywordInLog(envProps,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, configuration.getLogPath(),
            "MOM Introscope Enterprise Manager connected");

        log.info("EMs connected to MOM");

        log.info("Setting agent properties");

        FlowUtils.setUpAgentHttpsProperties(envProps,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID, new AgentLocalUtils(
                tomcatDir).getProfilePath());

        FlowUtils.startTomcat(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
            tomcatDir);

        Thread.sleep(30 * 1000);

        boolean connected = false;
        // Checking logs of collector 1 if it's connected to agent
        try {
            FlowUtils.isKeywordInLog(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                configuration.getLogPath(), "[Manager.Agent] Connected to Agent");
            connected = true;
        } catch (Exception e) {
            log.info("Expected exception caught, trying other collector");
        }

        // Agent is connected to collector 1 then no point checking collector 2, since there is only
        // one agent.
        if (!connected) {
            FlowUtils.isKeywordInLog(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
                configuration.getLogPath(), "[Manager.Agent] Connected to Agent");
        }



    }

    /**
     * EM BAT tests # 353262 <br>
     * Test latch functionality in loadbalancing.xml file
     *
     * <h5>PRECONDITIONS</h5>
     * <p>
     * <ul>
     * <li>TestBed with one MOM + 2 Collectors + 2 Agents</li>
     * </ul>
     * </p>
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * <li>Configure HTTPS for MOM + C1 + C2</li>
     * <li>Configure two agents to connect to MOM and configure HTTPS connection for them</li>
     * <li>Open loadbalancing.xml file in MOM and make a setting to allow agent to C1 and C2 with
     * affinity for C1, e.g.
     * 
     * <pre> {@code 
     *  <agent-collector name="EM Name">
     *      <agent-specifier>.*\|.*\|.*</agent-specifier> 
     *      <include> 
     *          <collector host="C1" port="5001" latched="true"/> 
     *          <collector host="C2" port="5001"/> 
     *      </include> 
     *  </agent-collector>
     *  }</pre>
     * </li>
     * <li>Start two agents</li>
     * <li>Both agents should get connected to C1</li>
     * <li>Give affinity to C2 by modifying the loadbalancing.xml file like below E.g.
     * 
     * <pre> {@code 
     * <agent-collector name="EM Name"> 
     * <agent-specifier>.*\|.*\|.*</agent-specifier> 
     *  <include>
     *      <collector host="C1" port="5001" /> 
     *      <collector host="C2" port="5001" latched="true"/>
     *  </include> 
     * </agent-collector>
     *  }</pre>
     * </li>
     * <li>Both agents should get connected to C2</li>
     * <li>After a minute stop C2</li>
     * <li>Both agents should get connected to C1</li>
     * <li>Start C2</li>
     * <li>Both agents should get connected to C2</li>
     * </ol>
     * </p>
     *
     * <h5>EXPECTED RESULTS</h5>
     * <p>
     * <ol>
     * <li>Agents should respect "latched" attribute as it's configured in loadbalancing.xml</li>
     * </ol>
     * </p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * <ul>
     * <li>"latched" attribute in loadbalancing.xml</li>
     * </ul>
     * </p>
     *
     */
    @Tas(testBeds = {@TestBed(name = OneMomTwoCollectorsLinuxTestbed.class, executeOn = OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "turyu01")
    @Test(groups = {"BAT"})
    public void testLatchInLoadbalancingXml() throws Exception {

        // synchronize time
        {
            TimeUtility.synchronizeTimeWithCAServer();
            FlowUtils.synchronizeTime(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID);
            FlowUtils.synchronizeTime(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID);
        }

        final String emInstallDir =
            (String) envProps.getMachineProperties()
                .get(OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)
                .get(OneMomTwoCollectorsAbstractTestbed.KeyEmInstallDir);
        final EmConfiguration config = new EmConfiguration(emInstallDir, 5001);

        // change rebalance interval to 2 minutes for quicker tests
        PropertiesUtility.updateProperty(config.getPropertiesPath(),
            "introscope.enterprisemanager.loadbalancing.interval", "120");

        final String col1hostname =
            envProps.getMachinePropertyById(
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                MachineEnvironmentProperties.HOSTNAME);
        final String col1fqdn = EmBatLocalUtils.hostnameToFqdn(col1hostname);
        final String col2hostname =
            envProps.getMachinePropertyById(
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
                MachineEnvironmentProperties.HOSTNAME);
        final String col2fqdn = EmBatLocalUtils.hostnameToFqdn(col2hostname);

        // Configure HTTPS for MOM + C1 + C2
        log.info("Configuring MOM for HTTPS ...");
        EmBatLocalUtils.setUpHttpsProperties(config.getPropertiesPath());
        log.info("Configuring C1 for HTTPS ...");
        FlowUtils.setUpEMHttpsProperties(envProps,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, config.getPropertiesPath());
        log.info("Configuring C2 for HTTPS ...");
        FlowUtils.setUpEMHttpsProperties(envProps,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID, config.getPropertiesPath());

        // Configure two agents to connect to MOM and configure HTTPS connection for them
        final String tomcatDir =
            (String) envProps.getMachineProperties()
                .get(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID)
                .get(OneMomTwoCollectorsAbstractTestbed.KeyTomcatInstallDir);
        final AgentLocalUtils agentUtil = new AgentLocalUtils(tomcatDir);
        log.info("Configuring A1 for HTTPS ...");
        FlowUtils.setUpAgentHttpsProperties(envProps,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, agentUtil.getProfilePath());
        log.info("Configuring A2 for HTTPS ...");
        FlowUtils.setUpAgentHttpsProperties(envProps,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID, agentUtil.getProfilePath());

        // Open loadbalancing.xml file in MOM and make a setting to allow agent to C1 and C2 with
        // affinity for C1, e.g.
        // <agent-collector name="EM Name">
        // <agent-specifier>.*\|.*\|.*</agent-specifier>
        // <include>
        // <collector host="C1" port="5001" latched="true"/>
        // <collector host="C2" port="5001"/>
        // </include>
        // </agent-collector>
        String loadbalancingXml =
            FileUtils.readFileToString(new File(config.getConfigDirPath() + "loadbalancing.xml"));
        if (loadbalancingXml.contains("<agent-collector name=\"Example 353262\">")) {
            log.info("loadbalancing.xml contains agent-collector group Example 353262 already, no update necessary");
        } else {
            log.info("Modifying loadbalancing.xml : adding agent-collector group Example 353262");

            final String stringToReplace;
            if (loadbalancingXml.contains("</loadbalancing>")) {
                stringToReplace = "</loadbalancing>";
            } else {
                stringToReplace = "loadbalancing0.1.xsd\"/>";
            }

            // C2 has latched="true"
            String newString =
                "    <agent-collector name=\"Example 353262\">\n"
                    + "       <agent-specifier>.*</agent-specifier>\n" + "       <include>\n"
                    + "           <collector host=\"" + col1fqdn
                    + "\" port=\"5001\" latched=\"true\"/>\n" + "           <collector host=\""
                    + col2fqdn + "\" port=\"5001\" latched=\"false\"/>\n" + "       </include>\n"
                    + "   </agent-collector>\n</loadbalancing>";
            if (!loadbalancingXml.contains("</loadbalancing>")) {
                newString = "loadbalancing0.1.xsd\">\n" + newString;
            }

            loadbalancingXml = loadbalancingXml.replace(stringToReplace, newString);
            FileUtils.writeStringToFile(new File(config.getConfigDirPath() + "loadbalancing.xml"),
                loadbalancingXml);
        }

        // Start MOM and Collectors
        {
            log.info("Starting MOM");
            EmBatLocalUtils.startLocalEm(config);
            log.info("Starting Collector1");
            FlowUtils.startEm(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_ROLE_ID);
            log.info("Starting Collector2");
            FlowUtils.startEm(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_ROLE_ID);
        }

        // Start agents
        log.info("Starting Tomcat on Collector1");
        FlowUtils.startTomcat(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
            tomcatDir);

        log.info("Starting Tomcat on Collector2");
        FlowUtils.startTomcat(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
            tomcatDir);

        Thread.sleep(30000);
        Assert.assertFalse(EmBatLocalUtils.isKeywordInLog(config.getLogPath(), "skew"),
            "Some of the collectors probably have skewed time - Check !");

        // Both agents should get connected to C1
        final ClwUtils cu = new ClwUtils();
        final String clWorkstationJarFileLocation =
            (String) envProps.getMachineProperties()
                .get(OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)
                .get(OneMomTwoCollectorsAbstractTestbed.KeyClWorkstationJarFileLocation);
        Assert.assertNotNull(clWorkstationJarFileLocation,
            "KeyClWorkstationJarFileLocation should not be null");
        cu.setClWorkstationJarFileLocation(clWorkstationJarFileLocation);
        {
            final int momAgents = cu.getAgents(null, "Disallowed");
            Assert.assertEquals(momAgents, 0, "None of the Agents should be Disallowed");
            log.info("None of the agents are disallowed - OK");

            final int col2Agents = cu.getAgents(col2hostname, "Active");
            Assert.assertEquals(col2Agents, 0,
                "None of the Agents should be connected to C2 after rebalance");
            log.info("None of the agents are connected to C2 - OK");

            final int col1Agents = cu.getAgents(col1hostname, "Active");
            Assert.assertEquals(col1Agents, 2,
                "Both Agents should be connected to C1 after rebalance, not C2");
            log.info("Both agents are connected to C1 - OK");
        }


        // Give affinity to C2 by modifying the loadbalancing.xml file like below E.g.
        // <agent-collector name="EM Name">
        // <agent-specifier>.*\|.*\|.*</agent-specifier>
        // <include>
        // <collector host="C1" port="5001" />
        // <collector host="C2" port="5001" latched="true"/>
        // </include>
        // </agent-collector></li>
        {
            log.info("Modifying loadbalancing.xml : setting latch to C2");

            final String toReplace1 =
                "<collector host=\"" + col1fqdn + "\" port=\"5001\" latched=\"true\"/>";
            final String newString1 =
                "<collector host=\"" + col1fqdn + "\" port=\"5001\" latched=\"false\"/>";

            final String toReplace2 =
                "<collector host=\"" + col2fqdn + "\" port=\"5001\" latched=\"false\"/>";
            final String newString2 =
                "<collector host=\"" + col2fqdn + "\" port=\"5001\" latched=\"true\"/>";

            String newXml = loadbalancingXml.replace(toReplace1, newString1);
            Assert.assertNotEquals(loadbalancingXml, newXml);
            String newXml1 = newXml.replace(toReplace2, newString2);
            Assert.assertNotEquals(newXml, newXml1);
            FileUtils.writeStringToFile(new File(config.getConfigDirPath() + "loadbalancing.xml"),
                newXml1);
        }

        // Both agents should get connected to C2
        Thread.sleep(250000);
        {
            final int momAgents = cu.getAgents(null, "Disallowed");
            Assert.assertEquals(momAgents, 0, "None of the Agents should be Disallowed");
            log.info("None of the agents are disallowed - OK");

            final int col1Agents = cu.getAgents(col1hostname, "Active");
            Assert.assertEquals(col1Agents, 0,
                "None of the Agents should be connected to C1 after rebalance");
            log.info("None of the agents are connected to C1 - OK");

            final int col2Agents = cu.getAgents(col2hostname, "Active");
            Assert.assertEquals(col2Agents, 2,
                "Both Agents should be connected to C2 after rebalance, not C1");
            log.info("Both agents are connected to C2 - OK");
        }

        // After a minute stop C2
        log.info("Stopping EM on Collector2");
        FlowUtils.stopEm(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_ROLE_ID);

        // Both agents should get connected to C1
        Thread.sleep(120000);
        {
            final int momAgents = cu.getAgents(null, "Disallowed");
            Assert.assertEquals(momAgents, 0, "None of the Agents should be Disallowed");
            log.info("None of the agents are disallowed - OK");

            final int col2Agents = cu.getAgents(col2hostname, "Active");
            Assert.assertEquals(col2Agents, 0,
                "None of the Agents should be connected to C2 after rebalance");
            log.info("None of the agents are connected to C2 - OK");

            final int col1Agents = cu.getAgents(col1hostname, "Active");
            Assert.assertEquals(col1Agents, 2,
                "Both Agents should be connected to C1 after rebalance, not C2");
            log.info("Both agents are connected to C1 - OK");
        }

        // Start C2
        log.info("Starting EM on Collector2");
        FlowUtils.startEm(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_ROLE_ID);

        // Both agents should get connected to C2
        Thread.sleep(250000);
        {
            final int momAgents = cu.getAgents(null, "Disallowed");
            Assert.assertEquals(momAgents, 0, "None of the Agents should be Disallowed");
            log.info("None of the agents are disallowed - OK");

            final int col1Agents = cu.getAgents(col1hostname, "Active");
            Assert.assertEquals(col1Agents, 0,
                "None of the Agents should be connected to C1 after rebalance");
            log.info("None of the agents are connected to C1 - OK");

            final int col2Agents = cu.getAgents(col2hostname, "Active");
            Assert.assertEquals(col2Agents, 2,
                "Both Agents should be connected to C2 after rebalance, not C1");
            log.info("Both agents are connected to C2 - OK");
        }
    }
}
