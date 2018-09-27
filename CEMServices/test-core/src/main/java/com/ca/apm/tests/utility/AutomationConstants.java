package com.ca.apm.tests.utility;

public abstract class AutomationConstants {
	// Agent Constants
	public static final class Agent {

		public static final String DUMMY_PROPERTY = "Spencer is not a Dummy!";

		// AutoProbe Properties //

		// On/Off Switch
		public static final String AUTOPROBE_ENABLE_PROPERTY = "introscope.autoprobe.enable";
		// Custom Log File Location
		public static final String AUTOPROBE_LOG_PATH_PROPERTY = "introscope.autoprobe.logfile";
		// Directives Files
		public static final String AUTOPROBE_DIRECTIVES_FILE_PROPERTY = "introscope.autoprobe.directivesFile";
		// Agent Properties //

		// Hidden
		public static final String CONFIGURATION_OLD_PROPERTY = "introscope.agent.configuration.old";

		public static final String METRIC_COUNT_PROPERTY = "introscope.ext.agent.metric.count";

		public static final String AUTOPROBE_LOGSKIPPEDCLASSES_ENABLED_PROPERTY = "introscope.autoprobe.logskippedclasses.enabled";

		public static final String HOST_NAME_PROPERTY = "introscope.agent.hostName";
		// Logging Configurations
		public static final String LOG_LEVEL = "log4j.logger.IntroscopeAgent";

		public static final String LOG_PATH_PROPERTY = "log4j.appender.logfile.File";

		public static final String LOG_ADDITIVITY_PROPERTY = "log4j.additivity.IntroscopeAgent";

		public static final String LOG_APPENDER_CONSOLE_PROPERTY = "log4j.appender.console";

		public static final String LOG_APPENDER_CONSOLE_LAYOUT_PROPERTY = "log4j.appender.console.layout";

		public static final String LOG_APPENDER_CONSOLE_LAYOUT_CONVERSION_PATTERN_PROPERTY = "log4j.appender.console.layout.ConversionPattern";

		public static final String LOG_APPENDER_CONSOLE_TARGET_PROPERTY = "log4j.appender.console.target";

		public static final String LOG_APPENDER_LOGFILE_PROPERTY = "log4j.appender.logfile";

		public static final String LOG_APPENDER_LOGFILE_LAYOUT_PROPERTY = "log4j.appender.logfile.layout";

		public static final String LOG_APPENDER_LOGFILE_LAYOUT_CONVERSION_PATTERN_PROPERTY = "log4j.appender.logfile.layout.ConversionPattern";

		public static final String LOG_APPENDER_LOGFILE_MAX_BACKUP_INDEX_PROPERTY = "log4j.appender.logfile.MaxBackupIndex";

		public static final String LOG_APPENDER_LOGFILE_MAX_FILESIZE_PROPERTY = "log4j.appender.logfile.MaxFileSize";
		
		//.net agent
		public static final String NATIVE_PROFILER_LOG_FILE_PROPERTY = "introscope.nativeprofiler.logfile";
		
		// Enterprise Manager Connection Order
		public static final String EM_CONNECTION_ORDER_PROPERTY = "introscope.agent.enterprisemanager.connectionorder";
		// Enterprise Manager Locations and Names
		public static final String EM_TRANSPORT_TCP_HOST_PROPERTY = "introscope.agent.enterprisemanager.transport.tcp.host.[key]";

		public static final String EM_TRANSPORT_TCP_PORT_PROPERTY = "introscope.agent.enterprisemanager.transport.tcp.port.[key]";

		public static final String EM_TRANSPORT_TCP_SOCKET_FACTORY_PROPERTY = "introscope.agent.enterprisemanager.transport.tcp.socketfactory.[key]";

		public static final String EM_TRANSPORT_HTTP_PROXY_HOST_PROPERTY = "introscope.agent.enterprisemanager.transport.http.proxy.host";

		public static final String EM_TRANSPORT_HTTP_PROXY_PORT_PROPERTY = "introscope.agent.enterprisemanager.transport.http.proxy.port";

		public static final String EM_TRANSPORT_HTTP_PROXY_USERNAME_PROPERTY = "introscope.agent.enterprisemanager.transport.http.proxy.username";

		public static final String EM_TRANSPORT_HTTP_PROXY_PASSWORD_PROPERTY = "introscope.agent.enterprisemanager.transport.http.proxy.password";

		public static final String EM_TRANSPORT_TCP_TRUST_STORE_PROPERTY = "introscope.agent.enterprisemanager.transport.tcp.truststore.[key]";

		public static final String EM_TRANSPORT_TCP_TRUST_PASSWORD_PROPERTY = "introscope.agent.enterprisemanager.transport.tcp.trustpassword.[key]";

		public static final String EM_TRANSPORT_TCP_KEY_STORE_PROPERTY = "introscope.agent.enterprisemanager.transport.tcp.keystore.[key]";

		public static final String EM_TRANSPORT_TCP_KEY_PASSWORD_PROPERTY = "introscope.agent.enterprisemanager.transport.tcp.keypassword.[key]";

		public static final String EM_TRANSPORT_TCP_CIPHER_SUITES_PROPERTY = "introscope.agent.enterprisemanager.transport.tcp.ciphersuites.[key]";

