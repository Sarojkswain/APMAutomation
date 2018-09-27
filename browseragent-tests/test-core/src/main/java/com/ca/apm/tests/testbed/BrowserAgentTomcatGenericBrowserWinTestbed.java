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
 * Browser Agent Automation Testbed - Tomcat
 *
 * @author gupra04
 */

package com.ca.apm.tests.testbed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.tests.artifact.testapp.BRTMTestAppArtifact;
import com.ca.apm.tests.artifact.testapp.BrowserAgentSecondaryTestappArtifact;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.webapp.AgentCapable;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.tests.annotations.TestBedDefinition;

@TestBedDefinition
public abstract class BrowserAgentTomcatGenericBrowserWinTestbed
    extends BrowserAgentGenericAppServerWinTestbed {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(BrowserAgentTomcatGenericBrowserWinTestbed.class);

    // @Override
    protected AgentCapable getWebAppServerRole(ITasResolver tasResolver, JavaRole javaRole) {
        clwJarHome =
            this.getSoftwareLoc() + "em" + this.getSeparator() + "lib" + this.getSeparator();
        seleniumDriverHome = this.getSoftwareLoc() + "lib" + this.getSeparator();

        applicationServer = "TOMCAT";
        agentName = "TomcatAgent";
        agentProcessName = "TomcatProcess";
        agentPblFile = "tomcat-typical.pbl";
        agentRoleId = super.TOMCAT_AGENT_ROLE_ID;
        agentPort = super.TOMCAT_PORT;

        // Tomcat role, no auto startup
        TomcatRole tomcatRole =
            new TomcatRole.Builder(TOMCAT_ROLE_ID, tasResolver)
                .customJava(javaRole)
                .tomcatVersion(TomcatVersion.v70)
                .tomcatCatalinaPort(TOMCAT_PORT)
                .webApplication(new BRTMTestAppArtifact(tasResolver).createArtifact(),
                    BRTM_TEST_APP_CONTEXT)
                .webApplication(
                    BrowserAgentSecondaryTestappArtifact.CLICK_LISTENER_TEST2_V1_0.getArtifact(),
                    CLICK_LISTENER_TEST_2_CONTEXT)
                .webApplication(
                    BrowserAgentSecondaryTestappArtifact.DEMO_WEBAPP_V1_0.getArtifact(),
                    DEMO_WEB_APP_CONTEXT)
                .webApplication(BrowserAgentSecondaryTestappArtifact.INFI_V1_0.getArtifact(),
                    INFI_CONTEXT)
                .webApplication(
                    BrowserAgentSecondaryTestappArtifact.SESSION_TEST_V1_0.getArtifact(),
                    SESSION_TEST_CONTEXT)
                .webApplication(
                    BrowserAgentSecondaryTestappArtifact.URCHIN_TRACKER_V1_0.getArtifact(),
                    URCHIN_TRACKER_CONTEXT)
                // TODO: Review DWR test application. Deployment fails without the fix.
                // .webApplication( BrowserAgentSecondaryTestappArtifact.DWR_V1_0, DWR_CONTEXT)
                .build();
        return tomcatRole;
    }
}
