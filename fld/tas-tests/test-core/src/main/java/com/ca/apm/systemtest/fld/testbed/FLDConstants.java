/**
 * 
 */
package com.ca.apm.systemtest.fld.testbed;

/**
 * Constants used for machines and roles in the FLD
 * @author keyja01
 *
 */
public interface FLDConstants {
    // machines IDs
    public static final String MOM_MACHINE_ID = "momMachine";
    public static final String COLL01_MACHINE_ID = "coll01Machine";
    public static final String COLL02_MACHINE_ID = "coll02Machine";
    public static final String COLL03_MACHINE_ID = "coll03Machine";
    public static final String COLL04_MACHINE_ID = "coll04Machine";
    public static final String COLL05_MACHINE_ID = "coll05Machine";
    public static final String COLL06_MACHINE_ID = "coll06Machine";
    public static final String COLL07_MACHINE_ID = "coll07Machine";
    public static final String COLL08_MACHINE_ID = "coll08Machine";
    public static final String COLL09_MACHINE_ID = "coll09Machine";
    public static final String COLL10_MACHINE_ID = "coll10Machine";
    public static final String[] COLL_MACHINES = {
                                                  COLL01_MACHINE_ID, 
                                                  COLL02_MACHINE_ID,
            COLL03_MACHINE_ID, COLL04_MACHINE_ID, COLL05_MACHINE_ID, COLL06_MACHINE_ID,
            COLL07_MACHINE_ID, COLL08_MACHINE_ID, COLL09_MACHINE_ID, COLL10_MACHINE_ID
                                                 };
    public static final String TIM01_MACHINE_ID = "tim01Machine";
    public static final String TIM02_MACHINE_ID = "tim02Machine";
    public static final String TIM03_MACHINE_ID = "tim03Machine";
    public static final String TIM04_MACHINE_ID = "tim04Machine";
    public static final String TIM05_MACHINE_ID = "tim05Machine";
    public static final String[] TIM_MACHINES = {
                                                  TIM01_MACHINE_ID, 
          TIM02_MACHINE_ID, TIM03_MACHINE_ID, TIM04_MACHINE_ID, TIM05_MACHINE_ID
                                                 };
    public static final String WEBVIEW_MACHINE_ID = "webviewMachine";
    public static final String WEBVIEW2_MACHINE_ID = "webview2Machine";
    public static final String DATABASE_MACHINE_ID = "databaseMachine";
    public static final String MOM2_MACHINE_ID = "momMachine2";
    public static final String COLL21_MACHINE_ID = "coll21Machine";
    public static final String COLL22_MACHINE_ID = "coll22Machine";
    public static final String[] COLL2_MACHINES = {
                                                  COLL21_MACHINE_ID, 
                                                  COLL22_MACHINE_ID
                                                 };
    public static final String AGC_MACHINE_ID = "agcMachine";
    public static final String COLL_AGC_MACHINE_ID = "collAgcMachine";
    
