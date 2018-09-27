package com.ca.apm.webui.test.framework.interfaces;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

/**
 * The <code>WebDriverConsumer</code> interface defines setter methods for
 * Selenium WebDriver objects: WebDriver, Actions, JavascriptExecutor.
 * 
 * @author whogu01
 * @since QATF2.0
 * @copyright 2013 CA Technology, All rights reserved.
 */
public interface IWebDriverConsumer
{

    /**
     * Set the current WebDriver to <code>wd</code>.
     * 
     * @param wd
     *            Selenium WebDriver object.
     * @since QATF2.0
     */
    void setWd(WebDriver wd);

    /**
     * Set the Actions object.
     * 
     * @since QATF2.0
     */
    void setActions(Actions act);

    /**
     * @return A Selenium JavascriptExecutor object.
     * @since QATF2.0
     */
    void setJavascriptExecutor(JavascriptExecutor je);

    /**
     * Set the WebDriver Wait object. Wait is used in conjunction with the
     * WebDriver to wait until an expected condition is met. Should the
     * condition not be met within the default timeout value, WebDriver abandons
     * the findElementsBy operation.
     * 
     * @since QATF2.0
     */
    void setWaitUntil(WebDriver wd, long waitValue);

} // end interface