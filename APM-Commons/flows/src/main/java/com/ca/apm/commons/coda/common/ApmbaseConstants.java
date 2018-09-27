package com.ca.apm.commons.coda.common;

public interface ApmbaseConstants{
	public static String         EM_LOG_LOC                         = System.getProperty("testbed_em.install.parent.dir")+"/em/logs";
    public static String         EM_LOC                             = System.getProperty("testbed_webapp.em.loc");
	public static String         EM_HOME                            = System.getProperty("testbed_client.em.loc");
	public static String         EM_JAVA_LOC                        = System.getProperty("testbed_em.install.parent.dir")+"/em/jre/bin/java";
    public static String         CLW_JAR_LOC                        = System.getProperty("testbed_em.install.parent.dir")
                                                                      + AutomationConstants.CLWJARLOCATION;

    public static String         HVR_LOC                            = System.getProperty("testbed_webapp.hvragent.loc");
    public static String         WEBLOGIC_AGENT_PREFIX              = "weblogic";
    public static String         TOMCAT_AGENT_PREFIX                = "catalina";
    public static String         JBOSS_AGENT_PREFIX                 = "jboss";
    public static String         WEBSPHERE_AGENT_PREFIX             = "websphere";

    public static String         START_COMMAND_WITH_ITERVAL         = "java -jar WatchDog.jar start -startcmd "
                                                                      + EM_LOC
                                                                      + "/Introscope_Enterprise_Manager.exe -watch -interval 300";

    static String                STOP_COMMAND                       = "java -jar WatchDog.jar stop";

    public static String         START_COMMAND_WITH_OUT_ITERVAL     = "java -jar WatchDog.jar start -startcmd "
                                                                      + EM_LOC
                                                                      + "/Introscope_Enterprise_Manager.exe";

    // public static String STATUS_COMMAND =
// "java -jar WatchDog.jar status";

    public static String         WATCH_DOG_FILE_NAME                = "WatchDog";

    static String                INT_EM_MAN_FILE_NAME               = "IntroscopeEnterpriseManager.log";

    static String                EM_EXE                             = "Introscope_Enterprise_Manager.exe";
    static String                EM_EXE_LINUX                       = "Introscope_Enterprise_Manager";

    public static final String   ORG_FILE_LOG_NAME                  = "org-file.log";
    
    public static final String   ORG_FILE_LOG_PATH                  = System.getProperty("testbed_em.install.parent.dir") + "/em/" + ORG_FILE_LOG_NAME;
    // STOP

    public static String         START_COMMAND_STOP                 = "java -jar WatchDog.jar stop";

    // STATUS

    public static String         START_COMMAND_STATUS               = "java -jar WatchDog.jar status";

    // START

    public static String         START_COMMAND                      = "java -jar WatchDog.jar start";

    public static String[]       CHECK_WD_PORT_START                = { "java",
            "-jar", "WatchDog.jar", "start", "-startcmd",
            "../Introscope_Enterprise_Manager.exe", "-watch", "-port", "1234" };

    public static String[]       CHECK_WD_STATUS                    = { "java",
            "-jar", "WatchDog.jar", "status"                       };

    public static String[]       CHECK_WD_PORT_STATUS               = { "java",
            "-jar", "Watchdog.jar", "status", "-port", "1234"      };

    public static String[]       CHECK_WD_PORT_STOP                 = { "java",
            "-jar", "Watchdog.jar", "stop", "-port", "1234"        };

    public static String[]       START_EM_COMMAND                   = { "java",
            "-jar", "WatchDog.jar", "start", "-startcmd",
            "../Introscope_Enterprise_Manager.exe"                 };

    public static String[]       STATUS_EM_COMMAND                  = { "java",
            "-jar", "WatchDog.jar", "status"                       };

    public static String[]       STOP_EM_COMMAND                    = { "java",
            "-jar", "WatchDog.jar", "stop"                         };

