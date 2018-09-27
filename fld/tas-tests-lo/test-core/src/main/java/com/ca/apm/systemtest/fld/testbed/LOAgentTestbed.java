/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and proprietary and shall not
 * be duplicated, used, disclosed or disseminated in any way except as authorized by the applicable
 * license agreement, without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT PERMITTED BY APPLICABLE
 * LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE
 * USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.systemtest.fld.testbed;


import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;
import com.ca.apm.systemtest.fld.role.ActiveMqRole;
import com.ca.apm.systemtest.fld.role.AgentRole;
import com.ca.apm.systemtest.fld.role.ChromeBrowserRole;
import com.ca.apm.systemtest.fld.role.ChromeDriverRole;
import com.ca.apm.systemtest.fld.role.LoadOrchestratorRole;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * LoadOrchestrator-Agent testbed - environment is running and prepared for activity flows.
 *
 * @author filja01
 */
@TestBedDefinition
public class LOAgentTestbed implements ITestbedFactory {

    public static final String TEST_MACHINE_ID = "testMachine";
    public static final String LO_ROLE_ID = "orchestratorRole";
    public static final String TC_ROLE_ID = "tomcatRole";
    public static final String AMQ_ROLE_ID = "activeMqRole";
    public static final String AGENT_ROLE_ID = "agentRole";
    public static final String DRIVER_ROLE_ID = "driverRole";
    public static final String CHROME_ROLE_ID = "chromeRole";

    public static final String TOMCAT_USERS =
        "<?xml version='1.0' encoding='utf-8'?>\n"
            + "<tomcat-users>\n"
            + "<role rolename=\"tomcat\" />\n"
            + "<role rolename=\"manager-gui\" />\n"
            + "<role rolename=\"manager-script\" />\n"
            + "<role rolename=\"admin-gui\" />\n"
            + "<role rolename=\"admin\" />\n"
            + "<user username=\"andy\" password=\"99Ball00ns\" "
            + "roles=\"tomcat,manager-gui,admin-gui,manager-script,admin\" />\n"
            + "<user username=\"andycon\" password=\"99Ball00ns\" roles=\"manager-script\" />\n"
            + "</tomcat-users>\n";

    @Override
    public ITestbed create(ITasResolver tasResolver) {

        ITestbedMachine testMachine =
            new TestbedMachine.Builder(TEST_MACHINE_ID).templateId("w64").build();

        IRole tomcatRole =
            new TomcatRole.Builder(TC_ROLE_ID, tasResolver)
                .installDir("C:\\sw\\tomcat")
                .tomcatVersion(TomcatVersion.v80)
                .autoStart()
                .addConfigFile("tomcat-users.xml",
                    Arrays.asList(StringUtils.split(TOMCAT_USERS, '\n')))
                .addConfigFile("fld.properties", Collections.singletonList(
                    "activemq.broker.url=tcp://localhost:61616\n"))
                .build();

        testMachine.addRole(tomcatRole);

        IRole activeMqRole = new ActiveMqRole(AMQ_ROLE_ID, "C:\\sw");
        testMachine.addRole(activeMqRole);

        IRole loadOrchestratorRole =
            new LoadOrchestratorRole(LO_ROLE_ID, tomcatRole.getEnvProperties().get("tomcat.home")
                + "\\webapps\\LoadOrchestrator.war");
        testMachine.addRole(loadOrchestratorRole);

        IRole agentRole = new AgentRole(AGENT_ROLE_ID, "C:\\sw\\agent", "tcp://localhost:61616",
            null, OperatingSystemFamily.Windows);
        testMachine.addRole(agentRole);

        IRole driverRole = new ChromeDriverRole(DRIVER_ROLE_ID,
            "C:\\Install\\selenium\\chromedriver.exe");
        testMachine.addRole(driverRole);

        IRole chromeRole = new ChromeBrowserRole(CHROME_ROLE_ID, "C:\\sw\\chromeinstaller.exe");
        testMachine.addRole(chromeRole);

        ITestbed testbed = new Testbed("LoadOrchestratorAgentTestbed");
        testbed.addMachine(testMachine);

        return testbed;
    }
}
