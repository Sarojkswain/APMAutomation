package com.ca.apm.commons.coda.common;

public abstract class AutomationConstants
{

    // Common properties
    public final static String PREPEND_SLASH_DOUBLEQUOTES                                     = "\\\"";

    // EM properties

    public final static String        CLWJARLOCATION                                                 = "/em/lib/CLWorkstation.jar";

    // agent properties

    public static final String AGENT_AUTONAMING_PROPERTY                                      = "introscope.agent.agentAutoNamingEnabled";

    public static final String AGENT_NAME_PROPERTY                                            = "introscope.agent.agentName";

    public static final String AGENT_CUSTOM_PROCESS_NAME_PROPERTY                             = "introscope.agent.customProcessName";

    public static final String AGENT_LOG_FILE_APPENDER                                        = "log4j.appender.logfile";
    
    public static final String AGENT_LOG_PATH_PROPERTY                                        = AGENT_LOG_FILE_APPENDER + ".File";

    public static final String AGENT_AUTOPROBE_LOG_PATH_PROPERTY                              = "introscope.autoprobe.logfile";

    public static final String AGENT_LEAKHUNTER_ENABLE_PROPERTY                               = "introscope.agent.leakhunter.enable";

    public static final String AGENT_DIRECTIVES_FILE_PROPERTY                                 = "introscope.autoprobe.directivesFile";

    public static final String AGENT_LEAKHUNTER_LOG_PROPERTY                                  = "introscope.agent.leakhunter.logfile.location";

    public static final String AGENT_LEAKHUNTER_COLLECT_ALLOCATION_STACK_TRACES               = "introscope.agent.leakhunter.collectAllocationStackTraces";

    public static final String AGENT_STARTUP_MESSAGE                                          = "Introscope Agent startup complete";

    public static final String AGENT_LEAKHUNTER_STARTUP_MESSAGE                               = "Identified extension Introscope LeakHunter";

    public static final String AGENT_EM_CONNECTED_MESSAGE                                     = "Connected Agent to the Introscope Enterprise Manager";

    public static final String AGENT_PMI_ENABLE_PROPERTY                                      = "introscope.agent.pmi.enable";

    public static final String TOMCAT_AGENT_STARTUP_MESSAGE                                   = "Introscope Agent startup complete";

    public static final String WAS_STARTUP_SCRIPT_WINDOWS                                     = "startServer.bat";

    public static final String WAS_STOP_SCRIPT_WINDOWS                                        = "stopServer.bat";

    public static final String TOMCAT_STARTUP_SCRIPT_WINDOWS                                  = "startup.bat";
    public static final String TOMCAT_STARTUP_SCRIPT_PATH_WINDOWS                             = "/bin/startup.bat";

    public static final String TOMCAT_STOP_SCRIPT_WINDOWS                                     = "shutdown.bat";
    public static final String TOMCAT_STOP_SCRIPT_PATH_WINDOWS                                = "/bin/shutdown.bat";

    public static final String WAS_STOP_MESSAGE                                               = "stop completed";

    public static final String WLS_STARTUP_SCRIPT_WINDOWS                                     = "startWebLogic.cmd";

    public static final String WLS_STOP_SCRIPT_WINDOWS                                        = "stopWebLogic.cmd";

    public static final String WLS_STARTING_MESSAGE_WINDOWS                                   = "Redirecting output from WLS window";

    public static final String WLS_STOP_MESSAGE                                               = "Disconnected from weblogic server";
	
	public static final String TOMCAT_STOP_MESSAGE											  = "Stopping service Catalina";
	
	public static final String TOMCAT_START_MESSAGE											  = "Server startup in";
    
	/* Following properties are for PMI Test suite */
    public static final String AGENT_PMI_ENABLED                                              = "introscope.agent.pmi.enable";

    // public static final String AGENT_PMI_ACTIVATING_MESSAGE =
// "Activating PMI Data Collection";

    public static final String AGENT_PMI_ACTIVATED_MESSAGE                                    = "PMI data collection activated";

    public static final String AGENT_PMI_FILETER_OBJREF                                       = "introscope.agent.pmi.filter.objref";

    public static final String AGENT_PMI_ENABLE_THREADPOOL                                    = "introscope.agent.pmi.enable.threadPool";

    public static final String AGENT_PMI_ENABLE_SERVLETSESSIONS                               = "introscope.agent.pmi.enable.servletSessions";

    public static final String AGENT_PMI_ENABLE_CONNECTIONPOOL                                = "introscope.agent.pmi.enable.connectionPool";

    public static final String AGENT_PMI_ENABLE_BEAN                                          = "introscope.agent.pmi.enable.bean";

