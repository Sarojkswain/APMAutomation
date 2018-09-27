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
package com.ca.apm.tests.testbed.machines.template;

import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.tests.artifact.JMeterJmxVersion;
import com.ca.apm.tests.role.JMeterJmxRole;
import com.ca.apm.tests.role.JMeterLogConverterRole;
import com.ca.apm.tests.role.JMeterRole;
import com.ca.apm.tests.role.JMeterStatsRole;
import com.ca.apm.tests.testbed.utils.NetworkUtils;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.TestbedMachine;

import java.util.Map;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public abstract class JMeterMachineAbs implements JMeterMachine {

    protected final String machineId;
    protected final ITasResolver tasResolver;

    protected final String sharePassword;

    protected boolean undeploy;
    protected boolean predeployed;

    public JMeterMachineAbs(String machineId, ITasResolver tasResolver, String sharePassword) {
        this.machineId = machineId;
        this.tasResolver = tasResolver;
        this.sharePassword = sharePassword;
    }

    public JMeterMachineAbs(String machineId, ITasResolver tasResolver, boolean undeploy, String sharePassword) {
        this(machineId, tasResolver, sharePassword);
        this.undeploy = undeploy;
    }


    protected abstract String getMachineId();

    protected abstract ITasResolver getTasResolver();

    protected abstract String getAppServerName();

    protected abstract String getTestResultsShare();

    protected abstract Long getJmeterNumThreads();

    protected abstract Long getJmeterDelayBetweenRequests();


    ////////////////////////////
    // JMETER JMX SCRIPT
    ////////////////////////////

    protected JMeterJmxRole createJmeterJmxRole() {
        return createJmeterJmxRole(JMeterJmxVersion.Agent_Performance_1_0);
    }

    protected JMeterJmxRole createJmeterJmxRole(JMeterJmxVersion version) {
        return new JMeterJmxRole
                .Builder(getMachineId() + "_jmeterJmxRoleId", getTasResolver()).version(version)
                .installPath(JMX_INSTALL_DIR)
                .build();
    }

    ////////////////////////////
    // JMETER LOG CONVERTER
    ////////////////////////////

    protected JMeterLogConverterRole createJmeterLogConverterNoAgentRole() {
        return new JMeterLogConverterRole
                .Builder(getMachineId() + "_jmeterLogConverterNoAgentRoleId", getTasResolver())
                .outputFileName(JMETER_LOG_CONVERTER_OUTPUT_DIR + "\\jmeterNoAgent.modified.csv")
                .build();
    }

    protected JMeterLogConverterRole createJmeterLogConverterCurrentJavaAgentNoSiRole() {
        return new JMeterLogConverterRole
                .Builder(getMachineId() + "_jmeterLogConverterCurrentJavaAgentNoSiRoleId", getTasResolver())
                .outputFileName(JMETER_LOG_CONVERTER_OUTPUT_DIR + "\\jmeterCurrentJavaAgentNoSi.modified.csv")
                .build();
    }

    /**
     * (only for 10.4+)
     */
    protected JMeterLogConverterRole createJmeterLogConverterCurrentJavaAgentNoSiBtRole() {
        return new JMeterLogConverterRole
                .Builder(getMachineId() + "_jmeterLogConverterCurrentJavaAgentNoSiBtRoleId", getTasResolver())
                .outputFileName(JMETER_LOG_CONVERTER_OUTPUT_DIR + "\\jmeterCurrentJavaAgentNoSiBt.modified.csv")
                .build();
    }

    protected JMeterLogConverterRole createJmeterLogConverterCurrentJavaAgentNoSiMvc4Role() {
        return new JMeterLogConverterRole
                .Builder(getMachineId() + "_jmeterLogConverterCurrentJavaAgentNoSiMvc4RoleId", getTasResolver())
                .outputFileName(JMETER_LOG_CONVERTER_OUTPUT_DIR + "\\jmeterCurrentJavaAgentNoSiMvc4.modified.csv")
                .build();
    }

    protected JMeterLogConverterRole createJmeterLogConverterCurrentJavaAgentNoSiAccRole() {
        return new JMeterLogConverterRole
                .Builder(getMachineId() + "_jmeterLogConverterCurrentJavaAgentNoSiAccRoleId", getTasResolver())
                .outputFileName(JMETER_LOG_CONVERTER_OUTPUT_DIR + "\\jmeterCurrentJavaAgentNoSiAcc.modified.csv")
                .build();
    }

    protected JMeterLogConverterRole createJmeterLogConverterCurrentJavaAgentNoSiBrtmRole() {
        return new JMeterLogConverterRole
                .Builder(getMachineId() + "_jmeterLogConverterCurrentJavaAgentNoSiBrtmRoleId", getTasResolver())
                .outputFileName(JMETER_LOG_CONVERTER_OUTPUT_DIR + "\\jmeterCurrentJavaAgentNoSiBrtm.modified.csv")
                .build();
    }

    protected JMeterLogConverterRole createJmeterLogConverterCurrentJavaAgentSiRole() {
        return new JMeterLogConverterRole
                .Builder(getMachineId() + "_jmeterLogConverterCurrentJavaAgentSiRoleId", getTasResolver())
                .outputFileName(JMETER_LOG_CONVERTER_OUTPUT_DIR + "\\jmeterCurrentJavaAgentSi.modified.csv")
                .build();
    }

    protected JMeterLogConverterRole createJmeterLogConverterPrevJavaAgentNoSiRole() {
        return new JMeterLogConverterRole
                .Builder(getMachineId() + "_jmeterLogConverterPrevJavaAgentNoSiRoleId", getTasResolver())
                .outputFileName(JMETER_LOG_CONVERTER_OUTPUT_DIR + "\\jmeterPrevJavaAgentNoSi.modified.csv")
                .build();
    }

    protected JMeterLogConverterRole createJmeterLogConverterPrevJavaAgentSiRole() {
        return new JMeterLogConverterRole
                .Builder(getMachineId() + "_jmeterLogConverterPrevJavaAgentSiRoleId", getTasResolver())
                .outputFileName(JMETER_LOG_CONVERTER_OUTPUT_DIR + "\\jmeterPrevJavaAgentSi.modified.csv")
                .build();
    }

    ////////////////////////////
    // JMETER
    ////////////////////////////

    protected JMeterRole createJmeterNoAgentRole(JMeterLogConverterRole logConverterRole, String jmxScript, Map<String, String> params) {
        return new JMeterRole
                .Builder(getMachineId() + JMETER_NO_AGENT_ROLE_ID, getTasResolver())
                .deploySourcesLocation(JMETER_PARENT_INSTALL_DIR)
                .scriptFilePath(jmxScript)
                .outputJtlFile(JMETER_OUTPUT_DIR + "\\" + "jmeterNoAgent.jtl")
                .outputLogFile(JMETER_OUTPUT_DIR + "\\" + "jmeterNoAgent.log")
                .deleteOutputLogsBeforeRun()
                .params(params)
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationJtlFileName("jmeter_" + getAppServerName() + ".noagent.modified.csv")
                .copyResultsDestinationPassword(sharePassword)
                .jmeterLogConverter(logConverterRole)
                .predeployed(predeployed)
                .build();
    }

    protected JMeterRole createJmeterCurrentJavaAgentNoSiRole(JMeterLogConverterRole logConverterRole, String jmxScript, Map<String, String> params) {
        return new JMeterRole
                .Builder(getMachineId() + JMETER_CURRENT_NO_SI_ROLE_ID, getTasResolver())
                .deploySourcesLocation(JMETER_PARENT_INSTALL_DIR)
                .scriptFilePath(jmxScript)
                .outputJtlFile(JMETER_OUTPUT_DIR + "\\" + "jmeterCurrentJavaAgentNoSi.jtl")
                .outputLogFile(JMETER_OUTPUT_DIR + "\\" + "jmeterCurrentJavaAgentNoSi.log")
                .deleteOutputLogsBeforeRun()
                .params(params)
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationJtlFileName("jmeter_" + getAppServerName() + ".nosi.CURRENT.modified.csv")
                .copyResultsDestinationPassword(sharePassword)
                .jmeterLogConverter(logConverterRole)
                .predeployed(predeployed)
                .build();
    }

    /**
     * (only for 10.4+)
     */
    protected JMeterRole createJmeterCurrentJavaAgentNoSiBtRole(JMeterLogConverterRole logConverterRole, String jmxScript, Map<String, String> params) {
        return new JMeterRole
                .Builder(getMachineId() + JMETER_CURRENT_NO_SI_BT_ROLE_ID, getTasResolver())
                .deploySourcesLocation(JMETER_PARENT_INSTALL_DIR)
                .scriptFilePath(jmxScript)
                .outputJtlFile(JMETER_OUTPUT_DIR + "\\" + "jmeterCurrentJavaAgentNoSiBt.jtl")
                .outputLogFile(JMETER_OUTPUT_DIR + "\\" + "jmeterCurrentJavaAgentNoSiBt.log")
                .deleteOutputLogsBeforeRun()
                .params(params)
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationJtlFileName("jmeter_" + getAppServerName() + ".nosi.bt.CURRENT.modified.csv")
                .copyResultsDestinationPassword(sharePassword)
                .jmeterLogConverter(logConverterRole)
                .predeployed(predeployed)
                .build();
    }

    protected JMeterRole createJmeterCurrentJavaAgentNoSiMvc4Role(JMeterLogConverterRole logConverterRole, String jmxScript, Map<String, String> params) {
        return new JMeterRole
                .Builder(getMachineId() + JMETER_CURRENT_NO_SI_ND4_ROLE_ID, getTasResolver())
                .deploySourcesLocation(JMETER_PARENT_INSTALL_DIR)
                .scriptFilePath(jmxScript)
                .outputJtlFile(JMETER_OUTPUT_DIR + "\\" + "jmeterCurrentJavaAgentNoSiMvc4.jtl")
                .outputLogFile(JMETER_OUTPUT_DIR + "\\" + "jmeterCurrentJavaAgentNoSiMvc4.log")
                .deleteOutputLogsBeforeRun()
                .params(params)
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationJtlFileName("jmeter_" + getAppServerName() + ".nosi.nd4.CURRENT.modified.csv")
                .copyResultsDestinationPassword(sharePassword)
                .jmeterLogConverter(logConverterRole)
                .predeployed(predeployed)
                .build();
    }

    protected JMeterRole createJmeterCurrentJavaAgentNoSiAccRole(JMeterLogConverterRole logConverterRole, String jmxScript, Map<String, String> params) {
        return new JMeterRole
                .Builder(getMachineId() + JMETER_CURRENT_NO_SI_ACC_ROLE_ID, getTasResolver())
                .deploySourcesLocation(JMETER_PARENT_INSTALL_DIR)
                .scriptFilePath(jmxScript)
                .outputJtlFile(JMETER_OUTPUT_DIR + "\\" + "jmeterCurrentJavaAgentNoSiAcc.jtl")
                .outputLogFile(JMETER_OUTPUT_DIR + "\\" + "jmeterCurrentJavaAgentNoSiAcc.log")
                .deleteOutputLogsBeforeRun()
                .params(params)
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationJtlFileName("jmeter_" + getAppServerName() + ".acc.CURRENT.modified.csv")
                .copyResultsDestinationPassword(sharePassword)
                .jmeterLogConverter(logConverterRole)
                .predeployed(predeployed)
                .build();
    }

    protected JMeterRole createJmeterCurrentJavaAgentNoSiBrtmRole(JMeterLogConverterRole logConverterRole, String jmxScript, Map<String, String> params) {
        return new JMeterRole
                .Builder(getMachineId() + JMETER_CURRENT_NO_SI_BRTM_ROLE_ID, getTasResolver())
                .deploySourcesLocation(JMETER_PARENT_INSTALL_DIR)
                .scriptFilePath(jmxScript)
                .outputJtlFile(JMETER_OUTPUT_DIR + "\\" + "jmeterCurrentJavaAgentNoSiBrtm.jtl")
                .outputLogFile(JMETER_OUTPUT_DIR + "\\" + "jmeterCurrentJavaAgentNoSiBrtm.log")
                .deleteOutputLogsBeforeRun()
                .params(params)
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationJtlFileName("jmeter_" + getAppServerName() + ".nosi.brtm.CURRENT.modified.csv")
                .copyResultsDestinationPassword(sharePassword)
                .jmeterLogConverter(logConverterRole)
                .predeployed(predeployed)
                .build();
    }

    protected JMeterRole createJmeterCurrentJavaAgentSiRole(JMeterLogConverterRole logConverterRole, String jmxScript, Map<String, String> params) {
        return new JMeterRole
                .Builder(getMachineId() + JMETER_CURRENT_SI_ROLE_ID, getTasResolver())
                .deploySourcesLocation(JMETER_PARENT_INSTALL_DIR)
                .scriptFilePath(jmxScript)
                .outputJtlFile(JMETER_OUTPUT_DIR + "\\" + "jmeterCurrentJavaAgentSi.jtl")
                .outputLogFile(JMETER_OUTPUT_DIR + "\\" + "jmeterCurrentJavaAgentSi.log")
                .deleteOutputLogsBeforeRun()
                .params(params)
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationJtlFileName("jmeter_" + getAppServerName() + ".si.CURRENT.modified.csv")
                .copyResultsDestinationPassword(sharePassword)
                .jmeterLogConverter(logConverterRole)
                .predeployed(predeployed)
                .build();
    }

    protected JMeterRole createJmeterPrevJavaAgentNoSiRole(JMeterLogConverterRole logConverterRole, String jmxScript, Map<String, String> params) {
        return new JMeterRole
                .Builder(getMachineId() + JMETER_PREV_NO_SI_ROLE_ID, getTasResolver())
                .deploySourcesLocation(JMETER_PARENT_INSTALL_DIR)
                .scriptFilePath(jmxScript)
                .outputJtlFile(JMETER_OUTPUT_DIR + "\\" + "jmeterPrevJavaAgentNoSi.jtl")
                .outputLogFile(JMETER_OUTPUT_DIR + "\\" + "jmeterPrevJavaAgentNoSi.log")
                .deleteOutputLogsBeforeRun()
                .params(params)
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationJtlFileName("jmeter_" + getAppServerName() + ".nosi.PREV.modified.csv")
                .copyResultsDestinationPassword(sharePassword)
                .jmeterLogConverter(logConverterRole)
                .predeployed(predeployed)
                .build();
    }

    protected JMeterRole createJmeterPrevJavaAgentSiRole(JMeterLogConverterRole logConverterRole, String jmxScript, Map<String, String> params) {
        return new JMeterRole
                .Builder(getMachineId() + JMETER_PREV_SI_ROLE_ID, getTasResolver())
                .deploySourcesLocation(JMETER_PARENT_INSTALL_DIR)
                .scriptFilePath(jmxScript)
                .outputJtlFile(JMETER_OUTPUT_DIR + "\\" + "jmeterPrevJavaAgentSi.jtl")
                .outputLogFile(JMETER_OUTPUT_DIR + "\\" + "jmeterPrevJavaAgentSi.log")
                .deleteOutputLogsBeforeRun()
                .params(params)
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationJtlFileName("jmeter_" + getAppServerName() + ".si.PREV.modified.csv")
                .copyResultsDestinationPassword(sharePassword)
                .jmeterLogConverter(logConverterRole)
                .predeployed(predeployed)
                .build();
    }

    ////////////////////////////
    // JMETER STATS
    ////////////////////////////

    protected JMeterStatsRole createJmeterStatsNoAgentRole() {
        return new JMeterStatsRole
                .Builder(getMachineId() + JMETER_STATS_NO_AGENT_ROLE_ID, getTasResolver())
                .numThreads(getJmeterNumThreads()).rampUpTime(DEF_RAMP_UP_TIME).runMinutes(DEF_TEST_RUNTIME)
                .startupDelaySeconds(DEF_TEST_STARTUP_DELAY).delayBetweenRequests(getJmeterDelayBetweenRequests())
                .outputFile(JMETER_STAT_OUTPUT_DIR + "\\jmeterNoAgent.stat.csv")
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("jmeter_" + getAppServerName() + ".noagent.stat.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected JMeterStatsRole createJmeterStatsCurrentJavaAgentNoSiRole() {
        return new JMeterStatsRole
                .Builder(getMachineId() + JMETER_STATS_CURRENT_NO_SI_ROLE_ID, getTasResolver())
                .numThreads(getJmeterNumThreads()).rampUpTime(DEF_RAMP_UP_TIME).runMinutes(DEF_TEST_RUNTIME)
                .startupDelaySeconds(DEF_TEST_STARTUP_DELAY).delayBetweenRequests(getJmeterDelayBetweenRequests())
                .outputFile(JMETER_STAT_OUTPUT_DIR + "\\jmeterCurrentJavaAgentNoSi.stat.csv")
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("jmeter_" + getAppServerName() + ".nosi.CURRENT.stat.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    /**
     * (only for 10.4+)
     */
    protected JMeterStatsRole createJmeterStatsCurrentJavaAgentNoSiBtRole() {
        return new JMeterStatsRole
                .Builder(getMachineId() + JMETER_STATS_CURRENT_NO_SI_BT_ROLE_ID, getTasResolver())
                .numThreads(getJmeterNumThreads()).rampUpTime(DEF_RAMP_UP_TIME).runMinutes(DEF_TEST_RUNTIME)
                .startupDelaySeconds(DEF_TEST_STARTUP_DELAY).delayBetweenRequests(getJmeterDelayBetweenRequests())
                .outputFile(JMETER_STAT_OUTPUT_DIR + "\\jmeterCurrentJavaAgentNoSiBt.stat.csv")
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("jmeter_" + getAppServerName() + ".nosi.bt.CURRENT.stat.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected JMeterStatsRole createJmeterStatsCurrentJavaAgentNoSiMvc4Role() {
        return new JMeterStatsRole
                .Builder(getMachineId() + JMETER_STATS_CURRENT_NO_SI_ND4_ROLE_ID, getTasResolver())
                .numThreads(getJmeterNumThreads()).rampUpTime(DEF_RAMP_UP_TIME).runMinutes(DEF_TEST_RUNTIME)
                .startupDelaySeconds(DEF_TEST_STARTUP_DELAY).delayBetweenRequests(getJmeterDelayBetweenRequests())
                .outputFile(JMETER_STAT_OUTPUT_DIR + "\\jmeterCurrentJavaAgentNoSiMvc4.stat.csv")
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("jmeter_" + getAppServerName() + ".nosi.nd4.CURRENT.stat.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected JMeterStatsRole createJmeterStatsCurrentJavaAgentNoSiAccRole() {
        return new JMeterStatsRole
                .Builder(getMachineId() + JMETER_STATS_CURRENT_NO_SI_ACC_ROLE_ID, getTasResolver())
                .numThreads(getJmeterNumThreads()).rampUpTime(DEF_RAMP_UP_TIME).runMinutes(DEF_TEST_RUNTIME)
                .startupDelaySeconds(DEF_TEST_STARTUP_DELAY).delayBetweenRequests(getJmeterDelayBetweenRequests())
                .outputFile(JMETER_STAT_OUTPUT_DIR + "\\jmeterCurrentJavaAgentNoSiAcc.stat.csv")
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("jmeter_" + getAppServerName() + ".acc.CURRENT.stat.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected JMeterStatsRole createJmeterStatsCurrentJavaAgentNoSiBrtmRole() {
        return new JMeterStatsRole
                .Builder(getMachineId() + JMETER_STATS_CURRENT_NO_SI_BRTM_ROLE_ID, getTasResolver())
                .numThreads(getJmeterNumThreads()).rampUpTime(DEF_RAMP_UP_TIME).runMinutes(DEF_TEST_RUNTIME)
                .startupDelaySeconds(DEF_TEST_STARTUP_DELAY).delayBetweenRequests(getJmeterDelayBetweenRequests())
                .outputFile(JMETER_STAT_OUTPUT_DIR + "\\jmeterCurrentJavaAgentNoSiBrtm.stat.csv")
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("jmeter_" + getAppServerName() + ".nosi.brtm.CURRENT.stat.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected JMeterStatsRole createJmeterStatsCurrentJavaAgentSiRole() {
        return new JMeterStatsRole
                .Builder(getMachineId() + JMETER_STATS_CURRENT_SI_ROLE_ID, getTasResolver())
                .numThreads(getJmeterNumThreads()).rampUpTime(DEF_RAMP_UP_TIME).runMinutes(DEF_TEST_RUNTIME)
                .startupDelaySeconds(DEF_TEST_STARTUP_DELAY).delayBetweenRequests(getJmeterDelayBetweenRequests())
                .outputFile(JMETER_STAT_OUTPUT_DIR + "\\jmeterCurrentJavaAgentSi.stat.csv")
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("jmeter_" + getAppServerName() + ".si.CURRENT.stat.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected JMeterStatsRole createJmeterStatsPrevJavaAgentNoSiRole() {
        return new JMeterStatsRole
                .Builder(getMachineId() + JMETER_STATS_PREV_NO_SI_ROLE_ID, getTasResolver())
                .numThreads(getJmeterNumThreads()).rampUpTime(DEF_RAMP_UP_TIME).runMinutes(DEF_TEST_RUNTIME)
                .startupDelaySeconds(DEF_TEST_STARTUP_DELAY).delayBetweenRequests(getJmeterDelayBetweenRequests())
                .outputFile(JMETER_STAT_OUTPUT_DIR + "\\jmeterPrevJavaAgentNoSi.stat.csv")
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("jmeter_" + getAppServerName() + ".nosi.PREV.stat.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected JMeterStatsRole createJmeterStatsPrevJavaAgentSiRole() {
        return new JMeterStatsRole
                .Builder(getMachineId() + JMETER_STATS_PREV_SI_ROLE_ID, getTasResolver())
                .numThreads(getJmeterNumThreads()).rampUpTime(DEF_RAMP_UP_TIME).runMinutes(DEF_TEST_RUNTIME)
                .startupDelaySeconds(DEF_TEST_STARTUP_DELAY).delayBetweenRequests(getJmeterDelayBetweenRequests())
                .outputFile(JMETER_STAT_OUTPUT_DIR + "\\jmeterPrevJavaAgentSi.stat.csv")
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("jmeter_" + getAppServerName() + ".si.PREV.stat.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    ////////////////////////////
    // DELETE JMETER
    ////////////////////////////

    protected ExecutionRole deleteJmeterRole() {
        FileModifierFlowContext deleteFlow = new FileModifierFlowContext.Builder().delete(JMETER_PARENT_INSTALL_DIR).build();
        return new ExecutionRole.Builder(getMachineId() + "_deleteJmeterRoleId").flow(FileModifierFlow.class, deleteFlow)
                .build();
    }

    ////////////////////////////
    // DELETE JMETER JMX SCRIPT
    ////////////////////////////

    protected ExecutionRole deleteJmeterJmxRole() {
        FileModifierFlowContext deleteFlow = new FileModifierFlowContext.Builder().delete(JMX_INSTALL_DIR).build();
        return new ExecutionRole.Builder(getMachineId() + "_deleteJmeterJmxRoleId").flow(FileModifierFlow.class, deleteFlow)
                .build();
    }

    ///////////////////////////////////////////
    // INCREASE NETWORK BUFFER
    ///////////////////////////////////////////

    protected void configureNetworkBuffer(TestbedMachine machine) {
        NetworkUtils.configureNetworkBuffer(machine, machineId);
    }
}
