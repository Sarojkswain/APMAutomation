/*
 * Copyright (c) 2016 CA. All rights reserved.
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

package com.ca.apm.systemtest.fld.testbed;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.systemtest.fld.artifact.thirdparty.MQExplorerVersion;
import com.ca.apm.systemtest.fld.role.ClientDeployRole;
import com.ca.apm.systemtest.fld.role.IBMMQRole;
import com.ca.apm.systemtest.fld.role.MQMonitorRole;
import com.ca.apm.systemtest.fld.role.MqJmsAppRole;
import com.ca.apm.tests.artifact.CsvToXlsTemplateVersion;
import com.ca.apm.tests.artifact.Websphere85Version;
import com.ca.apm.tests.role.*;
import com.ca.apm.tests.testbed.machines.EmMachine;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmptyRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Testbed for MQ + IIB10 FieldPack performance test
 *
 * The testbed has predeployed components:
 * - EM
 * - Fieldpack Agent (3x)
 * - WAS + MQ JMS App
 * - MQ Explorer (+ 600 queues)
 * - IIB 10 (+ 100 flows utilizing MQ queues)
 *
 * The testbed uses an external Java utility to push/pull messages from MQ queues
 *
 * SC issue 461560 / DE200642
 * SC issue 440683 / DE200849
 *
 * @Author Erik Melecky (meler02)
 */
@TestBedDefinition
public class PerfMQLoadTestbed extends PowerPackSystemTestBase {

    public static final String AGENT_MQ_ROLE_ID = "agentMqRoleId";

    public ITasResolver tasResolver;
    public static String WAS_85_ROLE_ID = "wasRoleId";
    public static final String WAS_MQAPP_ID = "mqJmsAppRole";

    public static final String WAS_TESTBED_ID = "mqtestbed";
    public static final String MQ_TESTBED_ID = "ibmmqRole";

    //MQ Queue properties
    private static final String creditReplySOJQueue = "creditReplySOJQueue";
    private static final String creditRequestSOJQueue = "creditRequestSOJQueue";
    private static final String replySOJQueue = "replySOJQueue";
    private static final String requestSOJQueue = "requestSOJQueue";
    private static final String qManagerName = "QMGR1";

    //general properties
    protected static final String RESULTS_LOC = "c:\\sw\\results\\";
    public static final String DEFAULT_WAS_INSTALL_PATH = "C:\\SW\\websphere";

    public static final String[] PERFMON_PROCESSOR = new String[]{
            "\\Processor(_Total)\\% Processor Time"
    };

    public static final long DEFAULT_RUN_TIME = 30L;

    public static final String TYPEPERF_OUTPUT_DIR = "C:\\automation\\test_results";
    public static final String JMX_MONITOR_OUTPUT_DIR = "C:\\automation\\test_results";
    public static final String LOGS_GATHERER_OUTPUT_DIR = "C:\\automation\\test_results\\logs";

    protected HashMap<String, String> qNamesAsList = new HashMap<>();
    protected HashMap<String, Integer> qmanagerPort = new HashMap<>();

    public static final String SHARE_DIR = "c:\\share";
    public static final String SHARE_DIR_NAME = "share";
    protected String testResultsShare;

    private static Map<String, String> sheetsMapping = new HashMap<>();
    static {
        // CPU
        sheetsMapping.put("cpu_nomq.csv", "cpu_nomq");
        sheetsMapping.put("cpu_mq_nomb.csv", "cpu_mq_nomb");
        sheetsMapping.put("cpu_nomq_mb.csv", "cpu_nomq_mb");
        sheetsMapping.put("cpu_mq_mb.csv", "cpu_mq_mb");
        // MEMORY
        sheetsMapping.put("mem_nomq.csv", "mem_nomq");
        sheetsMapping.put("mem_mq_nomb.csv", "mem_mq_nomb");
        sheetsMapping.put("mem_nomq_mb.csv", "mem_nomq_mb");
        sheetsMapping.put("mem_mq_mb.csv", "mem_mq_mb");
        // REQUESTS
        sheetsMapping.put("jmeter_nomq.modified.csv", "req_nomq");
        sheetsMapping.put("jmeter_mq_nomb.modified.csv", "req_mq_nomb");
        sheetsMapping.put("jmeter_nomq_mb.modified.csv", "req_nomq_mb");
        sheetsMapping.put("jmeter_mq_mb.modified.csv", "req_mq_mb");
    }

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        Map<String, String> gathererMap = new HashMap<>();

