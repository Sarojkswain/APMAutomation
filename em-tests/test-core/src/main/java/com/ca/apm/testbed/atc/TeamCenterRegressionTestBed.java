/*
 * Copyright (c) 2014 CA. All rights reserved.
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

package com.ca.apm.testbed.atc;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.agent.AgentInstrumentationLevel;
import com.ca.apm.automation.action.flow.em.config.LocalRealmUsersFlow;
import com.ca.apm.automation.action.flow.em.config.LocalRealmUsersFlowContext;
import com.ca.apm.automation.action.flow.em.config.ModifyDomainsXmlFlow;
import com.ca.apm.automation.action.flow.em.config.ModifyDomainsXmlFlowContext;
import com.ca.apm.automation.action.flow.em.config.ModifyDomainsXmlFlowContext.Builder;
import com.ca.apm.automation.action.flow.em.config.ModifyDomainsXmlFlowContext.Domain;
import com.ca.apm.automation.action.flow.em.config.ModifyDomainsXmlFlowContext.Grant;
import com.ca.apm.automation.action.flow.em.config.ModifyDomainsXmlFlowContext.LinuxBuilder;
import com.ca.apm.test.em.util.RoleUtility;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.artifact.IBuiltArtifact.ArtifactPlatform;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.CronEntryRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.ManagementModuleRole;
import com.ca.tas.role.PhantomJSRole;
import com.ca.tas.role.TIMAttendeeRole;
import com.ca.tas.role.TIMRole;
import com.ca.tas.role.testapp.custom.TradeServiceAppRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

import static com.ca.apm.testbed.BrowserPropertyHelper.handleBrowserProperty;
import static com.ca.tas.testbed.TestBedUtils.createLinuxMachine;
import static com.ca.tas.testbed.TestBedUtils.createWindowsMachine;
import static java.lang.String.format;

@TestBedDefinition
public class TeamCenterRegressionTestBed implements ITestbedFactory {
    private static final Logger log = LoggerFactory.getLogger(TeamCenterRegressionTestBed.class);

    public static final String MACHINE_ID_GATEWAY = "gateway";
    public static final String MACHINE_ID_END_USER = "endUserMachine";
    public static final String MACHINE_ID_TRADE_SERVICE_APP = "tradeServiceApp";

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        TestbedMachine timMachine
            = createLinuxMachine(MACHINE_ID_GATEWAY, ITestbedMachine.TEMPLATE_CO66);

        TIMRole timRole = new TIMRole.Builder("tim", tasResolver).build();
        timMachine.addRole(timRole);

        // setup end user machine where the user activities are done/test scenarios are run
        TestbedMachine endUserMachine
            = createWindowsMachine(MACHINE_ID_END_USER, ITestbedMachine.TEMPLATE_W64);

        TestbedMachine tradeServiceAppMachine
            = createWindowsMachine(MACHINE_ID_TRADE_SERVICE_APP, ITestbedMachine.TEMPLATE_W64);

        // install Introscope with complete list of features
        EmRole.Builder emBuilder = new EmRole.Builder("introscope", tasResolver)
            .dbpassword("quality")
            .nostartEM();
        EmRole introscopeStandaloneRole = emBuilder.build();
        endUserMachine.addRole(introscopeStandaloneRole);

        String emBaseDir = introscopeStandaloneRole.getEnvPropertyById(EmRole.ENV_PROPERTY_INSTALL_DIR);

        // Configure users for EM
        LocalRealmUsersFlowContext usersFlowContext = createUserFlowContext(emBaseDir);
        ExecutionRole addLocalUsersRole = new ExecutionRole.Builder("createUsers")
            .flow(LocalRealmUsersFlow.class, usersFlowContext)
            .build();
        addLocalUsersRole.after(introscopeStandaloneRole);
        endUserMachine.addRole(addLocalUsersRole);

        // Configure Domains for EM
        ModifyDomainsXmlFlowContext
            domainsFlowContext = createDomainFlowContext(emBaseDir);
        ExecutionRole addDomainsRole = new ExecutionRole.Builder("modifyDomains")
            .flow(ModifyDomainsXmlFlow.class, domainsFlowContext)
            .build();
        addDomainsRole.after(introscopeStandaloneRole);
        endUserMachine.addRole(addDomainsRole);

        // install MM to EM
        String emInstallDir = introscopeStandaloneRole.getInstallDir();
        log.info("EM install dir: {}", emInstallDir);
        ManagementModuleRole mmRole = new ManagementModuleRole("status_mm", "/StatusTestMM.jar",
                emInstallDir);
        mmRole.after(introscopeStandaloneRole);
        endUserMachine.addRole(mmRole);

        // Start EM
        ExecutionRole emStartRole =
            new ExecutionRole.Builder(introscopeStandaloneRole.getRoleId() + "_start")
                .asyncCommand(introscopeStandaloneRole.getEmRunCommandFlowContext())
                .build();
        emStartRole.after(addLocalUsersRole,addDomainsRole, mmRole);
        endUserMachine.addRole(emStartRole);

        PhantomJSRole phantomjsRole = new PhantomJSRole.Builder("phantomjs", tasResolver).build();
        endUserMachine.addRole(phantomjsRole);

        TIMAttendeeRole eumTimAttendeeRole =
                new TIMAttendeeRole.Builder("eum-tim-attendee", timRole, tasResolver)
                        .build();
        endUserMachine.addRole(eumTimAttendeeRole);

        endUserMachine.addRemoteResource(RemoteResource.createFromRegExp(".*screenshots.*", RemoteResource.TEMP_FOLDER));

        // setup testing machines

        JavaRole tomcatJavaRole = new JavaRole.Builder("tomcatJavaRole", tasResolver)
            .version(JavaBinary.WINDOWS_64BIT_JDK_18)
            .build();
        TomcatRole tomcatRole = new TomcatRole.Builder("tomcat60", tasResolver)
            .tomcatVersion(TomcatVersion.v60)
            .tomcatCatalinaPort(7080)
            .customJava(tomcatJavaRole)
            .installDir("C:/sw/apache-tomcat-6.0.36")
            .build();
        tomcatJavaRole.after(tomcatJavaRole);
        tradeServiceAppMachine.addRole(tomcatRole, tomcatJavaRole);

        AgentRole agentRole = new AgentRole.Builder("agent-tomcat", tasResolver)
                .webAppServer(tomcatRole)
                .platform(ArtifactPlatform.WINDOWS)
                .emRole(introscopeStandaloneRole)
                .intrumentationLevel(AgentInstrumentationLevel.TYPICAL)
                .webAppAutoStart()
                .build();
        // TODO pospa02: avoid cyclic dependency as AgentRole defined dependency in other way
        // agentRole.after(tomcatRole);
        tradeServiceAppMachine.addRole(agentRole);

        // HACK: currently has to be defined after AgentRole to ensure it won't be overridden
        TradeServiceAppRole tradeServiceAppRole =
                new TradeServiceAppRole.Builder("trade-service", tasResolver)
                        .tomcatRole(tomcatRole).build();

        tradeServiceAppMachine.addRole(tradeServiceAppRole);

        TIMAttendeeRole tsTimAttendeeRole =
                new TIMAttendeeRole.Builder("tsapp-tim-attendee", timRole, tasResolver).build();
        tradeServiceAppMachine.addRole(tsTimAttendeeRole);

        // every 5 minutes downloads all links from TradeService
        String cronEntry = "*/5 * * * * root wget -r --delete-after -nd http://" +
                tasResolver.getHostnameById(tomcatRole.getRoleId()) +
                ":7080/TradeService/ 2>/dev/null";
        AbstractRole cronTradeserviceRole = new CronEntryRole("cron_tradeservice", cronEntry);
        timMachine.addRole(cronTradeserviceRole);

        // every minute downloads login
        cronEntry = "* * * * * root wget --delete-after -nd http://" +
                tasResolver.getHostnameById(tomcatRole.getRoleId()) +
                ":7080/AuthenticationService/ServletA6 2>/dev/null";
        timMachine.addRole(new CronEntryRole("cron_login", cronEntry));

        // every even minute downloads TradeOptions 4 times
        String wgetStr = "wget --delete-after -nd http://" +
                tasResolver.getHostnameById(tomcatRole.getRoleId()) +
                ":7080/TradeService/TradeOptions 2>/dev/null";
        cronEntry = "*/2 * * * * root " + wgetStr + "\n" +
                "*/2 * * * * root sleep 15; " + wgetStr + "\n" +
                "*/2 * * * * root sleep 30; " + wgetStr + "\n" +
                "*/2 * * * * root sleep 45; " + wgetStr;
        timMachine.addRole(new CronEntryRole("cron_trade_options", cronEntry));

        // Remote Selenium Grid
        SeleniumGridMachinesFactory seleniumGridMachinesFactory = new SeleniumGridMachinesFactory();
        Collection<ITestbedMachine> seleniumGridMachines = seleniumGridMachinesFactory.createMachines(tasResolver);

        Collection<ITestbedMachine> testBedMachines = new ArrayList<>(4);
        testBedMachines.add(tradeServiceAppMachine);
        testBedMachines.add(timMachine);
        testBedMachines.addAll(seleniumGridMachines);

        ITestbed testbed = new Testbed(this.getClass().getSimpleName());
        testbed.addMachine(endUserMachine);
        testbed.addMachines(testBedMachines);

        testbed.addProperty("test.applicationBaseURL",
            format("http://%s:8082", tasResolver.getHostnameById("introscope")));
        testbed.addProperty("machine.gateway.ip_v4", RoleUtility.getIp(tasResolver.getHostnameById(timRole.getRoleId())));

        handleBrowserProperty(testbed, this.getClass().getClassLoader());

        return testbed;
    }

    private ModifyDomainsXmlFlowContext createDomainFlowContext(String emBaseDir) {
        Builder domainsFlowContextBuilder =
            new LinuxBuilder().emBase(emBaseDir);
        Domain tomcatDomain = new Domain("TomcatDomain");
        tomcatDomain.addAgentSpecifier("(.*)Tomcat Agent");
        tomcatDomain.addGrant(new Grant(Grant.Principal.USER,"reader","read"));
        tomcatDomain.addGrant(new Grant(Grant.Principal.GROUP,"readers","read"));
        tomcatDomain.addGrant(new Grant(Grant.Principal.GROUP,"ghost","read"));
        tomcatDomain.addGrant(new Grant(Grant.Principal.USER,"editor","write"));
        tomcatDomain.addGrant(new Grant(Grant.Principal.GROUP,"editors","write"));
        tomcatDomain.addGrant(new Grant(Grant.Principal.USER,"manager","full"));
        tomcatDomain.addGrant(new Grant(Grant.Principal.GROUP,"managers","full"));
        tomcatDomain.addGrant(new Grant(Grant.Principal.USER,"toggler","full"));
        tomcatDomain.addGrant(new Grant(Grant.Principal.GROUP,"togglers","full"));
        domainsFlowContextBuilder.domain(tomcatDomain);
        return domainsFlowContextBuilder.build();
    }

    private LocalRealmUsersFlowContext createUserFlowContext(String emBaseDir) {
        LocalRealmUsersFlowContext.Builder usersFlowContextBuilder =
            new LocalRealmUsersFlowContext.LinuxBuilder().emBase(emBaseDir);
        usersFlowContextBuilder.user("reader");
        usersFlowContextBuilder.user("editor");
        usersFlowContextBuilder.user("manager");
        usersFlowContextBuilder.user("toggler");
        usersFlowContextBuilder.user("ghost");
        usersFlowContextBuilder.user("neverlogin");
        usersFlowContextBuilder.group("readers", "reader");
        usersFlowContextBuilder.group("editors", "editor");
        usersFlowContextBuilder.group("managers", "manager");
        usersFlowContextBuilder.group("togglers", "toggler");
        usersFlowContextBuilder.group("ghost", "neverlogin");
        usersFlowContextBuilder.group("ghost", "ghost");
        return usersFlowContextBuilder.build();
    }
}
