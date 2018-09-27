package com.ca.apm.webui.test.framework.browsers;

import java.util.Properties;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.ca.apm.webui.test.framework.base.Browser;

/**
 * <code>FirefoxBrowser</code> instantiates the Selenium FirefoxDriver, Actions,
 * and JavascriptExecutor objects.
 * <p>
 * 
 * <i>Accessing Native WebDriver Methods</i>
 * <p>
 * 
 * Access the native methods of the Selenium objects contained within
 * <code>FirefoxBrowser</code> by getting the object instances via the object
 * getters. You may then call the native methods directly.
 * <p>
 * Example
 * <p>
 * <i> MyFF.getWebDriver().navigate().To("http://www.xanadu");<br>
 * MyFF.getJavascriptExecutor()...<br>
 * MyFF.getActions()...<br>
 * </i></code>
 * 
 * @since QATF2.0
 * @author whogu01
 * @copyright 2013 CA Technology, All rights reserved.
 */
public class FirefoxBrowser
    extends Browser
{
    /**
     * Default Constructor. Implicit wait set to 60 seconds.
     * 
     * @since QATF2.0
     */
    public FirefoxBrowser(Properties prop)
    {
        super(prop);
        DesiredCapabilities dc = getCapabilities();
        setBrowser(new FirefoxDriver(dc));
    } // end constructor

    /*
     * Read the file 'browsers.property' and, based on the settings, configure
     * the webdriver's desired capabilities settings.
     */
    private DesiredCapabilities getCapabilities()
    {
        // 11/04/2013 whogu01 - Adding Proxy Credentials

        /*
         * Letting Firefox browser itself handle proxy authentication. Manually
         * configure browser: From cmd prompt, enter firefox -p Select 'default'
         * profile and set to automatically load at startup. In URL bar, enter
         * "about:config" and set the following.
         * network.cookie.alwaysAcceptSessionCookies = true
         * signon.autologin.proxy=true
         */
        // ProfilesIni allProfiles = new ProfilesIni();
        // FirefoxProfile profile = allProfiles.getProfile("default");
        DesiredCapabilities cap = DesiredCapabilities.firefox();
        logTestCase(DEBUG, "Setting Firefox Profile to \""
                           + getProperty("ff.profile.name") + "\"");
        cap.setCapability(FirefoxDriver.PROFILE, getProperty("ff.profile.name"));
        logTestCase(DEBUG, "Setting Firefox nativeEvents = \""
                           + getProperty("ff.native.events") + "\"");
        cap.setCapability("nativeEvents",
                          Boolean.parseBoolean(getProperty("ff.native.events")));
        return cap;
    } // end method

    @Override
    public String getName()
    {
        return kFireFoxBrowser;
    }

    @Override
    public String getDriver()
    {
        return "";
    }

    @Override
    public void driverKiller()
    {
        try
        {} catch (Exception e)
        {}
    } //
} // end class