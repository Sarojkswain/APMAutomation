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

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.systemtest.fld.artifact.ManagementModulesArtifact;
import com.ca.apm.systemtest.fld.role.DelayRole;
import com.ca.apm.systemtest.fld.role.LogMonitorRole;
import com.ca.apm.systemtest.fld.testbed.loads.*;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.apm.systemtest.fld.testbed.regional.LogMonitorConfigurationSource;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.EmRole.LinuxBuilder;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.testbed.*;
import com.ca.tas.tests.annotations.TestBedDefinition;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Small FLD Main cluster testbed.
 * @author filja01
 *
 */
@TestBedDefinition()
public class SmallMainFLDClusterTestbed implements ITestbedFactory, FLDConstants {
    private static final Logger logger = LoggerFactory.getLogger(SmallMainFLDClusterTestbed.class);

    private static FLDConfiguration fldConfig = FLDConfigurationService.getConfig();

    public static final String MMODULE_VERSION = "99.99.aquarius-SNAPSHOT";//"10.2.0.9";//Version.SNAPSHOT_SYS_99_99.toString();
    
    public static final String INSTALL_DIR_TAS = "/opt/automation/deployed/em";
    public static final String INSTALL_DIR = "/home/sw/em/Introscope";
    public static final String INSTALL_TG_DIR = "/home/sw/em/Installer";
    public static final String DATABASE_DIR = "/data/em/database";
    public static final String GC_LOG_FILE_TAS = INSTALL_DIR_TAS + "/logs/gclog.txt";
    public static final String GC_LOG_FILE = INSTALL_DIR + "/logs/gclog.txt";
    private static final String GC_LOG_PARAM_TAS = GC_LOG_FILE_TAS;
    private static final String GC_LOG_PARAM = GC_LOG_FILE;
    public static final String INSTALL_TIM_DIR = "/opt";
    
