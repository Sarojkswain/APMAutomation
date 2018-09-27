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
import com.ca.apm.systemtest.fld.role.ChromeBrowserRole;
import com.ca.apm.systemtest.fld.role.ChromeDriverRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * FLD Agent testbed for Windows machine - set-up agent for remote Load Orchestrator.
 * @author filja01
 *
 */
@TestBedDefinition
public class FLDAgentWinTestbed implements ITestbedFactory {

    public static final String TEST_MACHINE_ID = "testMachine";
    public static final String AGENT_ROLE_ID = "agentRole";
    public static final String DRIVER_ROLE_ID = "driverRole";
    public static final String CHROME_ROLE_ID = "chromeRole";
    public static final String REMOTE_FLDCONTROLLER = "tas-cz-n1e";
    public static final boolean CHROME = false;
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbedMachine testMachine =
            new TestbedMachine.Builder(TEST_MACHINE_ID).templateId("w64").build();
        
        IRole agentRole = new AgentRole(AGENT_ROLE_ID, "C:\\sw\\agent", 
                          "tcp://"+REMOTE_FLDCONTROLLER+":61616", REMOTE_FLDCONTROLLER+":8080", OperatingSystemFamily.Windows);
        testMachine.addRole(agentRole);
        
        if (CHROME) {
            IRole driverRole = new ChromeDriverRole(DRIVER_ROLE_ID, "C:\\Install\\selenium\\chromedriver.exe");
            testMachine.addRole(driverRole);
            
            IRole chromeRole = new ChromeBrowserRole(CHROME_ROLE_ID, "C:\\sw\\chromeinstaller.exe");
            testMachine.addRole(chromeRole);
        }
        
        ITestbed testbed = new Testbed("FLDAgentWinTestbed");
        testbed.addMachine(testMachine);

        return testbed;
    }
}
