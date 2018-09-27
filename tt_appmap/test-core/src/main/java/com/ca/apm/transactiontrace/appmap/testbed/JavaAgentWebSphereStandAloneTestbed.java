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
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.web.QaAppWebSphereRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.web.QaAppWebSphereRole.Builder;
import com.ca.tas.role.webapp.IWebSphereRole;
import com.ca.tas.role.webapp.WebSphereRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Test bed that installs
 *
 * on a Windows machine
 * 1. Stand Alone EM
 * 2. WebSphere
 * 3. QATestApp
 * 4. Java agent
 * 
 * on a Windows machine
 * 1. Selenium web driver for Chrome
 */
@TestBedDefinition
public class JavaAgentWebSphereStandAloneTestbed implements ITestbedFactory {

    private static final String JAVA_AGENT_TEMPLATE_ID = ITestbedMachine.TEMPLATE_W64;
    public static final String JAVA_AGENT_WEBSPHERE_MACHINE = "javaAgentWebSphereMachine";

    public static final String QA_APP_WEBSPHERE_ROLE_ID = "qaAppWebSphereRole";
    public static final String WEBSPHERE_ROLE_ID = "webSphereRole";
    public static final String AGENT_ROLE_ID = "agentRole";
    public static final String JAVA_ROLE_ID = "javaRole";

    // ---

    public static final String EM_ROLE_ID = "emRole";
    public static final String INITIATE_TT_SESSION_ROLE_ID = "inititateTTSessionRole";

    private static final String EM_CONF_PROP_TT_ARRIVAL_BUFFER_INCUB_TIME_FAST =
        "introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.fast";

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        // qa app role
        Builder qaAppBuilder = new QaAppWebSphereRole.Builder(QA_APP_WEBSPHERE_ROLE_ID, tasResolver);
        // The generic ear should work, but there appears to be an issue with the deployment
        // descriptor XML
        qaAppBuilder.qaApp(QaApp.createWithDatabase(tasResolver, QaApp.Jvm.jvm6, QaApp.WebServer.TOMCAT));
        qaAppBuilder.wsAdminJavaOption("-Xmx512m");
        WebAppRole<IWebSphereRole> qaAppRole = qaAppBuilder.build();

        // websphere role
        WebSphereRole webSphereRole =
            new WebSphereRole.Builder(WEBSPHERE_ROLE_ID, tasResolver).addWebAppRole(qaAppRole)
                .build();

        // EM role
        EmRole emRole =
            new EmRole.Builder(EM_ROLE_ID, tasResolver).configProperty(
                EM_CONF_PROP_TT_ARRIVAL_BUFFER_INCUB_TIME_FAST, "30").build();

        // java agent role
        IRole agentRole =
            new AgentRole.Builder(AGENT_ROLE_ID, tasResolver).webAppServer(webSphereRole)
                .webAppAutoStart().emRole(emRole).build();

        // initiate TT session role
        DeferredInitiateTransactionTraceSessionRole traceSessionRole =
            new DeferredInitiateTransactionTraceSessionRole.Builder(INITIATE_TT_SESSION_ROLE_ID)
                .emRole(emRole).build();

        ITestbedMachine javaAgentWebSphereMachine =
            TestBedUtils.createWindowsMachine(JAVA_AGENT_WEBSPHERE_MACHINE,
                JAVA_AGENT_TEMPLATE_ID, qaAppRole, webSphereRole, agentRole, emRole,
                traceSessionRole);

        emRole.before(agentRole);

        SeleniumGridMachinesFactory seleniumGridMachinesFactory = new SeleniumGridMachinesFactory();
        Collection<ITestbedMachine> seleniumGridMachines = seleniumGridMachinesFactory.createMachines(tasResolver);

        return new Testbed(getClass().getSimpleName()).addMachine(javaAgentWebSphereMachine)
            .addMachines(seleniumGridMachines);
    }

}
