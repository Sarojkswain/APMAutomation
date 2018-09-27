package com.ca.apm.webui.test.framework.browsers;

import java.util.Properties;

import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.ca.apm.webui.test.framework.base.Browser;

/**
 * <code>Msie32BitBrowser</code> instantiates the Selenium IEDriver (32-bit),
 * Actions, and JavascriptExecutor objects.
 * <p>
 * 
 * <i>Accessing Native WebDriver Methods</i>
 * <p>
 * 
 * Access the native methods of the Selenium objects contained within
 * <code>MsieBrowser</code> by getting the object instances via the object
 * getters. You may then call the native methods directly.
 * <p>
 * Example
 * <p>
 * <i> MyIE.getWebDriver().navigate().To("http://www.xanadu");<br>
 * MyIE.getJavascriptExecutor()...<br>
 * MyIE.getActions()...<br>
 * </i></code>
 * 
 * @since QATF2.0
 * @author whogu01
 * @copyright 2013 CA Technology, All rights reserved.
 */
public class Msie32BitBrowser
    extends Browser
{
    /**
     * Default Constructor.
     * 
     * @since QATF2.0
     */
    public Msie32BitBrowser(Properties prop)
    {
        super(prop);
        System.setProperty("webdriver.ie.driver", this.getDriver());
        DesiredCapabilities dc = getCapabilities();
        setBrowser(new InternetExplorerDriver(dc));

    } // end method

    /*
     * Read the file 'browsers.property' and, based on the settings, configure
     * the webdriver's desired capabilities settings.
     */
    private DesiredCapabilities getCapabilities()
    {
        DesiredCapabilities cap = DesiredCapabilities.internetExplorer();
        logTestCase(DEBUG, "Setting MSIE nativeEvents = \""
                           + getProperty("ie.native.events") + "\"");
        cap.setCapability("nativeEvents",
                          Boolean.parseBoolean(getProperty("ie.native.events")));
        logTestCase(DEBUG, "Setting MSIE enablePersistentHover = \""
                           + getProperty("ie.enable.persistent.hover") + "\"");
        cap.setCapability("enablePersistentHover", Boolean
                .parseBoolean(getProperty("ie.enable.persistent.hover")));
        logTestCase(DEBUG, "Setting MSIE ensureCleanSession = \""
                           + getProperty("ie.ensure.clean.session") + "\"");
        cap.setCapability("ie.ensureCleanSession", Boolean
                .parseBoolean(getProperty("ie.ensure.clean.session")));
        cap.setCapability("requireWindowFocus", Boolean
                          .parseBoolean(getProperty("ie.require.window.focus")));
        logTestCase(DEBUG, "Setting MSIE requireWindowFocus = \""
                           + getProperty("ie.require.window.focus") + "\"");

        return cap;
    } // end method

    @Override
    public String getDriver()
    {
        return getProperty("ie32.driver.executable.path");
    }

    @Override
    public String getName()
    {
        return kIe32Browser;
    }

    @Override
    public void driverKiller()
    {
        try
        {
            final String KILL = "taskkill /IM ";
            String driverName = "IEDriverServer* /F /T";
            // Driver
            logTestCase(DEBUG, "Killing IEDriverServer... [taskkill "
                               + driverName + "]");
            Runtime.getRuntime().exec(KILL + driverName);
            sleep(7000);
        } catch (Exception e)
        {} // end try..catch
    } // end method

} // end class