		// Enterprise Manager Failback Retry Interval
		public static final String EM_FAILBACK_RETRY_INTERVAL_SECONDS_PROPERTY = "introscope.agent.enterprisemanager.failbackRetryIntervalInSeconds";
		// Custom Process Name
		public static final String CUSTOM_PROCESS_NAME_PROPERTY = "introscope.agent.customProcessName";
		// Default Process Name
		public static final String DEFAULT_PROCESS_NAME_PROPERTY = "introscope.agent.defaultProcessName";
		// Agent Name
		public static final String NAME_SYSTEM_PROPERTY_KEY_PROPERTY = "introscope.agent.agentNameSystemPropertyKey";

		public static final String AUTONAMING_ENABLED_PROPERTY = "introscope.agent.agentAutoNamingEnabled";

		public static final String AUTONAMING_MAX_CONNECTION_DELAY_SECONDS_PROPERTY = "introscope.agent.agentAutoNamingMaximumConnectionDelayInSeconds";

		public static final String AUTORENAMING_INTERVAL_MINUTES_PROPERTY = "introscope.agent.agentAutoRenamingIntervalInMinutes";

		public static final String DISABLE_LOGFILE_AUTONAMING_PROPERTY = "introscope.agent.disableLogFileAutoNaming";

		public static final String NAME_PROPERTY = "introscope.agent.agentName";

		public static final String DISPLAY_HOSTNAME_AS_FQDN_PROPERTY = "introscope.agent.display.hostName.as.fqdn";
		// Agent Socket Rate Metrics
		public static final String MANAGED_SOCKETS_RATE_METRICS = "introscope.agent.sockets.reportRateMetrics";
		// Agent Memory Overhead Settings
		public static final String REDUCE_AGENT_MEMORY_OVERHEAD_PROPERTY = "introscope.agent.reduceAgentMemoryOverhead";
		// Agent I/O Socket Metrics
		public static final String IO_SOCKETS_CLIENT_HOSTS_PROPERTY = "introscope.agent.io.socket.client.hosts";

		public static final String IO_SOCKETS_CLIENT_PORTS_PROPERTY = "introscope.agent.io.socket.client.ports";

		public static final String IO_SOCKETS_SERVER_PORTS_PROPERTY = "introscope.agent.io.socket.server.ports";
		// Agent NIO Metrics
		public static final String NIO_DATAGRAM_CLIENT_HOSTS_PROPERTY = "introscope.agent.nio.datagram.client.hosts";

		public static final String NIO_DATAGRAM_CLIENT_PORTS_PROPERTY = "introscope.agent.nio.datagram.client.ports";

		public static final String NIO_DATAGRAM_SERVER_PORTS_PROPERTY = "introscope.agent.nio.datagram.server.ports";

		public static final String NIO_SOCKET_CLIENT_HOSTS_PROPERTY = "introscope.agent.nio.socket.client.hosts";

		public static final String NIO_SOCKET_CLIENT_PORTS_PROPERTY = "introscope.agent.nio.socket.client.ports";

		public static final String NIO_SOCKET_SERVER_PORTS_PROPERTY = "introscope.agent.nio.socket.server.ports";
		// Agent Extensions Directory
		public static final String EXTENSIONS_DIR_PROPERTY = "introscope.agent.extensions.directory";
		// Agent Common Directory
		public static final String COMMON_DIR_PROPERTY = "introscope.agent.common.directory";
		// Agent Thread Priority
		public static final String THREAD_ALL_PRIORITY_PROPERTY = "introscope.agent.thread.all.priority";
		// Cloned Agent Configuration
		public static final String CLONED_AGENT_PROPERTY = "introscope.agent.clonedAgent";
		// Platform Monitor Configuration
		public static final String PLATFORM_MONITOR_SYSTEM_PROPERTY = "introscope.agent.platform.monitor";

		// WebSphere PMI Configuration
		public static final String PMI_ENABLE_PROPERTY = "introscope.agent.pmi.enable";

		public static final String PMI_FILETER_OBJREF_PROPERTY = "introscope.agent.pmi.filter.objref";

		public static final String PMI_ENABLE_THREADPOOL_PROPERTY = "introscope.agent.pmi.enable.threadPoolModule";

		public static final String PMI_ENABLE_SERVLETSESSIONS_PROPERTY = "introscope.agent.pmi.enable.servletSessionsModule";

		public static final String PMI_ENABLE_CONNECTIONPOOL_PROPERTY = "introscope.agent.pmi.enable.connectionPoolModule";

		public static final String PMI_ENABLE_BEAN_PROPERTY = "introscope.agent.pmi.enable.beanModule";

		public static final String PMI_ENABLE_TRANSACTION_PROPERTY = "introscope.agent.pmi.enable.transactionModule";

		public static final String PMI_ENABLE_WEBAPP_PROPERTY = "introscope.agent.pmi.enable.webAppModule";

		public static final String PMI_ENABLE_JVMRUNTIME_PROPERTY = "introscope.agent.pmi.enable.jvmRuntimeModule";

		public static final String PMI_ENABLE_SYSTEM_PROPERTY = "introscope.agent.pmi.enable.systemModule";

		public static final String PMI_ENABLE_CACHE_PROPERTY = "introscope.agent.pmi.enable.cacheModule";

		public static final String PMI_ENABLE_ORBPERF_PROPERTY = "introscope.agent.pmi.enable.orbPerfModule";

		public static final String PMI_ENABLE_J2C_PROPERTY = "introscope.agent.pmi.enable.j2cModule";

		public static final String PMI_ENABLE_WEBSERVICES_PROPERTY = "introscope.agent.pmi.enable.webServicesModule";

		public static final String PMI_ENABLE_WLM_PROPERTY = "introscope.agent.pmi.enable.wlmModule";

