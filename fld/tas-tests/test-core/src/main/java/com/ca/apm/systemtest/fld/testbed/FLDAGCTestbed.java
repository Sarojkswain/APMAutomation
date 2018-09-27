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

package com.ca.apm.systemtest.fld.testbed;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.apm.systemtest.fld.testbed.util.FLDTestbedUtil;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;

/**
 * FLD AGC cluster testbed.
 * @author filja01
 *
 */
public class FLDAGCTestbed implements FldTestbedProvider, FLDConstants {
    
/**** AGC Registration tokens ****/
    // FIXME - these tokens should probably be generated instead of hard-coded
    private static final String AGC_TOKEN_HASHED =
        "77534d7f7a7a7d86dec44de287d952cf572ca578fdcef6f16f989de3eb248c06";
    private static final String AGC_TOKEN = "91e158c4-9b92-4a51-8699-bcf6397e64d2";
    private static final String AGC_TOKEN_ENCRYPTED = 
        "3a.Z1SnwDpvFd4SfhWQpqdemQ==.5fWSAj0bWuQ4dhzTxXwhY68nEVG/lD4+5FjgPO2fDfYVGIWzBax4CcTvmX9eNunC";
    
    private static final String AGC_MOM_TOKEN = "ee827a0f-6c17-4601-9d89-93d1998b49f0";
    private static final String AGC_MOM_TOKEN_ENCRYPTED = 
        "3a.gcwJY67x7FcBZ2dFoqSfCA==.7WQEqfvIxht+vnNsLlUgP5KnwrLvSVwj6pA6tjQJbBZM2cOF+VNKHNrrCNeXQL/3";
    
    private static final String AGC_MOM_MASTER_TOKEN = "c78b74f9-50aa-4760-86f9-5a5e45cd003c";
    private static final String AGC_MOM_MASTER_TOKEN_ENCRYPTED = 
        "3a.NPvuNWA8QyrV3xPPuOrspA==.FuIkWTNUk/moM4/2lV2rsFNY0XrWlbjRIbg5vH5RQV3bh5di/ZmGAFxdF7C3wMjD";
    
    private static final String MOM_TOKEN_HASHED =
        "9d6d8d1946a163616010a8ed48d592ec2e4c6f52ec99e00740d786f0864492aa";
    private static final String MOM_TOKEN = "68acb2d4-4e56-40ef-9397-8808585a86bc";
    private static final String MOM_TOKEN_ENCRYPTED = 
        "3a.1tWrT2bFkC0lmOt0FsyWcQ==.2v7GN77uWvjRuGb4gtLImZ7v48+NbAQ4YbKDQCmKxsE2KoclLcDFSHZ7i8nbH4+j";
    
    private static final String MOM_AGC_TOKEN = "966bf0f5-387d-4852-a4c2-8ff2dcde371c";
    private static final String MOM_AGC_TOKEN_ENCRYPTED = 
        "3a.YbgCaTMWe1TFyY1asX5XUQ==.+AqxYioJvlS6r0dEtTYms+IXc4H0F1F28URCq5w6KCFPFnjwkvTMDmAUK0jgS4zD";
    
    private static final String AGC_TOKEN2_HASHED =
        "b621bae6830c22cac5744de6debf37a24b99a382f4e1d1d5450bd46508a2b490";
    private static final String AGC_TOKEN2 = "dc1a4fa3-99f5-4c7c-8bcd-69e0ec1f9530";
    private static final String AGC_TOKEN2_ENCRYPTED = 
        "3a.L4h5J1SuD/7GLGLmttCWwg==.A9jxxNq279oCJwqCujojRMyNJfl8+p2ebcEcWctX2rfiZGNQTvKBZN9NWOMoS88C";
    
    private static final String AGC_MOM2_TOKEN = "e310e6c2-47b0-4d42-bffa-ce5dfcaba5db";
    private static final String AGC_MOM2_TOKEN_ENCRYPTED = 
        "3a.dDt9cXXmzIFTI0V6UTM7jw==.niNRmpL8SFOPX6mI7y3d9ebMdv49DGvNV4jS45t4htG6lQ3kbBAQyDSnWcteV3Cq";
    
    private static final String AGC_MOM2_MASTER_TOKEN = "25fc61fe-352c-403d-8572-9bb373c84268";
    private static final String AGC_MOM2_MASTER_TOKEN_ENCRYPTED = 
        "3a.o7H4BNk0kb6XQfJPV3rmag==.eV1Qg7sCpc8atjyJqL3TI/gN4pMBYLKoKJ3R+NXbcncBbL2RGJPKBRZII2UlmAm9";
    
    private static final String MOM2_TOKEN_HASHED =
        "bbb31f1bfbbd84125257e90ec300050a8eb9030c4893bb65994abb976b1b5c7b";
    private static final String MOM2_TOKEN = "e78406a7-73b2-4ceb-b6e2-b52a186a7df1";
    private static final String MOM2_TOKEN_ENCRYPTED = 
        "3a.pHVsGo/4T08sESrqsnLRMw==.6CbJhkEgsX1mDkpkKLFcZPzlMSpInezl3FdOfLWjum1vwzSDnPiJG8G2dfHsasbt";
    
