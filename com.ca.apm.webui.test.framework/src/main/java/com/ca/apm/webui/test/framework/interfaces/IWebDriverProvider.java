package com.ca.apm.webui.test.framework.interfaces;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * The <code>WebDriverProvider</code> interface defines getter methods for
 * Selenium WebDriver objects: WebDriver, Actions, JavascriptExecutor.
 * 
 * <p>
 * Any class that instantiates Selenium objects must be able to hand off
 * references of it's Selenium objects to requesting, dependent classes. This
 * interface allows dependent classes access to the original Selenium objects.
 * 
 * 
 * @author whogu01
 * @since QATF2.0
 * @copyright 2013 CA Technology, All rights reserved.
 */
public interface IWebDriverProvider
{

    /**
     * @return A Selenium WebDriver object.
     * @since QATF2.0
     */
    WebDriver getWd();

    /**
     * @return A Selenium Actions object.
     * @since QATF2.0
     */
    Actions getActions();

    /**
     * @return A Selenium JavascriptExecutor object.
     * @since QATF2.0
     */
    JavascriptExecutor getJavascriptExecutor();

    /**
     * @return A Selenium WebDriverWait object.
     * @since QATF2.0
     */
    WebDriverWait getWait();

} // end interface