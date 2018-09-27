/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and proprietary and shall not
 * be duplicated, used, disclosed or disseminated in any way except as authorized by the applicable
 * license agreement, without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT PERMITTED BY APPLICABLE
 * LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE
 * USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.systemtest.fld.testbed.smokebeta;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.flow.FluentdFlow;
import com.ca.apm.systemtest.fld.flow.FluentdFlowContext;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.ca.apm.systemtest.fld.flow.FluentdFlow.InstallFluentdPlugin;
import static com.ca.apm.systemtest.fld.flow.FluentdFlow.StartTdAgentRole;
import static com.ca.apm.systemtest.fld.flow.FluentdFlowContext.*;

/**
 * Testbed to demo log collection with fluentD.
 * Contains configuration for EM/Collector (log agent) and fluentD machine (log server)
 * Lots of regex inside
 *
 * @author shadm01
 */
@TestBedDefinition
public class FluentdDemoTestBed implements ITestbedFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(FluentdDemoTestBed.class);
    private String testbedMachineHost;// = "fluentd"; //If we are not creating server here we can hardcode machine name

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbed testbed = new Testbed("FLDMainClusterTestbed");

        addServerReceiveMachine(testbed, tasResolver, "fluentdMachine"); //TODO - this machine can be permanent, does not have to change

        addIntroscopeMonitoringMachine(testbed, "momMachine", "mom");
        addIntroscopeMonitoringMachine(testbed, "collectorMachine", "collector");

        return testbed;
    }

    private void addIntroscopeMonitoringMachine(ITestbed testBed, String machineName, String tagPrefix) {
        LOGGER.info("Installing Monitoring client on MOM / Collector machine: " + machineName);
        ITestbedMachine machine =
                new TestbedMachine.LinuxBuilder(machineName)
                        .templateId("cod64")
                        .bitness(Bitness.b64)
                        .build();


        IFlowContext L1Context =
                new FluentdFlowContext.Builder()
                        .addInputFromTail(tagPrefix + ".introscope",
                                "/home/sw/em/Introscope/logs/IntroscopeEnterpriseManager.log", INTROSCOPE_PATTERN, INTROSCOPE_DATE_PATTERN)
                        .addInputFromTail(tagPrefix + ".dynamicdomains", "/home/sw/em/Introscope/logs/dynamicDomains.log")
                        .addInputFromTail(tagPrefix + ".introscopesupport", "/home/sw/em/Introscope/logs/IntroscopeEnterpriseManagerSupport.log")
                        .addInputFromTail(tagPrefix + ".perflog", "/home/sw/em/Introscope/logs/perflog.txt", PERFLOG_PATTERN, PERFLOG_DATE_PATTERN)
                        .addInputFromTail(tagPrefix + ".querylog", "/home/sw/em/Introscope/logs/querylog.txt")
                        .addInputFromTail(tagPrefix + ".teamcenterregistration", "/home/sw/em/Introscope/logs/TeamCenterRegistration.log")
                        .addInputFromTail(tagPrefix + ".testperf", "/home/sw/em/Introscope/logs/tessperflog.txt")
                        .addInputFromTail(tagPrefix + ".gcc", "/home/sw/em/Introscope/logs/gclog.txt", GC_PATTERN, "")
                        .addOutputToSocket(tagPrefix + ".*", testbedMachineHost, "16161")
                        .build();

        //TODO - add system counters as well ?

        ExecutionRole installAndConfigureFluentD =
                new ExecutionRole.Builder("INSTALL_AND_CONFIGURE_FLUENTD_" + machineName)
                        .flow(FluentdFlow.class, L1Context)
                        .build();


        ExecutionRole L1Start = StartTdAgentRole("FluentD_start_" + machineName);

        installAndConfigureFluentD.before(L1Start);

        machine.addRole(installAndConfigureFluentD);
        machine.addRole(L1Start);


        testBed.addMachine(machine);
    }

    private void addServerReceiveMachine(ITestbed testbed, ITasResolver resolver, String machineName) {
        LOGGER.info("Installing Monitoring server on machine: " + machineName);

        String logFolder = "/tmp/L1_LOGS/";
        //TODO - DM - performance - update file descriptors on the machine to 65553
        ITestbedMachine machine =
                new TestbedMachine.LinuxBuilder(machineName)
                        .templateId("cod64")
                        .bitness(Bitness.b64)
                        .build();

        FluentdFlowContext L2Context =
                new FluentdFlowContext.Builder()
                        .addInputFromScoket("16161")

                        .addOutputToFile("mom.introscope", logFolder + "mom_introscope.log", false)
                        .addOutputToFile("mom.gcc", logFolder + "mom_gcc.log", false)
                        .addOutputToFile("mom.perflog", logFolder + "mom_perflog.log", false)

                        .addOutputToFile("collector.introscope", logFolder + "collector_introscope.log", false)
                        .addOutputToFile("collector.gcc", logFolder + "collector_gcc.log", false)
                        .addOutputToFile("collector.perflog", logFolder + "collector_perflog.log", false)

                        .addMultipleOutputs(
                                "collector.*",
                                new FileOutput(null, logFolder + "collector_logs.log", false),
                                new ElasticSearchOutput(null, "http://172.20.80.12", "19200")
                        )
                        .addMultipleOutputs(
                                "mom.*",
                                new FileOutput(null, logFolder + "mom_logs.log", false),
                                new ElasticSearchOutput(null, "http://172.20.80.12", "19200")
                        )
                        .build();

        ExecutionRole installAndConfigureFluentD =
                new ExecutionRole.Builder("INSTALL_AND_CONFIGURE_FLUENT_D" + machineName)
                        .flow(FluentdFlow.class, L2Context)
                        .build();


        ExecutionRole installElasticSearchPlugin =
                InstallFluentdPlugin("Install_ElasticSearch_plugin" + machineName, "fluent-plugin-elasticsearch");

        String M2StartRole = "FluentD_start_" + machineName;
        ExecutionRole L2Start = StartTdAgentRole(M2StartRole);

        installAndConfigureFluentD.before(installElasticSearchPlugin);
        installElasticSearchPlugin.before(L2Start);

        machine.addRole(installAndConfigureFluentD, installElasticSearchPlugin, L2Start);

        testbed.addMachine(machine);

        testbedMachineHost = resolver.getHostnameById(M2StartRole);
    }


    public static ExecutionRole getCurrentTime(String roleID) {
        RunCommandFlowContext runCmdFlowContext = new RunCommandFlowContext
                .Builder("date")
                .build();

        System.out.println(roleID + "------------------------" + roleID);

        return new ExecutionRole.Builder(roleID)
                .flow(runCmdFlowContext)
                .build();
    }

    private void chmodRole() {
        RunCommandFlowContext chmodContext
                = new RunCommandFlowContext.Builder("chmod")
                .args(Arrays.asList("o+r", "/var/log/cron"))
                .build();
        ExecutionRole L1chModflow = new ExecutionRole.Builder("M1_chmod_on_cron").flow(
                RunCommandFlow.class, chmodContext).build();

    }
}
