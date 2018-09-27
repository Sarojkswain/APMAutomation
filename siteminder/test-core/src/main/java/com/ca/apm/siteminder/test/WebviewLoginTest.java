/*
 * Copyright (c) 2014 CA.  All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.siteminder.test;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.automation.utils.archive.TasArchiveFactory;
import com.ca.apm.siteminder.pages.LogoutPage;
import com.ca.apm.siteminder.pages.WebViewPage;
import com.ca.apm.siteminder.pages.WebstartPage;
import com.ca.apm.siteminder.pages.WebviewLoginPage;
import com.ca.apm.siteminder.testbed.SiteMinderTestbed;
import com.ca.tas.annotation.resource.RemoteResource;
import com.ca.tas.client.remotefile.RemoteFileTransporter;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.role.EmRole;
import com.google.common.io.Files;

public class WebviewLoginTest {

    private static final String LOG_ERROR_MSG =
            "[ERROR] [WebView] Unable to redirect response as response has already been committed";

    private static final Logger LOGGER = LoggerFactory.getLogger(WebviewLoginTest.class);

    private static final String DEFAULT_WEBVIEW_PORT = "8082";
    private static final String DEFAULT_EM_JETTY_PORT = "8081";
    private static String HOSTNAME = "localhost";
    private EnvironmentPropertyContext envProp;

    private WebDriver driver;

    @BeforeTest
    public void loadEnvProperties() throws IOException {
        envProp = new EnvironmentPropertyContextFactory().createFromSystemProperty();
        LOGGER.info("Webview URL: {}", getWebViewUrl());
        LOGGER.info("Idp redirect url: {}", getIdpRedirectUrl());
    }

    @Test
    public void webviewLoginTest() {
        driver = new FirefoxDriver();
        // Make the window large enough to have logout link visible
        driver.manage().window().setSize(new Dimension(1280, 1024));
        LOGGER.info("Trying Webview at " + getWebViewUrl());
        driver.get(getWebViewUrl());

        WebviewLoginPage loginPage = new WebviewLoginPage(driver);
        loginPage.checkLoginPageContent();
        loginPage.typeUserName("siteminder");
        loginPage.typePassword("siteminder");

        WebViewPage webViewPage = loginPage.submitLogin();
        // Siteminder doesn't redirect to webview page at the moment due to some configuration issue
        // So we try the URL again to check for successful log in
        webViewPage.tryDirectURL(getWebViewUrl());
        webViewPage.waitToLoad();
        webViewPage.verifySomeWidgetsVisible();

        LOGGER.info("Attempting log out");
        LogoutPage logoutPage = webViewPage.logout();
        LOGGER.info("Log out successful. Log in again");
        webViewPage = logoutPage.backToApp();
        webViewPage.verifySomeWidgetsVisible();

        LOGGER.info("Attempting page access");
        checkSecuredPageAccessible(driver);

        LOGGER.info("Test successful. Closing driver");
        driver.quit();
    }
    
    /**
     * Test to check that Webview log does not contain error message indicating unsuccessful redirect
     * @throws IOException
     */
    @Test(dependsOnMethods = "webviewLoginTest", alwaysRun = true)
    public void logResponseCommittedTest() throws IOException {
        String hostname = envProp.getMachineHostnameByRoleId(SiteMinderTestbed.EM_ROLE);
        String emInstallDir = envProp.getRolePropertyById(SiteMinderTestbed.EM_ROLE, EmRole.ENV_PROPERTY_INSTALL_DIR);
        String wvLog = emInstallDir + "/logs/IntroscopeWebView.log";
        
        // Get Webview log
        LOGGER.info("Get Webview log from " + wvLog);
        RemoteResource rr = RemoteResource.createFromName("", wvLog);
        Collection<RemoteResource> rrs = new ArrayList<RemoteResource>();
        rrs.add(rr);
        File tempDir = Files.createTempDir();
        String hostnameWithPort = envProp.getMachineHostnameWithPortByRoleId(SiteMinderTestbed.EM_ROLE);
        RemoteFileTransporter remoteFileTransporter = new RemoteFileTransporter("http://"+hostnameWithPort, 
            rrs, new TasArchiveFactory(), tempDir);
        try {
            remoteFileTransporter.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Check if log contains error message
        // TODO: check why the hostname is twice in path, this is a recent change
        String localWvLog = tempDir + File.separator + hostname + File.separator + hostname + File.separator + "IntroscopeWebView.log";
        File localWvLogFile = new File(localWvLog);
        String content = Files.toString(localWvLogFile, Charset.defaultCharset());
        boolean hasError = content.contains(LOG_ERROR_MSG);
        Assert.assertTrue(!hasError, "Log contains error message: " + LOG_ERROR_MSG);
    }

    /**
     * Method checks whether secured pages are accessible after login
     *
     * @param driver
     */
    private void checkSecuredPageAccessible(WebDriver driver) {
        // check webView is accessible
        driver.get(getWebViewUrl());
        WebViewPage vW2 = new WebViewPage(driver);
        vW2.waitToLoad();
        vW2.verifySomeWidgetsVisible();

        // check index of EM is accessible
        driver.get(getWebstartUrl());
        WebstartPage wsPage = new WebstartPage(driver);
        wsPage.checkContent();
        LOGGER.info("Webstart page visible");
    }

    private String getWebstartUrl() {
        String hostname = getHostName();
        String port = envProp.getRolePropertiesById("role_em").getProperty("emWebPort");
        if(port==null || port.isEmpty()) {
            port = DEFAULT_EM_JETTY_PORT;
        }
        return String.format("http://%s:%s", hostname, port);
    }

    private String getIdpRedirectUrl() {
        String hostname = getHostName();
        String port = "8443";
        return String.format("http://%s:%s/siteminderagent/forms/login.fcc", hostname, port);
    }

    private String getWebViewUrl() {
        String hostname = getHostName();
        String port = envProp.getRolePropertiesById("role_em").getProperty("wvPort");
        if(port==null || port.isEmpty())
        {
            port = DEFAULT_WEBVIEW_PORT;
        }
        return String.format("http://%s:%s/#home", hostname, port);
    }

    private String getHostName() {
        String hostname = envProp.getRolePropertiesById("role_em").getProperty("em_hostname");
        if(hostname==null || hostname.isEmpty())
        {
            if(HOSTNAME!=null && !HOSTNAME.equals("localhost")) {
                return HOSTNAME;
            }
            try {
                HOSTNAME = InetAddress.getLocalHost().getHostName();
                if(!HOSTNAME.endsWith(".ca.com")) {
                    HOSTNAME = HOSTNAME + ".ca.com";
                }
            } catch (UnknownHostException e) {
                LOGGER.warn("Hostname resolution failed. Using localhost");
            }
            hostname = HOSTNAME;
        }
        return hostname;
    }
    
    /**
     * Create screenshot if test failed. File is named by test method name.
     * @param testResult
     * @throws IOException
     */
    @AfterMethod
    public void takeScreenshotOnFailure(ITestResult testResult) throws IOException {
        if (testResult.getStatus() == ITestResult.FAILURE && driver != null) {
            File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE); 
            File destFile = new File("test-output/screenshots/" + testResult.getMethod().getMethodName() + ".jpg");
            FileUtils.copyFile(scrFile, destFile); 
            LOGGER.info("Storing screenshot into location: " + destFile.getCanonicalPath());
        }
        
    }

}