		public static final String PMI_ENABLE_WSGW_PROPERTY = "introscope.agent.pmi.enable.wsgwModule";

		public static final String PMI_ENABLE_ALARMMANAGER_PROPERTY = "introscope.agent.pmi.enable.alarmManagerModule";

		public static final String PMI_ENABLE_HAMANAGER_PROPERTY = "introscope.agent.pmi.enable.hamanagerModule";

		public static final String PMI_ENABLE_OBJECTPOOL_PROPERTY = "introscope.agent.pmi.enable.objectPoolModule";

		public static final String PMI_ENABLE_SCHEDULER_PROPERTY = "introscope.agent.pmi.enable.schedulerModule";

		// JMX Configuration
		public static final String JMX_ENABLE_PROPERTY = "introscope.agent.jmx.enable";

		public static final String JMX_MAX_POLLING_DURATION_ENABLE_PROPERTY = "introscope.agent.jmx.maxpollingduration.enable";

		public static final String JMX_RATE_COUNTER_ENABLE_PROPERTY = "introscope.agent.jmx.ratecounter.enable";

		public static final String JMX_RATE_COUNTER_RESET_ENABLE_PROPERTY = "introscope.agent.jmx.ratecounter.reset.enable";

		public static final String JMX_COMPOSITE_DATA_ENABLE_PROPERTY = "introscope.agent.jmx.compositedata.enable";
		// JSR 77 Support
		public static final String JMX_NAME_JSR77_DISABLE_PROPERTY = "introscope.agent.jmx.name.jsr77.disable";

		public static final String JMX_NAME_PRIMARY_KEYS_PROPERTY = "introscope.agent.jmx.name.primarykeys";

		public static final String JMX_NAME_FILTER_PROPERTY = "introscope.agent.jmx.name.filter";

		public static final String JMX_IGNORE_ATTRIBUTES_PROPERTY = "introscope.agent.jmx.ignore.attributes";

		public static final String JMX_EXCLUDE_STRING_METRICS_PROPERTY = "introscope.agent.jmx.excludeStringMetrics";
		// WLDF Configuration
		public static final String WLDF_ENABLE_PROPERTY = "introscope.agent.wldf.enable";
		// LEAKHUNTER
		public static final String LEAKHUNTER_ENABLE_PROPERTY = "introscope.agent.leakhunter.enable";

		public static final String LEAKHUNTER_LOGFILE_PATH_PROPERTY = "introscope.agent.leakhunter.logfile.location";

		public static final String LEAKHUNTER_LOGFILE_APPEND_PROPERTY = "introscope.agent.leakhunter.logfile.append";

		public static final String LEAKHUNTER_LEAK_SENSITIVITY_PROPERTY = "introscope.agent.leakhunter.leakSensitivity";

		public static final String LEAKHUNTER_TIMEOUT_MINUTES_PROPERTY = "introscope.agent.leakhunter.timeoutInMinutes";

		public static final String LEAKHUNTER_COLLECT_ALLOCATION_STACK_TRACES_PROPERTY = "introscope.agent.leakhunter.collectAllocationStackTraces";

		public static final String LEAKHUNTER_IGNORE_PROPERTY = "introscope.agent.leakhunter.ignore.[key]";
		// SQL Agent Configuration
		public static final String SQLAGENT_SQL_TURN_OFF_METRICS_PROPERTY = "introscope.agent.sqlagent.sql.turnoffmetrics";

		public static final String SQLAGENT_SQL_ART_ONLY_PROPERTY = "introscope.agent.sqlagent.sql.artonly";

		public static final String SQLAGENT_SQL_TURN_OFF_TRACE_PROPERTY = "introscope.agent.sqlagent.sql.turnofftrace";

		public static final String SQLAGENT_SQL_RAW_SQL_PROPERTY = "introscope.agent.sqlagent.sql.rawsql";
		// SQL Agent Normalizer extension
		public static final String SQLAGENT_NORMALIZER_EXTENSION = "introscope.agent.sqlagent.normalizer.extension";
		// hidden property
		public static final String SQLAGENT_NORMALIZER_EXTENSION_ERROR_COUNT_PROPERTY = "introscope.agent.sqlagent.normalizer.extension.errorCount";

		// RegexSqlNormalizer extension
		public static final String SQLAGENT_NORMALIZER_REGX_MATCH_FALL_THROUGH_PROPERTY = "introscope.agent.sqlagent.normalizer.regex.matchFallThrough";

		public static final String SQLAGENT_NORMALIZER_REGX_KEYS_PROPERTY = "introscope.agent.sqlagent.normalizer.regex.keys";

		public static final String SQLAGENT_NORMALIZER_REGX_PATTERN_PROPERTY = "introscope.agent.sqlagent.normalizer.regex.[key].pattern";

		public static final String SQLAGENT_NORMALIZER_REGX_REPLACE_ALL_PROPERTY = "introscope.agent.sqlagent.normalizer.regex.[key].replaceAll";

		public static final String SQLAGENT_NORMALIZER_REGX_REPLACE_FORMAT_PROPERTY = "introscope.agent.sqlagent.normalizer.regex.[key].replaceFormat";

		public static final String SQLAGENT_NORMALIZER_REGX_CASE_SENSITIVE_PROPERTY = "introscope.agent.sqlagent.normalizer.regex.[key].caseSensitive";

		// Cross JVM Tracing
		public static final String WEBLOGIC_CROSS_JVM_PROPERTY = "introscope.agent.weblogic.crossjvm";

		public static final String WEBSPHERE_CROSS_JVM_PROPERTY = "introscope.agent.websphere.crossjvm";

