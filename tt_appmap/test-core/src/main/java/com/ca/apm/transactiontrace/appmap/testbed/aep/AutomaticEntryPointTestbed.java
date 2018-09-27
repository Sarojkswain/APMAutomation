/*
 * Copyright (c) 2016 CA. All rights reserved.
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
package com.ca.apm.transactiontrace.appmap.testbed.aep;

import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.web.WebAppRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;
import org.eclipse.aether.artifact.DefaultArtifact;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author bhusu01
 */
@TestBedDefinition
public class AutomaticEntryPointTestbed implements ITestbedFactory {

    public static final String MACHINE_ID_EM = "emMachine";

    private static final String QA_TEST_APP_GROUP_ID = "com.ca.apm.coda-projects.test-tools";
    private static final String QA_TEST_APP_ARTIFACT_ID = "qatestapp";
    private static final String QA_TEST_APP_VERSION = "99.99.dev-SNAPSHOT";

    public static final String ROLE_ID_EM = "standAloneRole";
    private static final String ROLE_ID_QA_TEST_APP = "qaTestAppRole";
    private static final String CONTEXT_NAME_QA_TA = "QATestApp";
    public static final String ROLE_ID_TOMCAT = "tomcatRole";
    private static final String ROLE_ID_AGENT = "agentRole";

    // This is temporary for testing before copy up. This should be changed after copy up to use
    // 99.99.dev branch
    private static final String AGENT_VERSION = "99.99.leo_backend_C4154-SNAPSHOT";

    private static final String TRANSACTION_TRACES_FAST_BUFFER_PROPERTY =
        "introscope.enterprisemanager.transactiontrace.arrivalbuffer.incubationtime.fast";
    private static final String FAST_BUFFER_FAST_TIME = "30";

    public static final String ADMIN_AUX_TOKEN = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    public static final String ADMIN = "admin";

    public static final Collection<String> EM_LAXNL_JAVA_OPTION = Arrays.asList(
        "-Dappmap.token=" + ADMIN_AUX_TOKEN, "-Dappmap.user=" + ADMIN,
        "-Dcom.wily.assert=false", "-XX:+HeapDumpOnOutOfMemoryError", "-verbosegc", "-Xdebug",
        "-Xrunjdwp:server=y,transport=dt_socket,address=20555,suspend=n");

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        ITestbed testbed = new Testbed(AutomaticEntryPointTestbed.class.getSimpleName());
        /* -- Initialize machines -- */
        ITestbedMachine emMachine = TestBedUtils.createWindowsMachine(MACHINE_ID_EM,
            ITestbedMachine.TEMPLATE_W64);
        /* -- Declare roles -- */
        EmRole emRole;
        AgentRole agentRole;
        TomcatRole tomcatRole;
        WebAppRole webAppRole;
        /* -- Define roles -- */

        emRole = new EmRole.Builder(ROLE_ID_EM, tasResolver)
            .configProperty(TRANSACTION_TRACES_FAST_BUFFER_PROPERTY, FAST_BUFFER_FAST_TIME)
            .emLaxNlJavaOption(EM_LAXNL_JAVA_OPTION)
            .nostartEM()
            .nostartWV()
            .build();

        DefaultArtifact qaTestAppArtifact = new DefaultArtifact(QA_TEST_APP_GROUP_ID,
            QA_TEST_APP_ARTIFACT_ID, "jvm7-tomcatnodb", "war" , QA_TEST_APP_VERSION);

        webAppRole = new WebAppRole.Builder<TomcatRole>(ROLE_ID_QA_TEST_APP).artifact
            (qaTestAppArtifact).cargoDeploy().contextName(CONTEXT_NAME_QA_TA).build();

        emMachine.getDefaultJavaHome();

        tomcatRole = new TomcatRole.Builder(ROLE_ID_TOMCAT, tasResolver).tomcatVersion(TomcatVersion.v70)
            .webApp(webAppRole)
            .build();

        Map<String, String> agentAdditionalProps = new HashMap<>();
        agentAdditionalProps.put("log4j.logger.IntroscopeAgent", "INFO, logfile");
        agentAdditionalProps.put("introscope.autoprobe.dynamicinstrument.enabled", "true");
        agentAdditionalProps.put("introscope.agent.deep.entrypoint.enabled", "true");
        agentAdditionalProps.put("introscope.agent.deep.trace.enabled", "true");
        agentAdditionalProps.put("introscope.agent.deep.instrumentation.enabled", "true");

        agentAdditionalProps.put("introscope.agent.deep.trace.entrypoint.candidate.transaction"
            + ".count", "5");
        agentAdditionalProps.put("introscope.agent.deep.entrypoint.log.stackTrace.enabled", "true");
        agentAdditionalProps.put("log4j.logger.IntroscopeAgent.EntryPointDetection",
            "TRACE#com.wily.util.feedback.Log4JSeverityLevel, logfile");

        agentRole = new AgentRole.Builder(ROLE_ID_AGENT, tasResolver)
            .version(AGENT_VERSION)
            .additionalProperties(agentAdditionalProps)
            .webAppServer(tomcatRole)
            .emRole(emRole)
            .build();

        /* -- Role orchestration -- */
        /* -- Map roles to machines -- */
        emMachine.addRole(emRole, webAppRole, tomcatRole, agentRole);
        /* -- Add machines to testbed -- */
        testbed.addMachine(emMachine);
        return testbed;
    }
}
