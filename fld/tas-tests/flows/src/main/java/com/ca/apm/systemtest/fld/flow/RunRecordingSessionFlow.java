package com.ca.apm.systemtest.fld.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.tas.flow.tess.TessUI;

/**
 * Flow which runs recording sessions whether of type Agent or TIM using 
 * {@link TessUI}.  
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@Flow
public class RunRecordingSessionFlow extends FlowBase {

    private static final int TIME_FOR_WHICH_TO_RECORD = 300*1000;
	
	@FlowContext
    private ConfigureRecordingSessionFlowContext ctx;
    
	@Override
	public void run() throws Exception {
        TessUI tess = TessUI.createTessUIForFirefoxWebDriver(ctx.tessHostname, ctx.tessPort, ctx.tessUser, ctx.tessPassword);
        tess.setDelay(1000);
        tess.login();

        String windowId = tess.startRecordingSession(ctx.clientIP, ctx.recordType);
        
        Thread.sleep(TIME_FOR_WHICH_TO_RECORD);
        
        tess.stopRecordingSession(windowId);
        tess.close();
	}

}
