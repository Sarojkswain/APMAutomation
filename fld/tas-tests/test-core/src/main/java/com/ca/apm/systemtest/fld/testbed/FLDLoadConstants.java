/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed;


/**
 * Role and Machine constants for the FLD loads
 * @author keyja01
 *
 */
public interface FLDLoadConstants {
    public static final String TOMCAT_6_MACHINE_ID = "tomcat6Machine";
    public static final String TOMCAT_7_MACHINE_ID = "tomcat7Machine";
    public static final String TOMCAT_9080_MACHINE_ID = "tomcat9080Machine";
    public static final String TOMCAT_9081_MACHINE_ID = "tomcat9081Machine";
    public static final String[] TOMCAT_MACHINE_IDS = {TOMCAT_6_MACHINE_ID, TOMCAT_9080_MACHINE_ID};
    
    public static final String TOMCAT_6_ROLE_ID = "tomcat6Role";
    public static final String TOMCAT_6_AGENT_ROLE_ID = "tomcat6AgentRole";
    public static final String TOMCAT_7_ROLE_ID = "tomcat7Role";
    public static final String TOMCAT_7_AGENT_ROLE_ID = "tomcat7AgentRole";
    public static final String TOMCAT_9080_ROLE_ID = "tomcat9080Role";
    public static final String TOMCAT_9080_AGENT_ROLE_ID = "tomcat9080AgentRole";
    public static final String TOMCAT_9081_ROLE_ID = "tomcat9081Role";
    public static final String TOMCAT_9081_AGENT_ROLE_ID = "tomcat9081AgentRole";
    public static final String[] TOMCAT_ROLE_IDS = {TOMCAT_6_ROLE_ID, TOMCAT_7_ROLE_ID, TOMCAT_9080_ROLE_ID, TOMCAT_9081_ROLE_ID};

    public static final String TOMCAT_6_AXIS2_ROLE_ID = "tomcat6axis2Role";
    public static final String TOMCAT_6_QATESTAPP_ROLE_ID = "tomcat6qatestappRole";
    public static final String TOMCAT_6_TESTAPP_ROLE_ID = "tomcat6testappRole";
    public static final String TOMCAT_7_AXIS2_ROLE_ID = "tomcat7axis2Role";
    public static final String TOMCAT_7_QATESTAPP_ROLE_ID = "tomcat7qatestappRole";
    public static final String TOMCAT_9080_AXIS2_ROLE_ID = "tomcat9080axis2Role";
    public static final String TOMCAT_9080_WURLITZER_ROLE_ID = "tomcat9080wurlitzerRole";
    public static final String TOMCAT_9081_AXIS2_ROLE_ID = "tomcat9081axis2Role";
    public static final String TOMCAT_9081_WURLITZER_ROLE_ID = "tomcat9081wurlitzerRole";
    
    public static final String HISTORICAL_METRICS_TOMCAT_ROLE_ID = "historicalMetricsTomcatRole";
    
    public static final String WEBSPHERE_01_MACHINE_ID = "websphere01";
    public static final String WEBSPHERE_02_MACHINE_ID = "websphere02";
    public static final String WEBSPHERE_03_MACHINE_ID = "websphere03";
    public static final String WEBSPHERE_04_MACHINE_ID = "websphere04";
    public static final String WEBSPHERE_01_ROLE_ID = "websphereRole01";
    public static final String WEBSPHERE_02_ROLE_ID = "websphereRole02";
    public static final String WEBSPHERE_03_ROLE_ID = "websphereRole03";
    public static final String WEBSPHERE_04_ROLE_ID = "websphereRole04";
    
    public static final String JBOSS_MACHINE = "JbossMachine";
    public static final String JBOSS6_ROLE_ID = JBOSS_MACHINE + "-JBoss6";
    public static final String JBOSS7_ROLE_ID = JBOSS_MACHINE + "-JBoss7";