    public static String[]       START_EM_WITH_INTERVAL             = { "java",
            "-jar", "WatchDog.jar", "start", "-startcmd", "DYNAMIC", "-watch",
            "-interval", "300"                                     };

    public static String[]       START_COMMAND_WATCH                = { "java",
            "-jar", "WatchDog.jar", "start", "-startcmd",
            "../Introscope_Enterprise_Manager.exe", "-watch"       };

    public static String[]       STOP_EM_COMMAND_WATCH              = { "java",
            "-jar", "WatchDog.jar", "stop", "-watch"               };

    public static String         EM_LOG_FILE_NAME                   = "IntroscopeEnterpriseManager.log";

    public static String[]       START_COMMAND_WATCH_NEW            = { "java",
            "-jar", "WatchDog.jar", "start", "-startcmd",
            "../Introscope_Enterprise_Manager.exe", "-watch"       };

    public static String[]       START_WD_BATCH_COMMAND             = {
            "WatchDog.bat", "start"                                };

    public static String[]       STATUS_WD_BATCH_COMMAND            = {
            "WatchDog.bat", "status"                               };

    public static String[]       STOP_WD_BATCH_COMMAND              = {
            "WatchDog.bat", "stop"                                 };

    public static String[]       HELP_WD_BATCH_COMMAND              = {
            "WatchDog.bat", "help"                                 };

    public static String[]       STATUS_COMMAND                     = { "java",
            "-jar", "WatchDog.jar", "status"                       };

    public static String[]       WATCH_WD_BATCH_COMMAND             = {
            "WatchDog.bat", "watch"                                };

    public static String[]       START_VERIFY_EMUSER_PWD_COMMAND_SAP    = { "java",
            "-jar", "WatchDog.jar", "start", "-startcmd",
            "../Introscope_Enterprise_Manager.exe", "-emuser", "testuser",
            "-empwd", "testpwd", "-emport", "6001", "-watch"       };
    
    public static String[]       START_VERIFY_EMUSER_PWD_COMMAND    = { "java",
                                                                        "-jar", "WatchDog.jar", "start", "-startcmd",
                                                                        "../Introscope_Enterprise_Manager.exe", "-emuser", "testuser",
                                                                        "-empwd", "testpwd", "-watch"       };

	public static String[]       START_COMMAND_WATCH_NEW_SAP           = { "java",
            "-jar", "WatchDog.jar", "start", "-startcmd",
            "../Introscope_Enterprise_Manager.exe","-emuser", "sapsupport",
            "-empwd", "a20041013b",  "-emport", "6001", "-watch"       };
			
			
    public static String[]       START_EM_COMMAND_60_SEC            = { "java",
            "-jar", "WatchDog.jar", "start", "-startcmd",
            "../Introscope_Enterprise_Manager.exe", "-watch", "-startuptime",
            "60"                                                   };

    public static String         START_INDEXREBUILDER               = "IndexRebuilder.bat";

    public static String         START_INDEXREBUILDER_ARGS          = EM_LOC
                                                                      + "/traces/";

    public static String         START_INDEXREBUILDER_ARGS_SPACES   = EM_LOC
                                                                      + "/Copy of traces";

    public static String         APP_SERVER_WEBLOGIC                = "weblogic";

    public static String         APP_SERVER_WEBLOGIC_LOC            = "/bea/wlserver_10.3/samples/domains/medrec";

    public static String         APP_SERVER_NAME                    = "medrec";

    public static String         EM_CTRL_BAT                        = "/bin/EMCtrl64.bat";

    public static String         AGENT_SERVER_JAR                   = "com.wily.cd.common.agent_server_";

    public static String         ALL_ASPECT_JAR                     = "com.wily.cd.common.all.aspect_";

    public static String         COMMON_ALL_JAR                     = "com.wily.cd.common.all_";

    public static String         WORKSTATION_JAR                    = "com.wily.cd.common.workstation_server_";

    public static String         CD_SERVER_JAR                      = "com.wily.cd.server_";

