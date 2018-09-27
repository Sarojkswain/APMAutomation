/*
 * Copyright (c) 2016 CA.  All rights reserved.
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

package com.ca.apm.tests.coda.testbed;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.artifact.thirdParty.WebSphereVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.ControllerRole;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.role.webapp.WebLogicRole;
import com.ca.tas.role.webapp.WebSphereRole;
import com.ca.tas.testbed.CodaTestBed;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

/**
 * SapEmBatTestbed class.
 *
 * SapEmBatTestbed test-bed
 *
 * @author korzd01@ca.com
 */
@TestBedDefinition
public class SapEmBatTestbed extends CodaTestBed {

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ControllerRole controllerRole = initControllerBuilder(tasResolver).build();
        ITestbedMachine controller = TestBedUtils
            .createWindowsMachine("controllerMachine", TEMPLATE_W64, controllerRole);
        RoleUtility.gatherJenkinsLogs(controllerRole, controller);
        ITestbed testBed = new Testbed(getTestBedName())
            .addMachine(controller);
 
        //create machines used in CODA test
        TestbedMachine testMachine =
            new TestbedMachine.Builder("testMachine").automationBaseDir("C:\\sw")
                .templateId(TEMPLATE_W64).build();
        DeployFreeRole client_windowsservicewrappersRole = new DeployFreeRole("client_windowsservicewrappers");
        String testMachineHostname = tasResolver.getHostnameById(client_windowsservicewrappersRole.getRoleId());

        client_windowsservicewrappersRole.addProperty("apmbase.keystore.loc", "/config/internal/server/keystore");
        client_windowsservicewrappersRole.addProperty("bea.home", "C:/Oracle/Middleware");
        client_windowsservicewrappersRole.addProperty("binFolderName", "/bin");
        client_windowsservicewrappersRole.addProperty("em.loc", "C:/sw/em");
        client_windowsservicewrappersRole.addProperty("em.logFileName", "${em.loc}/logs/IntroscopeEnterpriseManager.log");
        client_windowsservicewrappersRole.addProperty("em.properties", "${em.loc}/config/IntroscopeEnterpriseManager.properties");
        client_windowsservicewrappersRole.addProperty("emfolderwithspace.location", "C:/sw/Introscope home");
        client_windowsservicewrappersRole.addProperty("epagent.home", "C:/sw/epagent");
        client_windowsservicewrappersRole.addProperty("epagent.java.home", "C:/Program Files/Java/jdk1.7.0_51");
        client_windowsservicewrappersRole.addProperty("hostfullname", RoleUtility.hostnameToFqdn(testMachineHostname));
        client_windowsservicewrappersRole.addProperty("hvragent.loc", "C:/SW/HVR_Agent");
        client_windowsservicewrappersRole.addProperty("introscope.profile", "C:/sw/webapp/pipeorgandomain/wily/core/config/IntroscopeAgent.profile");
        client_windowsservicewrappersRole.addProperty("invalidhostname", "abc");
        client_windowsservicewrappersRole.addProperty("java.agent.install.dir", "C:/SW");
        client_windowsservicewrappersRole.addProperty("java.home", "C:/Program Files/Java/jdk1.7.0_51");
        client_windowsservicewrappersRole.addProperty("logFolderName", "/logs");
        client_windowsservicewrappersRole.addProperty("max.heap.mb", "512");
        client_windowsservicewrappersRole.addProperty("max.permsize.mb", "256");
        client_windowsservicewrappersRole.addProperty("min.heap.mb", "512");
        client_windowsservicewrappersRole.addProperty("myresults.dir", "C:/automation/deployed/results/junitreports");
        client_windowsservicewrappersRole.addProperty("otherhostname", "ntbat01");
        client_windowsservicewrappersRole.addProperty("sap.admin.passw", "Admin89");
        client_windowsservicewrappersRole.addProperty("sap.admin.user", "Admin");
        client_windowsservicewrappersRole.addProperty("sap.changeDetectorIntegratedModule.pathTocheck", "/config/modules");
        client_windowsservicewrappersRole.addProperty("sap.changeDetectorIntegratedPlugin.pathTocheck", "/product/enterprisemanager/plugins");
        client_windowsservicewrappersRole.addProperty("sap.checkFileExists2.pathTocheck", "/docs");
        client_windowsservicewrappersRole.addProperty("sap.checkFileExists3.pathTocheck", "/license");
        client_windowsservicewrappersRole.addProperty("sap.customizedFile.pathTocheck", "/config");
        client_windowsservicewrappersRole.addProperty("sap.em.loc", "C:/sw/em");
        client_windowsservicewrappersRole.addProperty("sap.hostname", testMachineHostname);
        client_windowsservicewrappersRole.addProperty("sap.introscopeEMWebViewFile", "/logs/IntroscopeEMWebView.log");
        client_windowsservicewrappersRole.addProperty("sap.IntroscopeEnterpriseManagerFile", "/logs/IntroscopeEnterpriseManager.log");
        client_windowsservicewrappersRole.addProperty("sap.plugins.pathTocheck", "/product/enterprisemanager/plugins");
        client_windowsservicewrappersRole.addProperty("sap.port", "6001");
        client_windowsservicewrappersRole.addProperty("sap.sampleResponseFile", "/SampleResponseFile.Introscope.txt");
        client_windowsservicewrappersRole.addProperty("sap.sapEmBuildsBaseLineDb.pathTocheck", "/data");
        client_windowsservicewrappersRole.addProperty("sapem.install.parent.dir", "C:/sw/em");
        client_windowsservicewrappersRole.addProperty("sapem.sampleFile", "${java.io.tmpdir}/automation_stage/sap/em");
        client_windowsservicewrappersRole.addProperty("sapem.stage.dir", "${java.io.tmpdir}/automation_stage/sap/${role.name}");
        client_windowsservicewrappersRole.addProperty("smartstor.dir", "C://");
        client_windowsservicewrappersRole.addProperty("was.agent.install.dir", "${testbed_webapp.was7.home}/AppServer/wily");
        client_windowsservicewrappersRole.addProperty("was7.admin.port", "9043");
        client_windowsservicewrappersRole.addProperty("was7.appserver.dir", "${testbed_webapp.was7.home}/AppServer");
        client_windowsservicewrappersRole.addProperty("was7.home", "C:/IBM/WebSphere");
        client_windowsservicewrappersRole.addProperty("was7.node.info", "Node01");
        client_windowsservicewrappersRole.addProperty("was7.port", "9080");
        client_windowsservicewrappersRole.addProperty("weblogic.version", "10.3");
        client_windowsservicewrappersRole.addProperty("wls.agent.install.dir", "${testbed_webapp.install.parent.dir}/webapp/pipeorgandomain/wily");
        client_windowsservicewrappersRole.addProperty("wls.home", "C:/Oracle/Middleware/wlserver_10.3");
        client_windowsservicewrappersRole.addProperty("wls.port", "7001");
        client_windowsservicewrappersRole.addProperty("wurlitzer.em01.host", "${testbed_em01.hostname}");
        client_windowsservicewrappersRole.addProperty("wurlitzer.webapp1.host", "${hostname}");
        client_windowsservicewrappersRole.addProperty("wurlitzer.webapp1.port", "8080");
        client_windowsservicewrappersRole.addProperty("YourKit.dir", "''");
        client_windowsservicewrappersRole.addProperty("YourKit.max.heap.mb", "8000");
        client_windowsservicewrappersRole.addProperty("YourKit.min.heap.mb", "8000");
        testMachine.addRole(client_windowsservicewrappersRole);
        
