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

package com.ca.apm.systemtest.fld.testbed;

import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;
import com.ca.apm.systemtest.fld.role.AgentRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.linux.YumInstallPackageRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * FLD Agent testbed for Linux machine - set-up agent for remote Load Orchestrator.
 * @author filja01
 *
 */
@TestBedDefinition
public class FLDAgentLinuxTestbed implements ITestbedFactory {

    public static final String TEST_MACHINE_ID = "testMachine";
    public static final String AGENT_ROLE_ID = "agentRole";
    public static final String YUM_INSTALL_ROLE_ID = "yumRole";
    public static final String DRIVER_ROLE_ID = "driverRole";
    public static final String CHROME_ROLE_ID = "chromeRole";
    public static final String REMOTE_FLDCONTROLLER = "tas-cz-n1e";
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbedMachine testMachine =
            new TestbedMachine.Builder(TEST_MACHINE_ID).templateId("co66").build();
        
        YumInstallPackageRole yumRole = new YumInstallPackageRole.Builder(YUM_INSTALL_ROLE_ID)
            .addPackage("compat-libstdc++-33")
            .addPackage("pexpect")
            .addPackage("httpd")
            .addPackage("httpd-tools")
            .addPackage("mod_wsgi")
            .addPackage("mod_ssl")
            .addPackage("policycoreutils-python")
            .addPackage("gdb")
            .addPackage("lsof")
            .addPackage("pciutils")
            .addPackage("zip")
            .addPackage("unzip")
            .addPackage("libpcap")
            .addPackage("java-1.7.0-openjdk")
            .build();
        testMachine.addRole(yumRole);
        
        IRole agentRole = new AgentRole(AGENT_ROLE_ID, "/opt/CA/lo-agent", 
            "tcp://"+REMOTE_FLDCONTROLLER+":61616", REMOTE_FLDCONTROLLER+":8080", OperatingSystemFamily.Linux);
        testMachine.addRole(agentRole);
        
        ITestbed testbed = new Testbed("FLDAgentLinuxTestbed");
        testbed.addMachine(testMachine);

        return testbed;
    }
}