    public static String         EM_LOG                             = "IntroscopeEnterpriseManager.log";

    public static String         EM_PROP_FILE                       = "/config/IntroscopeEnterpriseManager.properties";

    public static String         CHG_DETECT_MNGT_JAR                = "ChangeDetectorManagementModule.jar";

    public static String         PLUGIN_FOLDER_LOC                  = "/product/enterprisemanager/plugins";

    public static String         MODULE_FOLDER_LOC                  = "/config/modules";

    public static String         LOG_PATH_LOC                       = "/logs";

    public static String         DB_FILE_LOC                        = "/myTraces";

    public static String         EM_PROPERTIES_FILE                 = "IntroscopeEnterpriseManager.properties";

    public static String         EM_PROP_FILE_LOC                   = "/config";

    public static String         EM_LAX_FILE                        = "Introscope_Enterprise_Manager.lax";

    public static String         EM_LOG_FILE                        = "IntroscopeEnterpriseManager.log";

    public static String         EM_TOOLS_LOC                       = "/tools";

    public static String         EM_DATA_LOC                        = "/data";

    public static String         SMART_STOR_TOOLS_BAT_FILE          = "SmartStorTools.bat";

    public static int            EXPETED_VALUE                      = 1;

    public static int            TOLERANCE                          = 0;

    public static String         AGENT_PROFILE                      = "IntroscopeAgent.profile";

    public static String         AGENT_PROFILE_LOC                  = "/core/config";

    public static String         AGENT_LOG_PATH                     = "/logs";

    public static String         AGENT_LOG                          = "IntroscopeAgent.WebLogicAgent.log";

    public static String         HVR_AGENT_BAT                      = "sample.replay.bat";
    public static String         HVRAgent_ExtractFile                = "sample.extract.bat";

    public static String         USERSFilePath                      = "/config/users.xml";

    public static String         emPropertyFileName                 = "IntroscopeEnterpriseManager.properties";

    public String                channelEnable                      = "introscope.enterprisemanager.enabled.channels";

    public static String         emBinLoc                           = "/bin";

    public static String         emExePath                          = "/Introscope_Enterprise_Manager.exe";

    public static String         IntroscopeEMfileName               = LOG_PATH_LOC
                                                                      + "/IntroscopeEnterpriseManager.log";

    public static String         EmPropLoc                          = "/config/IntroscopeEnterpriseManager.properties";

    public static String         agentPropLoc                       = "/bea/wily/core/config/IntroscopeWorkstation.properties";

    public static String         JettyFileLoc                       = "/config/";

    public static String         JettyFile                          = "/config/em-jetty-config.xml";

    public static String         DOMAINFilePath                     = "/config/domains.xml";

    public static String         AGENTCLUSTERSFilePath              = "/config/agentclusters.xml";

    public static String         ServerFilePath                     = "/config/server.xml";

    public static String         smartstorTier1Freq                 = "introscope.enterprisemanager.smartstor.tier1.frequency";

    public static String         smartstorTier1Age                  = "introscope.enterprisemanager.smartstor.tier1.age";

    public static String         smartstorTier2Freq                 = "introscope.enterprisemanager.smartstor.tier2.frequency";

    public static String         smartstorTier2Age                  = "introscope.enterprisemanager.smartstor.tier2.age";

    public static String         smartstorTier3Freq                 = "introscope.enterprisemanager.smartstor.tier3.frequency";

    public static String         smartstorTier3Age                  = "introscope.enterprisemanager.smartstor.tier3.age";

    public static String         RealmsFilePath                     = "/config/realms.xml";

    public static String         emScriptsLoc                       = "/scripts/";

    public static String         laxAdditionalProperty              = "lax.nl.java.option.additional";

    public static String         agent_profile                      = System.getProperty("role_agent.agent.profile");

    public static String         agent_log_prefix                   = System.getProperty("role_agent.log.prefix");

    public static String         agent_config_dir                   = System.getProperty("agent.config.dir");