        this.qNamesAsList.put(qManagerName, creditReplySOJQueue + ":" + creditRequestSOJQueue + ":" + replySOJQueue + ":" + requestSOJQueue);
        this.qmanagerPort.put(qManagerName, 1414);

        ITestbed testBed = new Testbed(WAS_TESTBED_ID);
        TestbedMachine appServerTestbedMachine = new TestbedMachine.Builder(appServerMachine).templateId(MACHINE_TEMPLATE_ID).build();
        TestbedMachine mqServerTestbedMachine = new TestbedMachine.Builder(mqServerMachine).templateId(MACHINE_TEMPLATE_ID).build();
        TestbedMachine loadTestbedMachine = new TestbedMachine.Builder(loadMachine).templateId(MACHINE_TEMPLATE_ID).build();

        ///////////////////////////////////////////
        // DEPLOY EM
        ///////////////////////////////////////////

        // PREDEPLOYED
        IRole emRole = new EmptyRole.Builder(EM_ROLE_ID, tasResolver).build();
        loadTestbedMachine.addRole(emRole);

        testResultsShare = "\\\\" + tasResolver.getHostnameById(emRole.getRoleId()) + "\\" + EmMachine.SHARE_DIR_NAME;

        ///////////////////////////////////////////
        // CREATE SHARE
        ///////////////////////////////////////////

        RunCommandFlowContext createShareDirFlowContext = new RunCommandFlowContext.Builder("if")
                .args(Arrays.asList("not", "exist", SHARE_DIR, "mkdir", SHARE_DIR)).build();
        ExecutionRole createShareDirRole = new ExecutionRole.Builder("createShareDirRoleId")
                .syncCommand(createShareDirFlowContext).build();

        loadTestbedMachine.addRole(createShareDirRole);

        RunCommandFlowContext createShareFlowContext = new RunCommandFlowContext.Builder("if")
                .args(Arrays.asList("not", "exist", "\\\\localhost\\" + SHARE_DIR_NAME, "net", SHARE_DIR_NAME, "share=" + SHARE_DIR, "/GRANT:Everyone,FULL")).build();
        ExecutionRole createShareRole = new ExecutionRole.Builder("createShareRoleId")
                .syncCommand(createShareFlowContext).build();

        createShareRole.after(createShareDirRole);
        loadTestbedMachine.addRole(createShareRole);


        ///////////////////////////////////////////
        // DEPLOY CSV2XLS TEMPLATE
        ///////////////////////////////////////////

        CsvToXlsTemplateRole csvToXlsTemplateRole = new CsvToXlsTemplateRole.Builder("csvToXlsTemplateRoleId", tasResolver)
                .installPath("c:/sw/wily/csvToXls").version(CsvToXlsTemplateVersion.AGENT_VER_10_2).build();

        loadTestbedMachine.addRole(csvToXlsTemplateRole);

        ///////////////////////////////////////////
        // DEPLOY CSV2XLS
        ///////////////////////////////////////////

        CsvToXlsRole csvToXlsRole = new CsvToXlsRole.Builder("csvToXlsRoleId", tasResolver)
                .installPath("c:/sw/wily/csvToXls")
                .shareDir(SHARE_DIR)
                .templateFileName(csvToXlsTemplateRole.getTemplateFilePath())
                .outputFileName(SHARE_DIR + "\\Results_40min.xls")
                .sheetsMapping(sheetsMapping).build();

