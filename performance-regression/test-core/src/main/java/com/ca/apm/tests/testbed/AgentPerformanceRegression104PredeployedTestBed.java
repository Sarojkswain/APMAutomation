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
package com.ca.apm.tests.testbed;

import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
@TestBedDefinition
public class AgentPerformanceRegression104PredeployedTestBed extends AgentPerformanceRegression104TestBed {

    public AgentPerformanceRegression104PredeployedTestBed() {
        super();

        DEPLOY_EM = true;

        DEPLOY_TOMCAT = true;
        DEPLOY_WAS = true;
        DEPLOY_WLS = true;
        DEPLOY_IIS = true;

        PREDEPLOYED_EM = true;

        PREDEPLOYED_TOMCAT = true;
        PREDEPLOYED_WAS = true;
        PREDEPLOYED_WLS = true;
        PREDEPLOYED_IIS = true;

        PREDEPLOYED_DB_TOMCAT = true;
        PREDEPLOYED_DB_WAS = true;
        PREDEPLOYED_DB_WLS = true;
        PREDEPLOYED_DB_IIS = true;

        PREDEPLOYED_JMETER_TOMCAT = true;
        PREDEPLOYED_JMETER_WAS = true;
        PREDEPLOYED_JMETER_WLS = true;
        PREDEPLOYED_JMETER_IIS = true;
    }

}
