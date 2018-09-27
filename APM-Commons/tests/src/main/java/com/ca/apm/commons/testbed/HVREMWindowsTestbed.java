/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 * Author : KETSW01/ KETHIREDDY SWETHA
 * Date : 01/08/2016
 */

package com.ca.apm.commons.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import com.ca.apm.role.CommonHvrAgentRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Common Windows Test-bed class.
 *
 * Test-bed description.
 */
@TestBedDefinition
public class HVREMWindowsTestbed implements ITestbedFactory {

    public static final String EM_MACHINE_ID = "emMachine";
    public static final String EM_ROLE_ID = "emRole";
    private static final String EM_MACHINE_TEMPLATE_ID = TEMPLATE_W64;
    public static final String HVRAGENT_ROLE = "hvragent-role";

    private static final String HVRAGENT_INSTALL_DIRECTORY = "C:\\sw\\hvragent\\";
    private static final String HVRAGENT_STAGE_DIRECTORY   = "C:\\sw\\stage";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        //create EM role
        
        
        
        EmRole emRoleid = new EmRole.Builder(EM_ROLE_ID, tasResolver)
            .nostartEM()
            .nostartWV()
            .build();
        ITestbed testbed = new Testbed("SampleTestbed");

      CommonHvrAgentRole hvrAgentRole = CommonHvrAgentRole(tasResolver, HVRAGENT_ROLE);
       
        ITestbedMachine emMachine = TestBedUtils.createWindowsMachine(EM_MACHINE_ID, EM_MACHINE_TEMPLATE_ID);
        
        emMachine.addRole(emRoleid, hvrAgentRole);

        testbed.addMachine(emMachine);

        return testbed;
    }


private CommonHvrAgentRole CommonHvrAgentRole(ITasResolver tasResolver, String Role) {
    return new CommonHvrAgentRole.Builder(HVRAGENT_ROLE, tasResolver)
            .installDir(HVRAGENT_INSTALL_DIRECTORY)
            .stagingDir(HVRAGENT_STAGE_DIRECTORY)
            .build();
}

  
    
}