    public static final String JMETER_MACHINE_01_ID = "jmeterMachine01";
    public static final String JMETER_MACHINE_02_ID = "jmeterMachine02";
    public static final String JMETER_MACHINE_03_ID = "jmeterMachine03";
    public static final String JMETER_MACHINE_04_ID = "jmeterMachine04";
    public static final String JMETER_ROLE_01_ID = "jmeterRole01";
    public static final String JMETER_ROLE_02_ID = "jmeterRole02";
    public static final String JMETER_ROLE_03_ID = "jmeterRole03";
    public static final String JMETER_ROLE_04_ID = "jmeterRole04";
    public static final String JMETER_LOAD_ROLE_TOMCAT9080_01_ID = "jmeterTomcat9080Load01Role";
    public static final String JMETER_LOAD_ROLE_TOMCAT9081_01_ID = "jmeterTomcat9081Load01Role";
    public static final String JMETER_LOAD_ROLE_WURLITZER_TOMCAT9080_01_ID = "jmeterWurlitzerTomcat9080Load01Role";
    public static final String JMETER_LOAD_ROLE_WURLITZER_TOMCAT9081_01_ID = "jmeterWurlitzerTomcat9081Load01Role";
    public static final String JMETER_LOAD_ROLE_6TOMCAT9091_01_ID = "jmeter6Tomcat9091Load01Role";
    public static final String JMETER_LOAD_ROLE_6TOMCAT9091T_01_ID = "jmeter6Tomcat9091TLoad01Role";
    public static final String JMETER_LOAD_ROLE_6TOMCAT_LOADTEST_01_ID = "jmeter6TomcatLoadTest01Role";
    public static final String ADD_LOADTEST_ROLE_JMETER_6TOMCAT_01_ID =  "addLoadTestInJMeter6Tomcat01Role";
    public static final String JMETER_LOAD_ROLE_7TOMCAT9090_01_ID = "jmeter7Tomcat9090Load01Role";
    public static final String JMETER_LOAD_ROLE_WAS_01_ID = "jmeterWASLoad01Role";
    public static final String JMETER_LOAD_ROLE_WAS_BRT_01_ID = "jmeterWASBRTTestAppLoad01Role";
    public static final String JMETER_LOAD_ROLE_SOA_WLS7001_01_ID = "jmeterSOA7001WebLogic01Role";
    public static final String JMETER_LOAD_ROLE_SOA_WLS7002_01_ID = "jmeterSOA7002WebLogic01Role";
    public static final String JMETER_LOAD_ROLE_SOA_WLS7001_02_ID = "jmeterSOA7001WebLogic02Role";
    public static final String JMETER_LOAD_ROLE_SOA_WLS7002_02_ID = "jmeterSOA7002WebLogic02Role";
    public static final String JMETER_LOAD_ROLE_APPMAP_ID = "jmeterAppMapRole";
    public static final String JMETER_LOAD_ROLE_APPMAP_TEAMCENTER_ID = "jmeterAppMapTeamCenterRole";
    public static final String JMETER_LOAD_ROLE_JBOSS6_01_ID = "jmeter6JBoss01Role";
    public static final String JMETER_LOAD_ROLE_JBOSS7_01_ID = "jmeter7JBoss01Role";
    public static final String JMETER_LOAD_ROLE_FLDNET01_01_ID = "jmeterDot1NET01Role";
    public static final String JMETER_LOAD_ROLE_FLDNET01_02_ID = "jmeterDot1NET02Role";
    public static final String JMETER_LOAD_ROLE_FLDNET01_03_ID = "jmeterDot1NET03Role";
    public static final String JMETER_LOAD_ROLE_FLDNET01_04_ID = "jmeterDot1NET04Role";
    public static final String JMETER_LOAD_ROLE_FLDNET02_01_ID = "jmeterDot2NET01Role";
    public static final String JMETER_LOAD_ROLE_FLDNET02_02_ID = "jmeterDot2NET02Role";
    public static final String JMETER_LOAD_ROLE_FLDNET02_03_ID = "jmeterDot2NET03Role";
    public static final String JMETER_LOAD_ROLE_FLDNET02_04_ID = "jmeterDot2NET04Role";

    public static final String HAMMOND_MACHINE_ID = "hammondMachine";
    public static final String HAMMOND_LOAD_ROLE_ID = "hammondLoadRole";
    public static final String HAMMOND_MACHINE_1_ID = "hammond1Machine";
    public static final String HAMMOND_LOAD_ROLE_1_ID = "hammondLoad1Role";
    public static final String HAMMOND_MACHINE_2_ID = "hammond2Machine";
    public static final String HAMMOND_LOAD_ROLE_2_ID = "hammondLoad2Role";

