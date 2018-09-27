/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.tests.test;

import com.ca.apm.tests.testbed.SampleTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import org.testng.annotations.Test;
import com.ca.tas.type.SizeType;
import com.ca.tas.test.TasTestNgTest;

import java.io.IOException;

import static org.testng.Assert.assertTrue;

/**
 * Sample test class to demonstrate use of test-bed using core roles
 *
 * @author TAS (tas@ca.com)
 * @since 1.0
 */
public class SampleTest extends TasTestNgTest {

    @Tas(testBeds = @TestBed(name = SampleTestbed.class, executeOn = SampleTestbed.EM_MACHINE_ID), size = SizeType.MEDIUM, owner = "pmfkey")
    @Test(groups = { "sample" })
    public void dummyTest() throws IOException {
        assertTrue(true);
    }
}
