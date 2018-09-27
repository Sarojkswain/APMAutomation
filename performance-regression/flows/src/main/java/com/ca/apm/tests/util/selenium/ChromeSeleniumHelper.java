package com.ca.apm.tests.util.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

/*
 * code based on
 * selenium-plugin/src/main/java/com/ca/apm/systemtest/fld/plugin/selenium/ChromeSeleniumPlugin.java
 */
public class ChromeSeleniumHelper extends SeleniumHelperBase {

    @Override
    public void initialize() {}

    @Override
    public String startSession() {
        WebDriver driver = new ChromeDriver();
        return super.startSession(driver, "chrome-");
    }

}
