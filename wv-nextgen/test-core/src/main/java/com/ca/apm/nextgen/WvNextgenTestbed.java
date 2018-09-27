/*
 * Copyright (c) 2015 CA. All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.nextgen;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.ant.AntRole;
import com.ca.apm.ant.AntVersion;
import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.Win32RegistryFlow;
import com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext;
import com.ca.apm.nextgen.qatng.artifacts.FrameworkArtifact;
import com.ca.apm.nextgen.qatng.artifacts.FunctionalTestArtifact;
import com.ca.apm.nextgen.qatng.artifacts.LibArtifact;
import com.ca.apm.nextgen.qatng.artifacts.MMArtifact;
import com.ca.apm.nextgen.role.HVRAgentRole;
import com.ca.apm.nextgen.role.artifacts.The100_TSDMetricsArtifact;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.artifact.thirdParty.TradeServiceAppVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.web.TradeServiceAppRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.CodaTestBed;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

import static com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext.RegHive
    .LOCAL_MACHINE;
import static com.ca.apm.automation.action.flow.utility.Win32RegistryFlowContext.RegValueType.DWORD;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

/**
 * CzprWv01 class.
 *
 * czpr-wv-01 test-bed
 *
 * @author svazd01@ca.com
 */
@TestBedDefinition
public class WvNextgenTestbed extends CodaTestBed {

    public static final String EM_ROLE = "em01";
    public static final String HVR_ROLE = "hvr01";
    public static final String ANT_ROLE = "ant_role";
    public static final String CONFIG_ROLE = "config_role";
    public static final String QATNG_DEPLOY_ROLE = "qatf01";
    public static final String TRADE_SERVICE_ROLE = "webapp01_tradeservice";
    public static final String TOMCAT_ROLE = "webapp01";
    public static final String AGENT_ROLE = "webapp01_agent";

    public static final String QATNG_PATH = "C:\\qa_sandbox\\selenium-tests\\bin";
    public static final String QATNG_LIB_PATH = "C:\\qa_sandbox\\selenium-tests";
    public static final String QATNG_LAUNCH_PROPS_PATH =
        "C:\\qa_sandbox\\selenium-tests\\bin\\launch.properties";


    public static final String LOGIN_URL = "http://%s:%s";
    public static final String LOGIN_URL_KEY = "web.login.url";
    public static final String BROWSER_TYPE = "browser.type";
    public static final String CHROME_OPTIONS = "chromeOptions";

    public static final int TOMCAT_CATALINA_PORT = 7080;

    public static final String DRIVERS_PATH = "C:\\sw\\seleniumdrivers";

    public static final String CHROME_DRIVER_PATH = DRIVERS_PATH + "\\chrome";
    public static final String MSIE_DRIVER_PATH = DRIVERS_PATH + "\\msie32b";