    public static String         webapp_server_name                 = System.getProperty("role_webapp.server.name");

    public static String         webapp_container_type              = System.getProperty("role_webapp.container.type");

    public static String         weblogic_webapp_home_dir           = System.getProperty("role_webapp.home.dir");

    public static String         websphere_webapp_home_dir          = System.getProperty("testbed_webapp.was7.home");

    public static String         weblogic_webapp_port               = "7001";

    public static String         websphere_webapp_port              = "9080";

    public static String         webapp_hostname                    = System.getProperty("testbed_webapp.hostname");

    public static String         em_hostname                        = System.getProperty("testbed_em.hostname");

    public static String         em_install_parent_dir              = System.getProperty("testbed_em.install.parent.dir");

    public static String         agent_install_parent_dir           = System.getProperty("testbed_agent.install.parent.dir");

    public static String         em_admin_user                      = System.getProperty("role_em.admin.user");

    public static String         em_admin_passw                     = System.getProperty("role_em.admin.passw");

    public static String         em_port                            = System.getProperty("role_em.port");

    public static String         results_dir                        = System.getProperty("results.dir");

    // / Added for watch dog testcases

    public static final String[] CHECK_WD_DIFF_PORT_START           = { "java",
            "-jar", "WatchDog.jar", "start", "-startcmd",
            "../Introscope_Enterprise_Manager.exe", "-watch", "-emport", "5003" };

    public static final String[] CHECK_WD_DIFF_PORT_STATUS          = { "java",
            "-jar", "Watchdog.jar", "status", "-emport", "5003"      };

    public static final String[] CHECK_WD_DIFF_PORT_STOP            = { "java",
            "-jar", "Watchdog.jar", "" + "stop", "-emport", "5003"   };

    public static final String[] START_VERIFY_EMPORT_COMMAND        = { "java",
            "-jar", "WatchDog.jar", "start", "-startcmd",
            "../Introscope_Enterprise_Manager.exe", "-watch", "-emport", "6001" };
			
	public static final String[] STATUS_VERIFY_EMPORT_COMMAND  = { "java",	"-jar", "Watchdog.jar", 
																"status", "-emport", "6001" };

    public static String         wd_port                            = "4321";

    public static final String[] STATUS_EM_COMMAND_30_SEC           = { "java",
            "-jar", "WatchDog.jar", "status", "-startuptime", "30" };

    public static final String[] STATUS_EM_COMMAND_45_SEC           = { "java",
            "-jar", "WatchDog.jar", "status", "-startuptime", "45" };

    public static final String[] STATUS_EM_COMMAND_60_SEC           = { "java",
            "-jar", "WatchDog.jar", "status", "-startuptime", "60" };

    public static String         TRIAGE_MMAP_CNFGS_MNGMT_MODULE_JAR = "TriageMapConfigurationsManagementModule.jar";

    public static String         installFolderPath                  = ApmbaseConstants.EM_LOC
                                                                      + "/install";

    // Added for SAP Domain Permission Management Module

    public static String         sap_em_loc                         = System.getProperty("testbed_webapp.sap.em.loc");

    public static String         sap_em_port                        = System.getProperty("testbed_webapp.sap.port");

    public static String         sap_em_hostname                    = System.getProperty("testbed_webapp.sap.hostname");

    public static String         sap_em_clwws                     = "/CLWorkstation.jar";
	 
	  public static String         sap_em_libLoc                     = sap_em_loc+ "/lib";

    public static String         sap_em_admin_user                  = System.getProperty("testbed_webapp.sap.admin.user");

    public static String         sap_em_admin_passw                 = System.getProperty("testbed_webapp.sap.admin.passw");
	
    public static String         sap_weblogic_webapp_home_dir           = System.getProperty("testbed_webapp.wls.home");

    public static final String   EM_FOLDER_NAME                     = "/em";

    public static String         sap_em_clwJarFileLoc               = sap_em_loc
                                                                      + "/lib/CLWorkstation.jar";

