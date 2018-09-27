/*
 * Copyright (c) 2014 CA.  All rights reserved.
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

package com.ca.tas.test.em.appmap;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.CronEntryRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.PhantomJSRole;
import com.ca.tas.role.TIMAttendeeRole;
import com.ca.tas.role.TIMRole;
import com.ca.tas.role.testapp.custom.TradeServiceAppRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.Platform;
import com.google.common.collect.Sets;

/**
 * Represents testbed for verifying the AppMap capability of Introscope CA product which is
 * monitoring the TradeService testing application. The testbed also supports monitoring
 * <b>Business Transactions</b> defined in CEM console against that monitored testing application.
 * 
 * @author Pospichal, Pavel <pospa02@ca.com>
 * 
 */
@TestBedDefinition
public class AppMapTradingServiceTestBed implements ITestbedFactory {

    
    public static final Collection<String> EM_LAXNL_JAVA_OPTION_9001 = Arrays.asList(
        "-Dappmap.token=f47ac10b-58cc-4372-a567-0e02b2c3d479", "-Dappmap.user=admin",
        "-Dcom.wily.assert=false", "-XX:+HeapDumpOnOutOfMemoryError", "-verbosegc", "-Xdebug",
        "-Xrunjdwp:server=y,transport=dt_socket,address=9001,suspend=n");

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        // TODO pospa02: should the id of testbed reflect the structure of requirements in ALM ?
        ITestbed testbed = new Testbed("Introscope/AppMap/TradeService");

        ITestbedMachine timMachine =
                new TestbedMachine.LinuxBuilder("gateway").platform(Platform.LINUX).templateId("co65")
                        .bitness(Bitness.b64).build();

        TIMRole timRole = new TIMRole.Builder("tim", tasResolver).build();
        timMachine.addRole(timRole);

        
        // setup end user machine where the user activities are done/test scenarios are run
        ITestbedMachine endUserMachine =
                new TestbedMachine.Builder("endUserMachine").platform(Platform.WINDOWS)
                        .templateId("w64").bitness(Bitness.b64).automationBaseDir("C:/sw").build();

        ITestbedMachine tradeServiceAppMachine =
                new TestbedMachine.Builder("tradeServiceApp").platform(Platform.WINDOWS)
                        .templateId("w64").bitness(Bitness.b64).automationBaseDir("C:/sw").build();

        // install Introscope/Collector with complete list of features
        EmRole introscopeCollectorRole; // used for Agent
        EmRole introscopeMomRole; // used for installing MM
        EmRole.Builder emBuilder;
        if (deployCluster()) {
            introscopeCollectorRole =
                    new EmRole.Builder("introscope_collector", tasResolver)
                            .dbpassword("quality")
                            .emClusterRole(DeployEMFlowContext.EmRoleEnum.COLLECTOR)
                            .emLaxNlJavaOption(RoleUtility.getDevEmLaxnlJavaOption(9001))
                            .silentInstallChosenFeatures(
                                    Sets.newHashSet("Enterprise Manager", "ProbeBuilder",
                                            "Database"))
                            .nostartWV().build();
            emBuilder =
                    new EmRole.Builder("introscope", tasResolver)
                            // role.introscope.wvPort is used by test
                            .dbpassword("quality")
                            .dbhost(tasResolver.getHostnameById(introscopeCollectorRole.getRoleId()))
                            .emClusterRole(DeployEMFlowContext.EmRoleEnum.MANAGER)
                            .emCollector(introscopeCollectorRole)
                            .emLaxNlJavaOption(RoleUtility.getDevEmLaxnlJavaOption(9001))
                            .silentInstallChosenFeatures(
                                    Sets.newHashSet("Enterprise Manager", "WebView", "ProbeBuilder"));
            introscopeMomRole = emBuilder.build();
            introscopeMomRole.after(introscopeCollectorRole);
            tradeServiceAppMachine.addRole(introscopeCollectorRole);
       } else {
            emBuilder = new EmRole.Builder("introscope", tasResolver).dbpassword("quality")
                .emLaxNlJavaOption(RoleUtility.getDevEmLaxnlJavaOption(9001));            
            introscopeMomRole = introscopeCollectorRole = emBuilder.build();
        }
        endUserMachine.addRole(introscopeMomRole);
        
        RoleUtility.addMmRole(endUserMachine, introscopeMomRole.getRoleId() + "1_mm",
                                introscopeMomRole, "StatusTestMM");
        RoleUtility.addMmRole(endUserMachine, introscopeMomRole.getRoleId() + "2_mm",
                                introscopeMomRole, "TradingServiceMM");