    public static final String WURLITZER_01_MACHINE_ID = "wurlitzerMachine01";
    public static final String WURLITZER_LOAD_BASE01_LOAD01_ROLE_ID = "wurlitzerLoadBase01Load01";
    public static final String WURLITZER_LOAD_BASE01_LOAD02_ROLE_ID = "wurlitzerLoadBase01Load02";
    public static final String WURLITZER_LOAD_BASE01_LOAD03_ROLE_ID = "wurlitzerLoadBase01Load03";
    public static final String WURLITZER_LOAD_BASE01_LOAD04_ROLE_ID = "wurlitzerLoadBase01Load04";
    public static final String WURLITZER_LOAD_BASE01_LOAD05_ROLE_ID = "wurlitzerLoadBase01Load05";
    public static final String WURLITZER_BASE01_ROLE_ID = "wurlitzerBase01";

    public static final String WURLITZER_02_MACHINE_ID = "wurlitzerMachine02";
    public static final String WURLITZER_LOAD_BASE02_LOAD01_ROLE_ID = "wurlitzerLoadBase02Load01";
    public static final String WURLITZER_LOAD_BASE02_LOAD02_ROLE_ID = "wurlitzerLoadBase02Load02";
    public static final String WURLITZER_LOAD_BASE02_LOAD03_ROLE_ID = "wurlitzerLoadBase02Load03";
    public static final String WURLITZER_LOAD_BASE02_LOAD04_ROLE_ID = "wurlitzerLoadBase02Load04";
    public static final String WURLITZER_LOAD_BASE02_LOAD05_ROLE_ID = "wurlitzerLoadBase02Load05";
    public static final String WURLITZER_BASE02_ROLE_ID = "wurlitzerBase02";

    public static final String WURLITZER_03_MACHINE_ID = "wurlitzerMachine03";
    public static final String WURLITZER_LOAD_BASE03_LOAD01_ROLE_ID = "wurlitzerLoadBase03Load01";
    public static final String WURLITZER_LOAD_BASE03_LOAD02_ROLE_ID = "wurlitzerLoadBase03Load02";
    public static final String WURLITZER_LOAD_BASE03_LOAD03_ROLE_ID = "wurlitzerLoadBase03Load03";
    public static final String WURLITZER_LOAD_BASE03_LOAD04_ROLE_ID = "wurlitzerLoadBase03Load04";
    public static final String WURLITZER_LOAD_BASE03_LOAD05_ROLE_ID = "wurlitzerLoadBase03Load05";
    public static final String WURLITZER_BASE03_ROLE_ID = "wurlitzerBase03";

    public static final String WURLITZER_04_MACHINE_ID = "wurlitzerMachine04";
    public static final String WURLITZER_LOAD_BASE04_LOAD01_ROLE_ID = "wurlitzerLoadBase04Load01";
    public static final String WURLITZER_LOAD_BASE04_LOAD02_ROLE_ID = "wurlitzerLoadBase04Load02";
    public static final String WURLITZER_LOAD_BASE04_LOAD03_ROLE_ID = "wurlitzerLoadBase04Load03";
    public static final String WURLITZER_LOAD_BASE04_LOAD04_ROLE_ID = "wurlitzerLoadBase04Load04";
    public static final String WURLITZER_LOAD_BASE04_LOAD05_ROLE_ID = "wurlitzerLoadBase04Load05";
    public static final String WURLITZER_BASE04_ROLE_ID = "wurlitzerBase04";

    public static final String WURLITZER_05_MACHINE_ID = "wurlitzerMachine05";
    public static final String WURLITZER_LOAD_BASE05_LOAD01_ROLE_ID = "wurlitzerLoadBase05Load01";
    public static final String WURLITZER_LOAD_BASE05_LOAD02_ROLE_ID = "wurlitzerLoadBase05Load02";
    public static final String WURLITZER_LOAD_BASE05_LOAD03_ROLE_ID = "wurlitzerLoadBase05Load03";
    public static final String WURLITZER_LOAD_BASE05_LOAD04_ROLE_ID = "wurlitzerLoadBase05Load04";
    public static final String WURLITZER_LOAD_BASE05_LOAD05_ROLE_ID = "wurlitzerLoadBase05Load05";
    public static final String WURLITZER_BASE05_ROLE_ID = "wurlitzerBase05";
    
    public static final String WURLITZER_06_MACHINE_ID = "wurlitzerMachine06";
    public static final String WURLITZER_LOAD_BASE06_LOAD01_ROLE_ID = "wurlitzerLoadBase06Load01";
    public static final String WURLITZER_LOAD_BASE06_LOAD02_ROLE_ID = "wurlitzerLoadBase06Load02";
    public static final String WURLITZER_LOAD_BASE06_LOAD03_ROLE_ID = "wurlitzerLoadBase06Load03";
    public static final String WURLITZER_BASE06_ROLE_ID = "wurlitzerBase06";
    
