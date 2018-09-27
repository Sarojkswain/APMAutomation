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
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.tests.flow.ChangeSystemTimeFlow;
import com.ca.apm.tests.flow.ChangeSystemTimeFlowContext;
import com.ca.apm.tests.flow.SynchronizeTimeFlow;
import com.ca.apm.tests.flow.SynchronizeTimeFlowContext;
import com.ca.apm.tests.testbed.OneCdvOneMomTwoCollectorsAbstractTestbed;
import com.ca.apm.tests.testbed.OneCdvOneMomTwoCollectorsLinuxTestbed;
import com.ca.apm.tests.testbed.OneMomOneCollectorAbstractTestbed;
import com.ca.apm.tests.testbed.OneMomOneCollectorLinuxTestbed;
import com.ca.apm.tests.testbed.OneMomOneCollectorWindowsTestbed;
import com.ca.apm.tests.testbed.OneMomTwoCollectorsAbstractTestbed;
import com.ca.apm.tests.utils.FlowUtils;
import com.ca.apm.tests.utils.agents.AgentLocalUtils;
import com.ca.apm.tests.utils.clw.ClwUtils;
import com.ca.apm.tests.utils.configutils.TimeUtility;
import com.ca.apm.tests.utils.emutils.EmBatLocalUtils;
import com.ca.apm.tests.utils.emutils.EmConfiguration;
import com.ca.tas.client.AutomationAgentClientFactory;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.envproperty.EnvironmentPropertyException;
import com.ca.tas.envproperty.MachineEnvironmentProperties;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;


public class CcdvTests {

    public static int DEFAULT_EM_PORT = 5001;

    private static final long TIME_DIFFERENCE = 60 * 1000;

    private EnvironmentPropertyContext envProps;
    private IAutomationAgentClient aaClient;
    private static final Logger log = LoggerFactory.getLogger(CcdvTests.class);

    @BeforeTest
    public void setUp() throws EnvironmentPropertyException, IOException {
        envProps = new EnvironmentPropertyContextFactory().createFromSystemProperty();
        aaClient = new AutomationAgentClientFactory(envProps).create();
    }

