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
import com.ca.tas.artifact.thirdParty.JBossVersion;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.web.QaAppJbossRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.JbossRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * Test bed that installs
 *
 * on a RedHat machine
 * 1. Stand Alone EM
 * 2. JBoss (+ Java)
 * 3. QATestApp
 * 4. Java agent
 * 
 * on a Windows machine
 * 1. Selenium web driver for Chrome
 */
@TestBedDefinition
public class JavaAgentJBossStandAloneTestbed implements ITestbedFactory {

    private static final String JAVA_AGENT_JBOSS_TEMPLATE_ID = ITestbedMachine.TEMPLATE_RH66;
    public static final String JAVA_AGENT_JBOSS_MACHINE = "javaAgentJBossMachine";

    public static final String QA_APP_JBOSS_ROLE_ID = "qaAppJBossRole";
    public static final String JBOSS_ROLE_ID = "jBossRole";
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
        WebAppRole<JbossRole> qaAppRole =
            new QaAppJbossRole.Builder(QA_APP_JBOSS_ROLE_ID, tasResolver).cargoDeploy()
                .contextName("qa-app").build();

        // java role
        JavaRole javaRole =
            new JavaRole.LinuxBuilder(JAVA_ROLE_ID, tasResolver).version(
                JavaBinary.LINUX_64BIT_JRE_17).build();

        // jboss role
        JbossRole jbossRole =
            new JbossRole.LinuxBuilder(JBOSS_ROLE_ID, tasResolver).version(JBossVersion.JBOSS711)
                .customJava(javaRole).addWebAppRole(qaAppRole).build();

        // EM role
        EmRole emRole =
            new EmRole.LinuxBuilder(EM_ROLE_ID, tasResolver).configProperty(
                EM_CONF_PROP_TT_ARRIVAL_BUFFER_INCUB_TIME_FAST, "30").build();

        // java agent role
        IRole agentRole =
            new AgentRole.LinuxBuilder(AGENT_ROLE_ID, tasResolver).webAppServer(jbossRole)
                .webAppAutoStart().emRole(emRole).build();

        // initiate TT session role
        DeferredInitiateTransactionTraceSessionRole traceSessionRole =
            new DeferredInitiateTransactionTraceSessionRole.LinuxBuilder(
                INITIATE_TT_SESSION_ROLE_ID).emRole(emRole).build();


        emRole.before(agentRole);

        javaRole.before(jbossRole);

        ITestbedMachine javaAgentJBossMachine =
            TestBedUtils.createLinuxMachine(JAVA_AGENT_JBOSS_MACHINE, JAVA_AGENT_JBOSS_TEMPLATE_ID,
                javaRole, qaAppRole, jbossRole, agentRole, emRole, traceSessionRole);

        SeleniumGridMachinesFactory seleniumGridMachinesFactory = new SeleniumGridMachinesFactory();
        Collection<ITestbedMachine> seleniumGridMachines = seleniumGridMachinesFactory.createMachines(tasResolver);

        return new Testbed(getClass().getSimpleName()).addMachine(javaAgentJBossMachine)
            .addMachines(seleniumGridMachines);
    }

}