    public static final String AGENT_PMI_ENABLE_TRANSACTION                                   = "introscope.agent.pmi.enable.transaction";

    public static final String AGENT_PMI_ENABLE_WEBAPP                                        = "introscope.agent.pmi.enable.webApp";

    public static final String AGENT_PMI_ENABLE_JVMRUNTIME                                    = "introscope.agent.pmi.enable.jvmRuntime";

    public static final String AGENT_PMI_ENABLE_SYSTEM                                        = "introscope.agent.pmi.enable.system";

    public static final String AGENT_PMI_ENABLE_CACHE                                         = "introscope.agent.pmi.enable.cache";

    public static final String AGENT_PMI_ENABLE_ORBPERF                                       = "introscope.agent.pmi.enable.orbPerf";

    public static final String AGENT_PMI_ENABLE_J2C                                           = "introscope.agent.pmi.enable.j2c";

    public static final String AGENT_PMI_ENABLE_WEBSERVICES                                   = "introscope.agent.pmi.enable.webServices";

    public static final String AGENT_PMI_ENABLE_WLM                                           = "introscope.agent.pmi.enable.wlm";

    public static final String AGENT_PMI_ENABLE_WSGW                                          = "introscope.agent.pmi.enable.wsgw";

    public static final String AGENT_PMI_ENABLE_ALARMMANAGER                                  = "introscope.agent.pmi.enable.alarmManager";

    public static final String AGENT_PMI_ENABLE_HAMANAGER                                     = "introscope.agent.pmi.enable.hamanager";

    public static final String AGENT_PMI_ENABLE_OBJECTPOOL                                    = "introscope.agent.pmi.enable.objectPool";

    public static final String AGENT_PMI_ENABLE_SCHEDULER                                     = "introscope.agent.pmi.enable.scheduler";

    public static final String TT_NO_DATA                                                     = "No transaction traces collected";

    /* Following properties are for Frontends test suite */
    public final static String CONCURRENTINVOCATION_FRONTENDS                                 = "|Apps|pipeorgan_was.war:Concurrent Invocations";

    public final static String CONCURRENTINVOCATION_SERVLETS                                  = "|ExecutorServlet_1:Concurrent Invocations";

    public final static String RESPONSEPERINTERVAL_FRONTENDS                                  = "Apps|pipeorgan_was.war:Responses Per Interval";

    public final static String RESPONSEPERINTERVAL_SERVLETS                                   = "|ExecutorServlet_1:Responses Per Interval";

    public final static String AVERAGERESPONSETIME_SERVLETS                                   = "|ExecutorServlet_1:Average Response Time (ms)";

    public final static String AVERAGERESPONSETIME_FRONTENDS                                  = "|Apps|pipeorgan_was.war:Average Response Time (ms)";

    public final static String AGGREGATERESPONSETIME_SERVLETS                                 = ":Average Response Time (ms)";

    public final static String AVERAGERESPONSETIME_FRONTENDS_ST                               = "|Apps|pipeorgan_was.war|URLs|Default:Average Response Time (ms)";

    public final static String RESPONSEPERINTERVAL_FRONTENDS_ST                               = "|Apps|pipeorgan_was.war|URLs|Default:Responses Per Interval";

    public final static String STALL_FRONTENDS                                                = "|Apps|pipeorgan_was.war:Stall Count";

    public final static String STALL_SERVLETS                                                 = "|ExecutorServlet_1:Stall Count";

    public final static String STALL__THRESHOLD_FRONTENDS                                     = "|Apps|pipeorgan_was.war|URLs|Default:Stall Count";

    public final static String STALL_VALUE                                                    = "introscope.agent.stalls.thresholdseconds";

    /* Following properties are for SmartStorResult.java */
    public static final String SMARTSTOR_COLUMN_DOMAIN                                        = "Domain";

    public static final String SMARTSTOR_COLUMN_HOST                                          = "Host";

    public static final String SMARTSTOR_COLUMN_PROCESS                                       = "Process";

    public static final String SMARTSTOR_COLUMN_AGENT_NAME                                    = "AgentName";

    public static final String SMARTSTOR_COLUMN_RESOURCE                                      = "Resource";

    public static final String SMARTSTOR_COLUMN_METRIC_NAME                                   = "MetricName";

    public static final String SMARTSTOR_COLUMN_RECORD_TYPE                                   = "Record_Type";

    public static final String SMARTSTOR_COLUMN_PERIOD                                        = "Period";

    public static final String SMARTSTOR_COLUMN_INTENDED_END_TIMESTAMP                        = "Intended_End_Timestamp";

