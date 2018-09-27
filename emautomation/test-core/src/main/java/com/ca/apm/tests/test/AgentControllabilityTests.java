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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

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
import com.ca.tas.envproperty.MachineEnvironmentProperties;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Agent Controllability tests
 * ALM # 296852, 430033, 430036, 440678, 450351
 */
public class AgentControllabilityTests {

    private static final Logger log = LoggerFactory.getLogger(AgentControllabilityTests.class);
    private EnvironmentPropertyContext envProps;

    @BeforeTest
    public void setUp() throws Exception {
        envProps = new EnvironmentPropertyContextFactory().createFromSystemProperty();
    }

    /**
     * Verifies EM handling of <b>Agents</b> and agent_control permissions. ALM test#296852
     * 
     * <h5>PRECONDITIONS</h5>
     * <p>
     * <ul>
     * <li>Provisioned Testbed with One MOM + 2 Collectors with default configuration</li>
     * </ul>
     * </p>
     * 
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * <li>Add users ACC1 and ACC2 to users.xml</li>
     * <li>Add the tag &lt;grant group="ACCgroup" permission="agent_control"/&gt; to server.xml</li>
     * <li>Add agents mapping to loadbalancing.xml</li>
     * <li>Edit the value "introscope.apm.agentcontrol.clw.enable=true" in the MOM properties file</li>
     * <li>Restart MOM and Controllers</li>
     * <li>Run CLW command to modify loadbalancing.xml, e.g remove agent ".*\|.*\|EPAgent1"</li>
     * <li>Observe that loadbalancing.xml should be changed/modified</li>
     * </ol>
     * </p>
     * 
     * <h5>EXPECTED RESULTS</h5>
     * <p>
     * loadbalancing.xml should be changed/modified by CLW commands
     * <p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * <ul>
     * <li>Ability to control agents through CLWorkstation</li>
     * </ul>
     * </p>
     * 
     * @throws Exception
     */

