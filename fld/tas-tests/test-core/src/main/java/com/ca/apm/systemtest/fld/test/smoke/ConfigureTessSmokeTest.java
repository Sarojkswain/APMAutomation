package com.ca.apm.systemtest.fld.test.smoke;

import java.util.List;

import org.junit.Assert;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.testbed.smoke.ConfigureTessSmokeSmallerTestbed;
import com.ca.tas.flow.tess.TessUI;
import com.ca.tas.flow.tess.reports.TessReportConfiguration;
import com.ca.tas.role.tess.ConfigureTessRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Smoke tests for {@link ConfigureTessRole}.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class ConfigureTessSmokeTest extends TasTestNgTest {

    @Tas(testBeds = @TestBed(name = ConfigureTessSmokeSmallerTestbed.class, executeOn = ConfigureTessSmokeSmallerTestbed.SELENIUM_GRID_HUB_MACHINE_ID),
            size = SizeType.BIG, owner = "sinal04")
    @Test(groups = { "conf_tess_smoke" })
    public void testSMTPHostIsConfiguredCorrectly() throws Exception {
    	String hubHost = envProperties.getMachineHostnameByRoleId(ConfigureTessSmokeSmallerTestbed.SELENIUM_GRID_HUB_ROLE_ID);
		String cemHost = envProperties.getMachineHostnameByRoleId(ConfigureTessSmokeSmallerTestbed.EM_ROLE_ID);
		String hubUrl = "http://" + hubHost + ":4444/wd/hub";
    	TessUI tessUI = null;
		try {
			tessUI = TessUI.createTessUIForFirefoxRemoteWebDriver(cemHost, 8081, "cemadmin", "quality", hubUrl);
	        tessUI.setDelay(2000L);
	        tessUI.login();

			String smtpHost = tessUI.getSMTPHost();
			Assert.assertEquals(ConfigureTessSmokeSmallerTestbed.TEST_SMTP_HOST, smtpHost);
		} finally {
			if (tessUI != null) {
				tessUI.close();
			}
		}
    }
    
    @Tas(testBeds = @TestBed(name = ConfigureTessSmokeSmallerTestbed.class, executeOn = ConfigureTessSmokeSmallerTestbed.SELENIUM_GRID_HUB_MACHINE_ID),
            size = SizeType.BIG, owner = "sinal04")
    @Test(groups = { "conf_tess_smoke" })
    public void testCEMReportHasCorrectToAddress() throws Exception {
    	String hubHost = envProperties.getMachineHostnameByRoleId(ConfigureTessSmokeSmallerTestbed.SELENIUM_GRID_HUB_ROLE_ID);
		String cemHost = envProperties.getMachineHostnameByRoleId(ConfigureTessSmokeSmallerTestbed.EM_ROLE_ID);
		String hubUrl = "http://" + hubHost + ":4444/wd/hub";
    	TessUI tessUI = null;
		try {
			tessUI = TessUI.createTessUIForFirefoxRemoteWebDriver(cemHost, 8081, "cemadmin", "quality", hubUrl);
	        tessUI.setDelay(2000L);
	        tessUI.login();
			
			List<TessReportConfiguration> reportConfigs = tessUI.listReports();
			for (TessReportConfiguration reportConf : reportConfigs) {
				Assert.assertNotNull(reportConf.schedule);
				Assert.assertEquals(ConfigureTessSmokeSmallerTestbed.TEST_REPORT_EMAIL, reportConf.schedule.toAddress);
			}
		} finally {
			if (tessUI != null) {
				tessUI.close();
			}
		}
    }

    @Tas(testBeds = @TestBed(name = ConfigureTessSmokeSmallerTestbed.class,
        executeOn = ConfigureTessSmokeSmallerTestbed.SELENIUM_GRID_HUB_MACHINE_ID), size = SizeType.BIG,
        owner = "haiva04")
    @Test(groups = { "conf_tess_smoke" })
    public void testRttmConfiguration() throws Exception {
        String hubHost = envProperties.getMachineHostnameByRoleId(ConfigureTessSmokeSmallerTestbed.SELENIUM_GRID_HUB_ROLE_ID);
        String cemHost = envProperties.getMachineHostnameByRoleId(ConfigureTessSmokeSmallerTestbed.EM_ROLE_ID);
        String hubUrl = "http://" + hubHost + ":4444/wd/hub";
        TessUI tessUI = null;
        try {
            tessUI = TessUI.createTessUIForFirefoxRemoteWebDriver(cemHost, 8081, "cemadmin", "quality", hubUrl);
            tessUI.setDelay(2000L);
            tessUI.login();

            tessUI.configureRttm();
        } finally {
            if (tessUI != null) {
                tessUI.close();
            }
        }
    }
}
