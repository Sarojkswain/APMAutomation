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

import com.ca.apm.systemtest.fld.plugin.em.DatabasePluginImpl;
import com.ca.apm.systemtest.fld.plugin.em.EmPlugin.InstallationParameters;
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
public class DatabasePluginTest {

    public DatabasePluginTest() throws IOException {
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
        //config.installDir = "C:\\sw\\database\\";
        config.installDir = "/home/database/";

        config.dbHost = "LOD1629.ca.com";
        config.dbUserPass = "1qaz!QAZ";
        config.dbAdminName = "postgresAdmin";
        config.dbAdminPass = "1qaz!QAZ";

        //config.platform = OperatingSystemFamily.Windows;
        config.platform = OperatingSystemFamily.Linux;
        //config.logs = "C:\\";
        config.logs = "/tmp/";
        
        DatabasePluginImpl wv = new DatabasePluginImpl();
        wv.install(config);
        //wv.uninstall(config);
    }

    public static void main(String[] args) throws Exception {
        DatabasePluginTest tpt = new DatabasePluginTest();
        tpt.deploy();
    }
}
