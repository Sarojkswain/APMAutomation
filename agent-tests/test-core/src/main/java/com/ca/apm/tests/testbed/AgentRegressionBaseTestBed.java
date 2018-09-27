/*
 * Copyright (c) 2015 CA.  All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.tests.testbed;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.testng.RunTestNgFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.tests.flow.CreateAccPackageFlow;
import com.ca.apm.tests.flow.CreateAccPackageFlowContext;
import com.ca.apm.tests.flow.CreateAccPackageFlowContext.OsName;
import com.ca.apm.tests.flow.CreateAccPackageFlowContext.Process;
import com.ca.apm.tests.role.AccAgentSetupRole;
import com.ca.apm.tests.role.EnableCemApiRole;
import com.ca.apm.tests.role.FetchEMDataRole;
import com.ca.apm.tests.role.FileUpdateRole;
import com.ca.apm.tests.role.MySqlWinRole;
import com.ca.apm.tests.role.SetupEMPostgresWinRole;
import com.ca.apm.tests.role.StartEMRole;
import com.ca.apm.tests.testbed.jvm8.acc.onprem.AccServerRoleHelper;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.WebLogicVersion;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.acc.ACCConfigurationServerRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.role.webapp.WebLogicRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Java Agent Automation 
 *
 * @author pojja01@ca.com, kurma05@ca.com, ahmal01@ca.com
 */
@TestBedDefinition
public class AgentRegressionBaseTestBed implements ITestbedFactory {
   
    public static final String MACHINE_1                        = "machine1";
    public static final String MACHINE_2                        = "machine2";
    public static final String MACHINE_3                        = "machine3";
    public static final String MACHINE_4                        = "machine4";
    public static final String MACHINE_5                        = "machine5";
    public static final String EM_ROLE_ID                       = "em01";
    private static final String QC_UPLOAD_ROLE_ID               = "qcuploadtool01";
    private static final String PERL_ROLE_ID                    = "perlRole";
    private static final String CYGWIN_INSTALL_ROLE_ID          = "cygwinInstallRole";
    private static final String CYGWIN_SSH_ROLE_ID              = "cygwinSSHRole";
    private static final String CYGWIN_EXE_ROLE_ID              = "cygwinExeRole";
    private static final String PERFMON_REBUILD_ROLE_ID         = "perfmonRebuildRole";
    protected static final String DEFAULT_AGENT_ROLE_ID         = "default_agent01"; 
    protected static final String EPAGENT_ROLE_ID               = "epagent01";
    protected static final String EPAGENT_TESTAPP_ROLE_ID       = "epagent_testapp";
    protected static final String DEFAULT_JDK_EXE_PATH          = TasBuilder.WIN_JDK_1_8 + "\\bin\\java.exe";
    protected static final String DEPLOY_BASE                   = TasBuilder.WIN_SOFTWARE_LOC;
    protected static final String DEPLOY_LINUX_BASE             = TasBuilder.LINUX_SOFTWARE_LOC;
    private static final String CYGWIN_INSTALL_HOME             = "C:\\cygwin";
    protected static final String WLS12C_INSTALL_HOME           = DEPLOY_BASE + "Oracle/Middleware12.1.3";
    public String defaultAgentTemplateId                        = TEMPLATE_W64;
    public static final String TEMPLATE_W64_JASS                = "jass";
    protected boolean useCodaControllerAsAgentMachine           = true;
    protected boolean isJassEnabled                             = false;
    protected boolean isLegacyMode                              = false;
    protected boolean isBackupEMData                            = false;
    protected boolean isNoRedefEnabled                          = false;
    protected static final String QATESTAPP_CONTEXT             = "QATestApp";
    protected static final String JAVA7NEWAPP_CONTEXT           = "Java7NewApp";
    protected static final String JAVA7NEWAPP_ARTIFACTID        = "qatestapp-jvm7";
    protected static final String AXIS2TESTAPP                  = "Axis2TestApp";
    protected static final String STRUTS2TESTAPP                  = "Struts2TestApp";
    protected static final String SPRINGTESTAPP                 = "spring-mvc-testapp";
    public static String agentVersion                           = "";
    public static final String RESULTS_DIR                      = "testlogs";
    private JavaRole qcuploadtoolJdk                            = null;
    private static final String QC_RESULTS_DIR                  = DEPLOY_BASE + "qcuploadtool/resources/qctestresults";
	public final static String EMAIL_RECIPIENTS_DEFAULT         = "marina.kur@ca.com";
    public final static String EMAIL_RECIPIENTS_JASS            = EMAIL_RECIPIENTS_DEFAULT;
    public final static String EMAIL_RECIPIENTS_DEV             = EMAIL_RECIPIENTS_DEFAULT;
    public final static String EMAIL_RECIPIENTS_TAURUS          = "Anand.Krishnamurthy@ca.com";
    public final static String EMAIL_RECIPIENTS_ARIES           = "Swetha.Bhamidipati@ca.com";
    public final static String EMAIL_RECIPIENTS_SERPENS         = "Abhijit.Bhadra@ca.com";
    public final static String EMAIL_RECIPIENTS_CUSTOMERSUCCESS = "Ting.Zhu@ca.com Cornel.Lee@ca.com Anil.KondichettyJoel@ca.com";
    public final static String EMAIL_RECIPIENTS_RELEASE         = EMAIL_RECIPIENTS_DEV + " " + EMAIL_RECIPIENTS_CUSTOMERSUCCESS;
    protected String emailRecipients                            = EMAIL_RECIPIENTS_DEFAULT;
    protected final static String EMAIL_SENDER                  = "Team-APM-Agent@ca.com";
    protected final static String SMTP_SERVER                   = "mail.ca.com";
    protected static boolean isLinuxTestbed                     = false;
    public static final String ACC_PACKAGES_PATH                = "http://jass5:9091/acc/";
    public static final String ACC_DEFAULT_WIN_AGENT_URL        = ACC_PACKAGES_PATH + "Java.zip";
    public static final String ACC_WEBLOGIC_WIN_AGENT_URL       = ACC_PACKAGES_PATH + "WebLogic_Spring.zip";
    public static final String ACC_WEBSPHERE_WIN_AGENT_URL      = ACC_PACKAGES_PATH + "WebSphere_Spring.zip";
    public static final String ACC_JBOSS_WIN_AGENT_URL          = ACC_PACKAGES_PATH + "JBoss_Spring.zip";
    public static final String ACC_TOMCAT_WIN_AGENT_URL         = ACC_PACKAGES_PATH + "Tomcat_Spring.zip";
    protected boolean isAccAgentBundle                          = false;
    protected static final String ACC_SERVER_ROLE_ID            = "accServerRole1";
    protected static final String ACC_CREATE_PACKAGE_ROLE_ID    = "accCreatePackageRole";
    
