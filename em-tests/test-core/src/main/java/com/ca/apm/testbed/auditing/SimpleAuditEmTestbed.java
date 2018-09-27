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

package com.ca.apm.testbed.auditing;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.apm.tests.artifact.CsvToXlsTemplateVersion;
import com.ca.apm.tests.artifact.HammondDataVersion;
import com.ca.apm.tests.role.CsvToXlsRole;
import com.ca.apm.tests.role.CsvToXlsTemplateRole;
import com.ca.apm.tests.role.PerfMonitorRole;
import com.ca.apm.tests.role.WurlitzerRole;
import com.ca.tas.artifact.IBuiltArtifact.Version;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumChromeDriver;
import com.ca.tas.artifact.thirdParty.selenium.SeleniumStandaloneServer;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.HammondRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.seleniumgrid.BrowserType;
import com.ca.tas.role.seleniumgrid.NodeCapability;
import com.ca.tas.role.seleniumgrid.NodeConfiguration;
import com.ca.tas.role.seleniumgrid.NodePlatform;
import com.ca.tas.role.seleniumgrid.SeleniumGridHubRole;
import com.ca.tas.role.seleniumgrid.SeleniumGridNodeRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;

@TestBedDefinition
public class SimpleAuditEmTestbed implements ITestbedFactory {
    
    static public String MACHINE_ID = "endUserMachine";
    static public String EM_ROLE_ID = "introscope";
    static public String NWMACHINE_ID = "nwMachine";
    
    public static final JavaBinary JAVA_VERSION = JavaBinary.WINDOWS_64BIT_JDK_17;

    public static final String EMVERSION = "10.5.1.52";
    
    public static final String LOAD_01_MACHINE_ID = "L01";
    public static final String LOAD_02_MACHINE_ID = "L02";
    public static final String LOAD_03_MACHINE_ID = "L03";
    public static final String LOAD_04_MACHINE_ID = "L04";
    public static final String LOAD_05_MACHINE_ID = "L05";
    public static final String[] AT_LOAD_MACHINE_IDS = {LOAD_01_MACHINE_ID, LOAD_02_MACHINE_ID, LOAD_03_MACHINE_ID,
                                                        LOAD_04_MACHINE_ID, LOAD_05_MACHINE_ID};
    public static final String SELENIUM_HUB_ROLE_ID = "seleniumHubRoleId";
    public static final String SELENIUM_HUB_MACHINE_ID = "seleniumHubMachineId";
    
    private ITestbedMachine[] atLoadMachines;
    private ITestbedMachine seleniumHubMachine;
    
    public static final String CREATE_SHARE_DIR_ROLE_ID = "createShareDirRoleId";
    public static final String CREATE_SHARE_ROLE_ID = "createShareRoleId";
    
