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
 * 
 * Author : KETSW01
 */
package com.ca.apm.tests.javascriptcalculators;

import com.ca.apm.tests.agentcontrollability.AgentControllabilityConstants;
import com.ca.tas.builder.TasBuilder;


public class JavaScriptCalculatorsConstants extends AgentControllabilityConstants {


    public static final String JAVASCRIPTFILES_ROLE_ID = "jSFilesRoleId";
    public static final String JAVASCRIPTFILES_ARTIFACT_VERSION = "2.0";
    public static final String JAVASCRIPTFILES_LOC_WIN = TasBuilder.WIN_SOFTWARE_LOC
        + "javaScriptFiles" + TasBuilder.WIN_SEPARATOR;
    public static final String JAVASCRIPTFILES_LOC_LINUX = TasBuilder.LINUX_SOFTWARE_LOC
        + "javaScriptFiles" + TasBuilder.LINUX_SEPARATOR;

    public static final String heapUsedPercentageJSFile = "HeapUsedPercentage.js";
    public static final String cpuAverageJSFile = "CPUAverage.js";
    public static final String heapUsedPercentageMauiJSFile = "HeapUsedPercentageMaui.js";
    public static final String cpuAverageMauiJSFile = "CPUAverageMaui.js";
    public static final String JS_280512 = "JS_280512.js";
    public static final String JS_298247 = "JS_298247.js";
    public static final String JS_298248 = "JS_298247.js";
    public static final String JS_298250 = "JS_298250.js";
    public static final String JS_298253 = "JS_298253.js";
    public static final String JS_298257 = "JS_298257.js";
    public static final String JS_300030 = "JS_300030.js";
    public static final String JS_300031 = "JS_300031.js";
    public static final String JS_300032 = "JS_300032.js";
    public static final String JS_300033 = "JS_300033.js";
    public static final String JS_300034 = "JS_300034.js";
    public static final String JS_300035_TestFluctuateCounter2 =
        "JS_300035_TestFluctuateCounter2.js";
    public static final String JS_300035_TestFluctuatingCounter1 =
        "JS_300035_TestFluctuatingCounter1.js";
    public static final String JS_300038 = "JS_300038.js";
    public static final String JS_300040 = "JS_300040.js";
    public static final String sample1AddMetric = "Sample1_AddMetric.js";
    public static final String tomcatAppSampleJspPage = "/index.jsp";
}
