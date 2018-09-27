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

/**
 * Base Browser Agent Automation Testbed
 *
 * @author gupra04
 */

package com.ca.apm.tests.testbed;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.tests.role.EnableBrowserAgentRole;
import com.ca.apm.tests.role.EnableCemApiRole;
import com.ca.apm.tests.role.StartEMRole;
import com.ca.apm.tests.test.BrowserAgentBaseTest;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.AgentCapable;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public abstract class BrowserAgentGenericAppServerWinTestbed extends BrowserAgentBaseTestbed {

    protected static final Logger LOGGER = LoggerFactory.getLogger(BrowserAgentBaseTest.class);

    protected static final String WIN_TEMPLATE_ID = ITestbedMachine.TEMPLATE_W64;
    public static final String WIN_EM_MACHINE_ID = "emMachine";
    public static final String WIN_BROWSERAGENT_MACHINE_ID = "browserAgentMachine";

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        // set properties to Windows specific values
        separator = TasBuilder.WIN_SEPARATOR;
        softwareLocation = TasBuilder.WIN_SOFTWARE_LOC;
        ieSeleniumDriver = "IEDriverServer.exe";
        chromeSeleniumDriver = "chromedriver.exe";
        java8Binary = JavaBinary.WINDOWS_64BIT_JDK_18;
        java7Binary = JavaBinary.WINDOWS_64BIT_JDK_17;
        LOGGER.info("WINDOWS - separator ++++++: = " + separator);

        JavaRole java8Role = createJava8Role(tasResolver);
        // TODO:: Java8Role is not used. Waiting for a fix for defect DE134564
        AgentCapable appServerRole = getWebAppServerRole(tasResolver, java8Role);

        EmRole emRole = createEMRole(tasResolver);

        // Enable cem api & start em/webview
        EnableCemApiRole enableCemApiRole =
            new EnableCemApiRole.Builder(ENABLE_CEM_API_ROLE_ID, tasResolver)
                .emHomeDir(codifyPath(emRole.getDeployEmFlowContext().getInstallDir()))
                .emInstallVersion(tasResolver.getDefaultVersion()).build();
        StartEMRole startEMRole =
            new StartEMRole.Builder(START_EM_ROLE_ID, tasResolver).emHomeDir(
                codifyPath(emRole.getDeployEmFlowContext().getInstallDir())).build();

        emRole.before(enableCemApiRole, startEMRole);
        enableCemApiRole.before(startEMRole);

        UniversalRole clwRole = createCLWRole(tasResolver, clwJarHome);

        UniversalRole ieSeleniumDriverRole =
            createIESeleniumDriverRole(tasResolver, seleniumDriverHome);
        UniversalRole chromeSeleniumDriverRole =
            createChromeSeleniumDriverRole(tasResolver, seleniumDriverHome);

        AgentRole appServerAgentRole = createAgentRole(tasResolver, appServerRole, emRole);

        EnableBrowserAgentRole browserAgentRole =
            new EnableBrowserAgentRole.Builder(BROWSERAGENT_ROLE_ID, tasResolver)
                .agentInstallDir(appServerAgentRole.getAgentFlowContext().getInstallDir())
                .appServerPblFile(agentPblFile).agentName(agentName)
                .agentProcessName(agentProcessName).build();

        browserAgentRole.addProperty("appServer", applicationServer);
        browserAgentRole.addProperty("appServerPort", agentPort);
        browserAgentRole.addProperty("appServerHome", appServerRole.getInstallDir());
        this.setBrowser();
        browserAgentRole.addProperty("browser", browser);

        // Start Agent after agent agent configuration
        // ExecutionRole agentStartUpRole = createagentStartUpRole(tasResolver, appServerRole);
        // agentStartUpRole.after(tomcatRole, browserAgentRole);

        // Uncomment next 2 lines to change to 2 machine testbed
        // ITestbedMachine emMachine =
        // TestBedUtils.createWindowsMachine(EM_MACHINE_ID, WIN_TEMPLATE_ID, emRole,
        // enableCemApiRole, startEMRole);
        ITestbedMachine browserAgentMachine =
            TestBedUtils.createWindowsMachine(BROWSERAGENT_MACHINE_ID, WIN_TEMPLATE_ID, emRole,
                enableCemApiRole, startEMRole, java8Role, appServerRole, appServerAgentRole,
                ieSeleniumDriverRole, chromeSeleniumDriverRole, browserAgentRole, // agentStartUpRole,
                clwRole);

        ITestbed testbed = new Testbed(getClass().getSimpleName());
        testbed.addMachine(browserAgentMachine);
        // Uncomment to change to 2 machine testbed
        // testbed.addMachine(emMachine);

        Map<String, String> env = System.getenv();
        LOGGER.info("*** Environment variables ***");
        for (String envName : env.keySet()) {
            LOGGER.info(envName + "=" + env.get(envName));
        }

        String emailRecipients = env.get(EMAIL_RECIPIENTS_ENV_VAR);

        if (emailRecipients != null) {
            testbed.addProperty(EMAIL_RECIPIENTS_ENV_VAR, emailRecipients);
        }

        return testbed;
    }

    protected abstract AgentCapable getWebAppServerRole(ITasResolver tasResolver, JavaRole javaRole);

    protected abstract void setBrowser();
    // protected abstract ExecutionRole createagentStartUpRole(ITasResolver tasResolver,
    // AgentCapable
    // agentRole);

}
