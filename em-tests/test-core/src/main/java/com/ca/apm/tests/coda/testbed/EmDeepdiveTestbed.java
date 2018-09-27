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

import org.eclipse.aether.artifact.DefaultArtifact;
import org.jetbrains.annotations.NotNull;

import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.ControllerRole;
import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.utility.UniversalRole;
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
 * EM Deep dive testbed class.
 *
 * Originally from testfarm czprtest - Tcsdeepdive07_testbed test-bed
 *
 * @author myspe01@ca.com
 */
@TestBedDefinition
public class EmDeepdiveTestbed extends CodaTestBed {
    
    private static final String JSENH_FILES_LOC =  "c:/automation/deployed/files";

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ControllerRole controllerRole = initControllerBuilder(tasResolver).build();
        ITestbedMachine controller = TestBedUtils
            .createWindowsMachine("controllerMachine", TEMPLATE_W64, controllerRole);
        RoleUtility.gatherJenkinsLogs(controllerRole, controller);

        ITestbed testBed = new Testbed(getTestBedName())
            .addMachine(controller);

        //create machines used in CODA test
        TestbedMachine tcsdeepdive09 = new TestbedMachine.Builder("tcsdeepdive09").templateId(TEMPLATE_W64).build();
        DeployFreeRole agent02_blRole = new DeployFreeRole("agent02_bl");
        agent02_blRole.addProperty("apmbase.keystore.loc", "/config/internal/server/keystore");
        agent02_blRole.addProperty("bat.name", "abc.bat");
        agent02_blRole.addProperty("bea.home", "C:/bea");
        agent02_blRole.addProperty("binFolderName", "/bin");
        agent02_blRole.addProperty("clwloc", "C:/sw/em/lib");
        agent02_blRole.addProperty("cmddrive.name", "C:/");
        agent02_blRole.addProperty("em.loc", "C:/sw/em");
        agent02_blRole.addProperty("em.port", "5001");
        agent02_blRole.addProperty("epagent.home", "C:/sw/em");
        agent02_blRole.addProperty("epagent.java.home", "C:/Progra~1/Java/jdk1.7.0_51");
        agent02_blRole.addProperty("hostfullname", RoleUtility.hostnameToFqdn(tasResolver.getHostnameById("agent02_bl")));
        agent02_blRole.addProperty("hvragent.loc", "C:/sw/HVR_Agent");
        agent02_blRole.addProperty("invalidhostname", "xyz");
        agent02_blRole.addProperty("ipaddress", RoleUtility.getIp(tasResolver.getHostnameById("agent02_bl")));
        agent02_blRole.addProperty("java.agent.install.dir", "C:/sw");
        agent02_blRole.addProperty("java.home", "C:/Progra~1/Java/jdk1.7.0_51");
        agent02_blRole.addProperty("logFolderName", "/logs");
        agent02_blRole.addProperty("mapped.dir", "C$/sw/results");
        agent02_blRole.addProperty("max.heap.mb", "512");
        agent02_blRole.addProperty("max.permsize.mb", "256");
        agent02_blRole.addProperty("metricsFile", "C:/sw/metricsFile.txt");
        agent02_blRole.addProperty("min.heap.mb", "512");
        agent02_blRole.addProperty("myresults.dir", "c:/automation/deployed/results/junitreports");
        agent02_blRole.addProperty("myResults.dir", "C:/sw/results");
        agent02_blRole.addProperty("otherhostname", tasResolver.getHostnameById("agent02_bl"));
        agent02_blRole.addProperty("sap.admin.passw", "Admin89");
        agent02_blRole.addProperty("sap.admin.user", "Admin");
        agent02_blRole.addProperty("sap.changeDetectorIntegratedModule.pathTocheck", "/config/modules");
        agent02_blRole.addProperty("sap.changeDetectorIntegratedPlugin.pathTocheck", "/product/enterprisemanager/plugins");
        agent02_blRole.addProperty("sap.checkFileExists2.pathTocheck", "/docs");
        agent02_blRole.addProperty("sap.checkFileExists3.pathTocheck", "/license");
        agent02_blRole.addProperty("sap.customizedFile.pathTocheck", "/config");
        agent02_blRole.addProperty("sap.hostname", "kapna01-VM20038");
        agent02_blRole.addProperty("sap.introscopeEMWebViewFile", "/logs/IntroscopeEMWebView.log");
        agent02_blRole.addProperty("sap.IntroscopeEnterpriseManagerFile", "/logs/IntroscopeEnterpriseManager.log");
        agent02_blRole.addProperty("sap.plugins.pathTocheck", "/product/enterprisemanager/plugins");
        agent02_blRole.addProperty("sap.port", "6001");
        agent02_blRole.addProperty("sap.sampleResponseFile", "/SampleResponseFile.Introscope.txt");
        agent02_blRole.addProperty("sap.sapEmBuildsBaseLineDb.pathTocheck", "/data");
        agent02_blRole.addProperty("sapem.install.parent.dir", "C:/sw/sap/em");
        agent02_blRole.addProperty("sapem.stage.dir", "${java.io.tmpdir}/automation_stage/sap/${role.name}");
        agent02_blRole.addProperty("slave1", "tcsdeepdive07");
        agent02_blRole.addProperty("was.agent.install.dir", "${testbed_webapp.was7.home}/AppServer/wily");
        agent02_blRole.addProperty("was7.admin.port", "9060");
        agent02_blRole.addProperty("was7.appserver.dir", "${testbed_webapp.was7.home}/AppServer");
        agent02_blRole.addProperty("was7.home", "C:/sw/IBM/WebSphere");
        agent02_blRole.addProperty("was7.node.info", "tcsdeepdive09Node01");
        agent02_blRole.addProperty("was7.port", "9080");
        agent02_blRole.addProperty("weblogic.version", "10.3");
        agent02_blRole.addProperty("wls.agent.install.dir", "${testbed_webapp.install.parent.dir}/webapp/pipeorgandomain/wily");
        agent02_blRole.addProperty("wls.home", "C:/bea/wlserver_10.3");
        agent02_blRole.addProperty("wls.port", "7011");
        agent02_blRole.addProperty("YourKit.max.heap.mb", "8000");
        agent02_blRole.addProperty("YourKit.min.heap.mb", "8000");
        tcsdeepdive09.addRole(agent02_blRole);

