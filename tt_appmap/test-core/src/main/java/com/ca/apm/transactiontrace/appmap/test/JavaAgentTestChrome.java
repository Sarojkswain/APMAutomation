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

package com.ca.apm.transactiontrace.appmap.test;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.Test;

import com.ca.apm.transactiontrace.appmap.testbed.JavaAgentJBossStandAloneTestbed;
import com.ca.apm.transactiontrace.appmap.testbed.JavaAgentWebLogicStandAloneTestbed;
import com.ca.apm.transactiontrace.appmap.testbed.JavaAgentWebSphereStandAloneTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Test that starts TT session, then executes some actions on QATestApp and then logs
 * into the Team Center and requests the graph object using private API - Chrome version.
 *
 * We check whether there are any edges in the resulting graph.
 *
 * @author Jan Zak (zakja01@ca.com)
 */
public class JavaAgentTestChrome extends AbstractJavaAgentTest {

    public JavaAgentTestChrome() {
        super(DesiredCapabilities.chrome());
    }

    @Test(groups = {"appmap", "agent", "java", "jboss"})
    @Tas(testBeds = @TestBed(name = JavaAgentJBossStandAloneTestbed.class, executeOn = JavaAgentJBossStandAloneTestbed.JAVA_AGENT_JBOSS_MACHINE), size = SizeType.MEDIUM, owner = "zakja01")
    public void smokeJBossTest() {
        verifyWithJavaAgent(JavaAgentJBossStandAloneTestbed.JBOSS_ROLE_ID, "8080",
            JavaAgentJBossStandAloneTestbed.EM_ROLE_ID,
            JavaAgentJBossStandAloneTestbed.INITIATE_TT_SESSION_ROLE_ID);

    }

    @Test(groups = {"appmap", "agent", "java", "weblogic"})
    @Tas(testBeds = @TestBed(name = JavaAgentWebLogicStandAloneTestbed.class, executeOn = JavaAgentWebLogicStandAloneTestbed.JAVA_AGENT_WEBLOGIC_MACHINE), size = SizeType.MEDIUM, owner = "zakja01")
    public void smokeWebLogicTest() {
        verifyWithJavaAgent(JavaAgentWebLogicStandAloneTestbed.WEBLOGIC_ROLE_ID, "7001",
            JavaAgentWebLogicStandAloneTestbed.EM_ROLE_ID,
            JavaAgentWebLogicStandAloneTestbed.INITIATE_TT_SESSION_ROLE_ID);

    }

    @Test(groups = {"appmap", "agent", "java", "websphere"})
    @Tas(testBeds = @TestBed(name = JavaAgentWebSphereStandAloneTestbed.class, executeOn = JavaAgentWebSphereStandAloneTestbed.JAVA_AGENT_WEBSPHERE_MACHINE), size = SizeType.MEDIUM, owner = "zakja01")
    public void smokeWebSphereTest() {
        verifyWithJavaAgent(JavaAgentWebSphereStandAloneTestbed.WEBSPHERE_ROLE_ID, "9080",
            JavaAgentWebSphereStandAloneTestbed.EM_ROLE_ID,
            JavaAgentWebSphereStandAloneTestbed.INITIATE_TT_SESSION_ROLE_ID);

    }

}
