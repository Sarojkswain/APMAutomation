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
package com.ca.tas.test.em.regression;

import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.thirdParty.ControllerJenkins;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.ControllerRole;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.role.webapp.WebLogicRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.google.common.collect.Sets;

/**
 * Represents testbed driven by CODA controller which consists of following machines to reflect the
 * intention of CQ department to replicate scenarios with incorrect functionality reported by
 * customer.
 * 
 * @author Pospichal, Pavel <pospa02@ca.com>
 * 
 */
@TestBedDefinition
public class CQEMBugsTestBed implements ITestbedFactory {

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("cq/automation/bugs/bugs_em.xml");
        // setup controller
        ITestbedMachine controllerMachine =
                new TestbedMachine.Builder("controller").templateId(ITestbedMachine.TEMPLATE_W64).bitness(Bitness.b64)
                        .automationBaseDir("C:/sw").build();

        ControllerRole controllerRole =
                new ControllerRole.Builder("coda-controller", tasResolver).jenkinsPort(8080)
                        .jenkinsCodaArtifact(ControllerJenkins.v4_0_12).build();

        controllerMachine.addRole(controllerRole);
        controllerMachine.addRole(new DeployFreeRole("clientresults"));
        testbed.addMachine(controllerMachine);

        // setup testing machines
        ITestbedMachine cl1TestingMachine =
                new TestbedMachine.Builder("cl1").templateId(ITestbedMachine.TEMPLATE_W64).bitness(Bitness.b64)
                        .automationBaseDir("C:/sw").build();

        cl1TestingMachine.addRole(new DeployFreeRole("clientdb"));
        cl1TestingMachine.addRole(new DeployFreeRole("db01"));
        cl1TestingMachine.addRole(new DeployFreeRole("em01"));
        cl1TestingMachine.addRole(new DeployFreeRole("agent03"));
        cl1TestingMachine.addRole(new DeployFreeRole("client05"));
        cl1TestingMachine.addRole(new DeployFreeRole("client03"));
        cl1TestingMachine.addRole(new DeployFreeRole("emoracle"));
        cl1TestingMachine.addRole(new DeployFreeRole("client04"));
        DeployFreeRole client01Role = new DeployFreeRole("client01");
        client01Role.addProperty("myresults.dir", "c:/automation/deployed/results/junitreports");
        cl1TestingMachine.addRole(client01Role);
        cl1TestingMachine.addRemoteResource(RemoteResource.createFromLocation("c:/automation/deployed/results"));
        testbed.addMachine(cl1TestingMachine);

        ITestbedMachine cl2TestingMachine =
                new TestbedMachine.Builder("cl2").templateId(ITestbedMachine.TEMPLATE_W64).bitness(Bitness.b64)
                        .automationBaseDir("C:/sw").build();

        // TODO: optimize the list of required components, so far the core with admin console and
        // samples is required
        WebLogicRole wlsRole = new WebLogicRole.Builder("wls103", tasResolver)
                .installLocation("C:/Oracle/Middleware")
                .installDir("C:/Oracle/Middleware/wlserver_10.3")
                .webLogicInstallerDir("C:/Oracle/sources")
                .installLogFile("C:/Oracle/install.log")
                .responseFileDir("C:/Oracle/responseFiles")
                .customComponentPaths(
                        Sets.newHashSet(
                                "WebLogic Server/Core Application Server",
                                "WebLogic Server/Administration Console",
                                "WebLogic Server/Configuration Wizard and Upgrade Framework",
                                "WebLogic Server/Web 2.0 HTTP Pub-Sub Server",
                                "WebLogic Server/WebLogic JDBC Drivers",
                                "WebLogic Server/Third Party JDBC Drivers",
                                "WebLogic Server/WebLogic Server Clients",
                                "WebLogic Server/WebLogic Web Server Plugins",
                                "WebLogic Server/UDDI and Xquery Support",
                                "WebLogic Server/Server Examples"))
                .build();
        cl2TestingMachine.addRole(wlsRole);

        TomcatRole tomcatRole =
                new TomcatRole.Builder("tomcat60", tasResolver).tomcatVersion(TomcatVersion.v60)
                        .tomcatCatalinaPort(8088).jdkHomeDir("C:/Program Files/Java/jdk1.6.0_45")
                        .installDir("C:/sw/apache-tomcat-6.0.36").build();
        cl2TestingMachine.addRole(tomcatRole);

        cl2TestingMachine.addRole(new DeployFreeRole("agent01"));
        cl2TestingMachine.addRole(new DeployFreeRole("agent02"));

        testbed.addMachine(cl2TestingMachine);

        return testbed;
    }

}
