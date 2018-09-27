package com.ca.apm.tests.utility;

import com.ca.tas.builder.TasBuilder;
import org.apache.http.util.Args;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.logging.Level;

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

/**
 * Selenium details - Browser Agent
 *
 * @author - gupra04
 * 
 */

public class SeleniumDetails {

    protected static final String ieLogWindows = TasBuilder.WIN_SOFTWARE_LOC + "IEConsole.log";
    protected static final String chromeLogWindows = TasBuilder.WIN_SOFTWARE_LOC
        + "ChromeConsole.log";
    protected static final String firefoxLogWindows = TasBuilder.WIN_SOFTWARE_LOC
        + "FirefoxConsole.log";

    private String browser;
    private String browserLogFile;
    private DesiredCapabilities browserCapability;
    private WebDriver driver;
    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumDetails.class);

    private SeleniumDetails(Builder builder) {

        this.browser = builder.browser;
        this.browserLogFile = builder.browserLogFile;
        this.browserCapability = builder.browserCapability;
        this.driver = builder.driver;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public DesiredCapabilities getBrowserCapability() {
        return browserCapability;
    }

    public void setBrowserCapability(DesiredCapabilities browserCapability) {
        this.browserCapability = browserCapability;
    }

    public String getBrowserLogFile() {
        return browserLogFile;
    }

    public void setBrowserLogFile(String browserLogFile) {
        this.browserLogFile = browserLogFile;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public static class Builder {
        private String browser;
        private String browserLogFile;
        private DesiredCapabilities browserCapability;
        private WebDriver driver;

        public Builder() {}

        public SeleniumDetails build() {
            Args.notNull(this.browser, "BROWSER IS REQUIRIED");
            return new SeleniumDetails(this);
        }

        public Builder browser(String browser) {
            this.browser = browser;

            LoggingPreferences logPrefs = new LoggingPreferences();
            logPrefs.enable(LogType.BROWSER, Level.ALL);

            if (browser.contains("Chrome")) {
                // TODO: do we need to take action if file is not deleted? If yes, add - if-else
                // block
                new File(chromeLogWindows).delete();
                this.browserCapability = DesiredCapabilities.chrome();
                this.browserCapability.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

                this.browserLogFile = chromeLogWindows;

            } else if (browser.contains("InternetExplorer")) {
                this.browserCapability = DesiredCapabilities.internetExplorer();
                this.browserCapability.setCapability(
                    InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
                this.browserCapability.setCapability(InternetExplorerDriver.LOG_FILE, ieLogWindows);
                this.browserCapability.setCapability(InternetExplorerDriver.LOG_LEVEL, "DEBUG");

                this.browserLogFile = ieLogWindows;

            } else {
                this.browserCapability = DesiredCapabilities.firefox();
                this.browserCapability.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

                this.browserLogFile = firefoxLogWindows;
            }
            return this;
        }
    }
}
