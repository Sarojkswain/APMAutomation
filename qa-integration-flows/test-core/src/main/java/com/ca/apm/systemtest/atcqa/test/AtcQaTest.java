package com.ca.apm.systemtest.atcqa.test;

import static com.ca.apm.role.JMeterLoadRole.START_JMETER_FLOW_KEY;
import static com.ca.apm.role.JMeterLoadRole.STOP_JMETER_FLOW_KEY;

import com.ca.apm.systemtest.atcqa.testbed.Constants;
import com.ca.apm.test.atc.UITest;

public abstract class AtcQaTest extends UITest implements Constants {

    protected void startJMeter() {
        runSerializedCommandFlowFromRole(JMETER_LOAD_ROLE_ID, START_JMETER_FLOW_KEY);
    }

    protected void stopJMeter() {
        runSerializedCommandFlowFromRole(JMETER_LOAD_ROLE_ID, STOP_JMETER_FLOW_KEY);
    }

}