    public static final String SMARTSTOR_COLUMN_ACTUAL_START_TIMESTAMP                        = "Actual_Start_Timestamp";

    public static final String SMARTSTOR_COLUMN_ACTUAL_END_TIMESTAMP                          = "Actual_End_Timestamp";

    public static final String SMARTSTOR_COLUMN_COUNT                                         = "Count";

    public static final String SMARTSTOR_COLUMN_TYPE                                          = "Type";

    public static final String SMARTSTOR_COLUMN_VALUE                                         = "Value";

    public static final String SMARTSTOR_COLUMN_MIN                                           = "Min";

    public static final String SMARTSTOR_COLUMN_MAX                                           = "Max";

    public static final String SMARTSTOR_COLUMN_STRING_VALUE                                  = "String_Value";

    /* Following properties are for EJB test suite */
    public final static String STATELES_SSESSION_1BEAN_RPI                                    = "|Session|StatelessSession_1Bean:Responses Per Interval";

    public final static String EJB_AGGREGRATE_METHODINV_PERINTERVAL                           = "|Session:Method Invocations Per Interval";

    public final static String EJB_AVERAGE_METHODINV_TIME                                     = "|Session:Average Method Invocation Time (ms)";

    public final static String EJB_STATELESS_SESSION_1_AVG_RESPTIME                           = "|Session|StatelessSession_1Bean:Average Response Time (ms)";

    /* Following properties are for EJB Stalls test suite */
    public final static String STATELES_SSESSION_1BEAN_STALL                                  = "|Session|StatelessSession_1Bean:Stall Count";

    public final static String STATEFUL_SSESSION_3BEAN_STALL                                  = "|Session|StatefulSession_3Bean:Stall Count";

    public final static String STATEFUL_SESSION_2BEAN_STALL                                   = "|Session|StatefulSession_2Bean:Stall Count";

    public final static String STATELESS_SESSION_2BEAN_STALL                                  = "|Session|StatelessSession_2Bean:Stall Count";

    public final static String BACKEND_PROC_STALL                                             = "|mary mary-1521 (Oracle DB)|SQL|Dynamic|Query|CALL JAVASLEEP (\\?):Stall Count";

    /* Following properties are for BACKENDS test suite */
    public final static String STATELESS_SESSION_1_RPI                                        = "|StatelessSession_1:Responses Per Interval";

    public final static String STATELEFUL_SESSION_3_RPI                                       = "|StatefulSession_3:Responses Per Interval";

    public final static String CALLED_BACKEND_STATELESS_SESSION_1_RPI                         = "|Apps|pipeorgan_was.war|URLs|Default|Called Backends|StatelessSession_1:Responses Per Interval";

    // Following properties are for Servlets test suite

    public static final String SERVLETCALLING_EJB_LOCALSTATELESS_URL                          = "/QATestApp/backends/EJBCallingServlet?hits=1&duration=20000&type=LocalStateless&error=true&submit=call";

    public static final String SERVLETCALLING_EJB_LOCALSTATEFUL_URL                           = "/QATestApp/backends/EJBCallingServlet?hits=1&duration=20000&type=LocalStateful&error=true&submit=call";

    public static final String SERVLETCALLING_EJB_REMOTESTATELESS_URL                         = "/QATestApp/backends/EJBCallingServlet?hits=1&duration=20000&type=RemoteStateless&error=true&submit=call";

    public static final String SERVLETCALLING_EJB_REMOTESTATEFUL_URL                          = "/QATestApp/backends/EJBCallingServlet?hits=1&duration=20000&type=RemoteStateful&error=true&submit=call";

    /* following properties are for backends errors test suite */
    public final static String BACKEND_SELECT_ERRORS                                          = "|mary mary-1521 (Oracle DB)|SQL|Dynamic|Query|SELECT \\* FROM PIPEORGANENTITIES_X:Errors Per Interval";

    public final static String BACKEND_CREATE_ERRORS                                          = "|mary mary-1521 (Oracle DB)|SQL|Dynamic|Query|CREATE TABLE BEAN (ID VARCHARX (\\?), NAME VARCHAR (\\?), VALUE VARCHAR (\\?)):Errors Per Interval";

    public final static String BACKEND_INSERT_ERRORS                                          = "|mary mary-1521 (Oracle DB)|SQL|Dynamic|Query|INSERT INTO BEAN (ID, NAMEX, VALUE) VALUES (\\?, \\?, \\.\\.\\.):Errors Per Interval";

    public final static String BACKEND_DROP_ERRORS                                            = "|mary mary-1521 (Oracle DB)|SQL|Dynamic|Query|DROP TABLE BEANX:Errors Per Interval";

