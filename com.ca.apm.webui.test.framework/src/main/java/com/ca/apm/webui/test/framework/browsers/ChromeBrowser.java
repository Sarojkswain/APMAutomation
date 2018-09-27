package com.ca.apm.webui.test.framework.browsers;

import java.util.Properties;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.ca.apm.webui.test.framework.base.Browser;

/**
 * <code>ChromeBrowser</code> instantiates the Selenium ChromeDriver, Actions,
 * and JavascriptExecutor objects.
 * <p>
 * 
 * <i>Accessing Native WebDriver Methods</i>
 * <p>
 * 
 * Access the native methods of the Selenium objects contained within
 * <code>ChromeBrowser</code> by getting the object instances via the object
 * getters. You may then call the native methods directly.
 * <p>
 * Example
 * <p>
 * <i> MyChrome.getWebDriver().navigate().To("http://www.xanadu");<br>
 * MyChrome.getJavascriptExecutor()...<br>
 * MyChrome.getActions()...<br>
 * </i></code>
 * 
 * @since QATF2.0
 * @author whogu01
 * @copyright 2013 CA Technology, All rights reserved.
 */
public class ChromeBrowser
    extends Browser
{
    /**
     * Default Constructor.
     * 
     * @since QATF2.0
     */
    public ChromeBrowser(Properties prop)
    {
        super(prop);
        System.setProperty("webdriver.chrome.driver", this.getDriver());
        DesiredCapabilities dc = getCapabilities();
        setBrowser(new ChromeDriver(dc));
    }

    /*
     * Read the file 'browsers.property' and, based on the settings, configure
     * the webdriver's desired capabilities settings.
     */
    private DesiredCapabilities getCapabilities()
    {
        DesiredCapabilities cap = DesiredCapabilities.chrome();
        logTestCase(DEBUG, "Setting Chrome nativeEvents = \""
                           + getProperty("chr.native.events") + "\"");
        cap.setCapability("nativeEvents", Boolean
                .parseBoolean(getProperty("chr.native.events")));
        return cap;
    } // end method

    @Override
    public String getDriver()
    {
        return getProperty("chr.driver.executable.path");
    }

    @Override
    public String getName()
    {
        return kChromeBrowser;
    }

    @Override
    public void driverKiller()
    {
        try
        {

            final String KILL = "taskkill /IM ";
            String browserName = "chrome* /F /T";
            String driverName = "chromedriver* /F /T";
            // Browser
            logTestCase(DEBUG, "Killing browser process... [taskkill "
                               + browserName + "]");
            Runtime.getRuntime().exec(KILL + browserName);
            // Driver
            logTestCase(DEBUG, "Killing chromedriver.exe... [taskkill "
                               + driverName + "]");
            Runtime.getRuntime().exec(KILL + driverName);
            sleep(7000);
        } catch (Exception e)
        {} // end try..catch
    } // end method

} // end class