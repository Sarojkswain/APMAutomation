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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.idp.internal.pages.AccPage;
import com.ca.apm.idp.internal.pages.LoginPage;
import com.ca.apm.test.testbed.SamlEmInternalIdpTestbed;
import com.ca.apm.test.testbed.SamlEmInternalIdpWithAccTestbedWindows;
import com.ca.apm.test.testbed.SeleniumGridMachinesFactory;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.role.EmRole;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;


@Test
@Tas(testBeds = @TestBed(executeOn = "", name = SamlEmInternalIdpWithAccTestbedWindows.class))
public abstract class InternalIdpAccTest {

    private static final Logger log = LoggerFactory.getLogger(InternalIdpAccTest.class);
    protected WebDriver driver;
    private EnvironmentPropertyContext envProp;

    @BeforeMethod
    public void initWebDriver() throws MalformedURLException {
        driver = createWebDriver();
        driver.manage().window().maximize();
        log.info("Browser window size: " + driver.manage().window().getSize()); 
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
        log.info("Creating web driver from " + seleniumGridHubUrl);
        return new RemoteWebDriver(new URL(seleniumGridHubUrl), dc);
    }

    @BeforeTest
    public void loadEnvProperties() throws IOException {
        envProp = new EnvironmentPropertyContextFactory().createFromSystemProperty();
        log.info("ACC URL: {}", getAccUrl());
        log.info("Selenium Grid Hub URL: {}", getSeleniumGridHubUrl());
    }

    /**
     * This test check whether user is redirected from ACC to internal ipd login page an
     * successfully signed in into ACC application
     */
    @Test(groups = "idpTest")
    public void successfulLoginAccTest() {
        log.info("Start successfulLoginWebViewTest");
        driver.get(getAccUrl());

        LoginPage loginPage = new LoginPage(driver);
        loginPage.checkLoginPageContent();
        loginPage.typeUserName("admin");
        loginPage.typePassword("");

        // check redirect back to acc page
        AccPage accPage = loginPage.submitLoginToAcc();
        accPage.waitToLoad();
        accPage.verifySomeWidgetsVisible();
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

    private String getAccUrl() {
        String hostname = envProp.getMachineHostnameByRoleId(SamlEmInternalIdpTestbed.ROLE_EM);
        return String.format("http://%s.ca.com:8088", hostname);
    }

    private String getSeleniumGridHubUrl() {
        String hostname = envProp.getMachineHostnameByRoleId(SeleniumGridMachinesFactory.HUB_ROLE_ID);
        return String.format("http://%s:4444/wd/hub", hostname);
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
            log.info("Storing screenshot into location: " + destFile.getCanonicalPath());
        }        
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