    public static String         sap_transaction_tracer_query       = "trace transactions exceeding 0 ms in agents matching (.*) for 120 seconds";

    public static final String   EM_LOG_FILE_PRPTY                  = "log4j.appender.logfile.File";

    public static final String   AGENT_EM_DEFAULT_PORT              = "introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT";

    public static final String   AGENT_EM_DEFAULT_HOST              = "introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT";

    public static final String   EM_WEBAPPS_FOLDER_NAME             = "/webapps/";
    
    public static final String   WEBAPPS_SOURCE_DIR_PATH             = "/client/apmbase/webapp/";
	
	public static final String   sap_em_installerPath                  = "/examples/installer";
	
	public String[] EM_INSTALL_EXE_CMD   = {"cmd.exe", "/c", "introscope9.6.0.0windows.exe -i silent -f SampleResponseFile.Introscope.txt"};
    
    public String[] EM_UNINSTALL_EXE_CMD = {"cmd.exe", "/c", "Uninstall_Introscope.exe"};
    
    public int THREAD_SLEEP = 180;
    
    public static final String  SUCCESS_MESSAGE = "Successful";
	   
  //added for embasics
    public static String         SereverFilePath                = "/config/server.xml";

    public static final String   EM_LOGFILE_NAME 		            = "IntroscopeEnterpriseManager";

    public static final String   EM_WITH_SPACE_INSTALL_DIR_LOC = System.getProperty("testbed_webapp.em.space.loc");
	
	public String testUserXmlFilePath = "/config/testusers.xml";
	
	//Added for EM Basics
	  
    public  final static String  SAP_CLWJAR_LOCATION               = "/lib/CLWorkstation.jar";
    
    public  final static String  AUTOPROBE_lOG                     = ".Autoprobe.log";
	
	// added for smartstor
	
	public static final String CLONE_PARAMS = "cloneconnections 11 -cloneagents 2 -secondspertrace 15";
    
    public static final String  SMARTSTOR_DATA_LOC = "/SmartStorData";
    
  	 //added for supportability metric
    
    public static String         EM_DATA_FILE                             = EM_LOC+"/data";

    public static String         EM_DATA_BAK_FILE                             = EM_LOC+"/data_bak";

    public static String         EM_TRACES                            = EM_LOC+"/traces";
   
    public static String         EM_TRACES_BAK                            = EM_LOC+"/traces_bak";
	
	// added for mykons
	
	public static String         APP_SERVER_WEBSPHERE                = "websphere";

	// Added for SupportBundler Enhancements for Krakatau Module
	public static String START_SUPPORTBUNDLER = "SupportBundler.bat -u Admin";

	public static String START_SUPPORTBUNDLER1 = "SupportBundler.bat";
	
	public static String EXCLUDEFILE = "users.xml";
	
	public static String EXCLUDEFILE1 = "lax.jar";
	
	public static String EXCLUDEFILE2 = "com.wily.introscope.workstation.webstart_";
	
	public static String EXCLUDEFILE3 = "oracle";
	
	public static String EXCLUDEFILE4 = "shutoff";
	
	public static String EXCLUDEFILE5 = "custompbd";
	
	public static String EXCLUDEFILE6 = "IntroscopeEnterpriseManagertemp.properties";
	
	public static String FILEEXTENSION = ".jar.manifest.txt";
	
	public static String SET_EM_HVR = "@set EM_DIR=C:\\SW\\em";
	
	public static String SET_EM_HVR1 = "@set EM_DIR=C:\\sw\\em";

	public static String APPENDER_PERF_FILE = "log4j.appender.performance.File";
	
	public static String EXTERNAL = "external";
	
	//Added for slow collector
	public static String START_CORRUPTMETRICS_AGENT  = "sampleagent.bat";

	// Added for JDBC Driver
	public static String START_MD5ENCODER = "MD5Encoder.bat";
	
	public static String SHA2ENCODER = "SHA2Encoder";

