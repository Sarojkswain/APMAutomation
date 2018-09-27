/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.tests.tibco.flow;

import org.apache.commons.lang.SystemUtils;

/**
 * Al the constants go in here.
 * Vashistha Singh (sinva01.ca.com)
 *
 */
public interface TibcoConstants {
    public static final String TIBCO_RV_ROLE_ID = "tibcoRV";
    public static final String TIBCO_TRA_ROLE_ID = "tibcoTRA";
    public static final String TIBCO_EMS_ROLE_ID = "tibcoEMS";
    public static final String TIBCO_BW_ROLE_ID = "tibcoBW";
    public static final String TIBCO_ADMIN_ROLE_ID = "tibcoAdmin";
    public static final String DOMAIN_UTILITY_WIN_EXECUTABLE = "domainutilitycmd.exe";
    public static final String RV_SERVICE_REG_WIN_EXECUTABLE = "rvntsreg.exe";
    public static final String TIBCO_EMS_CONFIG_DIR = "C:\\progData\\";

    public static final String PATH_SEPARATOR = SystemUtils.IS_OS_WINDOWS ? "\\" : "/";
}