    // roles IDs
    public static final String EM_MOM_ROLE_ID = "emMomRole";
    public static final String EM_COLL01_ROLE_ID = "emColl01Role";
    public static final String EM_COLL02_ROLE_ID = "emColl02Role";
    public static final String EM_COLL03_ROLE_ID = "emColl03Role";
    public static final String EM_COLL04_ROLE_ID = "emColl04Role";
    public static final String EM_COLL05_ROLE_ID = "emColl05Role";
    public static final String EM_COLL06_ROLE_ID = "emColl06Role";
    public static final String EM_COLL07_ROLE_ID = "emColl07Role";
    public static final String EM_COLL08_ROLE_ID = "emColl08Role";
    public static final String EM_COLL09_ROLE_ID = "emColl09Role";
    public static final String EM_COLL10_ROLE_ID = "emColl10Role";
    public static final String[] EM_COLL_ROLES = {
                                                  EM_COLL01_ROLE_ID, 
                                                  EM_COLL02_ROLE_ID, 
          EM_COLL03_ROLE_ID, EM_COLL04_ROLE_ID, EM_COLL05_ROLE_ID, EM_COLL06_ROLE_ID,
          EM_COLL07_ROLE_ID, EM_COLL08_ROLE_ID, EM_COLL09_ROLE_ID, EM_COLL10_ROLE_ID
                                                 };
    public static final String TIM01_ROLE_ID = "tim01Role";
    public static final String TIM02_ROLE_ID = "tim02Role";
    public static final String TIM03_ROLE_ID = "tim03Role";
    public static final String TIM04_ROLE_ID = "tim04Role";
    public static final String TIM05_ROLE_ID = "tim05Role";
    public static final String[] TIM_ROLES = {
          TIM01_ROLE_ID, TIM02_ROLE_ID, TIM03_ROLE_ID, TIM04_ROLE_ID, TIM05_ROLE_ID
                                                 };
    public static final String EM_WEBVIEW_ROLE_ID = "emWebviewRole";
    public static final String EM_DATABASE_ROLE_ID = "emDatabaseRole";
    public static final String EM_MOM2_ROLE_ID = "emMomRole2";
    public static final String EM_MOM2_WEBVIEW_ROLE_ID = "emMom2WebviewRole";
    public static final String EM_COLL21_ROLE_ID = "emColl21Role";
    public static final String EM_COLL22_ROLE_ID = "emColl22Role";
    public static final String[] EM_COLL2_ROLES = {
                                                  EM_COLL21_ROLE_ID, 
                                                  EM_COLL22_ROLE_ID
                                                 };
    public static final String AGC_ROLE_ID = "agcRole";
    public static final String AGC_COLL01_ROLE_ID = "agcColl01Role";
    public static final String AGC_MOM_REGISTER_ROLE_ID = "agcMOMRegister";
    
    public static final String JAVA_DB_ROLE_ID = "javaDBRole";
    public static final String DB_DOMAIN_CONFIG_IMPORT_ROLE_ID
        = "dbDomainConfigImportRole";
    
    // templates IDs
    public static final String FLD_MOM_TMPL_ID = "fldmom";
    public static final String FLD_COLL01_TMPL_ID = "fldcoll01";
    public static final String FLD_COLL02_TMPL_ID = "fldcoll02";
    public static final String FLD_COLL03_TMPL_ID = "fldcoll03";
    public static final String FLD_COLL04_TMPL_ID = "fldcoll04";
    public static final String FLD_COLL05_TMPL_ID = "fldcoll05";
    public static final String FLD_COLL06_TMPL_ID = "fldcoll06";
    public static final String FLD_COLL07_TMPL_ID = "fldcoll07";
    public static final String FLD_COLL08_TMPL_ID = "fldcoll08";
    public static final String FLD_COLL09_TMPL_ID = "fldcoll09";
    public static final String FLD_COLL10_TMPL_ID = "fldcoll10";
    public static final String FLD_COLL11_TMPL_ID = "fldcoll11";
    public static final String FLD_COLL12_TMPL_ID = "fldcoll12";
    public static final String FLD_COLL13_TMPL_ID = "fldcoll13";
    public static final String[] FLD_COLL_TMPLS = {
                                                  FLD_COLL01_TMPL_ID, 
                                                  FLD_COLL02_TMPL_ID,
          FLD_COLL03_TMPL_ID, FLD_COLL04_TMPL_ID, FLD_COLL05_TMPL_ID, FLD_COLL06_TMPL_ID,
          FLD_COLL07_TMPL_ID, FLD_COLL08_TMPL_ID, FLD_COLL09_TMPL_ID, FLD_COLL10_TMPL_ID
                                                 };
    public static final String FLD_LINUX_TMPL_ID = "co65";
    public static final String FLD_TIM_TMPL_ID = "co65_tim";
    public static final String FLD_WEBVIEW_TMPL_ID = "fldwebview";
    public static final String FLD_DATBASE_TMPL_ID = "flddb";
    public static final String FLD_AGC_TMPL_ID = "fldcoll11";
    
    public static final String LOG_MONITOR_LINUX_ROLE_ID = "logMonitorLinuxRoleId";
    public static final String[] EMAILS_LOG = {"filja01@ca.com", "keyja01@ca.com"};

