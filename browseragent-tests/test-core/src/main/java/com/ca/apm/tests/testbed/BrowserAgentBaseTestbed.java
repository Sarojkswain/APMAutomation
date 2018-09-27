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

import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.apm.tests.artifact.ChromeDriverArtifact;
import com.ca.apm.tests.artifact.IEDriverServerArtifact;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AgentRole;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.AgentCapable;
import com.ca.tas.testbed.ITestbedFactory;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.aether.artifact.DefaultArtifact;

@TestBedDefinition
public abstract class BrowserAgentBaseTestbed implements ITestbedFactory {

    // protected final Map<String, String> agentAdditionalProps = new HashMap<>();

    protected static final String JAVA7_ROLE_ID = "java7Role";
    protected static final String JAVA8_ROLE_ID = "java8Role";

    public static final String TOMCAT_ROLE_ID = "tomcatRole";
    public static final String TOMCAT_AGENT_ROLE_ID = "tomcatAgentRole";
    public static final String TOMCAT_AGENT_START_ROLE_ID = "startUpTomcat";
    public static final int TOMCAT_PORT = 8086;

    public static final String WAS_ROLE_ID = "WASRole";
    public static final String WAS_AGENT_ROLE_ID = "WASAgentRole";
    public static final String WAS_AGENT_START_ROLE_ID = "startUpWAS";
    public static final int WAS_PORT = 9086;

    public static final String EM_ROLE_ID = "emRole";
    public static final String ENABLE_CEM_API_ROLE_ID = "enableCemApiRole";
    public static final String START_EM_ROLE_ID = "startEmRole";
    public static final String CLW_ROLE_ID = "clwRole";

    public static final String EM_MACHINE_ID = "emMachine";
    public static final String BROWSERAGENT_MACHINE_ID = "browserAgentMachine";

    protected static final String EMAIL_RECIPIENTS_ENV_VAR = "gupra04@ca.com";

    public static final String BROWSERAGENT_ROLE_ID = "browserAgentRole";

    public static final String IE_SELENIUM_DRIVER_ROLE_ID = "seleniumDriverRoleIE";
    public static final String CHROME_SELENIUM_DRIVER_ROLE_ID = "seleniumDriverRoleChrome";

    protected static final String CLW_JAR_FILE = "CLWorkstation.jar";

    // Used for name to be assigned after artifact is copied to machine.
    protected static final String BRTM_TEST_APP_CONTEXT = "brtmtestapp";
    protected static final String CLICK_LISTENER_TEST_2_CONTEXT = "ClicklistenerTest_2";
    protected static final String DEMO_WEB_APP_CONTEXT = "demowebapp";
    protected static final String SESSION_TEST_CONTEXT = "SessionTest";
    protected static final String URCHIN_TRACKER_CONTEXT = "UrchinTracker";
    protected static final String DWR_CONTEXT = "dwr";
    protected static final String INFI_CONTEXT = "infi";

    protected String separator;
    protected String softwareLocation;

    protected JavaBinary java7Binary;
    protected JavaBinary java8Binary;

    protected String ieSeleniumDriver;
    protected String chromeSeleniumDriver;

    protected String agentPblFile;
    protected String agentName;
    protected String agentProcessName;

    protected String agentRoleId;
    protected int agentPort;
    protected String applicationServer;
    protected String browser;

    protected String clwJarHome;
    protected String seleniumDriverHome;

    protected EmRole createEMRole(ITasResolver tasResolver) {
        EmRole emRole =
            new EmRole.Builder(EM_ROLE_ID, tasResolver)
                .configProperty("introscope.enterprisemanager.hotconfig.enable", "false")
                .configProperty("introscope.enterprisemanager.performance.compressed", "false")
                .configProperty("log4j.logger.Manager.Performance", "DEBUG, performance, logfile")
                .nostartEM().nostartWV().build();

        emRole.addProperty("emPassword", "");

        return emRole;
    }

