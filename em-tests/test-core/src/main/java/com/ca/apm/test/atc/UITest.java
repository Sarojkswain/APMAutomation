/*
 * Copyright (c) 2015 CA. All rights reserved.
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
package com.ca.apm.test.atc;

import java.io.File;
import java.lang.reflect.Method;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.ca.apm.test.atc.common.Browser;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.testbed.atc.SeleniumGridMachinesFactory;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.test.TasTestNgTest;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.defaultString;

public abstract class UITest extends TasTestNgTest {

    private static final String SCREENSHOTS_FOLDER_NAME = "screenshots";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private EnvironmentPropertyContext env = null;
    private UI ui = null;
    private Method methodUnderTest = null;

    /**
     * Prepare for test
     * 
     * @throws Exception
     */
    @BeforeMethod(alwaysRun = true)
    public void before(Method method) throws Exception {
        this.methodUnderTest = method;

        env = new EnvironmentPropertyContextFactory().createFromSystemProperty();

        if (env.getTestbedProperties().containsKey("chromeDriverPath")) {
            DesiredCapabilities dc = prepChromeCaps();
            ui = createLocalUI(dc, env.getTestbedProperties().get("chromeDriverPath"));
        } else {
            DesiredCapabilities dc = prepCapabilities(
                defaultString(env.getTestbedPropertyById("browser"), "CHROME"));
            try {
                String hostname = env.getMachineHostnameByRoleId(SeleniumGridMachinesFactory.HUB_ROLE_ID);
                ui = createRemoteUI(dc, String.format("http://%s:4444/wd/hub", hostname));
            } catch (IllegalStateException e) {
                // Fallback to URL from config
                logger.info(env.getTestbedProperties().get("selenium.webdriverURL"));
                ui = createRemoteUI(dc, env.getTestbedProperties().get("selenium.webdriverURL"));
            }
        }
        
        String startURL = env.getTestbedProperties().get("test.applicationBaseURL");
        if (!startURL.endsWith("/")) {
            startURL += "/";
        }
        ui.setStartUrl(startURL);
    }
    
    /**
     * Getter
     * 
     * @return
     */
    public UI getUI() {
        return ui;
    }
    
    public void setUI(UI ui) {
        this.ui = ui;
    }

    /**
     * Do some cleanup after test
     */
    @AfterMethod
    public void after(ITestResult testResult) {
        if (testResult.getStatus() == ITestResult.FAILURE) {
            takeScreenshot("FAILURE");
        }
        if (ui != null) {
            ui.cleanup();
            try {
                ui.getDriver().quit();
            } catch(Exception e) {
                
            }
        }
        if (this.methodUnderTest != null) {
            this.methodUnderTest = null;
        }
    }

    /**
     * Takes a screenshot of the current browser page we see once the test step is done.
     * 
     * @param stepName The name of test step executed.
     */
    protected void takeScreenshot(String stepName) {
        // TODO pospa02: determine whether we are in debug mode
        try {
            File screenshotFile = ui.getDriver().getScreenshotAs(OutputType.FILE);
            String screenshotExtension =
                FilenameUtils.getExtension(screenshotFile.getAbsolutePath());

            File targetScreenshotFile =
                FileUtils.getFile(SCREENSHOTS_FOLDER_NAME, getClass().getSimpleName(),
                    methodUnderTest.getName() + "-" + stepName + "." + screenshotExtension);
            targetScreenshotFile.getParentFile().mkdirs();
            FileUtils.copyFile(screenshotFile, targetScreenshotFile);
            logger.debug(format("Created a screenshots for test %s: %s", methodUnderTest.getName(),
                targetScreenshotFile.getAbsolutePath()));
        } catch (Exception e) {
            // be quiet
            logger.warn(
                format("Unable to take a screenshots for test %s!", methodUnderTest.getName()), e);
        }
    }
    
    protected UI createLocalUI(DesiredCapabilities dc, String localDriverPath) {
        return UI.getLocal(dc, localDriverPath);
    }

    protected UI createRemoteUI(DesiredCapabilities dc, String remoteDriverUrl) throws Exception {
        return UI.getRemote(dc, remoteDriverUrl);
    }

    protected DesiredCapabilities prepFirefoxCaps() {
        return Browser.prepFirefoxCaps();
    }

    protected DesiredCapabilities prepChromeCaps() {
        return Browser.prepChromeCaps();
    }

    protected DesiredCapabilities prepInternetExplorerCaps() {
        return Browser.prepInternetExplorer11Caps();
    }

    protected DesiredCapabilities prepEdgeCaps() {
        return Browser.prepEdgeCaps();
    }

    protected DesiredCapabilities prepCapabilities(String browser) {
        switch (browser) {
            default:
                return Browser.prepCapabilities(browser);

            case "FIREFOX":
                return prepFirefoxCaps();

            case "CHROME":
                return prepChromeCaps();

            case "IE":
            case "IE11":
                return prepInternetExplorerCaps();

            case "EDGE":
                return prepEdgeCaps();
        }
    }
}
