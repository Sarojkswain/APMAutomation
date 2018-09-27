package com.ca.apm.systemtest.fld.test;

import com.ca.apm.systemtest.fld.flow.DynamicInstrumentationFlow;
import com.ca.apm.systemtest.fld.flow.DynamicInstrumentationFlowContext;
import com.ca.apm.systemtest.fld.role.DynamicInstrumentationLoadRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@Test
public class FLDAgentDynamicInstrumentationTest extends BaseFldLoadTest implements FLDLoadConstants {
    private Logger log = LoggerFactory.getLogger(FLDAgentDynamicInstrumentationTest.class);
    private Timer timer;
    

    @Override
    protected String getLoadName() {
        return "dynamic-instrumentation";
    }

    @Override
    protected void startLoad() {
        timer = new Timer(true);
        try {
            final DynamicInstrumentationFlowContext diCtx;
            
            diCtx = deserializeFlowContextFromRole(DYNAMIC_INSTR_ROLE_ID, 
                DynamicInstrumentationLoadRole.RUN_DI_FLOW, DynamicInstrumentationFlowContext.class);

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        runFlowByMachineId(DYNAMIC_INSTR_MACHINE_ID, DynamicInstrumentationFlow.class, diCtx, TimeUnit.HOURS, 12);
//                        runSerializedCommandFlowFromRole(DYNAMIC_INSTR_ROLE_ID, DynamicInstrumentationLoadRole.RUN_DI_FLOW, TimeUnit.HOURS, 12);
                    } catch (Exception e) {
                        log.warn("An exception occured while running dynamic instrumentation", e.getMessage());
                        log.debug("Exception", e);
                    }
                }
            };
            timer.scheduleAtFixedRate(task, 3600000L, 86400000L);
        } catch (Exception e) {
            log.warn("Unable to start FLD Dynamic Instrumentation load: {}", e.getMessage());
            log.debug("Exception:", e);
        }
    }

    @Override
    protected void stopLoad() {
        try {
            timer.cancel();
        } catch (Exception e) {
            log.warn("An exception occured while stopping dynamic instrumentation load: {}", e.getMessage());
        }
        timer = null;
    }
}