    public static final String PERFMON_MOM_ROLE_ID = "perfmonMomRole";
    public static final String CSV_TO_XLS_ROLE_ID = "csvToXlsRole";
    public static final String WURLITZER_ROLE_ID = "wurlitzer_role";
    public static final String HAMMOND_ROLE_ID = "hammond_role";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAuditEmTestbed.class);
    
    public static final String GC_LOG_FILE = TasBuilder.WIN_SOFTWARE_LOC + "em\\logs\\gclog.txt";
    
    private static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    private static final Collection<String> MOM_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-Dmail.mime.charset=UTF-8", "-Dorg.owasp.esapi.resources=./config/esapi",
        "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC",
        "-Xss512k", "-Dcom.wily.assert=false", "-showversion",
        "-XX:CMSInitiatingOccupancyFraction=50", 
        "-XX:+HeapDumpOnOutOfMemoryError", "-Xms8192m", "-Xmx8192m",
        "-verbose:gc", "-Xloggc:"+GC_LOG_FILE, "-Dappmap.user=admin",
        "-Dappmap.token="+ADMIN_AUX_TOKEN);
    
    private static final Collection<String> WV_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Djava.awt.headless=true", "-Dorg.owasp.esapi.resources=./config/esapi", "-Dsun.java2d.noddraw=true",
        "-Dorg.osgi.framework.bootdelegation=org.apache.xpath", "-javaagent:./product/webview/agent/wily/Agent.jar",
        "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile",
        "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily", "-Xms2048m", "-Xmx4096m",
        "-XX:+PrintGCDateStamps", "-XX:+HeapDumpOnOutOfMemoryError",
        "-verbose:gc", "-Xloggc:"+GC_LOG_FILE);
    

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed("SimpleAuditEm");
        
        atLoadMachines = new TestbedMachine[AT_LOAD_MACHINE_IDS.length];
        for (int i = 0; i < AT_LOAD_MACHINE_IDS.length; i++) {
            String machineId = AT_LOAD_MACHINE_IDS[i];
            atLoadMachines[i] = new TestbedMachine.Builder(machineId)
                    .platform(Platform.WINDOWS)
                    .templateId("jass")
                    .build();
        }
        seleniumHubMachine = new TestbedMachine
                .Builder(SELENIUM_HUB_MACHINE_ID)
                .templateId(ITestbedMachine.TEMPLATE_W64)
                .build();
        
        String hubHost = tasResolver.getHostnameById(SELENIUM_HUB_ROLE_ID);

        ArrayList<SeleniumGridNodeRole> seleniumRolesList = new ArrayList<>();

        for (int i = 0; i < AT_LOAD_MACHINE_IDS.length; i++) {
            String machineId = AT_LOAD_MACHINE_IDS[i];
            ITestbedMachine machine = atLoadMachines[i];

            JavaRole javaRole = addJavaRoleToMachine(tasResolver, machineId);

            SeleniumGridNodeRole seleniumNodeRole
                    = createSeleniumNodeRole(tasResolver, machineId, hubHost);

            machine.addRole(javaRole, seleniumNodeRole);

            seleniumRolesList.add(seleniumNodeRole);
        }

        createSeleniumHubRole(tasResolver, seleniumRolesList);
        
        testbed.addMachine(seleniumHubMachine, atLoadMachines);
        
        ITestbedMachine emMachine = new TestbedMachine.Builder(MACHINE_ID).platform(Platform.WINDOWS)
            .templateId("w64p_16gb").bitness(Bitness.b64).build();
        ITestbedMachine nowhereBankMachine = new TestbedMachine.Builder(NWMACHINE_ID).platform(Platform.WINDOWS)
            .templateId("w64p_16gb").bitness(Bitness.b64).build();
        
        // CREATE SHARE
        RunCommandFlowContext createShareDirFlowContext =
                new RunCommandFlowContext.Builder("if").args(
                        Arrays.asList("not", "exist", "c:\\share", "mkdir", "c:\\share")).build();
        ExecutionRole createShareDirRole =
                new ExecutionRole.Builder(CREATE_SHARE_DIR_ROLE_ID).syncCommand(
                        createShareDirFlowContext).build();

        RunCommandFlowContext createShareFlowContext =
                new RunCommandFlowContext.Builder("if").args(
                        Arrays.asList("not", "exist", "\\\\localhost\\share", "net", "share",
                                "share=c:\\share", "/GRANT:Everyone,FULL")).build();
        ExecutionRole createShareRole =
                new ExecutionRole.Builder(CREATE_SHARE_ROLE_ID)
                        .syncCommand(createShareFlowContext)
                        .build();

        createShareRole.after(createShareDirRole);
        emMachine.addRole(createShareDirRole, createShareRole);

        String shareHost = tasResolver.getHostnameById(CREATE_SHARE_ROLE_ID);
        String shareFolder = "share";
        
        
        EmRole.Builder emBuilder = new EmRole.Builder(EM_ROLE_ID, tasResolver)
                .version(EMVERSION).nostartEM().nostartWV();
        
        emBuilder.emLaxNlClearJavaOption(MOM_LAXNL_JAVA_OPTION);
        emBuilder.wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION);
        
        //emBuilder = configureBuilder(emBuilder); //audit is on if commented
        
        EmRole emRole = emBuilder.build();
        
        emMachine.addRole(emRole);
        RoleUtility.addMmRole(emMachine, emRole.getRoleId() + "_mm", emRole, "NowhereBankMM");
        
        IRole startReqRole = RoleUtility.addNowhereBankRole(nowhereBankMachine, emRole, null, tasResolver);
        IRole startEmRole = RoleUtility.addStartEmRole(emMachine, emRole, true, emRole);
        startReqRole.after(startEmRole);
        
        // ///////////////////////////////////////////////////
        // wurlitzer machine
        // ///////////////////////////////////////////////////
        WurlitzerRole wurlitzerRole =
                new WurlitzerRole.Builder(WURLITZER_ROLE_ID, tasResolver)
                        .wurlitzerMachine(tasResolver.getHostnameById(WURLITZER_ROLE_ID))
                        .targetMachine(tasResolver.getHostnameById(emRole.getRoleId()))
                        .runDuration(getRunDuration(TimeUnit.MINUTES)).version(Version.SNAPSHOT_DEV_99_99)
                        .antScriptPathSegments("scripts", "xml", "appmap-stress", "load-test", "build.xml")
                        .antScriptArgs("20-agents-150-apps-15-backends-1-frontends")
                        .terminateOnMatch("Connected to MOM").build();
        nowhereBankMachine.addRole(wurlitzerRole);
        
        // AGC hammond
        HammondRole hammondAgcRole =
                new HammondRole.Builder(HAMMOND_ROLE_ID, tasResolver)
                        .heapMemory("4g")
                        .scale(50)
                        .collector(tasResolver.getHostnameById(emRole.getRoleId()))
                        .data(HammondDataVersion.FLD_mainframe).runDuration(getRunDuration(TimeUnit.SECONDS)).build();
        nowhereBankMachine.addRole(hammondAgcRole);
        
        
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put("performance*.csv", "mom01_pm.csv");
        String momInstallDir = emRole.getEnvPropertyById(EmRole.ENV_PROPERTY_INSTALL_DIR);
        mapping.put(momInstallDir + "\\logs\\perflog.txt", "mom01_em.csv");
        mapping.put(momInstallDir + "\\logs\\gclog.txt", "mom01_gclog.txt");
        mapping.put(TasBuilder.WIN_SOFTWARE_LOC + "gc.png", "mom01_mem.png");
        mapping.put(TasBuilder.WIN_SOFTWARE_LOC + "mm.csv", "mom01_mm.csv");
        List<String> command = Arrays.asList(
                "java -Duser=admin -jar " +
                        emRole.getEnvPropertyById(EmRole.ENV_PROPERTY_INSTALL_DIR) + "\\lib\\CLWorkstation.jar " +
                        "get historical data from agents matching \".*Virtual.$\" " +
                        "and metrics matching \".*In Use Post GC.*|.*SmartStor Disk Usage.*\" " +
                        "for past " + (getRunDuration(TimeUnit.MINUTES) + 20) + " minutes with frequency of 15 sec " +
                        ">" + TasBuilder.WIN_SOFTWARE_LOC + "mm.csv");
        PerfMonitorRole perfmonMomRole = new PerfMonitorRole.Builder(PERFMON_MOM_ROLE_ID, tasResolver)
                .sharedFolder(shareHost, shareFolder)
                .sharePassword("Lister@123")
                .perfLogFileMapping(mapping)
                .sampleInterval("15")
                .perfLogPrepareCommands(command)
                .build();
        
        emMachine.addRole(perfmonMomRole);
        
        String csvToXlsInstallPath = TasBuilder.WIN_SOFTWARE_LOC + "csvToXls";
        CsvToXlsTemplateRole csvToXlsTemplateRole =
                new CsvToXlsTemplateRole.Builder("csvToXlsTemplateRoleId", tasResolver)
                        .installPath(csvToXlsInstallPath).version(CsvToXlsTemplateVersion.EM_PERFORMANCE_2)
                        .build();
        // DEPLOY CSV2XLS
        Map<String, String> sheetsMapping = new HashMap<String, String>();
        sheetsMapping.put("mom01_em.csv", "mom_em");
        sheetsMapping.put("mom01_pm.csv", "mom_pm");
        sheetsMapping.put("mom01_mm.csv", "mom_mm");

        String xlsOutputFileName = String.format("result_%s_%s.xls",
                new SimpleDateFormat("yyyyMMdd_hhmm").format(new Date()),
                EMVERSION);
        CsvToXlsRole csvToXlsRole = new CsvToXlsRole.Builder(CSV_TO_XLS_ROLE_ID, tasResolver)
                .shareDir("c:\\"+shareFolder)
                .installPath(csvToXlsInstallPath)
                .templateFileName(csvToXlsTemplateRole.getTemplateFilePath())
                .outputFileName("c:\\" + shareFolder + "\\" + xlsOutputFileName)
                .sheetsMapping(sheetsMapping).build();

        emMachine.addRole(csvToXlsTemplateRole, csvToXlsRole);
        
        testbed.addMachine(emMachine, nowhereBankMachine);

        return testbed;
    }
    
    public static int getRunDuration(TimeUnit unit) {
        return (int) unit.convert(2, TimeUnit.DAYS);
//        return (int) unit.convert(30, TimeUnit.MINUTES);
    }
    
    protected EmRole.Builder configureBuilder(EmRole.Builder builder) {
        builder.configProperty("introscope.apmserver.audit.enabled", "false");
        return builder;
    }
    
    private static JavaRole addJavaRoleToMachine(ITasResolver tasResolver, String machineId) {
        final String javaDir = "C:\\sw\\java\\"
            + JAVA_VERSION.getJavaRuntime().name().toLowerCase()
            + JAVA_VERSION.getArtifact().getVersion();
        JavaRole javaRole = new JavaRole.Builder("javaRole_" + machineId, tasResolver)
            .dir(javaDir)
            .version(JAVA_VERSION)
            .build();
        return javaRole;
    }

    private void createSeleniumHubRole(ITasResolver tasResolver, ArrayList<SeleniumGridNodeRole> seleniumRolesList) {
        SeleniumGridHubRole.Builder hubRoleBuilder =
                new SeleniumGridHubRole.Builder(SELENIUM_HUB_ROLE_ID, tasResolver);
        SeleniumGridHubRole hubRole;

        for (SeleniumGridNodeRole role : seleniumRolesList) {
            hubRoleBuilder.addNodeRole(role);
        }

        hubRole = hubRoleBuilder.standaloneServerVersion(SeleniumStandaloneServer.V3_4_0).build();

        for (SeleniumGridNodeRole role : seleniumRolesList) {
            hubRole.after(role);
        }

        seleniumHubMachine.addRole(hubRole);
    }

    private SeleniumGridNodeRole createSeleniumNodeRole(ITasResolver tasResolver,
                                                        String machineId, String hubHost) {
        LOGGER.info("Using HUB host {}", hubHost);
        URL hubUrl = null;
        try {
            hubUrl = new URL("http://" + hubHost + ":4444/grid/register/");
        } catch (MalformedURLException ex) {
            LOGGER.error("HUB URL IS malformed", ex);
        }

        NodeCapability chromeCapability = new NodeCapability.Builder()
                .browserType(BrowserType.CHROME)
                .platform(NodePlatform.WINDOWS)
                .maxInstances(20)
                .build();

        // Define the whole node configuration
        NodeConfiguration nodeConfiguration = new NodeConfiguration.Builder()
                .addCapability(chromeCapability)
                .maxSession(100)
                .hub(hubUrl)
                .build();

        return new SeleniumGridNodeRole
                .Builder(machineId + "_seleniumNodeRole", tasResolver)
                .nodeConfiguration(nodeConfiguration)
                .chromeDriver(SeleniumChromeDriver.V2_29_B32)
                .standaloneServerVersion(SeleniumStandaloneServer.V3_4_0)
                .build();
    }
    
}
