package com.ca.apm.webui.test.framework.base;

import java.util.Properties;

import com.ca.apm.webui.test.framework.browsers.ChromeBrowser;
import com.ca.apm.webui.test.framework.browsers.FirefoxBrowser;
import com.ca.apm.webui.test.framework.browsers.Msie32BitBrowser;
import com.ca.apm.webui.test.framework.browsers.Msie64BitBrowser;
import com.ca.apm.webui.test.framework.interfaces.IConstants;

/**
 * <code>BrowserFactory</code> is responsible for instantiating a browser
 * object.
 * 
 * @author mccda04
 * @since QATF2.0
 * @copyright 2014 CA Technology, All rights reserved.
 */
public final class BrowserFactory
    implements IConstants
{
    /**
     * Get a new instance of a <code>Browser</code>
     * 
     * @return A <code>Browser</code> object wrapping an instance of one of the
     *         supported Selenium WebDrivers.
     * @since QATF2.0
     */
    public static Browser getBrowser(Properties prop)
    {
        Browser br;
        String browserType = prop.getProperty("browser.type").toLowerCase()
                .trim();

        // browserType has been set to an allowed browser type at this point.
        if (browserType.equalsIgnoreCase("firefox") || browserType.equals("ff"))
        {
            br = new FirefoxBrowser(prop);
        } else if (browserType.equalsIgnoreCase("chrome"))
        {
            br = new ChromeBrowser(prop);
        } else if (browserType.toLowerCase().contains("msie32"))
        {
            br = new Msie32BitBrowser(prop);
        } else if (browserType.toLowerCase().contains("msie64"))
        {
            br = new Msie64BitBrowser(prop);
        } else
        {
            br = new FirefoxBrowser(prop);
        } // end if..else

        return br;
    }
}