    public ITestbed create(ITasResolver tasResolver) {         
        
        ITestbed testBed = new Testbed(getTestBedName());
        return testBed;     
    }
   
    protected void addPerlRole(ITasResolver tasResolver, TestbedMachine machine) {
        
        String installerDestination = DEPLOY_BASE + "perl" + TasBuilder.WIN_SEPARATOR + "installer.msi";
        RunCommandFlowContext installPerlCommand = new RunCommandFlowContext.Builder("msiexec")
                .workDir("C:\\Windows\\system32")
                .args(Arrays.asList("/i", installerDestination, "TARGETDIR=\"c:\"", "PERL_PATH=\"Yes\"", "PERL_EXT=\"Yes\"", "/q"))
                .build();

        GenericRole perlRole = new GenericRole.Builder(machine.getMachineId() + "_" + PERL_ROLE_ID, tasResolver)
                .download(new DefaultArtifact("com.ca.apm.binaries", "active-perl", "x86-64", "msi", "5.20.2.2002"), installerDestination)
                .runCommand(installPerlCommand)
                .build();
        machine.addRole(perlRole);
    }
    
    protected void addPerfmonRebuildRole(ITasResolver tasResolver, ITestbedMachine machine) {
        
        //applicable to windows vms - sometimes perfmon counters are 
        //missing when vm gets cloned; have to rebuid them
        RunCommandFlowContext context = new RunCommandFlowContext.Builder("lodctr")
            .workDir("C:\\Windows\\system32")
            .args(Arrays.asList("/r"))
            .build();
        
        ExecutionRole role =
            new ExecutionRole.Builder(machine.getMachineId() + "_" + PERFMON_REBUILD_ROLE_ID)
            .flow(RunCommandFlow.class, context)
            .build();
        
        machine.addRole(role);
    }
 
    protected void addCygwinRole(ITasResolver tasResolver, ITestbedMachine machine) {
        
        GenericRole cygwinExeRole = getCygwinExeRole(tasResolver, machine);
        GenericRole cygwinInstallRole = getCygwinInstallRole(tasResolver, machine);
        ExecutionRole cygwinSSHRole = getCygwinSSHRole(tasResolver, machine);
        
        cygwinExeRole.before(cygwinInstallRole);
        cygwinInstallRole.before(cygwinSSHRole);
        machine.addRole(cygwinExeRole);
        machine.addRole(cygwinInstallRole);
        machine.addRole(cygwinSSHRole);
    }
    
    /**    
     * defaultAagent -- check if LegacyMode, create defaultAgent Role, and add the role to the machine 
     * @author hsiwa01@ca.com
     * Date: 03-02-16
     */     
    protected void addDefaultAgentRole(ITasResolver tasResolver, TestbedMachine machine) {
        
        String artifact = "agent-noinstaller-default-windows";
        if(isLegacyMode) {
            artifact = "agent-legacy-noinstaller-default-windows";
        }

        DefaultArtifact agentArtifact = 
            new DefaultArtifact("com.ca.apm.delivery", artifact, "zip", getAgentArtifactVersion(tasResolver));

        //set agent version
        setAgentVersion(artifact, tasResolver.getArtifactUrl(agentArtifact));
       
        //get agent
        GenericRole defaultAgentRole = new GenericRole.Builder(DEFAULT_AGENT_ROLE_ID, tasResolver)
            .unpack(agentArtifact, codifyPath(DEPLOY_BASE + "/default"))
            .build();   
        machine.addRole(defaultAgentRole);
    }
    
    protected void addDefaultAccAgentRole(ITasResolver tasResolver, TestbedMachine machine) {
     
        AccAgentSetupRole defaultAgentRole =
            new AccAgentSetupRole.Builder(DEFAULT_AGENT_ROLE_ID, tasResolver)
            .installDir(codifyPath(DEPLOY_BASE + "/default"))
            .shouldSetup(false)
           // .url(ACC_DEFAULT_WIN_AGENT_URL)
            .packageName("Java")
            .osName("windows")
            .build();
        machine.addRole(defaultAgentRole);   
    }
    
    protected void setAgentVersion(String artifact,
                                   URL url) {
        
        String extension = "\\.zip";
        if(isLinuxTestbed)
            extension = "\\.tar";
        
        String pattern = "(.*" + artifact + "-)(.*)"+extension;        
        
        Matcher match = Pattern.compile(pattern).matcher(url.getFile());
        
        if (match.find()) {
            agentVersion = match.group(2);
        }  
    }
    
    protected String getAccAgentVersion() {
        
        return "ACC_" + new SimpleDateFormat("yyyy.MM.dd").format(new Date());        
    }
    
    /**
     * epagentAagent -- create epagent Role with testApp, and add the role to the machine
     * @author hsiwa01@ca.com
     * Date: 03-14-16
     */
    @NotNull
    protected void addEPAgentRole(ITasResolver tasResolver, TestbedMachine machine) {
        //epagent has no legacy mode, so no need to check
        String artifact_epa = "epagent-package";
        String artifact_testapp = "epatestapp";

        // epagent
        DefaultArtifact epagentArtifact = null;
        if(isLinuxTestbed) {
            epagentArtifact = new DefaultArtifact("com.ca.apm.delivery", artifact_epa,
                "jsw-unix","tar", getAgentArtifactVersion(tasResolver));
        }
        else {
            epagentArtifact = new DefaultArtifact("com.ca.apm.delivery", artifact_epa,
                "jsw-win","zip", getAgentArtifactVersion(tasResolver));
        }

        //epagent_testapp
        DefaultArtifact agentArtifact_testapp =
                new DefaultArtifact("com.ca.apm.coda-projects.test-tools", artifact_testapp,"dist","zip", tasResolver.getDefaultVersion());

        //set agent version
        setAgentVersion(artifact_epa, tasResolver.getArtifactUrl(epagentArtifact));

        //get agent
        GenericRole epAgentRole = new GenericRole.Builder(EPAGENT_ROLE_ID, tasResolver)
            .unpack(epagentArtifact, codifyPath(DEPLOY_BASE + "/epagent"))
            .build();

        GenericRole epAgentRole_Testapp = new GenericRole.Builder(EPAGENT_TESTAPP_ROLE_ID, tasResolver)
            .unpack(agentArtifact_testapp, codifyPath(DEPLOY_BASE + "/epagent"))
            .build();

        epAgentRole.before(epAgentRole_Testapp);

        machine.addRole(epAgentRole);
        machine.addRole(epAgentRole_Testapp);
    }