		// Agent Metric Clamp Configuration
		public static final String METRIC_CLAMP_PROPERTY = "introscope.agent.metricClamp";
		// Transaction Tracer Configuration
		public static final String TT_USERID_METHOD = "introscope.agent.transactiontracer.userid.method";

		public static final String TT_USERID_KEY = "introscope.agent.transactiontracer.userid.key";
		
		public static final String TT_DISABLEHTTP = "introscope.agent.asp.disableHttpProperties";

		public static final String TT_HTTPREQUEST_HEADERS = "introscope.agent.transactiontracer.parameter.httprequest.headers";

		public static final String TT_HTTPREQUEST_PARAMETERS = "introscope.agent.transactiontracer.parameter.httprequest.parameters";

		public static final String TT_HTTPSESSION_ATTRIBUTES = "introscope.agent.transactiontracer.parameter.httpsession.attributes";

		public static final String TT_PARAMETER_CAPTURE_SESSION_ID_PROPERTY = "introscope.agent.transactiontracer.parameter.capture.sessionid";

		public static final String TT_COMPONENT_COUNT_CLAMP_PROPERTY = "introscope.agent.transactiontrace.componentCountClamp";

		public static final String TT_HEAD_FILTER_CLAMP_PROPERTY = "introscope.agent.transactiontrace.headFilterClamp";

		public static final String TT_CROSS_PROCESS_COMPRESSION_PROPERTY = "introscope.agent.crossprocess.compression";

		public static final String TT_CROSS_PROCESS_COMPRESSION_MIN_LIMIT_PROPERTY = "introscope.agent.crossprocess.compression.minlimit";

		public static final String TT_CROSS_PROCESS_CORRELATION_ID_MAX_LIMIT_PROPERTY = "introscope.agent.crossprocess.correlationid.maxlimit";

		public static final String TT_SAMPLING_ENABLED_PROPERTY = "introscope.agent.transactiontracer.sampling.enabled";

		public static final String TT_TAIL_FILTER_PROPAGATE_PROPERTY = "introscope.agent.transactiontracer.tailfilterPropagate.enable";

		public static final String TT_CLAMP_PROPERTY = "introscope.agent.ttClamp";
		
		//hidden
		public static final String TT_TRACE_DEEP_CALLS = "introscope.agent.HttpServletTracer.traceDeepCalls";
		
		public static final String TT_TRACE_DEEP_COUNT = "introscope.agent.HttpServletTracer.traceDeepCount";
		
		// URL Grouping Configuration
		public static final String URLGROUP_KEYS = "introscope.agent.urlgroup.keys";

		public static final String URLGROUP_PATHPREFIX = "introscope.agent.urlgroup.group.[key].pathprefix";

		public static final String URLGROUP_FORMAT = "introscope.agent.urlgroup.group.[key].format";
		// Error Detector Configuration
		public static final String ERROR_SNAPSHOTS_ENABLE_PROPERTY = "introscope.agent.errorsnapshots.enable";

		public static final String ERROR_SNAPSHOTS_THROTTLE_PROPERTY = "introscope.agent.errorsnapshots.throttle";

		public static final String ERROR_SNAPSHOTS_IGNORE_PROPERTY = "introscope.agent.errorsnapshots.ignore.[key]";

		public static final String STALL_VALUE = "introscope.agent.stalls.thresholdseconds";

		public static final String STALLS_RESOLUTION_SECONDS_PROPERTY = "introscope.agent.stalls.resolutionseconds";
		// TT Sampling
		public static final String TTRACER_SAMPLING_PER_INTERVAL_COUNT_PROPERTY = "introscope.agent.transactiontracer.sampling.perinterval.count";

		public static final String TTRACER_SAMPLING_INTERVAL_SECONDS_PROPERTY = "introscope.agent.transactiontracer.sampling.interval.seconds";
		// Remote Configuration Settings
		public static final String REMOTE_AGENT_CONFIGURATION_ENABLED_PROPERTY = "introscope.agent.remoteagentconfiguration.enabled";

		public static final String REMOTE_AGENT_CONFIGURATION_ALLOWED_FILES_PROPERTY = "introscope.agent.remoteagentconfiguration.allowedFiles";
		// Bootstrap Classes Instrumentation Manager
		public static final String BOOTSTRAP_CLASS_MANAGER_ENABLED_PROPERTY = "introscope.bootstrapClassesManager.enabled";

		public static final String BOOTSTRAP_CLASS_MANAGER_WAIT_AT_STARTUP_PROPERTY = "introscope.bootstrapClassesManager.waitAtStartup";
		// Remote Dynamic Instrumentation Settings
		public static final String REMOTE_DI_ENABLED_PROPERTY = "introscope.agent.remoteagentdynamicinstrumentation.enabled";
		// Dynamic Instrumentation Settings
		public static final String AUTOPROBE_DI_ENABLED_PROPERTY = "introscope.autoprobe.dynamicinstrument.enabled";

		public static final String AUTOPROBE_DI_POLL_INTERVAL_MINUTES_PROPERTY = "introscope.autoprobe.dynamicinstrument.pollIntervalMinutes";

		public static final String AUTOPROBE_DI_CLASS_FILE_SIZE_LIMIT_MEGS_PROPERTY = "introscope.autoprobe.dynamicinstrument.classFileSizeLimitInMegs";