    public final static String BACKEND_PROCEDURE_ERRORS                                       = "|mary mary-1521 (Oracle DB)|SQL|Dynamic|Query|CALL JAVASLEEPX (\\?):Errors Per Interval";

    // Below string using another variable for escaping " with a slash.
// Otherwise compilation error will occur
    public final static String BACKEND_UPDATE_ERRORS                                          = "|mary mary-1521 (Oracle DB)|SQL|Dynamic|Query|UPDATE BEAN SET NAMEX = "
                                                                                                + PREPEND_SLASH_DOUBLEQUOTES
                                                                                                + "ERR"
                                                                                                + PREPEND_SLASH_DOUBLEQUOTES
                                                                                                + " WHERE ID = "
                                                                                                + PREPEND_SLASH_DOUBLEQUOTES
                                                                                                + "ID"
                                                                                                + PREPEND_SLASH_DOUBLEQUOTES
                                                                                                + ":Errors Per Interval";

    /* Following properties are for Transaction Traces test suite */
    public final static String NO_TRANS_TRACE_COLLECTED                                       = "No transaction traces collected.";

    /* Following properties are for Agent Properties test suite */
    public static final String DYNAMICINSTRUMENT_CLASSFILESIZE                                = "introscope.autoprobe.dynamicinstrument.classFileSizeLimitInMegs";


    public static final String DYNAMICINSTRUMENT_ENABLED                                      = "introscope.autoprobe.dynamicinstrument.enabled";

    public static final String AGENT_DYNAMICINSTRUMENT_CLASSFILESIZE_DEFAULTFRONT_MESSAGE     = "Property \"introscope.autoprobe.dynamicinstrument.classFileSizeLimitInMegs\" has value \"";

    public static final String AGENT_DYNAMICINSTRUMENT_CLASSFILESIZE_DEFAULTREAR_MESSAGE      = "\" where a positive integer value is expected.  Using default value of 1.";

    public static final String AGENT_DYNAMICINSTRUMENT_CLASSFILESIZE_REARINVALIDVALUE_MESSAGE = "\" where an integer value is expected.  Using default value of 1.";

    public static final String AGENT_FQDN_DEFAULTFRONT_MESSAGE                                = "Invalid value \"";

    public static final String AGENT_FQDN_DEFAULTREAR_MESSAGE                                 = "\" for Fully Qualified Agent Host Name";

    public static final String FQDN_VALUE                                                     = "introscope.agent.display.hostName.as.fqdn";

    /* Following properties are for Sockets test suite */
    public static final String AGENT_SOCKETS_ENABLE_PROPERTY                                  = "introscope.agent.sockets.reportRateMetrics";

    /* Following properties are for Clamps test suite */
    public static final String CLAMP_VALUE                                                    = "introscope.agent.ttClamp";

    public static final String METRIC_CLAMP_VALUE                                             = "introscope.agent.metricClamp";

    public static final String AGENT_CLAMPVALUE_ERROR_REAR_MESSAGE                            = "\" for Agent Transaction Trace Limit ";

    public static final String AGENT_CLAMPVALUE_ERROR_FRONT_MESSAGE                           = "\\[ERROR\\] \\[IntroscopeAgent.Agent\\] Invalid value \"";

    public static final String AGENT_CLAMPVALUE_DEFAULT_FRONT_MESSAGE                         = "\\[INFO\\] \\[IntroscopeAgent.Agent\\] Agent Transaction Trace Limit \\(introscope.agent.ttClamp\\) set to \"";

    public static final String AGENT_CLAMPVALUE_DEFAULT_REAR_MESSAGE                          = "\"";

    public static final String AGENT_METRIC_CLAMPVALUE_FRONT_MESSAGE                          = "\\[ERROR\\] \\[IntroscopeAgent.Agent\\] Invalid value \"";

    public static final String AGENT_METRIC_CLAMPVALUE_REAR_MESSAGE                           = "\" for Agent Metric Limit";

    /* Following properties are for Tracers test suite */
    public static final String AGENT_TRACERS_WLS_FULL_PBL                                     = "weblogic-full.pbl";

    public static final String AGENT_TRACERS_WLS_FULL_LEGACY_PBL                              = "weblogic-full-legacy.pbl";

    public static final String AGENT_TRACERS_ERRORS_PBD                                       = "errors.pbd";

    public static final String AGENT_TRACERS_SQLAGENT_PBD                                     = "sqlagent.pbd";

    public static final String AGENT_TRACERS_SQLAGENT_61_PBD                                  = "sqlagent-6.1.pbd";

