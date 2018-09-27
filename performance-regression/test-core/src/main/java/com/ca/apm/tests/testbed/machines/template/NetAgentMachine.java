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

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public interface NetAgentMachine {

    public static final String AGENT_PREV_NO_DI_ROLE_ID = "_prevNetAgentNoDiRoleId";
    public static final String AGENT_PREV_DI_ROLE_ID = "_prevNetAgentDiRoleId";
    public static final String AGENT_CURRENT_NO_DI_ROLE_ID = "_currentNetAgentNoDiRoleId";
    public static final String AGENT_CURRENT_NO_DI_BT_ROLE_ID = "_currentNetAgentNoDiBtRoleId";
    public static final String AGENT_CURRENT_DI_ROLE_ID = "_currentNetAgentDiRoleId";

    public static final String PERFMON_CPU_NO_AGENT_ROLE_ID = "_perfMonitorCpuNoAgentRoleId";
    public static final String PERFMON_CPU_PREV_NO_DI_ROLE_ID = "_perfMonitorCpuPrevAgentNoDiRoleId";
    public static final String PERFMON_CPU_PREV_DI_ROLE_ID = "_perfMonitorCpuPrevAgentDiRoleId";
    public static final String PERFMON_CPU_CURRENT_NO_DI_ROLE_ID = "_perfMonitorCpuCurrentAgentNoDiRoleId";
    public static final String PERFMON_CPU_CURRENT_NO_DI_BT_ROLE_ID = "_perfMonitorCpuCurrentAgentNoDiBtRoleId";
    public static final String PERFMON_CPU_CURRENT_DI_ROLE_ID = "_perfMonitorCpuCurrentAgentDiRoleId";

    public static final String PERFMON_MEM_NO_AGENT_ROLE_ID = "_perfMonitorMemNoAgentRoleId";
    public static final String PERFMON_MEM_PREV_NO_DI_ROLE_ID = "_perfMonitorMemPrevAgentNoDiRoleId";
    public static final String PERFMON_MEM_PREV_DI_ROLE_ID = "_perfMonitorMemPrevAgentDiRoleId";
    public static final String PERFMON_MEM_CURRENT_NO_DI_ROLE_ID = "_perfMonitorMemCurrentAgentNoDiRoleId";
    public static final String PERFMON_MEM_CURRENT_NO_DI_BT_ROLE_ID = "_perfMonitorMemCurrentAgentNoDiBtRoleId";
    public static final String PERFMON_MEM_CURRENT_DI_ROLE_ID = "_perfMonitorMemCurrentAgentDiRoleId";

    public static final String AGENT_PARENT_INSTALL_DIR = "c:\\sw\\wily\\netAgent";

    public static final String AGENT_STAT_OUTPUT_DIR = "C:\\automation\\test_results";
    public static final String LOGS_GATHERER_OUTPUT_DIR = "C:\\automation\\test_results\\logs";

}
