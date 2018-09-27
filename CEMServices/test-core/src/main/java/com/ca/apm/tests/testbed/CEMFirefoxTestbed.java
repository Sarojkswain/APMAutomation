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

/**
 * Browser Agent Automation Testbed - Tomcat & Firefox
 *
 * @author gupra04
 */

package com.ca.apm.tests.testbed;

import com.ca.tas.tests.annotations.TestBedDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestBedDefinition
public class CEMFirefoxTestbed extends CEMTestBed {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(CEMFirefoxTestbed.class);

    protected void setBrowser() {
        super.browser = "firefox";
        LOGGER.info("Browser set to firefox");
    }
}
