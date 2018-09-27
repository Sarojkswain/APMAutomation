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

import java.util.Collection;

import com.ca.apm.testbed.atc.SeleniumGridMachinesFactory;
import com.ca.tas.annotation.resource.RemoteResource;
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
 * Represents testbed for verifying the AGC registration.
 * 
 * @author Korcak, Zdenek <korzd01@ca.com>
 * 
 */
@TestBedDefinition
public class SimpleAgcTestBed implements ITestbedFactory {
    
    public static final String MASTER_ROLE_ID = "master_em";
    public static final String FOLLOWER_ROLE_ID = "follower_em";
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbed testbed = new Testbed("AGC/Simple");
         
        ITestbedMachine masterMachine = createMachine("master");
        addStandaloneRoles(masterMachine, MASTER_ROLE_ID, tasResolver, true);
        masterMachine.addRemoteResource(RemoteResource.createFromRegExp(".*screenshots.*", RemoteResource.TEMP_FOLDER));
        testbed.addMachine(masterMachine);

        ITestbedMachine followerMachine = createMachine("follower");
        addStandaloneRoles(followerMachine, FOLLOWER_ROLE_ID, tasResolver, false);
        followerMachine.addRemoteResource(RemoteResource.createFromRegExp(".*screenshots.*", RemoteResource.TEMP_FOLDER));
        testbed.addMachine(followerMachine);
        
        // Remote Selenium Grid
        SeleniumGridMachinesFactory seleniumGridMachinesFactory = new SeleniumGridMachinesFactory();
        Collection<ITestbedMachine> seleniumGridMachines = seleniumGridMachinesFactory.createMachines(tasResolver);

        testbed.addMachines(seleniumGridMachines);
                
        // register remote Selenium Grid
        String hubHostName = tasResolver.getHostnameById(SeleniumGridMachinesFactory.HUB_ROLE_ID);
        testbed.addProperty("selenium.webdriverURL", "http://" + hubHostName + ":4444/wd/hub");

        return testbed;
    }
    
    private ITestbedMachine createMachine(String machineId) {
        return new TestbedMachine.LinuxBuilder(machineId).platform(Platform.LINUX)
                        .templateId("rh66").bitness(Bitness.b64).build();
    }
    
    private EmRole addStandaloneRoles(ITestbedMachine machine, String roleId, ITasResolver tasResolver, boolean agc) {
        EmRole.Builder emBuilder =
            new EmRole.LinuxBuilder(roleId, tasResolver).dbpassword("quality")
                .nostartEM()
                .nostartWV();
        
        if (agc) {
            emBuilder.configProperty("introscope.apmserver.teamcenter.master", "true");
        }
        
        EmRole emStandaloneRole = emBuilder.build();
        
        machine.addRole(emStandaloneRole);
        
        return emStandaloneRole;
    }
    
    
}