    /**
     * @author sobar03 <br>
     *         Test #276542<br>
     *         <h5>PRECONDITIONS</h5>
     *         <p>
     *         <ul>
     *         <li>Provisioned Testbed with One MOM + One Collector configuration, with clock skewed
     *         on one of them.</li>
     *         </ul>
     *         </p>
     *         <h5>TEST ACTIVITY</h5>
     *         <p>
     *         <ol>
     *         <li>Make sure the collector info is added in the MOM.</li>
     *         <li>Start MOM and Collector.</li>
     *         </ol>
     *         </p>
     *         <h5>EXPECTED RESULTS</h5>
     *         <p>
     *         <ol>
     *         <li>Collector should not get connect to MOM if it has time difference more that 3 sec
     *         </li>
     *         </ol>
     *         </p>
     *         <h5>RISKS MITIGATED</h5>
     *         <p>
     *         <ul>
     *         <li>Possibility of connection happening despite clock being skewed.</li>
     *         </ul>
     *         </p>
     */
    @Tas(testBeds = {
            @TestBed(name = OneMomOneCollectorWindowsTestbed.class, executeOn = OneMomOneCollectorAbstractTestbed.MOM_MACHINE_ID),
            @TestBed(name = OneMomOneCollectorLinuxTestbed.class, executeOn = OneMomOneCollectorAbstractTestbed.MOM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "sobar03")
    @Test(groups = {"MOM", "Time", "BAT"})
    public void timeDifferenceTest() throws Exception {

        final String emInstallDir =
            (String) envProps.getMachineProperties()
                .get(OneMomOneCollectorAbstractTestbed.COLLECTOR_MACHINE_ID)
                .get(OneMomOneCollectorAbstractTestbed.KeyEmInstallDir);

        EmConfiguration localEMConfig = new EmConfiguration(emInstallDir, DEFAULT_EM_PORT);

        final String collectorHostnameWithPort =
            envProps.getMachinePropertyById(OneMomOneCollectorAbstractTestbed.COLLECTOR_MACHINE_ID,
                MachineEnvironmentProperties.HOSTNAME_WITH_PORT);

        TimeUtility.synchronizeTimeWithCAServer();


        ChangeSystemTimeFlowContext timeFlowContext =
            new ChangeSystemTimeFlowContext(System.currentTimeMillis() - TIME_DIFFERENCE);

        aaClient.runJavaFlow(new FlowConfigBuilder(ChangeSystemTimeFlow.class, timeFlowContext,
            collectorHostnameWithPort));


        EmBatLocalUtils.startLocalEm(localEMConfig);

        FlowUtils.startEm(envProps, OneMomOneCollectorAbstractTestbed.COLLECTOR_MACHINE_ID,
            OneMomOneCollectorAbstractTestbed.COLLECTOR_ROLE_ID);

        Thread.sleep(120 * 1000);

        assertTrue(EmBatLocalUtils.isKeywordInLog(localEMConfig.getLogPath(),
            "ClockSyncException: Collector clock is skewed from MOM"));

        aaClient.runJavaFlow(new FlowConfigBuilder(SynchronizeTimeFlow.class,
            new SynchronizeTimeFlowContext(), collectorHostnameWithPort));

    }

    /**
     * Tests CDV ability to collect metrics from different collectors and dynamically react for
     * mounting and unmouting of agents.
     * 
     * EM BAT tests #275494<br>
     * Author : Martin Batelka
     * 
     * @author batma08
     * 
     *         <h5>PRECONDITIONS</h5>
     *         <p>
     *         <ul>
     *         <li>Provisioned Testbed with One CDV + One MOM + 2 Collectors + 2 Agents with default
     *         configuration</li>
     *         </ul>
     *         </p>
     * 
     *         <h5>TEST ACTIVITY</h5>
     *         <p>
     *         <ol>
     *         <li>Start all EMs</li>
     *         <li>Start an agent on each of collectors</li>
     *         <li>Verify that agents are connected</li>
     *         <li>Stop the agent on one collector and repoint it to other</li>
     *         <li>Start agent again and verify that this agent is pointing to the other collector</li>
     *         </ol>
     *         </p>
     * 
     *         <h5>EXPECTED RESULTS</h5>
     *         <p>
     *         <ol>
     *         <li>CDV can see a metrics from both collectors. After repointing agent from one
     *         collector to another collector's metrics should have still the same "host" value but
     *         different EM host value.</li>
     *         </ol>
     *         </p>
     * 
     *         <h5>RISKS MITIGATED</h5>
     *         <p>
     *         <ul>
     *         <li>CDV hot config doesn't work or its observation logic is broken.</li>
     *         </ul>
     *         </p>
     */

    @Tas(testBeds = {@TestBed(name = OneCdvOneMomTwoCollectorsLinuxTestbed.class, executeOn = OneCdvOneMomTwoCollectorsAbstractTestbed.CDV_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "batma08")
    @Test(groups = {"CDV", "Agent", "BAT"})
    public void agentReconnectionTest() throws Exception {
        final String cdvEmInstallDir =
            (String) envProps.getMachineProperties()
                .get(OneCdvOneMomTwoCollectorsAbstractTestbed.CDV_MACHINE_ID)
                .get(OneCdvOneMomTwoCollectorsAbstractTestbed.KeyEmInstallDir);

        final String cdvHostname =
            envProps.getMachinePropertyById(
                OneCdvOneMomTwoCollectorsAbstractTestbed.CDV_MACHINE_ID,
                MachineEnvironmentProperties.HOSTNAME);


        final String c1tomcatInstallDir =
            (String) envProps.getMachineProperties()
                .get(OneCdvOneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID)
                .get(OneCdvOneMomTwoCollectorsAbstractTestbed.KeyTomcatInstallDir);
        final String c1Hostname =
            envProps.getMachinePropertyById(
                OneCdvOneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                MachineEnvironmentProperties.HOSTNAME);

        final String c2tomcatInstallDir =
            (String) envProps.getMachineProperties()
                .get(OneCdvOneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID)
                .get(OneCdvOneMomTwoCollectorsAbstractTestbed.KeyTomcatInstallDir);
        final String c2Hostname =
            envProps.getMachinePropertyById(
                OneCdvOneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
                MachineEnvironmentProperties.HOSTNAME);

        final EmConfiguration cdvEMConfig = new EmConfiguration(cdvEmInstallDir, DEFAULT_EM_PORT);

        final String c1tomcatDir =
            (String) envProps.getMachineProperties()
                .get(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID)
                .get(OneMomTwoCollectorsAbstractTestbed.KeyTomcatInstallDir);
        final AgentLocalUtils c1agentUtil = new AgentLocalUtils(c1tomcatDir);

        final String c2tomcatDir =
            (String) envProps.getMachineProperties()
                .get(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID)
                .get(OneMomTwoCollectorsAbstractTestbed.KeyTomcatInstallDir);
        final AgentLocalUtils c2agentUtil = new AgentLocalUtils(c2tomcatDir);

        // sets agents to observe EM on its own host. Default is sets to MOM
        {
            FlowUtils.updateProperty(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                c1agentUtil.getProfilePath(),
                OneCdvOneMomTwoCollectorsAbstractTestbed.KeyAgentHost, c1Hostname);
            FlowUtils.updateProperty(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
                c2agentUtil.getProfilePath(),
                OneCdvOneMomTwoCollectorsAbstractTestbed.KeyAgentHost, c2Hostname);
        }


        // Start MOM, all the collectors and CDV
        {
            log.info("Starting MOM");
            FlowUtils.startEm(envProps, OneCdvOneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID,
                OneCdvOneMomTwoCollectorsAbstractTestbed.MOM_ROLE_ID);
            log.info("Starting Collector1");
            FlowUtils.startEm(envProps,
                OneCdvOneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                OneCdvOneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_ROLE_ID);
            log.info("Starting Collector2");
            FlowUtils.startEm(envProps,
                OneCdvOneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
                OneCdvOneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_ROLE_ID);
            log.info("Starting CDV");
            EmBatLocalUtils.startLocalEm(cdvEMConfig);

        }


        // Start Agents on all collectors
        {
            log.info("Starting Tomcat on Collector1");
            FlowUtils
                .startTomcat(envProps,
                    OneCdvOneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                    c1tomcatInstallDir);

            log.info("Starting Tomcat on Collector2");
            FlowUtils
                .startTomcat(envProps,
                    OneCdvOneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
                    c2tomcatInstallDir);
        }
        Thread.sleep(5 * 1000);
        final ClwUtils cu = new ClwUtils();
        cu.setClWorkstationJarFileLocation(cdvEMConfig.getClwPath());
        String[] collectors =
            {c1Hostname + "@" + DEFAULT_EM_PORT, c2Hostname + "@" + DEFAULT_EM_PORT};
        cu.checkClusterConfiguration(cdvHostname, collectors);
        assertEquals(cu.getCollectorHostnameForAgent(".*Tomcat.*", c1Hostname), c1Hostname);
        assertEquals(cu.getCollectorHostnameForAgent(".*Tomcat.*", c2Hostname), c2Hostname);

        // Stopping agent on Collector1 and repointing it to collector 2
        {
            log.info("Stoping Tomcat on Collector1");
            FlowUtils
                .stopTomcat(envProps,
                    OneCdvOneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                    c1tomcatInstallDir);
            Thread.sleep(30 * 1000);
            assertEquals(cu.getAgents(c1Hostname, "Active"), 0,
                "There are some active agents but shouldn't be.");
            log.info("Starting Tomcat on Collector1 pointing to Collector2");
            FlowUtils.updateProperty(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                c1agentUtil.getProfilePath(),
                OneCdvOneMomTwoCollectorsAbstractTestbed.KeyAgentHost, c2Hostname);
            FlowUtils
                .startTomcat(envProps,
                    OneCdvOneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                    c1tomcatInstallDir);
        }
        Thread.sleep(30 * 1000);
        assertEquals(cu.getCollectorHostnameForAgent(".*Tomcat.*", c1Hostname), c2Hostname);
    }
}
