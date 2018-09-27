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
package com.ca.apm.test.em.managementmodules;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.test.em.appmap.SimpleEmTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Contains the end-to-end testing scenarios to verify the management module clean-up activities.
 * 
 * @author skomi04
 */
@Tas(testBeds = @TestBed(name = SimpleEmTestBed.class, executeOn = "endUserMachine"), owner = "skomi04", size = SizeType.SMALL)
@Test(groups = {"managementmodules", "bat"})
public class ManagementModulesTest extends TasTestNgTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    static public String EM_CFG_MODULES_DIR = TasBuilder.WIN_SEPARATOR + "config"
        + TasBuilder.WIN_SEPARATOR + "modules" + TasBuilder.WIN_SEPARATOR;

    private String emConfigDir;
    private String emInstallDir;

    public ManagementModulesTest() {
        emConfigDir =
            envProperties.getRolePropertyById(SimpleEmTestBed.EM_ROLE_ID,
                DeployEMFlowContext.ENV_EM_CONFIG_DIR);

        emInstallDir =
            envProperties.getRolePropertyById(SimpleEmTestBed.EM_ROLE_ID,
                DeployEMFlowContext.ENV_EM_INSTALL_DIR);
    }

    /**
     * Checks whether the management modules are present or not after the fresh installation.
     * 
     * @throws Exception
     */
    @Test
    public void testManagementModulesArePresent() throws Exception {

        List<String> availableMMs =
            Arrays.asList("DefaultMM.jar", "Supportability.jar", "SystemMM.jar",
                "ADA-APM_ManagementModule.jar");

        List<String> missingMMs =
            Arrays.asList(
                // completely removed
                "SampleManagementModule.jar", "DifferentialAnalysisMM.jar",
                "TriageMapConfigurationsManagementModule.jar", "SPM_ManagementModule.jar",
                // moved to the examples directory
                "ChangeDetectorManagementModule.jar",
                "BtStats_ManagementModule.jar");

        List<String> examplesMMs =
            Arrays.asList(
                // moved to the examples directory
                "ChangeDetector" + EM_CFG_MODULES_DIR + "ChangeDetectorManagementModule.jar",
                "CEMBTStats" + EM_CFG_MODULES_DIR + "BtStats_ManagementModule.jar");

        String emModulesDir = emConfigDir + "modules";
        log.info("EM config modules directory=" + emModulesDir);

        // check that default management modules are present by default
        for (String mm : availableMMs) {
            File mmJar = new File(emModulesDir, mm);
            Assert.assertTrue(mmJar.exists(), "Missing " + mm + " in " + emModulesDir); // exists
        }

        // check that other management modules are not present by default
        for (String mm : missingMMs) {
            File mmJar = new File(emModulesDir, mm);
            Assert.assertFalse(mmJar.exists(), "Available " + mm + " in " + emModulesDir); // not
                                                                                           // exists
        }

        String emExamplesDir = emInstallDir + TasBuilder.WIN_SEPARATOR + "examples";
        log.info("EM examples directory=" + emExamplesDir);
        // check that other management modules are moved to the examples directory
        for (String mm : examplesMMs) {
            File mmJar = new File(emExamplesDir, mm);
            Assert.assertTrue(mmJar.exists(), "Missing " + mm + " in " + emExamplesDir); // exists
        }
    }

    /**
     * Checks whether the Business Segment typeviewer is present after the fresh installation.
     * 
     * @throws Exception
     */
    @Test
    public void testBusinessSegmentTypeviewerIsPresent() throws Exception {

        String xmlTypeviewerName = "businesssegment.typeviewers.xml";
        String emXmlTVDir =
            emInstallDir + TasBuilder.WIN_SEPARATOR + "ext" + TasBuilder.WIN_SEPARATOR + "xmltv";
        log.info("EM ext xmltv directory=" + emXmlTVDir);

        File xmlTVJar = new File(emXmlTVDir, xmlTypeviewerName);
        Assert.assertTrue(xmlTVJar.exists(), "Missing " + xmlTypeviewerName + " in " + emXmlTVDir); // exists
    }

    /**
     * Checks whether the 'Welcome to the APM Dashboards' dashboard is configured by default.
     * 
     * @throws Exception
     */
    @Test
    public void testDefaultAPMDashboard() throws Exception {

        String emPropertiesPathname =
            emConfigDir + TasBuilder.WIN_SEPARATOR + "IntroscopeEnterpriseManager.properties";

        log.info("EM main config Properties=" + emPropertiesPathname);

        FileInputStream fileInput = new FileInputStream(new File(emPropertiesPathname));
        Properties properties = new Properties();
        properties.load(fileInput);
        fileInput.close();

        Assert.assertEquals(properties.getProperty("introscope.workstation.dashboard.home.module"),
            "Default");
        Assert.assertEquals(
            properties.getProperty("introscope.workstation.dashboard.home.dashboard"),
            "Welcome to APM Dashboards");
    }
}
