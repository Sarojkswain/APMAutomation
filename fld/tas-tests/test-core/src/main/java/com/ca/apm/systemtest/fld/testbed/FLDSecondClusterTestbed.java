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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
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
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * FLD Second cluster testbed.
 * @author filja01
 *
 */
@TestBedDefinition//(cleanUpTestBed = FLDMainClusterTestBedCleaner.class)
public class FLDSecondClusterTestbed implements FldTestbedProvider, FLDConstants {
    
    //use FLDMainClusterTestbed constants
    /*
    public static final Version EM_VERSION = Version.SNAPSHOT_SYS_99_99;
    
    public static final String INSTALL_DIR = "/home/sw/em/Introscope";
    public static final String INSTALL_TG_DIR = "/home/sw/em/Installer";
    public static final String DATABASE_DIR = "/data/em/database";
    public static final String GC_LOG_FILE = INSTALL_DIR+"/logs/gclog.txt";
    public static final String INSTALL_TIM_DIR = "/opt";
    
    public static final String DB_PASSWORD = "password";
    public static final String DB_USERNAME = "cemadmin";
    public static final String DB_ADMIN_USERNAME = "postgres";
    
    public static final String ADMIN_AUX_TOKEN_HASHED =
        "8f400c257611ed5d30c0e6607ac61074307dfa24cf70a8e92c3e8147d67d2c70";
    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    */
    public static final String GC_LOG_WV_FILE = FLDMainClusterTestbed.INSTALL_DIR+"/logs/gclog_wv.txt";
    