		public static final String AUTOPROBE_DYNAMIC_LIMIT_REDEFINED_CLASS_PER_BATCH = "introscope.autoprobe.dynamic.limitRedefinedClassesPerBatchTo";
		// Deep Inheritance Settings
		public static final String AUTOPROBE_DEEP_INHERITANCE_ENABLED_PROPERTY = "introscope.autoprobe.deepinheritance.enabled";
		// Multiple Inheritance Settings
		public static final String AUTOPROBE_HIERARCHY_SUPPORT_ENABLED_PROPERTY = "introscope.autoprobe.hierarchysupport.enabled";

		public static final String AUTOPROBE_HIERARCHY_SUPPORT_RUN_ONCE_ONLY_PROPERTY = "introscope.autoprobe.hierarchysupport.runOnceOnly";

		public static final String AUTOPROBE_HIERARCHY_SUPPORT_POLL_INTERVAL_MINUTES_PROPERTY = "introscope.autoprobe.hierarchysupport.pollIntervalMinutes";

		public static final String AUTOPROBE_HIERARCHY_SUPPORT_EXECUTION_COUNT_PROPERTY = "introscope.autoprobe.hierarchysupport.executionCount";

		public static final String AUTOPROBE_HIERARCHY_SUPPORT_DISABLE_LOGGING_PROPERTY = "introscope.autoprobe.hierarchysupport.disableLogging";

		public static final String AUTOPROBE_HIERARCHY_SUPPORT_DISABLE_DIRECTIVES_CHANGE_PROPERTY = "introscope.autoprobe.hierarchysupport.disableDirectivesChange";

		public static final String LOG_ADDITIVITY_INHERITANCE_PROPERTY = "log4j.additivity.IntroscopeAgent.inheritance";

		public static final String LOG_LEVEL_INHERITANCE_PROPERTY = "log4j.logger.IntroscopeAgent.inheritance";

		public static final String LOG_PATH_INHERITANCE_PROPERTY = "log4j.appender.pbdlog.File";

		public static final String LOG_APPENDER_INHERITANCE_PROPERTY = "log4j.appender.pbdlog";

		public static final String LOG_APPENDER_INHERITANCE_LAYOUT_PROPERTY = "log4j.appender.pbdlog.layout";

		public static final String LOG_APPENDER_INHERITANCE_LAYOUT_CONVERSION_PATTERN_PROPERTY = "log4j.appender.pbdlog.layout.ConversionPattern";
		// Agent Metric Aging
		public static final String METRIC_AGING_TURN_ON_PROPERTY = "introscope.agent.metricAging.turnOn";

		public static final String METRIC_AGING_HEARTBEAT_INTERVAL_PROPERTY = "introscope.agent.metricAging.heartbeatInterval";

		//hidden property (to make heartbeat happen faster than default 2min)
		public static final String METRIC_AGING_HEARTBEAT_HIDDEN_BASE_PROPERTY = "introscope.agent.metricAging.heartbeatInterval.hidden.base";

		public static final String METRIC_AGING_DATA_CHUNK_PROPERTY = "introscope.agent.metricAging.dataChunk";

		public static final String METRIC_AGING_NUMBER_TIMESLICES_PROPERTY = "introscope.agent.metricAging.numberTimeslices";

		public static final String METRIC_AGING_METRIC_EXCLUDE_IGNORE_PROPERTY = "introscope.agent.metricAging.metricExclude.ignore.[key]";
		// Servlet Header Decorator
		public static final String SERVLET_HEADER_DECORATOR_ENABLED_PROPERTY = "introscope.agent.decorator.enabled";

		public static final String SERVLET_HEADER_DECORATOR_SECURITY_PROPERTY = "introscope.agent.decorator.security";
		// ChangeDetector configuration properties
		public static final String CD_ENABLED_PROPERTY = "introscope.changeDetector.enable";

		public static final String CD_ROOT_DIR_PROPERTY = "introscope.changeDetector.rootDir";

		public static final String CD_ISENGARD_STARTUP_WAIT_TIME_SECONDS_PROPERTY = "introscope.changeDetector.isengardStartupWaitTimeInSec";

		public static final String CD_WAIT_TIME_BETWEEN_RECONNECT_SECONDS_PROPERTY ="introscope.changeDetector.waitTimeBetweenReconnectInSec";

		public static final String CD_AGENT_ID_PROPERTY = "introscope.changeDetector.agentID";

		public static final String CD_PROFILE_PROPERTY = "introscope.changeDetector.profile";

		public static final String CD_PROFILE_DIR_PROPERTY = "introscope.changeDetector.profileDir";

		public static final String CD_COMPRESS_ENTRIES_ENABLE_PROPERTY = "introscope.changeDetector.compressEntries.enable";

		public static final String CD_COMPRESS_ENTRIES_BATCH_SIZE_PROPERTY = "introscope.changeDetector.compressEntries.batchSize";

		//deprecated
		public static final String CD_AGENT_NAME_PROPERTY = "introscope.changeDetector.agentName";

		// Application Map Agent Side
		public static final String APPMAP_ENABLED_PROPERTY = "introscope.agent.appmap.enabled";

		public static final String APPMAP_METRICS_ENABLED_PROPERTY = "introscope.agent.appmap.metrics.enabled";

		public static final String APPMAP_CATALYST_INTEGRATION_ENABLED_PROPERTY = "introscope.agent.appmap.catalystIntegration.enabled";

		public static final String APPMAP_QUEUE_SIZE_PROPERTY = "introscope.agent.appmap.queue.size";

		public static final String APPMAP_QUEUE_PERIOD_PROPERTY = "introscope.agent.appmap.queue.period";

		public static final String APPMAP_INTERMEDIATE_NODES_ENABLED_PROPERTY = "introscope.agent.appmap.intermediateNodes.enabled";