    public static final String AGENT_TRACERS_TURNON_SQLAGENT_CONN                             = "TurnOn: SQLAgentConnections";

    public static final String AGENT_TRACERS_TURNON_SQLAGENT_RS                               = "TurnOn: SQLAgentResultSets";

    public static final String AGENT_TRACERS_TURNON_SQLAGENT_STMT                             = "TurnOn: SQLAgentStatements";

    // Toggles Tracers
    public static final String AGENT_TRACERS_WLS_TOGGLES_FULL_PBD                             = "toggles-full.pbd";

    public static final String AGENT_TRACERS_WLS_THROW_EXCEPTION                              = "InstrumentPoint: ThrowException";

    public static final String AGENT_TRACERS_WLS_CATCH_EXCEPTION                              = "InstrumentPoint: CatchException";

    public static final String AGENT_TRACERS_WLS_TURNON_FILE_SYSTEM                           = "TurnOn: FileSystemTracing";

    public static final String AGENT_TRACERS_WLS_TURNON_JAVA_MAIL                             = "TurnOn: JavaMailTransportTracing";

    public static final String AGENT_TRACERS_WLS_TURNON_JNDI_TRACING                          = "TurnOn: JNDITracing";

    public static final String AGENT_TRACERS_WLS_TURNON_SOCKETS_TRACING                       = "TurnOn: SocketTracing";

    public static final String AGENT_TRACERS_WLS_TURNON_THREAD_TRACING                        = "TurnOn: ThreadTracing";

    /* Toggles for EJB 3.0 */
    public static final String AGENT_TRACERS_WLS_TURNON_EJB3STUB_TRACING                      = "TurnOn: EJB3StubTracing";

    public static final String AGENT_TRACERS_WLS_TURNON_SESSIONBEAN3_TRACING                  = "TurnOn: SessionBean3Tracing";

    public static final String AGENT_TRACERS_WLS_TURNON_ENTITYBEAN3_TRACING                   = "TurnOn: EntityBean3Tracing";

    public static final String AGENT_TRACERS_WLS_TURNON_MESSAGEDRIVENBEAN3_TRACING            = "TurnOn: MessageDrivenBean3Tracing";

    public static final String AGENT_TRACERS_WLS_TURNON_EJB3METHODLEVEL_TRACING               = "TurnOn: EJB3MethodLevelTracing";

    /* Toggles for JSP */
    public static final String AGENT_TRACERS_WLS_TURNON_JSP_TRACING                           = "TurnOn: JSPTracing";

    /* Toggles for HTTP Servlet */
    public static final String AGENT_TRACERS_WLS_TURNON_HTTPSERVLET_TRACING                   = "TurnOn: HTTPServletTracing";

    public static final String AGENT_TRACERS_WLS_TURNON_SERVLETFILTER_TRACING                 = "TurnOn: ServletFilterTracing";

    /* Toggles for NIO Sockets/Datagram */
    public static final String AGENT_TRACERS_WLS_NIOSOCKET_TRACING                            = "TurnOn: NIOSocketTracing";

    public static final String AGENT_TRACERS_WLS_NIOSOCKETSUMMARY_TRACING                     = "TurnOn: NIOSocketSummaryTracing";

    public static final String AGENT_TRACERS_WLS_NIOSELECTOR_TRACING                          = "TurnOn: NIOSelectorTracing";

    public static final String AGENT_TRACERS_WLS_NIODATAGRAM_TRACING                          = "TurnOn: NIODatagramTracing";

    public static final String AGENT_TRACERS_WLS_NIODATAGRAMSUMMARYP_TRACING                  = "TurnOn: NIODatagramSummaryTracing";

    /* Following properties are of LogFile Autonaming test suite */

    public static final String DEFAULT_AUTOFILE_NAMING_VALUE                                  = "introscope.agent.disableLogFileAutoNaming";

    public static final String DEFAULT_MAX_FILE_SIZE                                          = AGENT_LOG_FILE_APPENDER + ".MaxFileSize";

    public static final String DEFAULT_MAX_BACKUP_INDEX                                       = AGENT_LOG_FILE_APPENDER + ".MaxBackupIndex";

    public static final String DEFAULT_MAX_ADDITIVITY_PROPERTY                                = "log4j.additivity.IntroscopeAgent";

    /* Following properties are for JMX Test Cases */

    public final static String JMX_VALUE                                                      = "|com.bea|Name=ThreadPoolRuntime|ServerRuntime=server|Type=ThreadPoolRuntime:ExecuteThreadIdleCount";

