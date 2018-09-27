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
package com.ca.apm.tests.test.atc;

import static java.lang.String.format;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterSuite;

import com.ca.apm.test.atc.common.UI;
import com.ca.apm.tests.testbed.atc.SeleniumGridMachinesFactory;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.test.TasTestNgTest;

public abstract class UITest extends TasTestNgTest {

    private static final String SCREENSHOTS_FOLDER_NAME = "screenshots";

    protected final Logger log = Logger.getLogger(getClass());

    private EnvironmentPropertyContext env = null;
    private UI ui = null;
    public Method methodUnderTest = null;
    protected RemoteWebDriver driver;
    public static long IMPLICITLY_WAIT = 3;
 
    
    /**
     * Prepare for test
     * 
     * @throws Exception
     */
    public void prepareATCUI(String browserType) throws Exception {

        ChromeOptions opt = new ChromeOptions();
        fillChromeOptions(opt);
        DesiredCapabilities capabilities;

        switch (browserType) {
            case "internetexplorer": {
                capabilities = DesiredCapabilities.internetExplorer();
                break;
            }
            case "firefox": {
                capabilities = DesiredCapabilities.firefox();
                break;
            }
            case "chrome": {
                capabilities = DesiredCapabilities.chrome();
                capabilities.setCapability(ChromeOptions.CAPABILITY, opt);
                break;
            }
            default: {
                throw new IllegalArgumentException("Should be one of 'internetexplorer', 'firefox', 'chrome'");
            }
        }
        
        env = new EnvironmentPropertyContextFactory().createFromSystemProperty();
        if (env.getTestbedProperties().containsKey("chromeDriverPath")) {
            ui = createLocalUI(capabilities, env.getTestbedProperties().get("chromeDriverPath"));
        } else {
            try {
                String hostname = env.getMachineHostnameByRoleId(SeleniumGridMachinesFactory.HUB_ROLE_ID);
                ui = createRemoteUI(capabilities, String.format("http://%s:4444/wd/hub", hostname));
            } catch (IllegalStateException e) {
                // Fallback to URL from config
                log.info(env.getTestbedProperties().get("selenium.webdriverURL"));
                ui = createRemoteUI(capabilities, env.getTestbedProperties().get("selenium.webdriverURL"));
            }
        }
        String startURL = env.getTestbedProperties().get("test.applicationBaseURL");
        if (!startURL.endsWith("/")) {
            startURL += "/";
        }
        ui.setStartUrl(startURL);
    }
    
    /**
     * Fill chrome options with any options that are necessary for test class.
     * @param opt
     */
    public void fillChromeOptions(ChromeOptions opt) {
        // needed for performance tests
        opt.addArguments("--enable-precise-memory-info");
        opt.addArguments("--disable-extensions");
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
    @AfterSuite
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
            log.debug(format("Created a screenshots for test %s: %s", methodUnderTest.getName(),
                targetScreenshotFile.getAbsolutePath()));
        } catch (Exception e) {
            // be quiet
            log.warn(
                format("Unable to take a screenshots for test %s!", methodUnderTest.getName()), e);
        }
    }
    
    protected UI createLocalUI(DesiredCapabilities dc, String localDriverPath) {
        
        UI ui = new UI();

        if (dc.getBrowserName().equals(DesiredCapabilities.chrome().getBrowserName())) {
            System.setProperty("webdriver.chrome.driver", localDriverPath);
            ui.setDriver(new ChromeDriver(dc));
        } else if (dc.getBrowserName().equals(DesiredCapabilities.firefox().getBrowserName())) {
            ui.setDriver(new FirefoxDriver(dc));
        } else {
            throw new IllegalArgumentException("Invalid browser specified");
        }
        return ui;
    }

    protected UI createRemoteUI(DesiredCapabilities dc, String remoteDriverUrl) throws Exception {
        
        UI ui = new UI();

        if (dc.getBrowserName().equals("chrome") || dc.getBrowserName().equals("firefox")) {
            ui.setDriver(new RemoteWebDriver(new URL(remoteDriverUrl), dc));
        } else {
            throw new IllegalArgumentException("Invalid browser specified");
        }

        ui.getDriver().manage().window().maximize();
        ui.getDriver().manage().timeouts().implicitlyWait(IMPLICITLY_WAIT, TimeUnit.SECONDS);
        return ui;
    }
}
