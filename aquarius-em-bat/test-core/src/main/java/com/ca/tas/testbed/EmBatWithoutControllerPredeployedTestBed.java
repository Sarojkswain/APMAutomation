package com.ca.tas.testbed;


import com.ca.tas.artifact.thirdParty.ControllerJenkins;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.ControllerRole;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.mercurial.HgServerCertificateWindowsSilentAcceptorRole;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public class EmBatWithoutControllerPredeployedTestBed  implements ITestbedFactory {
	@Override
	public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("APMRegression/bat/apmbase/testSuitesModified/em_bat_sa_v2.xml");
        //setup controller
        ITestbedMachine controllerMachine = new TestbedMachine.Builder("controller")
                .templateId("w64c").bitness(Bitness.b64).automationBaseDir("C:/sw").build();

        ControllerRole controllerRole = new ControllerRole.Builder("coda-controller", tasResolver)
                .jenkinsPort(8080).jenkinsCodaArtifact(ControllerJenkins.v4_0_12).build();
        controllerMachine.addRole(controllerRole);
        controllerMachine.addRole(new HgServerCertificateWindowsSilentAcceptorRole("HgServerCertificateWindowsSilentAcceptorRole"));
        testbed.addMachine(controllerMachine);

        //setup testing machine
        ITestbedMachine testMachine = new TestbedMachine.Builder("machine1").templateId("WINDOWS64").bitness(Bitness.b64).automationBaseDir("C:/sw").build();

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
        testMachine.addRole(new DeployFreeRole("client01"));
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
        testMachine.addRole(new DeployFreeRole("webapp01"));
        testMachine.addRole(new DeployFreeRole("webapp01_aggregateagent"));
        testMachine.addRole(new DeployFreeRole("webapp01_domainspermissionmanagement"));
        testMachine.addRole(new DeployFreeRole("webapp01_ldapConfiguration"));
        testMachine.addRole(new DeployFreeRole("webapp01_watchdog_2"));
        testMachine.addRole(new DeployFreeRole("webapp02"));
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
        testMachine.addRole(new DeployFreeRole("qcuploadtool01"));
        
        testbed.addMachine(testMachine);

        return testbed;
    }

}