    private static final Collection<String> MOM_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-XX:MaxPermSize=256m", "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
        "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms3072m", "-Xmx3072m",
        "-verbose:gc", "-Xloggc:"+FLDMainClusterTestbed.GC_LOG_FILE, "-Dappmap.user=admin",
        "-Dappmap.token="+FLDMainClusterTestbed.ADMIN_AUX_TOKEN);
     
    public static final Collection<String> COLL_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true","-XX:MaxPermSize=256m" , "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
        "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC", "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", "-XX:+HeapDumpOnOutOfMemoryError", "-Xms2048m", "-Xmx2048m",
        "-verbose:gc", "-Xloggc:"+FLDMainClusterTestbed.GC_LOG_FILE);
    
    public static final Collection<String> WV_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-Dorg.owasp.esapi.resources=./config/esapi", "-Dsun.java2d.noddraw=true",
        "-javaagent:./product/webview/agent/wily/Agent.jar",
        "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
        "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms1024m", "-Xmx1536m",
        "-XX:+PrintGCDateStamps", "-XX:+HeapDumpOnOutOfMemoryError",
        "-verbose:gc", "-Xloggc:"+GC_LOG_WV_FILE);
    
    public static final String ORACLE_DB_USERNAME = "CEMADMMOM";

    private FLDConfiguration fldConfig = FLDConfigurationService.getConfig();
    private ITestbedMachine momMachine;
    private ITestbedMachine webviewMachine;
    private ITestbedMachine[] collMachines;
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        // no machine necessary for the 2nd mom's DB, will install on the webviewMachine
        momMachine = new TestbedMachine.LinuxBuilder(MOM2_MACHINE_ID).templateId(FLD_LINUX_TMPL_ID)
            .bitness(Bitness.b64).build();
        
        webviewMachine = new TestbedMachine.LinuxBuilder(WEBVIEW2_MACHINE_ID).templateId(FLD_LINUX_TMPL_ID)
            .bitness(Bitness.b64).build();
        
        collMachines = new ITestbedMachine[EM_COLL2_ROLES.length];
        for (int i = 0; i < EM_COLL2_ROLES.length; i++) {
            String machineId = COLL2_MACHINES[i];
            collMachines[i] = new TestbedMachine.LinuxBuilder(machineId)
                .templateId(FLD_LINUX_TMPL_ID).bitness(Bitness.b64)
                .build(); 
        }
        
        ArrayList<ITestbedMachine> list = new ArrayList<>(Arrays.asList(collMachines));
        list.add(momMachine);
        list.add(webviewMachine);
        return list;
    }
    
    
    public String[] getMemoryMonitorMachineIds() {
        return MEMORY_MONITOR_SECOND_CLUSTER_MACHINE_IDS;
    }
    
    public String[] getTimeSyncMachineIds() {
        return TIME_SYNCHRONIZATION_SECOND_CLUSTER_MACHINE_IDS;
    }
    
    public String[] getNetworkMonitorMachineIds() {
        return NETWORK_TRAFFIC_MONITOR_SECOND_CLUSTER_MACHINE_IDS;
    }
    
    
    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {

        //ITestbed testbed = new Testbed("FLDMainClusterTestbed");

        //MOM machine
        EmRole.LinuxBuilder momBuilder = new EmRole.LinuxBuilder(EM_MOM2_ROLE_ID, tasResolver);
        String emHost = tasResolver.getHostnameById(EM_MOM2_ROLE_ID);
        
        //WebView machine
        EmRole.LinuxBuilder wvBuilder = new EmRole.LinuxBuilder(EM_MOM2_WEBVIEW_ROLE_ID, tasResolver);
        String dbHost = tasResolver.getHostnameById(EM_MOM2_WEBVIEW_ROLE_ID);
        

        //Collectors machines
        ArrayList<EmRole> collectors = new ArrayList<>();
        for (int i = 0; i < EM_COLL2_ROLES.length; i++) {
            ITestbedMachine collectorMachine = collMachines[i];
                
            EmRole.LinuxBuilder collBuilder = new EmRole.LinuxBuilder(EM_COLL2_ROLES[i], tasResolver); 
            collBuilder
                .silentInstallChosenFeatures(
                    Arrays.asList("Enterprise Manager","ProbeBuilder","EPA"))
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
                    .dbhost(dbHost);
            }
            
            collBuilder.emLaxNlClearJavaOption(COLL_LAXNL_JAVA_OPTION);
            
            EmRole collectorRole = collBuilder.build();
            collectors.add(collectorRole);
            
            collectorMachine.addRole(collectorRole);
           
            //setup logging in config/IntroscopeEnterpriseManager.properties
            IRole loggingRole = FLDMainClusterTestbed.addLoggingSetupRole(collectorMachine, collectorRole, tasResolver, 100);
            
            //start COLLECTOR
            FLDMainClusterTestbed.addStartEmRole(collectorMachine, collectorRole, false, true, loggingRole);
            
            momBuilder.emCollector(collectorRole);
        }
        
        //MOM role settings
        momBuilder
            .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager","ProbeBuilder","EPA"))
            .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
            .nostartEM()
            .nostartWV()
            .emWebPort(FLDMainClusterTestbed.EMWEBPORT)
            .version(fldConfig.getEmVersion())
            .installDir(FLDMainClusterTestbed.INSTALL_DIR)
            .installerTgDir(FLDMainClusterTestbed.INSTALL_TG_DIR)
            .emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION);
        
        if (fldConfig.isOracleMode()) {
            momBuilder
                .oracleDbHost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID))
                .oracleDbPassword(FLDMainClusterTestbed.ORACLE_DB_PASSWORD)
                .oracleDbPort(FLDMainClusterTestbed.ORACLE_DB_PORT)
                .oracleDbSidName(FLDMainClusterTestbed.ORACLE_SID_NAME)
                .oracleDbUsername(ORACLE_DB_USERNAME)
                .installerProperty("useExistingSchemaForOracle", "true")
                .useOracle();
        } else {
            momBuilder
                .dbuser(FLDMainClusterTestbed.DB_USERNAME)
                .dbpassword(FLDMainClusterTestbed.DB_PASSWORD)
                .dbAdminUser(FLDMainClusterTestbed.DB_ADMIN_USERNAME)
                .dbAdminPassword(FLDMainClusterTestbed.DB_ADMIN_PASSWORD)
                .dbhost(dbHost);
        }
        
        EmRole momRole = momBuilder.build();
        for (EmRole emRole: collectors) {
            emRole.after(momRole);
        }
        momMachine.addRole(momRole);

        //setup logging in config/IntroscopeEnterpriseManager.properties
        IRole loggingRole = FLDMainClusterTestbed.addLoggingSetupRole(momMachine, momRole, tasResolver, 100);
        
        //start MOM
        //FLDMainClusterTestbed.addStartEmRole(momMachine, momRole, false, true, loggingRole);
        
        //WebView machine
        
        wvBuilder
            .silentInstallChosenFeatures(Arrays.asList("WebView","Database"))
            .wvEmHost(emHost)
            .wvPort(FLDMainClusterTestbed.WVPORT2)
            .nostartEM()
            .nostartWV()
            .version(fldConfig.getEmVersion())
            .installDir(FLDMainClusterTestbed.INSTALL_DIR)
            .installerTgDir(FLDMainClusterTestbed.INSTALL_TG_DIR)
            .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION);
        
        if (fldConfig.isOracleMode()) {
            wvBuilder
                .oracleDbHost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID))
                .oracleDbPassword(FLDMainClusterTestbed.ORACLE_DB_PASSWORD)
                .oracleDbPort(FLDMainClusterTestbed.ORACLE_DB_PORT)
                .oracleDbSidName(FLDMainClusterTestbed.ORACLE_SID_NAME)
                .oracleDbUsername(ORACLE_DB_USERNAME)
                .useOracle();
        } else {
            wvBuilder
                .dbuser(FLDMainClusterTestbed.DB_USERNAME)
                .dbpassword(FLDMainClusterTestbed.DB_PASSWORD)
                .dbAdminUser(FLDMainClusterTestbed.DB_ADMIN_USERNAME)
                .dbAdminPassword(FLDMainClusterTestbed.DB_ADMIN_PASSWORD)
                .databaseDir(FLDMainClusterTestbed.DATABASE_DIR);
        }
        EmRole webviewRole = wvBuilder.build();
        
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
                new FileModifierFlowContext.Builder().create("/tmp/"+EM_MOM2_WEBVIEW_ROLE_ID+"_user.sh", data).build();
            ExecutionRole execRole =
                new ExecutionRole.Builder(EM_MOM2_WEBVIEW_ROLE_ID + "_user")
                    .flow(FileModifierFlow.class, createFileFlow)
                    .asyncCommand(new RunCommandFlowContext.Builder("/tmp/"+EM_MOM2_WEBVIEW_ROLE_ID+"_user.sh").build()).build();
            execRole.addProperty(FLDMainClusterTestbed.SCRIPT_LOCATION, "/tmp/"+EM_MOM2_WEBVIEW_ROLE_ID+"_user.sh");
            testbed.getMachineById(DATABASE_MACHINE_ID).addRole(execRole);
            execRole.before(webviewRole);
        }
        
        // since the DB was moved onto the webview server, it *must* be finished before the MoM and collectors are installed
        // otherwise we will see intermittent random failures when then DB is not available (happened in ITC today)S
        webviewRole.before(momRole);
        webviewRole.before(new ArrayList<IRole>(collectors));
        webviewMachine.addRole(webviewRole);
        
        //setup webview agent on WV
        Map<String, String> propsMap = new HashMap<String, String>();
        propsMap.put("introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT",emHost);
        propsMap.put("agentManager.url.1", emHost+":"+momRole.getEmPort());
        
        ConfigureFlowContext ctx =
            new ConfigureFlowContext.Builder()
                .configurationMap(FLDMainClusterTestbed.INSTALL_DIR+"/product/webview/agent/wily/core/config/IntroscopeAgent.profile", 
                    propsMap)
                .build();

        UniversalRole setWVAgent =
            new UniversalRole.Builder(EM_MOM2_WEBVIEW_ROLE_ID + "_setupWVAgentProfile", tasResolver).runFlow(ConfigureFlow.class, ctx)
                .build();
        setWVAgent.after(webviewRole);
        webviewMachine.addRole(setWVAgent);
        
        FLDMainClusterTestbed.addLogMonitorRole(momMachine, momRole, tasResolver, fldConfig.getLogMonitorEmail(),
            FLDTestbedUtil.getDefaultMomLogMonitorConfiguration());
        
        //start Webview
        //FLDMainClusterTestbed.addStartEmRole(webviewMachine, webviewRole, true, false, setWVAgent);
        
        
        // the following three calls have been moved to the main cluster
//        new MemoryMonitorTestbedProvider(MEMORY_MONITOR_SECOND_CLUSTER_MACHINE_IDS).initTestbed(testbed, tasResolver);
//        new TimeSynchronizationTestbedProvider(TIME_SYNCHRONIZATION_SECOND_CLUSTER_MACHINE_IDS).initTestbed(testbed, tasResolver);
//        new NetworkTrafficMonitorTestbedProvider(NETWORK_TRAFFIC_MONITOR_SECOND_CLUSTER_MACHINE_IDS).initTestbed(testbed, tasResolver);
    }
}
