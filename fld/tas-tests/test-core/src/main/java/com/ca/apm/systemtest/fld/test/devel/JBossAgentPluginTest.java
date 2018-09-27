/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and proprietary and shall not
 * be duplicated, used, disclosed or disseminated in any way except as authorized by the applicable
 * license agreement, without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT PERMITTED BY APPLICABLE
 * LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE
 * USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.systemtest.fld.test.devel;

import java.io.IOException;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.testbed.JBossAgentPluginTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

/**
 * SampleTest class.
 * <p/>
 * Test description
 */
public class JBossAgentPluginTest {

//    private final EnvironmentPropertyContext envProp;

    public JBossAgentPluginTest() throws IOException {
//        envProp = new EnvironmentPropertyContextFactory().createFromSystemProperty();
    }

    @Tas(
        testBeds = @TestBed(
            name = JBossAgentPluginTestbed.class, 
            executeOn = JBossAgentPluginTestbed.TEST_MACHINE_ID), 
        owner = "filja01", 
        size = SizeType.DEBUG, 
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"fld_agent"})
    public void deploy() throws IOException {
        // Config file
        /*
        InstallationParameters config = new InstallationParameters();
        config.buildNumber = "000031";
        config.codeName = "99.99.sys-ISCP";
        config.buildId = "99.99.0.sys";
        config.agentExecute = "/IntroscopeAgent99.99.0.syswindows.exe";
        config.platform = SystemUtil.OperatingSystemFamily.Windows;

        config.jbossInstallDir = envProp
            .getRolePropertiesById(JBossAgentPluginTestbed.JBOSS_ROLE_ID)
            .getProperty("home");

        config.envJava = "C:/Program Files (x86)/Java/jdk1.7.0_25"; 
        
        config.agentInstallDir = "c:/sw/testagent";
        config.jbossServerName = "jboss";
        config.emHost = "lod0389.ca.com";
        config.logs = "c:/sw/testagent";

        //
        JBossPluginImpl plugin = new JBossPluginImpl();

        plugin.installAgent(config);

        // Configure jboss to run with the Agent
        plugin.setAgent(config);
        // Start / Stop jboss
        plugin.startServer(config);
        assertTrue(plugin.isServerRunning("http://localhost:8080", 300000));

        plugin.stopServer(config);

        assertTrue(plugin.isServerStopped("http://localhost:8080", 300000));
        */
    }

    public static void main(String[] args) throws Exception {
        JBossAgentPluginTest tpt = new JBossAgentPluginTest();
        tpt.deploy();
    }
}
