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
package com.ca.apm.tests.testbed.featureTestbeds;

import com.ca.apm.tests.testbed.AgentPerformanceRegression104PredeployedTestBed;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
@TestBedDefinition
public class AgentPerformanceRegressionF21462PredeployedTestBed extends AgentPerformanceRegression104PredeployedTestBed {

    public AgentPerformanceRegressionF21462PredeployedTestBed() {
        super();

        AGENT_CURRENT_VERSION = "99.99.scorp_F21462_TTonTC-SNAPSHOT";
        AGENT_PREV_VERSION = "10.4.0-SNAPSHOT";

        AGENT_USE_TRUSS_GA = false;

        DEPLOY_TOMCAT = true;
        DEPLOY_WAS = false;
        DEPLOY_WLS = false;
        DEPLOY_IIS = false;
    }

}