    protected AgentRole createAgentRole(ITasResolver tasResolver, AgentCapable appServerRole,
        EmRole emRole) {
        // Java agent on application server, agent not started -- BrowserAgent has to be
        // configured first

        AgentRole appServerAgentRole =
            new AgentRole.Builder(agentRoleId, tasResolver).webAppRole(appServerRole)
                .emRole(emRole).build();
        appServerAgentRole.addProperty("agentIntallLocation", appServerAgentRole
            .getAgentFlowContext().getInstallDir() + this.getSeparator() + "wily");

        return appServerAgentRole;
    }

    protected JavaRole createJava7Role(ITasResolver tasResolver) {
        JavaRole javaRole =
            new JavaRole.Builder(JAVA7_ROLE_ID, tasResolver).version(this.java7Binary).build();
        return javaRole;
    }

    protected JavaRole createJava8Role(ITasResolver tasResolver) {
        JavaRole javaRole =
            new JavaRole.Builder(JAVA8_ROLE_ID, tasResolver).version(this.java8Binary).build();
        return javaRole;
    }

    // TODO: Check if TAS supports startup for AgentCapble
    /*
     * protected ExecutionRole agentStartUpRole(ITasResolver tasResolver, AgentCapable
     * appServerRole) {
     * 
     * ExecutionRole agentStartUpRole =
     * new ExecutionRole.Builder(TOMCAT_AGENT_START_ROLE_ID).syncCommand(
     * appServerRole.getStartCmdFlowContext()).build();
     * 
     * return agentStartUpRole;
     * }
     */
    protected UniversalRole createCLWRole(ITasResolver tasResolver, String home) {

        DefaultArtifact clwJar =
            new DefaultArtifact("com.ca.apm.em", "com.wily.introscope.clw.feature", "", "jar",
                tasResolver.getDefaultVersion());

        UniversalRole clwRole =
            new UniversalRole.Builder(CLW_ROLE_ID, tasResolver).download(clwJar,
                home + "CLWorkstation.jar").build();
        clwRole.addProperty("clwJarPath", this.getCLWJarHome() + CLW_JAR_FILE);
        return clwRole;
    }

    protected UniversalRole createIESeleniumDriverRole(ITasResolver tasResolver, String path) {

        UniversalRole ieSeleniumDriverRole =
            new UniversalRole.Builder(IE_SELENIUM_DRIVER_ROLE_ID, tasResolver).download(
                IEDriverServerArtifact.ENUM_NAME.getArtifact(),
                seleniumDriverHome + ieSeleniumDriver).build();

        ieSeleniumDriverRole.addProperty("seleniumDriverHome", seleniumDriverHome);
        ieSeleniumDriverRole.addProperty("ieSeleniumDriver", ieSeleniumDriver);

        return ieSeleniumDriverRole;
    }

    protected UniversalRole createChromeSeleniumDriverRole(ITasResolver tasResolver, String path) {

        UniversalRole chromeSeleniumDriverRole =
            new UniversalRole.Builder(CHROME_SELENIUM_DRIVER_ROLE_ID, tasResolver).download(
                ChromeDriverArtifact.ENUM_NAME.getArtifact(),
                seleniumDriverHome + chromeSeleniumDriver).build();

        chromeSeleniumDriverRole.addProperty("seleniumDriverHome", seleniumDriverHome);
        chromeSeleniumDriverRole.addProperty("chromeSeleniumDriver", chromeSeleniumDriver);

        return chromeSeleniumDriverRole;
    }

    protected static String codifyPath(String path) {
        return FilenameUtils.separatorsToUnix(path);
    }

    protected String getCLWJarHome() {
        return clwJarHome;
    }

    protected String getSeparator() {
        return separator;
    }

    protected String getSoftwareLoc() {
        return softwareLocation;
    }

    protected JavaBinary getJava8Binary() {
        return java8Binary;
    }

    protected JavaBinary getJava7Binary() {
        return java7Binary;
    }

    protected String getChromeSeleniumDriver() {
        return chromeSeleniumDriver;
    }

    protected String getIESeleniumDriver() {
        return ieSeleniumDriver;
    }

    public String getAgentRoleId() {
        return agentRoleId;
    }
}
