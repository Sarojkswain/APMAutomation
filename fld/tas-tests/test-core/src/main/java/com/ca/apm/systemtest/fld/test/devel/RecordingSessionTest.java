package com.ca.apm.systemtest.fld.test.devel;

import com.ca.apm.systemtest.fld.flow.ConfigureRecordingSessionFlowContext;
import com.ca.apm.systemtest.fld.flow.ConfigureTessFlow;
import com.ca.apm.systemtest.fld.flow.ConfigureTessFlowContext;
import com.ca.apm.systemtest.fld.flow.RunRecordingSessionFlow;
import com.ca.apm.systemtest.fld.role.RecordingSessionRole;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;
import com.ca.apm.systemtest.fld.testbed.devel.RecordingSessionTestbed;
import com.ca.tas.role.tess.ConfigureTessRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Development testbed for testing Agent and TIM recording session role functionality.
 *
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class RecordingSessionTest extends TasTestNgTest {
    private static final Logger LOG = LoggerFactory.getLogger(RecordingSessionTest.class);

    @Tas(testBeds = @TestBed(name = RecordingSessionTestbed.class, executeOn = RecordingSessionTestbed.EM_MACHINE_ID),
            size = SizeType.BIG, owner = "sinal04")
    @Test(groups = { "RecordingSessionTest" })
    public void testAgentRecordingSession() throws IOException, InterruptedException {
        configureTess();
        runRecordingSessions(FLDLoadConstants.AGENT_SESSION_RECORDING_ROLE_ID, 0);
        runRecordingSessions(FLDLoadConstants.AGENT_SESSION_RECORDING_ROLE_ID, 80000);
        runRecordingSessions(FLDLoadConstants.TIM_SESSION_RECORDING_ROLE_ID, 160000);
        TimeUnit.SECONDS.sleep(500);
    }

    private void configureTess() {
        final ConfigureTessFlowContext ctx = deserializeFlowContextFromRole(
            RecordingSessionTestbed.CONFIGURE_TESS_ROLE_ID,
            ConfigureTessRole.CONFIGURE_TESS_FLOW_KEY, ConfigureTessFlowContext.class);
        runFlowByMachineId(RecordingSessionTestbed.EM_MACHINE_ID, ConfigureTessFlow.class, ctx);
    }

    private void runRecordingSessions(String roleId, long delay) {
        final ConfigureRecordingSessionFlowContext ctx = deserializeFlowContextFromRole(roleId,
            RecordingSessionRole.RUN_RECORDING_SESSION_FLOW_CTX_KEY,
            ConfigureRecordingSessionFlowContext.class);
        final int durationMillis = ctx.getRecordingDurationMillis();

        Timer timer = new Timer(true);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    runFlowByMachineId(RecordingSessionTestbed.EM_MACHINE_ID, RunRecordingSessionFlow.class, ctx,
                            TimeUnit.MILLISECONDS, 2 * durationMillis);
                } catch (Exception e) {
                    LOG.warn("An exception occured while recording agent sessions: {}", e.getMessage());
                    LOG.debug("Exception", e);
                }
            }
        };

        timer.schedule(task, delay);
    }
}