    // memory monitoring
    public static final String[] MEMORY_MONITOR_MAIN_CLUSTER_MACHINE_IDS = {
                                                                            MOM_MACHINE_ID,
                                                                            COLL01_MACHINE_ID,
                                                                            COLL02_MACHINE_ID,
                                                                            COLL03_MACHINE_ID,
                                                                            COLL04_MACHINE_ID,
                                                                            COLL05_MACHINE_ID,
                                                                            COLL06_MACHINE_ID,
                                                                            COLL07_MACHINE_ID,
                                                                            COLL08_MACHINE_ID,
                                                                            COLL09_MACHINE_ID,
                                                                            COLL10_MACHINE_ID,
                                                                            WEBVIEW_MACHINE_ID
                                                                           };

    public static final String[] MEMORY_MONITOR_SECOND_CLUSTER_MACHINE_IDS = {
                                                                              MOM2_MACHINE_ID,
                                                                              COLL21_MACHINE_ID,
                                                                              COLL22_MACHINE_ID
                                                                             };

    public static final String[] MEMORY_MONITOR_AGC_MACHINE_IDS = {
                                                                   AGC_MACHINE_ID,
                                                                   COLL_AGC_MACHINE_ID
                                                                   };

    public static final String FLD_MEMORY_MONITOR_GROUP = "DEMO";

    public static final String MEMORY_MONITOR_WEBAPP_MACHINE_ID = "memoryMonitorWebappMachine";
    public static final String MEMORY_MONITOR_WEBAPP_ROLE_ID = "memoryMonitorWebappRole";
    public static final String MEMORY_MONITOR_WEBAPP_TOMCAT_ROLE_ID = "memoryMonitorWebappTomcatRole";
    // //

    // time synchronization
    public static final String[] TIME_SYNCHRONIZATION_MAIN_CLUSTER_MACHINE_IDS = {
            TIM01_MACHINE_ID,
            TIM02_MACHINE_ID,
            TIM03_MACHINE_ID,
            TIM04_MACHINE_ID,
            TIM05_MACHINE_ID,
            MOM_MACHINE_ID,
            COLL01_MACHINE_ID,
            COLL02_MACHINE_ID,
            COLL03_MACHINE_ID,
            COLL04_MACHINE_ID,
            COLL05_MACHINE_ID,
            COLL06_MACHINE_ID,
            COLL07_MACHINE_ID,
            COLL08_MACHINE_ID,
            COLL09_MACHINE_ID,
            COLL10_MACHINE_ID,
            WEBVIEW_MACHINE_ID
    };
    public static final String[] TIME_SYNCHRONIZATION_SECOND_CLUSTER_MACHINE_IDS = MEMORY_MONITOR_SECOND_CLUSTER_MACHINE_IDS;
    public static final String[] TIME_SYNCHRONIZATION_AGC_MACHINE_IDS = MEMORY_MONITOR_AGC_MACHINE_IDS;
        
    // //

    // network traffic monitoring
    public static final String NETWORK_TRAFFIC_MONITOR_WEBAPP_MACHINE_ID = "networkTrafficMonitorWebappMachine";
    public static final String NETWORK_TRAFFIC_MONITOR_WEBAPP_ROLE_ID = "networkTrafficMonitorWebappRole";
    public static final String NETWORK_TRAFFIC_MONITOR_WEBAPP_TOMCAT_ROLE_ID = "networkTrafficMonitorWebappTomcatRole";

    public static final String[] NETWORK_TRAFFIC_MONITOR_MAIN_CLUSTER_MACHINE_IDS = { MOM_MACHINE_ID };
    public static final String[] NETWORK_TRAFFIC_MONITOR_SECOND_CLUSTER_MACHINE_IDS = { MOM2_MACHINE_ID };
    public static final String[] NETWORK_TRAFFIC_MONITOR_AGC_MACHINE_IDS = { AGC_MACHINE_ID };
    // //

    public static final String SSH_SOURCE_ROLE_ID = "sshSourceRoleId";
    public static final String BACKUP_MACHINE_TEMPLATE_ID = "fldcoll12";
    public static final String BACKUP_MACHINE_ID = "backupMachine";
    
    /**
     * Deafault SMTP hostname.
     */
    public static final String DEFAULT_SMTP_HOST = "mail.ca.com";

