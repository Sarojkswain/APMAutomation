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

import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.plugin.tomcat.TomcatPlugin.Configuration;
import com.ca.apm.systemtest.fld.plugin.tomcat.TomcatPluginImpl;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;
import com.ca.apm.systemtest.fld.testbed.EmTomcatPluginTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

/**
 * SampleTest class.
 * <p/>
 * Test description
 */
public class EmTomcatPluginTest {

//    private final EnvironmentPropertyContext envProp;

    public EmTomcatPluginTest() throws IOException {
//        envProp = new EnvironmentPropertyContextFactory().createFromSystemProperty();
    }

    @Tas(
        testBeds = @TestBed(
            name = EmTomcatPluginTestbed.class, 
            executeOn = EmTomcatPluginTestbed.TEST_MACHINE_ID), 
        owner = "filja01", 
        size = SizeType.DEBUG, 
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"fld_agent"})
    public void deploy() throws IOException {
        // Config file
        Configuration config = new Configuration();
        config.buildNumber = "000046";
        config.codeName = "99.99.sys-ISCP";
        config.buildId = "99.99.0.sys";
        config.agentExecute = "/IntroscopeAgent99.99.0.syswindows.exe";
        config.platform = SystemUtil.OperatingSystemFamily.Windows;

        config.tomcatInstallDir = "c:\\sw\\wily\\tomcat";

        config.agentInstallDir = "c:/sw/testagent";
        config.tomcatServerName = "tomcat";
        config.emHost = "lod0389.ca.com";
        config.logs = "c:/sw/testagent";

        //
        TomcatPluginImpl plugin = new TomcatPluginImpl();

        plugin.installTomcat(config);

        plugin.installAgent(config);

        // Configure tomcat to run with the Agent
        plugin.setAgent(config);
        // Start / Stop tomcat
        plugin.startServer(config);
        assertTrue(plugin.isServerRunning("http://localhost:8080", 300000));

        if (plugin.isServerRunning("http://localhost:8080", 300000)) {
            plugin.stopServer(config);
        }

        assertTrue(plugin.isServerStopped("http://localhost:8080", 300000));
    }

    public static void main(String[] args) throws Exception {
        EmTomcatPluginTest tpt = new EmTomcatPluginTest();
        tpt.deploy();
    }
}
