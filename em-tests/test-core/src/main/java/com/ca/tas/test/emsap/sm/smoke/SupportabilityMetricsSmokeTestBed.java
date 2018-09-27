/*
 * Copyright (c) 2015 CA. All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.tas.test.emsap.sm.smoke;

import com.ca.tas.artifact.thirdParty.ControllerJenkins;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.ControllerRole;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Represents testbed driven by CODA controller which consists of following machines to reflect the
 * intention of customer to install CA Introscope SAP in various roles.
 * 
 * @author Pospichal, Pavel <pospa02@ca.com>
 */
@TestBedDefinition
public class SupportabilityMetricsSmokeTestBed implements ITestbedFactory {

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed =
                new Testbed(
                        "qa/EM/92/SupportabilityMetrics/suite/sm_sap_testsuite_smoke_with_onerror.xml");
        // setup controller
        ITestbedMachine controllerMachine =
                new TestbedMachine.Builder("controller").templateId(ITestbedMachine.TEMPLATE_W64).bitness(Bitness.b64)
                        .automationBaseDir("C:/sw").build();

        ControllerRole controllerRole =
                new ControllerRole.Builder("coda-controller", tasResolver).jenkinsPort(8080)
                        .jenkinsCodaArtifact(ControllerJenkins.v4_0_12).build();
        controllerMachine.addRole(controllerRole);
        testbed.addMachine(controllerMachine);

        // setup testing machine
        ITestbedMachine testingMachine =
                new TestbedMachine.Builder("testing").templateId(ITestbedMachine.TEMPLATE_W64).bitness(Bitness.b64)
                        .automationBaseDir("C:\\sw").build();

        TomcatRole tomcatRole = new TomcatRole.Builder("tomcat55", tasResolver).build();
        testingMachine.addRole(tomcatRole);

        testingMachine.addRole(new DeployFreeRole("c1"));
        testingMachine.addRole(new DeployFreeRole("emdb"));
        testingMachine.addRole(new DeployFreeRole("client01"));
        testingMachine.addRole(new DeployFreeRole("st"));
        testingMachine.addRole(new DeployFreeRole("tomcatclient01"));
        testingMachine.addRole(new DeployFreeRole("mom"));

        testbed.addMachine(testingMachine);

        return testbed;
    }

}