    // .NET http/https conn test
    public static final String DOTNET_HTTP_CONN_DATABASE_MACHINE_ID = DATABASE_MACHINE_ID; // "dotNetHttpConnDatabaseMachine";
    public static final String DOTNET_HTTP_CONN_COLL01_MACHINE_ID = COLL01_MACHINE_ID; // "dotNetHttpConnColl01Machine";
    public static final String DOTNET_HTTP_CONN_COLL02_MACHINE_ID = COLL02_MACHINE_ID; // "dotNetHttpConnColl02Machine";
    public static final String[] DOTNET_HTTP_CONN_COLL_MACHINES = {
        DOTNET_HTTP_CONN_COLL01_MACHINE_ID,
        DOTNET_HTTP_CONN_COLL02_MACHINE_ID
    };
    public static final String DOTNET_HTTP_CONN_TIM01_MACHINE_ID = TIM01_MACHINE_ID; // "dotNetHttpConnTim01Machine";
    public static final String[] DOTNET_HTTP_CONN_TIM_MACHINES = {
        DOTNET_HTTP_CONN_TIM01_MACHINE_ID
    };
    public static final String DOTNET_HTTP_CONN_MOM_MACHINE_ID = MOM_MACHINE_ID; // "dotNetHttpConnMomMachine";
    public static final String DOTNET_HTTP_CONN_WEBVIEW_MACHINE_ID = WEBVIEW_MACHINE_ID; // "dotNetHttpConnWebviewMachine";

    public static final String DOTNET_HTTP_CONN_EM_DATABASE_ROLE_ID = EM_DATABASE_ROLE_ID; // "dotNetHttpConnDbEmDatabaseRole";
    public static final String DOTNET_HTTP_CONN_DB_DOMAIN_CONFIG_IMPORT_ROLE_ID = DB_DOMAIN_CONFIG_IMPORT_ROLE_ID; // "dotNetHttpConnDbDomainConfigImportRole";
    public static final String DOTNET_HTTP_CONN_EM_COLL01_ROLE_ID = EM_COLL01_ROLE_ID; // "dotNetHttpConnEmColl01Role";
    public static final String[] DOTNET_HTTP_CONN_EM_COLL_ROLES = {
        DOTNET_HTTP_CONN_EM_COLL01_ROLE_ID
    };
    public static final String DOTNET_HTTP_CONN_TIM01_ROLE_ID = TIM01_ROLE_ID; // "dotNetHttpConnTim01Role";
    public static final String[] DOTNET_HTTP_CONN_TIM_ROLES = {
        DOTNET_HTTP_CONN_TIM01_ROLE_ID
    };
    public static final String DOTNET_HTTP_CONN_EM_MOM_ROLE_ID = EM_MOM_ROLE_ID; // "dotNetHttpConnEmMomRole";
    public static final String DOTNET_HTTP_CONN_EM_WEBVIEW_ROLE_ID = EM_WEBVIEW_ROLE_ID; // "dotNetHttpConnEmWebviewRole";
    public static final String DOTNET_HTTP_CONN_LOG_MONITOR_LINUX_ROLE_ID = LOG_MONITOR_LINUX_ROLE_ID; // "dotNetHttpConnLogMonitorLinuxRoleId";

    public static final String DOTNET_HTTP_CONN_FLD_DATABASE_TMPL_ID = FLD_DATBASE_TMPL_ID; // "co65";
    public static final String DOTNET_HTTP_CONN_FLD_COLL01_TMPL_ID = FLD_COLL01_TMPL_ID; // "co65";
    public static final String[] DOTNET_HTTP_CONN_FLD_COLL_TMPLS = {
        DOTNET_HTTP_CONN_FLD_COLL01_TMPL_ID
    };
    public static final String DOTNET_HTTP_CONN_FLD_TIM_TMPL_ID = "co65_tim";
    public static final String DOTNET_HTTP_CONN_FLD_MOM_TMPL_ID = FLD_MOM_TMPL_ID; // "co65";
    public static final String DOTNET_HTTP_CONN_FLD_WEBVIEW_TMPL_ID = FLD_WEBVIEW_TMPL_ID; // "co65";
    // //

}
