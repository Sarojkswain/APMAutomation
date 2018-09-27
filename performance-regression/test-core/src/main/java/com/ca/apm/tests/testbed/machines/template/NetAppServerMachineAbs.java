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
import com.ca.apm.tests.artifact.NetAgentTrussVersion;
import com.ca.apm.tests.role.GacutilRole;
import com.ca.apm.tests.role.NetAgentRole;
import com.ca.apm.tests.role.TypeperfRole;
import com.ca.apm.tests.testbed.utils.NetworkUtils;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.testbed.TestbedMachine;
import junit.framework.Assert;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public abstract class NetAppServerMachineAbs implements NetAgentMachine, PerfMonMachine {

    protected final String machineId;
    protected final ITasResolver tasResolver;

    protected final String sharePassword;

    protected final String agentCurrentVersion;
    protected final String agentPrevVersion;

    protected final boolean agentUseTruss;
    protected final NetAgentTrussVersion agentCurrentVersionTruss;
    protected final NetAgentTrussVersion agentPrevVersionTruss;

    protected final String agentDllCurrentVersion;
    protected final String agentDllPrevVersion;

    public NetAppServerMachineAbs(String machineId, ITasResolver tasResolver, String agentCurrentVersion, String agentPrevVersion, String sharePassword) {
        this(machineId, tasResolver, agentCurrentVersion, agentPrevVersion, null, null, sharePassword);
    }

    public NetAppServerMachineAbs(String machineId, ITasResolver tasResolver, String agentCurrentVersion, String agentPrevVersion,
                                  String agentDllCurrentVersion, String agentDllPrevVersion, String sharePassword) {
        Assert.assertNotNull(agentCurrentVersion);
        Assert.assertNotNull(agentPrevVersion);
        this.machineId = machineId;
        this.tasResolver = tasResolver;
        this.sharePassword = sharePassword;
        this.agentCurrentVersion = agentCurrentVersion;
        this.agentPrevVersion = agentPrevVersion;
        this.agentDllCurrentVersion = agentDllCurrentVersion;
        this.agentDllPrevVersion = agentDllPrevVersion;
        this.agentUseTruss = false;
        this.agentCurrentVersionTruss = null;
        this.agentPrevVersionTruss = null;
    }

    public NetAppServerMachineAbs(String machineId, ITasResolver tasResolver, NetAgentTrussVersion agentCurrentVersion, NetAgentTrussVersion agentPrevVersion, String sharePassword) {
        Assert.assertNotNull(agentCurrentVersion);
        Assert.assertNotNull(agentPrevVersion);
        this.machineId = machineId;
        this.tasResolver = tasResolver;
        this.sharePassword = sharePassword;
        this.agentCurrentVersion = null;
        this.agentPrevVersion = null;
        this.agentDllCurrentVersion = null;
        this.agentDllPrevVersion = null;
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

    protected abstract String getTestResultsShare();

    protected abstract String getEmHostname();

    protected abstract Long getRuntimee();

    /* ================ */
    /* ==== AGENTS ==== */
    /* ================ */

    protected NetAgentRole createCurrentNetAgentNoDiRole(GacutilRole gacutilRole) {
        NetAgentRole.Builder builder = new NetAgentRole.Builder(getMachineId() + AGENT_CURRENT_NO_DI_ROLE_ID, getTasResolver());
        if (agentUseTruss) {
            builder.version(agentCurrentVersionTruss);
        } else {
            builder.version(getAgentCurrentVersion(), agentDllCurrentVersion, 64);
        }
        return builder.agentName(getAgentCurrentVersion() + " " + getAppServerName() + " Agent no_DI")
                .emLocation(getEmHostname())
                .deploySourcesLocation(AGENT_PARENT_INSTALL_DIR + "\\currentNetAgentNoDi")
                .gacutilPath(gacutilRole.getDeploySourcesLocation())
                .undeployExistingBeforeInstall(true)
                .build();
    }

    /**
     * (only for 10.4+)
     */
    protected NetAgentRole createCurrentNetAgentNoDiBtRole(GacutilRole gacutilRole) {
        NetAgentRole.Builder builder = new NetAgentRole.Builder(getMachineId() + AGENT_CURRENT_NO_DI_BT_ROLE_ID, getTasResolver());
        if (agentUseTruss) {
            builder.version(agentCurrentVersionTruss);
        } else {
            builder.version(getAgentCurrentVersion(), agentDllCurrentVersion, 64);
        }
        return builder.agentName(getAgentCurrentVersion() + " " + getAppServerName() + " Agent no_DI BT")
                .emLocation(getEmHostname())
                .deploySourcesLocation(AGENT_PARENT_INSTALL_DIR + "\\currentNetAgentNoDiBt")
                .gacutilPath(gacutilRole.getDeploySourcesLocation())
                .undeployExistingBeforeInstall(true)
                .btOn()
                .build();
    }

    protected NetAgentRole createCurrentNetAgentDiRole(GacutilRole gacutilRole) {
        NetAgentRole.Builder builder = new NetAgentRole.Builder(getMachineId() + AGENT_CURRENT_DI_ROLE_ID, getTasResolver());
        if (agentUseTruss) {
            builder.version(agentCurrentVersionTruss);
        } else {
            builder.version(getAgentCurrentVersion(), agentDllCurrentVersion, 64);
        }
        return builder.enableDeepInstrumentation()
                .agentName(getAgentCurrentVersion() + " " + getAppServerName() + " Agent DI")
                .emLocation(getEmHostname())
                .deploySourcesLocation(AGENT_PARENT_INSTALL_DIR + "\\currentNetAgentDi")
                .gacutilPath(gacutilRole.getDeploySourcesLocation())
                .undeployExistingBeforeInstall(true)
                .build();
    }

    protected NetAgentRole createPrevNetAgentNoDiRole(GacutilRole gacutilRole) {
        NetAgentRole.Builder builder = new NetAgentRole.Builder(getMachineId() + AGENT_PREV_NO_DI_ROLE_ID, getTasResolver());
        if (agentUseTruss) {
            builder.version(agentPrevVersionTruss);
        } else {
            builder.version(getAgentPrevVersion(), agentDllPrevVersion, 64);
        }
        return builder.agentName(getAgentPrevVersion() + " " + getAppServerName() + " Agent no_DI")
                .emLocation(getEmHostname())
                .deploySourcesLocation(AGENT_PARENT_INSTALL_DIR + "\\prevNetAgentNoDi")
                .gacutilPath(gacutilRole.getDeploySourcesLocation())
                .undeployExistingBeforeInstall(true)
                .build();
    }

    protected NetAgentRole createPrevNetAgentDiRole(GacutilRole gacutilRole) {
        NetAgentRole.Builder builder = new NetAgentRole.Builder(getMachineId() + AGENT_PREV_DI_ROLE_ID, getTasResolver());
        if (agentUseTruss) {
            builder.version(agentPrevVersionTruss);
        } else {
            builder.version(getAgentPrevVersion(), agentDllPrevVersion, 64);
        }
        return builder.enableDeepInstrumentation()
                .agentName(getAgentPrevVersion() + " " + getAppServerName() + " Agent DI")
                .emLocation(getEmHostname())
                .deploySourcesLocation(AGENT_PARENT_INSTALL_DIR + "\\prevNetAgentDi")
                .gacutilPath(gacutilRole.getDeploySourcesLocation())
                .undeployExistingBeforeInstall(true)
                .build();
    }

    /* ====================== */
    /* ==== PERF MONITOR ==== */
    /* ====================== */

    protected TypeperfRole createPerfMonitorCpuNoAgentRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_CPU_NO_AGENT_ROLE_ID, getTasResolver())
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuNoAgent.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("cpu_" + getAppServerName() + ".noagent.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected TypeperfRole createPerfMonitorCpuCurrentNetAgentNoDiRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_CPU_CURRENT_NO_DI_ROLE_ID, getTasResolver())
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuCurrentNetAgentNoDi.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("cpu_" + getAppServerName() + ".nosi.CURRENT.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    /**
     * (only for 10.4+)
     */
    protected TypeperfRole createPerfMonitorCpuCurrentNetAgentNoDiBtRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_CPU_CURRENT_NO_DI_BT_ROLE_ID, getTasResolver())
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuCurrentNetAgentNoDiBt.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("cpu_" + getAppServerName() + ".nosi.bt.CURRENT.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected TypeperfRole createPerfMonitorCpuCurrentNetAgentDiRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_CPU_CURRENT_DI_ROLE_ID, getTasResolver())
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuCurrentNetAgentDi.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("cpu_" + getAppServerName() + ".si.CURRENT.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected TypeperfRole createPerfMonitorCpuPrevNetAgentNoDiRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_CPU_PREV_NO_DI_ROLE_ID, getTasResolver())
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuPrevNetAgentNoDi.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("cpu_" + getAppServerName() + ".nosi.PREV.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected TypeperfRole createPerfMonitorCpuPrevNetAgentDiRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_CPU_PREV_DI_ROLE_ID, getTasResolver())
                .metrics(PERFMON_PROCESSOR)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\cpuPrevNetAgentDi.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("cpu_" + getAppServerName() + ".si.PREV.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }


    protected TypeperfRole createPerfMonitorMemNoAgentRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_MEM_NO_AGENT_ROLE_ID, getTasResolver())
                .metrics(PERFMON_MEMORY)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\memNoAgent.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("mem_" + getAppServerName() + ".noagent.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected TypeperfRole createPerfMonitorMemCurrentNetAgentNoDiRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_MEM_CURRENT_NO_DI_ROLE_ID, getTasResolver())
                .metrics(PERFMON_MEMORY)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\memCurrentNetAgentNoDi.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("mem_" + getAppServerName() + ".nosi.CURRENT.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    /**
     * (only for 10.4+)
     */
    protected TypeperfRole createPerfMonitorMemCurrentNetAgentNoDiBtRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_MEM_CURRENT_NO_DI_BT_ROLE_ID, getTasResolver())
                .metrics(PERFMON_MEMORY)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\memCurrentNetAgentNoDiBt.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("mem_" + getAppServerName() + ".nosi.bt.CURRENT.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected TypeperfRole createPerfMonitorMemCurrentNetAgentDiRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_MEM_CURRENT_DI_ROLE_ID, getTasResolver())
                .metrics(PERFMON_MEMORY)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\memCurrentNetAgentDi.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("mem_" + getAppServerName() + ".si.CURRENT.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected TypeperfRole createPerfMonitorMemPrevNetAgentNoDiRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_MEM_PREV_NO_DI_ROLE_ID, getTasResolver())
                .metrics(PERFMON_MEMORY)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\memPrevNetAgentNoDi.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("mem_" + getAppServerName() + ".nosi.PREV.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

    protected TypeperfRole createPerfMonitorMemPrevNetAgentDiRole() {
        return new TypeperfRole
                .Builder(getMachineId() + PERFMON_MEM_PREV_DI_ROLE_ID, getTasResolver())
                .metrics(PERFMON_MEMORY)
                .outputFileName(TYPEPERF_OUTPUT_DIR + "\\memPrevNetAgentDi.csv").runTime(getRuntimee())
                .copyResultsDestinationDir(getTestResultsShare())
                .copyResultsDestinationFileName("mem_" + getAppServerName() + ".si.PREV.csv")
                .copyResultsDestinationPassword(sharePassword)
                .build();
    }

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
