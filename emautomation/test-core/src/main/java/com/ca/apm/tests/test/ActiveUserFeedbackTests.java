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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.tests.testbed.OneMomTwoCollectorsAbstractTestbed;
import com.ca.apm.tests.testbed.OneMomTwoCollectorsLinuxTestbed;
import com.ca.apm.tests.utils.FlowUtils;
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
 * Active user's feedback tests
 * ALM # 262182
 *
 */
public class ActiveUserFeedbackTests {

    private static final Logger log = LoggerFactory.getLogger(ActiveUserFeedbackTests.class);
    private EnvironmentPropertyContext envProps;

    @BeforeTest
    public void setUp() throws Exception {
        envProps = new EnvironmentPropertyContextFactory().createFromSystemProperty();
    }

    /**
     * Verifies EM handling of <b>Agents</b> monitored in the testing
     * environment. ALM test#262182
     * 
     * <h5>PRECONDITIONS</h5>
     * <p>
     * <ul>
     * <li>Provisioned Testbed with One MOM + 2 Collectors + 2 Agents with default configuration</li>
     * </ul>
     * </p>
     * 
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * <li>Go to "IntroscopeEnterpriseManager.properties" in MOM config file and set the value
     * "introscope.apm.agentcontrol.agent.allowed=false"</li>
     * <li>Start one Agent. Observe that agent is denied to connect, i.e. reported as
     * <i>Disallowed</i></li>
     * <li>Edit the value "introscope.apm.agentcontrol.agent.allowed=true" again</li>
     * <li>Restart the Agent. Start the second Agent as well</li>
     * <li>Observe that previously denied agent should now get connected.</li>
     * <li>The count of Disallowed Agents should go to zero. Agents must connect to a collector and
     * become <i>Active</i></li>
     * </ol>
     * </p>
     * 
     * <h5>EXPECTING RESULTS</h5>
     * <p>
     * Agents are reported as <i>Active</i> or <i>Disallowed</i> depending on the config setting
     * <p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * <ul>
     * <li>hot config of "introscope.apm.agentcontrol.agent.allowed"</li>
     * <li>correct handling Agent connections by MOM depending on
     * "introscope.apm.agentcontrol.agent.allowed". loadbalancing.xml is not tested.</li>
     * </ul>
     * </p>
     * 
     * @throws Exception
     */