        testMachine.addRole(new DeployFreeRole("webapp03"));
        testMachine.addRole(new DeployFreeRole("agent_sstools_2"));
        testMachine.addRole(new DeployFreeRole("agent_domainPermission"));
        testMachine.addRole(new DeployFreeRole("client_watchdog_2"));
        testMachine.addRole(new DeployFreeRole("client01_domainpermissions1"));
        testMachine.addRole(new DeployFreeRole("agent_embasics"));
        WebSphereRole wasRole =
            new WebSphereRole.Builder("webapp01", tasResolver)
                .wasInstallLocation("C:/IBM/WebSphere/AppServer")
                .responseFileDir("C:/IBM/responseFiles")
                .wasUnpackedInstallSourcesDir("C:/IBM/sourcesUnpacked/install")
                .wasInstaller(WebSphereVersion.v70x64w).build();
        testMachine.addRole(wasRole);
        WebLogicRole wlRole =
            new WebLogicRole.Builder("webapp02", tasResolver)
                .installLocation("C:/Oracle/Middleware")
                .installDir("C:/Oracle/Middleware/wlserver_10.3")
                .webLogicInstallerDir("C:/Oracle/sources").installLogFile("C:/Oracle/install.log")
                .responseFileDir("C:/Oracle/responseFiles").build();
        testMachine.addRole(wlRole);
        
        TomcatRole tomcatRole = new TomcatRole.Builder("tomcat70", tasResolver)
            .tomcatVersion(TomcatVersion.v70)
            .installDir("c:/sw/tomcat-7.0.22")
            .tomcatCatalinaPort(8181)
            .build();
        testMachine.addRole(tomcatRole);
        
        // Modify setenv.bat to use agent deployed by coda later
        FileModifierFlowContext.Builder modifierContextBuilder = new FileModifierFlowContext.Builder();
        List<String> configData = Collections.singletonList("set JAVA_OPTS=%JAVA_OPTS% -javaagent:c:\\sw\\wily\\Agent.jar "
            + "-Dcom.wily.introscope.agentProfile=c:\\sw\\wily\\core\\config\\IntroscopeAgent.profile");
        modifierContextBuilder.append("c:/sw/tomcat-7.0.22/bin/setenv.bat", configData);

