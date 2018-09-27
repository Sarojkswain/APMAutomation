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
import com.ca.apm.tests.artifact.AgentTrussVersion;
import com.ca.apm.tests.role.JavaAgentRole;
import com.ca.apm.tests.role.JstatRole;
import com.ca.apm.tests.role.TypeperfRole;
import com.ca.apm.tests.testbed.utils.NetworkUtils;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.webapp.IWebAppServerRole;
import com.ca.tas.role.webapp.JavaRole;
import com.ca.tas.testbed.TestbedMachine;
import junit.framework.Assert;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public abstract class AppServerMachineAbs implements JavaAgentMachine, PerfMonMachine, JstatMachine {

    protected final String machineId;
    protected final ITasResolver tasResolver;

    protected final String sharePassword;

    protected final String agentCurrentVersion;
    protected final String agentPrevVersion;

    protected final boolean agentUseTruss;
    protected final AgentTrussVersion agentCurrentVersionTruss;
    protected final AgentTrussVersion agentPrevVersionTruss;

    public AppServerMachineAbs(String machineId, ITasResolver tasResolver, String agentCurrentVersion, String agentPrevVersion, String sharePassword) {
        Assert.assertNotNull(agentCurrentVersion);
        Assert.assertNotNull(agentPrevVersion);
        this.machineId = machineId;
        this.tasResolver = tasResolver;
        this.sharePassword = sharePassword;
        this.agentCurrentVersion = agentCurrentVersion;
        this.agentPrevVersion = agentPrevVersion;
        this.agentUseTruss = false;
        this.agentCurrentVersionTruss = null;
        this.agentPrevVersionTruss = null;
    }

    public AppServerMachineAbs(String machineId, ITasResolver tasResolver, AgentTrussVersion agentCurrentVersion, AgentTrussVersion agentPrevVersion, String sharePassword) {
        Assert.assertNotNull(agentCurrentVersion);
        Assert.assertNotNull(agentPrevVersion);
        this.machineId = machineId;
        this.tasResolver = tasResolver;
        this.sharePassword = sharePassword;
        this.agentCurrentVersion = null;
        this.agentPrevVersion = null;
        this.agentUseTruss = true;
        this.agentCurrentVersionTruss = agentCurrentVersion;
        this.agentPrevVersionTruss = agentPrevVersion;
    }

    protected String getAgentCurrentVersion() {
        return agentUseTruss ? agentCurrentVersionTruss.getVersion() : agentCurrentVersion;
    }

    protected String getAgentPrevVersion() {
        return agentUseTruss ? agentPrevVersionTruss.getVersion() : agentPrevVersion;
    }

    public String getMachineId() {
        return machineId;
    }

    public ITasResolver getTasResolver() {
        return tasResolver;
    }

    protected abstract String getAppServerName();

    protected abstract String getAppServerIdent();

    protected abstract String getAppServerNameString();

    protected abstract String getOs();

    protected abstract String getTestResultsShare();

    protected abstract String getEmHostname();

    protected abstract Long getRuntimee();

    /* ================ */
    /* ==== AGENTS ==== */
    /* ================ */

    protected JavaAgentRole createCurrentJavaAgentNoSiRole(IWebAppServerRole appServer) {
        return createCurrentJavaAgentNoSiRole(appServer, false, false);
    }

    protected JavaAgentRole createCurrentJavaAgentNoSiRole(IWebAppServerRole appServer, boolean accDefault, boolean accMockOn) {
        JavaAgentRole.Builder builder = new JavaAgentRole.Builder(getMachineId() + AGENT_CURRENT_NO_SI_ROLE_ID, getTasResolver());
        if (agentUseTruss) {
            builder.version(agentCurrentVersionTruss);
        } else {
            builder.version(getAgentCurrentVersion(), getAppServerNameString(), getOs());
        }
        return builder.agentName(getAgentCurrentVersion() + " " + getAppServerName() + " Agent no_SI")
                .emLocation(getEmHostname())
                .deploySourcesLocation(AGENT_PARENT_INSTALL_DIR + "\\currentJavaAgentNoSi")
                .undeployExistingBeforeInstall(true)
                .appServer(appServer)
                .accDefault(accDefault)
                .build();
    }

    protected JavaAgentRole createCurrentJavaAgentNoSiBtRole(IWebAppServerRole appServer, boolean accDefault, boolean accMockOn) {
        JavaAgentRole.Builder builder = new JavaAgentRole.Builder(getMachineId() + AGENT_CURRENT_NO_SI_BT_ROLE_ID, getTasResolver());
        if (agentUseTruss) {
            builder.version(agentCurrentVersionTruss);
        } else {
            builder.version(getAgentCurrentVersion(), getAppServerNameString(), getOs());
        }
        return builder.agentName(getAgentCurrentVersion() + " " + getAppServerName() + " Agent no_SI BT")
                .emLocation(getEmHostname())
                .deploySourcesLocation(AGENT_PARENT_INSTALL_DIR + "\\currentJavaAgentNoSiBt")
                .undeployExistingBeforeInstall(true)
                .appServer(appServer)
                .accDefault(accDefault)
                .accMockOn(accMockOn)
                .btOn()
                .build();
    }

    protected JavaAgentRole createCurrentJavaAgentNoSiAccRole(IWebAppServerRole appServer) {
        return createCurrentJavaAgentNoSiAccRole(appServer, false, true);
    }

    protected JavaAgentRole createCurrentJavaAgentNoSiAccRole(IWebAppServerRole appServer, boolean accDefault, boolean accMockOn) {
        JavaAgentRole.Builder builder = new JavaAgentRole.Builder(getMachineId() + AGENT_CURRENT_NO_SI_ACC_ROLE_ID, getTasResolver());
        if (agentUseTruss) {
            builder.version(agentCurrentVersionTruss);
        } else {
            builder.version(getAgentCurrentVersion(), getAppServerNameString(), getOs());
        }
        if (accDefault) {
            builder.accDefault();
            builder.accMockOn(accMockOn);
        } else {
            builder.enableAcc();
        }
        return builder
                .agentName(getAgentCurrentVersion() + " " + getAppServerName() + " Agent no_SI ACC")
                .emLocation(getEmHostname())
                .deploySourcesLocation(AGENT_PARENT_INSTALL_DIR + "\\currentJavaAgentNoSiAcc")
                .undeployExistingBeforeInstall(true)
                .appServer(appServer)
                .build();
    }

    protected JavaAgentRole createCurrentJavaAgentNoSiBrtmRole(IWebAppServerRole appServer) {
        return createCurrentJavaAgentNoSiBrtmRole(appServer, false, false);
    }

    protected JavaAgentRole createCurrentJavaAgentNoSiBrtmRole(IWebAppServerRole appServer, boolean accDefault, boolean accMockOn) {
        JavaAgentRole.Builder builder = new JavaAgentRole.Builder(getMachineId() + AGENT_CURRENT_NO_SI_BRTM_ROLE_ID, getTasResolver());
        if (agentUseTruss) {
            builder.version(agentCurrentVersionTruss);
        } else {
            builder.version(getAgentCurrentVersion(), getAppServerNameString(), getOs());
        }
        return builder.enableBrtm() // todo logic may not yet exist
                .agentName(getAgentCurrentVersion() + " " + getAppServerName() + " Agent no_SI BRTM")
                .emLocation(getEmHostname())
                .deploySourcesLocation(AGENT_PARENT_INSTALL_DIR + "\\currentJavaAgentNoSiBrtm")
                .undeployExistingBeforeInstall(true)
                .appServer(appServer)
                .accDefault(accDefault)
                .accMockOn(accMockOn)
                .build();
    }

    protected JavaAgentRole createCurrentJavaAgentSiRole(IWebAppServerRole appServer) {
        return createCurrentJavaAgentSiRole(appServer, false, false);
    }

    protected JavaAgentRole createCurrentJavaAgentSiRole(IWebAppServerRole appServer, boolean accDefault, boolean accMockOn) {
        JavaAgentRole.Builder builder = new JavaAgentRole.Builder(getMachineId() + AGENT_CURRENT_SI_ROLE_ID, getTasResolver());
        if (agentUseTruss) {
            builder.version(agentCurrentVersionTruss);
        } else {
            builder.version(getAgentCurrentVersion(), getAppServerNameString(), getOs());
        }
        return builder.enableSmartInstrumentation()
                .agentName(getAgentCurrentVersion() + " " + getAppServerName() + " Agent SI")
                .emLocation(getEmHostname())
                .deploySourcesLocation(AGENT_PARENT_INSTALL_DIR + "\\currentJavaAgentSi")
                .undeployExistingBeforeInstall(true)
                .appServer(appServer)
                .accDefault(accDefault)
                .accMockOn(accMockOn)
                .build();
    }

    protected JavaAgentRole createPrevJavaAgentNoSiRole(IWebAppServerRole appServer) {
        JavaAgentRole.Builder builder = new JavaAgentRole.Builder(getMachineId() + AGENT_PREV_NO_SI_ROLE_ID, getTasResolver());
        if (agentUseTruss) {
            builder.version(agentPrevVersionTruss);
        } else {
            builder.version(getAgentPrevVersion(), getAppServerNameString(), getOs());
        }
        return builder.agentName(getAgentPrevVersion() + " " + getAppServerName() + " Agent no_SI")
                .emLocation(getEmHostname())
                .deploySourcesLocation(AGENT_PARENT_INSTALL_DIR + "\\prevJavaAgentNoSi")
                .undeployExistingBeforeInstall(true)
                .appServer(appServer)
                .build();
    }

    protected JavaAgentRole createPrevJavaAgentSiRole(IWebAppServerRole appServer) {
        JavaAgentRole.Builder builder = new JavaAgentRole.Builder(getMachineId() + AGENT_PREV_SI_ROLE_ID, getTasResolver());
        if (agentUseTruss) {
            builder.version(agentPrevVersionTruss);
        } else {
            builder.version(getAgentPrevVersion(), getAppServerNameString(), getOs());
        }
        return builder.enableSmartInstrumentation()
                .agentName(getAgentPrevVersion() + " " + getAppServerName() + " Agent SI")
                .emLocation(getEmHostname())
                .deploySourcesLocation(AGENT_PARENT_INSTALL_DIR + "\\prevJavaAgentSi")
                .undeployExistingBeforeInstall(true)
                .appServer(appServer)
                .build();
    }

    /* ====================== */
    /* ==== PERF MONITOR ==== */
    /* ====================== */

    protected TypeperfRole createPerfMonitorNoAgentRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_NO_AGENT_ROLE_ID, getTasResolver())
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuNoAgent.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("cpu_" + getAppServerName() + ".noagent.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected TypeperfRole createPerfMonitorCurrentJavaAgentNoSiRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_CURRENT_NO_SI_ROLE_ID, getTasResolver())
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuCurrentJavaAgentNoSi.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("cpu_" + getAppServerName() + ".nosi.CURRENT.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected TypeperfRole createPerfMonitorCurrentJavaAgentNoSiBtRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_CURRENT_NO_SI_BT_ROLE_ID, getTasResolver())
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuCurrentJavaAgentNoSiBt.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("cpu_" + getAppServerName() + ".nosi.bt.CURRENT.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected TypeperfRole createPerfMonitorCurrentJavaAgentNoSiAccRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_CURRENT_NO_SI_ACC_ROLE_ID, getTasResolver())
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuCurrentJavaAgentNoSiAcc.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("cpu_" + getAppServerName() + ".acc.CURRENT.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected TypeperfRole createPerfMonitorCurrentJavaAgentNoSiBrtmRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_CURRENT_NO_SI_BRTM_ROLE_ID, getTasResolver())
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuCurrentJavaAgentNoSiBrtm.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("cpu_" + getAppServerName() + ".nosi.brtm.CURRENT.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected TypeperfRole createPerfMonitorCurrentJavaAgentSiRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_CURRENT_SI_ROLE_ID, getTasResolver())
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuCurrentJavaAgentSi.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("cpu_" + getAppServerName() + ".si.CURRENT.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected TypeperfRole createPerfMonitorPrevJavaAgentNoSiRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_PREV_NO_SI_ROLE_ID, getTasResolver())
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuPrevJavaAgentNoSi.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("cpu_" + getAppServerName() + ".nosi.PREV.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected TypeperfRole createPerfMonitorPrevJavaAgentSiRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_PREV_SI_ROLE_ID, getTasResolver())
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuPrevJavaAgentSi.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("cpu_" + getAppServerName() + ".si.PREV.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    /* =============== */
    /* ==== JSTAT ==== */
    /* =============== */

    protected JstatRole createJstatNoAgentRole(JavaRole javaRole) {
        return new JstatRole
                .Builder(getMachineId() + JSTAT_NO_AGENT_ROLE_ID, getTasResolver())
                .identString(getAppServerIdent()).javaHome(javaRole.getInstallDir())
                .outputFileName(JSTAT_OUTPUT_DIR + "\\memNoAgent.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("mem_" + getAppServerName() + ".noagent.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected JstatRole createJstatCurrentJavaAgentNoSiRole(JavaRole javaRole) {
        return new JstatRole
                .Builder(getMachineId() + JSTAT_CURRENT_NO_SI_ROLE_ID, getTasResolver())
                .identString(getAppServerIdent()).javaHome(javaRole.getInstallDir())
                .outputFileName(JSTAT_OUTPUT_DIR + "\\memCurrentJavaAgentNoSi.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("mem_" + getAppServerName() + ".nosi.CURRENT.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected JstatRole createJstatCurrentJavaAgentNoSiBtRole(JavaRole javaRole) {
        return new JstatRole
                .Builder(getMachineId() + JSTAT_CURRENT_NO_SI_BT_ROLE_ID, getTasResolver())
                .identString(getAppServerIdent()).javaHome(javaRole.getInstallDir())
                .outputFileName(JSTAT_OUTPUT_DIR + "\\memCurrentJavaAgentNoSiBt.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("mem_" + getAppServerName() + ".nosi.bt.CURRENT.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected JstatRole createJstatCurrentJavaAgentNoSiAccRole(JavaRole javaRole) {
        return new JstatRole
                .Builder(getMachineId() + JSTAT_CURRENT_NO_SI_ACC_ROLE_ID, getTasResolver())
                .identString(getAppServerIdent()).javaHome(javaRole.getInstallDir())
                .outputFileName(JSTAT_OUTPUT_DIR + "\\memCurrentJavaAgentNoSiAcc.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("mem_" + getAppServerName() + ".acc.CURRENT.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected JstatRole createJstatCurrentJavaAgentNoSiBrtmRole(JavaRole javaRole) {
        return new JstatRole
                .Builder(getMachineId() + JSTAT_CURRENT_NO_SI_BRTM_ROLE_ID, getTasResolver())
                .identString(getAppServerIdent()).javaHome(javaRole.getInstallDir())
                .outputFileName(JSTAT_OUTPUT_DIR + "\\memCurrentJavaAgentNoSiBrtm.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("mem_" + getAppServerName() + ".nosi.brtm.CURRENT.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected JstatRole createJstatCurrentJavaAgentSiRole(JavaRole javaRole) {
        return new JstatRole
                .Builder(getMachineId() + JSTAT_CURRENT_SI_ROLE_ID, getTasResolver())
                .identString(getAppServerIdent()).javaHome(javaRole.getInstallDir())
                .outputFileName(JSTAT_OUTPUT_DIR + "\\memCurrentJavaAgentSi.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("mem_" + getAppServerName() + ".si.CURRENT.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected JstatRole createJstatPrevJavaAgentNoSiRole(JavaRole javaRole) {
        return new JstatRole
                .Builder(getMachineId() + JSTAT_PREV_NO_SI_ROLE_ID, getTasResolver())
                .identString(getAppServerIdent()).javaHome(javaRole.getInstallDir())
                .outputFileName(JSTAT_OUTPUT_DIR + "\\memPrevJavaAgentNoSi.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("mem_" + getAppServerName() + ".nosi.PREV.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected JstatRole createJstatPrevJavaAgentSiRole(JavaRole javaRole) {
        return new JstatRole
                .Builder(getMachineId() + JSTAT_PREV_SI_ROLE_ID, getTasResolver())
                .identString(getAppServerIdent()).javaHome(javaRole.getInstallDir())
                .outputFileName(JSTAT_OUTPUT_DIR + "\\memPrevJavaAgentSi.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("mem_" + getAppServerName() + ".si.PREV.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    /* ======================= */
    /* ==== DELETE AGENTS ==== */
    /* ======================= */

    protected ExecutionRole deleteAgentsRole() {
        FileModifierFlowContext deleteFlow = new FileModifierFlowContext.Builder().delete(AGENT_PARENT_INSTALL_DIR).build();
        return new ExecutionRole.Builder(getMachineId() + "_deleteAgentsRoleId").flow(FileModifierFlow.class, deleteFlow)
                .build();
    }

    ///////////////////////////////////////////
    // INCREASE NETWORK BUFFER
    ///////////////////////////////////////////

    protected void configureNetworkBuffer(TestbedMachine machine) {
        NetworkUtils.configureNetworkBuffer(machine, machineId);
    }

}