	 //Added  - SAP Installer
	 public static final String   EM_EPAGENT_PROPERTIES_FILE       = "IntroscopeEPAgent.properties";
	   
	 //Added  - SAP Installer
	 public static final String   EM_EPAGENT_JAR_FILE       		  = "EPAgent.jar";
	 //Added  - SAP Installer
	 public static String         EM_SAP_EPAGENT_LOC                  = System.getProperty("testbed_client.epagent.home");
	 
	 //Added  - SAP Installer
	 public static String         EM_SAP_LOC                  = System.getProperty("testbed_client.sapem.install.parent.dir");
	 
	 //Added  - SAP Installer
	 public static String 		  EM_STARTED_STRING 				= "Introscope Enterprise Manager started.";
	 
	 //Added  - SAP Installer
	 public static String 		  LOG4J_INFO_LEVEL 				= "[INFO]";
	 
	 //Added  - SAP Installer
	 public static String 		  LOG4J_WARN_LEVEL 				= "[WARN]";
	 
	 //Added  - SAP Installer
	 public static String 		  LOG4J_ERROR_LEVEL 			= "[ERROR]";
	 
	 //Added  - SAP Installer
	 public static String 		  LOG4J_DEBUG_LEVEL 			= "[DEBUG]";
	 
	public static final String   EPAGENT_PROPERTIES_FILENAME        = "IntroscopeEPAgent.properties";

    public static final String   KEYSTORE_fILE_PATH                 = "/internal/server";

    public static final String   KEYSTORE_FILE_NAME                 = "/keystore";

    public static final String   EPAGENT_LOG_FILENAME               = "IntroscopeEPA.log";
	
	 // /added for mom js calc
	public static String MODULES = "modules";
	
	public static String SRCJSFILE = "srcjsfile";
	
	public static String DESTJSFILE = "destjsfile";
	
	public static String INVALID_FILE_TYPE_ENTERED = "Invalid File Type entered, values can be only [srcjsfile/destjsfile/modules]";
	
	public static String JS_SCRIPTS_FOLDER_LOC = "/scripts";
	
	public static String JS_EX_SCRIPTS_FOLDER_LOC = "/examples/scripts";
	
	public static String JS_CALC_MOM_PROP_FILE = "/config/internal/server/scripts/JavaScriptCalculatorsMOM.properties";

    	 
	 //Added for LDAP Configuration
	 public static String 		  EM_STOP_FAILED 				= "Introscope Enterprise Manager is not stopped properly.";
	 
	 //Added for LDAP Configuration
	 public static String 		  EM_START_FAILED 				= "Introscope Enterprise Manager is not started properly.";
	 
	 //Added for LDAP Configuration
	 public static String 		  REALMS_FILE_BACKUP_FAILED 	= "Back up of realm file is not done properly";
	 
	 //Added for LDAP Configuration
	 public static String 		  REALMS_FILE_REVERT_FAILED 	= "Revert of realm file is not done properly";
	 
	 //Added for LDAP Configuration
	 public static String 		  DOMAINS_FILE_BACKUP_FAILED 	= "Back up of domains file is not done properly";
	 
	 //Added for LDAP Configuration
	 public static String 		  DOMAINS_FILE_REVERT_FAILED 	= "Revert of domains file is not done properly";
	 
	 //Added for LDAP Configuration
	 public static String 		  USER_ADDITION_NOT_SUCCESSFUL 	= "user not added in users.xml";
	 
	 //Added for LDAP Configuration
	 public static String 		  USER_DELETION_NOT_SUCCESSFUL 	= "user not deleted in users.xml";
	 
	 //Added for supportability Metrics
	 public static String 		  HVR_SEARCH_PATTERN 	= "com.wily.introscope.tools.fakeagent.FakeAgent";
	 
	 //Added for supportability Metrics
	 public static String 		  COMMAND_JPS 	= "jps -mlv";
	 
	 // Added for DomainPermissionCluster Module
    