    public static final String WURLITZER_07_MACHINE_ID = "wurlitzerMachine07";
    public static final String WURLITZER_BASE07_ROLE_ID = "wurlitzerBase07";
    
    public static final String[] WURLITZER_LOAD_ROLES = {
        WURLITZER_LOAD_BASE01_LOAD01_ROLE_ID, WURLITZER_LOAD_BASE01_LOAD02_ROLE_ID, 
        WURLITZER_LOAD_BASE01_LOAD03_ROLE_ID, WURLITZER_LOAD_BASE01_LOAD04_ROLE_ID, 
        WURLITZER_LOAD_BASE01_LOAD05_ROLE_ID, WURLITZER_LOAD_BASE02_LOAD01_ROLE_ID,
        WURLITZER_LOAD_BASE02_LOAD02_ROLE_ID, WURLITZER_LOAD_BASE02_LOAD03_ROLE_ID, 
        WURLITZER_LOAD_BASE02_LOAD04_ROLE_ID, WURLITZER_LOAD_BASE02_LOAD05_ROLE_ID,
        WURLITZER_LOAD_BASE03_LOAD01_ROLE_ID, WURLITZER_LOAD_BASE03_LOAD02_ROLE_ID,
        WURLITZER_LOAD_BASE03_LOAD03_ROLE_ID, WURLITZER_LOAD_BASE03_LOAD04_ROLE_ID, 
        WURLITZER_LOAD_BASE03_LOAD05_ROLE_ID, WURLITZER_LOAD_BASE04_LOAD01_ROLE_ID, 
        WURLITZER_LOAD_BASE04_LOAD02_ROLE_ID, WURLITZER_LOAD_BASE04_LOAD03_ROLE_ID,
        WURLITZER_LOAD_BASE04_LOAD04_ROLE_ID, WURLITZER_LOAD_BASE04_LOAD05_ROLE_ID, 
        WURLITZER_LOAD_BASE05_LOAD01_ROLE_ID, WURLITZER_LOAD_BASE05_LOAD02_ROLE_ID, 
        WURLITZER_LOAD_BASE05_LOAD03_ROLE_ID, WURLITZER_LOAD_BASE06_LOAD01_ROLE_ID,
        // , WURLITZER_LOAD_BASE05_LOAD04_ROLE_ID, WURLITZER_LOAD_BASE05_LOAD05_ROLE_ID
        // , WURLITZER_LOAD_BASE06_LOAD02_ROLE_ID, WURLITZER_LOAD_BASE06_LOAD03_ROLE_ID
    };
    

    public static final String METRICSYNTH_01_MACHINE_ID = "metricSynthMachine01";
    public static final String METRICSYNTH_02_MACHINE_ID = "metricSynthMachine02";
    public static final String METRICSYNTH_03_MACHINE_ID = "metricSynthMachine03";
    public static final String METRICSYNTH_04_MACHINE_ID = "metricSynthMachine04";
    public static final String METRICSYNTH_05_MACHINE_ID = "metricSynthMachine05";
    public static final String METRICSYNTH_06_MACHINE_ID = "metricSynthMachine06";
    public static final String METRICSYNTH_07_MACHINE_ID = "metricSynthMachine07";
    
    public static final String METRICSYNTH_01_ROLE_ID = "metricSynthRole01";
    public static final String METRICSYNTH_02_ROLE_ID = "metricSynthRole02";
    public static final String METRICSYNTH_03_ROLE_ID = "metricSynthRole03";
    public static final String METRICSYNTH_04_ROLE_ID = "metricSynthRole04";
    public static final String METRICSYNTH_05_ROLE_ID = "metricSynthRole05";
    public static final String METRICSYNTH_06_ROLE_ID = "metricSynthRole06";
    public static final String METRICSYNTH_07_ROLE_ID = "metricSynthRole07";

    public static final String EMLOAD_01_MACHINE_ID = "emLoadMachine01";
    
    public static final String HVR_LOAD_ROLE_ID = "hvrLoadRole";
    
