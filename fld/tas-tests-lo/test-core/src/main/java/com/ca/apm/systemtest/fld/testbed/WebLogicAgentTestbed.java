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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.ca.apm.systemtest.fld.role.ActiveMqRole;
import com.ca.apm.systemtest.fld.role.ActivitiRole;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.artifact.thirdParty.WebLogicVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.role.webapp.WebLogicRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * WebLogic testbed with activiti framework prepared.
 *
 * @author jirji01
 */
@TestBedDefinition
public class WebLogicAgentTestbed implements ITestbedFactory {

    public static final String TEST_MACHINE_ID = "testMachine";
    public static final String WL_ROLE_ID = "weblogicRole";
    public static final String TC_ROLE_ID = "tomcatRole";
    public static final String AMQ_ROLE_ID = "activeMqRole";
    public static final String ACT_ROLE_ID = "activitiRole";

    private static final String TOMCAT_USERS =
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
            new TestbedMachine.Builder(TEST_MACHINE_ID).templateId("w64t").build();

        Set<String> customComponentPaths = new HashSet<>(10);
        customComponentPaths.add("WebLogic Server/Core Application Server");
        customComponentPaths.add("WebLogic Server/Administration Console");
        customComponentPaths.add("WebLogic Server/Configuration Wizard and Upgrade Framework");
        customComponentPaths.add("WebLogic Server/Web 2.0 HTTP Pub-Sub Server");
        customComponentPaths.add("WebLogic Server/WebLogic JDBC Drivers");
        customComponentPaths.add("WebLogic Server/Third Party JDBC Drivers");
        customComponentPaths.add("WebLogic Server/WebLogic Server Clients");
        customComponentPaths.add("WebLogic Server/WebLogic Web Server Plugins");
        customComponentPaths.add("WebLogic Server/UDDI and Xquery Support");
        customComponentPaths.add("WebLogic Server/Server Examples");


        IRole wlRole =
            new WebLogicRole.Builder(WL_ROLE_ID, tasResolver)
                .installLocation("C:\\sw\\wily\\weblogic")
                .installLogFile("C:\\sw\\wily\\weblogic\\install.log")
                .version(WebLogicVersion.v103x86w)
                .responseFileDir("C:\\sw\\wily\\weblogic\\responseFiles")
                .installDir("C:\\sw\\wily\\weblogic\\wlserver_10.3")
                .nodeManagerPort(111)
                .customComponentPaths(customComponentPaths)
//                .installNodeManagerService()
                .build();

        testMachine.addRole(wlRole);

        IRole tomcatRole =
            new TomcatRole.Builder(TC_ROLE_ID, tasResolver)
                .installDir("C:\\sw\\wily\\tomcat")
                .tomcatVersion(TomcatVersion.v80)
                .autoStart()
                .addConfigFile("tomcat-users.xml", Arrays
                    .asList(StringUtils.split(TOMCAT_USERS, '\n')))
                .addConfigFile("fld.properties",
                    Collections.singletonList("activemq.broker.url=tcp://localhost:61616\n"))
                .build();

        testMachine.addRole(tomcatRole);

        IRole activeMqRole = new ActiveMqRole(AMQ_ROLE_ID, "C:\\sw\\wily");
        testMachine.addRole(activeMqRole);

        IRole activitiIRole =
            new ActivitiRole(ACT_ROLE_ID, tomcatRole.getEnvProperties().get("tomcat.home")
                + "\\webapps");
        testMachine.addRole(activitiIRole);

        ITestbed testbed = new Testbed("WebLogicAgentTestbed");
        testbed.addMachine(testMachine);

        return testbed;
    }
}