    public static String         DOMAIN_TAG                         = "domain";

    public static String         NAME_TAG                           = "name";

    public static String         USER_LITERAL                       = "user";

    public static String         GUEST_LITERAL                      = "Guest";

    public static String         PWD_LITERAL                        = "password";

    public static String         DOMAIN_LITERAL                     = "domains";

    public static String         SUPERDOMAIN_LITERAL                = "SuperDomain";

    public static String         DESCRIPTION_TAG                    = "description";

    public static String         AGENT_TAG                          = "agent";

    public static String         MAPPING_TAG                        = "mapping";

    public static String         GRANT_TAG                          = "grant";

    public static String         GROUP_TAG                          = "group";

    public static String         PERMISSION_TAG                     = "permission";

    //AgentControllability and BPEM    
	public static String         capsVM                             = "VM";
    public static String         smallVM                            = "vm";
    public static String         domainName                         = ".ca.com:";
    public static String         gcHeapMetric                       = "GC Heap,Bytes In Use";
    public static String         Smalltrue                          = "true";
    public static String         capitaltrue                        = "True";
    
    public static String 		emHealthMetric = "*SuperDomain*|Custom Metric Host (Virtual)|Custom "
    		+ "Metric Process (Virtual)|Custom Metric Agent (Virtual)|Enterprise Manager|"
    		+ "Health:CPU Capacity (%)"; 
    
	public static String agentConnectMetric = "*SuperDomain*|Custom Metric Host (Virtual)|Custom "
			+ "Metric Process (Virtual)|Custom Metric Agent (Virtual)|Enterprise Manager|"
			+ "Connections:Number of Agents"; 
    		
    public static String cemUser = "cemadmin";
    public static String cemPassw = "quality";
    
    public static String guestUser="Guest";
    public static String guestPassw="Guest";
    
    public static String emUser = "Admin";
    public static String emPassw = "";
    public static String emPort = "5001";
    public static String emSecureWebPort = "8444";
    public static String emSSLPort = "5443";

    public static String tomcatAgentProcess =  "Tomcat";
    public static String tomcatAgentName =  "TomcatAgent";
    public static String agentLogMessage = "[INFO] [IntroscopeAgent.IsengardServerConnectionManager] Connected controllable Agent to the Introscope Enterprise Manager";
    
    //AgentEM Failover 
    public static String failoverEnableDisableProperty="introscope.enterprisemanager.failover.enable";
    public static String failoverPrimaryEMProperty="introscope.enterprisemanager.failover.primary";
    public static String failoverSecondaryEMProperty="introscope.enterprisemanager.failover.secondary";
    public static String failoverInterval="introscope.enterprisemanager.failover.interval";
    
    public static String emLaxJavaOptions="-Xms1024m -XX:+UseConcMarkSweepGC -showversion -verbosegc -Dcom.wily.assert=false -Xmx1024m -Dmail.mime.charset=UTF-8 -Dorg.owasp.esapi.resources=./config/esapi -XX:+UseParNewGC -XX:CMSInitiatingOccupancyFraction=50 -XX:+HeapDumpOnOutOfMemoryError -Xss256k -XX:MaxPermSize=256m ";

    public static String primaryEmLogMessage="[INFO] [main] [Manager.HotFailover] The Introscope Enterprise Manager is running as a Primary EM";
    public static String secondaryEmLogMessage="[INFO] [main] [Manager.HotFailover] The Introscope Enterprise Manager is running as a Secondary EM";
    public static String nonFailoverEmLogMessage="[INFO] [main] [Manager.HotFailover] The Introscope Enterprise Manager is running as a Non-Failover EM";
    public static String primaryLockMessage="[INFO] [main] [Manager.HotFailover] Acquiring primary lock...";
    public static String secondaryLockMessage ="[INFO] [main] [Manager.HotFailover] Acquiring secondary lock...";
    public static String emFailoverModeLogMessage="The Introscope Enterprise Manager is configured as a Secondary EM";
    public static String momModeMessage="The Introscope Enterprise Manager is set to run in MOM mode.";
    public static String collectorModeMessage="The Introscope Enterprise Manager is set to run in Collector mode.";
    public static String failoverEmShudownMessage="Shutting down because a Primary EM is waiting to start";
    public static String emErrorLogMessage="Select failed for com.timestock.tess.data.objects.Monitor";
    public static String emFailoverPrimaryConfigurationMessage = "[INFO] [main] [Manager.HotFailover] The Introscope Enterprise Manager is configured as a Primary EM";
    
