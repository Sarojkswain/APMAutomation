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
package com.ca.apm.tests.test;

import com.ca.apm.tests.testbed.SamplePerformTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Date;

import static org.testng.Assert.assertTrue;

/**
 * SampleTest class
 * <p/>
 * Test description
 */
public class SamplePerfTest extends MyTasTestNgTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamplePerfTest.class);

    public SamplePerfTest() throws Exception {
//        envProp = new EnvironmentPropertyContextFactory().createFromSystemProperty();

//        auth = new NtlmPasswordAuthentication(null, "administrator", "Lister@123");
    }

    @Tas(
            testBeds = @TestBed(name = SamplePerformTestBed.class, executeOn = SamplePerformTestBed.MACHINE_ID),
            size = SizeType.COLOSSAL, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"cluster"})
    public void sampleTest() throws Exception {
        LOGGER.info("");
        LOGGER.info("===========================================================");
        LOGGER.info("===========================================================");
        LOGGER.info("    Time : " + new Date());
        LOGGER.info("===========================================================");
        LOGGER.info("===========================================================");
        LOGGER.info("");
        assertTrue(true);
    }

}
