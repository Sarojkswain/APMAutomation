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

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.tests.testbed.OneEmOneAgentAbstractTestbed;
import com.ca.apm.tests.testbed.OneEmOneAgentLinuxTestbed;
import com.ca.apm.tests.utils.agents.AgentLocalUtils;
import com.ca.apm.tests.utils.agents.TomcatUtils;
import com.ca.apm.tests.utils.clw.ClwUtils;
import com.ca.apm.tests.utils.configutils.PropertiesUtility;
import com.ca.apm.tests.utils.emutils.EmBatLocalUtils;
import com.ca.apm.tests.utils.emutils.EmConfiguration;
import com.ca.tas.client.AutomationAgentClientFactory;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.envproperty.EnvironmentPropertyException;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class SslTests {

    private EnvironmentPropertyContext envProps;
    private static final Logger log = LoggerFactory.getLogger(SslTests.class);


    @BeforeTest
    public void setUp() throws EnvironmentPropertyException, IOException {
        envProps = new EnvironmentPropertyContextFactory().createFromSystemProperty();
        new AutomationAgentClientFactory(envProps).create();
    }



    /**
     * EM BAT tests # 430041 <br>
     * Author : Artur Sobieski
     * 
     * <h5>PRE-REQUISITES:</h5> <br>
     * <ol>
     * <li>Machine with EM and One agent</li>
     * </ol>
     *
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * 
     * <li>Configure Agent and EM to connect using 5001 port (default Isengard).</li>
     * <li>After the connection has happened, Login to WS and check for agent's EMPort metric, it
     * should show 5001.</li>
     * <li>Now Stop Agent</li>
     * <li>Reconfigure agent to</li>
     * connect to EM in HTTP mode(port 8081)
     * <li>No changes are to be made on the EM side for connection in HTTP mode.</li>
     * <li>Now Start Agent.</li>
     * <li>Check for Agent's EMPort metric it should reflect the current connection mode 8081.</li>
     * <li>Set UP agent and collector HTTPS</li>
     * <li>Restart EM</li>
     * <li>
     * Reconfigure agent to connect to EM in SSL mode(port 5443)<br>
     * <li>
     * After the connection has happened, check for agent's EMPort metric, it should show 5443.</li>
     * <li>Reconfigure agent to connect to EM in HTTPS mode(port 8444) <br>
     * <li>After the connection has happened, Check for agent's EMPort metric, it should show 8444.</li>
     * </ol>
     * </p>
     *
     * <h5>EXPECTED RESULTS</h5>
     * <p>
     * <ol>
     * <li>Expected behavior:The agent's EMPort metric should be updated with the port number for
     * current connection type (SSL, HTTP or HTTPS or Isengard) set on the agent side, without EM
     * restart.</li>
     * <li>Defect: The agent's EMPort metric is not updated with the current port set on the agent
     * side, unless and until the EM is restarted.</li>
     * </ol>
     * </p>
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * <ul>
     * <li>Possibility of port number not being updated without restart</li>
     * </ul>
     * </p>
     */
    @Tas(testBeds = {@TestBed(name = OneEmOneAgentLinuxTestbed.class, executeOn = OneEmOneAgentAbstractTestbed.MACHINE_ID)}, size = SizeType.MEDIUM, owner = "Artur")
    @Test(groups = {"BAT", "SSL"})
    public void testAgentEMMetric() throws Exception {

        final ClwUtils cu = new ClwUtils();

        final String clWorkstationJarFileLocation =
            (String) envProps.getMachineProperties().get(OneEmOneAgentAbstractTestbed.MACHINE_ID)
                .get(OneEmOneAgentAbstractTestbed.KeyClWorkstationJarFileLocation);

        final String emInstallDir =
            (String) envProps.getMachineProperties().get(OneEmOneAgentAbstractTestbed.MACHINE_ID)
                .get(OneEmOneAgentAbstractTestbed.KeyEmInstallDir);

        final String tomcatInstallDir =
            (String) envProps.getMachineProperties().get(OneEmOneAgentAbstractTestbed.MACHINE_ID)
                .get(OneEmOneAgentAbstractTestbed.TomcatInstallDir);


        cu.setClWorkstationJarFileLocation(clWorkstationJarFileLocation);

        AgentLocalUtils agentUtil = new AgentLocalUtils(tomcatInstallDir);
        EmConfiguration emConfig = new EmConfiguration(emInstallDir, 5001);

        // Default config Check

        TomcatUtils.startTomcat(tomcatInstallDir);

        Thread.sleep(15000);

        assertEquals("5001", cu.getMetricFromAgent(".*|Tomcat|Tomcat Agent", "EM Port"));

        log.info("DEFAULT PORT 5001 Checked Fine");


        TomcatUtils.stopTomcat(tomcatInstallDir);

        // HTTP config check

        HashMap<String, String> httpProps = new HashMap<String, String>();
        httpProps.put("introscope.agent.enterprisemanager.transport.tcp.socketfactory.DEFAULT",
            "com.wily.isengard.postofficehub.link.net.HttpTunnelingSocketFactory");
        httpProps.put("introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT", "8081");

        log.info("Setting http properties and switching port to default http : 8081");
        PropertiesUtility.saveProperties(agentUtil.getProfilePath(), httpProps, true);

        TomcatUtils.startTomcat(tomcatInstallDir);

        Thread.sleep(15000);

        assertEquals("8081", cu.getMetricFromAgent(".*|Tomcat|Tomcat Agent", "EM Port"));

        log.info("DEFAULT HTTP PORT 8081 Checked Fine");

        TomcatUtils.stopTomcat(tomcatInstallDir);

        // SSL config check
        EmBatLocalUtils.setUpHttpsProperties(emConfig.getPropertiesPath());

        EmBatLocalUtils.stopLocalEm(emConfig);

        EmBatLocalUtils.startLocalEm(emConfig);

        HashMap<String, String> sslProps = new HashMap<String, String>();
        sslProps.put("introscope.agent.enterprisemanager.transport.tcp.socketfactory.DEFAULT",
            "com.wily.isengard.postofficehub.link.net.SSLSocketFactory");
        sslProps.put("introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT", "5443");

        log.info("Setting SSL properties and switching port to default SSL : 5443");

        PropertiesUtility.saveProperties(agentUtil.getProfilePath(), sslProps, true);

        TomcatUtils.startTomcat(tomcatInstallDir);

        Thread.sleep(60 * 1000);

        assertEquals("5443", cu.getMetricFromAgent(".*|Tomcat|Tomcat Agent", "EM Port"));

        log.info("DEFAULT SSL PORT 5443 Checked Fine");


        log.info("Setting HTTPS properties and switching port to default HTTPS : 8444");

        // Now HTTPS config check

        agentUtil.setUpHttpsProperties();

        TomcatUtils.stopTomcat(tomcatInstallDir);

        TomcatUtils.startTomcat(tomcatInstallDir);

        Thread.sleep(30 * 1000);


        assertEquals("8444", cu.getMetricFromAgent(".*|Tomcat|Tomcat Agent", "EM Port"));

        log.info("DEFAULT SSL PORT 8444 Checked Fine");

    }
}