        loadTestbedMachine.addRole(csvToXlsRole);

        /////////////////////////////
        ///// MQ Monitor
        /////////////////////////////

        // MQ no MB
        // PREDEPLOYED
        MQMonitorRole mqMonitorMqNoMbRole = new MQMonitorRole.Builder("mqMonitorMqNoMbRole", tasResolver)
                .installDir("c:\\sw\\agent_mq").build();
        mqServerTestbedMachine.addRole(mqMonitorMqNoMbRole);
        gathererMap.put("mq_monitor_mq_nomb.zip", mqMonitorMqNoMbRole.getMqMonitorInstallDir() + "\\logs");

        // no MQ MB
        // PREDEPLOYED
        MQMonitorRole mqMonitorNoMqMbRole = new MQMonitorRole.Builder("mqMonitorNoMqMbRole", tasResolver)
                .installDir("c:\\sw\\agent_nomq_mb").build();
        mqServerTestbedMachine.addRole(mqMonitorNoMqMbRole);
        gathererMap.put("mq_monitor_nomq_mb.zip", mqMonitorNoMqMbRole.getMqMonitorInstallDir() + "\\logs");

        // MQ MB
        // PREDEPLOYED
        MQMonitorRole mqMonitorMqMbRole = new MQMonitorRole.Builder("mqMonitorMqMbRole", tasResolver)
                .installDir("c:\\sw\\agent_mq_mb").build();
        mqServerTestbedMachine.addRole(mqMonitorMqMbRole);
        gathererMap.put("mq_monitor_mq_mb.zip", mqMonitorMqMbRole.getMqMonitorInstallDir() + "\\logs");

        ////////////////////////////
        // DEPLOY APP SERVER
        ////////////////////////////

        // PREDEPLOYED
        Websphere85Role was85Role = new Websphere85Role.Builder(WAS_85_ROLE_ID, tasResolver)
                .version(Websphere85Version.VER_85_JAVA7)
                .installWasLocation(DEFAULT_WAS_INSTALL_PATH + "\\websphere85")
                .installManagerLocation(DEFAULT_WAS_INSTALL_PATH + "\\websphere85_manager")
                .imSharedLocation(DEFAULT_WAS_INSTALL_PATH + "\\IMShared")
                .hostName("localhost") // todo put this machine hostname
                .predeployed(true)
                .build();
        appServerTestbedMachine.addRole(was85Role);
        gathererMap.put("app_server_was.zip", was85Role.getProfilePath() + "\\logs");

        ////////////////////////////
        // DEPLOY TYPEPERF
        ////////////////////////////

        // no MQ
        TypeperfRole perfMonitorNoMqRole = new TypeperfRole
                .Builder("perfMonitorNoMqRole", tasResolver)
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuNoMq.csv").runTime(DEFAULT_RUN_TIME)
                .copyResultsDestinationDir(testResultsShare)
                .copyResultsDestinationFileName("cpu_nomq.csv")
                .build();
        appServerTestbedMachine.addRole(perfMonitorNoMqRole);

        // MQ no MB
        TypeperfRole perfMonitorMqNoMbRole = new TypeperfRole
                .Builder("perfMonitorMqNoMbRole", tasResolver)
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuMqNoMb.csv").runTime(DEFAULT_RUN_TIME)
                .copyResultsDestinationDir(testResultsShare)
                .copyResultsDestinationFileName("cpu_mq_nomb.csv")
                .build();
        appServerTestbedMachine.addRole(perfMonitorMqNoMbRole);

        // no MQ MB
        TypeperfRole perfMonitorNoMqMbRole = new TypeperfRole
                .Builder("perfMonitorNoMqMbRole", tasResolver)
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuNoMqMb.csv").runTime(DEFAULT_RUN_TIME)
                .copyResultsDestinationDir(testResultsShare)
                .copyResultsDestinationFileName("cpu_nomq_mb.csv")
                .build();
        appServerTestbedMachine.addRole(perfMonitorNoMqMbRole);