		public static final String APPMAP_LEVELS_ENABLED_PROPERTY = "introscope.agent.appmap.levels.enabled";

		public static final String APPMAP_OWNERS_ENABLED_PROPERTY = "introscope.agent.appmap.owners.enabled";
		// Application Map and Socket
		public static final String SOCKETS_MANAGED_REPORT_TO_APPMAP_PROPERTY = "introscope.agent.sockets.managed.reportToAppmap";

		public static final String SOCKETS_MANAGED_REPORT_CLASS_APP_EDGE_PROPERTY = "introscope.agent.sockets.managed.reportClassAppEdge";

		public static final String SOCKETS_MANAGED_REPORT_METHOD_APP_EDGE_PROPERTY = "introscope.agent.sockets.managed.reportMethodAppEdge";

		public static final String SOCKETS_MANAGED_REPORT_CLASS_BT_EDGE_PROPERTY = "introscope.agent.sockets.managed.reportClassBTEdge";

		public static final String SOCKETS_MANAGED_REPORT_METHOD_BT_EDGE_PROPERTY = "introscope.agent.sockets.managed.reportMethodBTEdge";
		// Business Recording
		public static final String BIZ_RECORDING_ENABLED_PROPERTY = "introscope.agent.bizRecording.enabled";

		// hidden
		public static final String BIZ_RECORDING_TIM_COMPATIBLE_PROPERTY = "introscope.agent.bizRecording.timCompatible";

		public static final String BIZ_DEF_MATCH_POST_PROPERTY = "introscope.agent.bizdef.matchPost";
		// Garbage collection and Memory Monitoring
		public static final String GC_MONITOR_ENABLE_PROPERTY = "introscope.agent.gcmonitor.enable";
		// Thread Dump Collection
		public static final String THREAD_DUMP_ENABLE_PROPERTY = "introscope.agent.threaddump.enable";

		public static final String THREAD_DUMP_MAX_STACK_ELEMENTS_PROPERTY = "introscope.agent.threaddump.MaxStackElements";

		public static final String THREAD_DUMP_DEADLOCK_POLLER_ENABLE_PROPERTY = "introscope.agent.threaddump.deadlockpoller.enable";

		public static final String THREAD_DUMP_DEADLOCK_POLLER_INTERVAL_PROPERTY = "introscope.agent.threaddump.deadlockpollerinterval";
		// Primary network interface selector
		public static final String PRIMARY_NET_INTERFACE_NAME_PROPERTY = "introscope.agent.primary.net.interface.name";
		// Default Backend Legacy
		public static final String CONFIGURATION_DEFAULT_BACKENDS_LEGACY = "introscope.agent.configuration.defaultbackends.legacy";
		
		public static final String DOTNET_MONITOR_APPS = "introscope.agent.dotnet.monitorApplications";


		public static final class Logging {

			// Agent Log Messages

			public static final String STARTUP_MESSAGE = "Introscope Agent startup complete";

			public static final String LEAKHUNTER_STARTUP_MESSAGE = "Identified extension Introscope LeakHunter";

			public static final String EM_CONNECTED_MESSAGE = "Connected Agent to the Introscope Enterprise Manager";

			public static final String PMI_ACTIVATED_MESSAGE = "PMI data collection activated";

			// TODO: ??? NO TRANS, find usage
			public final static String NO_TRANS_TRACE_COLLECTED = "No transaction traces collected.";
			// TODO: find value for this variable
			public static final String EM_CONNECTED_FAIL_MESSAGE = "TBD";

		}

	}

	public static final class EPA{

		// Logging Configuration
		public static final String LOG_LEVEL = "log4j.logger.EPAgent";

		public static final String LOG_PATH_PROPERTY = "log4j.appender.logfile.File";

		public static final String LOG_ADDITIVITY_EPAGENT_PROPERTY = "log4j.additivity.EPAgent";

		public static final String LOG_ADDITIVITY_ISCOPE_AGENT_PROPERTY = "log4j.additivity.IntroscopeAgent";

		public static final String LOG_APPENDER_CONSOLE_PROPERTY = "log4j.appender.console";

		public static final String LOG_APPENDER_CONSOLE_LAYOUT_PROPERTY = "log4j.appender.console.layout";

		public static final String LOG_APPENDER_CONSOLE_LAYOUT_CONVERSION_PATTERN_PROPERTY = "log4j.appender.console.layout.ConversionPattern";

		public static final String LOG_APPENDER_LOGFILE_PROPERTY = "log4j.appender.logfile";

		public static final String LOG_APPENDER_LOGFILE_LAYOUT_PROPERTY = "log4j.appender.logfile.layout";

		public static final String LOG_APPENDER_LOGFILE_LAYOUT_CONVERSION_PATTERN_PROPERTY = "log4j.appender.logfile.layout.ConversionPattern";

		public static final String LOG_APPENDER_LOGFILE_MAX_BACKUP_INDEX_PROPERTY = "log4j.appender.logfile.MaxBackupIndex";

		public static final String LOG_APPENDER_LOGFILE_MAX_FILESIZE_PROPERTY = "log4j.appender.logfile.MaxFileSize";

		// EPAgent Configuration
		public static final String NETWORK_DATA_PORT_PROPERTY = "introscope.epagent.config.networkDataPort";

		public static final String HTTP_SERVER_PORT_PROPERTY = "introscope.epagent.config.httpServerPort";

