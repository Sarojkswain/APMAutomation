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

package com.ca.apm.powerpack.sysview.tests.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.ca.apm.automation.utils.mainframe.ControlMvsTaskContext;
import com.ca.apm.automation.utils.mainframe.MvsTask;
import com.ca.apm.automation.utils.mainframe.MvsTask.Command;
import com.ca.apm.automation.utils.mainframe.MvsTask.State;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview;
import com.ca.apm.automation.utils.mainframe.sysview.Sysview.ExecResult;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole;
import com.ca.apm.powerpack.sysview.tests.role.CeapmRole.CeapmJavaConfig;
import com.ca.apm.powerpack.sysview.tests.testbed.CeapmAgentTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

import org.apache.commons.lang.StringUtils;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Test CEAPM agent start/stop sequences. Supports different versions of Java.
 */
public class CeapmAgentTest extends TasTestNgTest {
    private static final Logger logger = LoggerFactory.getLogger(CeapmAgentTest.class);

    @Tas(testBeds = @TestBed(name = CeapmAgentTestbed.class,
        executeOn = CeapmAgentTestbed.MF_MACHINE_ID), size = SizeType.MEDIUM)
    @Test(groups = TestClassification.SMOKE)
    public void agentStopTest() throws Exception {
        for (CeapmJavaConfig java : CeapmRole.SUPPORTED_JAVA_VERSIONS) {
            logger.info("Testing using {}", java);
            agentStopTest(java);
        }
    }

    protected void agentStopTest(CeapmJavaConfig java) throws Exception {
        final String task = CeapmAgentTestbed.CEAPM.getTaskName();
        final String roleId = CeapmAgentTestbed.CEAPM.getRole();
        final Map<String, String> javaParms = CeapmRole.getAgentJavaParameters(java);

        assertEquals(MvsTask.getTaskState(task, null), MvsTask.State.STOPPED,
            " should be stopped before test");

        logger.info("Start/stop {} task sequence using STOP callback", task);
        CeapmRole.startAgent(envProperties, roleId, javaParms, true);
        CeapmRole.stopAgent(envProperties, roleId);
        assertEquals(MvsTask.getTaskState(task, null), MvsTask.State.STOPPED,
            "should be stopped after STOP");
        assertTaskRc(task, "0");

        logger.info("Start/stop {} task sequence using ACTION=STOP", task);
        CeapmRole.startAgent(envProperties, roleId, javaParms, true);
        ControlMvsTaskContext.Builder ctx =
            new ControlMvsTaskContext.Builder(task, State.STOPPED).action(Command.START)
                .taskParms(javaParms).taskParms(Collections.singletonMap("ACTION", "STOP"));
        MvsTask.execute(ctx.build());
        assertEquals(MvsTask.getTaskState(task, null), MvsTask.State.STOPPED,
            "should be stopped after START ACTION=STOP");
        assertTaskRc(task, "0");
    }

    private static void assertTaskRc(String taskName, String rc) throws IOException {
        Args.notBlank(taskName, "task name");
        Args.notNull(rc, "rc");
        try (Sysview sysv = new Sysview(CeapmAgentTestbed.SYSVIEW.getLoadlib())) {
            ExecResult jobs =
                sysv.execute("PREFIX {0}; OWNER; JOBSUM; SORT InpDate,D,InpTime,D", taskName);
            assertTrue(jobs.getRc().isOk(), "query command RC");
            List<Map<String, String>> allRows = jobs.getTabularData().getAllRows();
            assertTrue(allRows.size() > 0, "missing task result");
            ArrayList<String> unexpected = new ArrayList<>();
            for (Map<String, String> row : allRows) {
                String result = row.get("CCode");
                assertNotNull(result);
                logger.debug("{}({})={}", taskName, row.get("JobNr"), result);
                if (!rc.equals(result)) {
                    unexpected.add(result);
                }
            }
            assertEquals(StringUtils.join(unexpected, ","), "", "unexpected RCs");
        }
    }
}