    public static final String JMX_IGNORE_ATTRIBUTE                                           = "introscope.agent.jmx.ignore.attributes";

    public static final String JMX_EXCLUDE_STRINGMETRICS                                      = "introscope.agent.jmx.excludeStringMetrics";

    /* Following properties are for Socket channel Test Cases */
    public static final String AGENT_TOGGLES_FULL_PBD                                         = "toggles-full.pbd";

    public static final String AGENT_TOGGLES_TYPICAL_PBD                                      = "toggles-typical.pbd";

    public static final String WLG_TYPICAL_PBL                                                = "weblogic-typical.pbl";

    public static final String WLG_FULL_PBL                                                   = "weblogic-full.pbl";

    public static final String WSP_TYPICAL_PBL                                                = "websphere-typical.pbl";

    public static final String WSP_FULL_PBL                                                   = "websphere-full.pbl";

    public final static String TASK_LIST                                                      = "tasklist";

    /** EM exe name */
    public static final String EM_EXE                                                         = "Introscope_Enterprise_Manager.exe";

    public static final String INTROSCOPE_PROFILE                                             = "IntroscopeAgent.profile";

    public static final String INTROSCOPE_PROFILE_PATH                                        = "/wily/"
                                                                                                + System.getProperty("role_agent.config.dir");

    public static final String URL_GROUPING_TEST_CONFIG_XML_FILE_LOC                          = "UrlGroupingTestConfig.xml";

    public static final String TESTBED_AGENT_INSTALL_PARENT_DIR                               = System.getProperty("testbed_agent.install.parent.dir");

    public static final String TESTBED_EM_INSTALL_PARENT_DIR                                  = System.getProperty("testbed_em.install.parent.dir");

    /** Following properties are Log4j Module */

    public static final String SYSTEM_PROP_AGENT_INSTALL_DIR                                  = System.getProperty("role_agent.install.dir");

    public static final String SYSTEM_PROP_AGENT_PROFILE                                      = System.getProperty("role_agent.agent.profile");

    public static final String SYSTEM_PROP_WEBAPP_TYPE                                        = System.getProperty("role_webapp.container.type");

    public static final String SYSTEM_PROP_WEBAPP_HOME_DIR                                    = System.getProperty("role_webapp.home.dir");

    public static final String SYSTEM_PROP_WEBAPP_SERVER_NAME                                 = System.getProperty("role_webapp.server.name");

    /** Following properties are for SQLStatementNormalizerExtension Module */

    public static final String DB_TEST_ORACLE                                                 = "DBTestOracle_wls.xml";

    public static final String SQL_TEST_ORACLE                                                = "SQLStmnt_test_wls.xml";

    public static final String DB_TEST_ORACLE_SQLNOR                                          = "DBTestOracle.xml";

    public static final String DB_TEST_ORACLE_SQLNOR_WAS                                      = "DBTestOracle_was.xml";

    public static final String CLASSES12_JAR                                                  = "/classes12.jar";

    public static final String AGENT_JAR                                                      = "/Agent.jar ";                                                                                                                                                                                                                                                     // -Dcom.wily.introscope.agentProfile=

    public static final String JVM_ARG_1                                                      = "java -classpath ";

    public static final String JVM_ARG_2                                                      = " -javaagent:";

    public static final String JVM_ARG_3                                                      = "-Dcom.wily.introscope.agentProfile=";

    public static final String REGEXAGENTTEST                                                 = " RegexAgentTest ";

    public static final String INSTANCE_AGENT                                                 = "Instance";

    public static final String INSTANCE_CLASS_FILE_LOC                                        = "/client/pbddirectoriesandhotdeploy/classes";

    public static final String LST_PBD_FILES                                                  = "/hotdeploypbds/pbddirectoriesandhotdeploy/";

    public static final String INSTANCE_CLASS_FILE_DI2_LOC                                    = "/DI_CLWPart1_2_attachedFiles";

    public static final String EM_JAR                                                         = "/com.wily.introscope.em.xml_9.1.0.jar";

    public static final String EM_JAVA_LOC                                                    = "/jre/bin/java";

    public static final String EM_TRACES_FOLDER_NAME                                          = "/traces";

    public static final String EM_FOLDER_NAME                                                 = "/em";

    public static final String STOP_PO_SCRIPT_CMD                                             = "wmic Path win32_process Where \"CommandLine Like '%com.wily.tools.pipeorgan.Main%'\" Call Terminate";

    public static final String AGENT_COMMON                                                   = "/common";

    public static final String AGENT_CHANGEDETECTOR                                           = "/change_detector";