        tcsdeepdive09.addRole(new DeployFreeRole("agent_metricexplosion"));
        tcsdeepdive09.addRole(new DeployFreeRole("agent_baseliner"));
        tcsdeepdive09.addRole(new DeployFreeRole("agent_log4j"));
        tcsdeepdive09.addRole(new DeployFreeRole("agent_baseliner1"));
        tcsdeepdive09.addRole(new DeployFreeRole("webapp02_bl"));
        tcsdeepdive09.addRole(new DeployFreeRole("em03"));
        tcsdeepdive09.addRole(new DeployFreeRole("client27"));
        tcsdeepdive09.addRole(new DeployFreeRole("em04"));
        tcsdeepdive09.addRole(new DeployFreeRole("client22"));
        tcsdeepdive09.addRole(new DeployFreeRole("client25"));
        tcsdeepdive09.addRole(new DeployFreeRole("client24"));
        
        tcsdeepdive09.addRemoteResource(RemoteResource.createFromLocation("c:/automation/deployed/results"));
        testBed.addMachine(tcsdeepdive09);
        
        TestbedMachine tcsdeepdive07 = new TestbedMachine.Builder("tcsdeepdive07").templateId(TEMPLATE_W64).build();
        DeployFreeRole client17Role = new DeployFreeRole("client17");
        client17Role.addProperty("apmbase.keystore.loc", "/config/internal/server/keystore");
        client17Role.addProperty("bat.name", "abc.bat");
        client17Role.addProperty("bea.home", "C:/bea");
        client17Role.addProperty("binFolderName", "/bin");
        client17Role.addProperty("clwloc", "C:/automation/deployed/em/lib");
        client17Role.addProperty("cmddrive.name", "C:/");
        client17Role.addProperty("em.loc", "C:/automation/deployed/em");
        client17Role.addProperty("em.port", "5001");
        client17Role.addProperty("epagent.home", "C:/automation/deployed/em");
        client17Role.addProperty("epagent.java.home", "C:/Progra~1/Java/jdk1.7.0_51");
        client17Role.addProperty("hostfullname", RoleUtility.hostnameToFqdn(tasResolver.getHostnameById("client17")));
        client17Role.addProperty("hvragent.loc", "C:/sw/HVR_Agent");
        client17Role.addProperty("invalidhostname", "xyz");
        client17Role.addProperty("ipaddress", RoleUtility.getIp(tasResolver.getHostnameById("client17")));
        client17Role.addProperty("java.agent.install.dir", "C:/sw");
        client17Role.addProperty("java.home", "C:/Progra~1/Java/jdk1.7.0_51");
        client17Role.addProperty("java.jrocket.home", "C:/sw/java/jrockit-R27.3.1-jdk1.6.0_01");
        client17Role.addProperty("javaVersion.13", "1.7.0_21");
        client17Role.addProperty("logFolderName", "/logs");
        client17Role.addProperty("mapped.dir", "C$/sw/results");
        client17Role.addProperty("max.heap.mb", "512");
        client17Role.addProperty("max.permsize.mb", "256");
        client17Role.addProperty("metricsFile", "C:/sw/metricsFile.txt");
        client17Role.addProperty("min.heap.mb", "512");
        client17Role.addProperty("myresults.dir", "c:/automation/deployed/results/junitreports");
        client17Role.addProperty("myResults.dir", "C:/sw/results");
        client17Role.addProperty("otherhostname", tasResolver.getHostnameById("client17"));
        client17Role.addProperty("sap.admin.passw", "Admin89");
        client17Role.addProperty("sap.admin.user", "Admin");
        client17Role.addProperty("sap.changeDetectorIntegratedModule.pathTocheck", "/config/modules");
        client17Role.addProperty("sap.changeDetectorIntegratedPlugin.pathTocheck", "/product/enterprisemanager/plugins");
        client17Role.addProperty("sap.checkFileExists2.pathTocheck", "/docs");
        client17Role.addProperty("sap.checkFileExists3.pathTocheck", "/license");
        client17Role.addProperty("sap.customizedFile.pathTocheck", "/config");
        client17Role.addProperty("sap.hostname", "kapna01-VM20038");
        client17Role.addProperty("sap.introscopeEMWebViewFile", "/logs/IntroscopeEMWebView.log");
        client17Role.addProperty("sap.IntroscopeEnterpriseManagerFile", "/logs/IntroscopeEnterpriseManager.log");
        client17Role.addProperty("sap.plugins.pathTocheck", "/product/enterprisemanager/plugins");
        client17Role.addProperty("sap.port", "6001");
        client17Role.addProperty("sap.sampleResponseFile", "/SampleResponseFile.Introscope.txt");
        client17Role.addProperty("sap.sapEmBuildsBaseLineDb.pathTocheck", "/data");
        client17Role.addProperty("sapem.install.parent.dir", "C:/sw/sap/em");
        client17Role.addProperty("sapem.stage.dir", "${java.io.tmpdir}/automation_stage/sap/${role.name}");
        client17Role.addProperty("slave1", "tcsdeepdive07");
        client17Role.addProperty("was.agent.install.dir", "${testbed_webapp.was7.home}/AppServer/wily");
        client17Role.addProperty("was7.admin.port", "9060");
        client17Role.addProperty("was7.appserver.dir", "${testbed_webapp.was7.home}/AppServer");
        client17Role.addProperty("was7.home", "C:/sw/IBM/WebSphere");
        client17Role.addProperty("was7.node.info", "tcsdeepdive07Node01");
        client17Role.addProperty("was7.port", "9080");
        client17Role.addProperty("Weblogic", "Agent and WAS Agent install locations");
        client17Role.addProperty("weblogic.version", "10.3");
        client17Role.addProperty("wls.agent.install.dir", "${testbed_webapp.install.parent.dir}/webapp/pipeorgandomain/wily");
        client17Role.addProperty("wls.home", "C:/bea/wlserver_10.3");
        client17Role.addProperty("wls.port", "7011");
        client17Role.addProperty("YourKit.max.heap.mb", "8000");
        client17Role.addProperty("YourKit.min.heap.mb", "8000");
        tcsdeepdive07.addRole(client17Role);
        