        // MQ MB
        TypeperfRole perfMonitorMqMbRole = new TypeperfRole
                .Builder("perfMonitorMqMbRole", tasResolver)
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuMqMb.csv").runTime(DEFAULT_RUN_TIME)
                .copyResultsDestinationDir(testResultsShare)
                .copyResultsDestinationFileName("cpu_mq_mb.csv")
                .build();
        appServerTestbedMachine.addRole(perfMonitorMqMbRole);

        ///////////////////////////////////////////
        // DEPLOY JMETER LOG CONVERTER
        ///////////////////////////////////////////

        // no MQ
        JMeterLogConverterRole jmeterLogConverterNoMqRole = new JMeterLogConverterRole
                .Builder("jmeterLogConverterNoMqRoleId", tasResolver)
                .outputFileName("C:\\automation\\test_results\\jmeterNoMq.modified.csv")
                .build();
        loadTestbedMachine.addRole(jmeterLogConverterNoMqRole);

        // MQ no MB
        JMeterLogConverterRole jmeterLogConverterMqNoMbRole = new JMeterLogConverterRole
                .Builder("jmeterLogConverterMqNoMbRoleId", tasResolver)
                .outputFileName("C:\\automation\\test_results\\jmeterMqNoMb.modified.csv")
                .build();
        loadTestbedMachine.addRole(jmeterLogConverterMqNoMbRole);

        // no MQ MB
        JMeterLogConverterRole jmeterLogConverterNoMqMbRole = new JMeterLogConverterRole
                .Builder("jmeterLogConverterNoMqMbRoleId", tasResolver)
                .outputFileName("C:\\automation\\test_results\\jmeterNoMqMb.modified.csv")
                .build();
        loadTestbedMachine.addRole(jmeterLogConverterNoMqMbRole);

        // MQ MB
        JMeterLogConverterRole jmeterLogConverterMqMbRole = new JMeterLogConverterRole
                .Builder("jmeterLogConverterMqMbRoleId", tasResolver)
                .outputFileName("C:\\automation\\test_results\\jmeterMqMb.modified.csv")
                .build();
        loadTestbedMachine.addRole(jmeterLogConverterMqMbRole);

        ///////////////////////////////////////////
        // DEPLOY JMETER
        ///////////////////////////////////////////

        Map<String, String> params = new HashMap<>();
        params.put("appServerHost", tasResolver.getHostnameById(was85Role.getRoleId()));
        params.put("testDurationInSeconds", String.valueOf(30));
        params.put("testWarmupInSeconds", String.valueOf(5));

        // no MQ
        JMeterRole jmeterNoMqRole = new JMeterRole
                .Builder("jmeterNoMqRoleId", tasResolver)
                .deploySourcesLocation("c:\\sw\\jmeter")
                .scriptFilePath("C:\\automation\\deployed\\client\\mqloanapp_load.jmx")
                .outputJtlFile("C:\\automation\\test_results" + "\\" + "jmeterNoMq.jtl")
                .outputLogFile("C:\\automation\\test_results" + "\\" + "jmeterNoMq.log")
                .deleteOutputLogsBeforeRun()
                .params(params)
                .jmeterLogConverter(jmeterLogConverterNoMqRole)
                .copyResultsDestinationDir(testResultsShare)
                .copyResultsDestinationJtlFileName("jmeter_nomq.modified.csv")
                //.predeployed(predeployed)
                .build();
        loadTestbedMachine.addRole(jmeterNoMqRole);

