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

package com.ca.tas.test.em.agc;

import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

/**
 * Represents testbed for performance testing of the AGC.
 * 
 * @author Korcak, Zdenek <korzd01@ca.com>
 * 
 */
@TestBedDefinition
public class PerformanceAgcTestBed implements ITestbedFactory {
    
    public static final String MASTER_ROLE_ID = "master_em";
    public static final String FOLLOWER1_ROLE_ID = "follower1_em";
    public static final String FOLLOWER2_ROLE_ID = "follower2_em";
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbed testbed = new Testbed("AGC/Performance");
         
        ITestbedMachine masterMachine = createMachine("master");
        addStandaloneRoles(masterMachine, MASTER_ROLE_ID, tasResolver, true);
        testbed.addMachine(masterMachine);

        ITestbedMachine follower1Machine = createMachine("follower1");
        addStandaloneRoles(follower1Machine, FOLLOWER1_ROLE_ID, tasResolver, false);
        testbed.addMachine(follower1Machine);

        ITestbedMachine follower2Machine = createMachine("follower2");
        addStandaloneRoles(follower2Machine, FOLLOWER2_ROLE_ID, tasResolver, false);
        testbed.addMachine(follower2Machine);

        return testbed;
    }
    
    private ITestbedMachine createMachine(String machineId) {
        return new TestbedMachine.Builder(machineId).platform(Platform.WINDOWS)
                        .templateId("w64_8G").bitness(Bitness.b64).automationBaseDir("C:/sw").build();
    }
    
    private EmRole addStandaloneRoles(ITestbedMachine machine, String roleId, ITasResolver tasResolver, boolean agc) {
        EmRole.Builder emBuilder =
            new EmRole.Builder(roleId, tasResolver).dbpassword("quality")
                .emLaxNlJavaOption(RoleUtility.getDevEmLaxnlJavaOption(9001))
                .nostartEM()
                .nostartWV();
        
        if (agc) {
            emBuilder.configProperty("introscope.apmserver.teamcenter.master", "true");
        }
        
        EmRole emRole = emBuilder.build();
        machine.addRole(emRole);
        
        RoleUtility.addStartEmRole(machine, emRole, agc, emRole);
        
        return emRole;
    }
    
}