    public static final String WLS_01_MACHINE_ID = "wls01MachineId";
    public static final String WLS_01_INSTALLATION_ROLE_ID = "wlsInstallationRole01";
    public static final String WLS_01_BASE_AGENT_ROLE_ID = "wlsBaseAgentRole01";
    public static final String WLS_01_SERVER_01_ROLE_ID = "wls01Server01Role";
    public static final String WLS_01_SERVER_02_ROLE_ID = "wls01Server02Role";
    
    public static final String WLS_02_MACHINE_ID = "wls02MachineId";
    public static final String WLS_02_INSTALLATION_ROLE_ID = "wlsInstallationRole02";
    public static final String WLS_02_BASE_AGENT_ROLE_ID = "wlsBaseAgentRole02";
    public static final String WLS_02_SERVER_01_ROLE_ID = "wls02Server01Role";
    public static final String WLS_02_SERVER_02_ROLE_ID = "wls02Server02Role";
    
    /*
     * WLS03 and WLS04 provide the cross cluster load
     */
    public static final String WLS03_MACHINE_ID = "wls03MachineId";
    public static final String WLS03_CLIENT_ROLE_ID = "wls03ClientRole";
    public static final String WLS03_ROLE_ID = "wls03Role";
    
    public static final String WLS04_MACHINE_ID = "wls04MachineId";
    public static final String WLS04_ROLE_ID = "wls04Role";
    
    public static final String GEOLOCATION_LOAD_TIM01_ROLE_ID = "geolocationLoadTim01Role";
    
    public static final String JDBC_QUERY_MACHINE_ID = "jdbcQueryMachine";
    public static final String JDBC_QUERY_ROLE_ID = "jdbcQueryRole";
    
    public static final String FAKEWS01_MACHINE_ID = "fakeWS01Machine";
    public static final String FAKEWS02_MACHINE_ID = "fakeWS02Machine";
    public static final String FAKEWS01_ROLE_ID = "fakeWS01Role";
    public static final String FAKEWS02_ROLE_ID = "fakeWS02Role";
    
    public static final String ENTITY_ALERT_MACHINE_ID = "entityAlertMachine";
    public static final String ENTITY_ALERT_ROLE_ID = "entityAlertRole";
    
    public static final String FLD_CONTROLLER_MACHINE_ID = "fldControllerMachine";
    public static final String FLD_CONTROLLER_ROLE_ID = "fldControllerRole";
    
    public static final String ACC_MACHINE_ID = "accMachine";
    public static final String ACC_ROLE_ID = "accRole";

    public static final String CLW_MACHINE_ID = "clwMachine";
    public static final String CLW_ROLE_ID = "clwRole";

    public static final String WORKSTATION_LOAD_MACHINE_ID= "wsLoadMachine";
    public static final String WORKSTATION_LOAD_ROLE_ID= "wsLoadRole";
    
    public static final String DYNAMIC_INSTR_MACHINE_ID = "dynamicInstrumentationMachine";
    public static final String DYNAMIC_INSTR_ROLE_ID = "dynamicInstrumentationRole";

    public static final String WEBVIEW_LOAD_01_MACHINE_ID = "wv01";
    public static final String WEBVIEW_LOAD_02_MACHINE_ID = "wv02";
    public static final String WEBVIEW_LOAD_03_MACHINE_ID = "wv03";
    public static final String WEBVIEW_LOAD_04_MACHINE_ID = "wv04";
    public static final String WEBVIEW_LOAD_05_MACHINE_ID = "wv05";
    public static final String[] WEBVIEW_LOAD_MACHINE_IDS = {WEBVIEW_LOAD_01_MACHINE_ID,
            WEBVIEW_LOAD_02_MACHINE_ID, WEBVIEW_LOAD_03_MACHINE_ID, WEBVIEW_LOAD_04_MACHINE_ID,
            WEBVIEW_LOAD_05_MACHINE_ID};
    
    public static final String REAL_WORKSTATION_01_ROLE_ID = "realWorkstationMachine01";
    public static final String REAL_WORKSTATION_02_ROLE_ID = "realWorkstationMachine02";
    public static final String REAL_WORKSTATION_01_MACHINE_ID = "realWorkstationRole01";
    public static final String REAL_WORKSTATION_02_MACHINE_ID = "realWorkstationRole02";
    
