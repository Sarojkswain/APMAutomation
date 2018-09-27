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
 * @author filja01
 *
 */
public class RunTIMRecordingSessionsTest extends BaseFldLoadTest implements FLDLoadConstants, FLDConstants {
    private static final Logger LOG = LoggerFactory.getLogger(RunTIMRecordingSessionsTest.class);

	private Timer timer;
	
	@Override
	protected String getLoadName() {
		return "tim-recording";
	}

	@Override
	protected void startLoad() {
	    // wait 15 minutes with start
        shortWait(900000L);
	    
        final ConfigureRecordingSessionFlowContext ctx = deserializeFlowContextFromRole(
            FLDLoadConstants.TIM_SESSION_RECORDING_ROLE_ID,
            RecordingSessionRole.RUN_RECORDING_SESSION_FLOW_CTX_KEY,
            ConfigureRecordingSessionFlowContext.class);

        int durationMillis = ctx.getRecordingDurationMillis();

		timer = new Timer(true);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    runFlowByMachineId(TIM_SESSION_RECORDING_MACHINE_ID, RunRecordingSessionFlow.class, ctx, 
                			TimeUnit.HOURS, 1);//give it a bit more time to finish running	
                } catch (Exception e) {
                    LOG.warn("An exception occured while recording TIM sessions: {}", e.getMessage());
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
            LOG.warn("An exception occurred while stopping recording TIM sessions: {}", e.getMessage(), e);
        }
        timer = null;
	
	}
	
	private synchronized void shortWait(long ms) {
        try {
            wait(ms);
        } catch (Exception e) {
        }
    }

}