    public static String clusteringMode="introscope.enterprisemanager.clustering.mode";
    public static String agentAllowed="introscope.apm.agentcontrol.agent.allowed";
    public static String emMode="introscope.enterprisemanager.clustering.mode";
    
    //other properties
    public static String SUPPORT_LOG_FILE = "log4j.appender.supportlogfile.File";	
	public static String QUERY_LOG_FILE = "log4j.appender.querylog.File";
	public static String SMARTSTOR_DIR = "introscope.enterprisemanager.smartstor.directory";
	public static String SMARTSTOR_DIR_ARCHIVE ="introscope.enterprisemanager.smartstor.directory.archive";
	public static String TRAN_EVENTS_STORAGE = "introscope.enterprisemanager.transactionevents.storage.dir";
	public static String EM_DIR_CONFIG = "introscope.enterprisemanager.directory.config";
	public static String DOMAINCONFIG_DYNAMICUPDATE_PROPERTY = "introscope.enterprisemanager.domainsconfiguration.dynamicupdate.enable";
	
	/***
	 * XPATH Constants
	 */
	
    public static final String validateCertificatesExpr =
        "/Configure/Call/Arg/New[@class=\"com.wily.webserver.TrustingSslSocketConnector\"]/Set[@name=\"validateCertificates\"]";
    public static final String needClientAuthExpr =
        "/Configure/Call/Arg/New[@class=\"com.wily.webserver.TrustingSslSocketConnector\"]/Set[@name=\"needClientAuth\"]";
    public static final String verifyHostnamesExpr =
        "/Configure/Call/Arg/New[@class=\"com.wily.webserver.TrustingSslSocketConnector\"]/Set[@name=\"verifyHostnames\"]";
	
    // user.xml value
    public static final String CEM_ADMIN_USER = "cemadmin";
    public static final String SAAS_ADMIN_USER = "SaasAdmin";
    public static final String ADMIN_USER = "Admin";
    public static final String GUEST_USER = "Guest";
    public static final String CEM_CONFIG_GROUP = "CEM Configuration Administrator";
    public static final String CEM_SYS_ADMIN_GROUP = "CEM System Administrator";
    public static final String ADMIN_GROUP = "Admin";
    public static final String CEM_TENANT_GROUP = "CEM Tenant Administrator";
    public static final String CEM_ANALYST_GROUP = "CEM Analyst";
    public static final String CEM_INCIDENT_ANALYST_GROUP = "CEM Incident Analyst";
    
    // Management Module
    public static final String WELCOME_DASHBOARD = "Welcome to APM Dashboards";
    public static final String DEFAULT_MM = "Default";
    public static final String DEFAULT_MM_JAR = "DefaultMM.jar";
    
    //JDBC Driver
	public static final String log4jInfoProp = "log4j.logger.Manager=INFO,console,logfile";
	public static final String log4jDebugProp = "log4j.logger.Manager=DEBUG,console,logfile";
	public static final String localeDe = "introscope.enterprisemanager.jdbc.locale.language=de";
	public static final String regionDe = "introscope.enterprisemanager.jdbc.locale.region=de";
	public static final String jdbcSynchronousTrueProp = "introscope.enterprisemanager.jdbc.synchronous=true";
	public static final String jdbcSynchronousFalseProp = "introscope.enterprisemanager.jdbc.synchronous=false";
    
}
