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
package com.ca.apm.tests.testbed.machines;

import com.ca.apm.tests.role.*;
import com.ca.apm.tests.testbed.machines.template.JMeterMachineAbs;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.TestbedMachine;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class JMeterIisMachine extends JMeterMachineAbs {

    public static final String DEFAULT_TEMPLATE = "AgentPerf_JMETERIIS";

    public static final String APP_SERVER_NAME = "iis75";

    protected String testResultsShare;

    protected Long jmeterNumThreads = 200L;
    protected Long jmeterDelayBetweenRequests = 200L;

    public JMeterIisMachine(String machineId, ITasResolver tasResolver, String sharePassword) {
        super(machineId, tasResolver, sharePassword);
    }

    public JMeterIisMachine(String machineId, ITasResolver tasResolver, boolean undeploy, String sharePassword) {
        super(machineId, tasResolver, undeploy, sharePassword);
    }

    public TestbedMachine undeploy() {
        TestbedMachine machine = new TestbedMachine.Builder(machineId).templateId(DEFAULT_TEMPLATE).build();

        ExecutionRole deleteJmeterRole = deleteJmeterRole();
        machine.addRole(deleteJmeterRole);

        ExecutionRole deleteJmxRole = deleteJmeterJmxRole();
        machine.addRole(deleteJmxRole);

        return machine;
    }

    public TestbedMachine initPredeployed(NetStockTraderRole netStockTraderRole, IRole emRole) {
        this.predeployed = true;
        return init(netStockTraderRole, emRole);
    }

    public TestbedMachine init(NetStockTraderRole netStockTraderRole, IRole emRole) {
        TestbedMachine machine = new TestbedMachine.Builder(machineId).templateId(DEFAULT_TEMPLATE).build();

        if (undeploy) {
            undeploy();
        } else {

            testResultsShare = "\\\\" + tasResolver.getHostnameById(emRole.getRoleId()) + "\\" + EmMachine.SHARE_DIR_NAME;

            String jmxScriptFile = "net.stocktrader-jmeter-var.jmx";

            ///////////////////////////////////////////
            // DEPLOY JMETER JMX
            ///////////////////////////////////////////

            JMeterJmxRole jmeterJmxRole = createJmeterJmxRole();
            machine.addRole(jmeterJmxRole);

            String jmxScript = jmeterJmxRole.getScriptsDirectory() + "/" + jmxScriptFile;

            ///////////////////////////////////////////
            // DEPLOY JMETER LOG CONVERTER
            ///////////////////////////////////////////

            // no agent
            JMeterLogConverterRole jmeterLogConverterNoAgentRole = createJmeterLogConverterNoAgentRole();
            machine.addRole(jmeterLogConverterNoAgentRole);

            // current no SI
            JMeterLogConverterRole jmeterLogConverterCurrentJavaAgentNoSiRole = createJmeterLogConverterCurrentJavaAgentNoSiRole();
            machine.addRole(jmeterLogConverterCurrentJavaAgentNoSiRole);

            // current SI
            JMeterLogConverterRole jmeterLogConverterCurrentJavaAgentSiRole = createJmeterLogConverterCurrentJavaAgentSiRole();
            machine.addRole(jmeterLogConverterCurrentJavaAgentSiRole);

            // prev no SI
            JMeterLogConverterRole jmeterLogConverterPrevJavaAgentNoSiRole = createJmeterLogConverterPrevJavaAgentNoSiRole();
            machine.addRole(jmeterLogConverterPrevJavaAgentNoSiRole);

            // prev SI
            JMeterLogConverterRole jmeterLogConverterPrevJavaAgentSiRole = createJmeterLogConverterPrevJavaAgentSiRole();
            machine.addRole(jmeterLogConverterPrevJavaAgentSiRole);

            ///////////////////////////////////////////
            // DEPLOY JMETER
            ///////////////////////////////////////////

            Map<String, String> params = new HashMap<>();
            params.put(PARAM_RAMP_UP_TIME, String.valueOf(DEF_RAMP_UP_TIME));
            params.put(PARAM_TEST_RUNTIME, String.valueOf(DEF_TEST_RUNTIME));
            params.put(PARAM_TEST_STARTUP_DELAY, String.valueOf(DEF_TEST_STARTUP_DELAY));

            params.put("NET.STOCKTRADER.NUM.THREADS", String.valueOf(jmeterNumThreads));
            params.put("NET.STOCKTRADER.HOSTNAME", tasResolver.getHostnameById(netStockTraderRole.getRoleId()));
            params.put("NET.STOCKTRADER.HTTP.PORT", String.valueOf(netStockTraderRole.getPortNumber()));
            params.put("NET.STOCKTRADER.DELAY.BETWEEN.REQUESTS", String.valueOf(jmeterDelayBetweenRequests));

            // no agent
            JMeterRole jmeterNoAgentRole = createJmeterNoAgentRole(jmeterLogConverterNoAgentRole, jmxScript, params);
            machine.addRole(jmeterNoAgentRole);

            // current no SI
            JMeterRole jmeterCurrentJavaAgentNoSiRole = createJmeterCurrentJavaAgentNoSiRole(jmeterLogConverterCurrentJavaAgentNoSiRole, jmxScript, params);
            machine.addRole(jmeterCurrentJavaAgentNoSiRole);

            // current SI
            JMeterRole jmeterCurrentJavaAgentSiRole = createJmeterCurrentJavaAgentSiRole(jmeterLogConverterCurrentJavaAgentSiRole, jmxScript, params);
            machine.addRole(jmeterCurrentJavaAgentSiRole);

            // prev no SI
            JMeterRole jmeterPrevJavaAgentNoSiRole = createJmeterPrevJavaAgentNoSiRole(jmeterLogConverterPrevJavaAgentNoSiRole, jmxScript, params);
            machine.addRole(jmeterPrevJavaAgentNoSiRole);

            // prev SI
            JMeterRole jmeterPrevJavaAgentSiRole = createJmeterPrevJavaAgentSiRole(jmeterLogConverterPrevJavaAgentSiRole, jmxScript, params);
            machine.addRole(jmeterPrevJavaAgentSiRole);

            ///////////////////////////////////////////
            // DEPLOY JMETER STATS
            ///////////////////////////////////////////

            // no agent
            JMeterStatsRole jmeterStatsNoAgentRole = createJmeterStatsNoAgentRole();
            machine.addRole(jmeterStatsNoAgentRole);

            // current no SI
            JMeterStatsRole jmeterStatsCurrentJavaAgentNoSiRole = createJmeterStatsCurrentJavaAgentNoSiRole();
            machine.addRole(jmeterStatsCurrentJavaAgentNoSiRole);

            // current SI
            JMeterStatsRole jmeterStatsCurrentJavaAgentSiRole = createJmeterStatsCurrentJavaAgentSiRole();
            machine.addRole(jmeterStatsCurrentJavaAgentSiRole);

            // prev no SI
            JMeterStatsRole jmeterStatsPrevJavaAgentNoSiRole = createJmeterStatsPrevJavaAgentNoSiRole();
            machine.addRole(jmeterStatsPrevJavaAgentNoSiRole);

            // prev SI
            JMeterStatsRole jmeterStatsPrevJavaAgentSiRole = createJmeterStatsPrevJavaAgentSiRole();
            machine.addRole(jmeterStatsPrevJavaAgentSiRole);

            ///////////////////////////////////////////
            // INCREASE NETWORK BUFFER
            ///////////////////////////////////////////

            configureNetworkBuffer(machine);

        }

        return machine;
    }

    @Override
    public String getMachineId() {
        return machineId;
    }

    @Override
    public ITasResolver getTasResolver() {
        return tasResolver;
    }

    @Override
    public String getAppServerName() {
        return APP_SERVER_NAME;
    }

    @Override
    public String getTestResultsShare() {
        return testResultsShare;
    }

    @Override
    public Long getJmeterNumThreads() {
        return jmeterNumThreads;
    }

    @Override
    public Long getJmeterDelayBetweenRequests() {
        return jmeterDelayBetweenRequests;
    }

}