    public static final String DOTNET_AGENT_ROLE_ID = "dotNetAgentRole";
    public static final String DOTNET_APPS_ROLE_ID = "dotNetAppsRole";
    public static final String IIS_ENABLE_ROLE_ID = "iisEnableRole";
    public static final String IIS_UPDATE_PORT_ROLE_ID = "iisUpdatePortRole";
    public static final String IIS_REGISTER_ROLE_ID = "iisRegisterRole";
    public static final String ODP_NET_SCRIPTS_ROLE_ID = "odpNetScriptsRole";
    public static final String COPY_TNS_FILE_ROLE_ID = "copyTnsFileRole";
    public static final String DOTNET_MACHINE1 = "dotNetMachine1";
    public static final String DOTNET_MACHINE2 = "dotNetMachine2";
    
    public static final String FLEX_ECHO_WEBAPP_MACHINE_ID = "flexEchoWebappMachine";
    public static final String JMETER_MACHINE_ID = "flexJMeterMachine";
    public static final String FLEX_ECHO_WEBAPP_ROLE_ID = "flexEchoWebappRole";
    public static final String TC_ROLE_ID = "flexTomcatRole";
    public static final String JMETER_ROLE_AMF_ID = "jmeterRoleAmf";
    public static final String JMETER_ROLE_AMFX_ID = "jmeterRoleAmfx";
    public static final String FLD_WORKFLOWS_ROLE_ID = "fldWorkflowsRole";
    public static final String JAVA_18_FLEX_MACHINE_ROLE = "java18RoleFlexMachine";
    public static final String JAVA_18_JMETER_MACHNE_ROLE = "java18RoleJmeterMachine";

    public static final String WAS_XCLUSTER_CLIENT_ROLE_ID = "wasXClusterRole";
    public static final String LOAD1_ROLE_ID = "loadRole1";
    public static final String LOAD2_ROLE_ID = "loadRole2";
    public static final String LOAD3_ROLE_ID = "loadRole3";

    public static final String AGENT_SESSION_RECORDING_MACHINE_ID = "agentRecordingSessionMachine";
    public static final String TIM_SESSION_RECORDING_MACHINE_ID = "timRecordingSessionMachine";
    public static final String AGENT_SESSION_RECORDING_ROLE_ID = "agentRecordingSessionRole";
    public static final String TIM_SESSION_RECORDING_ROLE_ID = "timRecordingSessionRole";
    public static final int DURATION_24_HOURS_IN_MILLIS = 86400000;
    public static final int DEFAULT_AGENT_RECORDING_SESSION_DURATION_IN_MILLIS = 28800000;//8 hours
    
    // Hostname aliases for agent configuration - since "fldjboss01" is much more descriptive than tas-cz-fld-na8
    public static final String JBOSS01_HOST_NAME = "fldjboss01";
    public static final String TOMCAT_HOST_NAME = "fldtomcat01";
    public static final String WAS_HOST_NAME = "fldwas01";
    public static final String DOTNET_01_HOST_NAME = "fldnet01";
    public static final String DOTNET_02_HOST_NAME = "fldnet02";
    public static final String WLS01_HOST_NAME = "fldwls01";
    public static final String WLS02_HOST_NAME = "fldwls02";
    
    public static final String TOMCAT6_AGENT = "Tomcat6";
    public static final String TOMCAT7_AGENT = "Tomcat7";
    public static final String TOMCAT_AGENT_9080 = "TomcatAgent_9080";
    public static final String TOMCAT_AGENT_9081 = "TomcatAgent_9081";
    public static final String WEBLOGIC_WURLITZER_1_AGENT = "WebLogic_Wurlitzer1";
    public static final String WEBLOGIC_WURLITZER_2_AGENT = "WebLogic_Wurlitzer2";
    public static final String JBOSS_AGENT = "JbossAgent";
    public static final String WAS85_AGENT = "WebSphere85";

    public static final String SELENIUM_HUB_MACHINE_ID = "seleniumHubMachine";
    public static final String SELENIUM_HUB_ROLE_ID = "seleniumHubRole";

    public static final String SELENIUM_ATCUI_HUB_MACHINE_ID = "seleniumATCUIHubMachine";
    public static final String SELENIUM_ATCUI_HUB_ROLE_ID = "seleniumATCUIHubRole";
    public static final String ATCUI_SET_LOAD_ROLE_ID = "atcuiSetLoadRole";
    public static final String APM_JDBC_QUERY_LOAD_ROLE_ID = "apmJDBCQueryLoadRole";
    public static final String CEM_TESS_LOAD_ROLE_ID = "cemTessLoadRole";
    public static final String CEM_TESS_LOAD_MACHINE_ID = "cemTessLoadMachine";

