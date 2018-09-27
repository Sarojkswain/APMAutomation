/*
 * Copyright (c) 2017 CA. All rights reserved.
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

package com.ca.apm.test.em.telemetry;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.ca.apm.automation.action.test.LogUtils;
import com.ca.apm.test.atc.common.Utils;
import com.ca.tas.test.em.appmap.MockTelemetryServiceTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

@Tas(testBeds = @TestBed(name = MockTelemetryServiceTestbed.class, executeOn = MockTelemetryServiceTestbed.NGINX_MACHINE), owner = "SVAZD01", size = SizeType.SMALL)
@Test(groups = {"telemetry", "bat"})
public class TelemetryTest {

    public void testLogs() {
        Utils.sleep(300000);
        assertTrue(new LogUtils(MockTelemetryServiceTestbed.POST_DATA_PATH, "\\x22TestInstanceId\\x22").isKeywordInLog(),
            "Missing instance ID");
        assertTrue(new LogUtils(MockTelemetryServiceTestbed.POST_DATA_PATH, "\\x22Harvest Duration (ms)\\x22").isKeywordInLog(),
            "Missing data for metric \"Harvest Duration (ms)\"");
        assertTrue(new LogUtils(MockTelemetryServiceTestbed.POST_DATA_PATH, "\\x22Number of Historical Metrics\\x22").isKeywordInLog(),
            "Missing data for metric \"Number of Historical Metrics\"");
    }
}
