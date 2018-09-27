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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.em.ImportDomainConfigFlowContext;
import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.systemtest.fld.artifact.ManagementModulesArtifact;
import com.ca.apm.systemtest.fld.artifact.thirdparty.FldDomainConfigArtifact;
import com.ca.apm.systemtest.fld.role.DelayRole;
import com.ca.apm.systemtest.fld.role.DockerEmRole;
import com.ca.apm.systemtest.fld.role.LogMonitorRole;
import com.ca.apm.systemtest.fld.role.TIMSettingsRole;
import com.ca.apm.systemtest.fld.testbed.loads.ConfigureTessLoadProvider;
import com.ca.apm.systemtest.fld.testbed.loads.DotNetAgentLoadProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FLDACCLoadProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FLDAgentDynamicInstrumentationProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FLDAgentRecordingSessionProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FLDCLWLoadProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FLDEntityAlertLoadProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FLDFakeWorkStationLoadProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FLDFlexLoadProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FLDGeolocationLoadProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FLDJbossLoadProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FLDRealWorkstationLoadProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FLDTIMRecordingSessionProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FLDWebLogicCrossClusterProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FLDWebSphereLoadProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FldControllerLoadProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FldHammondProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FldJMeterLoadProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FldLoadMetricSynthProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FldLoadTomcatProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FldLoadWeblogicProvider;
import com.ca.apm.systemtest.fld.testbed.loads.FldLoadWurlitzerProvider;
import com.ca.apm.systemtest.fld.testbed.loads.HVRAgentLoadProvider;
import com.ca.apm.systemtest.fld.testbed.loads.HistoricalMetricsLoadProvider;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.apm.systemtest.fld.testbed.regional.LogMonitorConfigurationSource;
import com.ca.apm.systemtest.fld.testbed.smokebeta.docker.FLDLoadWebSphereDockerProvider;
import com.ca.apm.systemtest.fld.testbed.smokebeta.docker.FldLoadJbossDockerProvider;
import com.ca.apm.systemtest.fld.testbed.smokebeta.docker.FldLoadTomcatDockerProvider;
import com.ca.apm.systemtest.fld.testbed.smokebeta.docker.FldLoadWeblogicDockerProvider;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.EmRole.LinuxBuilder;
import com.ca.tas.role.IRole;
import com.ca.tas.role.ImportDomainConfigRole;
import com.ca.tas.role.TIMRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

import static com.ca.apm.systemtest.fld.testbed.FLDLoadConstants.FLD_CONTROLLER_MACHINE_ID;

/**
 * FLD Main cluster testbed.
 * @author filja01
 *
 */
@TestBedDefinition(cleanUpTestBed = FLDMainClusterTestBedCleaner.class)
public class FLDMainClusterTestbed implements ITestbedFactory, FLDConstants {
    private static final Logger logger = LoggerFactory.getLogger(FLDMainClusterTestbed.class);

    private static FLDConfiguration fldConfig = FLDConfigurationService.getConfig();

    public static final String MMODULE_VERSION = "99.99.aquarius-SNAPSHOT";//"10.2.0.9";//Version.SNAPSHOT_SYS_99_99.toString();
    
    public static final String INSTALL_DIR = "/home/sw/em/Introscope";
    public static final String INSTALL_TG_DIR = "/home/sw/em/Installer";
    public static final String DATABASE_DIR = "/data/em/database";
    public static final String GC_LOG_FILE = INSTALL_DIR + "/logs/gclog.txt";
    private static final String GC_LOG_PARAM = fldConfig.isDockerMode() ? "/opt/ca/apm/logs/gclog.txt" : GC_LOG_FILE;
    public static final String INSTALL_TIM_DIR = "/opt";
    
    public static final String DB_PASSWORD = "wily";
    public static final String DB_USERNAME = "admin";
    public static final String DB_ADMIN_USERNAME = "postgres";
    public static final String DB_ADMIN_PASSWORD = "C@wilyapm90";
    
    //private static final String ORACLE_DB_HOST = "flddb01c.ca.com";
    public static final String ORACLE_DB_PASSWORD = "FLD";
    public static final int ORACLE_DB_PORT = 1521;
    public static final String ORACLE_SID_NAME = "orcl.ca.com";
    public static final String ORACLE_DB_USERNAME = "CEMADMIN";
    public static final String ORACLE_SID = "ORCL";
    public static final String SQLPLUS_LOCATION = "/bin/sqlplus";
    public static final String ORACLE_HOME= "/home/sw/ora/oracle/product/12.2.0/dbhome_1";
    public static final String ORACLE_SYSDB_USERNAME = "SYS";
    public static final String ORACLE_SYSDB_PASSWORD = "ApmApm?123";
    
    public static final String SCRIPT_LOCATION = "scriptLocation";

    public static final int WVPORT = 8080;
    public static final int WVPORT2 = 8082;
    public static final int EMWEBPORT = 8081;
    public static final int EM_PORT = 5001;

