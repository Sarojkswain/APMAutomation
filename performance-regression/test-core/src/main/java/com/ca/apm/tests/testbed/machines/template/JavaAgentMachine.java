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
public interface JavaAgentMachine {

    public static final String AGENT_PREV_NO_SI_ROLE_ID = "_prevAgentNoSiRoleId";
    public static final String AGENT_PREV_SI_ROLE_ID = "_prevAgentSiRoleId";
    public static final String AGENT_CURRENT_NO_SI_ROLE_ID = "_currentAgentNoSiRoleId";
    public static final String AGENT_CURRENT_NO_SI_BT_ROLE_ID = "_currentAgentNoSiBtRoleId";
    public static final String AGENT_CURRENT_NO_SI_ACC_ROLE_ID = "_currentAgentNoSiAccRoleId";
    public static final String AGENT_CURRENT_NO_SI_BRTM_ROLE_ID = "_currentAgentNoSiBrtmRoleId";
    public static final String AGENT_CURRENT_SI_ROLE_ID = "_currentAgentSiRoleId";

    public static final String PERFMON_NO_AGENT_ROLE_ID = "_perfMonitorNoAgentRoleId";
    public static final String PERFMON_PREV_NO_SI_ROLE_ID = "_perfMonitorPrevAgentNoSiRoleId";
    public static final String PERFMON_PREV_SI_ROLE_ID = "_perfMonitorPrevAgentSiRoleId";
    public static final String PERFMON_CURRENT_NO_SI_ROLE_ID = "_perfMonitorCurrentAgentNoSiRoleId";
    public static final String PERFMON_CURRENT_NO_SI_BT_ROLE_ID = "_perfMonitorCurrentAgentNoSiBtRoleId";
    public static final String PERFMON_CURRENT_NO_SI_ACC_ROLE_ID = "_perfMonitorCurrentAgentNoSiAccRoleId";
    public static final String PERFMON_CURRENT_NO_SI_BRTM_ROLE_ID = "_perfMonitorCurrentAgentNoSiBrtmRoleId";
    public static final String PERFMON_CURRENT_SI_ROLE_ID = "_perfMonitorCurrentAgentSiRoleId";

    public static final String JSTAT_NO_AGENT_ROLE_ID = "_jstatNoAgentRoleId";
    public static final String JSTAT_PREV_NO_SI_ROLE_ID = "_jstatPrevAgentNoSiRoleId";
    public static final String JSTAT_PREV_SI_ROLE_ID = "_jstatPrevAgentSiRoleId";
    public static final String JSTAT_CURRENT_NO_SI_ROLE_ID = "_jstatCurrentAgentNoSiRoleId";
    public static final String JSTAT_CURRENT_NO_SI_BT_ROLE_ID = "_jstatCurrentAgentNoSiBtRoleId";
    public static final String JSTAT_CURRENT_NO_SI_ACC_ROLE_ID = "_jstatCurrentAgentNoSiAccRoleId";
    public static final String JSTAT_CURRENT_NO_SI_BRTM_ROLE_ID = "_jstatCurrentAgentNoSiBrtmRoleId";
    public static final String JSTAT_CURRENT_SI_ROLE_ID = "_jstatCurrentAgentSiRoleId";

    public static final String JMXMON_NO_AGENT_ROLE_ID = "_jmxMonitorNoAgentRoleId";
    public static final String JMXMON_PREV_NO_SI_ROLE_ID = "_jmxMonitorPrevAgentNoSiRoleId";
    public static final String JMXMON_PREV_SI_ROLE_ID = "_jmxMonitorPrevAgentSiRoleId";
    public static final String JMXMON_CURRENT_NO_SI_ROLE_ID = "_jmxMonitorCurrentAgentNoSiRoleId";
    public static final String JMXMON_CURRENT_NO_SI_BT_ROLE_ID = "_jmxMonitorCurrentAgentNoSiBtRoleId";
    public static final String JMXMON_CURRENT_NO_SI_ACC_ROLE_ID = "_jmxMonitorCurrentAgentNoSiAccRoleId";
    public static final String JMXMON_CURRENT_SI_ROLE_ID = "_jmxMonitorCurrentAgentSiRoleId";

    public static final String AGENT_PARENT_INSTALL_DIR = "c:\\sw\\wily\\javaAgent";

    public static final String AGENT_STAT_OUTPUT_DIR = "C:\\automation\\test_results";
    public static final String LOGS_GATHERER_OUTPUT_DIR = "C:\\automation\\test_results\\logs";

}