    protected GenericRole getCygwinExeRole(ITasResolver tasResolver, final ITestbedMachine machine) {
        
        GenericRole cygwinInstallRole = new GenericRole.Builder(machine.getMachineId() + "_" + CYGWIN_EXE_ROLE_ID, tasResolver)
            .unpack(new DefaultArtifact("com.ca.apm.libs", "cygwin", "x64", "zip", "2.871"), CYGWIN_INSTALL_HOME)
            .build();  
        
        return cygwinInstallRole;
    }

    protected GenericRole getCygwinInstallRole(ITasResolver tasResolver, final ITestbedMachine machine) {
        
        RunCommandFlowContext installCygwinCommand = new RunCommandFlowContext.Builder("install_cygwin.bat")
            .workDir(CYGWIN_INSTALL_HOME)
            .args(Arrays.asList(CYGWIN_INSTALL_HOME))
            .build();
        
        DefaultArtifact artifact = new DefaultArtifact("com.ca.apm.tests",
            "agent-tests-core",
            "dist_cygwin",
            "zip",
            tasResolver.getDefaultVersion());
        
        GenericRole cygwinInstallRole = new GenericRole.Builder(machine.getMachineId() + "_" + CYGWIN_INSTALL_ROLE_ID, tasResolver)
            .unpack(artifact, CYGWIN_INSTALL_HOME)
            .runCommand(installCygwinCommand)
            .build();  
        
        return cygwinInstallRole;
    }
    
    protected ExecutionRole getCygwinSSHRole(ITasResolver tasResolver, ITestbedMachine machine) {
        
        ArrayList<String> args = new ArrayList<String>();
        args.add(CYGWIN_INSTALL_HOME);
        args.add(machine.getLocalSSHUserName());
        args.add(machine.getLocalSSHPassword());
        
        RunCommandFlowContext context = new RunCommandFlowContext.Builder("install_ssh_service.bat")
            .args(args)
            .workDir(CYGWIN_INSTALL_HOME)
            .build();
        
        ExecutionRole cygwinSSHRole =
            new ExecutionRole.Builder(machine.getMachineId() + "_" + CYGWIN_SSH_ROLE_ID)
            .flow(RunCommandFlow.class, context)
            .build();
        
        return cygwinSSHRole;
    }

    
    @NotNull
    protected GenericRole getwls12cRole(ITasResolver tasResolver, String wlsRole) {

        ArrayList<String> args = new ArrayList<String>();
        args.add("-silent");
        
        RunCommandFlowContext installWlc12cCommand = new RunCommandFlowContext.Builder("configure.cmd")
        .workDir(WLS12C_INSTALL_HOME)
        .args(args)
        .build();
        
        GenericRole wls12cInstallRole = new GenericRole.Builder(wlsRole, tasResolver)
            .unpack(new DefaultArtifact("com.ca.apm.binaries", "weblogic", "dev", "zip", "12.1.3"),  codifyPath(WLS12C_INSTALL_HOME))
            .runCommand(installWlc12cCommand)
            .build();  
        
        return wls12cInstallRole;
    }
    
    @NotNull
    protected WebLogicRole getwls103Role(ITasResolver tasResolver, String wlsRoleId) {

        String baseDir = DEPLOY_BASE + "Oracle";
        
        WebLogicRole webLogicRole = new WebLogicRole.Builder(wlsRoleId, tasResolver)
            .installLocation(baseDir + "/Middleware10.3")
            .genericJavaInstaller()
            .installLogFile(baseDir + "/install.log")
            .webLogicInstallerDir(baseDir + "/sources")
            .version(WebLogicVersion.v1035generic)
            .responseFileDir(baseDir + "/responseFiles")
            .installDir(baseDir + "/Middleware10.3/wlserver")  
            .build();
        
        return webLogicRole;
    }
     
    @NotNull
    protected void addQCUploadRole(ITasResolver tasResolver, TestbedMachine machine) {
        
        qcuploadtoolJdk = new JavaRole.Builder("qcuploadtoolJdk", tasResolver)
                .version(JavaBinary.WINDOWS_32BIT_JDK_16).build();
        
        DefaultArtifact artifact =
                new DefaultArtifact("com.ca.apm.coda-projects.test-tools", "qcuploadtool",
                    "dist","zip", tasResolver.getDefaultVersion());
        GenericRole qcUploadToolRole = new GenericRole.Builder(QC_UPLOAD_ROLE_ID, tasResolver)
            .unpack(artifact, codifyPath(DEPLOY_BASE + "/qcuploadtool"))
            .build();
        
        //remove this line once .net agent converted to tas w/o coda
        qcUploadToolRole.addProperty("qcuploadtool.java.home", codifyPath(qcuploadtoolJdk.getInstallDir()));

        machine.addRole(qcuploadtoolJdk, qcUploadToolRole);
    }
    