    // .NET http/https conn test
    public static final String DOTNET_HTTP_CONN_MACHINE1 = "dotNetHttpConnMachine1";
    public static final String DOTNET_HTTP_CONN_MACHINE2 = "dotNetHttpConnMachine2";
    public static final String DOTNET_HTTP_CONN_MACHINE3 = "dotNetHttpConnMachine3";
    public static final String DOTNET_HTTP_CONN_MACHINE4 = "dotNetHttpConnMachine4";
    public static final String DOTNET_HTTP_CONN_MACHINE5 = "dotNetHttpConnMachine5";
    public static final String DOTNET_HTTP_CONN_MACHINE6 = "dotNetHttpConnMachine6";
    public static final String DOTNET_HTTP_CONN_MACHINE7 = "dotNetHttpConnMachine7";
    public static final String DOTNET_HTTP_CONN_MACHINE8 = "dotNetHttpConnMachine8";
    public static final String[] DOTNET_HTTP_CONN_MACHINES = {
        DOTNET_HTTP_CONN_MACHINE1,
        DOTNET_HTTP_CONN_MACHINE2/*,
        DOTNET_HTTP_CONN_MACHINE3,
        DOTNET_HTTP_CONN_MACHINE4,
        DOTNET_HTTP_CONN_MACHINE5,
        DOTNET_HTTP_CONN_MACHINE6,
        DOTNET_HTTP_CONN_MACHINE7,
        DOTNET_HTTP_CONN_MACHINE8*/
    };
    public static final String DOTNET_HTTP_CONN_01_HOST_NAME = "fldnet03";
    public static final String DOTNET_HTTP_CONN_02_HOST_NAME = "fldnet04";
    public static final String DOTNET_HTTP_CONN_03_HOST_NAME = "fldnet05";
    public static final String DOTNET_HTTP_CONN_04_HOST_NAME = "fldnet06";
    public static final String DOTNET_HTTP_CONN_05_HOST_NAME = "fldnet07";
    public static final String DOTNET_HTTP_CONN_06_HOST_NAME = "fldnet08";
    public static final String DOTNET_HTTP_CONN_07_HOST_NAME = "fldnet09";
    public static final String DOTNET_HTTP_CONN_08_HOST_NAME = "fldnet10";
    public static final String[] DOTNET_HTTP_CONN_HOST_NAMES = {
        DOTNET_HTTP_CONN_01_HOST_NAME,
        DOTNET_HTTP_CONN_02_HOST_NAME,
        DOTNET_HTTP_CONN_03_HOST_NAME,
        DOTNET_HTTP_CONN_04_HOST_NAME,
        DOTNET_HTTP_CONN_05_HOST_NAME,
        DOTNET_HTTP_CONN_06_HOST_NAME,
        DOTNET_HTTP_CONN_07_HOST_NAME,
        DOTNET_HTTP_CONN_08_HOST_NAME
    };

    public static final String DOTNET_HTTP_CONN_AGENT_ROLE_ID = DOTNET_AGENT_ROLE_ID; // "dotNetHttpConnAgentRole";
    public static final String DOTNET_HTTP_CONN_APPS_ROLE_ID = DOTNET_APPS_ROLE_ID; // "dotNetHttpConnAppsRole";
    public static final String DOTNET_HTTP_CONN_IIS_ENABLE_ROLE_ID = IIS_ENABLE_ROLE_ID; // "dotNetHttpConnIisEnableRole";
    public static final String DOTNET_HTTP_CONN_IIS_UPDATE_PORT_ROLE_ID = IIS_UPDATE_PORT_ROLE_ID; // "dotNetHttpConnIisUpdatePortRole";
    public static final String DOTNET_HTTP_CONN_IIS_REGISTER_ROLE_ID = IIS_REGISTER_ROLE_ID; // "dotNetHttpConnIisRegisterRole";
    public static final String DOTNET_HTTP_CONN_ODP_NET_SCRIPTS_ROLE_ID = ODP_NET_SCRIPTS_ROLE_ID; // "dotNetHttpConnOdpNetScriptsRole";
    public static final String DOTNET_HTTP_CONN_COPY_TNS_FILE_ROLE_ID = COPY_TNS_FILE_ROLE_ID; // "dotNetHttpConnCopyTnsFileRole";
    // //

}