        UniversalRole reconfigureTomcatRole = new UniversalRole.Builder("reconfigureTomcat", tasResolver)
            .runFlow(FileModifierFlow.class, modifierContextBuilder.build())
            .build();
        reconfigureTomcatRole.after(tomcatRole);
        testMachine.addRole(reconfigureTomcatRole);        

        testMachine.addRole(new DeployFreeRole("client01_embasics"));
        testMachine.addRole(new DeployFreeRole("client_embasics_2"));
        testMachine.addRole(new DeployFreeRole("client_javascriptenhancements_1"));
        testMachine.addRole(new DeployFreeRole("agent02_sstools_2"));
        testMachine.addRole(new DeployFreeRole("client01_empropertiesandlax3"));
        testMachine.addRole(new DeployFreeRole("agent02_ccdirectory"));
        testMachine.addRole(new DeployFreeRole("agent_watchdog"));
        testMachine.addRole(new DeployFreeRole("agent_sstools_tomcat"));
        testMachine.addRole(new DeployFreeRole("client_controlscripts"));
        testMachine.addRole(new DeployFreeRole("client01"));
        testMachine.addRole(new DeployFreeRole("agent_javascriptenhancements"));
        testMachine.addRole(new DeployFreeRole("client01_databasebackup"));
        testMachine.addRole(new DeployFreeRole("client01_domainPermission2"));
        testMachine.addRole(new DeployFreeRole("client01_ccdirectory"));
        testMachine.addRole(new DeployFreeRole("client_rebindportcommunication"));
        testMachine.addRole(new DeployFreeRole("client_sstools_tomcat"));
        testMachine.addRole(new DeployFreeRole("agent_ccdirectory"));
        testMachine.addRole(new DeployFreeRole("sapem02"));
        testMachine.addRole(new DeployFreeRole("client01_embasics_2"));
        testMachine.addRole(new DeployFreeRole("client01_smartstorflag"));
        testMachine.addRole(new DeployFreeRole("sapem01"));
        testMachine.addRole(new DeployFreeRole("client01_watchdog"));
        testMachine.addRole(new DeployFreeRole("client01_rebindportcommunication"));
        testMachine.addRole(new DeployFreeRole("client01_watchdog_2"));
        testMachine.addRole(new DeployFreeRole("agent_embasics_1"));
        testMachine.addRole(new DeployFreeRole("agent_watchdog_2"));
        testMachine.addRole(new DeployFreeRole("client01_sstools_tomcat"));
        testMachine.addRole(new DeployFreeRole("agent02_javascriptenhancements"));
        testMachine.addRole(new DeployFreeRole("client_smartstorflag"));
        testMachine.addRole(new DeployFreeRole("agent03"));
        testMachine.addRole(new DeployFreeRole("agent04"));
        testMachine.addRole(new DeployFreeRole("agent01"));
        testMachine.addRole(new DeployFreeRole("agent02"));
        testMachine.addRole(new DeployFreeRole("client01_controlscripts"));
        testMachine.addRole(new DeployFreeRole("client01_sapinstaller"));
        testMachine.addRole(new DeployFreeRole("client_watchdog"));
        testMachine.addRole(new DeployFreeRole("client01_apm91newtests"));
        testMachine.addRole(new DeployFreeRole("client01_windowsservicewrappers"));
        testMachine.addRole(new DeployFreeRole("agent02_embasics"));
        testMachine.addRole(new DeployFreeRole("agent02_sstools"));
        testMachine.addRole(new DeployFreeRole("client01_sstools_2"));
        testMachine.addRole(new DeployFreeRole("agent_smartstorflag"));
        testMachine.addRole(new DeployFreeRole("agent_javascriptenhancements_1"));
        testMachine.addRole(new DeployFreeRole("agent_sstools"));
        testMachine.addRole(new DeployFreeRole("client01_sstools"));
        testMachine.addRole(new DeployFreeRole("client01_javascriptenhancements"));
        testMachine.addRole(new DeployFreeRole("client01_javascriptenhancements_1"));
        testBed.addMachine(testMachine);

        testMachine.addRemoteResource(RemoteResource.createFromLocation("C:/automation/deployed/results/junitreports"));

        RoleUtility.fixRegistryForJenkinsRole(tasResolver, controller, controller.getRoles());
        RoleUtility.fixRegistryForJenkinsRole(tasResolver, testMachine, testMachine.getRoles());
        
        return testBed;
    }

    @NotNull
    @Override
    protected String getTestBedName() {
        return getClass().getSimpleName();
    }

    @NotNull
    @Override
    protected ControllerRole.Builder initControllerBuilder(ITasResolver tasResolver) {
        ControllerRole.Builder result = super.initControllerBuilder(tasResolver);
        result.globalProperty("email.sender", "tas@ca.com")
              .globalProperty("email.recipients", "korzd01@ca.com");
        return result;
    }
}