    public static final String DB_PASSWORD = "wily";
    public static final String DB_USERNAME = "admin";
    public static final String DB_ADMIN_USERNAME = "postgres";
    public static final String DB_ADMIN_PASSWORD = "C@wilyapm90";

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
        "-XX:+HeapDumpOnOutOfMemoryError", "-Xms3072m", "-Xmx3072m",
        "-verbose:gc", "-Xloggc:"+GC_LOG_PARAM_TAS);
    
    private static final Collection<String> COLL_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true","-XX:MaxPermSize=256m" , "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
        "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC",
        //"-XX:+UseG1GC",
        //"-XX:G1ReservePercent=20", "-XX:InitiatingHeapOccupancyPercent=35",
        "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", 
        "-XX:+HeapDumpOnOutOfMemoryError", "-Xms3072m", "-Xmx3072m",
        "-verbose:gc", "-Xloggc:"+GC_LOG_PARAM_TAS);
    
    private static final Collection<String> WV_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-Dorg.owasp.esapi.resources=./config/esapi", "-Dsun.java2d.noddraw=true",
        "-Dorg.osgi.framework.bootdelegation=org.apache.xpath", "-javaagent:./product/webview/agent/wily/Agent.jar",
        "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
        "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms2048m", "-Xmx3072m",
        //"-XX:+UseG1GC",
        //"-XX:G1ReservePercent=20", "-XX:InitiatingHeapOccupancyPercent=35",
        "-XX:+PrintGCDateStamps", "-XX:+HeapDumpOnOutOfMemoryError",
        "-verbose:gc", "-Xloggc:"+GC_LOG_PARAM_TAS);
    
    private static final String DAILYSTATS_JAVA_OPTION = "-XX:+CMSClassUnloadingEnabled -XX:+UseConcMarkSweepGC"
        + " -XX:+UseBiasedLocking -XX:SurvivorRatio=8 -XX:TargetSurvivorRatio=90 -Dlog4j.configuration=log4j.properties"
        + " -Xms256M -Xss512k -Xms1024m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError";
    
    public static final String PID_FILE_KEY = "pidFile";
    
    
    @Override
    public ITestbed create(ITasResolver tasResolver) {
        
        Testbed testbed = new Testbed("FLDMainClusterTestbed");

        //ITasArtifact domainConfigArtifact = new FldDomainConfigArtifact().createArtifact(fldConfig.getDomainConfigVersion());
        
        ITestbedMachine databaseMachine;
        databaseMachine = createDbMachine(tasResolver);
        
        //import domainconfig
        /*IRole domainConfigImportLinuxRole
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
        */
        testbed.addMachine(databaseMachine);
        
        //Collectors machines
        List<IRole> collectorRoles = new ArrayList<>();
        String tmplId = "co65";
        for (int i = 0; i < EM_COLL_ROLES.length; i++) {
            ITestbedMachine collMachine;
            if (i == 3) {
                tmplId = "co66";
            }
            collMachine = createCollectorMachine(tasResolver, databaseMachine, collectorRoles, i, tmplId);
            testbed.addMachine(collMachine);
        }

        //MOM machine
        ITestbedMachine momMachine;
        logger.info("Creating MoM with silent installer");
        momMachine = createMomMachine(tasResolver, databaseMachine, collectorRoles);
        
        testbed.addMachine(momMachine);


        ITestbedMachine webviewMachine;
        webviewMachine = createWebViewMachine(tasResolver, databaseMachine, momMachine);
        
        testbed.addMachine(webviewMachine);

        //start EM
        //startEM(tasResolver, testbed, MOM_MACHINE_ID, WEBVIEW_MACHINE_ID, EM_MOM_ROLE_ID, EM_WEBVIEW_ROLE_ID, true);
        
        List<FldTestbedProvider> testbedProviders = new ArrayList<>();
        testbedProviders.add(new FldControllerLoadProvider(fldConfig));
        testbedProviders.add(new SmallFldLoadWurlitzerProvider());
        List<String> ids = new ArrayList<>(Arrays.asList(MEMORY_MONITOR_MAIN_CLUSTER_MACHINE_IDS));
        testbedProviders.add(new TimeSynchronizationTestbedProvider(ids.toArray(new String[ids.size()])));
        
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

    @NotNull
    private ITestbedMachine createMomMachine(ITasResolver tasResolver, ITestbedMachine databaseMachine, List<IRole> collectorRoles) {
        ITestbedMachine momMachine =
                new TestbedMachine.LinuxBuilder(MOM_MACHINE_ID)
                        .templateId("fldcoll13").bitness(Bitness.b64)
                        .build();
        LinuxBuilder momBuilder = new LinuxBuilder(EM_MOM_ROLE_ID, tasResolver);

        momBuilder
        .silentInstallChosenFeatures(Arrays.asList("Enterprise Manager","ProbeBuilder","EPA"))
        .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
        .nostartEM()
        .nostartWV()
        .dbpassword(DB_PASSWORD)
        .dbAdminPassword(DB_ADMIN_PASSWORD)
        .dbuser(DB_USERNAME)
        .dbhost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID))
        .version(fldConfig.getEmVersion())
        .emPort(EM_PORT)
        .emWebPort(EMWEBPORT)
        .installDir(INSTALL_DIR)
        .installerTgDir(INSTALL_TG_DIR)
        .emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION);

        for (IRole collectorRole : collectorRoles) {
            momBuilder.emCollector((EmRole) collectorRole);
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
        IRole loggingRole = addLoggingSetupRole(momMachine, setRealms, tasResolver, 200, INSTALL_DIR);

        //start MOM
        addStartEmRole(momMachine, momRole, false, true, loggingRole);
        return momMachine;
    }

    @NotNull
    private ITestbedMachine createWebViewMachine(ITasResolver tasResolver, ITestbedMachine databaseMachine, ITestbedMachine momMachine) {
        //WebView machine
        ITestbedMachine webviewMachine =
                new TestbedMachine.LinuxBuilder(WEBVIEW_MACHINE_ID)
                        .templateId("co65")
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
                .installDir(INSTALL_DIR_TAS)
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
                .configurationMap(INSTALL_DIR_TAS+"/product/webview/agent/wily/core/config/IntroscopeAgent.profile",
                    propsMap)
                .build();

        UniversalRole setWVAgent =
            new UniversalRole.Builder(EM_WEBVIEW_ROLE_ID + "_setupWVAgentProfile", tasResolver).runFlow(ConfigureFlow.class, ctx)
                .build();
        setWVAgent.after(webviewRole);
        webviewMachine.addRole(setWVAgent);

        //start Webview
        addStartEmRole(webviewMachine, webviewRole, true, false, setWVAgent);
        return webviewMachine;
    }

    @NotNull
    private ITestbedMachine createCollectorMachine(ITasResolver tasResolver, ITestbedMachine databaseMachine, 
                                                   List<IRole> collectorRoles, int i, String tmplId) {
        String collMachineName = COLL_MACHINES[i];
        ITestbedMachine collectorMachine =
                new TestbedMachine.LinuxBuilder(collMachineName)
                            .templateId(tmplId).bitness(Bitness.b64)
                            .build();
        LinuxBuilder collBuilder = new LinuxBuilder(EM_COLL_ROLES[i], tasResolver);
        collBuilder
        .silentInstallChosenFeatures(
            Arrays.asList("Enterprise Manager","ProbeBuilder","EPA"))
        .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
        .nostartEM()
        .nostartWV()
        .dbpassword(DB_PASSWORD)
        .dbAdminPassword(DB_ADMIN_PASSWORD)
        .dbuser(DB_USERNAME)
        .dbhost(tasResolver.getHostnameById(EM_DATABASE_ROLE_ID))
        .installDir(INSTALL_DIR_TAS)
        .version(fldConfig.getEmVersion());

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
        IRole loggingRole = addLoggingSetupRole(collectorMachine, collectorRole, tasResolver, 200, INSTALL_DIR_TAS);

        //tess-default.properties settings role on Coll02
        if (collMachineName.equals(COLL02_MACHINE_ID)) { //collector 2
            Map<String, String> propsMap = new HashMap<>();

            propsMap.put("dailystats.jvmArgs",DAILYSTATS_JAVA_OPTION);
            propsMap.put("dailystats.aggregateInSeparateJvm", "true");

            ConfigureFlowContext ctx =
                new ConfigureFlowContext.Builder().configurationMap(INSTALL_DIR_TAS+"/config/tess-default.properties", propsMap)
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
    private ITestbedMachine createDbMachine(ITasResolver tasResolver) {
        //database machine
        ITestbedMachine databaseMachine =
                new TestbedMachine.LinuxBuilder(DATABASE_MACHINE_ID)
                        .templateId("co66")
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
                .build();
        databaseMachine.addRole(databaseRole);
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
          
        if (startWV) {
            builder.asyncCommand(emRole.getWvRunCommandFlowContext());
        } 
        if (startEM) {
            builder.asyncCommand(emRole.getEmRunCommandFlowContext());
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
        String[] emailArray, LogMonitorConfigurationSource logMonitorConfigurationSource, String instDir) {
        LogMonitorRole logMonitorLinuxRole
            = configureLogMonitorBuilder(
                new LogMonitorRole.LinuxBuilder(
                    machine.getMachineId() + "_" + LOG_MONITOR_LINUX_ROLE_ID, tasResolver),
                logMonitorConfigurationSource)
            .emails(Arrays.asList(emailArray))
            .vars(Collections.singletonMap("currentLogDir", instDir + "/logs"))
            .build();
        logMonitorLinuxRole.addProperty(PID_FILE_KEY,
            logMonitorLinuxRole.getDeployLogMonitorFlowContext().getPidFile());
        //logMonitorLinuxRole.after(after);
        machine.addRole(logMonitorLinuxRole);
    }
    
    static IRole addLoggingSetupRole(ITestbedMachine machine, IRole afterRole, 
                                          ITasResolver tasResolver, Integer backupIndex, String instDir) {
            
        Map<String, String> propsMap = new HashMap<>();
        
        if (backupIndex == null) backupIndex = 4; //set default value
        
        propsMap.put("log4j.appender.logfile.MaxBackupIndex", backupIndex.toString());
        propsMap.put("log4j.logger.Manager", "VERBOSE#com.wily.util.feedback.Log4JSeverityLevel, console, logfile");

        ConfigureFlowContext ctx =
            new ConfigureFlowContext.Builder().configurationMap(instDir+"/config/IntroscopeEnterpriseManager.properties", propsMap)
                .build();

        UniversalRole configureLogSetup =
            new UniversalRole.Builder("configLogSetup_"+machine.getMachineId(), tasResolver).runFlow(ConfigureFlow.class, ctx)
                .build();
        configureLogSetup.after(afterRole);
        machine.addRole(configureLogSetup);
        
        return configureLogSetup;
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