        PhantomJSRole phantomjsRole = new PhantomJSRole.Builder("phantomjs", tasResolver).build();
        endUserMachine.addRole(phantomjsRole);

        TIMAttendeeRole eumTimAttendeeRole =
                new TIMAttendeeRole.Builder("eum-tim-attendee", timRole, tasResolver).build();
        endUserMachine.addRole(eumTimAttendeeRole);


        // setup testing machines

        TomcatRole tomcatRole =
                new TomcatRole.Builder("tomcat60", tasResolver).tomcatVersion(TomcatVersion.v60)
                        .tomcatCatalinaPort(7080).jdkHomeDir("C:/Program Files/Java/jdk1.6.0_45")
                        .installDir("C:/sw/apache-tomcat-6.0.36").build();
        tradeServiceAppMachine.addRole(tomcatRole);
        RoleUtility.createSleepTxtFile(tradeServiceAppMachine, tomcatRole, tasResolver);

        Map<String, String> agentAdditionalProps = new HashMap<>();
        IRole createSnippetRole = RoleUtility.createBaSnippetFile(tradeServiceAppMachine,
                                                        agentAdditionalProps, tasResolver);

        AgentRole agentRole = new AgentRole.Builder("agent-tomcat", tasResolver)
                .webAppServer(tomcatRole)
                .platform(ArtifactPlatform.WINDOWS)
                .emRole(introscopeCollectorRole)
                .intrumentationLevel(AgentInstrumentationLevel.TYPICAL)
                .additionalProperties(agentAdditionalProps)
                .webAppAutoStart()
                .build();
        // TODO pospa02: avoid cyclic dependency as AgentRole defined dependency in other way
        // agentRole.after(tomcatRole);
        tradeServiceAppMachine.addRole(agentRole);
        agentRole.after(createSnippetRole);

        // HACK: currently has to be defined after AgentRole to ensure it won't be overridden
        TradeServiceAppRole tradeServiceAppRole =
                new TradeServiceAppRole.Builder("trade-service", tasResolver)
                        .tomcatRole(tomcatRole).build();

        tradeServiceAppMachine.addRole(tradeServiceAppRole);
        RoleUtility.addNewTradeServiceWars(tradeServiceAppMachine, tomcatRole,
                                                         tradeServiceAppRole, tasResolver);

        TIMAttendeeRole tsTimAttendeeRole =
                new TIMAttendeeRole.Builder("tsapp-tim-attendee", timRole, tasResolver).build();
        tradeServiceAppMachine.addRole(tsTimAttendeeRole);

        String baseUrl = "http://" + tasResolver.getHostnameById(tomcatRole.getRoleId()) + ":7080/";
        // every 4 hours downloads all links from TradeService
        String cronEntry = "* */4 * * * root wget -r --delete-after -nd " + baseUrl + "TradeService/ 2>/dev/null";
        timMachine.addRole(new CronEntryRole("cron_tradeservice", cronEntry));

        // every minute downloads login
        cronEntry = "* * * * * root wget --delete-after -nd " + baseUrl + "AuthenticationService/ServletA6 2>/dev/null";
        timMachine.addRole(new CronEntryRole("cron_login", cronEntry));

        // every even minute downloads TradeOptions 4 times
        String wgetStr = "wget --delete-after -nd " + baseUrl + "TradeService/TradeOptions 2>/dev/null";
        cronEntry = "*/2 * * * * root " + wgetStr + "\n" +
                "*/2 * * * * root sleep 15; " + wgetStr + "\n" +
                "*/2 * * * * root sleep 30; " + wgetStr + "\n" +
                "*/2 * * * * root sleep 45; " + wgetStr;
        timMachine.addRole(new CronEntryRole("cron_trade_options", cronEntry));

        Artifact testArtifact =
            new DefaultArtifact("com.ca.apm.test", "em-tests-core", "jar-with-dependencies", "jar",
                tasResolver.getDefaultVersion());
        String jarPath = timMachine.getAutomationBaseDir() + "em-tests-core.jar";
        timMachine.addRole(new GenericRole.Builder("test_jar", tasResolver).download(testArtifact,
            jarPath).build());
        cronEntry = "* * * * * root java -cp " + jarPath
                + " com.ca.apm.test.em.appmap.AppMapTopologySystemTest " + baseUrl;
        timMachine.addRole(new CronEntryRole("cron_chrome", cronEntry));
        
        testbed.addMachine(timMachine);
        testbed.addMachine(endUserMachine);
        testbed.addMachine(tradeServiceAppMachine);
        testbed.addProperty("machine.gateway.ip_v4", RoleUtility.getIp(tasResolver.getHostnameById(timRole.getRoleId())));

        return testbed;
    }

    public boolean deployCluster() {
        return false;
    }
}