        // MQ no MB
        JMeterRole jmeterMqNoMbRole = new JMeterRole
                .Builder("jmeterMqNoMbRoleId", tasResolver)
                .deploySourcesLocation("c:\\sw\\jmeter")
                .scriptFilePath("C:\\automation\\deployed\\client\\mqloanapp_load.jmx")
                .outputJtlFile("C:\\automation\\test_results" + "\\" + "jmeterMqNoMb.jtl")
                .outputLogFile("C:\\automation\\test_results" + "\\" + "jmeterMqNoMb.log")
                .deleteOutputLogsBeforeRun()
                .params(params)
                .jmeterLogConverter(jmeterLogConverterMqNoMbRole)
                .copyResultsDestinationDir(testResultsShare)
                .copyResultsDestinationJtlFileName("jmeter_mq_nomb.modified.csv")
                //.predeployed(predeployed)
                .build();
        loadTestbedMachine.addRole(jmeterMqNoMbRole);

        // no MQ MB
        JMeterRole jmeterNoMqMbRole = new JMeterRole
                .Builder("jmeterNoMqMbRoleId", tasResolver)
                .deploySourcesLocation("c:\\sw\\jmeter")
                .scriptFilePath("C:\\automation\\deployed\\client\\mqloanapp_load.jmx")
                .outputJtlFile("C:\\automation\\test_results" + "\\" + "jmeterNoMqMb.jtl")
                .outputLogFile("C:\\automation\\test_results" + "\\" + "jmeterNoMqMb.log")
                .deleteOutputLogsBeforeRun()
                .params(params)
                .jmeterLogConverter(jmeterLogConverterNoMqMbRole)
                .copyResultsDestinationDir(testResultsShare)
                .copyResultsDestinationJtlFileName("jmeter_nomq_mb.modified.csv")
                //.predeployed(predeployed)
                .build();
        loadTestbedMachine.addRole(jmeterNoMqMbRole);

        // MQ MB
        JMeterRole jmeterMqMbRole = new JMeterRole
                .Builder("jmeterMqMbRoleId", tasResolver)
                .deploySourcesLocation("c:\\sw\\jmeter")
                .scriptFilePath("C:\\automation\\deployed\\client\\mqloanapp_load.jmx")
                .outputJtlFile("C:\\automation\\test_results" + "\\" + "jmeterMqMb.jtl")
                .outputLogFile("C:\\automation\\test_results" + "\\" + "jmeterMqMb.log")
                .deleteOutputLogsBeforeRun()
                .params(params)
                .jmeterLogConverter(jmeterLogConverterNoMqMbRole)
                .copyResultsDestinationDir(testResultsShare)
                .copyResultsDestinationJtlFileName("jmeter_mq_mb.modified.csv")
                //.predeployed(predeployed)
                .build();
        loadTestbedMachine.addRole(jmeterMqMbRole);

        ////////////////////////////
        /// Deploy MQ server
        ////////////////////////////

        // PREDEPLOYED
        IBMMQRole ibmMqRole = new IBMMQRole.Builder(MQ_TESTBED_ID, tasResolver)
                .installPath("C:\\SW\\WebSphere MQ\\")
                .version(MQExplorerVersion.VER_75)
                .createQueue(true)
                .queueMap(qNamesAsList)
                .portMap(qmanagerPort)
                .predeployed(true)
                .build();
        mqServerTestbedMachine.addRole(ibmMqRole);

        /////////////////////////////
        /// Install JMS Loan App
        /////////////////////////////

        // PREDEPLOYED
        MqJmsAppRole mqJmsAppRole = new MqJmsAppRole.Builder(WAS_MQAPP_ID, tasResolver)
                .websphereRole(was85Role)
                .ibmmqRole(ibmMqRole)
                .wasIsAutostart(false)
                .nodeName(was85Role.getNodeName())
                .cellName(was85Role.getCellName())
                .profileName(was85Role.getProfileName()).serverName("server1").replyQ(replySOJQueue).creditReplyQ(creditReplySOJQueue)
                .creditRequestQ(creditRequestSOJQueue).requestQ(requestSOJQueue).qmanagerName(qManagerName)
                .predeployed(true)
                .build();
        mqJmsAppRole.after(ibmMqRole, was85Role);
        appServerTestbedMachine.addRole(mqJmsAppRole);

