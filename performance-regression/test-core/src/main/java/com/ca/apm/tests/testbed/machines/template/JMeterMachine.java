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
public interface JMeterMachine {

    public static final String JMETER_NO_AGENT_ROLE_ID = "_jmeterNoAgentRoleId";
    public static final String JMETER_PREV_NO_SI_ROLE_ID = "_jmeterPrevAgentNoSiRoleId";
    public static final String JMETER_PREV_SI_ROLE_ID = "_jmeterPrevAgentSiRoleId";
    public static final String JMETER_CURRENT_NO_SI_ROLE_ID = "_jmeterCurrentAgentNoSiRoleId";
    public static final String JMETER_CURRENT_NO_SI_BT_ROLE_ID = "_jmeterCurrentAgentNoSiBtRoleId";
    public static final String JMETER_CURRENT_NO_SI_ND4_ROLE_ID = "_jmeterCurrentAgentNoSiNerdDinner4RoleId";
    public static final String JMETER_CURRENT_NO_SI_ACC_ROLE_ID = "_jmeterCurrentAgentNoSiAccRoleId";
    public static final String JMETER_CURRENT_NO_SI_BRTM_ROLE_ID = "_jmeterCurrentAgentNoSiBrtmRoleId";
    public static final String JMETER_CURRENT_SI_ROLE_ID = "_jmeterCurrentAgentSiRoleId";

    public static final String JMETER_STATS_NO_AGENT_ROLE_ID = "_jmeterStatsNoAgentRoleId";
    public static final String JMETER_STATS_PREV_NO_SI_ROLE_ID = "_jmeterStatsPrevAgentNoSiRoleId";
    public static final String JMETER_STATS_PREV_SI_ROLE_ID = "_jmeterStatsPrevAgentSiRoleId";
    public static final String JMETER_STATS_CURRENT_NO_SI_ROLE_ID = "_jmeterStatsCurrentAgentNoSiRoleId";
    public static final String JMETER_STATS_CURRENT_NO_SI_BT_ROLE_ID = "_jmeterStatsCurrentAgentNoSiBtRoleId";
    public static final String JMETER_STATS_CURRENT_NO_SI_ND4_ROLE_ID = "_jmeterStatsCurrentAgentNoSiNerdDinner4RoleId";
    public static final String JMETER_STATS_CURRENT_NO_SI_ACC_ROLE_ID = "_jmeterStatsCurrentAgentNoSiAccRoleId";
    public static final String JMETER_STATS_CURRENT_NO_SI_BRTM_ROLE_ID = "_jmeterStatsCurrentAgentNoSiBrtmRoleId";
    public static final String JMETER_STATS_CURRENT_SI_ROLE_ID = "_jmeterStatsCurrentAgentSiRoleId";

    public static final String PARAM_RAMP_UP_TIME = "RAMP.UP.TIME";
    public static final String PARAM_TEST_RUNTIME = "TEST.RUNTIME";
    public static final String PARAM_TEST_STARTUP_DELAY = "TEST.STARTUP.DELAY";

    public static final Long DEF_NUM_THREADS = 1L;
    public static final Long DEF_RAMP_UP_TIME = 0L;
    public static final Long DEF_TEST_RUNTIME = 1L;
    public static final Long DEF_DELAY_BETWEEN_REQUESTS = 0L;
    public static final Long DEF_TEST_STARTUP_DELAY = 0L;

    public static final String JMX_INSTALL_DIR = "c:\\sw\\jmx";
    public static final String JMETER_PARENT_INSTALL_DIR = "c:\\sw\\jmeter";
    public static final String JMETER_OUTPUT_DIR = "C:\\automation\\test_results";
    public static final String JMETER_LOG_CONVERTER_OUTPUT_DIR = "C:\\automation\\test_results";
    public static final String JMETER_STAT_OUTPUT_DIR = "C:\\automation\\test_results";

}