    @NotNull
    protected void addEmRole(ITasResolver tasResolver, 
                             TestbedMachine machine) {
        
        //create em role
        EmRole emRole = getEmRole(tasResolver, machine,
            new EmRole.Builder(EM_ROLE_ID, tasResolver), 
            TasBuilder.WIN_JDK_1_7_51 + "/bin/java.exe", JassTestBedUtil.shouldInstallDbWithEM) ;
            
        //enable cem api & start em/webview
        EnableCemApiRole enableCemApiRole = new EnableCemApiRole.Builder(JassTestBedUtil.ENABLE_CEM_API_ROLE_ID, tasResolver)
            .emHomeDir(codifyPath(emRole.getDeployEmFlowContext().getInstallDir()))
            .unpackDir(TasBuilder.WIN_SOFTWARE_LOC + "/cemtemp")
            .emInstallVersion(getEMArtifactVersion(tasResolver)) 
            .build();
        
        StartEMRole startEMRole = new StartEMRole.Builder(JassTestBedUtil.START_EM_ROLE_ID, tasResolver)
            .emHomeDir(codifyPath(emRole.getDeployEmFlowContext().getInstallDir()))
            .build();
        
        if(!JassTestBedUtil.shouldInstallDbWithEM) {
            SetupEMPostgresWinRole postgresRole = new SetupEMPostgresWinRole.Builder("SetupEMPostgresWinRole", tasResolver)
                .emInstallDir(emRole.getDeployEmFlowContext().getInstallDir())
                .emInstallVersion(getEMArtifactVersion(tasResolver))
                .build();
            emRole.before(postgresRole);
            startEMRole.after(postgresRole);
            machine.addRole(postgresRole);
        }
        
        //add roles
        emRole.before(enableCemApiRole, startEMRole);
        startEMRole.after(enableCemApiRole);
        machine.addRole(JassTestBedUtil.getUpdatePermissionRole(emRole, machine.getMachineId()));
        machine.addRole(emRole, enableCemApiRole, startEMRole);
        
        if (isBackupEMData) {
            FetchEMDataRole fetchEMDataRole = new FetchEMDataRole("fetchEMData", codifyPath(emRole.getDeployEmFlowContext().getInstallDir()));
            machine.addRole(fetchEMDataRole);
        }
    }
        
    @NotNull
    protected void addEmLinuxRole(ITasResolver tasResolver, 
                                  TestbedMachine machine) {
  
        //create em role
        EmRole emRole = getEmRole(tasResolver, machine,
            new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver), 
            TasBuilder.LINUX_JDK_1_7  + "/bin/java.exe", true) ;
            
        //enable cem api & start em/webview
        EnableCemApiRole enableCemApiRole = new EnableCemApiRole.LinuxBuilder(JassTestBedUtil.ENABLE_CEM_API_ROLE_ID, tasResolver)
            .emHomeDir(emRole.getDeployEmFlowContext().getInstallDir())
            .unpackDir(TasBuilder.LINUX_SOFTWARE_LOC + "/cemtemp")
            .emInstallVersion(getEMArtifactVersion(tasResolver)) 
            .build();
        StartEMRole startEMRole = new StartEMRole.LinuxBuilder(JassTestBedUtil.START_EM_ROLE_ID, tasResolver)
            .emHomeDir(emRole.getDeployEmFlowContext().getInstallDir())
            .build();
        
        //add roles
        emRole.before(enableCemApiRole, startEMRole);
        enableCemApiRole.before(startEMRole);
        machine.addRole(emRole, enableCemApiRole, startEMRole);
        
