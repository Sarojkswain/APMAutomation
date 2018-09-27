package com.ca.tas.test.aba.smoke;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.ControllerRole;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.web.TradeServiceAppRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO65;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

/**
 * Represents testbed driven by CODA controller which consists of following
 * machines to reflect the intention of customer to install CA Introscope with
 * Prelert.
 *
 * @author Pospichal, Pavel <pospa02@ca.com>
 */
@TestBedDefinition
public class ABASmokeTestBed implements ITestbedFactory {

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("qa/wv_ui/smoke/default/gremlin_smoke_with_as.xml");
        // setup controller
        ITestbedMachine controllerMachine = new TestbedMachine.Builder("controller")
            .templateId(TEMPLATE_W64)
            .bitness(Bitness.b64)
            .automationBaseDir("C:/sw")
            .build();

        ControllerRole controllerRole = new ControllerRole.Builder("coda-controller", tasResolver)
            .jenkinsPort(8080).build();

        controllerMachine.addRole(controllerRole);
        testbed.addMachine(controllerMachine);

        // setup testing machine
        ITestbedMachine testingMachine = new TestbedMachine.Builder("testing")
            .templateId(TEMPLATE_W64)
            .bitness(Bitness.b64)
            .automationBaseDir("C:\\Users\\Administrator")
            .build();

        TradeServiceAppRole tradeServiceAppRole = new TradeServiceAppRole.Builder("trade-service",
            tasResolver)
            .installDir("c:\\sw\\apache-tomcat-6.0.26").build();
        testingMachine.addRole(tradeServiceAppRole);

        testingMachine.addRole(new DeployFreeRole("AttemptCreateMMNonUniqueJarName"));
        testingMachine.addRole(new DeployFreeRole("CreateMMFromActivitySpecifyJarName"));
        testingMachine.addRole(new DeployFreeRole("airge01-10"));
        testingMachine.addRole(new DeployFreeRole("PreventCreateMMAnalysisServerNotAvailable"));
        testingMachine.addRole(new DeployFreeRole("airge01-14"));
        testingMachine.addRole(new DeployFreeRole("airge01-13"));
        testingMachine.addRole(new DeployFreeRole("airge01-12"));
        testingMachine.addRole(new DeployFreeRole("airge01-11"));
        testingMachine.addRole(new DeployFreeRole("analyticsserver01"));
        testingMachine.addRole(new DeployFreeRole("airge01-17"));
        testingMachine.addRole(new DeployFreeRole("webapp01"));
        testingMachine.addRole(new DeployFreeRole("airge01-16"));
        testingMachine.addRole(new DeployFreeRole("airge01-15"));
        testingMachine.addRole(new DeployFreeRole("joncu01-10"));
        testingMachine.addRole(new DeployFreeRole("joncu01-11"));
        testingMachine.addRole(new DeployFreeRole("joncu01-12"));
        testingMachine.addRole(new DeployFreeRole("qatf500"));
        testingMachine.addRole(new DeployFreeRole("joncu01-13"));
        testingMachine.addRole(new DeployFreeRole("joncu01-14"));
        testingMachine.addRole(new DeployFreeRole("smka01"));
        testingMachine.addRole(new DeployFreeRole("joncu01-15"));
        testingMachine.addRole(new DeployFreeRole("AttemptCreateMMAnalysisServerNotAvailable"));
        testingMachine.addRole(new DeployFreeRole("CreateMMFromActivity"));
        testingMachine.addRole(new DeployFreeRole("qcuploadtool01"));
        testingMachine.addRole(new DeployFreeRole("client01"));
        testingMachine.addRole(new DeployFreeRole("AttemptCreateMMWithTooManyMetricsSelected"));
        testingMachine.addRole(new DeployFreeRole("client02"));
        testingMachine.addRole(new DeployFreeRole("joncu01-09"));
        testingMachine.addRole(new DeployFreeRole("joncu01-07"));
        testingMachine.addRole(new DeployFreeRole("joncu01-08"));
        testingMachine.addRole(new DeployFreeRole("joncu01-05"));
        testingMachine.addRole(new DeployFreeRole("joncu01-06"));
        testingMachine.addRole(new DeployFreeRole("joncu01-03"));
        testingMachine.addRole(new DeployFreeRole("AttemptCreateMMNonUniqueName"));
        testingMachine.addRole(new DeployFreeRole("joncu01-04"));
        testingMachine.addRole(new DeployFreeRole("joncu01-01"));
        testingMachine.addRole(new DeployFreeRole("joncu01-02"));
        testingMachine.addRole(new DeployFreeRole("MMCreationUnavailableWhenNoMetricsSelected"));
        testingMachine.addRole(new DeployFreeRole("CreateSecondMMFromActivity"));
        testingMachine.addRole(new DeployFreeRole("em01"));
        testingMachine.addRole(new DeployFreeRole("agent01"));
        testingMachine.addRole(new DeployFreeRole("CancelCreateMMFromActivity"));
        testingMachine.addRole(new DeployFreeRole("airge01-01"));
        testingMachine.addRole(new DeployFreeRole("airge01-03"));
        testingMachine.addRole(new DeployFreeRole("airge01-02"));
        testingMachine.addRole(new DeployFreeRole("qatf01"));
        testingMachine.addRole(new DeployFreeRole("airge01-09"));
        testingMachine.addRole(new DeployFreeRole("ForceCreateMMNonUniqueName"));
        testingMachine.addRole(new DeployFreeRole("airge01-08"));
        testingMachine.addRole(new DeployFreeRole("CreateMMWhereMetricsCrossPageBoundaries"));
        testingMachine.addRole(new DeployFreeRole("airge01-05"));
        testingMachine.addRole(new DeployFreeRole("airge01-04"));
        testingMachine.addRole(new DeployFreeRole("airge01-07"));
        testingMachine.addRole(new DeployFreeRole("airge01-06"));

        testbed.addMachine(testingMachine);

        // setup machine with CA AS
        ITestbedMachine machineCAAS = new TestbedMachine.Builder("caas")
            .templateId(TEMPLATE_CO65)
            .sshUserName("bob")
            .automationBaseDir("/home/bob")
            .build();

        machineCAAS.addRole(new DeployFreeRole("caas01"));

        testbed.addMachine(machineCAAS);

        return testbed;
    }

}