    @Tas(testBeds = @TestBed(name = OneMomTwoCollectorsLinuxTestbed.class, executeOn = OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID), size = SizeType.MEDIUM, owner = "turyu01")
    @Test(groups = {"BAT"})
    public void testActiveDeniedStatusChange() throws Exception {

        // synchronize time
        {
            TimeUtility.synchronizeTimeWithCAServer();
            FlowUtils.synchronizeTime(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID);
            FlowUtils.synchronizeTime(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID);
        }

        // Go to "IntroscopeEnterpriseManager.properties" in MOM config file and set the value
        // "introscope.apm.agentcontrol.agent.allowed=false"
        log.info("Updating introscope.apm.agentcontrol.agent.allowed to false");
        final String emInstallDir =
            (String) envProps.getMachineProperties()
                .get(OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)
                .get(OneMomTwoCollectorsAbstractTestbed.KeyEmInstallDir);
        final EmConfiguration config = new EmConfiguration(emInstallDir, 5001);

        // change rebalance interval to 2 minutes for quicker tests
        PropertiesUtility.updateProperty(config.getPropertiesPath(),
            "introscope.enterprisemanager.loadbalancing.interval", "120");

        PropertiesUtility.updateProperty(config.getPropertiesPath(),
            "introscope.apm.agentcontrol.agent.allowed", "false");

        // Start MOM and all the collectors
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

        // Start one Agent. Observe that agent is denied to connect, i.e. reported as Disallowed
        final String tomcatDir =
            (String) envProps.getMachineProperties()
                .get(OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID)
                .get(OneMomTwoCollectorsAbstractTestbed.KeyTomcatInstallDir);

        log.info("Starting Tomcat on Collector1");
        FlowUtils.startTomcat(envProps, OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
            tomcatDir);
        EmBatLocalUtils.waitForKeywordInLog(config.getLogPath(), "Allowed collectors list",
            3 * 60 * 1000L);

        final ClwUtils cu = new ClwUtils();
        final String clWorkstationJarFileLocation =
            (String) envProps.getMachineProperties()
                .get(OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID)
                .get(OneMomTwoCollectorsAbstractTestbed.KeyClWorkstationJarFileLocation);
        Assert.assertNotNull(clWorkstationJarFileLocation,
            "KeyClWorkstationJarFileLocation should not be null");
        cu.setClWorkstationJarFileLocation(clWorkstationJarFileLocation);

        {
            log.info("Checking a number of Disallowed agents");
            // wait for next harvest cycle
            Thread.sleep(30000);
            final String hostname =
                envProps.getMachinePropertyById(OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID,
                    MachineEnvironmentProperties.HOSTNAME);
            cu.setEmHost(hostname);
            final int numOfDeniedAgents = cu.getAgents(null, "Disallowed");
            Assert.assertEquals(numOfDeniedAgents, 1, "Agent connection should have been denied");
        }

        // Edit the value "introscope.apm.agentcontrol.agent.allowed=true" again
        {
            PropertiesUtility.updateProperty(config.getPropertiesPath(),
                "introscope.apm.agentcontrol.agent.allowed", "true");
            // wait for Hot Config
            EmBatLocalUtils.waitForKeywordInLog(config.getLogPath(), "Detected hot config change",
                2 * 60 * 1000L);
            Thread.sleep(30000);
        }

        // Restart the Agent. Start the second Agent as well
        {
            FlowUtils.stopTomcat(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, tomcatDir);
            Thread.sleep(125000);
            FlowUtils.startTomcat(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, tomcatDir);
            FlowUtils.startTomcat(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID, tomcatDir);
        }

        // Observe that previously denied agent should now get connected.
        // The count of Disallowed Agents should go to zero. Agents must connect to a collector and
        // become Active
        {
            Thread.sleep(125000);
            log.info("Checking a number of Active agents");
            final String col1 =
                envProps.getMachinePropertyById(
                    OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID,
                    MachineEnvironmentProperties.HOSTNAME);
            final int col1Agents = cu.getAgents(col1, "Active");

            final String col2 =
                envProps.getMachinePropertyById(
                    OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID,
                    MachineEnvironmentProperties.HOSTNAME);
            final int col2Agents = cu.getAgents(col2, "Active");

            Assert.assertEquals(col1Agents + col2Agents, 2, "Both Agents should be active by now");
            log.info("Both agents are Active - OK");

            log.info("Checking a number of Disallowed agents");
            final int numOfDeniedAgents = cu.getAgents(null, "Disallowed");
            Assert
                .assertEquals(numOfDeniedAgents, 0, "There should be no disallowed agents by now");
            log.info("There are no Disallowed agents - OK");
        }

        // Edit the value "introscope.apm.agentcontrol.agent.allowed=false" again
        {
            PropertiesUtility.updateProperty(config.getPropertiesPath(),
                "introscope.apm.agentcontrol.agent.allowed", "false");
            // wait for Hot Config
            EmBatLocalUtils.waitForKeywordInLog(config.getLogPath(), "Detected hot config change",
                2 * 60 * 1000L);
            Thread.sleep(30000);
        }

        // Restart the Agents
        {
            FlowUtils.stopTomcat(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, tomcatDir);
            FlowUtils.stopTomcat(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID, tomcatDir);
            Thread.sleep(125000);
            FlowUtils.startTomcat(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_1_MACHINE_ID, tomcatDir);
            FlowUtils.startTomcat(envProps,
                OneMomTwoCollectorsAbstractTestbed.COLLECTOR_2_MACHINE_ID, tomcatDir);
        }
        {
            log.info("Checking a number of Disallowed agents");
            // wait for next harvest cycle
            Thread.sleep(30000);
            final String hostname =
                envProps.getMachinePropertyById(OneMomTwoCollectorsAbstractTestbed.MOM_MACHINE_ID,
                    MachineEnvironmentProperties.HOSTNAME);
            cu.setEmHost(hostname);
            final int numOfDeniedAgents = cu.getAgents(null, "Disallowed");
            Assert.assertEquals(numOfDeniedAgents, 2, "Agent connection should have been denied");
        }
    }
}