        if (isBackupEMData) {
            FetchEMDataRole fetchEMDataRole = new FetchEMDataRole("fetchEMData", emRole.getDeployEmFlowContext().getInstallDir());
            machine.addRole(fetchEMDataRole);
        }
    }

    private EmRole getEmRole(ITasResolver tasResolver, 
                             TestbedMachine machine,
                             EmRole.Builder builder, 
                             String jdkPath,
                             boolean installDb) {
        
        Collection<String> emFeatures = new ArrayList<String>();
        emFeatures.add("Enterprise Manager");
        emFeatures.add("ProbeBuilder");
        emFeatures.add("WebView");
        
        EmRole.Builder emBuilder =
            builder
                .version(getEMArtifactVersion(tasResolver))
                .configProperty("introscope.changeDetector.disable", "false")
                .configProperty("introscope.enterprisemanager.performance.compressed", "false")
                .configProperty("log4j.logger.Manager", "DEBUG, console,logfile")
                .configProperty("log4j.logger.Manager.Performance", "DEBUG, performance, logfile")
                .configProperty("transport.buffer.input.maxNum", "1500")
                .configProperty("transport.buffer.input.maxNumNio", "6000")
                .configProperty("introscope.enterprisemanager.threaddump.storage.max.disk.usage", "50")
                .configProperty("enable.default.BusinessTransaction", "false")
                .nostartEM()
                .nostartWV();        
        if(isAccAgentBundle) {
            builder.configProperty("introscope.apmserver.teamcenter.saas", "true");
        }
        
        if(installDb){           
            emFeatures.add("Database");            
        }
        else {
            builder.dbuser("postgres").dbpassword("Lister@123");         
        }
      
        builder.silentInstallChosenFeatures(emFeatures);
        EmRole emRole = emBuilder.build();
      
        emRole.addProperty("min.heap.mb", "1024");
        emRole.addProperty("max.heap.mb", "2048");
        emRole.addProperty("max.permsize.mb", "512");
        emRole.addProperty("em.port", emRole.getEnvPropertyById("emPort"));
        emRole.addProperty("em.loc", codifyPath(emRole.getDeployEmFlowContext().getInstallDir()));
        emRole.addProperty("em.java.exe.path", codifyPath(jdkPath));
        emRole.addProperty("em.host.name", tasResolver.getHostnameById(EM_ROLE_ID));

        return emRole;
    }
    
    @NotNull
    protected UniversalRole tomcatUnixAgentRole(ITasResolver tasResolver,
            TestbedMachine machine, TomcatRole tomcat, String javaHome,
            String tomcatVersion) {

        String artifact = "agent-noinstaller-tomcat-unix";
        if (isLegacyMode)
            artifact = "agent-legacy-noinstaller-tomcat-unix";

        DefaultArtifact agentArtifact = new DefaultArtifact(
                "com.ca.apm.delivery", artifact, "tar",
                getAgentArtifactVersion(tasResolver));
        DefaultArtifact startupScriptArtifact = new DefaultArtifact(
                "com.ca.apm.binaries.tomcat", "tomcat.startup.scripts", "",
                "zip", tomcatVersion);

        UniversalRole deployTomcatAgentRole = new UniversalRole.Builder(
                "download", tasResolver)
                .unpack(startupScriptArtifact, tomcat.getInstallDir() + "/bin")
                .unpack(agentArtifact, tomcat.getInstallDir()).build();
        

        Map<String, String> replaceJava = new HashMap<String, String>();
        Map<String, String> replacePairs = new HashMap<String, String>();

        replaceJava
                .put("\\[JAVA_HOME\\]", codifyPath(javaHome));
        replaceJava.put("\\[JRE_HOME\\]", codifyPath(javaHome));
        replacePairs.put("\\[PERM.SPACE.SIZE\\]", "256");
        replacePairs.put("\\[MAX.PERM.SPACE\\.SIZE\\]", "512");
        replacePairs.put("\\[MAX.HEAP.SIZE\\]", "512");
        replacePairs.put("\\[MIN.HEAP.SIZE\\]", "256");
        replacePairs.put("\\[AGENT.JAVA.OPTIONS\\]",
                "-javaagent\\:" + codifyPath(tomcat.getInstallDir())
                        + "/wily/Agent.jar"
                        + " -Dcom.wily.introscope.agentProfile="
                        + codifyPath(tomcat.getInstallDir())
                        + "/wily/core/config/IntroscopeAgent.profile");
        String fileName1 = tomcat.getInstallDir() + "/bin/setclasspath.sh";
        String fileName2 = tomcat.getInstallDir() + "/bin/catalina.sh";

        FileUpdateRole fileUpdateRole1 = new FileUpdateRole.Builder(
                "fileupdate1", tasResolver).filePath(fileName1)
                .replacePairs(replaceJava).build();
        FileUpdateRole fileUpdateRole2 = new FileUpdateRole.Builder(
                "fileupdate2", tasResolver).filePath(fileName2)
                .replacePairs(replacePairs).build();

        fileUpdateRole1.after(deployTomcatAgentRole);
        fileUpdateRole2.after(deployTomcatAgentRole);
                
        setAgentVersion(artifact, tasResolver.getArtifactUrl(agentArtifact));
        machine.addRole(fileUpdateRole1, fileUpdateRole2);
        return deployTomcatAgentRole;  
    }
    
    @NotNull
	protected void tomcatWinAgentRole(ITasResolver tasResolver,
			TestbedMachine machine, TomcatRole tomcat, String javaHome,
			String tomcatVersion) {

        //create agent role
        if(isAccAgentBundle) {
            getTomcatAccAgent(tasResolver, tomcat, machine);
        }
        else {
            getTomcatAgent(tasResolver, tomcat, machine);
        }
        getTomcatStartupScripts(tasResolver, tomcat, tomcatVersion, machine);
        
		//update startup scripts
		Map<String, String> replaceJava = new HashMap<String, String>();
		Map<String, String> replacePairs = new HashMap<String, String>();

		String agentJar = "Agent.jar";
		String agentProfile = "IntroscopeAgent.profile";
		
		if(isNoRedefEnabled) {
		    agentJar = "AgentNoRedefNoRetrans.jar";
		    if(!isAccAgentBundle) {
		        agentProfile = "IntroscopeAgent.NoRedef.profile";
		    }
		}
		
		replaceJava
				.put("\\[JAVA_HOME\\]", codifyPath(javaHome));
		replaceJava.put("\\[JRE_HOME\\]", codifyPath(javaHome));
		replacePairs.put("\\[PERM.SPACE.SIZE\\]", "256");
		replacePairs.put("\\[MAX.PERM.SPACE\\.SIZE\\]", "512");
		replacePairs.put("\\[MAX.HEAP.SIZE\\]", "512");
		replacePairs.put("\\[MIN.HEAP.SIZE\\]", "256");
		replacePairs.put("\\[AGENT.JAVA.OPTIONS\\]",
				"-javaagent\\:" + codifyPath(tomcat.getInstallDir())
						+ "/wily/" + agentJar
						+ " -Dcom.wily.introscope.agentProfile="
						+ codifyPath(tomcat.getInstallDir())
						+ "/wily/core/config/" + agentProfile);
		String fileName1 = tomcat.getInstallDir() + "/bin/setclasspath.bat";
		String fileName2 = tomcat.getInstallDir() + "/bin/catalina.bat";

		FileUpdateRole fileUpdateRole1 = new FileUpdateRole.Builder(
				"fileupdate1", tasResolver).filePath(fileName1)
				.replacePairs(replaceJava).build();
		FileUpdateRole fileUpdateRole2 = new FileUpdateRole.Builder(
				"fileupdate2", tasResolver).filePath(fileName2)
				.replacePairs(replacePairs).build();

		IRole tomcatStartupScriptsRole = machine.getRoleById("tomcatStartupScriptsRole");
		tomcatStartupScriptsRole.before(fileUpdateRole1, fileUpdateRole2);        
		machine.addRole(fileUpdateRole1, fileUpdateRole2);
    }
    
    private void getTomcatAgent(ITasResolver tasResolver,
                                TomcatRole tomcat,
                                TestbedMachine machine) {
        
        String artifact = "agent-noinstaller-tomcat-windows";
        if (isLegacyMode) {
            artifact = "agent-legacy-noinstaller-tomcat-windows";
        }

        DefaultArtifact agentArtifact = new DefaultArtifact(
                "com.ca.apm.delivery", artifact, "zip",
                getAgentArtifactVersion(tasResolver));
        setAgentVersion(artifact, tasResolver.getArtifactUrl(agentArtifact));
        
        UniversalRole tomcatAgentRole = new UniversalRole.Builder(
                "tomcatAgentRole", tasResolver)
                .unpack(agentArtifact, tomcat.getInstallDir())
                .build();
        
        tomcat.before(tomcatAgentRole);
        machine.addRole(tomcatAgentRole);
    }

    protected void getTomcatAccAgent(ITasResolver tasResolver,
                                   TomcatRole tomcat,                                  
                                   TestbedMachine machine) {

        AccAgentSetupRole tomcatAgentRole =
            new AccAgentSetupRole.Builder("tomcatAgentRole", tasResolver)
            .installDir(tomcat.getInstallDir())
            .shouldSetup(false)
           // .url(ACC_TOMCAT_WIN_AGENT_URL)
            .packageName("Tomcat - Spring")
            .osName("windows")
            .build();
        tomcat.before(tomcatAgentRole);
        machine.addRole(tomcatAgentRole);
    }

    private void getTomcatStartupScripts(ITasResolver tasResolver,
                                         TomcatRole tomcat,
                                         String tomcatVersion,
                                         TestbedMachine machine) {

        DefaultArtifact startupScriptArtifact = new DefaultArtifact(
            "com.ca.apm.binaries.tomcat", "tomcat.startup.scripts", "",
            "zip", tomcatVersion);
        
        UniversalRole tomcatStartupScriptsRole = new UniversalRole.Builder(
            "tomcatStartupScriptsRole", tasResolver)
            .unpack(startupScriptArtifact, tomcat.getInstallDir() + "/bin")
            .build();
        
        tomcat.before(tomcatStartupScriptsRole);
        machine.addRole(tomcatStartupScriptsRole);
    }
    
    protected void addKonakartRoles(TestbedMachine machine, 
                                    TomcatRole tomcatRole, 
                                    ITasResolver tasResolver) {
        
        //get konakart
        DefaultArtifact konakartDist =  new DefaultArtifact("com.ca.apm.coda", 
            "konakart", "wars", "zip", "8.7.0.0");        
        GenericRole konakartDistRole = new GenericRole.Builder("konakartDistRole", tasResolver)
            .unpack(konakartDist, DEPLOY_BASE + "/konakart")
            .build();  
        
        //copy war files            
        RunCommandFlowContext copyWars = new RunCommandFlowContext.Builder("XCOPY")
            .args(Arrays.asList(DEPLOY_BASE + "\\konakart\\*.war", tomcatRole.getInstallDir() + "\\webapps"))
            .build();        
        ExecutionRole copyKonakartWarsRole = new ExecutionRole.Builder("copyKonakartWarsRole")
            .flow(RunCommandFlow.class, copyWars)
            .build();
    
        //install mysql            
        MySqlWinRole mysqlRole = new MySqlWinRole.Builder("mysqlRole", tasResolver)
            .installDir(DEPLOY_BASE)
            .dbName("konakartDB")
            .userName("konakartUser")
            .userPassword("konakartPwd")
            .sqlScript(DEPLOY_BASE + "/konakart/database/MySQL/konakart_demo.sql")
            .build();
        
        konakartDistRole.before(mysqlRole);
        tomcatRole.before(copyKonakartWarsRole);
        machine.addRole(konakartDistRole, copyKonakartWarsRole, mysqlRole);        
    }    
    
    protected IRole addAccServerRoles(ITasResolver tasResolver, 
                                      TestbedMachine machine,
                                      Process packageProcess,
                                      String packageName) {
        
        ACCConfigurationServerRole accServer = AccServerRoleHelper.createAccServerRole(tasResolver,
                ACC_SERVER_ROLE_ID);
        IRole startACCServerRole = accServer.getServerStartRole();

        // Copy from resources to config folder
        FileModifierFlowContext.Builder fileModifierBuilder = new FileModifierFlowContext.Builder();
        fileModifierBuilder.resource("//APMCommandCenterServer/config/tokens.xml",
                "/acc/tokens.xml");
        ExecutionRole tokensXMlCopyRole = new ExecutionRole.Builder("tokensXMLCopyRole").flow(
                FileModifierFlow.class, fileModifierBuilder.build()).build();

        String accVersion = com.ca.apm.acc.common.TestUtilities.getACCVersionTruncated();
        
        CreateAccPackageFlowContext createPkgContext = new CreateAccPackageFlowContext.Builder()
            .accServerUrl(AccServerRoleHelper.getServerUrl(tasResolver.getHostnameById(ACC_SERVER_ROLE_ID)))
            .packageName(packageName)
            .process(packageProcess)
            .osName(OsName.WINDOWS)
            .sleep(120000)
            .agentVersion(accVersion)
            .build();
        ExecutionRole createPkgRole = new ExecutionRole.Builder(ACC_CREATE_PACKAGE_ROLE_ID).flow(
                CreateAccPackageFlow.class, createPkgContext).build();

        accServer.before(tokensXMlCopyRole);
        tokensXMlCopyRole.before(startACCServerRole);
        startACCServerRole.before(createPkgRole);

        machine.addRole(accServer, tokensXMlCopyRole, startACCServerRole, createPkgRole);
        return accServer;
    }
    
    protected void initGenericSystemProperties(ITasResolver tasResolver, 
                                               ITestbed testBed, 
                                               HashMap<String,String> props) {
        
        
        String parentDir = codifyPath(DEPLOY_BASE);
        
        if(isLinuxTestbed)
            parentDir = codifyPath(DEPLOY_LINUX_BASE);
        
        //testng   
        props.put("data.file", "testng_java_main.list");
        props.put("test.priority.exclusive", "false"); 
       // props.put("role_client.test.priority", "1"); //enabling here will overwrite suite level
        props.put("client.dir", parentDir + "client/");
        props.put("results.dir", parentDir + RESULTS_DIR + "/");
        props.put("log4j.configuration", "file:" + parentDir + "client/lib/log4j-testng-agent.properties");
        props.put("config.location", "file");
        props.put("org.uncommons.reportng.title", "Agent_Test_Results");    
        props.put("default.cqapps.dir", parentDir + "client/resources/cqapps"); 
        props.put("db.servers.data.files", "sc_dbserver_data.csv");      
        //em & agent
        props.put("legacy.param","");
        props.put("testbed_em.hostname", tasResolver.getHostnameById(EM_ROLE_ID));
        props.put("role_em.port", "5001");  
        props.put("mockem.port", "5002");   
        props.put("common.scp.user", testBed.getMachineById(MACHINE_1).getLocalSSHUserName());
        props.put("common.scp.password", testBed.getMachineById(MACHINE_1).getLocalSSHPassword());
        props.put("agent.build.number", agentVersion); 
        props.put("browseragent.enabled", "true"); 
        props.put("agent.noredef.enabled", "" + isNoRedefEnabled);         
        //standalone apps
        props.put("role_client.sqlmetricgen.install.dir", parentDir + "sqlmetricgen");
        props.put("role_client.ditestapp.install.dir", parentDir + "ditestapp");
        props.put("role_client.stressapp.install.dir", parentDir + "stressapp");
        props.put("role_client.probebuilderapp.install.dir", parentDir + "probebuilderapp");
        props.put("role_client.deepinheritance.install.dir", parentDir + "deepinheritance");
        //misc
        props.put("role_client.jmeter.install.dir", parentDir + "jmeter/apache-jmeter-3.1");
        props.put("java_v2.email.feature.enabled", "true"); 
        
        //set email recipients for functional tests
        if(!isJassEnabled) {
            String tasVersion = tasResolver.getDefaultVersion();
            if(tasVersion.contains("dev")) {
                emailRecipients = EMAIL_RECIPIENTS_DEV;
            }
            else if(tasVersion.contains("taurus")) {
                emailRecipients = EMAIL_RECIPIENTS_TAURUS;
            }
            else if(tasVersion.contains("aries")) {
                emailRecipients = EMAIL_RECIPIENTS_ARIES;
            }
            else if(tasVersion.contains("serpens")) {
                emailRecipients = EMAIL_RECIPIENTS_SERPENS;
            }
            else if(tasVersion.contains("CustomerSuccess")) {
                emailRecipients = EMAIL_RECIPIENTS_CUSTOMERSUCCESS;
            } 
        }
        
        props.put("java_v2.email.recipients", emailRecipients.replace(" ", ","));
        props.put("java_v2.email.smtp.server", SMTP_SERVER); 
        props.put("java_v2.email.sender", EMAIL_SENDER); 
        
        //qcuploadtool
        if(!isLinuxTestbed) {
            //disabling ALM results' updates for now
            props.put("role_qcuploadtool.upload.results", "false");
            props.put("qcuploadtool.install.dir", parentDir + "/qcuploadtool");
            props.put("qcuploadtool.java.home", qcuploadtoolJdk.getInstallDir());
            props.put("qcuploadtool.testset.name", "APM - " + getAgentArtifactVersion(tasResolver) + " - Java Agent");
            props.put("qcuploadtool.testset.folder", "Root/APM/System/Automation/TAS DG");
        }
        else
        {
            props.put("role_qcuploadtool.upload.results", "false");
            props.put("role_qcuploadtool.generate.results", "false");
        }
    }
    
    protected void initWlsSystemProperties(String host,
                                           String xjvmhost,
                                           HashMap<String,String> props,
                                           String javaHome) {
        
        String home = codifyPath(DEPLOY_BASE + "webapp/pipeorgandomain");
        
        props.put("testbed_client.hostname", host);       
        props.put("role_agent.install.dir", home + "/wily");
        props.put("role_webapp.container.type", "weblogic");
        props.put("role_webapp.domain.name", "pipeorgandomain");
        props.put("role_webapp.home.dir", home);
        props.put("role_webapp.port", "7001");
        props.put("testbed_webapp.hostname", host);
        props.put("role_webapp.appserver.dir", home);
        props.put("role_webapp.appserver.version", "12c");
        props.put("role_webapp.server.name", "server");
        props.put("role_webapp.appserver.jvm.home", javaHome);
        props.put("standalone.apps.jvm.home", javaHome);
        props.put("role_webapp.appserver.log.path", home + "/servers/server/logs");
        props.put("role_webapp.console.log.path", home);
        props.put("role_client.qcupload.result.file", QC_RESULTS_DIR + "/qcupload_weblogic_" + host + ".csv");
        props.put("xjvm.port", "7001");
        props.put("xjvm.appserver.dir", home);
        props.put("xjvm.hostname", xjvmhost);
        props.put("xjvm.agent.install.dir", home + "/wily");
        props.put("agent.start.sleep", "180000");
        props.put("agent.stop.sleep", "60000");
        props.put("pipeorgan.client.jvm.home", javaHome);
    }
    
    protected void initDefaultSystemProperties(String host,
                                               HashMap<String,String> props,
                                               String javaHome) {
        
        String parentDir = codifyPath(DEPLOY_BASE);
        
        props.put("testbed_client.hostname", host);       
        props.put("role_agent.install.dir", parentDir + "default/wily");
        props.put("role_webapp.container.type", "default");
        props.put("testbed_webapp.hostname", host);
        props.put("standalone.apps.jvm.home", javaHome);
        props.put("role_client.qcupload.result.file", QC_RESULTS_DIR + "/qcupload_default_" + host + ".csv");
    }
    
    protected void initTomcatSystemProperties(String host,
                                              String home,
                                              String version,
                                              HashMap<String,String> props,
                                              String javaHome) {
        
        props.put("testbed_client.hostname", host);       
        props.put("role_agent.install.dir", home + "/wily");
        props.put("role_webapp.container.type", "tomcat");
        props.put("role_webapp.home.dir", home);
        props.put("role_webapp.port", "9091");
        props.put("testbed_webapp.hostname", host);
        props.put("role_webapp.appserver.dir", home);
        props.put("role_webapp.appserver.version", version);
        props.put("role_webapp.appserver.jvm.home", javaHome);
        props.put("standalone.apps.jvm.home", javaHome);
        props.put("role_webapp.appserver.log.path", home + "/logs");  
        props.put("role_client.qcupload.result.file", QC_RESULTS_DIR + "/qcupload_tomcat_" + host + ".csv");
    }
    
    protected void initJbossSystemProperties(String host,
                                             String home,
                                             String version,
                                             HashMap<String,String> props,
                                             String javaHome) {

        props.put("testbed_client.hostname", host);       
        props.put("role_agent.install.dir", home + "/wily");
        props.put("role_webapp.container.type", "jboss");
        props.put("role_webapp.home.dir", home);
        props.put("role_webapp.port", "8585");
        props.put("testbed_webapp.hostname", host);
        props.put("role_webapp.appserver.dir", home);
        props.put("role_webapp.appserver.version", version);
        props.put("role_webapp.appserver.jvm.home", javaHome);
        props.put("standalone.apps.jvm.home", javaHome);
        props.put("role_webapp.appserver.log.path", home + "/standalone/log");   
        props.put("pipeorgan.client.jvm.home", javaHome);
        props.put("role_client.qcupload.result.file", QC_RESULTS_DIR + "/qcupload_jboss_" + host + ".csv");
    }
    
    protected void initWasSystemProperties(String host,
                                           String xjvmhost,
                                           HashMap<String,String> props,
                                           String standaloneJavaHome,
                                           String wasJvmVersion) {
        
        String home = codifyPath(DEPLOY_BASE + "was");
        
        props.put("testbed_client.hostname", host);       
        props.put("role_agent.install.dir", home + "/wily_server1");
        props.put("role_webapp.container.type", "websphere");
        props.put("role_webapp.home.dir", home);
        props.put("role_webapp.port", "9080");
        props.put("testbed_webapp.hostname", host);
        props.put("role_webapp.appserver.dir", home);
        props.put("role_webapp.appserver.version", "8.5");
        props.put("role_webapp.appserver.jvm.home", home + "/" + wasJvmVersion);
        props.put("standalone.apps.jvm.home", standaloneJavaHome);
        props.put("role_webapp.appserver.log.path", home + "/profiles/AppSrv01/logs/server1");
        props.put("role_client.qcupload.result.file", QC_RESULTS_DIR + "/qcupload_websphere_" + host + ".csv");
        props.put("role_webapp.server.name", "server1");
        props.put("role_webapp.profile.name", "AppSrv01");
        props.put("role_webapp.domain.name", host + "Node01Cell");            
        props.put("xjvm.port", "9100");
        props.put("xjvm.appserver.dir", home);
        props.put("xjvm.hostname", xjvmhost);
        props.put("xjvm.server.name", "server1");
        props.put("xjvm.profile.name", "AppSrv01");
        props.put("xjvm.agent.install.dir", home + "/wily_server1");    
        props.put("pipeorgan.client.jvm.home", standaloneJavaHome);
    }
    
    protected void initEpagentSystemProperties(String host,
                                               HashMap<String,String> props,
                                               String javaHome) {
            
        String parentDir = codifyPath(DEPLOY_BASE);
        
        props.put("testbed_client.hostname", host);       
        props.put("role_agent.install.dir", parentDir + "epagent");
        props.put("perl.exe.path", "C:/Perl/bin/perl.exe");  
        props.put("role_agent.config.dir", "");
        props.put("role_webapp.home.dir", parentDir + "epagent");
        props.put("role_agent.agent.profile", "IntroscopeEPAgent.properties");
        props.put("role_webapp.container.type", "epagent");
        props.put("testbed_webapp.hostname", host);
        props.put("standalone.apps.jvm.home", javaHome);
        props.put("role_client.qcupload.result.file", QC_RESULTS_DIR + "/qcupload_epagent_" + host + ".csv");
    }
    
    protected void initDotNetSystemProperties(ITasResolver tasResolver,
                                              String host,
                                              HashMap<String,String> props) {
        
        props.put("data.file", "testng_dotnet.csv");
        props.put("role_agent.install.dir", codifyPath(DEPLOY_BASE + "dotnet/wily"));
        props.put("testbed_client.hostname", host);       
        props.put("testbed_webapp.hostname", host);
        props.put("role_webapp.container.type", "dotnet");
        props.put("role_agent.config.dir", "");
        props.put("role_webapp.port", "80");
        props.put("dotnettestapp.home", codifyPath(DEPLOY_BASE + "testapps/DotNetTestApp"));
        props.put("role_client.qcupload.result.file", QC_RESULTS_DIR + "/qcupload_dotnet_" + host + ".csv");
        props.put("qcuploadtool.testset.name", "APM - " + getAgentArtifactVersion(tasResolver) + " - Dotnet Agent");
    }    
    
    protected void initJassSystemProperties(HashMap<String,String> props, 
                                            ITasResolver tasResolver) {

        props.put("data.file", "testng_java_system.csv");
        props.put("jass.enabled", "true");
        props.put("jass.cluster", "true");
        props.put("test.priority.exclusive", "true");
        props.put("jass.mom.hostname",  tasResolver.getHostnameById(JassTestBedUtil.MOM_ROLE_ID));
        props.put("jass.test.duration", "43200000"); 
        props.put("role_em.install.dir", codifyPath(DEPLOY_BASE) + "em");
        props.put("qcuploadtool.testset.name", "APM - " + getAgentArtifactVersion(tasResolver) + " - JASS");
        props.put("qcuploadtool.testset.folder", "Root/APM/System/Automation/TAS DG/JASS");
        props.put("jass.get.priority.index.from.db", "true");
    }
    
    protected void initJassStandaloneEMSystemProperties(HashMap<String,String> props, 
                                                        ITasResolver tasResolver) {

        props.put("data.file", "testng_java_system.csv");
        props.put("jass.enabled", "true");
        props.put("jass.cluster", "false");
        props.put("test.priority.exclusive", "true");
        props.put("jass.test.duration", "43200000");
        props.put("role_em.install.dir", codifyPath(DEPLOY_BASE) + "em");
        props.put("qcuploadtool.testset.name", "APM - " + getAgentArtifactVersion(tasResolver) + " - JASS");
        props.put("qcuploadtool.testset.folder", "Root/APM/System/Automation/TAS DG/JASS");
        props.put("jass.get.priority.index.from.db", "true");
    }
    
    protected void initAccSystemProperties(HashMap<String,String> props) {
        
        props.put("agent.build.number", getAccAgentVersion()); 
        props.put("use.acc.agent.bundle", "true");
        props.put("jass.get.priority.index.from.db", "true");
        //props.put("browseragent.enabled", "false"); 
    }
    
    protected void setTestngCustomJvmArgs(HashMap<String,String> map, ITestbed testBed) {
        updateTestBedProps(map, testBed);
    }
    
    protected void updateTestBedProps(HashMap<String,String> map, ITestbed testBed) {
        
        HashSet<String> props = new HashSet<String>();
        
        for (Map.Entry<String, String> entry : map.entrySet()) {
            props.add("-D" + entry.getKey() + "=" + entry.getValue());
        }        
        testBed.addProperty(RunTestNgFlowContext.CUSTOM_JAVA_ARGS, props);  
    }
    
    @NotNull
    protected String codifyPath(String path) {
        return FilenameUtils.separatorsToUnix(path);
    }

    @NotNull
    protected String getTestBedName() {
        return AgentRegressionBaseTestBed.class.getSimpleName();
    }
    
    public static String getAgentArtifactVersion(ITasResolver tasResolver) {
        return tasResolver.getDefaultVersion();
    }
    
    public static String getEMArtifactVersion(ITasResolver tasResolver) {
        return tasResolver.getDefaultVersion();
    }
}