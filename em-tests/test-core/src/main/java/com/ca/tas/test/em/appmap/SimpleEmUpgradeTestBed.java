/*
 * Copyright (c) 2016 CA.  All rights reserved.
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

@TestBedDefinition
public class SimpleEmUpgradeTestBed implements ITestbedFactory {

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("Introscope/AppMap/SimpleUpgradeEm");

        ITestbedMachine emMachine =
            new TestbedMachine.Builder(SimpleEmTestBed_10_X.MACHINE_ID).platform(Platform.WINDOWS)
                .templateId("w64").bitness(Bitness.b64).automationBaseDir("C:/sw").build();
        EmRole.Builder emBuilder =
            new EmRole.Builder(SimpleEmTestBed_10_X.EM_ROLE_ID, tasResolver)
                .installerProperty("shouldUpgrade", "true")
                .installerProperty("upgradeSchema", "true");
        customizeEmBuilder(testbed, emBuilder, tasResolver);
        EmRole emRole = emBuilder.build();
        emMachine.addRole(emRole);
        testbed.addMachine(emMachine);
        
        // register remote Selenium Grid
        testbed.addProperty("selenium.webdriverURL", "http://cz-selenium1.ca.com:4444/wd/hub");

        testbed.addProperty(
            "test.applicationBaseURL",
            String.format("http://%s:8082", tasResolver.getHostnameById(SimpleEmTestBed_10_X.EM_ROLE_ID)));

        return testbed;
    }
    
    void customizeEmBuilder(ITestbed testbed, EmRole.Builder emBuilder, ITasResolver tasResolver) {
    }
}