    private static final String MOM2_AGC_TOKEN = "864cc17e-2a67-41c4-b31c-79ef1451e214";
    private static final String MOM2_AGC_TOKEN_ENCRYPTED = 
        "3a.KjSzVUZ69ofxHw4uLN6u/g==.PSlkXH9RiQlkVR3Jp3gYSL9k7q7Dceh4oPckwlDwRMajjQ2txCoa18OlRfiX+wHQ";
/**** AGC Registration tokens END ****/
    
// AGC registration mapping
    private static final String M_WEBVIEWPORT = "webviewPort";
    private static final String M_URLWEBVIEW = "urlWebview";
    private static final String M_URLMOM = "urlMom";
    private static final String M_DBMACHINEID = "databaseMachineId";
    private static final String M_DBORACLEUSER = "databaseOracleUser";
    private static final String M_MOMMACHINEID = "momMachineId";
    private static final String M_AGCMOMTOKEN = "agcMomToken";
    private static final String M_AGCMOMTOKEN_CRYPT = "agcMomTokenCrypt";
    private static final String M_AGCMOMMASTERTOKEN = "agcMomMasterToken";
    private static final String M_AGCMOMMASTERTOKEN_CRYPT = "agcMomMasterTokenCrypt";
    private static final String M_AGCHASHEDTOKEN = "agcHashedToken";
    private static final String M_AGCTOKEN = "agcToken";
    private static final String M_AGCTOKEN_CRYPT = "agcTokenCrypt";
    private static final String M_MOMAGCTOKEN = "momAgcToken";
    private static final String M_MOMAGCTOKEN_CRYPT = "momAgcTokenCrypt";
    private static final String M_MOMHASHEDTOKEN = "momHashedToken";
    private static final String M_MOMTOKEN = "momToken";
    private static final String M_MOMTOKEN_CRYPT = "momTokenCrypt";
    
    private static final String SCRIPT_FILE = "/tmp/add_token.sh";
    