        tcsdeepdive07.addRole(new DeployFreeRole("webapp03"));
        tcsdeepdive07.addRole(new DeployFreeRole("client16"));
        tcsdeepdive07.addRole(new DeployFreeRole("client19"));
        tcsdeepdive07.addRole(new DeployFreeRole("client18"));
        tcsdeepdive07.addRole(new DeployFreeRole("client12"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_embasics"));
        tcsdeepdive07.addRole(new DeployFreeRole("client13"));
        tcsdeepdive07.addRole(new DeployFreeRole("webapp01"));
        tcsdeepdive07.addRole(new DeployFreeRole("client14"));
        tcsdeepdive07.addRole(new DeployFreeRole("webapp02"));
        tcsdeepdive07.addRole(new DeployFreeRole("client15"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_sm2"));
        tcsdeepdive07.addRole(new DeployFreeRole("client10"));
        tcsdeepdive07.addRole(new DeployFreeRole("client11"));
        tcsdeepdive07.addRole(new DeployFreeRole("client09"));
        tcsdeepdive07.addRole(new DeployFreeRole("client08"));
        tcsdeepdive07.addRole(new DeployFreeRole("client07"));
        tcsdeepdive07.addRole(new DeployFreeRole("client06"));
        tcsdeepdive07.addRole(new DeployFreeRole("client05"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_smartstor2"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_smartstor1"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_commandlinepropertyoverride"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_aggregateagent"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_watchdog2"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_watchdog"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_ldap"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_ldapconfiguration"));
        tcsdeepdive07.addRole(new DeployFreeRole("client03"));
        tcsdeepdive07.addRole(new DeployFreeRole("client04"));
        tcsdeepdive07.addRole(new DeployFreeRole("client01"));
        tcsdeepdive07.addRole(new DeployFreeRole("client02"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_supportbundlerenhance"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_movetojava6"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_DPManagement2"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_smartstor"));
        tcsdeepdive07.addRole(new DeployFreeRole("em02"));
        tcsdeepdive07.addRole(new DeployFreeRole("em01"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_smart"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_dpmanagement"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent01"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent02"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_indexrebuilder"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_smartstor3"));
        tcsdeepdive07.addRole(new DeployFreeRole("agent_JavaScriptEnhancements"));
        tcsdeepdive07.addRole(new DeployFreeRole("client21"));
        tcsdeepdive07.addRole(new DeployFreeRole("client20"));
        tcsdeepdive07.addRole(new DeployFreeRole("client26"));
        tcsdeepdive07.addRole(new DeployFreeRole("client23"));
        
        
        WebSphereRole.Builder websphereBuilder = new WebSphereRole.Builder("websphere", tasResolver);
        websphereBuilder.wasInstallLocation("C:/sw/IBM/WebSphere/AppServer");
        //websphereBuilder.noHotfixes();
        websphereBuilder.autoStart();
        tcsdeepdive07.addRole(websphereBuilder.build());
        
        WebLogicRole.Builder webLogicBuilder = new WebLogicRole.Builder("weblogic", tasResolver);
        webLogicBuilder.installLocation("C:/bea");
        webLogicBuilder.installDir("C:/bea/wlserver_10.3");
        webLogicBuilder.autoStart();
        WebLogicRole webLogicRole = webLogicBuilder.build();
        tcsdeepdive07.addRole(webLogicRole);
        tcsdeepdive07.addRemoteResource(RemoteResource.createFromLocation("c:/automation/deployed/results"));

        final UniversalRole.Builder jsEnhFilesRole = new UniversalRole.Builder("javascript-enhancements-files", tasResolver)
            .unpack(new DefaultArtifact("com.ca.apm.coda.testdata", "javascript-enhancements","zip", "1.0"), JSENH_FILES_LOC);
        tcsdeepdive07.addRole(jsEnhFilesRole.build());

        testBed.addMachine(tcsdeepdive07);

        RoleUtility.fixRegistryForJenkinsRole(tasResolver, controller, controller.getRoles());
        RoleUtility.fixRegistryForJenkinsRole(tasResolver, tcsdeepdive07, tcsdeepdive07.getRoles());
        RoleUtility.fixRegistryForJenkinsRole(tasResolver, tcsdeepdive09, tcsdeepdive09.getRoles());

        return testBed;
    }

    @NotNull
    @Override
    protected String getTestBedName() {
        return getClass().getSimpleName();
    }
    
}

