package com.ca.apm.systemtest.fld.test;

import com.ca.apm.systemtest.fld.flow.ConfigureRecordingSessionFlowContext;
import com.ca.apm.systemtest.fld.flow.RunRecordingSessionFlow;
import com.ca.apm.systemtest.fld.role.RecordingSessionRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * FLD Agent recording sessions test.
 *  
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class RunAgentRecordingSessionsTest extends BaseFldLoadTest implements FLDLoadConstants, FLDConstants {
    private static final Logger LOG = LoggerFactory.getLogger(RunAgentRecordingSessionsTest.class);

	private Timer timer;
	
	@Override
	protected String getLoadName() {
		return "agent-recording";
	}

	@Override
	protected void startLoad() {
        final ConfigureRecordingSessionFlowContext ctx = deserializeFlowContextFromRole(
            FLDLoadConstants.AGENT_SESSION_RECORDING_ROLE_ID,
            RecordingSessionRole.RUN_RECORDING_SESSION_FLOW_CTX_KEY,
            ConfigureRecordingSessionFlowContext.class);

        int durationMillis = ctx.getRecordingDurationMillis();

		timer = new Timer(true);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    runFlowByMachineId(AGENT_SESSION_RECORDING_MACHINE_ID, RunRecordingSessionFlow.class, ctx, 
                			TimeUnit.HOURS, 1);//give it a bit more time to finish running	
                } catch (Exception e) {
                    LOG.warn("An exception occured while recording agent sessions: {}", e.getMessage());
                    LOG.debug("Exception", e);
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, durationMillis);
	}

	@Override
	protected void stopLoad() {
        try {
            timer.cancel();
        } catch (Exception e) {
            LOG.warn("An exception occurred while stopping recording agent sessions: {}", e.getMessage(), e);
        }
        timer = null;
	
	}
	
	

}
