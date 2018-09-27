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

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.plugin.prelert.PrelertPlugin;
import com.ca.apm.systemtest.fld.plugin.prelert.PrelertPlugin.Configuration;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;
import com.ca.apm.systemtest.fld.testbed.DefaultFreeTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * SampleTest class.
 * <p>
 * Test description
 */
public class PrelertPluginTest {

//    private final EnvironmentPropertyContext envProp;

    public PrelertPluginTest() throws IOException {
//        envProp = new EnvironmentPropertyContextFactory().createFromSystemProperty();
    }

    @Tas(
        testBeds = @TestBed(
            name = DefaultFreeTestbed.class,
            executeOn = DefaultFreeTestbed.TEST_MACHINE_ID),
        owner = "filja01",
        size = SizeType.DEBUG,
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"fldPrelertPlugin"})
    public void deploy() throws IOException, InterruptedException {
        // Config file

        Configuration config = new Configuration();
        config.prelertInstallDir = "C:\\SW\\CAAnalysisServer";
        config.buildNumber = "000065";
        config.codeName = "99.99.sys-ISCP";
        config.buildId = "99.99.0.sys";
        config.trussServer = "truss.ca.com";

        config.learnonlytimeCfg = "600";
        config.platform = OperatingSystemFamily.Windows;

        try (AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(
            "fld-tas-test-context.xml")) {
            PrelertPlugin pre = ctx.getBean(PrelertPlugin.class);
//        PrelertPluginImpl pre = new PrelertPluginImpl(); 
            pre.install(config);

            assertTrue(pre.isServerRunning("http://localhost:8080/prelertApi/prelert.svc"));
            System.out.println("stop Prelert");
            pre.stop(config);
            assertFalse(pre.isServerRunning("http://localhost:8080/prelertApi/prelert.svc"));
            System.out.println("start Prelert");
            pre.start(config);

            pre.uninstall(config, null);
        }
    }

    public static void main(String[] args) throws Exception {
        PrelertPluginTest ppt = new PrelertPluginTest();
        ppt.deploy();
    }
}
