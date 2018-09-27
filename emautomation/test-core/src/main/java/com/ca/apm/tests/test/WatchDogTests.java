/*
 * Copyright (c) 2014 CA. All rights reserved.
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

import static org.testng.Assert.assertTrue;

import com.ca.apm.tests.testbed.OneEmAbstractTestbed;
import com.ca.apm.tests.testbed.OneEmWindowsTestbed;
import com.ca.apm.tests.testbed.OneEmLinuxTestbed;
import com.ca.apm.tests.utils.emutils.EmBatLocalUtils;
import com.ca.apm.tests.utils.emutils.EmConfiguration;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.envproperty.EnvironmentPropertyException;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Tests of EM Watchdog
 *
 * @author turlu01
 */
public class WatchDogTests {
    private static final Logger log = LoggerFactory.getLogger(WatchDogTests.class);

    private EnvironmentPropertyContext envProps;

    @BeforeTest
    public void setUp() throws EnvironmentPropertyException, IOException {
        envProps = new EnvironmentPropertyContextFactory().createFromSystemProperty();
    }

    /**
     * Verifies EM can be started through WatchDog
     * 
     * <h5>PRECONDITIONS</h5>
     * <p>
     * <ul>
     * <li>Testbed with one running EM</li>
     * </ul>
     * </p>
     * 
     * <h5>TEST ACTIVITY</h5>
     * <p>
     * <ol>
     * <li>Stop the EM</li>
     * <li>Run Watchdog as "java -jar WatchDog.jar start"</li>
     * </ol>
     * </p>
     * 
     * <h5>EXPECTING RESULTS</h5>
     * <p>
     * <ul>
     * <li>EM is started</li>
     * <li>WatchDog.log is present in the logs folder</li>
     * <li>"startcommandissued" is present in the WatchDog.log</li>
     * </ul>
     * <p>
     * 
     * <h5>RISKS MITIGATED</h5>
     * <p>
     * This scenario runs watchdog as jar file. WatchDog.bat functionality is not tested. If EM
     * doesn't start then Wtchdog.jar is not starting it as expected
     * </p>
     * 
     * @throws Exception
     */
    @Tas(testBeds = {
            @TestBed(name = OneEmWindowsTestbed.class, executeOn = OneEmAbstractTestbed.EM_MACHINE_ID),
            @TestBed(name = OneEmLinuxTestbed.class, executeOn = OneEmAbstractTestbed.EM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "turlu01")
    @Test(groups = {"BAT"})
    public void testWatchdogStartsEM() throws Exception {
        final String emInstallDir =
            (String) envProps.getMachineProperties().get(OneEmAbstractTestbed.EM_MACHINE_ID)
                .get("emInstallDir");

        final EmConfiguration config =
            new EmConfiguration(emInstallDir, OneEmAbstractTestbed.EM_PORT);

        log.info("Stopping Local EM");
        EmBatLocalUtils.stopLocalEm(config);

        log.info("Starting EM by Watchdog");
        EmBatLocalUtils.startEmByWatchdog(emInstallDir, OneEmAbstractTestbed.EM_PORT);

        log.info("Asserting watchdog log is in the file folder");
        File watchdogLog =
            new File(emInstallDir + File.separator + "logs" + File.separator + "WatchDog.log");
        assertTrue(watchdogLog.exists(), "Watchdog log is in the logs folder");

        log.info("Asserting watchdog logs contain 'startcommandissued'");
        boolean isStartIssuedInLogs =
            FileUtils.readFileToString(watchdogLog).contains("startcommandissued");
        assertTrue(isStartIssuedInLogs, "'startcommandissued' is in watchdog log");
    }
}