    public static final String ADMIN_AUX_TOKEN_HASHED =
        "8f400c257611ed5d30c0e6607ac61074307dfa24cf70a8e92c3e8147d67d2c70";
    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    
    private static final Collection<String> MOM_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
        "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC",
        //"-XX:+UseG1GC",
        //"-XX:G1ReservePercent=20", "-XX:InitiatingHeapOccupancyPercent=35",
        "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", 
        "-XX:+HeapDumpOnOutOfMemoryError", "-Xms16384m", "-Xmx16384m",
        "-verbose:gc", "-Xloggc:"+GC_LOG_PARAM, "-Dappmap.user=admin",
        "-Dappmap.token="+ADMIN_AUX_TOKEN);
    
    private static final Collection<String> COLL01_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
        "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC",
        //"-XX:+UseG1GC",
        //"-XX:G1ReservePercent=20", "-XX:InitiatingHeapOccupancyPercent=35",
        "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", 
        "-XX:+HeapDumpOnOutOfMemoryError", "-Xms8192m", "-Xmx8192m",
        "-verbose:gc", "-Xloggc:"+GC_LOG_PARAM);
    
    private static final Collection<String> COLL_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true","-XX:MaxPermSize=256m" , "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
        "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC",
        //"-XX:+UseG1GC",
        //"-XX:G1ReservePercent=20", "-XX:InitiatingHeapOccupancyPercent=35",
        "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", 
        "-XX:+HeapDumpOnOutOfMemoryError", "-Xms6144m", "-Xmx6144m",
        "-verbose:gc", "-Xloggc:"+GC_LOG_PARAM);
    
    private static final Collection<String> WV_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-Dorg.owasp.esapi.resources=./config/esapi", "-Dsun.java2d.noddraw=true",
        "-Dorg.osgi.framework.bootdelegation=org.apache.xpath", "-javaagent:./product/webview/agent/wily/Agent.jar",
        "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
        "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms2048m", "-Xmx4096m",
        //"-XX:+UseG1GC",
        //"-XX:G1ReservePercent=20", "-XX:InitiatingHeapOccupancyPercent=35",
        "-XX:+PrintGCDateStamps", "-XX:+HeapDumpOnOutOfMemoryError",
        "-verbose:gc", "-Xloggc:"+GC_LOG_PARAM);
    
    private static final String DAILYSTATS_JAVA_OPTION = "-XX:+CMSClassUnloadingEnabled -XX:+UseConcMarkSweepGC"
        + " -XX:+UseBiasedLocking -XX:SurvivorRatio=8 -XX:TargetSurvivorRatio=90 -Dlog4j.configuration=log4j.properties"
        + " -Xms256M -Xss512k -Xms4096m -Xmx4096m -XX:+HeapDumpOnOutOfMemoryError";
    
    public static final String PID_FILE_KEY = "pidFile";

    
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        
        Testbed testbed = new Testbed("FLDMainClusterTestbed");

        ITasArtifact domainConfigArtifact = new FldDomainConfigArtifact().createArtifact(fldConfig.getDomainConfigVersion());
        
        // backup machine
