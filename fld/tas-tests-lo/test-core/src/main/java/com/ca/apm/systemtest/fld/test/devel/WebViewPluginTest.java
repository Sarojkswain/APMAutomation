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

import com.ca.apm.systemtest.fld.plugin.em.EmPlugin.InstallationParameters;
import com.ca.apm.systemtest.fld.plugin.em.WebViewPluginImpl;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;
import com.ca.apm.systemtest.fld.testbed.DefaultFreeTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

/**
 * SampleTest class.
 * <p/>
 * Test description
 */
public class WebViewPluginTest {

//    private final EnvironmentPropertyContext envProp;

    public WebViewPluginTest() throws IOException {
//        envProp = new EnvironmentPropertyContextFactory().createFromSystemProperty();
    }

    @Tas(
        testBeds = @TestBed(
            name = DefaultFreeTestbed.class, 
            executeOn = DefaultFreeTestbed.TEST_MACHINE_ID), 
        owner = "filja01", 
        size = SizeType.DEBUG, 
        exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"fld_agent"})
    public void deploy() throws IOException {
        // Config file
        
        InstallationParameters config = new InstallationParameters();
        //config.installDir = "C:\\sw\\webview\\";
        config.installDir = "/home/Introscope9.7.1.0/";
        config.osgiBuildId = "9.7.1.0";

        config.wvEmHost = "sqw64xeoserv30";
        config.wvEmPort = 5001;

        //config.platform = OperatingSystemFamily.Windows;
        config.platform = OperatingSystemFamily.Linux;
        //config.logs = "C:\\";
        config.logs = "/tmp/";
        
        WebViewPluginImpl wv = new WebViewPluginImpl();
        wv.install(config);
        System.out.println("stop WebView");
        wv.stop(config);

        System.out.println("start WebView");
        wv.start(config);
        //wv.uninstall(config);
    }

    public static void main(String[] args) throws Exception {
        WebViewPluginTest tpt = new WebViewPluginTest();
        tpt.deploy();
    }
}