		public static final String STALLED_STATELESS_PLUGIN_TIMEOUT_SECS_PROPERTY = "introscope.epagent.config.stalledStatelessPluginTimeoutInSeconds";

		// Plugins
		public static final String PLUGIN_STATEFUL_NAMES_PROPERTY = "introscope.epagent.plugins.stateful.names";

		public static final String PLUGIN_STATEFUL_COMMAND_PROPERTY = "introscope.epagent.stateful.[key].command";

		public static final String PLUGIN_STATEFUL_CLASS_PROPERTY = "introscope.epagent.stateful.[key].class";

		public static final String PLUGIN_STATELESS_NAMES_PROPERTY = "introscope.epagent.plugins.stateless.names";

		public static final String PLUGIN_STATELESS_COMMAND_PROPERTY = "introscope.epagent.stateless.[key].command";

		public static final String PLUGIN_STATELESS_CLASS_PROPERTY = "introscope.epagent.stateless.[key].class";

		public static final String PLUGIN_STATELESS_DELAY_SEC_PROPERTY = "introscope.epagent.stateless.[key].delayInSeconds";

		public static final String PLUGIN_STATELESS_SCHEDULE_PROPERTY = "introscope.epagent.stateless.[key].schedule";

		public static final String PLUGIN_STATELESS_METRIC_NOT_REPORTED_ACTION_PROPERTY = "introscope.epagent.stateless.[key].metricNotReportedAction";

		public static final String METRICS_CASE_SENSITIVE_PROPERTY = "introscope.epagent.metricscasesensitive";

		// Agent Properties
		public static final String CUSTOM_PROCESS_NAME_PROPERTY = "introscope.agent.customProcessName";

		public static final String DEFAULT_PROCESS_NAME_PROPERTY = "introscope.agent.defaultProcessName";

		public static final String NAME_PROPERTY = "introscope.agent.agentName";

		// Enterprise Manager Connection Order
		public static final String EM_CONNECTION_ORDER_PROPERTY = "introscope.agent.enterprisemanager.connectionorder";
		// Enterprise Manager Locations and Names
		public static final String EM_TRANSPORT_TCP_HOST_PROPERTY = "introscope.agent.enterprisemanager.transport.tcp.host.[key]";

		public static final String EM_TRANSPORT_TCP_PORT_PROPERTY = "introscope.agent.enterprisemanager.transport.tcp.port.[key]";

		public static final String EM_TRANSPORT_TCP_SOCKET_FACTORY_PROPERTY = "introscope.agent.enterprisemanager.transport.tcp.socketfactory.[key]";

		public static final String EM_TRANSPORT_HTTP_PROXY_HOST_PROPERTY = "introscope.agent.enterprisemanager.transport.http.proxy.host";

		public static final String EM_TRANSPORT_HTTP_PROXY_PORT_PROPERTY = "introscope.agent.enterprisemanager.transport.http.proxy.port";

		public static final String EM_TRANSPORT_HTTP_PROXY_USERNAME_PROPERTY = "introscope.agent.enterprisemanager.transport.http.proxy.username";

		public static final String EM_TRANSPORT_HTTP_PROXY_PASSWORD_PROPERTY = "introscope.agent.enterprisemanager.transport.http.proxy.password";

		public static final String EM_TRANSPORT_TCP_TRUST_STORE_PROPERTY = "introscope.agent.enterprisemanager.transport.tcp.truststore.[key]";

		public static final String EM_TRANSPORT_TCP_TRUST_PASSWORD_PROPERTY = "introscope.agent.enterprisemanager.transport.tcp.trustpassword.[key]";

		public static final String EM_TRANSPORT_TCP_KEY_STORE_PROPERTY = "introscope.agent.enterprisemanager.transport.tcp.keystore.[key]";

		public static final String EM_TRANSPORT_TCP_KEY_PASSWORD_PROPERTY = "introscope.agent.enterprisemanager.transport.tcp.keypassword.[key]";

		public static final String EM_TRANSPORT_TCP_CIPHER_SUITES_PROPERTY = "introscope.agent.enterprisemanager.transport.tcp.ciphersuites.[key]";
		// Enterprise Manager Failback Retry Interval
		public static final String EM_FAILBACK_RETRY_INTERVAL_SECONDS_PROPERTY = "introscope.agent.enterprisemanager.failbackRetryIntervalInSeconds";
		// Agent Extensions Directory
		public static final String EXTENSIONS_DIR_PROPERTY = "introscope.agent.extensions.directory";
		// Remote Configuration Settings
		public static final String REMOTE_AGENT_CONFIGURATION_ENABLED_PROPERTY = "introscope.agent.remoteagentconfiguration.enabled";
		// Agent Metric Aging
		public static final String METRIC_AGING_TURN_ON_PROPERTY = "introscope.agent.metricAging.turnOn";

		public static final String METRIC_AGING_HEARTBEAT_INTERVAL_PROPERTY = "introscope.agent.metricAging.heartbeatInterval";

		public static final String METRIC_AGING_DATA_CHUNK_PROPERTY = "introscope.agent.metricAging.dataChunk";

		public static final String METRIC_AGING_NUMBER_TIMESLICES_PROPERTY = "introscope.agent.metricAging.numberTimeslices";

		public static final String METRIC_AGING_METRIC_EXCLUDE_IGNORE_PROPERTY = "introscope.agent.metricAging.metricExclude.ignore.[key]";
		// ChangeDetector configuration properties
		public static final String CD_ENABLED_PROPERTY = "introscope.changeDetector.enable";

		public static final String CD_ROOT_DIR_PROPERTY = "introscope.changeDetector.rootDir";