    private static final Collection<String> AGC_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
        "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC",
        //"-XX:+UseG1GC",
        //"-XX:G1ReservePercent=20", "-XX:InitiatingHeapOccupancyPercent=35",
        "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", 
        "-XX:+HeapDumpOnOutOfMemoryError", "-Xms8192m", "-Xmx8192m",
        "-verbose:gc", "-Xloggc:"+FLDMainClusterTestbed.GC_LOG_FILE, "-Dappmap.user=admin",
        "-Dappmap.token="+FLDMainClusterTestbed.ADMIN_AUX_TOKEN);
  
    public static final String ORACLE_DB_USERNAME = "CEMADMINAGC";
    
    private ITestbedMachine agcMachine;
    private ITestbedMachine collectorMachine;
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        FLDConfiguration fldConfig = FLDConfigurationService.getConfig();
        String agcTemplateId = fldConfig.getMachineTemplateOverrides().get(COLL_AGC_MACHINE_ID);
        if (agcTemplateId == null) {
            agcTemplateId = FLD_AGC_TMPL_ID;
        }
        
        System.out.println("****************************************");
        System.out.println("Using template " + agcTemplateId + " for AGC machine");
        System.out.println("  map contains: " + fldConfig.getMachineTemplateOverrides());
        System.out.println("Using config instance: " + fldConfig);
        System.out.println("****************************************");
        agcMachine = new TestbedMachine.LinuxBuilder(AGC_MACHINE_ID)
                              .templateId(agcTemplateId).bitness(Bitness.b64)
                              .build();
        collectorMachine = new TestbedMachine.LinuxBuilder(COLL_AGC_MACHINE_ID)
                              .templateId(FLD_LINUX_TMPL_ID).bitness(Bitness.b64)
                              .build();
        
        return Arrays.asList(agcMachine, collectorMachine);
    }
    
    
    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        FLDConfiguration fldConfig = FLDConfigurationService.getConfig();

        //AGC machine
        EmRole.LinuxBuilder agcBuilder = new EmRole.LinuxBuilder(AGC_ROLE_ID, tasResolver);

        String emHost = fqdn(tasResolver.getHostnameById(AGC_ROLE_ID));

        //Collectors machines
        EmRole.LinuxBuilder collBuilder = new EmRole.LinuxBuilder(AGC_COLL01_ROLE_ID, tasResolver); 
        collBuilder
            .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager","ProbeBuilder","EPA"))
            .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
            .nostartEM()
            .nostartWV()
            .version(fldConfig.getEmVersion())
            .installDir(FLDMainClusterTestbed.INSTALL_DIR)
            .installerTgDir(FLDMainClusterTestbed.INSTALL_TG_DIR);
        
        if (fldConfig.isOracleMode()) {
            collBuilder
                .oracleDbHost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID))
                .oracleDbPassword(FLDMainClusterTestbed.ORACLE_DB_PASSWORD)
                .oracleDbPort(FLDMainClusterTestbed.ORACLE_DB_PORT)
                .oracleDbSidName(FLDMainClusterTestbed.ORACLE_SID_NAME)
                .oracleDbUsername(ORACLE_DB_USERNAME)
                .installerProperty("useExistingSchemaForOracle", "true")
                .useOracle();
        } else {
            collBuilder
                .dbuser(FLDMainClusterTestbed.DB_USERNAME)
                .dbpassword(FLDMainClusterTestbed.DB_PASSWORD)
                .dbAdminUser(FLDMainClusterTestbed.DB_ADMIN_USERNAME)
                .dbAdminPassword(FLDMainClusterTestbed.DB_ADMIN_PASSWORD)
                .dbhost(emHost);
        }
        
        collBuilder.emLaxNlClearJavaOption(FLDSecondClusterTestbed.COLL_LAXNL_JAVA_OPTION);
        
        EmRole collectorRole = collBuilder.build();
        
        collectorMachine.addRole(collectorRole);
       
        //setup logging in config/IntroscopeEnterpriseManager.properties
        IRole loggingRole = FLDMainClusterTestbed.addLoggingSetupRole(collectorMachine, collectorRole, tasResolver, 100);
        
        //start COLLECTOR
        FLDMainClusterTestbed.addStartEmRole(collectorMachine, collectorRole, false, true, loggingRole);
        
        agcBuilder.emCollector(collectorRole);
        
        
        //AGC role settings
        agcBuilder
            .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager","ProbeBuilder","EPA","Database","WebView"))
            .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
            .nostartEM()
            .nostartWV()
            .emWebPort(FLDMainClusterTestbed.EMWEBPORT)
            .wvPort(FLDMainClusterTestbed.WVPORT)
            .version(fldConfig.getEmVersion())
            .installDir(FLDMainClusterTestbed.INSTALL_DIR)
            .installerTgDir(FLDMainClusterTestbed.INSTALL_TG_DIR)
            .databaseDir(FLDMainClusterTestbed.DATABASE_DIR)
            .configProperty("introscope.apmserver.teamcenter.master", "true")
            .emLaxNlClearJavaOption(AGC_LAXNL_JAVA_OPTION)
            .wvLaxNlClearJavaOption(FLDSecondClusterTestbed.WV_LAXNL_JAVA_OPTION);
        
        if (fldConfig.isOracleMode()) {
            agcBuilder
                .oracleDbHost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID))
                .oracleDbPassword(FLDMainClusterTestbed.ORACLE_DB_PASSWORD)
                .oracleDbPort(FLDMainClusterTestbed.ORACLE_DB_PORT)
                .oracleDbSidName(FLDMainClusterTestbed.ORACLE_SID_NAME)
                .oracleDbUsername(ORACLE_DB_USERNAME)
                .useOracle();
        } else {
            agcBuilder
                .dbuser(FLDMainClusterTestbed.DB_USERNAME)
                .dbpassword(FLDMainClusterTestbed.DB_PASSWORD)
                .dbAdminUser(FLDMainClusterTestbed.DB_ADMIN_USERNAME)
                .dbAdminPassword(FLDMainClusterTestbed.DB_ADMIN_PASSWORD)
                .dbhost(emHost);
        }
        
        EmRole agcRole = agcBuilder.build();
        agcMachine.addRole(agcRole);
        
        if (fldConfig.isOracleMode()) {
            Collection<String> data = Arrays.asList(
                "export ORACLE_HOME=" + FLDMainClusterTestbed.ORACLE_HOME,
                FLDMainClusterTestbed.ORACLE_HOME + FLDMainClusterTestbed.SQLPLUS_LOCATION + " " 
                    + FLDMainClusterTestbed.ORACLE_SYSDB_USERNAME + "/" 
                    + FLDMainClusterTestbed.ORACLE_SYSDB_PASSWORD + "@" + FLDMainClusterTestbed.ORACLE_SID 
                    + " as SYSDBA <<EOF\n"
                    + "alter session set \"_oracle_script\"=true;\n"
                    + "drop user "+ORACLE_DB_USERNAME+" cascade;\n"
                    + "create user "+ORACLE_DB_USERNAME+" identified by "+FLDMainClusterTestbed.ORACLE_DB_PASSWORD+";\n"
                    + "GRANT CONNECT, RESOURCE, CREATE TRIGGER, CREATE SEQUENCE, CREATE TYPE, CREATE PROCEDURE, "
                    + "CREATE TABLE, CREATE SESSION, CREATE VIEW, ANALYZE ANY, UNLIMITED TABLESPACE TO "+ORACLE_DB_USERNAME+";\n"
                    + "EOF");
            
            FileModifierFlowContext createFileFlow =
                new FileModifierFlowContext.Builder().create("/tmp/"+AGC_ROLE_ID+"_user.sh", data).build();
            ExecutionRole execRole =
                new ExecutionRole.Builder(AGC_ROLE_ID + "_user")
                    .flow(FileModifierFlow.class, createFileFlow)
                    .asyncCommand(new RunCommandFlowContext.Builder("/tmp/"+AGC_ROLE_ID+"_user.sh").build()).build();
            execRole.addProperty(FLDMainClusterTestbed.SCRIPT_LOCATION, "/tmp/"+AGC_ROLE_ID+"_user.sh");
            testbed.getMachineById(DATABASE_MACHINE_ID).addRole(execRole);
            execRole.before(agcRole);
        }

        //setup webview agent on WV
        Map<String, String> propsMap = new HashMap<String, String>(2);
        propsMap.put("introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT", emHost);
        propsMap.put("agentManager.url.1", emHost + ":" + agcRole.getEmPort());
        
        ConfigureFlowContext ctx =
            new ConfigureFlowContext.Builder()
                .configurationMap(FLDMainClusterTestbed.INSTALL_DIR+"/product/webview/agent/wily/core/config/IntroscopeAgent.profile", 
                    propsMap)
                .build();

        UniversalRole setWVAgent =
            new UniversalRole.Builder(AGC_ROLE_ID + "_setupWVAgentProfile", tasResolver).runFlow(ConfigureFlow.class, ctx)
                .build();
        
        setWVAgent.after(agcRole);
        agcMachine.addRole(setWVAgent);
        
        //setup logging in config/IntroscopeEnterpriseManager.properties
        IRole loggingRoleA = FLDMainClusterTestbed.addLoggingSetupRole(agcMachine, setWVAgent, tasResolver, 100);
        
        //start AGC + Webview
        //IRole lastRole = FLDMainClusterTestbed.addStartEmRole(agcMachine, agcRole, true, true, loggingRoleA);
        
        //register MOM to AGC
        //set-up MOM for AGC registration
        HashMap<String, String> tokenMOM = new HashMap<String, String>();
        tokenMOM.put(M_URLMOM, fqdn(tasResolver.getHostnameById(EM_MOM_ROLE_ID)));
        tokenMOM.put(M_URLWEBVIEW, fqdn(tasResolver.getHostnameById(EM_WEBVIEW_ROLE_ID)));
        tokenMOM.put(M_WEBVIEWPORT, String.valueOf(FLDMainClusterTestbed.WVPORT));
        tokenMOM.put(M_DBMACHINEID, DATABASE_MACHINE_ID);
        tokenMOM.put(M_DBORACLEUSER, FLDMainClusterTestbed.ORACLE_DB_USERNAME);
        tokenMOM.put(M_MOMMACHINEID, MOM_MACHINE_ID);
        tokenMOM.put(M_AGCMOMTOKEN, AGC_MOM_TOKEN);
        tokenMOM.put(M_AGCMOMTOKEN_CRYPT, AGC_MOM_TOKEN_ENCRYPTED);
        tokenMOM.put(M_AGCMOMMASTERTOKEN, AGC_MOM_MASTER_TOKEN);
        tokenMOM.put(M_AGCMOMMASTERTOKEN_CRYPT, AGC_MOM_MASTER_TOKEN_ENCRYPTED);
        tokenMOM.put(M_AGCHASHEDTOKEN, AGC_TOKEN_HASHED);
        tokenMOM.put(M_AGCTOKEN, AGC_TOKEN);
        tokenMOM.put(M_AGCTOKEN_CRYPT, AGC_TOKEN_ENCRYPTED);
        tokenMOM.put(M_MOMAGCTOKEN, MOM_AGC_TOKEN);
        tokenMOM.put(M_MOMAGCTOKEN_CRYPT, MOM_AGC_TOKEN_ENCRYPTED);
        tokenMOM.put(M_MOMHASHEDTOKEN, MOM_TOKEN_HASHED);
        tokenMOM.put(M_MOMTOKEN, MOM_TOKEN);
        tokenMOM.put(M_MOMTOKEN_CRYPT, MOM_TOKEN_ENCRYPTED);
        
        List<HashMap<String, String>> tokenList = new ArrayList<HashMap<String, String>>();
        tokenList.add(tokenMOM);

        //register MOM2 to AGC
        //set-up MOM2 for AGC registration
        HashMap<String, String> tokenMOM2 = new HashMap<String, String>();
        tokenMOM2.put(M_URLMOM, fqdn(tasResolver.getHostnameById(EM_MOM2_ROLE_ID)));
        tokenMOM2.put(M_URLWEBVIEW, fqdn(tasResolver.getHostnameById(EM_MOM2_WEBVIEW_ROLE_ID)));
        tokenMOM2.put(M_WEBVIEWPORT, String.valueOf(FLDMainClusterTestbed.WVPORT2));
        tokenMOM2.put(M_DBMACHINEID, WEBVIEW2_MACHINE_ID);
        tokenMOM2.put(M_DBORACLEUSER, FLDSecondClusterTestbed.ORACLE_DB_USERNAME);
        tokenMOM2.put(M_MOMMACHINEID, MOM2_MACHINE_ID);
        tokenMOM2.put(M_AGCMOMTOKEN, AGC_MOM2_TOKEN);
        tokenMOM2.put(M_AGCMOMTOKEN_CRYPT, AGC_MOM2_TOKEN_ENCRYPTED);
        tokenMOM2.put(M_AGCMOMMASTERTOKEN, AGC_MOM2_MASTER_TOKEN);
        tokenMOM2.put(M_AGCMOMMASTERTOKEN_CRYPT, AGC_MOM2_MASTER_TOKEN_ENCRYPTED);
        tokenMOM2.put(M_AGCHASHEDTOKEN, AGC_TOKEN2_HASHED);
        tokenMOM2.put(M_AGCTOKEN, AGC_TOKEN2);
        tokenMOM2.put(M_AGCTOKEN_CRYPT, AGC_TOKEN2_ENCRYPTED);
        tokenMOM2.put(M_MOMAGCTOKEN, MOM2_AGC_TOKEN);
        tokenMOM2.put(M_MOMAGCTOKEN_CRYPT, MOM2_AGC_TOKEN_ENCRYPTED);
        tokenMOM2.put(M_MOMHASHEDTOKEN, MOM2_TOKEN_HASHED);
        tokenMOM2.put(M_MOMTOKEN, MOM2_TOKEN);
        tokenMOM2.put(M_MOMTOKEN_CRYPT, MOM2_TOKEN_ENCRYPTED);
        
        tokenList.add(tokenMOM2);
        
        registerToAGC(tasResolver, testbed, fldConfig.getApiVersion(), tokenList, AGC_ROLE_ID, AGC_MACHINE_ID, fldConfig.isOracleMode());
        
        FLDMainClusterTestbed.addLogMonitorRole(agcMachine, agcRole, tasResolver, fldConfig.getLogMonitorEmail(),
            FLDTestbedUtil.getDefaultMomLogMonitorConfiguration());

