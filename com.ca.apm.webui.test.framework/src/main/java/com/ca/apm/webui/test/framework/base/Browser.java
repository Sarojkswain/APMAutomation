package com.ca.apm.webui.test.framework.base;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ca.apm.webui.test.framework.interfaces.IWebDriverProvider;

/**
 * <code>Browser</code> is the abstract base class for all test browsers. Common
 * methods for all browsers are defined and implemented in this class.
 * 
 * @author whogu01
 * @since QATF2.0
 * @copyright 2013 CA Technology, All rights reserved.
 */
public abstract class Browser
    extends BaseTestObject
    implements IWebDriverProvider
{
    /**
     * Selenium WebDriver object.
     * 
     * @since QATF2.0
     */
    private WebDriver          fWebDriver;

    /**
     * Selenium Actions object.
     * 
     * @since QATF2.0
     */
    private Actions            fActions;

    /**
     * Selenium JavascriptExecutors object.
     * 
     * @since QATF2.0
     */
    private JavascriptExecutor fJavaScriptExecutor;

    /**
     * Selenium wait object.
     * 
     * @since QATF2.0
     */
    private WebDriverWait      fWait;

    /**
     * Original size of browser window upon browser instantiation.
     * 
     * @since QATF2.0
     */
    private Dimension          initialDimension;

    protected Browser(Properties prop)
    {
        mergeProperties(prop);
    }

    protected void setBrowser(WebDriver wd)
    {
        this.fWebDriver = wd;
        this.fActions = new Actions(wd);
        this.fJavaScriptExecutor = (JavascriptExecutor) wd;

        long waitValue = Long
                .parseLong(getProperty("wait.until.timeout.in.seconds").trim());
        logTestCase(TRACE, "Setting wait.until to " + waitValue + " seconds.");
        this.fWait = new WebDriverWait(wd, waitValue);

        // set general object attributes
        setObjectName(getName());
        setObjectType("browser");

        logTestCase(TRACE, getName() + " constructor entry.");
        logTestCase(DEBUG, "Instantiating " + getName() + "...");

        // set initial dimension of window
        setInitialDimension(getWd().manage().window().getSize());

        // Temp - whogu01 12/10/2013 clear cookies
        logTestCase(DEBUG, "Deleting all coookies upon startup...");
        getWd().manage().deleteAllCookies();

        // Set Browser Implicit wait.
        setImplicitWait(Long.parseLong(getProperty("implicit.wait.in.millis")
                .trim()));

        // Set Browser Script timeout.
        setScriptTimeout(Long.parseLong(getProperty("script.timeout.in.millis")
                .trim()));

        // Set Browser PageLoad timeout.
        setPageTimeout(Long.parseLong(getProperty("pageload.timeout.in.millis")
                .trim()));

        buildLoginUrl();

        // write user-agent info to log
        logTestCase(DEBUG, "UserAgent is \"" + this.getUserAgent() + "\"");
    }

    public abstract String getName();

    public abstract String getDriver();

    public abstract void driverKiller();

    @Override
    /**
     * @since QATF2.0
     */
    public Actions getActions()
    {
        return fActions;
    } // end method

    @Override
    /**
     * @since QATF2.0
     */
    public JavascriptExecutor getJavascriptExecutor()
    {
        return fJavaScriptExecutor;
    } // end method

    @Override
    /**
     * @since QATF2.0
     */
    public WebDriver getWd()
    {
        return fWebDriver;
    } // end method

    @Override
    /**
     * @since QATF2.0
     */
    public WebDriverWait getWait()
    {
        return fWait;
    } // end method

    /**
     * @returns The initial dimension of the browser window.
     * @since QATF2.0
     */
    public final Dimension getIntialDimension()
    {
        return initialDimension;
    } // end method

    /**
     * Set <code>initialDimension</code>.
     * 
     * @since QATF2.0
     */
    public final void setInitialDimension(Dimension dm)
    {
        initialDimension = dm;
    } // end method

    /**
     * Get host name.
     * 
     * @since QATF2.0
     */
    public String getHostName()
    {
        return getProperty("url.host");
    } // end method

    /**
     * set host name.
     * 
     * @since QATF2.0
     */
    public void setHostName(String hostName)
    {
        setProperty("url.host", hostName.toLowerCase().trim());

        // rebuild testUrl based on new hostName
        String testUrl = getHostProtocol() + "://" + hostName + ":"
                         + getHostPort();

        if (StringUtils.isBlank(getResourcePath()))
        {} else
        {
            testUrl = testUrl + getResourcePath();
        }
        // save testUrl
        setLoginUrl(testUrl);
    } // end method

    /**
     * Get host port.
     * 
     * @since QATF2.0
     */
    public String getHostPort()
    {
        return getProperty("url.port").trim();
    } // end method

    /**
     * Set host port.
     * 
     * @since QATF2.0
     */
    public void setHostPort(String hostPort)
    {

        setProperty("url.port", hostPort.trim());

        // Rebuild testUrl based on new hostPort value.
        String testUrl = getHostProtocol() + "://" + getHostName() + ":"
                         + hostPort;
        if (StringUtils.isBlank(getResourcePath()))
        {} else
        {
            testUrl = testUrl + getResourcePath();
        }
        // Save testUrl.
        setLoginUrl(testUrl);
    } // end method

    /**
     * Get host protocol.
     * 
     * @since QATF2.0
     */
    public String getHostProtocol()
    {
        return getProperty("url.protocol").toLowerCase();
    } // end method

    /**
     * Set host protocol.
     * 
     * @since QATF2.0
     */
    public void setHostProtocol(String protocol)
    {

        setProperty("url.protocol", protocol.toLowerCase().trim());

        // Rebuild testUrl based on new protocol value.
        String testUrl = protocol + "://" + getHostName() + ":" + getHostPort();
        if (StringUtils.isBlank(getResourcePath()))
        {} else
        {
            testUrl = testUrl + getResourcePath();
        }

        // Save testUrl.
        setLoginUrl(testUrl);
    } // end method

    /**
     * Get resource path.
     * 
     * @since QATF2.0
     */
    public String getResourcePath()
    {
        return getProperty("url.path");
    } // end method

    /**
     * Set resource path.
     * 
     * @since QATF2.0
     */
    public void setResourcePath(String resourcePath)
    {
        setProperty("url.path", resourcePath.trim());

        // Rebuild testUrl based on new hostPort value.
        String testUrl = getHostProtocol() + "://" + getHostName() + ":"
                         + getHostPort();
        if (StringUtils.isBlank(getResourcePath()))
        {} else
        {
            testUrl = testUrl + resourcePath;
        }

        // Save testUrl.
        setLoginUrl(testUrl);
    } // end method

    /**
     * Get login Url.
     * 
     * @since QATF2.0
     */
    public String getLoginUrl()
    {
        return getProperty("url.login").trim();
    } // end method

    /**
     * Set login Url.
     * 
     * @since QATF2.0
     */
    public void setLoginUrl(String loginUrl)
    {
        // this is internally used by other class methods.
        setProperty("url.login", loginUrl);
    } // end method

    /**
     * Build loginURL from launch.properties.
     * 
     * @since QATF2.0
     */
    public void buildLoginUrl()
    {
        String testUrl = getProperty("url.protocol") + "://"
                         + getProperty("url.host");
        if (StringUtils.isBlank(getProperty("url.port")))
        {} else
        {
            testUrl += ":" + getProperty("url.port");
            if (StringUtils.isBlank(getProperty("url.path")))
            {} else
            {
                testUrl += getProperty("url.path");
            }
        } // end if..else url path
        setProperty("url.login", testUrl);
    } // end method

    /*
     * GET USER AGENT
     */

    /**
     * Get the user-agent string from the browser.
     * 
     * @return The user-agent value from the WebDriver object.
     * @since QATF2.0
     */
    public String getUserAgent()
    {

        logTestCase(TRACE, "getUserAgent() method entry.");
        String ver = (String) ((JavascriptExecutor) this.getWd())
                .executeScript("return navigator.userAgent;");
        // getting rid of excessive Agent info when MSIE
        String[] temp = ver.split(".NET CLR");
        logTestCase(TRACE, "getUserAgent() method exit.");
        return temp[0];

    } // end method

    /*
     * NAVIGATE TO, BACK, FORWARD
     */

    /**
     * Navigate back in the browser history.
     * 
     * @since QATF2.0
     */
    public final void navigateBack()
    {

        logTestCase(TRACE, "navigateBack() method entry.");
        logTestCase(DEBUG, "Navigating back in browser history.");
        getWd().navigate().back();
        logTestCase(TRACE, "navigateBack() method exit.");

    } // end method

    /**
     * Navigate forward in the browser history.
     * 
     * @since QATF2.0
     */
    public final void navigateForward()
    {

        logTestCase(TRACE, "navigateForward() method entry.");
        logTestCase(DEBUG, "Navigating forward in browser history.");
        getWd().navigate().forward();
        logTestCase(TRACE, "navigateForward() method exit.");

    } // end method

    /**
     * Navigate the browser to a URL. History is preserved.
     * 
     * @param url
     *            - A fully qualified URL.
     *            <p>
     *            Example: <i>http://www.somewhere.com/index.htm</i>
     * @since QATF2.0
     */
    public final void navigateTo(String url)
    {

        logTestCase(TRACE, "navigateTo() method entry.");
        if (StringUtils.isEmpty(url))
        {
            logTestCase(ERROR, "URL parm is empty! Can't navigate.");
        } else
        {
            logTestCase(DEBUG, "Navigating browser to \"" + url + "\"");
            getWd().navigate().to(url.trim());
        } // end if..else
        logTestCase(TRACE, "navigateTo() method exit.");

    } // end method

    /*
     * EXIT / CLOSE BROWSER
     */

    /**
     * Quit the browser entirely. WebDriver objects are destroyed.
     * 
     * @since QATF2.0
     */
    public final void quit()
    {

        logTestCase(TRACE, "quit() method entry.");
        logTestCase(DEBUG, "Quitting browser.");
        getWd().quit();
        driverKiller();
        logTestCase(TRACE, "quit() method exit.");

    } // end method

    /**
     * Close the current browser window.
     * 
     * @since QATF2.0
     */
    public final void close()
    {

        logTestCase(TRACE, "close() method entry.");
        logTestCase(DEBUG, "Closing browser.");
        getWd().close();
        logTestCase(TRACE, "close() method exit.");

    } // end method

    /*
     * SET WAITS
     */

    /**
     * Set the ImplicitWait value (in milliseconds) for the WebDriver object.
     * Once set, the implicit wait is retained for the life of the Webdriver
     * object instance or until the value is changed again with another call to
     * <code>setImlicitWait</code>.
     * 
     * @param wait
     *            - The number of milliseconds that WebDriver should wait when
     *            searching for WebElements. If wait < 0 or wait > 120000, wait
     *            defaults to 60000 milliseconds.
     * @since QATF2.0
     */
    public final void setImplicitWait(long wait)
    {

        logTestCase(TRACE, "setImplicitWait() method entry.");
        if ((wait < 0) || (wait > 120000))
        {
            wait = 60000;
        }

        logTestCase(TRACE, "Setting webdriver implicitWait to " + wait + " ms.");
        getWd().manage().timeouts().implicitlyWait(wait, TimeUnit.MILLISECONDS);
        logTestCase(TRACE, "setImplicitWait() method exit.");

    } // end method

    /**
     * Set ScriptTimeout value (in milliseconds) for the WebDriver object.
     * 
     * @param wait
     *            - The number of milliseconds that WebDriver should wait when
     *            searching for WebElements. If wait < 0 or wait > 120000, wait
     *            defaults to 60000 milliseconds.
     * @since QATF2.0
     */
    public final void setScriptTimeout(long wait)
    {

        logTestCase(TRACE, "setScriptTimeout() method entry.");
        if ((wait < 0) || (wait > 120000))
        {
            wait = 60000;
        }
        logTestCase(TRACE, "Setting webdriver ScriptTimeout to " + wait
                           + " ms.");
        getWd().manage().timeouts()
                .setScriptTimeout(wait, TimeUnit.MILLISECONDS);
        logTestCase(TRACE, "setScriptTimeout() method exit.");

    } // end method

    /**
     * Set PageTimeout value (in milliseconds) for the WebDriver object.
     * 
     * @param wait
     *            - The number of milliseconds that WebDriver should wait when
     *            loading a page. If wait < 0 or wait > 120000, wait defaults to
     *            60000 milliseconds.
     * @since QATF2.0
     */
    public final void setPageTimeout(long wait)
    {

        logTestCase(TRACE, "setPageTimeout() method entry.");
        if ((wait < 0) || (wait > 120000))
        {
            wait = 60000;
        }
        logTestCase(TRACE, "Setting webdriver pageLoadTimeout to " + wait
                           + " ms.");
        getWd().manage().timeouts()
                .pageLoadTimeout(wait, TimeUnit.MILLISECONDS);
        logTestCase(TRACE, "setPageTimeout() method exit.");

    } // end method

    /*
     * BROWSER MAXIMIZE / MINIMIZE / RESTORE
     */

    /**
     * Maximize the browser window.
     * 
     * @since QATF2.0
     */
    public void maximize()
    {

        logTestCase(TRACE, "maximize() method enty.");
        logTestCase(DEBUG, "Maximizing browser size.");
        getWd().manage().window().maximize();
        logTestCase(TRACE, "maximize() method exit.");

    } // end method

    /**
     * Minimize the browser window.
     * 
     * @since QATF2.0
     */
    public void minimize()
    {

        logTestCase(TRACE, "minimize() method enty.");
        logTestCase(DEBUG, "Minimizing browser size.");
        getWd().manage().window().setSize(new Dimension(0, 0));
        logTestCase(TRACE, "maximize() method exit.");

    } // end method

    /**
     * Restore the browser to it's original window size.
     * 
     * @since QATF2.0
     */
    public void restore()
    {

        logTestCase(TRACE, "restore() method enty.");
        logTestCase(DEBUG, "Restoring browser size.");
        getWd().manage().window().setSize(getIntialDimension());
        logTestCase(TRACE, "restore() method exit.");

    } // end method

    public void exitBrowser()
    {
        restore();
        quit();
        fWebDriver = null;
        fActions = null;
        fJavaScriptExecutor = null;
        fWait = null;
    }

} // end class