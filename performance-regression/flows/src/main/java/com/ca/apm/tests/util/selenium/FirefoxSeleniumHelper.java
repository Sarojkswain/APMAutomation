package com.ca.apm.tests.util.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

/*
 * code based on
 * selenium-plugin/src/main/java/com/ca/apm/systemtest/fld/plugin/selenium/FirefoxSeleniumPlugin.java
 */
public class FirefoxSeleniumHelper extends SeleniumHelperBase {

    @Override
    public void initialize() {}

    @Override
    public String startSession() {
        WebDriver driver = new FirefoxDriver();
        return super.startSession(driver, "firefox-");
    }

}
