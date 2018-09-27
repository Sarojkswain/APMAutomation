package com.ca.apm.systemtest.alertstateload.devel.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.alertstateload.devel.testbed.AlertStateLoadDataPreparationTestbed;
import com.ca.apm.systemtest.alertstateload.testbed.Constants;
import com.ca.apm.systemtest.alertstateload.util.GenerateAlerts;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

@Test
public class AlertStateLoadDataPreparationTest extends TasTestNgTest implements Constants {

    private Logger LOGGER = LoggerFactory.getLogger(AlertStateLoadDataPreparationTest.class);

    private static final long DURATION_MS_NORMAL = 15 * 60000L; // 15 min.
    private static final long DURATION_MS_ALERT = 5 * 60000L; // 5 min.
    private static final long ITERATION_COUNT =
        12 * 3600000L / (DURATION_MS_NORMAL + DURATION_MS_ALERT); // 36 ==> 12h

    @Tas(testBeds = @TestBed(name = AlertStateLoadDataPreparationTestbed.class, executeOn = ASL_TEST_MACHINE_ID), owner = "bocto01", size = SizeType.DEBUG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test
    public void alertStateLoadDataPreparationTest() throws Exception {
        LOGGER
            .info("AlertStateLoadDataPreparationTest.alertStateLoadDataPreparationTest():: entry");
        String tomcatHost =
            envProperties.getMachineHostnameByRoleId(ASL_TOMCAT_ROLE_ID);

        GenerateAlerts ga = new GenerateAlerts(DURATION_MS_NORMAL, DURATION_MS_ALERT);
        ga.setHost(tomcatHost);
        ga.runLoad(ITERATION_COUNT);
        LOGGER.info("AlertStateLoadDataPreparationTest.alertStateLoadDataPreparationTest():: exit");
    }

}
