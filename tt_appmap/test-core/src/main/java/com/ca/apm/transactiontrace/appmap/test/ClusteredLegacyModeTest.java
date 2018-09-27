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

package com.ca.apm.transactiontrace.appmap.test;

import com.ca.apm.automation.action.flow.commandline.BackgroundExecution;
import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.transactiontrace.appmap.testbed.OneMomOneCollectorNoWhereBank;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

/**
 * Test that TT EM plugin is not active in legacy mode
 */
public class ClusteredLegacyModeTest extends TasTestNgTest {

    private static final Logger logger = LoggerFactory.getLogger(ClusteredLegacyModeTest.class);

    /**
     * Test switch from TT to legacy mode
     * <ul>
     * <li>start EM in TT mode</li>
     * <li>start nowhereBank</li>
     * <li>start TT for 5 mins</li>
     * <li>verify that TT EM plugin is active by checking supportability metrics</li>
     * <li>restart EM in Legacy mode</li>
     * <li>verify that TT EM plugin is not active by checking supportability metrics</li>
     * </ul>
     */
    @Test(groups = {"appmap"})
    @Tas(testBeds = @TestBed(name = OneMomOneCollectorNoWhereBank.class, executeOn = OneMomOneCollectorNoWhereBank.MOM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "turyu01")
    public void testTtPluginIsNotActiveInToLegacyMode() throws Exception {

        // set introscope.apm.appmap.legacy.data.source=false
        final String emConfigFile =
            this.envProperties.getRolePropertyById(OneMomOneCollectorNoWhereBank.MOM_ROLE_ID,
                "emConfigFile");
        updateProperty(emConfigFile, "introscope.apm.data.agingTime", "180 MIN");
        updateProperty(emConfigFile, "introscope.apm.appmap.legacy.data.source", "false");

        // start MOM
        final ClwUtils clw =
            this.utilities.createClwUtils(OneMomOneCollectorNoWhereBank.MOM_ROLE_ID);
        final EmUtils eu = this.utilities.createEmUtils();
        eu.startLocalEm(OneMomOneCollectorNoWhereBank.MOM_ROLE_ID);

        // start nowhereBank
        startNowhereBank();

        // start TT to populate AppMap
        clw.getClwRunner().runClw(
            "trace transactions exceeding 1 ms in agents matching \".*|.*|.*\" for 300 s");

        // TT processor usage in TT mode should be non-zero
        {
            logger.info("Will keep checking TT Processor usage for the next 2 minutes...");
            String ttUsage = null;
            for (int i = 0; i < 2; i++) {
                Thread.sleep(60 * 1000);
                ttUsage =
                    clw.getMetricFromAgent(".*",
                        ".*|ApplicationTriageMap|Transaction Trace Processor:Register Usage");
            }
            Assert
                .assertNotEquals(ttUsage, "0",
                    "TT Processor usage should not be equal to 0 by now, meaning that TT EM Plugin should be active");
        }

        // stop MOM
        eu.stopLocalEm(clw.getClwRunner(), OneMomOneCollectorNoWhereBank.MOM_ROLE_ID);

        // set introscope.apm.appmap.legacy.data.source=true
        updateProperty(emConfigFile, "introscope.apm.appmap.legacy.data.source", "true");

        // start MOM
        eu.startLocalEm(OneMomOneCollectorNoWhereBank.MOM_ROLE_ID);

        // TT processor usage in legacy mode should be zero
        // Most of the times collector realizes that MOM has gone away immediately, but in a case
        // where collector just sent a collection query and is waiting for result, the collection
        // token waits for time out before release. So, we wait additional 2 minutes
        {
            logger.info("Will keep checking TT Processor usage for the next 8 minutes...");
            String ttUsage = null;
            for (int i = 0; i < 8; i++) {
                Thread.sleep(60 * 1000);
                ttUsage =
                    clw.getMetricFromAgent(".*",
                        ".*|ApplicationTriageMap|Transaction Trace Processor:Register Usage");
            }
            Assert.assertNotNull(ttUsage ,"Expected non-null value for register usage clw");
            Assert
                .assertEquals(ttUsage, "0",
                    "TT Processor usage should be equal to 0 by now, meaning that TT EM Plugin should NOT be active");
        }
    }

    /**
     * Updates existing property in config file
     */
    private static void updateProperty(String path, String existingKey, String newValue)
        throws Exception {

        final File f = new File(path);
        final List<String> lines = FileUtils.readLines(f);
        boolean found = false;

        for (int i = 0; i < lines.size(); i++) {

            final String currentLine = lines.get(i);
            if (currentLine.startsWith("#") || !currentLine.contains("=")) {
                continue;
            }

            String key = currentLine.substring(0, currentLine.indexOf("=")).trim();
            if (existingKey.equals(key)) {
                lines.set(i, key + "=" + newValue);
                found = true;
            }
        }
        if (!found) {
            throw new RuntimeException("Property " + existingKey + " was not found in file "
                + f.getCanonicalPath());
        }

        logger.info("Updating Property " + existingKey + " in file " + f.getCanonicalPath()
            + " to " + newValue);
        FileUtils.writeLines(f, lines);
    }


    private static void startNowhereBank() throws Exception {

        final String homeDir = "C:\\automation\\deployed\\agents\\noWhereBank\\App";
        final File workingDir = new File(homeDir);

        new BackgroundExecution.Builder(workingDir, homeDir + "\\01_MessagingServer.cmd").build()
            .go();
        new BackgroundExecution.Builder(workingDir, homeDir + "\\02_Banking-Engine-wily.cmd")
            .build().go();
        new BackgroundExecution.Builder(workingDir, homeDir + "\\03_Banking-Mediator-wily.cmd")
            .build().go();
        new BackgroundExecution.Builder(workingDir, homeDir + "\\04_Banking-Portal-wily.cmd")
            .build().go();
        new BackgroundExecution.Builder(workingDir, homeDir + "\\05_Banking-UI-Generator.cmd")
            .build().go();
    }
}
