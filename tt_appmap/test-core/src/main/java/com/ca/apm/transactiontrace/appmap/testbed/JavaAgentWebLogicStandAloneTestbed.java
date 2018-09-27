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

package com.ca.apm.transactiontrace.appmap.testbed;

import java.util.Collection;

import com.ca.apm.transactiontrace.appmap.role.DeferredInitiateTransactionTraceSessionRole;
import com.ca.tas.artifact.built.QaApp;
import com.ca.tas.artifact.thirdParty.WebLogicVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.linux.YumInstallGlibcI686Role;
import com.ca.tas.role.web.QaAppWebLogicRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.WebLogicRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Test bed that installs
 *
 * on a CentOS machine
 * 1. Stand Alone EM
 * 2. WebLogic (+ GLIBC)
 * 3. QATestApp
 * 4. Java agent
 * 
 * on a Windows machine
 * 1. Selenium web driver for Chrome
 */
@TestBedDefinition
public class JavaAgentWebLogicStandAloneTestbed implements ITestbedFactory {

    private static final String JAVA_AGENT_WEBLOGIC_TEMPLATE_ID = ITestbedMachine.TEMPLATE_CO66;
    public static final String JAVA_AGENT_WEBLOGIC_MACHINE = "javaAgentWeblogicMachine";

    public static final String QA_APP_WEBLOGIC_ROLE_ID = "qaAppWebLogicRole";
    public static final String WEBLOGIC_ROLE_ID = "webLogicRole";
    public static final String AGENT_ROLE_ID = "agentRole";
    public static final String JAVA_ROLE_ID = "javaRole";
    public static final String GLIBC_ROLE_ID = "glibcRole";

    // ---

    public static final String EM_ROLE_ID = "emRole";
    public static final String INITIATE_TT_SESSION_ROLE_ID = "inititateTTSessionRole";

    private static final String EM_CONF_PROP_TT_ARRIVAL_BUFFER_INCUB_TIME_FAST =
        "introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.fast";

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        // qa app role
        WebAppRole<WebLogicRole> qaAppRole =
            new QaAppWebLogicRole.Builder(QA_APP_WEBLOGIC_ROLE_ID, tasResolver)
                .qaApp(
                    QaApp.createWithoutDatabase(tasResolver, QaApp.Jvm.jvm6,
                        QaApp.WebServer.GENERIC)).cargoDeploy().contextName("qa-app").build();

        // glibc role
        YumInstallGlibcI686Role glibcRole = new YumInstallGlibcI686Role(GLIBC_ROLE_ID);

        // weblogic role
        WebLogicRole webLogicRole =
            new WebLogicRole.LinuxBuilder(WEBLOGIC_ROLE_ID, tasResolver)
                .version(WebLogicVersion.v1035x86linux).addWebAppRole(qaAppRole).build();

        // EM role
        EmRole emRole =
            new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver).configProperty(
                EM_CONF_PROP_TT_ARRIVAL_BUFFER_INCUB_TIME_FAST, "30").build();

        // java agent role
        IRole agentRole =
            new AgentRole.LinuxBuilder(AGENT_ROLE_ID, tasResolver).webAppServer(webLogicRole)
                .webAppAutoStart().emRole(emRole).build();

        // initiate TT session role
        DeferredInitiateTransactionTraceSessionRole traceSessionRole =
            new DeferredInitiateTransactionTraceSessionRole.LinuxBuilder(
                INITIATE_TT_SESSION_ROLE_ID).emRole(emRole).build();

        emRole.before(agentRole);
        webLogicRole.after(glibcRole);
        qaAppRole.after(webLogicRole);

        ITestbedMachine javaAgentJBossMachine =
            TestBedUtils.createLinuxMachine(JAVA_AGENT_WEBLOGIC_MACHINE,
                JAVA_AGENT_WEBLOGIC_TEMPLATE_ID, glibcRole, qaAppRole, webLogicRole, agentRole,
                emRole, traceSessionRole);

        SeleniumGridMachinesFactory seleniumGridMachinesFactory = new SeleniumGridMachinesFactory();
        Collection<ITestbedMachine> seleniumGridMachines = seleniumGridMachinesFactory.createMachines(tasResolver);

        return new Testbed(getClass().getSimpleName()).addMachine(javaAgentJBossMachine)
            .addMachines(seleniumGridMachines);
    }

}