		public static final String CD_ISENGARD_STARTUP_WAIT_TIME_SECONDS_PROPERTY = "introscope.changeDetector.isengardStartupWaitTimeInSec";

		public static final String CD_WAIT_TIME_BETWEEN_RECONNECT_SECONDS_PROPERTY ="introscope.changeDetector.waitTimeBetweenReconnectInSec";

		public static final String CD_ENABLE_EPA_PROPERTY = "introscope.changeDetector.enableEPA";

		public static final String CD_AGENT_ID_PROPERTY = "introscope.changeDetector.agentID";

		public static final String CD_PROFILE_PROPERTY = "introscope.changeDetector.profile";

		public static final String CD_PROFILE_DIR_PROPERTY = "introscope.changeDetector.profileDir";

	}

	public static final class Pbd{

		public static final String LEAKHUNTER = "leakhunter.pbd";

	}

	public static final class EM {

		public static final String CEM_DEFAULT_PORT = "8081";

	}

	// Values
	public static final String AGENT_CONFIG_LOC = "/" + System.getProperty("role_agent.config.dir");

	public static final String AGENT_EXT_LOC = "/core/ext/";

	public static final String AGENT_COMMON_LOC = "/common/";

	public static final String AGENT_NO_REDEF_JAR =  "AgentNoRedef.jar";

	public static final String CREATE_AUTOPROBE_CONNECTOR_JAR = "CreateAutoProbeConnector.jar";

	public static final String AUTOPROBE_CONNECTOR_JAR  = "AutoProbeConnector.jar";

	public static final String INTROSCOPE_NO_REDEF_PROFILE = "IntroscopeAgent.NoRedef.profile";

	public static final String INTROSCOPE_PROFILE = "IntroscopeAgent.profile";

	// Application Server Specifics

	// Apache Tomcat

	public static final String TOMCAT_STARTUP_SCRIPT_WINDOWS = "catalina.bat";

	public static final String TOMCAT_STOP_SCRIPT_WINDOWS = "shutdown.bat";

	public static final String TOMCAT_STARTUP_SCRIPT_UNIX = "catalina.sh";

	public static final String TOMCAT_STOP_SCRIPT_UNIX = "shutdown.sh";

	public static final String TOMCAT_STARTUP_SCRIPT_ARG = "run";

	public static final String TOMCAT_STARTUP_MESSAGE = "INFO: Server startup";


	// Oracle WebLogic

	public static final String WLS_STARTUP_SCRIPT_UNIX = "startWebLogic.sh";

	public static final String WLS_STARTUP_SCRIPT_WINDOWS = "startWebLogic.cmd";

	public static final String WLS_SET_DOMAIN_ENV_UNIX = "setDomainEnv.sh";

	public static final String WLS_SET_DOMAIN_ENV_WINDOWS = "setDomainEnv.cmd";

	public static final String WLS_STARTING_MESSAGE_WINDOWS = "Redirecting output from WLS window";

	public static final String WLS_STOP_MESSAGE = "Disconnected from weblogic server";

	public static final String WLS_STOP_SCRIPT_UNIX = "stopWebLogic.sh";

	public static final String WLS_STOP_SCRIPT_WINDOWS = "stopWebLogic.cmd";

	// Oracle WebLogic Portal

	public static final String WLS_PORTAL_START_MESSAGE = "Server started in RUNNING mode";

	public static final String WLS_PORTAL_STOP_MESSAGE = "Disconnected from weblogic server: portalServer";



	// IBM WebSphere
	public static final String WAS_STARTUP_SCRIPT_UNIX = "startServer.sh";

	public static final String WAS_STARTUP_SCRIPT_WINDOWS = "startServer.bat";

	public static final String WAS_STOP_SCRIPT_UNIX = "stopServer.sh";

	public static final String WAS_STOP_SCRIPT_WINDOWS = "stopServer.bat";

	public static final String WAS_STOP_MESSAGE = "stop completed";

	public static final String WAS_CONFIG_FILE = "server.xml";

	public static final String WAS_SERVER_POLICY = "server.policy";

	public static final String WAS_START_MESSAGE = "open for e-business";

	public static final String WAS_ALREADY_START_MESSAGE = "An instance of the server may already be running";

	public static final String WAS_ALREADY_STOP_MESSAGE = "It appears to be stopped";
	
	public static final String WAS_DEFAULT_PROFILE_NAME = "AppSrv01";
	
	public static final String WAS_DEFAULT_SERVER_NAME = "server1";

	// JBOSS

	public static final String JBOSS_STARTUP_SCRIPT_WINDOWS = "standalone.bat";

	public static final String JBOSS_STOP_SCRIPT_WINDOWS = "jboss-cli.bat";

	public static final String JBOSS_BIND_ADDRESS_ARG = "-Djboss.bind.address=0.0.0.0";

	public static final String JBOSS_STARTUP_SCRIPT_UNIX = "standalone.sh";

	public static final String JBOSS_STOP_SCRIPT_UNIX = "jboss-cli.sh";

	public static final String JBOSS_STARTUP_MESSAGE = ".*JBoss.*started.*";

	// .NET

	public static final String IIS_RESTART_MESSAGE = "Internet services successfully restarted";

	// Process

	public final static String TASK_LIST = "tasklist";

	public static final String TASK_KILL = "taskkill /f /t /PID ";

	// General

	public static final String FILE_BACKUP_SUFFIX = ".backup";

	public static final String FILE_ORIGINAL_SUFFIX = ".original";

	public static final String html_Type = "HtmlUnit";

}