    @Override
    public ITestbed create(ITasResolver tasResolver) {

        IRole controllerRole = initControllerBuilder(tasResolver).build();
        ITestbedMachine controller = TestBedUtils
            .createWindowsMachine(CONTROLLER_MACHINE_ID, TEMPLATE_W64, controllerRole);
        ITestbed testBed = new Testbed(getTestBedName())
            .addMachine(controller);

        ITestbedMachine czprWv01 = testBed.getMachineById(CONTROLLER_MACHINE_ID);

        // deploy ant 1.9.2
        AntRole antRole =
            new AntRole.Builder(ANT_ROLE, tasResolver).antVersion(AntVersion.v1_9_3_zip).build();

        controllerRole.addProperty("testvariant.testng.jar.loc",
            "C:\\qa_sandbox\\selenium-tests\\lib\\testng-6.9.9.jar");

        antRole.before(controllerRole);
        czprWv01.addRole(antRole);


        EmRole em01Role = new EmRole.Builder(EM_ROLE, tasResolver).nostartEM().nostartWV().build();
        czprWv01.addRole(em01Role);

        // TestNg artifacts (tests,framework and libs)
        Artifact framework = new FrameworkArtifact(tasResolver).createArtifact().getArtifact();
        Artifact tests = new FunctionalTestArtifact(tasResolver).createArtifact().getArtifact();
        Artifact lib = new LibArtifact().createArtifact("1.1").getArtifact();

        // management modules for EM
        Artifact mm = new MMArtifact().createArtifact().getArtifact();

        // selenium drivers
        Artifact chromeDriverArtifact =
            new DefaultArtifact("com.ca.apm.binaries.selenium:chromedriver:zip:win32:2.19");
        Artifact ieDriverArtifact =
            new DefaultArtifact("com.ca.apm.binaries.selenium:IEDriverServer:zip:win32:2.45.0");



        String emPath = em01Role.getDeployEmFlowContext().getInstallDir();


        // ///// deploy and configure testNg /////
        UniversalRole qatf01 =
            new UniversalRole.Builder(QATNG_DEPLOY_ROLE, tasResolver).unpack(framework, QATNG_PATH)
                .unpack(tests, QATNG_PATH)
                .unpack(lib, QATNG_LIB_PATH)
                .unpack(mm, emPath + "\\config\\modules")
                .unpack(chromeDriverArtifact, CHROME_DRIVER_PATH)
                .unpack(ieDriverArtifact, MSIE_DRIVER_PATH).build();

        qatf01.after(em01Role);
        czprWv01.addRole(qatf01);

        // setup test ng launch properties
        Map<String, String> propsMap = new HashMap<String, String>();
        Map<String, String> instMap = em01Role.getDeployEmFlowContext().getInstallerProperties();

        // TODO enhance hostname resolving this expects the wv installed on same machine as EM.
        propsMap.put(LOGIN_URL_KEY,
            String.format(LOGIN_URL, instMap.get("wvEmHost"), instMap.get("wvPort")));
        propsMap.put(BROWSER_TYPE, "chrome");
        propsMap.put(CHROME_OPTIONS,  "--no-sandbox"); // Fix for Chrome v51

        ConfigureFlowContext ctx =
            new ConfigureFlowContext.Builder().configurationMap(QATNG_LAUNCH_PROPS_PATH, propsMap)
                .build();

        String configPath = Paths.get(emPath, "config").toString();
        String userspath = Paths.get(emPath, "config", "users.xml").toString();
        String domainspath = Paths.get(emPath, "config", "domains.xml").toString();
        String clusterPath = Paths.get(emPath, "config", "agentclusters.xml").toString();

        FileModifierFlowContext modifyConfigs =
            new FileModifierFlowContext.Builder().resource(userspath, "/em-config/users.xml")
                .resource(domainspath, "/em-config/domains.xml")
                .resource(clusterPath, "/em-config/agentclusters.xml").build();

        ExecutionRole configuretestNg =
            new ExecutionRole.Builder("configureTestNg").flow(ConfigureFlow.class, ctx)
                .flow(FileModifierFlow.class, modifyConfigs).build();

        configuretestNg.after(qatf01);
        czprWv01.addRole(configuretestNg);

        // start
        ExecutionRole startEm =
            new ExecutionRole.Builder("start_em")
                .asyncCommand(em01Role.getEmRunCommandFlowContext())
                .asyncCommand(em01Role.getWvRunCommandFlowContext()).build();

        startEm.after(qatf01);
        czprWv01.addRole(startEm);

        // add hvr agent
        HVRAgentRole hvrRole =
            new HVRAgentRole.Builder(HVR_ROLE, tasResolver)
                .addMetricsArtifact(new The100_TSDMetricsArtifact()).loadFile("100_TSD").start()
                .build();

        czprWv01.addRole(hvrRole);

        czprWv01.addRole(new DeployFreeRole("agentwebapp"));
        czprWv01.addRole(new DeployFreeRole("tomcatagent"));


        // deploy trade service
        WebAppRole<TomcatRole> tradeService =
            new TradeServiceAppRole.Builder(TRADE_SERVICE_ROLE, tasResolver)
                .version(TradeServiceAppVersion.v100).contextName("TradeService").build();

        TomcatRole tomcat1 =
            new TomcatRole.Builder(TOMCAT_ROLE, tasResolver).tomcatVersion(TomcatVersion.v60)
                .autoStart().tomcatCatalinaPort(TOMCAT_CATALINA_PORT).webApp(tradeService).build();

        AgentRole agent1 =
            new AgentRole.Builder(AGENT_ROLE, tasResolver).emRole(em01Role)
                .webAppServer(tomcat1).build();

        czprWv01.addRole(tomcat1);
        czprWv01.addRole(tradeService);
        czprWv01.addRole(agent1);



        czprWv01.addRole(new DeployFreeRole("client01"));
        czprWv01.addRole(new DeployFreeRole("webapp02"));
        czprWv01.addRole(new DeployFreeRole("client02"));
        
        czprWv01.addRemoteResource(RemoteResource.createFromLocation("c:/automation/deployed/results"));

        fixRegistryForJenkinsRole(tasResolver, controller, controller.getRoles());
        fixRegistryForJenkinsRole(tasResolver, czprWv01, czprWv01.getRoles());

        return testBed;
    }

    @NotNull
    @Override
    protected String getTestBedName() {
        return getClass().getSimpleName();
    }

    public static IRole fixRegistryForJenkinsRole(ITasResolver tasResolver, ITestbedMachine machine,
        IRole... beforeRoles) {
        Win32RegistryFlowContext context = new Win32RegistryFlowContext.Builder()
            .setValue(LOCAL_MACHINE, "SYSTEM\\CurrentControlSet\\Services\\LanmanServer"
                    + "\\Parameters\\SMB1",
                DWORD, 1)
            .build();

        UniversalRole role = new UniversalRole.Builder(machine.getMachineId() + "_SMBv1Enable",
            tasResolver)
            .runFlow(Win32RegistryFlow.class, context)
            .build();
        machine.addRole(role);

        if (beforeRoles != null) {
            for (IRole r : beforeRoles) {
                role.before(r);
            }
        }

        return role;
    }
}
