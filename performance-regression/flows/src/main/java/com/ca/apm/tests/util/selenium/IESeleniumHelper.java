package com.ca.apm.tests.util.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/*
 * code based on
 * selenium-plugin/src/main/java/com/ca/apm/systemtest/fld/plugin/selenium/IESeleniumPlugin.java
 */
public class IESeleniumHelper extends SeleniumHelperBase {

    @Override
    public void initialize() {
        // check OS
        if (!isWindowsOS()) {
            throw new UnsupportedOperationException(
                "Internet Explorer WebDriver is supported on Windows OS only");
        }
    }

    @Override
    public String startSession() {
        DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
        // capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,
        // true);
        WebDriver driver = new InternetExplorerDriver(capabilities);
        return super.startSession(driver, "ie-");
    }

}
