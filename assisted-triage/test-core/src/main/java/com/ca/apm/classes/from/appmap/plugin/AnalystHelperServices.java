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

package com.ca.apm.classes.from.appmap.plugin;


/**
 * Service bean to be used by plug-able/extensible
 * Analysts. Should provide necessary utils helpful
 * and needed for anyone writing a new Analyst to
 * use REST queries for metrics.
 * 
 */
public class AnalystHelperServices {

    public static final String VIRTUAL_AGENT =
        "Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Business Application Agent (Virtual)";
    public static final String APPLICATION_SERVICE = "ApplicationService";
    public static final String AVG_RESP_TIME = "Average Response Time (ms)";
    public static final String RESP_PER_INT = "Responses Per Interval";
    public static final String ERR_PER_INT = "Errors Per Interval";
    public static final String STALL_COUNT = "Stall Count";
    public static final String CONC_INVOC = "Concurrent Invocations";
}