        ClientDeployRole clientDeployRole = new ClientDeployRole.Builder(CLIENT_ID_APPSERVER, tasResolver)
                .build();
        loadTestbedMachine.addRole(clientDeployRole);

        ClientDeployRole clientDeployLoadRole = new ClientDeployRole.Builder(CLIENT_ID_LOAD, tasResolver)
                .build();
        appServerTestbedMachine.addRole(clientDeployLoadRole);

        testBed.addMachine(appServerTestbedMachine, loadTestbedMachine, mqServerTestbedMachine);

        ////////////////////////////
        // DEPLOY JMX MONITOR
        ////////////////////////////

        // no MQ
        JmxMonitorRole jmxMonitorNoMqRole = new JmxMonitorRole
                .Builder("jmxMonitorNoMqRole", tasResolver)
                .outputFileName(JMX_MONITOR_OUTPUT_DIR + "\\memNoMq.csv").runTime(DEFAULT_RUN_TIME)
                .copyResultsDestinationDir(testResultsShare)
                .copyResultsDestinationFileName("mem_nomq.csv")
                .build();
        appServerTestbedMachine.addRole(jmxMonitorNoMqRole);

        // MQ no MB
        JmxMonitorRole jmxMonitorMqNoMbRole = new JmxMonitorRole
                .Builder("jmxMonitorMqNoMbRole", tasResolver)
                .outputFileName(JMX_MONITOR_OUTPUT_DIR + "\\memMqNoMb.csv").runTime(DEFAULT_RUN_TIME)
                .copyResultsDestinationDir(testResultsShare)
                .copyResultsDestinationFileName("mem_mq_nomb.csv")
                .build();
        appServerTestbedMachine.addRole(jmxMonitorMqNoMbRole);

        // no MQ MB
        JmxMonitorRole jmxMonitorNoMqMbRole = new JmxMonitorRole
                .Builder("jmxMonitorNoMqMbRole", tasResolver)
                .outputFileName(JMX_MONITOR_OUTPUT_DIR + "\\memNoMqMb.csv").runTime(DEFAULT_RUN_TIME)
                .copyResultsDestinationDir(testResultsShare)
                .copyResultsDestinationFileName("mem_nomq_mb.csv")
                .build();
        appServerTestbedMachine.addRole(jmxMonitorNoMqMbRole);

        // MQ MB
        JmxMonitorRole jmxMonitorMqMbRole = new JmxMonitorRole
                .Builder("jmxMonitorMqMbRole", tasResolver)
                .outputFileName(JMX_MONITOR_OUTPUT_DIR + "\\memMqMb.csv").runTime(DEFAULT_RUN_TIME)
                .copyResultsDestinationDir(testResultsShare)
                .copyResultsDestinationFileName("mem_mq_mb.csv")
                .build();
        appServerTestbedMachine.addRole(jmxMonitorMqMbRole);

        ////////////////////////////
        // DEPLOY LOGS GATHERER
        ////////////////////////////

        LogsGathererRole logsGathererWasRole = new LogsGathererRole.Builder("logsGathererWasRole", tasResolver)
                .targetDir(LOGS_GATHERER_OUTPUT_DIR)
                .filesMapping(gathererMap)
                .deleteSource(true)
                .addTimestamp(true)
                .ignoreDeletionErrors(true)
                .ignoreEmpty(true)
                .build();
        appServerTestbedMachine.addRole(logsGathererWasRole);

        LogsGathererRole logsGathererMqRole = new LogsGathererRole.Builder("logsGathererMqRole", tasResolver)
                .targetDir(LOGS_GATHERER_OUTPUT_DIR)
                .filesMapping(gathererMap)
                .deleteSource(true)
                .addTimestamp(true)
                .ignoreDeletionErrors(true)
                .ignoreEmpty(true)
                .build();
        mqServerTestbedMachine.addRole(logsGathererMqRole);

        return testBed;
    }
}