    public static final String AGENT_CORE_CONFIG_LOC                                          = "/"
                                                                                                + System.getProperty("role_agent.config.dir");

    public static final String AGENT_CORE_EXT_LOC                                             = "/core/ext/";

    public static final String INTROSCOPE_EM_PROPERTIES_FILE                                  = "/config/IntroscopeEnterpriseManager.properties";

    public static final String HOT_DEPLOY_PATH                                                = "hotdeploy/";

    public static final String CHANGE_INTROSCOPE_AGENT_PROFILE                                = "/ChangeIntroscopeAgent.profile";

    public static final String AGENT_JAR_PATH                                                 = "/core/config/Agent.jar";

    public static final String KRSQL_URL                                                      = "/krsql/result.jsp?machinename=mary&portnumber=1521&database=mary&username=devpotool&password=devpotool&query=+Select+id+as+id00%2C+mynumber+as+mynumTrue%2C++word+as+wordTRUE+from+ORA_TABLE+where+word%3D+%27basic%27+and+mynumber+%3D+123+and+word%3D%27abc%27";

    public static final String PO_SCRIPT_URL                                                  = "/pipeorgan/ExecutorServlet_1";

    public static final String WL_BIN_PATH                                                    = "samples/domains/medrec/bin";

    public static final String WAS_BIN_PATH                                                   = "AppServer/profiles/AppSrv01/bin";

    public static final String CONTAINER_TYPE                                                 = System.getProperty("role_webapp.container.type");

    public static final String WL_STARTUP_FILE                                                = System.getProperty("role_webapp.home.dir")
                                                                                                + "/bin/"
                                                                                                + WLS_STARTUP_SCRIPT_WINDOWS;

    public static final String WAS_STARTUP_FILE                                               = System.getProperty("testbed_webapp.was7.home")
                                                                                                + "/AppServer/profiles/AppSrv01/config/cells/"
                                                                                                + System.getProperty("testbed_webapp.hostname")
                                                                                                + "Node01Cell/nodes/"
                                                                                                + System.getProperty("testbed_webapp.hostname")
                                                                                                + System.getProperty("testbed_webapp.was7.node.info")+"/servers/server1/server.xml";

    public static final String WAS_INTROSCOPEFILE_PATH                                        = System.getProperty("testbed_webapp.was7.home")
                                                                                                + "/AppServer/wily/"
                                                                                                + System.getProperty("role_agent.config.dir")
                                                                                                + "IntroscopeAgent.profile";

    public static final String WAS_AGENT_JAR_PATH                                             = System.getProperty("testbed_webapp.was7.home")
                                                                                                + "/AppServer/wily/Agent.jar";

    /**
     * RESP_SUCCESS indicates that the CLW command was prepared correctly,
     * executed successfully and results are written to Transaction Trace xml
     */
    public static final String RESP_SUCCESS                                                   = "Writing Transaction Trace Data to";

    /**
     * RESP_INVALID_COMMAND indicates that the CLW command was prepared
     * incorrectly
     */
    public static final String RESP_INVALID_COMMAND                                           = "Invalid command";

    public static final String SUCCESS_MESSAGE                                                = "Successful";

    public static final String WAS_FULL_LEGACY_PBL                                            = "websphere-full-legacy.pbl";

    /**
     * RESP_NO_TRACES indicates that the CLW command was prepared correctly,
     * executed successfully and No transaction traces are collected
     */
    public static final String RESP_NO_TRACES                                                 = "No transaction traces collected";

    /**
     * UNKNOWN_RESPONSE_PATTERN indicates that the CLW command returned UNKNOWN
     * RESPONSE PATTERN
     */
    public static final String UNKNOWN_RESPONSE_PATTERN                                       = "UNKNOWN RESPONSE PATTERN";

    public static final String CHK_TRACE_PTRN                                                 = "|Servlets|SessionSetterServlet:Stall Count";

    public static final String INSTANCE_CLASS_FILE_DI6_LOC                                    = "DI_CLWPart6_attachedFiles";

    public static final String INSTANCE_CLASS_FILE_DI3_LOC                                    = "DI_CLWPart3_attachedFiles";

    public static final String INSTANCE_CLASS_FILE_DI4_LOC                                    = "DI_CLWPart4_attachedFiles";

    public static final String AGENT_CONNECTORS                                               = "/connectors";

    public static final String AGENT_PROFILE_PATH                                             = System.getProperty("role_agent.install.dir");

    public static final String AGENT_PROFILE_NAME                                             = System.getProperty("role_agent.agent.profile");

    public static final String AGENT_HOST                                                     = System.getProperty("testbed_webapp.hostname");