    @Tas(testBeds = {@TestBed(name = OneMomTwoCollectorsLinuxTestbed.class, executeOn = OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "turyu01")
    @Test(groups = {"BAT"})
    public void testAgentControlPermissions() throws Exception {

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

        // Add users ACC1 and ACC2 to users.xml
        {
            log.info("Checking users.xml");
            final String usersXml =
                FileUtils.readFileToString(new File(config.getConfigDirPath() + "users.xml"));

            // make sure we add users/groups just once
            if (usersXml.contains("name=\"ACC1\"")) {
                log.info("users.xml contains ACC1 user already, no update necessary");
            } else {
                log.info("Modifying users.xml : adding user ACC1, user ACC2 and group ACCgroup");

                String newXml =
                    usersXml
                        .replace(
                            "</users>",
                            "    <user password=\"\" name=\"ACC1\"/>\n        <user password=\"\" name=\"ACC2\"/>\n    </users>");
                newXml =
                    newXml
                        .replace(
                            "</groups>",
                            "    <group description=\"ACCgroup\" name=\"ACCgroup\">\n            <user name=\"ACC1\"/>\n            <user name=\"ACC2\"/>\n    </group>\n    </groups>");
                FileUtils.writeStringToFile(new File(config.getConfigDirPath() + "users.xml"),
                    newXml);
            }
        }

        // Add the tag <grant group="ACCgroup" permission="agent_control"/> to server.xml
        {
            log.info("Checking server.xml");
            final String serverXml =
                FileUtils.readFileToString(new File(config.getConfigDirPath() + "server.xml"));

            // make sure we add users/groups just once
            if (serverXml.contains("group=\"ACCgroup\"")) {
                log.info("server.xml contains ACCgroup group already, no update necessary");
            } else {
                log.info("Modifying server.xml : adding group ACCgroup");
                String newXml =
                    serverXml.replace("</server>",
                        "    <grant group=\"ACCgroup\" permission=\"agent_control\"/>\n</server>");
                FileUtils.writeStringToFile(new File(config.getConfigDirPath() + "server.xml"),
                    newXml);
            }
        }

        // add agents mapping to loadbalancing.xml
        final String loadbalancingXml =
            FileUtils.readFileToString(new File(config.getConfigDirPath() + "loadbalancing.xml"));
        if (loadbalancingXml.contains("<agent-collector name=\"ACC1\">")) {
            log.info("loadbalancing.xml contains agent-collector group ACC1 already, no update necessary");
        } else {
            log.info("Modifying loadbalancing.xml : adding agent-collector group ACC1");

            final String hostname =
                envProps.getMachinePropertyById(
                    OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                    MachineEnvironmentProperties.HOSTNAME);

            final String stringToReplace;
            if (loadbalancingXml.contains("</loadbalancing>")) {
                stringToReplace = "</loadbalancing>";
            } else {
                stringToReplace = "loadbalancing0.1.xsd\"/>";
            }

            String newString =
                "    <agent-collector name=\"ACC1\">\n        <agent-specifier>.*\\|.*\\|EPAgent1</agent-specifier>\n"
                    + "        <include>\n            <collector host=\""
                    + hostname
                    + "\" port=\"5001\"/>\n        </include>\n    </agent-collector>\n</loadbalancing>";
            if (!loadbalancingXml.contains("</loadbalancing>")) {
                newString = "loadbalancing0.1.xsd\">\n" + newString;
            }

            String newXml = loadbalancingXml.replace(stringToReplace, newString);
            FileUtils.writeStringToFile(new File(config.getConfigDirPath() + "loadbalancing.xml"),
                newXml);
        }

        // In the IntroscopeEnterpriseManager.properties file, set the property
        // 'introscope.apm.agentcontrol.clw.enable=true'
        PropertiesUtility.insertProperty(config.getPropertiesPath(),
            "introscope.apm.agentcontrol.clw.enable", "true");

        // Start MOM , Collector C1 and Collector C2
        {
            log.info("Starting MOM...");
            EmBatLocalUtils.startLocalEm(config);
            log.info("Starting Collector1...");
            FlowUtils.startEm(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_ROLE_ID);
            log.info("Starting Collector2...");
            FlowUtils.startEm(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_ROLE_ID);
        }

        // Now run a clw command to change the xml file.connect to MOM for queringjava -Xmx512M
        // -Duser=ACC -Dpassword= -Dhost=localhost -Dport=5001 -jar CLWorkstation.jar get
        // loadbalancer lastmodified timestamp
        // java -Xmx512M -Duser=ACC -Dpassword= -Dhost=localhost
        // -Dport=5001 -jar CLWorkstation.jar remove agent ".*\|.*\|EPAgent1" Fri Jul 15 20:07:39
        // IST 2011

        final ClwUtils cu = new ClwUtils();
        final String clWorkstationJarFileLocation =
            (String) envProps.getMachineProperties()
                .get(OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)
                .get(OneMomTwoCollectorsAbstractTestbed.KeyClWorkstationJarFileLocation);
        Assert.assertNotNull(clWorkstationJarFileLocation,
            "KeyClWorkstationJarFileLocation should not be null");
        cu.setClWorkstationJarFileLocation(clWorkstationJarFileLocation);

        final String s = cu.runClw("get loadbalancer lastmodified timestamp").get(0);
        final String s1 = cu.runClw("remove agent \".*\\|.*\\|EPAgent1\"").get(0);
        Assert.assertEquals(s1.trim(), "Command executed successfully.");

        // rebalance runs every 2 mins
        log.info("Sleeping for 3 minutes for loadbalancing.xml to get updated...");
        Thread.sleep(3 * 60 * 1000);

        // lb.xml should be changed/modified
        final String s3 = cu.runClw("get loadbalancer lastmodified timestamp").get(0);
        Assert.assertNotEquals(s, s3);

        final String xml1 =
            FileUtils.readFileToString(new File(config.getConfigDirPath() + "loadbalancing.xml"));
        Assert.assertFalse(xml1.contains("<agent-collector name=\"ACC1\">"),
            "loadbalancing.xml should not contain group ACC1 at this point");
    }

    /**
     * Verifies that Host Name in loadbalancing.xmls agent-specifier regex is case insensitive.
     * ALM test#430033
     * 
     * <h5>PRECONDITIONS</h5>
     * <p>
     * <ul>
     * <li>Provisioned Testbed with One MOM + 2 Collectors with default configuration</li>
     * </ul>
     * </p>
     * 
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * <li>Start MOM and C2</li>
     * <li>Start Tomcat Agents.</li>
     * <li>Start C1</li>
     * <li>Verify that both Agents are connected to C2</li>
     * <li>Modify loadbalancing.xml so that Agents get redirected to C1. Use different case for
     * hostnames in the Agent masks.</li>
     * <li>Wait for rebalancing to occur</li>
     * <li>Run CLW to verify that both Agents are connected to C1</li>
     * </ol>
     * </p>
     * 
     * <h5>EXPECTED RESULTS</h5>
     * <p>
     * Agents get redirected to C1
     * <p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * <ul>
     * <li>Agent Controllability</li>
     * </ul>
     * </p>
     * 
     * @throws Exception
     */
    @Tas(testBeds = {@TestBed(name = OneMomTwoCollectorsLinuxTestbed.class, executeOn = OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "turyu01")
    @Test(groups = {"BAT"})
    public void testHostnameCaseUnSensitifityInLoadbalancingXml() throws Exception {

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

        final String tomcatDir =
            (String) envProps.getMachineProperties()
                .get(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID)
                .get(OneMomTwoCollectorsAbstractTestbed.KeyTomcatInstallDir);

        // change rebalance interval to 2 minutes for quicker tests
        PropertiesUtility.updateProperty(config.getPropertiesPath(),
            "introscope.enterprisemanager.loadbalancing.interval", "120");

        // start MOM and C2
        {
            log.info("Starting MOM...");
            EmBatLocalUtils.startLocalEm(config);
            log.info("Starting Collector2...");
            FlowUtils.startEm(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_ROLE_ID);
        }

        Thread.sleep(30000);

        // start Agents
        log.info("Starting Tomcat on Collector1");
        FlowUtils.startTomcat(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
            tomcatDir);

        log.info("Starting Tomcat on Collector2");
        FlowUtils.startTomcat(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
            tomcatDir);

        Thread.sleep(30000);
        log.info("Starting Collector1");
        FlowUtils.startEm(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
            OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_ROLE_ID);

        // both agents should be connected to C2 now
        final String col1hostname =
            envProps.getMachinePropertyById(
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                MachineEnvironmentProperties.HOSTNAME);
        final String col2hostname =
            envProps.getMachinePropertyById(
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
                MachineEnvironmentProperties.HOSTNAME);

        final ClwUtils cu = new ClwUtils();
        final String clWorkstationJarFileLocation =
            (String) envProps.getMachineProperties()
                .get(OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)
                .get(OneMomTwoCollectorsAbstractTestbed.KeyClWorkstationJarFileLocation);
        Assert.assertNotNull(clWorkstationJarFileLocation,
            "KeyClWorkstationJarFileLocation should not be null");
        cu.setClWorkstationJarFileLocation(clWorkstationJarFileLocation);
        {
            log.info("Checking a number of Active agents");
            final int col1Agents = cu.getAgents(col1hostname, "Active");
            Assert.assertEquals(col1Agents, 0, "Both Agents should be connected to C2, not C1");
            log.info("None of the agents are connected to C1 - OK");

            final int col2Agents = cu.getAgents(col2hostname, "Active");

            Assert.assertEquals(col2Agents, 2, "Both Agents should be connected to C2, not C1");
            log.info("Both agents are connected to C2 - OK");
        }

        // add agent mapping to loadbalancing.xml
        final String loadbalancingXml =
            FileUtils.readFileToString(new File(config.getConfigDirPath() + "loadbalancing.xml"));
        if (loadbalancingXml.contains("<agent-collector name=\"Example 430033\">")) {
            log.info("loadbalancing.xml contains agent-collector group Example 1 already, no update necessary");
        } else {
            log.info("Modifying loadbalancing.xml : adding agent-collector groups Example 1 and Example 2");

            // change char case for C1
            String hostnameCollector1InDifferentCase = col1hostname.toUpperCase();
            if (hostnameCollector1InDifferentCase.equals(col1hostname)) {
                hostnameCollector1InDifferentCase = col1hostname.toLowerCase();
            }
            Assert.assertNotEquals(hostnameCollector1InDifferentCase, col1hostname);

            // change char case for C2
            String hostnameCollector2InDifferentCase = col2hostname.toUpperCase();
            if (hostnameCollector2InDifferentCase.equals(col2hostname)) {
                hostnameCollector2InDifferentCase = col2hostname.toLowerCase();
            }
            Assert.assertNotEquals(hostnameCollector2InDifferentCase, col2hostname);

            // modify loadbalancing.xml
            final String stringToReplace;
            if (loadbalancingXml.contains("</loadbalancing>")) {
                stringToReplace = "</loadbalancing>";
            } else {
                stringToReplace = "loadbalancing0.1.xsd\"/>";
            }

            // both agents should be redirected to C1
            String newString =
                "    <agent-collector name=\"Example 430033\">\n" + "       <agent-specifier>.*"
                    + hostnameCollector1InDifferentCase + ".*\\|.*\\|.*</agent-specifier>\n"
                    + "       <include>\n" + "           <collector host=\"" + col1hostname
                    + "\" port=\"5001\"/>\n" + "       </include>\n" + "   </agent-collector>\n"
                    + "   <agent-collector name=\"Example 2\">\n" + "       <agent-specifier>.*"
                    + hostnameCollector2InDifferentCase + ".*\\|.*\\|.*</agent-specifier>\n"
                    + "       <include>\n" + "           <collector host=\"" + col1hostname
                    + "\" port=\"5001\"/>\n" + "       </include>\n"
                    + "   </agent-collector>\n</loadbalancing>";
            if (!loadbalancingXml.contains("</loadbalancing>")) {
                newString = "loadbalancing0.1.xsd\">\n" + newString;
            }

            String newXml = loadbalancingXml.replace(stringToReplace, newString);
            FileUtils.writeStringToFile(new File(config.getConfigDirPath() + "loadbalancing.xml"),
                newXml);
        }

        // rebalance runs every 2 mins
        log.info("Waiting for rebalance...");
        EmBatLocalUtils.waitForKeywordInLog(config.getLogPath(),
            "[Manager.LoadBalancer] Redirected controllable agent", 3 * 60 * 1000L);

        Thread.sleep(60 * 1000);

        // both agents should be connected to C1 after the rebalance
        {
            log.info("Checking a number of Active agents");

            final int col1Agents = cu.getAgents(col1hostname, "Active");
            Assert.assertEquals(col1Agents, 2,
                "Both Agents should be connected to C1 after rebalance, not C2");
            log.info("Both agents are connected to C1 - OK");

            final int col2Agents = cu.getAgents(col2hostname, "Active");
            Assert.assertEquals(col2Agents, 0,
                "Both Agents should be connected to C1 after rebalance, not C2");
            log.info("None of the agents are connected to C2 - OK");
        }
    }

    /**
     * Verifies that Collector connects to the MOM after Hot Config.
     * ALM test#430036
     * 
     * <h5>PRECONDITIONS</h5>
     * <p>
     * <ul>
     * <li>Provisioned Testbed with One MOM + 2 Collectors with default configuration</li>
     * </ul>
     * </p>
     * 
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * <li>Start MOM, C1 and C2</li>
     * <li>Remove C1 and C2 from cluster by commenting out the lines in MOM config</li>
     * <li>Run CLW to verify that both Collectors are removed from cluster</li>
     * <li>Uncomment lines in MOM config to add C1 and C2 back to the cluster</li>
     * <li>Run CLW to verify that C1 and C2 were discovered and added to the cluster</li>
     * </ol>
     * </p>
     * 
     * <h5>EXPECTED RESULTS</h5>
     * <p>
     * Both Collectors are connected to MOM
     * <p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * <ul>
     * <li>Agent Controllability</li>
     * </ul>
     * </p>
     * 
     * @throws Exception
     */
    @Tas(testBeds = {@TestBed(name = OneMomTwoCollectorsLinuxTestbed.class, executeOn = OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "turyu01")
    @Test(groups = {"BAT"})
    public void testCollectorConnectsToMomAfterHotConfig() throws Exception {

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
        final String col1hostname =
            envProps.getMachinePropertyById(
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                MachineEnvironmentProperties.HOSTNAME);
        final String col2hostname =
            envProps.getMachinePropertyById(
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
                MachineEnvironmentProperties.HOSTNAME);

        // start MOM , C1 and C2
        {
            log.info("Starting MOM...");
            EmBatLocalUtils.startLocalEm(config);
            log.info("Starting Collector1...");
            FlowUtils.startEm(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_ROLE_ID);
            log.info("Starting Collector2...");
            FlowUtils.startEm(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_ROLE_ID);
        }

        // remove all collectors from the cluster
        final HashMap<String, String> props = new HashMap<String, String>();
        props.put("introscope.enterprisemanager.clustering.login.em1.host", col1hostname);
        props.put("introscope.enterprisemanager.clustering.login.em1.port", "5001");
        props.put("introscope.enterprisemanager.clustering.login.em1.publickey",
            "internal/server/EM.public");
        props.put("introscope.enterprisemanager.clustering.login.em2.host", col2hostname);
        props.put("introscope.enterprisemanager.clustering.login.em2.port", "5001");
        props.put("introscope.enterprisemanager.clustering.login.em2.publickey",
            "internal/server/EM.public");
        {
            log.info("Updating MOM configuration to remove all the collectors");
            PropertiesUtility.commentProperties(config.getPropertiesPath(), new ArrayList<String>(
                props.keySet()));
        }

        // wait for Hot Config
        EmBatLocalUtils.waitForKeywordInLog(config.getLogPath(), "Detected hot config change",
            2 * 60 * 1000L);
        Thread.sleep(15000);

        // we should have no collectors in cluster
        final ClwUtils cu = new ClwUtils();
        final String clWorkstationJarFileLocation =
            (String) envProps.getMachineProperties()
                .get(OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)
                .get(OneMomTwoCollectorsAbstractTestbed.KeyClWorkstationJarFileLocation);
        Assert.assertNotNull(clWorkstationJarFileLocation,
            "KeyClWorkstationJarFileLocation should not be null");
        cu.setClWorkstationJarFileLocation(clWorkstationJarFileLocation);

        // C1 and C2 are not in cluster
        {
            final List<String> nocluster = cu.runClw("get cluster configuration");
            for (String s : nocluster) {
                if (s.trim().equalsIgnoreCase(col1hostname + "@5001")) {
                    throw new RuntimeException(col1hostname + " should not be in cluster by now");
                }
            }
            for (String s : nocluster) {
                if (s.trim().equalsIgnoreCase(col2hostname + "@5001")) {
                    throw new RuntimeException(col2hostname + " should not be in cluster by now");
                }
            }
        }

        // add both collectors to the cluster
        {
            log.info("Updating MOM configuration to add both the collectors");
            PropertiesUtility.uncommentProperties(config.getPropertiesPath(),
                new ArrayList<String>(props.keySet()));
            PropertiesUtility.saveProperties(config.getPropertiesPath(), props, false);
        }

        // wait for Hot Config
        EmBatLocalUtils.waitForKeywordInLog(config.getLogPath(), "Detected hot config change",
            2 * 60 * 1000L);
        Thread.sleep(15000);

        // C1 and C2 are in the cluster
        {
            boolean found1 = false;
            final List<String> incluster = cu.runClw("get cluster configuration");
            for (String s : incluster) {
                if (s.trim().equalsIgnoreCase(col1hostname + "@5001")) {
                    found1 = true;
                    break;
                }
            }
            Assert.assertTrue(found1, col1hostname + " should be in the cluster by now");
            log.info(col1hostname + " is in cluster - OK");

            boolean found2 = false;
            for (String s : incluster) {
                if (s.trim().equalsIgnoreCase(col2hostname + "@5001")) {
                    found2 = true;
                    break;
                }
            }
            Assert.assertTrue(found2, col2hostname + " should be in the cluster by now");
            log.info(col2hostname + " is in cluster - OK");
        }
    }

    /**
     * Verifies Agent Affinity.
     * ALM test #440678
     * 
     * <h5>PRECONDITIONS</h5>
     * <p>
     * <ul>
     * <li>Provisioned Testbed with One MOM + 2 Collectors with default configuration</li>
     * </ul>
     * </p>
     * 
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * <li>Configure MOM's loadbalancing.xml and add rule for C1 (unlatched) and C2 (latched)</li>
     * <li>Start MOM, C1 and C2</li>
     * <li>Configure agents C1 and C2 to connect directly to collector C1</li>
     * <li>Start agents</li>
     * <li>Run CLW to verify that Agents C1 and C2 are connected to C2</li>
     * </ol>
     * </p>
     * 
     * <h5>EXPECTED RESULTS</h5>
     * <p>
     * Agents should now go and connect to collector C2 because in the load balancing file agents
     * are latched to collector C2
     * <p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * <ul>
     * <li>Agent Controllability</li>
     * </ul>
     * </p>
     * 
     * @throws Exception
     */
    @Tas(testBeds = {@TestBed(name = OneMomTwoCollectorsLinuxTestbed.class, executeOn = OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "turyu01")
    @Test(groups = {"BAT"})
    public void testAgentAffinity() throws Exception {

        // synchronize time
        {
            TimeUtility.synchronizeTimeWithCAServer();
            FlowUtils.synchronizeTime(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID);
            FlowUtils.synchronizeTime(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID);
        }

        // configure MOM lb.xml file and add the below entries
        // <agent-collector name="Example 9.4">
        // <agent-specifier>.*\|.*\|.*</agent-specifier>
        // <include>
        // <collector host="gnapr03-vm9324" port="c1-port" latched="false"/>
        // <collector host="gnapr03-vm9324" port="c2-port" latched="true"/>
        // </include>
        // </agent-collector>

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

        // add agent mapping to loadbalancing.xml
        final String loadbalancingXml =
            FileUtils.readFileToString(new File(config.getConfigDirPath() + "loadbalancing.xml"));
        if (loadbalancingXml.contains("<agent-collector name=\"Example 440678\">")) {
            log.info("loadbalancing.xml contains agent-collector group Example 440678 already, no update necessary");
        } else {
            log.info("Modifying loadbalancing.xml : adding agent-collector group Example 440678");

            final String stringToReplace;
            if (loadbalancingXml.contains("</loadbalancing>")) {
                stringToReplace = "</loadbalancing>";
            } else {
                stringToReplace = "loadbalancing0.1.xsd\"/>";
            }

            // C2 has latched="true"
            String newString =
                "    <agent-collector name=\"Example 440678\">\n"
                    + "       <agent-specifier>.*</agent-specifier>\n" + "       <include>\n"
                    + "           <collector host=\"" + col1fqdn
                    + "\" port=\"5001\" latched=\"false\"/>\n" + "           <collector host=\""
                    + col2fqdn + "\" port=\"5001\" latched=\"true\"/>\n" + "       </include>\n"
                    + "   </agent-collector>\n</loadbalancing>";
            if (!loadbalancingXml.contains("</loadbalancing>")) {
                newString = "loadbalancing0.1.xsd\">\n" + newString;
            }

            String newXml = loadbalancingXml.replace(stringToReplace, newString);
            FileUtils.writeStringToFile(new File(config.getConfigDirPath() + "loadbalancing.xml"),
                newXml);
        }

        // 3. Start MOM and all the collectors
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

        // Configure agents C1 and C2 to connect directly to collector C1
        final String tomcatDir =
            (String) envProps.getMachineProperties()
                .get(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID)
                .get(OneMomTwoCollectorsAbstractTestbed.KeyTomcatInstallDir);
        {
            final AgentLocalUtils agentUtil = new AgentLocalUtils(tomcatDir);

            FlowUtils.updateProperty(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                agentUtil.getProfilePath(),
                "introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT", col1fqdn);
            FlowUtils.updateProperty(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
                agentUtil.getProfilePath(),
                "introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT", col1fqdn);
        }

        // Start agents
        log.info("Starting Tomcat on Collector1");
        FlowUtils.startTomcat(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
            tomcatDir);

        log.info("Starting Tomcat on Collector2");
        FlowUtils.startTomcat(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
            tomcatDir);

        Thread.sleep(250000);
        Assert.assertFalse(EmBatLocalUtils.isKeywordInLog(config.getLogPath(), "skew"),
            "Some of the collectors probably have skewed time - Check !");

        // Agents should now go and connect to collector C2 because in the load balancing file
        // the agent is latched to collector C2
        {
            final ClwUtils cu = new ClwUtils();
            final String clWorkstationJarFileLocation =
                (String) envProps.getMachineProperties()
                    .get(OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)
                    .get(OneMomTwoCollectorsAbstractTestbed.KeyClWorkstationJarFileLocation);
            Assert.assertNotNull(clWorkstationJarFileLocation,
                "KeyClWorkstationJarFileLocation should not be null");
            cu.setClWorkstationJarFileLocation(clWorkstationJarFileLocation);

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

    /**
     * Verifies that Agent redirection is handled correctly in case of invalid port/hostname in
     * loadbalancing.xml
     * ALM test #450351
     * 
     * <h5>PRECONDITIONS</h5>
     * <p>
     * <ul>
     * <li>Provisioned Testbed with One MOM + 2 Collectors with default configuration</li>
     * </ul>
     * </p>
     * 
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * <li>Start MOM, C1 and C2</li>
     * <li>Configure MOM's loadbalancing.xml and add rule for invalid hostname</li>
     * <li>Start agents</li>
     * </ol>
     * </p>
     * 
     * <h5>EXPECTED RESULTS</h5>
     * <p>
     * <li>Agents should be connected to MOM as "Disallowed"</li>
     * <li>There should be no NPE reported in the MOM's log file</li>
     * <p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * <ul>
     * <li>Agent Controllability</li>
     * </ul>
     * </p>
     * 
     * @throws Exception
     */
    @Tas(testBeds = {@TestBed(name = OneMomTwoCollectorsLinuxTestbed.class, executeOn = OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "turyu01")
    @Test(groups = {"BAT"})
    public void testInvalidHostInLoadbalancingXml() throws Exception {

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

        // 5. Start MOM and all the Collectors
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

        // 6. Open MOM's lb.xml file and add the below entry,
        // <agent-collector name="Example 2">
        // <agent-specifier>.*Tomcat.*</agent-specifier>
        // <include>
        // <collector host="abcd" port="5003"/>
        // </include>
        // </agent-collector>
        final String loadbalancingXml =
            FileUtils.readFileToString(new File(config.getConfigDirPath() + "loadbalancing.xml"));
        if (loadbalancingXml.contains("<agent-collector name=\"Example 450351\">")) {
            log.info("loadbalancing.xml contains agent-collector group Example 450351 already, no update necessary");
        } else {
            log.info("Modifying loadbalancing.xml : adding agent-collector group Example 450351");

            final String stringToReplace;
            if (loadbalancingXml.contains("</loadbalancing>")) {
                stringToReplace = "</loadbalancing>";
            } else {
                stringToReplace = "loadbalancing0.1.xsd\"/>";
            }

            // pointing all agents to the invalid port
            String newString =
                "    <agent-collector name=\"Example 450351\">\n"
                    + "       <agent-specifier>.*\\|.*\\|.*</agent-specifier>\n"
                    + "       <include>\n"
                    + "           <collector host=\"badhostname\" port=\"5001\" latched=\"false\"/>\n"
                    + "       </include>\n" + "   </agent-collector>\n</loadbalancing>";
            if (!loadbalancingXml.contains("</loadbalancing>")) {
                newString = "loadbalancing0.1.xsd\">\n" + newString;
            }

            String newXml = loadbalancingXml.replace(stringToReplace, newString);
            FileUtils.writeStringToFile(new File(config.getConfigDirPath() + "loadbalancing.xml"),
                newXml);
        }

        Thread.sleep(250000);

        // Start the agents
        {
            final String tomcatDir =
                (String) envProps.getMachineProperties()
                    .get(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID)
                    .get(OneMomTwoCollectorsAbstractTestbed.KeyTomcatInstallDir);

            log.info("Starting Tomcat on Collector1");
            FlowUtils.startTomcat(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, tomcatDir);

            log.info("Starting Tomcat on Collector2");
            FlowUtils.startTomcat(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID, tomcatDir);
        }

        Thread.sleep(250000);

        // Expected behavior
        // 1. When incorrect collector host / port details are specified in the EM's lb.xml,the
        // agent should get connected in disallowed mode with the MOM.
        {
            final ClwUtils cu = new ClwUtils();
            final String clWorkstationJarFileLocation =
                (String) envProps.getMachineProperties()
                    .get(OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)
                    .get(OneMomTwoCollectorsAbstractTestbed.KeyClWorkstationJarFileLocation);
            Assert.assertNotNull(clWorkstationJarFileLocation,
                "KeyClWorkstationJarFileLocation should not be null");
            cu.setClWorkstationJarFileLocation(clWorkstationJarFileLocation);

            // none of the agents should be connected to C1
            final String col1hostname =
                envProps.getMachinePropertyById(
                    OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                    MachineEnvironmentProperties.HOSTNAME);

            final int col1Agents = cu.getAgents(col1hostname, "Active");
            Assert.assertEquals(col1Agents, 0,
                "None of the Agents should be connected to C1 after rebalance");
            log.info("None of the agents are connected to C1 - OK");

            // none of the agents should be connected to C2
            final String col2hostname =
                envProps.getMachinePropertyById(
                    OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
                    MachineEnvironmentProperties.HOSTNAME);

            final int col2Agents = cu.getAgents(col2hostname, "Active");
            Assert.assertEquals(col2Agents, 0,
                "None of the Agents should be connected to C2 after rebalance");
            log.info("None of the agents are connected to C2 - OK");

            // both agents should be "Disallowed"
            log.info("Checking a number of Disallowed agents");
            final int numOfDeniedAgents = cu.getAgents(null, "Disallowed");
            Assert.assertEquals(numOfDeniedAgents, 2, "Both agents should be Disallowed");
        }

        // 2. There should be no NPE reported on the MOM's log file
        Assert.assertFalse(
            EmBatLocalUtils.isKeywordInLog(config.getLogPath(), "NullPointerException"),
            "There must be no NullPointerException in " + config.getLogPath());
    }
}
