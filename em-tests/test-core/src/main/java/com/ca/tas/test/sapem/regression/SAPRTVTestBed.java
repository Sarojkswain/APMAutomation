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
package com.ca.tas.test.sapem.regression;

import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.thirdParty.ControllerJenkins;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.ControllerRole;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.webapp.WebLogicRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Represents testbed driven by CODA controller which consists of following machines to reflect the
 * intention of QA department to verify the integration of EM with SAP NetWeaver.
 * 
 * @author Pospichal, Pavel <pospa02@ca.com>
 * 
 */
@TestBedDefinition
public class SAPRTVTestBed implements ITestbedFactory {

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("APMRegression/common/apmbase/sap_integration/sap_rtv.xml");
        // setup controller
        ITestbedMachine controllerMachine =
                new TestbedMachine.Builder("controller").templateId(ITestbedMachine.TEMPLATE_W64).bitness(Bitness.b64)
                        .automationBaseDir("C:/sw").build();

        ControllerRole controllerRole =
                new ControllerRole.Builder("coda-controller", tasResolver).jenkinsPort(8080)
                        .jenkinsCodaArtifact(ControllerJenkins.v4_0_12).build();

        controllerMachine.addRole(controllerRole);
        testbed.addMachine(controllerMachine);

        // setup testing machines
        ITestbedMachine rtv01TestingMachine =
                new TestbedMachine.Builder("rtv01").templateId(ITestbedMachine.TEMPLATE_W64).bitness(Bitness.b64)
                        .automationBaseDir("C:/sw").build();

        WebLogicRole wlsRole = new WebLogicRole.Builder("wls103", tasResolver).build();
        rtv01TestingMachine.addRole(wlsRole);

        rtv01TestingMachine.addRole(new DeployFreeRole("agent_deep"));
        rtv01TestingMachine.addRole(new DeployFreeRole("agent_smoke"));
        rtv01TestingMachine.addRole(new DeployFreeRole("client01_deep"));
        rtv01TestingMachine.addRole(new DeployFreeRole("agent_full"));
        rtv01TestingMachine.addRole(new DeployFreeRole("client01_full1"));
        rtv01TestingMachine.addRole(new DeployFreeRole("client01_smoke"));
        rtv01TestingMachine.addRole(new DeployFreeRole("agent_mom_deep"));
        rtv01TestingMachine.addRole(new DeployFreeRole("collector01"));
        rtv01TestingMachine.addRole(new DeployFreeRole("agent02"));
        rtv01TestingMachine.addRole(new DeployFreeRole("agent_mom_smoke"));
        rtv01TestingMachine.addRole(new DeployFreeRole("client01_deep1"));
        rtv01TestingMachine.addRole(new DeployFreeRole("sapem01"));
        rtv01TestingMachine.addRole(new DeployFreeRole("client_mom_deep"));
        rtv01TestingMachine.addRole(new DeployFreeRole("mom01"));
        rtv01TestingMachine.addRole(new DeployFreeRole("client_mom_smoke"));
        rtv01TestingMachine.addRole(new DeployFreeRole("agent_deep1"));
        rtv01TestingMachine.addRole(new DeployFreeRole("webapp02"));
        DeployFreeRole clientRole = new DeployFreeRole("client01_full");
        clientRole.addProperty("myresults.dir", "c:/automation/deployed/results/junitreports");
        rtv01TestingMachine.addRole(clientRole);

        rtv01TestingMachine.addRemoteResource(RemoteResource.createFromLocation("c:/automation/deployed/results"));
        
        testbed.addMachine(rtv01TestingMachine);

        return testbed;
    }

}