    public static final String LOG_FILE_PATH                                                  = System.getProperty("results.dir");

    public static final String LOG_FILE_NAME                                                  = System.getProperty("role_agent.log.prefix");

    public static final String AGENT_HOST_NAME                                                = System.getProperty("testbed_agent.hostname");

    public static final String CLINET_INSTALL_DIR                                             = System.getProperty("role_client.install.dir");

    public static final String DITEST_APP1_BAT_LOC                                            = System.getProperty("role_client.ditestapp1");

    public static final String DITEST_APP2_BAT_LOC                                            = System.getProperty("role_client.ditestapp2");

    public static final String TASK_KILL                                                      = "taskkill /f /t /PID ";

    public static final String JBOSS_AGENT_STARTUP_MESSAGE                                    = "Probed runtime class name: org.hsqldb.jdbc.jdbcPreparedStatement";

    public static final String JBOSS_STARTUP_SCRIPT_WINDOWS                                   = "run.bat";

    public static final String LEAKHUNTER_PO_SCRIPT                                           = "LeakingAppScenario.xml";

    public static final String STOPPING_PO_SCRIPT                                             = "wmic Path win32_process Where \"CommandLine Like '%com.wily.tools.pipeorgan.Main%'\" Call Terminate";

    public static final String MANYMETRIC_APP_HOME                                            = "role_client.testapps.home";

    public static final String METRICCOLLISION_APP_HOME                                       = "role_client.testapps1.home";

    public static final String EXT_ATTACHED_FILES_LOC                                         = "attachedfiles.loc";

    public static final String AGENT_INSTALL_PARENT_DIR                                       = "testbed_agent.install.parent.dir";

    public static final String ROLE_AGENT_INSTALL_DIR                                         = "role_agent.install.dir";

    public static final String WEBSPHERE_START_MESSAGE                                        = "Tool information is being logged in file";
    
    public static final String START_WEBLOGIC_PORTAL_MESSAGE                                  = "Server started in RUNNING mode";

    public static final String STOP_WEBLOGIC_PORTAL_MESSAGE                                   = "Disconnected from weblogic server";

    public static final String AGENT_PORTAL_PBL                                               = "/powerpackforweblogicportal.pbl";

    public static final String AGENT_PORTAL_START_PATH                                        = "/samples/domains/portal";

    public static final String AGENT_PORTAL_STOP_PATH                                         = "/samples/domains/portal";

    public static final String AGENT_CORE_CONFIG_PARTIAL_LOC                                  = "/core/config";

    public static final String AGENT_LOG_PATH                                                 = "/wily/logs/IntroscopeAgent.log";
    
    public static final String WASAGENT_LOG_PATH                                          = "/wily/logs/IntroscopeAgent.WebSphere_Agent.log";

    public static final String AGENT_PROFILE_FILE_NAME                                        = "/IntroscopeAgent.profile";

    public static final String EXT_FOLDER_NAME                                                = "ext";

    public static final String LOGGING_CONFIG_XML                                             = "logging.config.xml";

    public static final String CATALINA_SCRIPT_WINDOWS                                        = "catalina.bat";
    
    public static final String CATALINA_HOME_ENV_PROPERTY                                     = "CATALINA_HOME";

    /* Added for Data Export Mechanism */

    public static String       GREMLIN_START                                                  = "changeFiles.bat";

    public static String       GREMLIN_ARGS_WL                                                = System.getProperty("testbed_agent.install.parent.dir")
                                                                                                + "/WebLogic_ChangeDetector";

    public static String       GREMLIN_ARGS_WS                                                = System.getProperty("testbed_agent.install.parent.dir")
                                                                                                + "/WebSphere_ChangeDetector";

    public static String       CHANGE_INTERVEL                                                = "1";

    public static String       XMl                                                            = "-xml";

    public static String       GREMLIN_FILE                                                   = "webLogicFile.xml";

    public static String       QUERYEXECUTER_JAR                                              = System.getProperty("role_client.testprojects.todir")
                                                                                                + "/QueryExecuter.jar";

    public static String       GREMLIN_XML                                                    = System.getProperty("role_client.testprojects.todir")
                                                                                                + "/gremlin";

	public static       String 		TEMP_ORG_XML 			= "\\temp_org.xml";
	public static final String INSTANCE_CLASS_FILE_D17_LOC                                    = "DI_BusyStatus_attachedFiles";
    
    public static final String INSTANCE_CLASS_FILE_D18_LOC                                    = "DISupportInterfaces_attachment";

	public static String         LOG_PATH_LOC                       = "/logs";
	
	public static final   String 			CHANGE_COUNT = "500";
}
