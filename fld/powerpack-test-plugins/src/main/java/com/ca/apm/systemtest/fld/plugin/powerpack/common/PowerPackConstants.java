package com.ca.apm.systemtest.fld.plugin.powerpack.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class to keep all constants used in PowerPack performance test classes.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class PowerPackConstants {
    
    //Process parameter names
    public static final String SAMPLES_COUNT_PARAM_NAME = "samplesCount";
    public static final String SAMPLE_INTERVAL_PARAM_NAME = "sampleIntervalMillis";
    public static final String LOG_DIR_PARAM_NAME = "logDir";
    public static final String LOG_DIR_SERVER_PARAM_NAME = "logDirServer";
    public static final String LOGS_ARCHIVE_FOLDER_PARAM_NAME = "logsArchiveFolder";
    public static final String GROUPED_LOGS_FOLDER_PARAM_NAME = "groupedLogsFolder";
    public static final String RESULTS_FOLDER_PARAM_NAME = "resultsFolder";
    public static final String JMX_PORT_PARAM_NAME = "jmxPort";
    public static final String APP_SERVER_HOST_PARAM_NAME = "appServerHost";
    public static final String APP_SERVER_PORT_PARAM_NAME = "appServerPort";
    public static final String JMX_METRICS_PARAM_NAME = "jmxMetrics";
    public static final String JMX_LOG_FILE_NAME_PARAM_NAME = "jmxLogFileName";
    public static final String TYPE_PERF_LOG_FILE_NAME_PARAM_NAME = "typePerfLogFileName";
    public static final String JSTAT_LOG_FILE_NAME_PARAM_NAME = "jstatLogFileName";
    public static final String RESULT_REPORT_FILE_NAME_PARAM_NAME = "resultReportFileName";
    public static final String PROCESS_INSTANCE_NAME_PARAM_NAME = "processInstanceName";
    public static final String PROCESS_NAME_PARAM_NAME = "processName";
    public static final String JAVA_PROCESS_NAME_PARAM_NAME = "javaProcessName";
    public static final String PROCESS_COMMAND_LINE_PATTERN_PARAM_NAME = "processCommandLinePattern";
    public static final String RUN_TYPEPERF_MONITORING_PARAM_NAME = "runTypePerfMonitoring";
    public static final String RUN_JMX_MONITORING_PARAM_NAME = "runJmxMonitoring";
    public static final String RUN_JSTAT_MONITORING_PARAM_NAME = "runJstatMonitoring";
    public static final String BUILD_TYPEPERF_REPORT_PARAM_NAME = "buildTypePerfReport";
    public static final String BUILD_JMX_REPORT_PARAM_NAME = "buildJmxReport";
    public static final String BUILD_JMETER_REPORT_PARAM_NAME = "buildJmeterReport";
    public static final String BUILD_JSTAT_REPORT_PARAM_NAME = "buildJstatReport";
    public static final String TYPE_PERF_COUNTERS_PARAM_NAME = "typePerfCounters";
    public static final String MONITORED_PROCESS_ID_PARAM_NAME = "monitoredProcessId";
    public static final String LOAD_ORCHESTRATOR_CONTROLLER_NODE_PARAM_NAME = "loNode";
    public static final String ORACLE_NODE_PARAM_NAME = "oracleNode";
    public static final String SERVER_TYPE_PARAM_NAME = "serverType";
    public static final String RECREATE_DB_SCRIPT_ARCHIVE_URL_PARAM_NAME = "reCreateDbScriptArchiveUrl";
    public static final String REPORT_TEMPLATE_URL_PARAM_NAME = "reportResultsFileTemplateUrl";
    public static final String JMETER_DOWNLOAD_URL_PARAM_NAME = "jmeterDistroHttpDownloadMethodUrl";
    public static final String DIRECT_INTROSCOPE_AGENT_DOWNLOAD_URL_PARAM_NAME = "directArtifactDownloadLink";
    public static final String JMETER_DOWNLOAD_DIRECTORY_PARAM_NAME = "jmeterDownloadDirectory";
    public static final String JMETER_SCENARIO_URL_PARAM_NAME = "jMeterScenarioUrl";
    public static final String JMETER_TASK_NAME_PARAM_NAME = "jMeterTaskName";
    public static final String JMETER_TASK_FINISHED_PARAM_NAME = "jMeterTaskFinished";
    public static final String JMETER_IS_INSTALLED_PARAM_NAME = "jMeterInstalled";
    public static final String JMETER_COLLECT_FILE_EXTENSIONS_PARAM_NAME = "jmeterCollectFileExtensions";
    public static final String TEST_WARMUP_PERIOD_IN_SECONDS_PARAM_NAME = "testWarmupInSeconds";
    public static final String TEST_RAMPUP_PERIOD_IN_SECONDS_PARAM_NAME = "testRampupInSeconds";
    public static final String TEST_DURATION_PERIOD_IN_SECONDS_PARAM_NAME = "testDurationInSeconds";
    public static final String JMETER_NODE_PARAM_NAME = "jmeterNode";
    public static final String JMETER_NUMBER_OF_THREADS_PARAM_NAME = "testNumberOfCVUS";
    public static final String JMETER_TEST_CYCLE_DELAY_PARAM_NAME = "cycleDelay";    
    public static final String JMETER_LOOP_COUNT_PARAM_NAME = "loops";
    public static final String TEST_TYPE_PARAM_NAME = "testType";
    public static final String ARTIFACT_SPECIFICATION_PARAM_NAME = "artifactSpecification";
    public static final String AGENT_NAME_PARAM_NAME = "agentName";
    public static final String AGENT_VERSION_PARAM_NAME = "agentVersion";
    public static final String RUN_NEW_AGENT_WITH_POWER_PACK_TEST_PARAM_NAME = "runNewAgentWithPPTest";
    public static final String RUN_NEW_AGENT_TEST_PARAM_NAME = "runNewAgentTest";
    public static final String RUN_NO_AGENT_TEST_PARAM_NAME = "runNoAgentTest";
    public static final String RUN_OLD_AGENT_WITH_POWER_PACK_TEST_PARAM_NAME = "runOldAgentWithPPTest";
    public static final String RUN_OLD_AGENT_TEST_PARAM_NAME = "runOldAgentTest";
    public static final String MOM_NODE_PARAM_NAME = "momNode";
    public static final String MOM_PORT_PARAM_NAME = "momPort";
    public static final String BRTM_EXTENSION_PARAM_NAME = "brtmExtension";
    public static final String ADDITIONAL_AGENT_PROFILE_PROPERTIES = "additionalAgentProfileProperties";
    public static final String AGENT_PROBE_DIRECTIVES = "agentProbeDirectives";
    public static final String AGENT_EXTRA_MODULES = "agentExtraModules";
    public static final String AGENT_EM_INVESTIGATOR_GROUP_NAME_PARAM_NAME = "agentEMInvestigatorGroupName";
    
    //Default names and values
    public static final int DEFAULT_SAMPLES_COUNT = 1000;
    public static final int DEFAULT_SAMPLES_INTERVAL_MILLIS = 60000;//60 seconds
    public static final String DEFAULT_TYPE_PERF_LOG_FILE_NAME = "typePerf.csv";
    public static final String DEFAULT_JSTAT_LOG_FILE_NAME = "jstat.log";
    public static final String DEFAULT_JMX_LOG_FILE_NAME = "jmx.csv";
    public static final String DEFAULT_TYPE_PERF_COUNTERS_FILE_NAME = "typePerfCounters.txt";
    public static final String DEFAULT_POWER_PACK_REPORT_TEMPLATE_NAME_NO_EXT = "powerPackReportTemplate";
    public static final String DEFAULT_POWER_PACK_REPORT_TEMPLATE_EXT = ".xls";
    public static final String DEFAULT_POWER_PACK_REPORT_TEMPLATE_NAME = DEFAULT_POWER_PACK_REPORT_TEMPLATE_NAME_NO_EXT + 
        DEFAULT_POWER_PACK_REPORT_TEMPLATE_EXT;
    public static final String DEFAULT_POWER_PACK_FINAL_RESULT_REPORT_FILE_NAME = "ppPerfTestRunReport.xls";
    public static final String DEFAULT_PROCESS_COMMAND_LINE_PATTERN = "-Dcom.sun.management.jmxremote.port=1099";
    public static final String DEFAULT_TYPE_PERF_COUNTERS = "\\Processor(_Total)\\% Processor Time,\\Process({0})\\ID Process";
    
    public static final String DEFAULT_JMX_METRICS =
        "java.lang:type=MemoryPool,name=Java heap|Usage/used,max;" +
            "java.lang:type=GarbageCollector,name=Copy|CollectionCount|CollectionTime;" +
            "java.lang:type=GarbageCollector,name=MarkSweepCompact|CollectionCount|CollectionTime";

    public static final int DEFAULT_JMETER_TEST_CYCLE_DELAY_IN_MILLISECONDS = 1000;    
    public static final int DEFAULT_JMETER_LOOP_COUNT = -1;//infinite loop
    
    public static final List<String> DEFAULT_JMETER_OUTPUT_FILE_SUFFIXES = Collections
        .unmodifiableList(Arrays.asList(".log", ".jtl", ".csv"));

    public static final String AGENT_DOWNLOAD_DIR_NAME = "download";

    public static final String WLS_APP_SERVER_ID_FOR_EM_CONNECTION_CHECK   = "WebLogic";
    public static final String WAS_APP_SERVER_ID_FOR_EM_CONNECTION_CHECK   = "WebSphere";
    public static final String TIBCO_APP_SERVER_ID_FOR_EM_CONNECTION_CHECK = "tibco";
    
    public static final String NO_AGENT_TEST_TYPE = "noAgent";
}
