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
package com.ca.apm.powerpack.sysview.tests.test;

/**
 * Defines all the test classifications supported by the Mainframe (Corvus) team.
 */
public class TestClassification {
    /**
     * BAT tests - expected to run daily.
     */
    public static final String BAT = "bat";

    /**
     * Smoke tests - expected to run weekly.
     */
    public static final String SMOKE = "smoke";

    /**
     * Full tests - expected to run bi-weekly (once per sprint).
     */
    public static final String FULL = "full";

    /**
     * Assisted tests - tests requiring manual steps / special care, not scheduled automatically.
     */
    public static final String ASSISTED = "assisted";

    /**
     * Special tests - tests with specific scheduling requirements.
     */
    public static final String SPECIAL = "special";
}