//        ITestbedMachine backupMachine = 
//            new TestbedMachine.LinuxBuilder(BACKUP_MACHINE_ID)
//                              .templateId(BACKUP_MACHINE_TEMPLATE_ID).bitness(Bitness.b64)
//                              .build();
//        testbed.addMachine(backupMachine);

        ITestbedMachine databaseMachine;
        if (fldConfig.isDockerMode()) {
            databaseMachine = createDbDocker(tasResolver);
        } else if (fldConfig.isOracleMode()) {
            databaseMachine = createOraDbMachine(tasResolver);
        } else {
            databaseMachine = createDbMachine(tasResolver);
        }
        
        if (!fldConfig.isOracleMode()) {
            //import domainconfig
            IRole domainConfigImportLinuxRole
                = new ImportDomainConfigRole.Builder(DB_DOMAIN_CONFIG_IMPORT_ROLE_ID, tasResolver)
                .dbHost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID))
                .dbName("cemdb")
                .dbPort(5432)
                .dbType(ImportDomainConfigFlowContext.DbType.PostgreSql)
                .dbUser(DB_USERNAME)
                .dbPassword(DB_PASSWORD)
                .emDir(INSTALL_DIR)
                .dbServiceUser(DB_ADMIN_USERNAME)
                .dbServicePwd(DB_ADMIN_PASSWORD)
                .dbInstallDir(DATABASE_DIR)
                .targetRelease(fldConfig.getDbTargetReleaseVersion())
                .importFile(domainConfigArtifact)
                .build();
            domainConfigImportLinuxRole.after(Arrays.asList(databaseMachine.getRoles()));
            
            if (! fldConfig.isDockerMode()) {
                databaseMachine.addRole(domainConfigImportLinuxRole);
            }
        }
        testbed.addMachine(databaseMachine);
        
        //Collectors machines
        List<IRole> collectorRoles = new ArrayList<>();
        for (int i = 0; i < EM_COLL_ROLES.length; i++) {
            ITestbedMachine collMachine;
            if (fldConfig.isDockerMode()) {
                collMachine = createCollectorDocker(tasResolver, databaseMachine, collectorRoles, i);
            } else {
                collMachine = createCollectorMachine(tasResolver, databaseMachine, collectorRoles, i);
            }
            collMachine.getRoleById(collMachine.getMachineId() + "_" + LOG_MONITOR_LINUX_ROLE_ID)
                .after(new HashSet<IRole>(Arrays.asList(collMachine.getRoles())));
            testbed.addMachine(collMachine);
        }

        //TIM roles
        List<IRole> timRoles = new ArrayList<>();
        for (int i = 0; i < TIM_ROLES.length; i++) {
            ITestbedMachine timMachine;
            if (fldConfig.isDockerMode()) {
                timMachine = createTimDocker(tasResolver, databaseMachine, timRoles, i);
            } else {
                timMachine = createTimMachine(tasResolver, databaseMachine, timRoles, i);
            }
            testbed.addMachine(timMachine);
        }

        //MOM machine
        ITestbedMachine momMachine;
        logger.info("FLD Docker mode: " + fldConfig.isDockerMode());
        if (fldConfig.isDockerMode()) {
            logger.info("Creating MoM with Docker");
            momMachine = createMomDocker(tasResolver, databaseMachine, collectorRoles, timRoles);
        } else {
            logger.info("Creating MoM with silent installer");
            momMachine = createMomMachine(tasResolver, databaseMachine, collectorRoles, timRoles);
        }
        testbed.addMachine(momMachine);


        ITestbedMachine webviewMachine;
        if (fldConfig.isDockerMode()) {
            webviewMachine = createWebViewDocker(tasResolver, databaseMachine, momMachine);
        } else {
            webviewMachine = createWebViewMachine(tasResolver, databaseMachine, momMachine);
        }
        testbed.addMachine(webviewMachine);

        //MOM2
        FLDSecondClusterTestbed secondCluster = new FLDSecondClusterTestbed();
        Collection<ITestbedMachine> secMachines = secondCluster.initMachines();
        if (secMachines != null) {
            testbed.addMachines(secMachines);
        }
        secondCluster.initTestbed(testbed, tasResolver);
        //AGC
        FLDAGCTestbed agcTestbed = new FLDAGCTestbed();
        Collection<ITestbedMachine> agcMachines = agcTestbed.initMachines();
        if (agcMachines != null) {
            testbed.addMachines(agcMachines);
        }
        agcTestbed.initTestbed(testbed, tasResolver);
        
        //start EM after install and AGC registration
        if (!fldConfig.isDockerMode()) {
            startEM(tasResolver, testbed, MOM_MACHINE_ID, WEBVIEW_MACHINE_ID, EM_MOM_ROLE_ID, EM_WEBVIEW_ROLE_ID, true);
        }
        startEM(tasResolver, testbed, MOM2_MACHINE_ID, WEBVIEW2_MACHINE_ID, EM_MOM2_ROLE_ID, EM_MOM2_WEBVIEW_ROLE_ID, true);
        startEM(tasResolver, testbed, AGC_MACHINE_ID, AGC_MACHINE_ID, AGC_ROLE_ID, null, true);

        List<FldTestbedProvider> testbedProviders = new ArrayList<>();
        testbedProviders.add(new FldControllerLoadProvider(fldConfig));
        if (fldConfig.isTomcatDockerDeploy()) {
            testbedProviders.add(new FldLoadTomcatDockerProvider());
        } else {
            testbedProviders.add(new FldLoadTomcatProvider());
        }
        if (fldConfig.isWeblogicDockerDeploy()) {
            testbedProviders.add(new FldLoadWeblogicDockerProvider());
        } else {
            testbedProviders.add(new FldLoadWeblogicProvider());
        }
        if (fldConfig.isWebsphereDockerDeploy()) {
            testbedProviders.add(new FLDLoadWebSphereDockerProvider());
        } else {
            testbedProviders.add(new FLDWebSphereLoadProvider());
        }
        if (fldConfig.isJBossDockerDeploy()) {
            testbedProviders.add(new FldLoadJbossDockerProvider());
        } else {
            testbedProviders.add(new FLDJbossLoadProvider());
        }
        testbedProviders.add(new DotNetAgentLoadProvider());
        testbedProviders.add(new FLDWebLogicCrossClusterProvider());
        testbedProviders.add(new FldLoadMetricSynthProvider());
        testbedProviders.add(new HVRAgentLoadProvider());
        testbedProviders.add(new FldHammondProvider().updateCollRoleId(EM_COLL09_ROLE_ID));
        testbedProviders.add(new FLDACCLoadProvider());
        testbedProviders.add(new FLDAgentDynamicInstrumentationProvider());
        testbedProviders.add(new FLDCLWLoadProvider());
        testbedProviders.add(new FLDEntityAlertLoadProvider());
        testbedProviders.add(new FLDFakeWorkStationLoadProvider(fldConfig.getEmVersion()));
        testbedProviders.add(new FLDGeolocationLoadProvider());
        testbedProviders.add(new FLDRealWorkstationLoadProvider());
        testbedProviders.add(new SeleniumWebViewLoadFldTestbedProvider());
        testbedProviders.add(new FLDFlexLoadProvider(testbed.getMachineById(TIM05_MACHINE_ID),
            tasResolver.getHostnameById(TIM05_ROLE_ID)));
        testbedProviders.add(new FldJMeterLoadProvider());
        testbedProviders.add(new FLDAgentRecordingSessionProvider());
        testbedProviders.add(new FLDTIMRecordingSessionProvider());
        testbedProviders.add(new HistoricalMetricsLoadProvider());
        List<String> ids = new ArrayList<>(Arrays.asList(MEMORY_MONITOR_MAIN_CLUSTER_MACHINE_IDS));
        ids.addAll(Arrays.asList(secondCluster.getMemoryMonitorMachineIds()));
        ids.addAll(Arrays.asList(agcTestbed.getMemoryMonitorMachineIds()));
        testbedProviders.add(new MemoryMonitorTestbedProvider(ids.toArray(new String[ids.size()])));
        ids = new ArrayList<>(Arrays.asList(TIME_SYNCHRONIZATION_MAIN_CLUSTER_MACHINE_IDS));
        ids.addAll(Arrays.asList(secondCluster.getTimeSyncMachineIds()));
        ids.addAll(Arrays.asList(agcTestbed.getTimeSyncMachineIds()));
        testbedProviders.add(new TimeSynchronizationTestbedProvider(ids.toArray(new String[ids.size()])));
        testbedProviders.add(new FLDApmJDBCQueryLoadTestbedProvider(tasResolver.getHostnameById(EM_MOM_ROLE_ID), FLD_CONTROLLER_MACHINE_ID));
        testbedProviders.add(new FLDCEMTessTestbedProvider());
        
        /*
         * WARNING: this load provider must be loaded last to ensure that its
         * dependent roles are available
         */
        testbedProviders.add(new ConfigureTessLoadProvider());
        
        // add all pre-requisite machines to the testbed
        for (FldTestbedProvider provider: testbedProviders) {
            Collection<ITestbedMachine> machines = provider.initMachines();
            if (machines != null) {
                testbed.addMachines(machines);
            }
        }
        // and initialize the roles
        for (FldTestbedProvider provider: testbedProviders) {
            provider.initTestbed(testbed, tasResolver);
        }
        

        return testbed;
    }

    private ITestbedMachine createMomDocker(ITasResolver tasResolver, ITestbedMachine databaseMachine, List<IRole> collectorRoles, List<IRole> timRoles) {
        ITestbedMachine momMachine =
                new TestbedMachine.LinuxBuilder(MOM_MACHINE_ID)
                        .templateId(FLD_MOM_TMPL_ID).bitness(Bitness.b64)
                        .build();
        DockerEmRole.MomBuilder momBuilder = new DockerEmRole.MomBuilder(EM_MOM_ROLE_ID, tasResolver);

        momBuilder
//                .autostartApmSqlServer()
//                .dbpassword(DB_PASSWORD)
//                .dbAdminPassword(DB_ADMIN_PASSWORD)
//                .dbuser(DB_USERNAME)
                .dbhost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID))
                .version(fldConfig.getEmVersion())
                .emWebPort(EMWEBPORT)
                .installDir(INSTALL_DIR)
                .emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION);

        for (IRole collectorRole : collectorRoles) {
            momBuilder.emCollector(collectorRole);
        }

        for (IRole timRole : timRoles) {
            momBuilder.tim((TIMRole) timRole);
        }

        momBuilder = configureClamps(momBuilder);

        //setup logging in config/IntroscopeEnterpriseManager.properties
        momBuilder.laxProperty("log4j.appender.logfile.MaxBackupIndex", "200");
        momBuilder.laxProperty("log4j.logger.Manager", "VERBOSE#com.wily.util.feedback.Log4JSeverityLevel, console, logfile");

        DockerEmRole momRole = momBuilder.build();
        momRole.after(new HashSet<>(Arrays.asList(databaseMachine.getRoles())));
        momRole.before(collectorRoles);
        momMachine.addRole(momRole);

        //set loadbalancing.xml on MOM
        FileModifierFlowContext modifyLoadbalancing =
                new FileModifierFlowContext.Builder().resource(INSTALL_DIR + "/config/loadbalancing.xml", "/em-config/loadbalancing.xml")
                        .build();

        UniversalRole setLoadbalancing =
                new UniversalRole.Builder("setLoadbalancing", tasResolver)
                        .runFlow(FileModifierFlow.class, modifyLoadbalancing).build();
        setLoadbalancing.before(momRole);
        momMachine.addRole(setLoadbalancing);

        //set  management modules on MOM
        ManagementModulesArtifact mmArtifact = new ManagementModulesArtifact(tasResolver);
        Artifact mm = mmArtifact.createArtifact().getArtifact();
        UniversalRole mmRole =
                new UniversalRole.Builder("mmConfigRol", tasResolver)
                        .unpack(mm, INSTALL_DIR+"/modules").build();

        mmRole.before(momRole);
        momMachine.addRole(mmRole);

        //set Realms.xml on MOM
        //FileModifierFlowContext modifyRealms =
        //        new FileModifierFlowContext.Builder().resource(INSTALL_DIR+"/config/realms.xml", "/em-config/realms.xml")
        //                .build();

        //UniversalRole setRealms =
        //        new UniversalRole.Builder("setRealmsResource", tasResolver)
        //                .runFlow(FileModifierFlow.class, modifyRealms).build();
        //setRealms.after(mmRole);
        //setRealms.before(momRole);
        //momMachine.addRole(setRealms);

        addLogMonitorRole(momMachine, momRole, tasResolver, fldConfig.getLogMonitorEmail(),
                fldConfig.getMomLogMonitorConfiguration());
        return momMachine;
    }

    @NotNull
    private ITestbedMachine createMomMachine(ITasResolver tasResolver, ITestbedMachine databaseMachine, List<IRole> collectorRoles, List<IRole> timRoles) {
        ITestbedMachine momMachine =
                new TestbedMachine.LinuxBuilder(MOM_MACHINE_ID)
                        .templateId(FLD_MOM_TMPL_ID).bitness(Bitness.b64)
                        .build();
        LinuxBuilder momBuilder = new LinuxBuilder(EM_MOM_ROLE_ID, tasResolver);

        momBuilder
        .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager","ProbeBuilder","EPA"))
        .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
        .nostartEM()
        .nostartWV()
        .autostartApmSqlServer()
        .apmSqlServerBindAddress("0.0.0.0")
        .version(fldConfig.getEmVersion())
        .emPort(EM_PORT)
        .emWebPort(EMWEBPORT)
        .installDir(INSTALL_DIR)
        .installerTgDir(INSTALL_TG_DIR)
        .emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION);

        if (fldConfig.isOracleMode()) {
            momBuilder
                .oracleDbHost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID))
                .oracleDbPassword(ORACLE_DB_PASSWORD)
                .oracleDbPort(ORACLE_DB_PORT)
                .oracleDbSidName(ORACLE_SID_NAME)
                .oracleDbUsername(ORACLE_DB_USERNAME)
                .installerProperty("useExistingSchemaForOracle", "true")
                .useOracle();
        } else {
            momBuilder
                .dbpassword(DB_PASSWORD)
                .dbAdminPassword(DB_ADMIN_PASSWORD)
                .dbuser(DB_USERNAME)
                .dbhost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID));
        }
        
        for (IRole collectorRole : collectorRoles) {
            momBuilder.emCollector((EmRole) collectorRole);
        }

        for (IRole timRole : timRoles) {
            momBuilder.tim((TIMRole) timRole);
        }

        momBuilder = configureClamps(momBuilder);

        EmRole momRole = momBuilder.build();
        momRole.after(new HashSet<>(Arrays.asList(databaseMachine.getRoles())));
        momRole.before(collectorRoles);
        momMachine.addRole(momRole);

        //set loadbalancing.xml on MOM
        FileModifierFlowContext modifyLoadbalancing =
                new FileModifierFlowContext.Builder().resource(INSTALL_DIR + "/config/loadbalancing.xml", "/em-config/loadbalancing.xml")
                        .build();

        UniversalRole setLoadbalancing =
                new UniversalRole.Builder("setLoadbalancing", tasResolver)
                        .runFlow(FileModifierFlow.class, modifyLoadbalancing).build();
        setLoadbalancing.after(momRole);
        momMachine.addRole(setLoadbalancing);

        //set  management modules on MOM
        ManagementModulesArtifact mmArtifact = new ManagementModulesArtifact(tasResolver);
        Artifact mm = mmArtifact.createArtifact().getArtifact();
        UniversalRole mmRole =
            new UniversalRole.Builder("mmConfigRol", tasResolver)
                .unpack(mm, INSTALL_DIR+"/config/modules").build();

        mmRole.after(momRole);
        momMachine.addRole(mmRole);

        //set Realms.xml on MOM
        FileModifierFlowContext modifyRealms =
            new FileModifierFlowContext.Builder().resource(INSTALL_DIR+"/config/realms.xml", "/em-config/realms.xml")
                .build();

        UniversalRole setRealms =
            new UniversalRole.Builder("setRealmsResource", tasResolver)
                .runFlow(FileModifierFlow.class, modifyRealms).build();
        setRealms.after(mmRole);
        momMachine.addRole(setRealms);

        //setup logging in config/IntroscopeEnterpriseManager.properties
        IRole loggingRole = addLoggingSetupRole(momMachine, setRealms, tasResolver, 200);

        addLogMonitorRole(momMachine, momRole, tasResolver, fldConfig.getLogMonitorEmail(),
            fldConfig.getMomLogMonitorConfiguration());
        //start MOM
        //addStartEmRole(momMachine, momRole, false, true, loggingRole);
        return momMachine;
    }

    @NotNull
    private ITestbedMachine createWebViewDocker(ITasResolver tasResolver, ITestbedMachine databaseMachine, ITestbedMachine momMachine) {
        //WebView machine
        ITestbedMachine webviewMachine =
                new TestbedMachine.LinuxBuilder(WEBVIEW_MACHINE_ID)
                        .templateId(getMachineTemplate(WEBVIEW_MACHINE_ID, FLD_WEBVIEW_TMPL_ID))
                        .bitness(Bitness.b64)
                        .build();
        String emHost = tasResolver.getHostnameById(EM_MOM_ROLE_ID);
        DockerEmRole webviewRole =
                new DockerEmRole.WVBuilder(EM_WEBVIEW_ROLE_ID, tasResolver)
                        .wvEmHost(emHost)
                        .wvPort(WVPORT)
                        .version(fldConfig.getEmVersion())
                        .installDir(INSTALL_DIR)
                        .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION)
                        .agentProperty("introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT",emHost)
                        .agentProperty("agentManager.url.1", emHost+":"+EM_PORT)
                        .build();

        webviewRole.after(new HashSet<>(Arrays.asList(databaseMachine.getRoles())));
        webviewRole.after(new HashSet<>(Arrays.asList(momMachine.getRoles())));
        webviewMachine.addRole(webviewRole);

        addLogMonitorRole(webviewMachine, webviewRole, tasResolver, fldConfig.getLogMonitorEmail(),
                fldConfig.getWebViewLogMonitorConfiguration());
        return webviewMachine;
    }

    @NotNull
    private ITestbedMachine createWebViewMachine(ITasResolver tasResolver, ITestbedMachine databaseMachine, ITestbedMachine momMachine) {
        //WebView machine
        ITestbedMachine webviewMachine =
                new TestbedMachine.LinuxBuilder(WEBVIEW_MACHINE_ID)
                        .templateId(getMachineTemplate(WEBVIEW_MACHINE_ID, FLD_WEBVIEW_TMPL_ID))
                        .bitness(Bitness.b64)
                        .build();
        String emHost = tasResolver.getHostnameById(EM_MOM_ROLE_ID);
        EmRole webviewRole =
            new LinuxBuilder(EM_WEBVIEW_ROLE_ID, tasResolver)
                .silentInstallChosenFeatures(Collections.singletonList("WebView"))
                .wvEmHost(emHost)
                .wvPort(WVPORT)
                .nostartEM()
                .nostartWV()
                .version(fldConfig.getEmVersion())
                .installDir(INSTALL_DIR)
                .installerTgDir(INSTALL_TG_DIR)
                .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION)
                .build();

        webviewRole.after(new HashSet<>(Arrays.asList(databaseMachine.getRoles())));
        webviewRole.after(new HashSet<>(Arrays.asList(momMachine.getRoles())));
        webviewMachine.addRole(webviewRole);

        //setup webview agent on WV
        Map<String, String> propsMap = new HashMap<>();
        propsMap.put("introscope.agent.enterprisemanager.transport.tcp.host.DEFAULT",emHost);
        propsMap.put("agentManager.url.1", emHost+":"+EM_PORT);

        ConfigureFlowContext ctx =
            new ConfigureFlowContext.Builder()
                .configurationMap(INSTALL_DIR+"/product/webview/agent/wily/core/config/IntroscopeAgent.profile",
                    propsMap)
                .build();

        UniversalRole setWVAgent =
            new UniversalRole.Builder(EM_WEBVIEW_ROLE_ID + "_setupWVAgentProfile", tasResolver).runFlow(ConfigureFlow.class, ctx)
                .build();
        setWVAgent.after(webviewRole);
        webviewMachine.addRole(setWVAgent);

        addLogMonitorRole(webviewMachine, webviewRole, tasResolver, fldConfig.getLogMonitorEmail(),
            fldConfig.getWebViewLogMonitorConfiguration());
        //start Webview
        //addStartEmRole(webviewMachine, webviewRole, true, false, setWVAgent);
        return webviewMachine;
    }

    @NotNull
    private ITestbedMachine createTimDocker(ITasResolver tasResolver, ITestbedMachine databaseMachine, List<IRole> timRoles, int timIndex) {
        return createTimMachine(tasResolver, databaseMachine, timRoles, timIndex);
    }

    @NotNull
    private ITestbedMachine createTimMachine(ITasResolver tasResolver, ITestbedMachine databaseMachine, List<IRole> timRoles, int timIndex) {
        String timMachineName = TIM_MACHINES[timIndex];
        ITestbedMachine timMachine =
            new TestbedMachine.LinuxBuilder(timMachineName)
                              .templateId(FLD_TIM_TMPL_ID).bitness(Bitness.b64)
                              .build();
        TIMRole.Builder timBuilder = new TIMRole.Builder(TIM_ROLES[timIndex], tasResolver);
        TIMRole timRole = timBuilder
        .timVersion(fldConfig.getEmVersion())
        .installDir(INSTALL_TIM_DIR)
        .build();
        
        //FIXME - this is a temporary hack until the TIMRole fix can be pushed in TAS into 
        // a release
        fixServiceCommandFlow(timRole);

        timRoles.add(timRole);

        timRole.after(new HashSet<>(Arrays.asList(databaseMachine.getRoles())));
        timMachine.addRole(timRole);
        String timHostname = tasResolver.getHostnameById(TIM_ROLES[timIndex]);

        //TIM settings roles
        TIMSettingsRole timSetRole = new TIMSettingsRole.Builder("setTIMRole"+ timIndex, tasResolver)
            .requestType("setDatabaseSetting")
            .settingName("MaxFlexRequestBodySize")
            .settingValue("100000")
            .timHostname(timHostname)
            .build();
        timSetRole.after(timRole);
        timMachine.addRole(timSetRole);

        TIMSettingsRole timSetRoleE = new TIMSettingsRole.Builder("setTIMRoleE"+ timIndex, tasResolver)
            .requestType("setDatabaseSetting")
            .settingName("MaxFlexResponseBodySize")
            .settingValue("100000")
            .timHostname(timHostname)
            .build();
        timSetRoleE.after(timRole);
        timMachine.addRole(timSetRoleE);

        TIMSettingsRole timSetRoleI = new TIMSettingsRole.Builder("setTIMRoleI"+ timIndex, tasResolver)
            .requestType("setNetworkInterfaces")
            .networkInterfaces("eth2")
            .timHostname(timHostname)
            .build();
        timSetRoleI.after(timRole);
        timMachine.addRole(timSetRoleI);
        return timMachine;
    }

    private void fixServiceCommandFlow(TIMRole timRole) {
        try {
            Field field = timRole.getClass().getDeclaredField("startCommands");
            field.setAccessible(true);
            RunCommandFlowContext[] startCommands = (RunCommandFlowContext[]) field.get(timRole);
            for (RunCommandFlowContext ctx: startCommands) {
                Field execField = ctx.getClass().getDeclaredField("executable");
                execField.setAccessible(true);
                String executable = (String) execField.get(ctx);
                if ("service".equals(executable)) {
                    execField.set(ctx, "/sbin/service");
                }
            }
        } catch (Exception e) {
            // do nothing: this is just a nasty hack while waiting on TAS to get updated with proper fix
            e.printStackTrace();
        }
        
    }

    
    @NotNull
    private ITestbedMachine createCollectorDocker(ITasResolver tasResolver, ITestbedMachine databaseMachine, List<IRole> collectorRoles, int i) {
        String collMachineName = COLL_MACHINES[i];
        ITestbedMachine collectorMachine =
                new TestbedMachine.LinuxBuilder(collMachineName)
                            .templateId(FLD_COLL_TMPLS[i]).bitness(Bitness.b64)
                            .build();
        DockerEmRole.CollectorBuilder collBuilder = new DockerEmRole.CollectorBuilder(EM_COLL_ROLES[i], tasResolver);
        collBuilder
//                .dbpassword(DB_PASSWORD)
//                .dbAdminPassword(DB_ADMIN_PASSWORD)
//                .dbuser(DB_USERNAME)
                .dbhost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID))
                .version(fldConfig.getEmVersion())
                .installDir(INSTALL_DIR);

        if (collMachineName.equals(COLL01_MACHINE_ID)) { //collector 1
            collBuilder.emLaxNlClearJavaOption(COLL01_LAXNL_JAVA_OPTION);
        } else {
            collBuilder.emLaxNlClearJavaOption(COLL_LAXNL_JAVA_OPTION);
        }
        collBuilder = configureClamps(collBuilder);

        //setup logging in config/IntroscopeEnterpriseManager.properties
        collBuilder.laxProperty("log4j.appender.logfile.MaxBackupIndex", "200");
        collBuilder.laxProperty("log4j.logger.Manager", "VERBOSE#com.wily.util.feedback.Log4JSeverityLevel, console, logfile");

        //tess-default.properties settings role on Coll02
        if (collMachineName.equals(COLL02_MACHINE_ID)) { //collector 2
            collBuilder
                .tessDefaultProperty("dailystats.jvmArgs",DAILYSTATS_JAVA_OPTION)
                .tessDefaultProperty("dailystats.aggregateInSeparateJvm", "true");
        }

        DockerEmRole collectorRole = collBuilder.build();
        collectorRoles.add(collectorRole);

        collectorRole.after(new HashSet<>(Arrays.asList(databaseMachine.getRoles())));

        DelayRole delayRole = new DelayRole.Builder(collectorRole.getRoleId() + "-delay").delaySeconds(10 * i).build();
        delayRole.before(collectorRole);

        collectorMachine.addRole(collectorRole, delayRole);


        addLogMonitorRole(collectorMachine, collectorRole, tasResolver, fldConfig.getLogMonitorEmail(),
                fldConfig.getCollectorLogMonitorConfiguration(collectorRole.getRoleId()));

        return collectorMachine;
    }

    @NotNull
    private ITestbedMachine createCollectorMachine(ITasResolver tasResolver, ITestbedMachine databaseMachine, List<IRole> collectorRoles, int i) {
        String collMachineName = COLL_MACHINES[i];
        ITestbedMachine collectorMachine =
                new TestbedMachine.LinuxBuilder(collMachineName)
                            .templateId(FLD_COLL_TMPLS[i]).bitness(Bitness.b64)
                            .build();
        LinuxBuilder collBuilder = new LinuxBuilder(EM_COLL_ROLES[i], tasResolver);
        collBuilder
        .silentInstallChosenFeatures(
            Arrays.asList("Enterprise Manager","ProbeBuilder","EPA"))
        .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
        .nostartEM()
        .nostartWV()
        .version(fldConfig.getEmVersion())
        .installDir(INSTALL_DIR)
        .installerTgDir(INSTALL_TG_DIR);
        
        if (fldConfig.isOracleMode()) {
            collBuilder
                .oracleDbHost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID))
                .oracleDbPassword(ORACLE_DB_PASSWORD)
                .oracleDbPort(ORACLE_DB_PORT)
                .oracleDbSidName(ORACLE_SID_NAME)
                .oracleDbUsername(ORACLE_DB_USERNAME)
                .installerProperty("useExistingSchemaForOracle", "true")
                .useOracle();
        } else {
            collBuilder
                .dbpassword(DB_PASSWORD)
                .dbAdminPassword(DB_ADMIN_PASSWORD)
                .dbuser(DB_USERNAME)
                .dbhost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID));
        }

        if (collMachineName.equals(COLL01_MACHINE_ID)) { //collector 1
            collBuilder.emLaxNlClearJavaOption(COLL01_LAXNL_JAVA_OPTION);
        } else {
            collBuilder.emLaxNlClearJavaOption(COLL_LAXNL_JAVA_OPTION);
        }
        collBuilder = configureClamps(collBuilder);

        EmRole collectorRole = collBuilder.build();
        collectorRoles.add(collectorRole);

        collectorRole.after(new HashSet<>(Arrays.asList(databaseMachine.getRoles())));

        DelayRole delayRole = new DelayRole.Builder(collectorRole.getRoleId() + "-delay").delaySeconds(10 * i).build();
        delayRole.before(collectorRole);

        collectorMachine.addRole(collectorRole, delayRole);

        //setup logging in config/IntroscopeEnterpriseManager.properties
        IRole loggingRole = addLoggingSetupRole(collectorMachine, collectorRole, tasResolver, 200);

        addLogMonitorRole(collectorMachine, collectorRole, tasResolver, fldConfig.getLogMonitorEmail(),
            fldConfig.getCollectorLogMonitorConfiguration(collectorRole.getRoleId()));

        //tess-default.properties settings role on Coll02
        if (collMachineName.equals(COLL02_MACHINE_ID)) { //collector 2
            Map<String, String> propsMap = new HashMap<>();

            propsMap.put("dailystats.jvmArgs",DAILYSTATS_JAVA_OPTION);
            propsMap.put("dailystats.aggregateInSeparateJvm", "true");

            ConfigureFlowContext ctx =
                new ConfigureFlowContext.Builder().configurationMap(INSTALL_DIR+"/config/tess-default.properties", propsMap)
                    .build();

            UniversalRole configureColl02 =
                new UniversalRole.Builder("configColl02TessDefProp", tasResolver).runFlow(ConfigureFlow.class, ctx)
                    .build();
            configureColl02.after(loggingRole);
            collectorMachine.addRole(configureColl02);
            //start COLLECTOR
            addStartEmRole(collectorMachine, collectorRole, false, true, configureColl02);
        } else {
            //start COLLECTOR
            addStartEmRole(collectorMachine, collectorRole, false, true, loggingRole);
        }
        return collectorMachine;
    }

    @NotNull
    private ITestbedMachine createDbDocker(ITasResolver tasResolver) {
        //database machine
        ITestbedMachine databaseMachine =
                new TestbedMachine.LinuxBuilder(DATABASE_MACHINE_ID)
                        .templateId(getMachineTemplate(DATABASE_MACHINE_ID, FLD_DATBASE_TMPL_ID))
                        .bitness(Bitness.b64)
                        .build();

        DockerEmRole databaseRole = new DockerEmRole.DbBuilder(EM_DATABASE_ROLE_ID, tasResolver)
//                .dbuser(DB_USERNAME)
//                .dbpassword(DB_PASSWORD)
//                .dbAdminUser(DB_ADMIN_USERNAME)
//                .dbAdminPassword(DB_ADMIN_PASSWORD)
                .autostartApmSqlServer()
                .nostartEM()
                .nostartWV()
                .version(fldConfig.getEmVersion())
                .installDir(INSTALL_DIR)
                .exposeFolder("install")
                .build();

        databaseMachine.addRole(databaseRole);
        return databaseMachine;
    }

    @NotNull
    private ITestbedMachine createDbMachine(ITasResolver tasResolver) {
        //database machine
        ITestbedMachine databaseMachine =
                new TestbedMachine.LinuxBuilder(DATABASE_MACHINE_ID)
                        .templateId(getMachineTemplate(DATABASE_MACHINE_ID, FLD_DATBASE_TMPL_ID))
                        .bitness(Bitness.b64)
                        .build();

        EmRole databaseRole =
            new LinuxBuilder(EM_DATABASE_ROLE_ID, tasResolver)
                .silentInstallChosenFeatures(Collections.singletonList("Database"))
                .dbuser(DB_USERNAME)
                .dbpassword(DB_PASSWORD)
                .dbAdminUser(DB_ADMIN_USERNAME)
                .dbAdminPassword(DB_ADMIN_PASSWORD)
                .nostartEM()
                .nostartWV()
                .version(fldConfig.getEmVersion())
                .databaseDir(DATABASE_DIR)
                .installDir(INSTALL_DIR)
                .installerTgDir(INSTALL_TG_DIR)
                .build();
        databaseMachine.addRole(databaseRole);
        return databaseMachine;
    }
    
    @NotNull
    private ITestbedMachine createOraDbMachine(ITasResolver tasResolver) {
        //database machine
        ITestbedMachine databaseMachine =
                new TestbedMachine.LinuxBuilder(DATABASE_MACHINE_ID)
                        .templateId(getMachineTemplate(DATABASE_MACHINE_ID, FLD_DATBASE_TMPL_ID))
                        .bitness(Bitness.b64)
                        .build();
        
        Collection<String> data = Arrays.asList(
            "export ORACLE_HOME=" + ORACLE_HOME,
            ORACLE_HOME + SQLPLUS_LOCATION + " " + ORACLE_SYSDB_USERNAME + "/" 
                + ORACLE_SYSDB_PASSWORD + "@" + ORACLE_SID + " as SYSDBA <<EOF\n"
                + "alter session set \"_oracle_script\"=true;\n"
                + "drop user "+ORACLE_DB_USERNAME+" cascade;\n"
                + "create user "+ORACLE_DB_USERNAME+" identified by "+ORACLE_DB_PASSWORD+";\n"
                + "GRANT CONNECT, RESOURCE, CREATE TRIGGER, CREATE SEQUENCE, CREATE TYPE, CREATE PROCEDURE, "
                + "CREATE TABLE, CREATE SESSION, CREATE VIEW, ANALYZE ANY, UNLIMITED TABLESPACE TO "+ORACLE_DB_USERNAME+";\n"
                + "EOF");
        
        FileModifierFlowContext createFileFlow =
            new FileModifierFlowContext.Builder().create("/tmp/"+EM_DATABASE_ROLE_ID+"_user.sh", data).build();
        ExecutionRole execRole =
            new ExecutionRole.Builder(EM_DATABASE_ROLE_ID + "_user")
                .flow(FileModifierFlow.class, createFileFlow)
                .asyncCommand(new RunCommandFlowContext.Builder("/tmp/"+EM_DATABASE_ROLE_ID+"_user.sh").build()).build();
        execRole.addProperty(SCRIPT_LOCATION, "/tmp/"+EM_DATABASE_ROLE_ID+"_user.sh");

        EmRole databaseRole =
            new LinuxBuilder(EM_DATABASE_ROLE_ID, tasResolver)
                .silentInstallChosenFeatures(Collections.singletonList("Database"))
                .oracleDbHost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID))
                .oracleDbPassword(ORACLE_DB_PASSWORD)
                .oracleDbPort(ORACLE_DB_PORT)
                .oracleDbSidName(ORACLE_SID_NAME)
                .oracleDbUsername(ORACLE_DB_USERNAME)
                .useOracle()
                .nostartEM()
                .nostartWV()
                .version(fldConfig.getEmVersion())
                .installDir(INSTALL_DIR)
                .installerTgDir(INSTALL_TG_DIR)
                .build();
        databaseRole.after(execRole);
        databaseMachine.addRole(databaseRole, execRole);
        return databaseMachine;
    }

    private void startEM(ITasResolver tasResolver, ITestbed testbed, String momMachineId,
        String webviewMachineId, String emMomRoleId, String emWebviewRoleId, boolean logMon) {
        
        if (momMachineId != null && !StringUtils.isBlank(emMomRoleId)) {
            ITestbedMachine momMachine = testbed.getMachineById(momMachineId);
            EmRole emRole = (EmRole)testbed.getRoleById(emMomRoleId);
            boolean startWVb = false;
            if (webviewMachineId != null && webviewMachineId.equals(momMachineId) 
                && StringUtils.isBlank(emWebviewRoleId)) {
                startWVb = true;
            }
            List<IRole> momRoles = new ArrayList<IRole>();
            IRole momLogRole = null;
            for (IRole r : Arrays.asList(momMachine.getRoles())) {
                if (r.getRoleId().equals(momMachineId + "_" + LOG_MONITOR_LINUX_ROLE_ID)) {
                    momLogRole = r;
                } else {
                    momRoles.add(r);
                }
            }
            IRole startEM = addStartEmRole(momMachine, emRole, startWVb, true, new HashSet<IRole>(momRoles));
            if (momLogRole != null && logMon) {
                momLogRole.after(startEM);
            }
            
            if (webviewMachineId != null && !StringUtils.isBlank(emWebviewRoleId)) {
                ITestbedMachine wvMachine = testbed.getMachineById(webviewMachineId);
                EmRole wvRole = (EmRole)testbed.getRoleById(emWebviewRoleId);
                List<IRole> wvRoles = new ArrayList<IRole>();
                IRole wvLogRole = null;
                for (IRole r : Arrays.asList(wvMachine.getRoles())) {
                    if (r.getRoleId().equals(webviewMachineId + "_" + LOG_MONITOR_LINUX_ROLE_ID)) {
                        wvLogRole = r;
                    } else {
                        wvRoles.add(r);
                    }
                }
                wvRoles.add(startEM);
                IRole startWV = addStartEmRole(wvMachine, wvRole, true, false, new HashSet<IRole>(wvRoles));
                if (wvLogRole != null && logMon) {
                    wvLogRole.after(startWV);
                }
            }
        }
    }
    
    static IRole addStartEmRole(ITestbedMachine machine, EmRole emRole, boolean startWV,
                                boolean startEM, IRole beforeRole) {
        return addStartEmRole(machine, emRole, startWV, startEM, new HashSet<IRole>(Arrays.asList(beforeRole)));
    }
    
    static IRole addStartEmRole(ITestbedMachine machine, EmRole emRole, boolean startWV,
                                 boolean startEM, HashSet<IRole> beforeRoles) {
        //starts EM or WebView
        ExecutionRole.Builder builder =
            new ExecutionRole.Builder(emRole.getRoleId() + "_start");
         
        if (startEM) {
            builder.asyncCommand(emRole.getEmRunCommandFlowContext());
        }
        if (startWV) {
            builder.asyncCommand(emRole.getWvRunCommandFlowContext());
        }
        ExecutionRole startRole = builder.build();
        startRole.after(beforeRoles);
        machine.addRole(startRole);
        
        return startRole;
    }

    static LogMonitorRole.Builder configureLogMonitorBuilder(LogMonitorRole.Builder builder,
        LogMonitorConfigurationSource configSource) {
        switch (configSource.getSourceType()) {
            case DiskFile:
                builder.configFile(configSource.getPath());
                break;

            case ResourceFile:
                builder.configFileFromResource(configSource.getPath());
                break;
        }

        return builder;
    }

    static void addLogMonitorRole(ITestbedMachine machine, IRole after, ITasResolver tasResolver,
        String[] emailArray, LogMonitorConfigurationSource logMonitorConfigurationSource) {
        LogMonitorRole logMonitorLinuxRole
            = configureLogMonitorBuilder(
                new LogMonitorRole.LinuxBuilder(
                    machine.getMachineId() + "_" + LOG_MONITOR_LINUX_ROLE_ID, tasResolver),
                logMonitorConfigurationSource)
            .emails(Arrays.asList(emailArray))
            .vars(Collections.singletonMap("currentLogDir", INSTALL_DIR + "/logs"))
            .build();
        logMonitorLinuxRole.addProperty(PID_FILE_KEY,
            logMonitorLinuxRole.getDeployLogMonitorFlowContext().getPidFile());
        //logMonitorLinuxRole.after(after);
        machine.addRole(logMonitorLinuxRole);
    }
    
    private String getMachineTemplate(String machineId, String defaultTemplateId) {
        if (fldConfig.getMachineTemplateOverrides().containsKey(machineId)) {
            return fldConfig.getMachineTemplateOverrides().get(machineId);
        }
        return defaultTemplateId;
    }
    
    
    static IRole addLoggingSetupRole(ITestbedMachine machine, IRole afterRole, 
                                          ITasResolver tasResolver, Integer backupIndex) {
            
        Map<String, String> propsMap = new HashMap<>();
        
        if (backupIndex == null) backupIndex = 4; //set default value
        
        propsMap.put("log4j.appender.logfile.MaxBackupIndex", backupIndex.toString());
        propsMap.put("log4j.logger.Manager", "VERBOSE#com.wily.util.feedback.Log4JSeverityLevel, console, logfile");

        ConfigureFlowContext ctx =
            new ConfigureFlowContext.Builder().configurationMap(INSTALL_DIR+"/config/IntroscopeEnterpriseManager.properties", propsMap)
                .build();

        UniversalRole configureLogSetup =
            new UniversalRole.Builder("configLogSetup_"+machine.getMachineId(), tasResolver).runFlow(ConfigureFlow.class, ctx)
                .build();
        configureLogSetup.after(afterRole);
        machine.addRole(configureLogSetup);
        
        return configureLogSetup;
    }

    private <T extends DockerEmRole.AbstractBuilder> T configureClamps(T builder) {
        builder
        .configProperty("introscope.enterprisemanager.query.datapointlimit", "1000000")
        .configProperty("introscope.enterprisemanager.query.returneddatapointlimit", "100000")
        .configProperty("transport.outgoingMessageQueueSize", "10000")
        .configProperty("transport.override.isengard.high.concurrency.pool.min.size", "15")
        .configProperty("transport.override.isengard.high.concurrency.pool.max.size", "15");

        return builder;
    }

    private LinuxBuilder configureClamps(LinuxBuilder emBuilder) {
        emBuilder
        .configProperty("introscope.enterprisemanager.query.datapointlimit", "1000000")
        .configProperty("introscope.enterprisemanager.query.returneddatapointlimit", "100000")
        .configProperty("transport.outgoingMessageQueueSize", "10000")
        .configProperty("transport.override.isengard.high.concurrency.pool.min.size", "15")
        .configProperty("transport.override.isengard.high.concurrency.pool.max.size", "15");
        
        return emBuilder;
    }
}