//        new MemoryMonitorTestbedProvider(MEMORY_MONITOR_AGC_MACHINE_IDS).initTestbed(testbed, tasResolver);
//        new TimeSynchronizationTestbedProvider(TIME_SYNCHRONIZATION_AGC_MACHINE_IDS).initTestbed(testbed, tasResolver);
//        new NetworkTrafficMonitorTestbedProvider(NETWORK_TRAFFIC_MONITOR_AGC_MACHINE_IDS).initTestbed(testbed, tasResolver);

    }
    
    private void registerToAGC(ITasResolver tasResolver, ITestbed testbed, String apiVersion,
                               List<HashMap<String, String>> tokenMap, String agcRoleId, String agcMachineId, boolean isOracleMode) {
                               
    StringBuilder agcFollowersInsert = new StringBuilder();
    int momNumber = 1;
    
    String agcHost = fqdn(tasResolver.getHostnameById(agcRoleId));
    
    agcFollowersInsert.append("INSERT INTO appmap_settings (id,user_id,type,data,created_at,updated_at,deleted_at) VALUES ('FW1',null,3,'{");
    
    List<String> insertListAGC = new ArrayList<String>();
    
    String now = "NOW()";
    String nextval = "NEXTVAL('seq_appmap_api_key_id')";
    if (isOracleMode) {
        now = "SYSDATE";
        nextval = "seq_appmap_api_key_id.nextval";
    }
    
    for (HashMap<String, String> token : tokenMap) {
        
        //create inserts for AGC DB
        StringBuilder agcFollowerBody = new StringBuilder();
        agcFollowerBody.append("\\\"").append(token.get(M_URLMOM)).append(":8081\\\"")
            .append(":{")
                .append("\\\"id\\\"").append(":").append("\\\"").append(token.get(M_URLMOM)).append(":8081\\\"")
                .append(",")
                .append("\\\"url\\\"").append(":").append("\\\"http://").append(token.get(M_URLMOM)).append(":8081\\\"")
                .append(",")
                .append("\\\"webviewUrl\\\"").append(":")
                .append("\\\"http://").append(token.get(M_URLWEBVIEW)).append(":").append(token.get(M_WEBVIEWPORT)).append("\\\"")
                .append(",")
                .append("\\\"status\\\"").append(":").append("\\\"JOINING\\\"")
                .append(",")
                .append("\\\"lastChanged\\\"").append(":").append("\\\"").append(System.currentTimeMillis()).append("\\\"")
                .append(",")
                // FIXME - version should probably not be "10.2 (incompatible)"
                .append("\\\"version\\\"").append(":").append("\\\"10.2 (incompatible)\\\"")
                .append(",")
                .append("\\\"hostname\\\"").append(":").append("\\\"").append(token.get(M_URLMOM)).append("\\\"")
                .append(",")
                .append("\\\"encryptedToken\\\"").append(":").append("\\\"").append(token.get(M_AGCMOMTOKEN)).append("\\\"")
                .append(",")
                .append("\\\"encryptedMasterToken\\\"").append(":").append("\\\"").append(token.get(M_AGCMOMMASTERTOKEN)).append("\\\"")
                .append(",")
                .append("\\\"momNumber\\\"").append(":").append(momNumber)
                .append(",")
                // FIXME - the apiVersion must be set to the actual version of the API used, which will evolve over time
                .append("\\\"apiVersion\\\"").append(":\\\"").append(apiVersion).append("\\\"")
            .append("}");
        
        if (isOracleMode) {
            agcFollowersInsert.append(agcFollowerBody.toString().replaceAll("\\\\", ""));
        }
        else {
            agcFollowersInsert.append(agcFollowerBody.toString());
        }
        if (tokenMap.size() > momNumber) {
            agcFollowersInsert.append(",");
        }
        
        StringBuilder agcSettingInsert = new StringBuilder();
        agcSettingInsert.append("INSERT INTO appmap_settings (id,user_id,type,data,created_at,updated_at,deleted_at) VALUES ('UNFW")
            .append(token.get(M_URLMOM)).append(":8081',null,2,'{\\\"includedVertices\\\":[],\\\"excludedVertices\\\":[],")
            .append("\\\"items\\\":[{\\\"operator\\\":\\\"AND\\\",\\\"attributeName\\\":\\\"Source cluster\\\",\\\"values\\\":[\\\"")
            .append(token.get(M_URLMOM)).append(":8081\\\"],\\\"not\\\":false,\\\"wildCard\\\":false,")
            .append("\\\"btCoverage\\\":null}],\\\"showEntry\\\":false,\\\"matchesNothing\\\":false,\\\"name\\\":\\\"")
            .append(token.get(M_URLMOM)).append(" components\\\",\\\"universeId\\\":\\\"UNFW")
            .append(token.get(M_URLMOM)).append(":8081\\\",\\\"users\\\":[],\\\"lastUpdate\\\":"+System.currentTimeMillis()+"}',")
            .append(now+","+now+",null);");
        
        if (isOracleMode) {
            insertListAGC.add(agcSettingInsert.toString().replaceAll("\\\\", ""));
        }
        else {
            insertListAGC.add(agcSettingInsert.toString());
        }
        
        StringBuilder agcApiKeysInsert = new StringBuilder();
        agcApiKeysInsert.append("INSERT INTO appmap_api_keys (id,username,date_created,date_expired,hashed_token,description) VALUES (")
            .append(nextval+",'admin',"+now+",null,'").append(token.get(M_AGCHASHEDTOKEN))
            .append("','{\\\"system\\\":true,\\\"description\\\":\\\"FLD token\\\",\\\"token\\\":\\\"")
            .append(token.get(M_AGCTOKEN)).append("\\\"}');");
        
        if (isOracleMode) {
            insertListAGC.add(agcApiKeysInsert.toString().replaceAll("\\\\", ""));
        }
        else {
            insertListAGC.add(agcApiKeysInsert.toString());
        }
        
        insertListAGC.add(secureStoreInsert(token.get(M_AGCMOMTOKEN), token.get(M_AGCMOMTOKEN_CRYPT), now));
        insertListAGC.add(secureStoreInsert(token.get(M_AGCMOMMASTERTOKEN), token.get(M_AGCMOMMASTERTOKEN_CRYPT), now));
        insertListAGC.add(secureStoreInsert(token.get(M_AGCTOKEN), token.get(M_AGCTOKEN_CRYPT), now));
        
        //create inserts for MOM DB
        StringBuilder momSettingInsert = new StringBuilder();
        momSettingInsert.append("INSERT INTO appmap_settings (id,user_id,type,data,created_at,updated_at,deleted_at) ")
            .append("VALUES ('FRG1',null,4,'{\\\"followerId\\\":\\\"").append(token.get(M_URLMOM))
            .append(":8081\\\",\\\"agcToken\\\":\\\"").append(token.get(M_MOMAGCTOKEN))
            .append("\\\",\\\"agcUrl\\\":\\\"http://").append(agcHost)
            .append(":8081\\\",\\\"agcWebviewUrl\\\":\\\"http://").append(agcHost)
            .append(":8080\\\",\\\"url\\\":\\\"http://").append(token.get(M_URLMOM)).append(":8081\\\",")
            .append("\\\"webviewUrl\\\":\\\"http://")
            .append(token.get(M_URLWEBVIEW)).append(":").append(token.get(M_WEBVIEWPORT))
            .append("\\\",\\\"validation\\\":false}',"+now+","+now+",null);");
        
        StringBuilder momApiKeysInsert = new StringBuilder();
        momApiKeysInsert.append("INSERT INTO appmap_api_keys (id,username,date_created,date_expired,hashed_token,description) VALUES (")
            .append(nextval+",'admin',"+now+",null,'").append(token.get(M_MOMHASHEDTOKEN))
            .append("','{\\\"system\\\":true,\\\"description\\\":\\\"[http://").append(agcHost).append(":8081]\\\",\\\"token\\\":\\\"")
            .append(token.get(M_MOMTOKEN)).append("\\\"}');");
        
        String[] momSecureInserts = {secureStoreInsert(token.get(M_MOMAGCTOKEN), token.get(M_MOMAGCTOKEN_CRYPT), now),
                                     secureStoreInsert(token.get(M_MOMTOKEN), token.get(M_MOMTOKEN_CRYPT), now)};
        
        StringBuilder insertsMOM = new StringBuilder();
        insertsMOM.append(momSettingInsert.toString())
                  .append(momApiKeysInsert.toString())
                  .append(momSecureInserts[0])
                  .append(momSecureInserts[1]);
        
        //DB insert into MOM DB
        ITestbedMachine dbMachine = testbed.getMachineById(token.get(M_DBMACHINEID));
        ExecutionRole execRole = null;
        if (!isOracleMode) {
            Collection<String> data = Arrays.asList(
                "export PGPASSWORD="+FLDMainClusterTestbed.DB_ADMIN_PASSWORD,
                FLDMainClusterTestbed.DATABASE_DIR+"/bin/psql --username="+FLDMainClusterTestbed.DB_ADMIN_USERNAME
                    +" --dbname=cemdb --command=\""+ insertsMOM.toString() +"\"");
            
            FileModifierFlowContext createFileFlow =
                new FileModifierFlowContext.Builder().create(SCRIPT_FILE, data).build();
            execRole =
                new ExecutionRole.Builder(token.get(M_DBMACHINEID) + "_token")
                    .flow(FileModifierFlow.class, createFileFlow)
                    .asyncCommand(new RunCommandFlowContext.Builder(SCRIPT_FILE).build()).build();
            execRole.addProperty(FLDMainClusterTestbed.SCRIPT_LOCATION, SCRIPT_FILE);
        }
        else {
            Collection<String> data = Arrays.asList(
                "export ORACLE_HOME=" + FLDMainClusterTestbed.ORACLE_HOME,
                FLDMainClusterTestbed.ORACLE_HOME + FLDMainClusterTestbed.SQLPLUS_LOCATION 
                    + " " + token.get(M_DBORACLEUSER) + "/" 
                    + FLDMainClusterTestbed.ORACLE_DB_PASSWORD + "@" 
                    + FLDMainClusterTestbed.ORACLE_SID + " <<EOF\n"
                    + momSettingInsert.toString().replaceAll("\\\\", "") + "\n"
                    + momApiKeysInsert.toString().replaceAll("\\\\", "") + "\n"
                    + momSecureInserts[0] + "\n"
                    + momSecureInserts[1] + "\n"
                    + "EOF");
            
            FileModifierFlowContext createFileFlow =
                new FileModifierFlowContext.Builder().create("/tmp/"+token.get(M_DBMACHINEID)+"_token.sh", data).build();
            execRole =
                new ExecutionRole.Builder(token.get(M_DBMACHINEID) + "_token")
                    .flow(FileModifierFlow.class, createFileFlow)
                    .asyncCommand(new RunCommandFlowContext.Builder("/tmp/"+token.get(M_DBMACHINEID)+"_token.sh").build()).build();
            execRole.addProperty(FLDMainClusterTestbed.SCRIPT_LOCATION, "/tmp/"+token.get(M_DBMACHINEID)+"_token.sh");
        }
        execRole.after(new HashSet<IRole>(Arrays.asList(dbMachine.getRoles())));
        if (!isOracleMode) {
            dbMachine.addRole(execRole);
        } else {
            testbed.getMachineById(DATABASE_MACHINE_ID).addRole(execRole);
        }
        
        //add follower properties file
        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        String date = df.format(new Date());
        Collection<String> dataF = Arrays.asList(
            "#","#"+date,"state=FOLLOWER_JOINING");
        FileModifierFlowContext createFileFlowF =
            new FileModifierFlowContext.Builder().create(FLDMainClusterTestbed.INSTALL_DIR + "/config/AGCFollower.properties", dataF).build();
        UniversalRole followerPropRole =
            new UniversalRole.Builder(token.get(M_MOMMACHINEID) + "-followerProperties", tasResolver)
                    .runFlow(FileModifierFlow.class, createFileFlowF).build();
        ITestbedMachine momMachine = testbed.getMachineById(token.get(M_MOMMACHINEID));
        //get rid off log monitor role
        List<IRole> momRoles = new ArrayList<IRole>();
        for (IRole r : Arrays.asList(momMachine.getRoles())) {
            if (!r.getRoleId().equals(token.get(M_MOMMACHINEID) + "_" + LOG_MONITOR_LINUX_ROLE_ID)) {
                momRoles.add(r);
            }
        }
        followerPropRole.after(new HashSet<IRole>(momRoles));
        followerPropRole.after(execRole);
        momMachine.addRole(followerPropRole);
        
        //add keystore.jceks file
        FileCreatorFlowContext context = new FileCreatorFlowContext.Builder()
                .fromResource("/agc-register/" + "keystoreMOM"+momNumber+".jceks")
                .destinationPath(FLDMainClusterTestbed.INSTALL_DIR + "/config/internal/server/keystore.jceks")
                .build();
        IRole keystoreRole = new UniversalRole.Builder(token.get(M_MOMMACHINEID) + "-keystore", tasResolver)
                .runFlow(FileCreatorFlow.class, context).build();
        keystoreRole.after(followerPropRole);
        momMachine.addRole(keystoreRole);
        
        momNumber++;
    }
    
    agcFollowersInsert.append("}',"+now+","+now+",null);");
    insertListAGC.add(agcFollowersInsert.toString());
    
    StringBuilder inserts = new StringBuilder();
    for (String s : insertListAGC) {
        if(isOracleMode) {
            inserts.append(s+"\n");
        } else {
            inserts.append(s);
        }
    }
    
    //DB insert into AGC
    ITestbedMachine agcMachine = testbed.getMachineById(agcMachineId);
    ExecutionRole execRole = null;
    if (!isOracleMode) {
        Collection<String> data = Arrays.asList(
            "export PGPASSWORD="+FLDMainClusterTestbed.DB_ADMIN_PASSWORD,
            FLDMainClusterTestbed.DATABASE_DIR+"/bin/psql --username="+FLDMainClusterTestbed.DB_ADMIN_USERNAME
                +" --dbname=cemdb --command=\""+ inserts.toString() +"\"");
        
        FileModifierFlowContext createFileFlow =
            new FileModifierFlowContext.Builder().create(SCRIPT_FILE, data).build();
        execRole =
            new ExecutionRole.Builder(agcMachineId + "_token")
                .flow(FileModifierFlow.class, createFileFlow)
                .asyncCommand(new RunCommandFlowContext.Builder(SCRIPT_FILE).build()).build();
        execRole.addProperty(FLDMainClusterTestbed.SCRIPT_LOCATION, SCRIPT_FILE);
    }
    else {
        Collection<String> data = Arrays.asList(
            "export ORACLE_HOME=" + FLDMainClusterTestbed.ORACLE_HOME,
            FLDMainClusterTestbed.ORACLE_HOME + FLDMainClusterTestbed.SQLPLUS_LOCATION 
                + " " + ORACLE_DB_USERNAME + "/" 
                + FLDMainClusterTestbed.ORACLE_DB_PASSWORD + "@" 
                + FLDMainClusterTestbed.ORACLE_SID + " <<EOF\n"
                + inserts.toString() + "EOF");
        
        FileModifierFlowContext createFileFlow =
            new FileModifierFlowContext.Builder().create("/tmp/"+agcMachineId+"_token.sh", data).build();
        execRole =
            new ExecutionRole.Builder(agcMachineId + "_token")
                .flow(FileModifierFlow.class, createFileFlow)
                .asyncCommand(new RunCommandFlowContext.Builder("/tmp/"+agcMachineId+"_token.sh").build()).build();
        execRole.addProperty(FLDMainClusterTestbed.SCRIPT_LOCATION, "/tmp/"+agcMachineId+"_token.sh");
    }
    execRole.after(new HashSet<IRole>(Arrays.asList(agcMachine.getRoles())));
    if (!isOracleMode) {
        agcMachine.addRole(execRole);
    } else {
        testbed.getMachineById(DATABASE_MACHINE_ID).addRole(execRole);
    }
    
    //add keystore.jceks file
    FileCreatorFlowContext context = new FileCreatorFlowContext.Builder()
            .fromResource("/agc-register/" + "keystoreAGC.jceks")
            .destinationPath(FLDMainClusterTestbed.INSTALL_DIR + "/config/internal/server/keystore.jceks")
            .build();
    IRole keystoreRole = new UniversalRole.Builder(agcMachineId + "-keystore", tasResolver)
                .runFlow(FileCreatorFlow.class, context).build();
        keystoreRole.after(execRole);
        agcMachine.addRole(keystoreRole);
    }

    private String secureStoreInsert(String token, String encrypt, String now) {
        
        StringBuilder agcSecureStoreInsert = new StringBuilder();
        agcSecureStoreInsert.append("INSERT INTO apm_secure_store (alias,cipher_text,created_date,last_read_date,client_id,user_id) VALUES ('")
        .append(token).append("','").append(encrypt).append("',"+now+",null,'Local.Local','CA.APM.SYSTEM.USER');");
        
        return agcSecureStoreInsert.toString();
    }
    
    /**
     * Returns the fully qualified domain name for the given dn.  Delegates to InetAddress.getCanonicalHostName()
     * @param dn
     * @return
     */
    private String fqdn(String dn) {
        if (dn == null) {
            return null;
        }
        try {
            return InetAddress.getByName(dn).getCanonicalHostName();
        } catch (UnknownHostException e) {
            return dn;
        }
    }
    
    
    public String[] getMemoryMonitorMachineIds() {
        return MEMORY_MONITOR_AGC_MACHINE_IDS;
    }
    
    public String[] getTimeSyncMachineIds() {
        return TIME_SYNCHRONIZATION_AGC_MACHINE_IDS;
    }
    
    public String[] getNetworkMonitorMachineIds() {
        return NETWORK_TRAFFIC_MONITOR_AGC_MACHINE_IDS;
    }
}
