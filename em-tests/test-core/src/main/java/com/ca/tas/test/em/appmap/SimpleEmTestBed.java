/*
 * Copyright (c) 2015 CA.  All rights reserved.
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
 */

package com.ca.tas.test.em.appmap;

import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

@TestBedDefinition
public class SimpleEmTestBed implements ITestbedFactory {
    
    static public String MACHINE_ID = "endUserMachine";
    static public String EM_ROLE_ID = "introscope";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("Introscope/AppMap/SimpleEm");

        ITestbedMachine emMachine =
            new TestbedMachine.Builder(MACHINE_ID).platform(Platform.WINDOWS)
                .templateId("w64").bitness(Bitness.b64).build();
        EmRole.Builder emBuilder = new EmRole.Builder(EM_ROLE_ID, tasResolver).dbpassword("quality")
                .emLaxNlJavaOption(RoleUtility.getDevEmLaxnlJavaOption(9001)).nostartEM()
                .nostartWV();
        
        configureBuilder(emBuilder);
        
        EmRole emRole = emBuilder.build();
        
        emMachine.addRole(emRole);
        RoleUtility.addMmRole(emMachine, emRole.getRoleId() + "_mm", emRole, "NowhereBankMM");
        IRole startReqRole = RoleUtility.addNowhereBankRole(emMachine, emRole, null, tasResolver);
        IRole startEmRole = RoleUtility.addStartEmRole(emMachine, emRole, true, emRole);
        startReqRole.after(startEmRole);
        
        testbed.addMachine(emMachine);

        return testbed;
    }
    
    protected void configureBuilder(EmRole.Builder builder) {
        
    }
}
