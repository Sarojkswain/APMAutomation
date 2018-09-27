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
 */

package com.ca.tas.test.sample.testbed;

import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.thirdParty.ControllerJenkins;
import com.ca.tas.artifact.thirdParty.WebSphereVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.ControllerRole;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.webapp.WebLogicRole;
import com.ca.tas.role.webapp.WebSphereRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * EmBatTestRunner class
 * <p/>
 * Class description
 *
 * @author Jan Pojer (pojja01.ca.com)
 * @since 1.0
 */
@TestBedDefinition
public class EmBatTestRunner
        implements ITestbedFactory
{
    @Override
    public ITestbed create(ITasResolver tasResolver)
    {
        ITestbed testbed = new Testbed("APMRegression/bat/apmbase/testSuitesModified/em_bat_sa_v2.xml");
        //setup controller
        ITestbedMachine controllerMachine = new TestbedMachine.Builder("controller")
                .templateId(ITestbedMachine.TEMPLATE_W64).bitness(Bitness.b64).automationBaseDir("C:\\sw").build();

        ControllerRole controllerRole = new ControllerRole.Builder("coda-controller", tasResolver)
                .jenkinsPort(8080)
                .jenkinsCodaArtifact(ControllerJenkins.v4_0_12)
                .globalProperty("email.sender", "tas@ca.com")
                .globalProperty("email.recipients", "korzd01@ca.com")
                .build();
        controllerMachine.addRole(controllerRole);
        RoleUtility.gatherJenkinsLogs(controllerRole, controllerMachine);
        testbed.addMachine(controllerMachine);

        //setup testing machine
        ITestbedMachine testMachine = new TestbedMachine.Builder("testMachine")
                .templateId(ITestbedMachine.TEMPLATE_W64).bitness(Bitness.b64).build();

        testMachine.addRole(new DeployFreeRole("agent01"));
        testMachine.addRole(new DeployFreeRole("agent01_aggregateagent"));
        testMachine.addRole(new DeployFreeRole("agent01_domainspermissionmanagement"));
        testMachine.addRole(new DeployFreeRole("agent01_ldapConfiguration"));
        testMachine.addRole(new DeployFreeRole("agent01_sstoolsmom"));
        testMachine.addRole(new DeployFreeRole("agent02"));
        testMachine.addRole(new DeployFreeRole("agent02_aggregateagent"));
        testMachine.addRole(new DeployFreeRole("agent02_ccdirectory"));
        testMachine.addRole(new DeployFreeRole("agent02_domainspermissionmanagement"));
        testMachine.addRole(new DeployFreeRole("agent02_embasics"));
        testMachine.addRole(new DeployFreeRole("agent02_embasics_test"));
        testMachine.addRole(new DeployFreeRole("agent02_javascriptenhancements"));
        testMachine.addRole(new DeployFreeRole("agent02_sstools"));
        testMachine.addRole(new DeployFreeRole("agent02_sstools_2"));
        testMachine.addRole(new DeployFreeRole("agent02_watchdog"));
        testMachine.addRole(new DeployFreeRole("agent02_watchdog_2"));
        testMachine.addRole(new DeployFreeRole("agent03_domainspermissionmanagement"));
        testMachine.addRole(new DeployFreeRole("agent04"));
        DeployFreeRole clientRole = new DeployFreeRole("client01");
        clientRole.addProperty("myresults.dir", "c:/automation/deployed/results/junitreports");
        testMachine.addRole(clientRole);
        testMachine.addRole(new DeployFreeRole("client01_aggregateagent"));
        testMachine.addRole(new DeployFreeRole("client01_apm9.1newtests_test"));
        testMachine.addRole(new DeployFreeRole("client01_ccdirectory"));
        testMachine.addRole(new DeployFreeRole("client01_controlscripts"));
        testMachine.addRole(new DeployFreeRole("client01_domainspermissionmanagement"));
        testMachine.addRole(new DeployFreeRole("client01_dpm1"));
        testMachine.addRole(new DeployFreeRole("client01_embasics"));
        testMachine.addRole(new DeployFreeRole("client01_embasics_test"));
        testMachine.addRole(new DeployFreeRole("client01_emproplax2"));
        testMachine.addRole(new DeployFreeRole("client01_emproplax2_agent"));
        testMachine.addRole(new DeployFreeRole("client01_empropslax"));
        testMachine.addRole(new DeployFreeRole("client01_empropslax_agent"));
        testMachine.addRole(new DeployFreeRole("client01_javascriptenhancements"));
        testMachine.addRole(new DeployFreeRole("client01_ldapConfiguration"));
        testMachine.addRole(new DeployFreeRole("client01_rebindportcommunication"));
        testMachine.addRole(new DeployFreeRole("client01_sstools"));
        testMachine.addRole(new DeployFreeRole("client01_sstools_2"));
        testMachine.addRole(new DeployFreeRole("client01_sstoolsmom"));
        testMachine.addRole(new DeployFreeRole("client01_watchdog"));
        testMachine.addRole(new DeployFreeRole("client01_watchdog_2"));
        testMachine.addRole(new DeployFreeRole("client01_windowsservicewrappers"));
        testMachine.addRole(new DeployFreeRole("em01"));
        testMachine.addRole(new DeployFreeRole("em02"));
        testMachine.addRole(new DeployFreeRole("sapem01"));
        testMachine.addRole(new DeployFreeRole("qcuploadtool01"));
        WebSphereRole wasRole = new WebSphereRole.Builder("webapp01", tasResolver)
                .wasInstallLocation("C:/IBM/WebSphere/AppServer")
                .responseFileDir("C:/IBM/responseFiles")
                .wasUnpackedInstallSourcesDir("C:/IBM/sourcesUnpacked/install")
                .wasInstaller(WebSphereVersion.v70x64w)
                .build();
        testMachine.addRole(wasRole);
        testMachine.addRole(new DeployFreeRole("webapp01_aggregateagent"));
        testMachine.addRole(new DeployFreeRole("webapp01_domainspermissionmanagement"));
        testMachine.addRole(new DeployFreeRole("webapp01_ldapConfiguration"));
        testMachine.addRole(new DeployFreeRole("webapp01_watchdog_2"));

        WebLogicRole wlRole = new WebLogicRole.Builder("webapp02", tasResolver).
            installLocation("C:/Oracle/Middleware").
            installDir("C:/Oracle/Middleware/wlserver_10.3").
            webLogicInstallerDir("C:/Oracle/sources").
            installLogFile("C:/Oracle/install.log").
            responseFileDir("C:/Oracle/responseFiles").
            build();
        testMachine.addRole(wlRole);
        
        testMachine.addRole(new DeployFreeRole("webapp02_2"));
        testMachine.addRole(new DeployFreeRole("webapp02_aggregateagent"));
        testMachine.addRole(new DeployFreeRole("webapp02_ccdirectory"));
        testMachine.addRole(new DeployFreeRole("webapp02_domainspermissionmanagement"));
        testMachine.addRole(new DeployFreeRole("webapp02_embasics"));
        testMachine.addRole(new DeployFreeRole("webapp02_embasics_test"));
        testMachine.addRole(new DeployFreeRole("webapp02_javascriptenhancements"));
        testMachine.addRole(new DeployFreeRole("webapp02_ldapConfiguration"));
        testMachine.addRole(new DeployFreeRole("webapp02_sstools"));
        testMachine.addRole(new DeployFreeRole("webapp02_sstools_2"));
        testMachine.addRole(new DeployFreeRole("webapp02_watchdog"));
        testMachine.addRole(new DeployFreeRole("webapp02_watchdog_2"));
        testMachine.addRole(new DeployFreeRole("db01"));

        testMachine.addRemoteResource(RemoteResource.createFromLocation("c:/automation/deployed/results"));
        
        testbed.addMachine(testMachine);

        RoleUtility.fixRegistryForJenkinsRole(tasResolver, controllerMachine, controllerMachine.getRoles());
        RoleUtility.fixRegistryForJenkinsRole(tasResolver, testMachine, testMachine.getRoles());

        return testbed;
    }
}
