/*
 * Copyright (c) 2016 CA.  All rights reserved.
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
package com.ca.apm.idp.internal.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverLogLevel;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.idp.internal.pages.CemLoginPage;
import com.ca.apm.idp.internal.pages.CemPage;
import com.ca.apm.idp.internal.pages.ErrorPage;
import com.ca.apm.idp.internal.pages.LoginPage;
import com.ca.apm.idp.internal.pages.LogoutPage;
import com.ca.apm.idp.internal.pages.NotFoundPage;
import com.ca.apm.idp.internal.pages.WebViewPage;
import com.ca.apm.idp.internal.pages.WebstartPage;
import com.ca.apm.test.testbed.SamlEmInternalIdpTestbed;
import com.ca.apm.test.testbed.SeleniumGridMachinesFactory;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.role.EmRole;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;


public abstract class InternalIdpTest {

    private static final Logger log = LoggerFactory.getLogger(InternalIdpTest.class);
    private static final String DOWNLOAD_DIR = "C:\\";

    protected WebDriver driver;
    private EnvironmentPropertyContext envProp;

    public static void addCommonCaps(DesiredCapabilities cap) {
        LoggingPreferences logs = new LoggingPreferences();
        logs.enable(LogType.DRIVER, Level.INFO);
        logs.enable(LogType.BROWSER, Level.INFO);
        cap.setCapability(CapabilityType.LOGGING_PREFS, logs);
        cap.setJavascriptEnabled(true);
    }

    public static DesiredCapabilities prepFirefoxCaps() {
        final DesiredCapabilities cap = DesiredCapabilities.firefox();
        // Use old Firefox extension based driver instead of new Marionette driver.
        cap.setCapability("marionette", false);
        FirefoxProfile prof = new FirefoxProfile();
        prof.setPreference("browser.helperApps.neverAsk.openFile", "text/csv");
        prof.setPreference("browser.helperApps.neverAsk.saveToDisk", "text/csv");
        prof.setPreference("browser.download.folderList", 2);
        prof.setPreference("browser.download.dir", DOWNLOAD_DIR);
        prof.setPreference("browser.download.manager.showWhenStarting", false);
        cap.setCapability("firefox_profile", prof);
        addCommonCaps(cap);
        return cap;
    }

    public static DesiredCapabilities prepChromeCaps() {
        Map<String, Object> chromePrefs = new HashMap<>(2);
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", DOWNLOAD_DIR);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);
        options.addArguments("enable-precise-memory-info", "disable-extensions", "start-maximized",
            "disable-infobars");
        DesiredCapabilities cap = DesiredCapabilities.chrome();
        cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        cap.setCapability(ChromeOptions.CAPABILITY, options);
        addCommonCaps(cap);
        return cap;
    }

    public static DesiredCapabilities prepInternetExplorerCaps() {
        DesiredCapabilities cap = DesiredCapabilities.internetExplorer();
        cap.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
        cap.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, true);
        cap.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true);
        cap.setCapability(InternetExplorerDriver.UNEXPECTED_ALERT_BEHAVIOR,
            UnexpectedAlertBehaviour.DISMISS);
        cap.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,
            true);
        cap.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
        cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        cap.setCapability(InternetExplorerDriver.FORCE_CREATE_PROCESS, true);
        cap.setCapability(InternetExplorerDriver.IE_SWITCHES, "-private");
        cap.setCapability(InternetExplorerDriver.LOG_FILE, "iewebdriver.log");
        cap.setCapability(InternetExplorerDriver.LOG_LEVEL, InternetExplorerDriverLogLevel.TRACE);
        addCommonCaps(cap);
        return cap;
    }

    @BeforeMethod
    public void initWebDriver() throws MalformedURLException {
        driver = createWebDriver();
        driver.manage().window().maximize();
        log.info("Browser window size: {}", driver.manage().window().getSize());
    }

    /**
     * Creates a remote web driver
     * 
     * @return
     * @throws MalformedURLException
     */
    public abstract WebDriver createWebDriver() throws MalformedURLException;

    protected WebDriver createWebDriver(DesiredCapabilities dc) throws MalformedURLException {
        final String seleniumGridHubUrl = getSeleniumGridHubUrl();
        log.info("Creating web driver from {}", seleniumGridHubUrl);
        return new RemoteWebDriver(new URL(seleniumGridHubUrl), dc);
    }

    @BeforeTest
    public void loadEnvProperties() throws IOException {
        envProp = new EnvironmentPropertyContextFactory().createFromSystemProperty();
        log.info("Webview URL: {}", getWebViewUrl());
        log.info("Idp redirect url: {}", getIdpRedirectUrl());
        log.info("Selenium Grid Hub URL: {}", getSeleniumGridHubUrl());
    }

    /**
     * This test check whether user is redirected from webView to internal ipd login page an
     * successfully signed
     * into application
     *
     * @throws IOException
     */
    @Test(groups = "idpTest")
    public void successfulLoginWebViewTest() throws IOException {
        log.info("Start successfulLoginWebViewTest");
        driver.get(getWebViewUrl());

        LoginPage loginPage = new LoginPage(driver);
        loginPage.checkLoginPageContent();
        loginPage.typeUserName("admin");
        loginPage.typePassword("");

        // check wevView redirect
        WebViewPage webViewPage = loginPage.submitLogin();
        webViewPage.waitToLoad();
        webViewPage.verifySomeWidgetsVisible();

        // check logout page and return to web view
        LogoutPage logoutPage = webViewPage.logout();
        webViewPage = logoutPage.backToApp();
        webViewPage.verifySomeWidgetsVisible();

        checkSecuredPageAccessible(driver);
    }

    /**
     * Test to validate message screen after logout and navigate to Webview URL
     * <p/>
     * Once user is logged out, he should see a message and a link taking him back to application
     *
     * @throws IOException
     */
    @Test(groups = "idpTest")
    public void backToAppTest() throws IOException {
        log.info("Start backToAppTest");
        driver.get(getWebViewUrl());

        LoginPage loginPage = new LoginPage(driver);
        loginPage.checkLoginPageContent();
        loginPage.typeUserName("admin");
        loginPage.typePassword("");

        // check webView redirect
        WebViewPage webViewPage = loginPage.submitLogin();
        webViewPage.waitToLoad();
        webViewPage.verifySomeWidgetsVisible();

        // check logout page and return to web view
        LogoutPage logoutPage = webViewPage.logout();
        logoutPage.validateBackLinkVisible();
        logoutPage.backToApp();
        driver.get(getWebViewUrl());
        webViewPage = new WebViewPage(driver);
        webViewPage.waitToLoad();
        webViewPage.verifySomeWidgetsVisible();
    }

    /**
     * This test check whether user is redirected from EM webstart Page to internal idp login page
     * and successfully logged into application
     *
     * @throws IOException
     */
    @Test(groups = "idpTest")
    public void successfulLoginEMIndexTest() throws IOException {
        log.info("Start successfulLoginEMIndexTest");
        driver.get(getWebstartUrl());

        WebstartPage webstartPage = new WebstartPage(driver);
        log.info("Validate webstart content");
        webstartPage.checkContent();

        LoginPage loginPage = webstartPage.webstartClickNotLogedIn();
        loginPage.checkLoginPageContent();
        loginPage.typeUserName("admin");
        loginPage.typePassword("");

        // check wevView redirect
        webstartPage = loginPage.submitLoginToWebstart();
        webstartPage.checkContent();

        checkSecuredPageAccessible(driver);
    }

    /**
     * checks behavior of login page
     */
    @Test(groups = "idpTest")
    public void failedLoginTestNonExistentUser() {
        log.info("Start failedLoginTestNonExistentUser");
        driver.get(getWebViewUrl());

        LoginPage loginPage = new LoginPage(driver);
        loginPage.checkLoginPageContent();
        loginPage.typeUserName("NonExistentUser");
        loginPage.typePassword("");

        // check failed login page content
        LoginPage failedLoginPage = loginPage.submitLoginShouldFailed();
        failedLoginPage.verifyLoginFailed();
        assertEquals(failedLoginPage.getPageUrl(), loginPage.getPageUrl());

        checkSecuredPagesNotAccessible(driver, loginPage.getPageUrl());
    }

    /**
     * checks behavior of login page
     */
    @Test(groups = "idpTest")
    public void failedLoginBlankUser() {
        log.info("Start failedLoginBlankUser");
        driver.get(getWebViewUrl());

        LoginPage loginPage = new LoginPage(driver);
        loginPage.checkLoginPageContent();
        loginPage.typeUserName("");
        loginPage.typePassword("");

        // check failed login page content
        LoginPage failedLoginPage = loginPage.submitLoginShouldFailed();
        failedLoginPage.verifyLoginFailed();
        assertEquals(failedLoginPage.getPageUrl(), loginPage.getPageUrl());

        checkSecuredPagesNotAccessible(driver, loginPage.getPageUrl());
    }


    /**
     * checks behavior of login page
     */
    @Test(groups = "idpTest")
    public void failedLoginWrongPassword() {
        log.info("Start failedLoginWrongPassword");
        driver.get(getWebViewUrl());

        LoginPage loginPage = new LoginPage(driver);
        loginPage.checkLoginPageContent();
        loginPage.typeUserName("admin");
        loginPage.typePassword("12345");

        // check failed login page content
        LoginPage failedLoginPage = loginPage.submitLoginShouldFailed();
        failedLoginPage.verifyLoginFailed();
        assertEquals(failedLoginPage.getPageUrl(), loginPage.getPageUrl());

        checkSecuredPagesNotAccessible(driver, loginPage.getPageUrl());
    }

    /**
     * Test to validate sp metadata and internal idp files are present after installation.
     * Only one file in every directory is checked for presence
     */
    @Test(groups = "install")
    public void installationTest() {
        log.info("Start installationTest");
        String installDir =
            envProp.getRolePropertyById(SamlEmInternalIdpTestbed.ROLE_EM, EmRole
                .ENV_PROPERTY_INSTALL_DIR);
        assertNotNull(installDir);
        File idpMetadata = new File(installDir + "/config/shibboleth/metadata/idp-metadata.xml");
        assertEquals("File does not exist: " + idpMetadata, true, idpMetadata.exists());
        File idpRelyingParty = new File(installDir + "/config/shibboleth/conf/relying-party.xml");
        assertEquals("File does not exist: " + idpRelyingParty, true, idpRelyingParty.exists());
        File idpProfileHandler = new File(
            installDir + "/config/shibboleth/schema/shibboleth-2.0-idp-profile-handler.xsd");
        assertEquals("File does not exist: " + idpProfileHandler, true, idpProfileHandler.exists());
        File idpInternal = new File(installDir + "/config/shibboleth/conf/internal.xml");
        assertEquals("File does not exist: " + idpInternal, true, idpInternal.exists());
    }
    
    /**
     * Test to validate sp metadata and internal idp files are present after installation.
     * Only one file in every directory is checked for presence
     */
    @Test(groups = "install")
    public void installationMetadataTest() {
        log.info("Start installationTest");
        String installDir =
            envProp.getRolePropertyById(SamlEmInternalIdpTestbed.ROLE_EM, EmRole
                .ENV_PROPERTY_INSTALL_DIR);
        assertNotNull(installDir);
        File samlWvMetadata = new File(installDir + "/config/saml-sp-metadata.xml");
        assertEquals("File does not exist: " + samlWvMetadata, true, samlWvMetadata.exists());
        File samlWsMetadata = new File(installDir + "/config/saml-sp-webstart-metadata.xml");
        assertEquals("File does not exist: " + samlWsMetadata, true, samlWsMetadata.exists());
        File samlEmMetadata = new File(installDir + "/config/saml-sp-em-metadata.xml");
        assertEquals("File does not exist: " + samlEmMetadata, true, samlEmMetadata.exists());
        File samlAccMetadata = new File(installDir + "/config/saml-sp-acc-metadata.xml");
        assertEquals("File does not exist: " + samlAccMetadata, true, samlAccMetadata.exists());
    }

    /**
     * Method checks that webView nor the EM index is not accessible and user is redirected back to
     * idp login page
     *
     * @param driver
     * @param loginPageUrl
     */
    private void checkSecuredPagesNotAccessible(WebDriver driver, String loginPageUrl) {

        // check index of EM webview
        log.info("Validate failed login page content for direct url");
        driver.get(getWebViewUrl());
        LoginPage failedLoginPage = new LoginPage(driver);
        failedLoginPage.checkLoginPageContent();
        assertEquals(driver.getCurrentUrl(), loginPageUrl);

        // check whether webstart is not accessible from
        driver.get(getWebstartUrl());
        WebstartPage webstart = new WebstartPage(driver);
        webstart.checkContent();
        failedLoginPage = webstart.webstartClickNotLogedIn();
        failedLoginPage.checkLoginPageContent();
        assertEquals(driver.getCurrentUrl(), loginPageUrl);
    }


    /**
     * Method checks whether secured pages are accessible after login
     *
     * @param driver
     */
    private void checkSecuredPageAccessible(WebDriver driver) {
        log.info("Validate secured page accessible");
        // check webView is accessible
        log.info("Validate Webview page accessible");
        driver.get(getWebViewUrl());
        WebViewPage vW2 = new WebViewPage(driver);
        vW2.waitToLoad();
        vW2.verifySomeWidgetsVisible();

        // check index of EM is accessible
        log.info("Validate Webstart page accessible");
        driver.get(getWebstartUrl());
        WebstartPage wsPage = new WebstartPage(driver);
        wsPage.checkContent();
    }

    private String getWebViewUrl() {
        String hostname = envProp.getMachineHostnameByRoleId(SamlEmInternalIdpTestbed.ROLE_EM);
        String port =
            envProp.getRolePropertiesById(SamlEmInternalIdpTestbed.ROLE_EM).getProperty("wvPort");
        // Use URL of webview, not ATC 
        return String.format("http://%s:%s/#home;tr=0", hostname, port);
    }

    private String getNonExistentUrl() {
        String hostname = envProp.getMachineHostnameByRoleId(SamlEmInternalIdpTestbed.ROLE_EM);
        String port =
            envProp.getRolePropertiesById(SamlEmInternalIdpTestbed.ROLE_EM).getProperty("wvPort");
        return String.format("http://%s:%s/ABCDEFGHIJKL", hostname, port);
    }

    private String getIdpRedirectUrl() {
        String hostname = envProp.getMachineHostnameByRoleId(SamlEmInternalIdpTestbed.ROLE_EM);
        String port =
            envProp.getRolePropertiesById(SamlEmInternalIdpTestbed.ROLE_EM).getProperty
                ("emWebPort");
        return String.format("http://%s:%s/idp/Authn/UserPassword", hostname, port);
    }


    private String getWebstartUrl() {
        String hostname = envProp.getMachineHostnameByRoleId(SamlEmInternalIdpTestbed.ROLE_EM);
        String port =
            envProp.getRolePropertiesById(SamlEmInternalIdpTestbed.ROLE_EM).getProperty
                ("emWebPort");
        return String.format("http://%s:%s", hostname, port);
    }

    private String getDeepLinkUrl() {
        String hostname = envProp.getMachineHostnameByRoleId(SamlEmInternalIdpTestbed.ROLE_EM);
        String port =
            envProp.getRolePropertiesById(SamlEmInternalIdpTestbed.ROLE_EM).getProperty("wvPort");
        String relPath =
            "#investigator;smm=false;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257CCustom+"
                + "Metric+Host+%28Virtual%29%257CCustom+Metric+Process+%28Virtual%29%257CCustom"
                + "+Metric+Agent+%28Virtual%29%257CEnterprise+Manager";
        return String.format("http://%s:%s/%s", hostname, port, relPath);
    }

    private String getCemUrl() {
        String hostname = envProp.getMachineHostnameByRoleId(SamlEmInternalIdpTestbed.ROLE_EM);
        String port =
            envProp.getRolePropertiesById(SamlEmInternalIdpTestbed.ROLE_EM).getProperty
                ("emWebPort");
        String relPath = "cempage";
        return String.format("http://%s:%s/%s", hostname, port, relPath);
    }

    private String getSeleniumGridHubUrl() {
        String hostname = envProp.getMachineHostnameByRoleId(SeleniumGridMachinesFactory.HUB_ROLE_ID);
        return String.format("http://%s:4444/wd/hub", hostname);
    }

    /**
     * This test check whether webview user is redirected to internal ipd login page when accessing
     * deeplink.
     *
     * @throws IOException
     */
    @Test(groups = "idpTest")
    public void deepLinkWebViewTest() throws IOException {
        log.info("Start deepLinkWebViewTest");
        String deepLinkUrl = getDeepLinkUrl();
        driver.navigate().to(deepLinkUrl);

        // Validate we are at login page
        LoginPage loginPage = new LoginPage(driver);
        loginPage.checkLoginPageContent();
        loginPage.typeUserName("admin");
        loginPage.typePassword("");

        // check wevView redirect
        WebViewPage webViewPage = loginPage.submitLogin();
        webViewPage.waitToLoad();
        webViewPage.verifyInvestigatorVisible();
    }

    /**
     * This test check whether user with no groups is redirected to error page after login
     * <p/>
     * * @throws IOException
     */
    @Test(groups = "idpTest")
    public void userWithNoGroupsTest() {
        log.info("Start userWithNoGroupsTest");
        driver.get(getWebViewUrl());

        LoginPage loginPage = new LoginPage(driver);
        loginPage.checkLoginPageContent();
        loginPage.typeUserName("user");
        loginPage.typePassword("");

        // check failed login page content
        log.info("Validate failed login page content");
        loginPage.submitLoginShouldRedirectToErrPage().checkHeader();

        // check index of EM webview
        log.info("Validate failed login page content for direct url");
        driver.get(getWebViewUrl());
        new ErrorPage(driver).checkHeader();
    }

    @Test(groups = "idpTest")
    public void nonExistentUrlTest() {
        log.info("Start nonExistentUrlTest");
        driver.get(getNonExistentUrl());
        new NotFoundPage(driver).check404page();

        // check whether the secured pages are not accessible
        driver.get(getWebViewUrl());
        LoginPage loginPage = new LoginPage(driver);
        checkSecuredPagesNotAccessible(driver, loginPage.getPageUrl());
    }

    /**
     * This test check whether admin is able to login to CEM
     *
     * @throws IOException
     */
    @Test(groups = "idpTest")
    public void cemLoginTest() throws IOException {
        log.info("Start cemLoginTest");
        String cemUrl = getCemUrl();
        driver.navigate().to(cemUrl);

        // Validate we are at login page
        CemLoginPage loginPage = new CemLoginPage(driver);
        loginPage.checkLoginPageContent();
        loginPage.typeUserName("admin");
        loginPage.typePassword("");
        CemPage cemPage = loginPage.submitLogin();
        cemPage.waitToLoad();
        cemPage.checkContent();
    }


    @AfterMethod
    public void closeBrowser() {
        log.info("============= Close browser ============");
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
